import unittest
from verify_release import verify, verify_certificate, HISTORICAL_CERTIFICATE_SHA256


class UpdateManifestTest(unittest.TestCase):
    def badging(self, package="dev.bautista.spotlight.next", code=25, sdk=26):
        return f"package: name='{package}' versionCode='{code}' versionName='1.3.0'\nsdkVersion:'{sdk}'\n"

    def test_update(self):
        self.assertIn("25 > installed 24", verify(self.badging(), 24))

    def test_wrong_identity(self):
        with self.assertRaises(ValueError):
            verify(self.badging(package="dev.bautista.spotlight"), 24)

    def test_equal_and_lower_versions(self):
        for code in (22, 24):
            with self.assertRaises(ValueError):
                verify(self.badging(code=code), 24)

    def test_raised_minimum(self):
        with self.assertRaises(ValueError):
            verify(self.badging(sdk=35), 24)

    def test_debuggable_rejected(self):
        with self.assertRaises(ValueError):
            verify(self.badging() + "application-debuggable\n", 24)

    def test_missing_metadata(self):
        with self.assertRaises(ValueError):
            verify("", 24)


class HistoricalCertificateTest(unittest.TestCase):
    def report(self, fingerprint):
        return f"Signer #1 certificate SHA-256 digest: {fingerprint}\n"

    def test_historical_certificate(self):
        self.assertIn(HISTORICAL_CERTIFICATE_SHA256, verify_certificate(self.report(HISTORICAL_CERTIFICATE_SHA256.upper())))

    def test_wrong_certificate(self):
        with self.assertRaises(ValueError):
            verify_certificate(self.report("0" * 64))

    def test_missing_certificate(self):
        with self.assertRaises(ValueError):
            verify_certificate("")

    def test_multiple_signers(self):
        with self.assertRaises(ValueError):
            verify_certificate(self.report(HISTORICAL_CERTIFICATE_SHA256) + self.report(HISTORICAL_CERTIFICATE_SHA256).replace("#1", "#2"))
