"""Prepare a verified local Maven mirror for the existing optional integrations."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / '.github/dependencies/modrinth-artifacts.json'


def verify(path, pin):
    data = path.read_bytes()
    actual = hashlib.sha256(data).hexdigest()
    if actual != pin['sha256'] or len(data) != pin['bytes']:
        raise ValueError(f'Pinned artifact mismatch: {path.name}: size={len(data)}, sha256={actual}')


def download(url, output):
    subprocess.run(['curl', '--fail', '--location', '--retry', '5', '--retry-all-errors',
                    '--connect-timeout', '20', '--max-time', '180', '--output', str(output), url],
                   check=True, timeout=1200)


def provision(repository, fetch=download, lock=LOCK):
    records = json.loads(lock.read_text(encoding='utf-8'))
    repository = Path(repository)
    repository.mkdir(parents=True, exist_ok=True)
    # Verify all downloaded payloads and the checked-in POMs before exposing any
    # of this invocation's artifacts to Gradle. Never fall back to unverified bytes.
    with tempfile.TemporaryDirectory(prefix='.pinned-download-', dir=repository) as temporary:
        staging = Path(temporary)
        prepared = []
        for record in records:
            directory = Path(*record['group'].split('.')) / record['artifact'] / record['version']
            stem = record['artifact'] + '-' + record['version']
            jar = staging / (stem + '.jar')
            fetch(record['jar']['url'], jar)
            verify(jar, record['jar'])
            pom = lock.parent / record['pom']['file']
            verify(pom, record['pom'])
            prepared.extend([(jar, directory / jar.name), (pom, directory / (stem + '.pom'))])
        for source, relative in prepared:
            target = repository / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, target)
    return len(records)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', required=True, type=Path)
    args = parser.parse_args()
    print(f'Verified and mirrored {provision(args.repo)} pinned Modrinth artifacts')
