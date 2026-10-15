"""Offline regression checks for the actual pinned artifact provisioning path."""
import hashlib
import json
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET

import fetch_pinned_modrinth as mirror


class PinnedModrinthTest(unittest.TestCase):
    def fixture(self, root):
        records = json.loads(mirror.LOCK.read_text(encoding='utf-8'))
        payloads = {}
        for record in records:
            jar = ('fixture:' + record['artifact']).encode()
            payloads[record['jar']['url']] = jar
            record['jar'].update(bytes=len(jar), sha256=hashlib.sha256(jar).hexdigest())
            pom = mirror.LOCK.parent / record['pom']['file']
            (root / pom.name).write_bytes(pom.read_bytes())
        lock = root / 'lock.json'
        lock.write_text(json.dumps(records), encoding='utf-8')
        return lock, records, payloads

    def test_exact_coordinates_and_original_pom_bytes(self):
        records = json.loads(mirror.LOCK.read_text(encoding='utf-8'))
        self.assertEqual({(r['artifact'], r['version']) for r in records},
                         {('iris', '1.8.14-beta.1+1.21.1-neoforge'), ('modernfix', '4SPHwiDB')})
        ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
        for record in records:
            pom = mirror.LOCK.parent / record['pom']['file']
            mirror.verify(pom, record['pom'])
            root = ET.parse(pom).getroot()
            for field, key in [('groupId', 'group'), ('artifactId', 'artifact'), ('version', 'version')]:
                self.assertEqual(root.findtext('m:' + field, namespaces=ns), record[key])
            self.assertFalse(root.findall('m:dependencies/m:dependency', ns))
            self.assertTrue(record['jar']['url'].startswith('https://cdn.modrinth.com/data/'))

    def test_success_preserves_group_artifact_version_filenames(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            lock, records, payloads = self.fixture(root)
            repo = root / 'repo'
            fetch = lambda url, output: output.write_bytes(payloads[url])
            self.assertEqual(mirror.provision(repo, fetch, lock), 2)
            for r in records:
                directory = repo / 'maven/modrinth' / r['artifact'] / r['version']
                stem = r['artifact'] + '-' + r['version']
                self.assertEqual((directory / (stem + '.jar')).read_bytes(), payloads[r['jar']['url']])
                self.assertEqual((directory / (stem + '.pom')).read_bytes(),
                                 (root / r['pom']['file']).read_bytes())

    def test_corruption_or_download_failure_publishes_nothing(self):
        for artifact in ['iris', 'modernfix']:
            for failure in ['jar', 'pom', 'network']:
                with self.subTest(artifact=artifact, failure=failure), tempfile.TemporaryDirectory() as tmp:
                    root = Path(tmp)
                    lock, records, payloads = self.fixture(root)
                    selected = next(r for r in records if r['artifact'] == artifact)
                    if failure == 'pom':
                        (root / selected['pom']['file']).write_bytes(b'corrupt')
                    def fetch(url, output):
                        if url == selected['jar']['url'] and failure == 'network':
                            raise OSError('fixture network failure')
                        content = payloads[url]
                        if url == selected['jar']['url'] and failure == 'jar':
                            content += b'corrupt'
                        output.write_bytes(content)
                    repo = root / 'repo'
                    with self.assertRaises((ValueError, OSError)):
                        mirror.provision(repo, fetch, lock)
                    self.assertEqual(list(repo.rglob('*.jar')), [])
                    self.assertEqual(list(repo.rglob('*.pom')), [])


if __name__ == '__main__':
    unittest.main(verbosity=2)
