"""Local validation of CI configuration and its shell; does not dispatch Actions."""
from pathlib import Path
import subprocess
import unittest
try:
    import yaml
except ImportError:
    yaml = None

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / '.github/workflows/build-apk.yml'


def _parse_scalar(value):
    value = value.strip()
    if value.startswith('[') and value.endswith(']'):
        return [_parse_scalar(part) for part in value[1:-1].split(',') if part.strip()]
    if (value.startswith("'") and value.endswith("'")) or (value.startswith('"') and value.endswith('"')):
        return value[1:-1]
    return value


def _load_workflow_fallback(raw):
    raw_lines = raw.splitlines()
    idx = 0

    def next_significant(pos):
        if pos is None:
            return None, -1, ''
        while pos < len(raw_lines):
            stripped = raw_lines[pos].strip()
            if stripped and not stripped.startswith('#'):
                return pos, len(raw_lines[pos]) - len(raw_lines[pos].lstrip(' ')), stripped
            pos += 1
        return None, -1, ''

    def parse_block(pos, min_indent):
        pos0, indent, stripped = next_significant(pos)
        if pos0 is None:
            return {}, len(raw_lines)
        if indent < min_indent:
            return {}, pos0
        pos = pos0
        if stripped.startswith('- '):
            items = []
            while True:
                pos, cur_indent, stripped = next_significant(pos)
                if pos is None or cur_indent < indent or not stripped.startswith('- '):
                    break
                rest = stripped[2:].strip()
                pos += 1
                item = {}
                if rest:
                    k, _, v = rest.partition(':')
                    item[k.strip()] = _parse_scalar(v)
                sub, pos = parse_block(pos, cur_indent + 1)
                if isinstance(sub, dict):
                    item.update(sub)
                items.append(item)
            return items, pos
        mapping = {}
        while True:
            pos, cur_indent, stripped = next_significant(pos)
            if pos is None or cur_indent < indent or stripped.startswith('- '):
                break
            key, _, rest = stripped.partition(':')
            key = key.strip()
            rest = rest.strip()
            pos += 1
            if rest == '|':
                block_lines = []
                block_indent = None
                while pos < len(raw_lines):
                    line = raw_lines[pos]
                    if not line.strip():
                        block_lines.append('')
                        pos += 1
                        continue
                    line_indent = len(line) - len(line.lstrip(' '))
                    if line_indent <= cur_indent:
                        break
                    if block_indent is None:
                        block_indent = line_indent
                    block_lines.append(line[block_indent:])
                    pos += 1
                mapping[key] = '\n'.join(block_lines) + '\n'
            elif rest == '':
                sub, pos = parse_block(pos, cur_indent + 1)
                mapping[key] = sub
            else:
                mapping[key] = _parse_scalar(rest)
        return mapping, pos

    doc, _ = parse_block(0, 0)
    return doc


def load_workflow(text):
    if yaml is not None:
        return yaml.load(text, Loader=yaml.BaseLoader)
    return _load_workflow_fallback(text)


class GateWorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.doc = load_workflow(WORKFLOW.read_text())
        cls.gate = cls.doc['jobs']['directed-gate']
    def test_manual_only_default_gate_and_separate_preview(self):
        self.assertEqual({'workflow_dispatch'}, set(self.doc['on']))
        inputs = self.doc['on']['workflow_dispatch']['inputs']
        self.assertEqual('directed-tests', inputs['mode']['default'])
        self.assertEqual(['directed-tests', 'preview'], inputs['mode']['options'])
        self.assertIn("inputs.mode == 'directed-tests'", self.gate['if'])
        self.assertIn("inputs.mode == 'preview'", self.doc['jobs']['raycast-preview']['if'])
        self.assertEqual({'contents': 'read'}, self.doc['permissions'])
    def test_directed_gate_and_preview_allow_only_authorized_branches_and_verify_preview55_identity(self):
        self.assertIn("refs/heads/feat/raycast-sidebar-permissions-20261020", self.gate['if'])
        self.assertIn("refs/heads/feat/antigravity-tablet-20261026", self.gate['if'])
        gate_text = '\n'.join(s.get('run', '') for s in self.gate['steps'])
        self.assertIn('refs/heads/feat/antigravity-tablet-20261026', gate_text)
        self.assertIn('git merge-base --is-ancestor "$SOURCE_SHA" "refs/remotes/origin/$target_branch"', gate_text)
        preview = self.doc['jobs']['raycast-preview']
        self.assertIn("refs/heads/feat/raycast-sidebar-permissions-20261020", preview['if'])
        self.assertIn("refs/heads/feat/antigravity-tablet-20261026", preview['if'])
        self.assertNotIn("refs/heads/main", preview['if'])
        self.assertNotIn("feat/premium-product-checkpoint", WORKFLOW.read_text())
        preview_text = '\n'.join(s.get('run', '') for s in preview['steps'])
        self.assertIn('git merge-base --is-ancestor "$SOURCE_SHA" "refs/remotes/origin/$target_branch"', preview_text)
        self.assertIn('signing/spotlight-debug.keystore', preview_text)
        self.assertIn('-Pspotlight.preview.optimize=false', preview_text)
        self.assertIn("package: name='dev.bautista.spotlight.next.raycastpreview' versionCode='55' versionName='2.0.1-raycast-preview-experimental'", preview_text)
        self.assertIn('885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba', preview_text)
        self.assertIn('zipalign" -c -P 16 -v 4 "$apk"', preview_text)
        self.assertIn('p_align < 16384', preview_text)
        self.assertIn('lib/arm64-v8a/', preview_text)
        self.assertNotIn('rm -rf signing', preview_text)
        cleanup = next(s for s in preview['steps'] if s.get('name') == 'Clean up temporary signing files')
        self.assertEqual('always()', cleanup.get('if'))
        self.assertIn('rm -f "$HOME/.android/debug.keystore" signing/spotlight-debug.keystore signing/credentials.properties', cleanup.get('run', ''))
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
        self.assertIn(':app:warmDirectedTestRuntime', text)
        self.assertIn('for group in ui d28 capabilities spotify cast imports metrics sources', text)
        self.assertIn('if (( result != 0 )); then exit "$result"; fi', text)
    def test_logs_xml_and_failures_are_preserved(self):
        text = '\n'.join(s.get('run', '') for s in self.gate['steps'])
        self.assertIn('tee gate-evidence/compile.log', text)
        self.assertIn('gate-evidence/$group.log', text)
        self.assertIn('gate-evidence/results/$group/', text)
        self.assertIn('set -euo pipefail', text)
        self.assertFalse(any(s.get('uses', '').startswith('actions/upload-artifact@') for s in self.gate['steps']))
        summary = next(s for s in self.gate['steps'] if 'summarize-directed-gate.py' in s.get('run', ''))
        self.assertEqual('always()', summary['if'])
        self.assertEqual('${{ steps.compile.outcome }}', summary['env']['COMPILE_RESULT'])
        self.assertEqual('${{ steps.directed.outcome }}', summary['env']['GROUPS_RESULT'])
        self.assertFalse(any('continue-on-error' in s for s in self.gate['steps']))
        preview = self.doc['jobs']['raycast-preview']
        self.assertTrue(any(s.get('uses', '').startswith('actions/upload-artifact@') for s in preview['steps']))
    def test_all_embedded_shell_steps_have_valid_bash_syntax(self):
        for job in self.doc['jobs'].values():
            for step in job['steps']:
                if 'run' in step:
                    result = subprocess.run(['bash', '-n'], input=step['run'], text=True, capture_output=True)
                    self.assertEqual(0, result.returncode, step['name'] + ': ' + result.stderr)

if __name__ == '__main__':
    unittest.main(verbosity=2)
