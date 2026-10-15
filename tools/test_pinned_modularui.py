"""Exercise the actual CI shell step with deterministic local download fixtures."""
from pathlib import Path
import hashlib
import os
import re
import shutil
import subprocess
import tempfile
import textwrap
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'modularui-mc1.21.1'
STAMP = '3.3.1-20260718.084355-4'
SUFFIXES = ['.jar', '-sources.jar', '.module', '.pom', '-interfaceinjection.json']


def workflow_step():
    workflow = (ROOT / '.github/workflows/build.yml').read_text(encoding='utf-8')
    marker = '      - name: Fetch the pinned ModularUI snapshot\n        run: |\n'
    return textwrap.dedent(workflow.split(marker, 1)[1].split('\n      - name:', 1)[0])


class PinnedSnapshotTest(unittest.TestCase):
    def run_step(self, corrupt=None, corrupt_metadata=False):
        with tempfile.TemporaryDirectory(prefix='mui-ci-test-') as temporary:
            root = Path(temporary)
            fixtures = root / 'fixtures'
            fixtures.mkdir()
            payloads = {PREFIX + '-' + STAMP + suffix: ('fixture:' + suffix).encode()
                        for suffix in SUFFIXES}
            for name, content in payloads.items():
                (fixtures / name).write_bytes(content + (b'corrupt' if name == corrupt else b''))
            # Upstream metadata is mutable, so deliberately advertise a different build.
            (fixtures / 'maven-metadata.xml').write_text(
                '<metadata><versioning><snapshot><timestamp>20990101.000000</timestamp>'
                '<buildNumber>999</buildNumber></snapshot></versioning></metadata>',
                encoding='utf-8')
            script = workflow_step()
            # Substitute only artifact digests/sizes for local fixture bytes. The shell
            # verification code and committed metadata digest remain unmodified.
            pattern = r'(verify_sha256\s+\\\s*\n\s*)"([0-9a-f]{64})"(\s+\\\s*\n\s*)"([^"]+)"'
            def fixture_digest(match):
                filename = match[4].rsplit('/', 1)[-1].replace('$MUI_PREFIX', PREFIX).replace('$MUI_TIMESTAMP', STAMP)
                if filename not in payloads:
                    return match[0]
                return match[1] + '"' + hashlib.sha256(payloads[filename]).hexdigest() + '"' + match[3] + '"' + match[4] + '"'
            script = re.sub(pattern, fixture_digest, script)
            script = script.replace('-eq 1761336', '-eq ' + str(len(payloads[PREFIX + '-' + STAMP + '.jar'])))
            script = script.replace('-eq 806832', '-eq ' + str(len(payloads[PREFIX + '-' + STAMP + '-sources.jar'])))
            pinned = ROOT / '.github/dependencies/modularui-maven-metadata.xml'
            if pinned.exists():
                destination = root / '.github/dependencies/modularui-maven-metadata.xml'
                destination.parent.mkdir(parents=True)
                destination.write_bytes(pinned.read_bytes() + (b'corrupt' if corrupt_metadata else b''))
            # Run curl's call site without networking, recording every requested URL.
            shim = '''curl() {
  local output='' url=''
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --output) output="$2"; shift 2 ;;
      --retry|--connect-timeout|--max-time) shift 2 ;;
      --*) shift ;;
      *) url="$1"; shift ;;
    esac
  done
  printf '%s\\n' "$url" >> "$REQUEST_LOG"
  cp "$FIXTURES/${url##*/}" "$output"
}
'''
            shell_script = root / 'step.sh'
            shell_script.write_text(shim + script + '\n', encoding='utf-8', newline='\n')
            runtime = root / 'runner'
            runtime.mkdir()
            env = os.environ.copy()
            env.update({'RUNNER_TEMP': runtime.as_posix(), 'GITHUB_ENV': (root / 'github-env').as_posix(),
                        'FIXTURES': fixtures.as_posix(), 'REQUEST_LOG': (root / 'requests').as_posix()})
            bash = shutil.which('bash')
            if not bash and os.name == 'nt':
                bash = str(Path(os.environ.get('ProgramFiles', 'C:/Program Files')) / 'Git/bin/bash.exe')
            result = subprocess.run([bash or 'bash', shell_script.as_posix()], cwd=root, env=env,
                                    capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=30)
            requests = (root / 'requests').read_text(encoding='utf-8').splitlines() if (root / 'requests').exists() else []
            output = runtime / 'maven-repo/brachy/modularui/modularui-mc1.21.1/3.3.1-SNAPSHOT'
            contents = {p.name: p.read_bytes() for p in output.iterdir()} if output.exists() else {}
            return result, requests, contents

    def test_committed_metadata_selects_only_the_reviewed_artifacts(self):
        root = ET.parse(ROOT / '.github/dependencies/modularui-maven-metadata.xml').getroot()
        self.assertEqual(root.findtext('groupId'), 'brachy.modularui')
        self.assertEqual(root.findtext('artifactId'), PREFIX)
        self.assertEqual(root.findtext('version'), '3.3.1-SNAPSHOT')
        self.assertEqual(root.findtext('versioning/snapshot/timestamp'), '20260718.084355')
        self.assertEqual(root.findtext('versioning/snapshot/buildNumber'), '4')
        versions = root.findall('versioning/snapshotVersions/snapshotVersion')
        self.assertEqual(len(versions), 5)
        self.assertEqual({(v.findtext('extension'), v.findtext('classifier')) for v in versions},
                         {('jar', None), ('jar', 'sources'), ('module', None),
                          ('pom', None), ('json', 'interfaceinjection')})
        self.assertEqual({v.findtext('value') for v in versions}, {STAMP})

    def test_mutable_remote_metadata_cannot_change_the_pinned_snapshot(self):
        result, requests, contents = self.run_step()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(len(requests), 5)
        self.assertFalse(any(url.endswith('/maven-metadata.xml') for url in requests))
        self.assertEqual({url.rsplit('/', 1)[-1] for url in requests},
                         {PREFIX + '-' + STAMP + suffix for suffix in SUFFIXES})
        self.assertEqual(contents['maven-metadata.xml'],
                         (ROOT / '.github/dependencies/modularui-maven-metadata.xml').read_bytes())
        for suffix in SUFFIXES:
            self.assertEqual(contents[PREFIX + '-' + STAMP + suffix],
                             contents[PREFIX + '-3.3.1-SNAPSHOT' + suffix])

    def test_every_corrupt_pinned_artifact_is_rejected(self):
        for suffix in SUFFIXES:
            name = PREFIX + '-' + STAMP + suffix
            with self.subTest(artifact=name):
                result, _, _ = self.run_step(corrupt=name)
                self.assertNotEqual(result.returncode, 0)
                self.assertIn('SHA-256 mismatch', result.stderr)
                self.assertIn(name + ': expected', result.stderr)

    def test_corrupt_committed_metadata_is_rejected(self):
        result, _, _ = self.run_step(corrupt_metadata=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('maven-metadata.xml: expected', result.stderr)


if __name__ == '__main__':
    unittest.main(verbosity=2)
