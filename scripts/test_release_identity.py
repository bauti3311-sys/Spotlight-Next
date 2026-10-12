import unittest
import tempfile
from pathlib import Path
from release_identity import read

class ReleaseIdentityTest(unittest.TestCase):
    def test_current_identity_exceeds_distribution(self):
        value=read()
        self.assertGreater(int(value["code"]),int(value["installed"]))
        self.assertEqual("2.0.1",value["name"])
    def test_invalid_versions_fail_closed(self):
        for body in ["version.name=bad\nversion.code=28\ninstalled.code=27", "version.name=2.0.1\nversion.code=27\ninstalled.code=27"]:
            with tempfile.TemporaryDirectory() as folder:
                p=Path(folder)/"release.properties";p.write_text(body)
                with self.assertRaises(ValueError):read(p)
