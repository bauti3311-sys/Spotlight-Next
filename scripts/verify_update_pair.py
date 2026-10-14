"""Public metadata/payload gates for a private debug update, not a release verifier."""
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile
from verify_release import verify_certificate, HISTORICAL_CERTIFICATE_SHA256

SOURCE = '58ac0a2d4cb4f96c9fbef46da4f4104475c251ae'
PACKAGE = 'dev.bautista.spotlight.next'
SIGNATURE = re.compile(r'^META-INF/(?:MANIFEST\.MF|[^/]+\.(?:SF|RSA|DSA|EC))$', re.I)


def digest(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def identity(report):
    m = re.search(r"^package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", report, re.M)
    sdk = re.search(r"^sdkVersion:'(\d+)'", report, re.M)
    if not m or not sdk:
        raise ValueError('Missing APK identity')
    return {'package': m[1], 'code': int(m[2]), 'name': m[3], 'minSdk': int(sdk[1])}


def certificate(report):
    values = re.findall(r'^Signer #\d+ certificate SHA-256 digest: ([a-fA-F0-9]{64})$', report, re.M)
    if len(values) != 1:
        raise ValueError('Expected one APK signer')
    return values[0].lower()


def validate_pair(old, new, old_cert, new_cert):
    a, b = identity(old), identity(new)
    verify_certificate(old_cert)
    if a['package'] != PACKAGE or b['package'] != PACKAGE:
        raise ValueError('Package mismatch')
    if a['code'] != 27 or b['code'] != 28 or b['code'] <= a['code'] or b['name'] != '2.0.1':
        raise ValueError('Unexpected versions; refuse downgrade or unapproved source')
    if b['minSdk'] != 26 or not re.search(r"native-code:.*'arm64-v8a'", new):
        raise ValueError('Android minimum/ARM64 mismatch')
    if 'application-debuggable' not in new:
        raise ValueError('This authorized artifact must remain a private debug update')
    before = certificate(new_cert)
    if before == HISTORICAL_CERTIFICATE_SHA256:
        raise ValueError('Candidate already has historical signer; investigate installed APK instead')
    return {'historical': a, 'candidate': b, 'historical_certificate': certificate(old_cert),
            'candidate_certificate': before, 'source': SOURCE}


def payload(path):
    result = {}
    with zipfile.ZipFile(path) as apk:
        names = apk.namelist()
        if len(names) != len(set(names)):
            raise ValueError('Duplicate APK entries')
        for name in names:
            if name.endswith('/') or SIGNATURE.fullmatch(name):
                continue
            with apk.open(name) as stream:
                result[name] = hashlib.file_digest(stream, 'sha256').hexdigest()
    return result


def verify_unchanged(before, after):
    if payload(before) != payload(after):
        raise ValueError('APK code/resources changed while signing; withhold artifact')


if __name__ == '__main__':
    stage, root = sys.argv[1], Path(sys.argv[2])
    report = validate_pair(*(root.joinpath(name).read_text() for name in
                            ['historical.badging.txt', 'candidate.badging.txt',
                             'historical.cert.txt', 'candidate.cert.txt']))
    original = Path('input/candidate/Spotlight-Next-debug-58ac0a2.apk')
    if Path('input/candidate/SOURCE_COMMIT').read_text().strip() != SOURCE:
        raise ValueError('Source commit mismatch')
    recorded = Path('input/candidate/SHA256SUMS').read_text().split()
    if len(recorded) != 2 or recorded[1] != original.name or digest(original) != recorded[0]:
        raise ValueError('Original APK checksum mismatch')
    if stage == 'final':
        signed, certs = Path(sys.argv[3]), Path(sys.argv[4])
        verify_certificate(certs.read_text())
        verify_unchanged(original, signed)
        report.update(signed_certificate=certificate(certs.read_text()),
                      apk_sha256=digest(signed), apk_bytes=signed.stat().st_size,
                      payload_unchanged=True, compatible_with_historical_artifact=True,
                      physical_update_not_tested=True)
    elif stage != 'preflight':
        raise ValueError('Unknown stage')
    print(json.dumps(report, indent=2))
