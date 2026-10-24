"""Local validation of CI configuration and its shell; does not dispatch Actions."""
from pathlib import Path
import subprocess
import unittest
import yaml

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / '.github/workflows/build-apk.yml'

class GateWorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.doc = yaml.load(WORKFLOW.read_text(), Loader=yaml.BaseLoader)
        cls.gate = cls.doc['jobs']['directed-gate']
    def test_manual_only_default_gate_and_separate_preview(self):
        self.assertEqual({'workflow_dispatch'}, set(self.doc['on']))
        inputs = self.doc['on']['workflow_dispatch']['inputs']
        self.assertEqual('directed-tests', inputs['mode']['default'])
        self.assertEqual(['directed-tests', 'preview'], inputs['mode']['options'])
        self.assertIn("inputs.mode == 'directed-tests'", self.gate['if'])
        self.assertIn("inputs.mode == 'preview'", self.doc['jobs']['raycast-preview']['if'])
        self.assertEqual({'contents': 'read'}, self.doc['permissions'])
    def test_exact_source_and_safe_checkout(self):
        checkouts = [s for s in self.gate['steps'] if s.get('uses', '').startswith('actions/checkout@')]
        self.assertEqual(1, len(checkouts))
        self.assertEqual('${{ inputs.source_sha }}', checkouts[0]['with']['ref'])
        self.assertEqual('false', checkouts[0]['with']['persist-credentials'])
        text = '\n'.join(s.get('run', '') for s in self.gate['steps'])
        self.assertIn('^[0-9a-f]{40}$', text)
        self.assertIn('git merge-base --is-ancestor', text)
        self.assertIn('test "$(git rev-parse HEAD)" = "$SOURCE_SHA"', text)
    def test_one_bounded_job_without_signing_apk_or_instrumentation(self):
        self.assertEqual('ubuntu-24.04', self.gate['runs-on'])
        self.assertEqual('30', self.gate['timeout-minutes'])
        self.assertNotIn('strategy', self.gate)
        text = '\n'.join(s.get('run', '') for s in self.gate['steps'])
        for forbidden in ('assembleDebug', 'assembleRaycastPreview', 'connectedDebugAndroidTest', 'sign-release', 'apksigner', 'keytool', 'secrets.'):
            self.assertNotIn(forbidden, text)
        self.assertIn(':app:compileDebugUnitTestKotlin', text)
        self.assertIn('for group in ui d28 capabilities spotify cast imports metrics sources', text)
        self.assertIn('if (( result != 0 )); then exit "$result"; fi', text)
    def test_logs_xml_and_failures_are_preserved(self):
        text = '\n'.join(s.get('run', '') for s in self.gate['steps'])
        self.assertIn('tee gate-evidence/compile.log', text)
        self.assertIn('gate-evidence/$group.log', text)
        self.assertIn('gate-evidence/results/$group/', text)
        upload = [s for s in self.gate['steps'] if s.get('uses', '').startswith('actions/upload-artifact@')][0]
        self.assertEqual('always()', upload['if'])
        self.assertEqual('gate-evidence/', upload['with']['path'])
        self.assertEqual('3', upload['with']['retention-days'])
    def test_all_embedded_shell_steps_have_valid_bash_syntax(self):
        for job in self.doc['jobs'].values():
            for step in job['steps']:
                if 'run' in step:
                    result = subprocess.run(['bash', '-n'], input=step['run'], text=True, capture_output=True)
                    self.assertEqual(0, result.returncode, step['name'] + ': ' + result.stderr)

if __name__ == '__main__':
    unittest.main(verbosity=2)
