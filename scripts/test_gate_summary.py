"""Controlled XML fixtures; no Android, network or Actions invocation."""
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('summary', Path(__file__).with_name('summarize-directed-gate.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class GateSummaryTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)

    def report(self, group, xml, code='0'):
        (self.root / f'{group}.started').touch()
        (self.root / f'{group}.exit-code').write_text(code)
        folder = self.root / 'results' / group
        folder.mkdir(parents=True)
        (folder / 'TEST-fixture.xml').write_text(xml)

    def summary(self, compile_result='success', groups_result='success'):
        return module.summarize(self.root, 'a' * 40, compile_result, groups_result)

    def test_compile_failure_does_not_claim_tests(self):
        text, invalid = self.summary('failure', 'skipped')
        self.assertIn('Compilation: **failure**', text)
        self.assertEqual(8, text.count('not started; no XML'))
        self.assertFalse(invalid)

    def test_passed_failed_error_and_skipped_counts(self):
        self.report('ui', '<testsuite tests="10" failures="2" errors="1" skipped="3"/>', '1')
        text, invalid = self.summary(groups_result='failure')
        self.assertIn('| ui | failed (exit 1) | 4 | 2 | 1 | 3 |', text)
        self.assertIn('| d28 | not started; no XML', text)
        self.assertFalse(invalid)

    def test_multiple_reports_aggregate_only_within_group(self):
        self.report('ui', '<testsuite tests="2"/>')
        (self.root / 'results/ui/TEST-other.xml').write_text('<testsuite tests="3" skipped="1"/>')
        self.report('d28', '<testsuite tests="7"/>')
        text, invalid = self.summary()
        self.assertIn('| ui | success (exit 0) | 4 | 0 | 0 | 1 |', text)
        self.assertIn('| d28 | success (exit 0) | 7 | 0 | 0 | 0 |', text)
        self.assertFalse(invalid)

    def test_interrupted_group_without_xml(self):
        (self.root / 'ui.started').touch()
        text, invalid = self.summary(groups_result='cancelled')
        self.assertIn('started; no exit status (interrupted); no XML', text)
        self.assertFalse(invalid)

    def test_malformed_and_inconsistent_xml_not_reported_as_passed(self):
        for xml in ('<broken', '<testsuite tests="1" failures="2"/>'):
            with self.subTest(xml=xml):
                if not (self.root / 'results/ui').exists():
                    self.report('ui', xml)
                else:
                    (self.root / 'results/ui/TEST-fixture.xml').write_text(xml)
                text, invalid = self.summary()
                self.assertIn('invalid XML (counts unavailable) | — | — | — | — |', text)
                self.assertTrue(invalid)

    def test_entrypoint_appends_summary_and_logs_without_upload(self):
        output = self.root / 'summary.md'
        output.write_text('Existing summary\n')
        result = subprocess.run(
            ['python3', str(Path(module.__file__).resolve())], cwd=self.root,
            env={**os.environ, 'SOURCE_SHA': 'a' * 40, 'COMPILE_RESULT': 'failure',
                 'GROUPS_RESULT': 'skipped', 'GITHUB_STEP_SUMMARY': str(output)},
            text=True, capture_output=True,
        )
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual('Existing summary\n' + result.stdout, output.read_text())
        self.assertIn('Compilation: **failure**', result.stdout)


if __name__ == '__main__':
    unittest.main(verbosity=2)
