import tempfile
from pathlib import Path
import unittest
import zipfile
from verify_update_pair import validate_pair, verify_unchanged, HISTORICAL_CERTIFICATE_SHA256


def cert(value):
    return f'Signer #1 certificate SHA-256 digest: {value}\n'

OLD = "package: name='dev.bautista.spotlight.next' versionCode='27' versionName='2.0.0'\nsdkVersion:'26'"
NEW = "package: name='dev.bautista.spotlight.next' versionCode='28' versionName='2.0.1'\nsdkVersion:'26'\nnative-code: 'arm64-v8a'\napplication-debuggable"


class UpdateGatesTest(unittest.TestCase):
    def test_version_and_certificate_comparison(self):
        r = validate_pair(OLD, NEW, cert(HISTORICAL_CERTIFICATE_SHA256), cert('a' * 64))
        self.assertEqual(27, r['historical']['code'])
        self.assertEqual(28, r['candidate']['code'])
        self.assertNotEqual(r['historical_certificate'], r['candidate_certificate'])

    def test_refuses_wrong_identity_version_or_historical_key(self):
        for modified in [NEW.replace('dev.bautista.spotlight.next', 'other.app'), NEW.replace("versionCode='28'", "versionCode='27'"), NEW.replace("'arm64-v8a'", "'x86'"), NEW.replace('application-debuggable', '')]:
            with self.subTest(apk=modified), self.assertRaises(ValueError):
                validate_pair(OLD, modified, cert(HISTORICAL_CERTIFICATE_SHA256), cert('a' * 64))
        with self.assertRaises(ValueError):
            validate_pair(OLD, NEW, cert('b' * 64), cert('a' * 64))

    def test_signature_metadata_can_change_but_code_cannot(self):
        with tempfile.TemporaryDirectory() as tmp:
            before, after = Path(tmp) / 'before.apk', Path(tmp) / 'after.apk'
            for path, sig in [(before, b'debug'), (after, b'historical')]:
                with zipfile.ZipFile(path, 'w') as z:
                    z.writestr('classes.dex', b'unchanged-code')
                    z.writestr('META-INF/CERT.RSA', sig)
                    z.writestr('META-INF/library.kotlin_module', b'unchanged-metadata')
            verify_unchanged(before, after)
            with zipfile.ZipFile(after, 'w') as z:
                z.writestr('classes.dex', b'modified-code')
                z.writestr('META-INF/library.kotlin_module', b'unchanged-metadata')
            with self.assertRaises(ValueError):
                verify_unchanged(before, after)
