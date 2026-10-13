import copy
import tempfile
from pathlib import Path
import unittest
import zipfile

from delivery_gate import REQUIRED, REPO, SOURCE, extract_baseline, identity, select_checks


class DeliveryGateTest(unittest.TestCase):
    def runs(self):
        return [{"id": i + 1, "path": path, "head_sha": SOURCE, "event": "pull_request",
                 "head_repository": {"full_name": REPO}, "status": "completed",
                 "conclusion": "success", "html_url": "https://github.com/check"}
                for i, path in enumerate(REQUIRED)]

    def test_requires_both_successful_checks_on_authorized_sha(self):
        self.assertEqual(2, len(select_checks(self.runs(), SOURCE)))
        with self.assertRaises(ValueError):
            select_checks(self.runs(), "0" * 40)
        with self.assertRaises(ValueError):
            select_checks(self.runs()[:1], SOURCE)

    def test_failed_latest_attempt_cannot_be_replaced_by_old_green(self):
        runs = self.runs()
        failed = copy.deepcopy(runs[0])
        failed.update(id=100, conclusion="failure")
        with self.assertRaises(ValueError):
            select_checks(runs + [failed], SOURCE)

    def test_foreign_repo_wrong_sha_and_running_checks_are_rejected(self):
        for changes in ({"head_repository": {"full_name": "foreign/fork"}},
                        {"head_sha": "0" * 40}, {"status": "in_progress"},
                        {"event": "workflow_dispatch"}):
            runs = self.runs()
            runs[0].update(changes)
            with self.assertRaises(ValueError):
                select_checks(runs, SOURCE)

    def test_untrusted_baseline_bytes_are_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            archive = Path(folder) / "baseline.zip"
            with zipfile.ZipFile(archive, "w") as z:
                z.writestr("baseline.apk", b"fake APK")
            with self.assertRaises(ValueError):
                extract_baseline(archive, Path(folder) / "baseline.apk")

    def test_manifest_identity_is_read_from_apk_tool_output(self):
        self.assertEqual(("dev.bautista.spotlight.next", 28, "2.0.1"), identity(
            "package: name='dev.bautista.spotlight.next' versionCode='28' versionName='2.0.1'\n"))
        with self.assertRaises(ValueError):
            identity("missing manifest")


if __name__ == "__main__":
    unittest.main()
