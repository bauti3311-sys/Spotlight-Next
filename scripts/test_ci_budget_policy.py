import importlib.util
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]

def load(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / '.codex/scripts' / (name + '.py'))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

PATHS = load('ci-paths')
DEVICES = load('device-matrix')


class CiBudgetPolicyTest(unittest.TestCase):
    def test_manual_defaults_to_one_android16_tablet(self):
        self.assertEqual({'include': [{'profile': 'pixel_c', 'api': 36}]}, DEVICES.device_matrix('android16-tablet'))
        legacy = DEVICES.device_matrix('legacy-complete')['include']
        self.assertEqual({('pixel_6', 29), ('pixel_c', 29), ('pixel_6', 30), ('pixel_c', 30)}, {(r['profile'], r['api']) for r in legacy})
        self.assertEqual(5, len(DEVICES.device_matrix('complete')['include']))
        with self.assertRaises(ValueError):
            DEVICES.device_matrix('unexpected')

    def test_cancelled_or_unverified_push_never_skips_inherited_app_changes(self):
        event = {'action': 'synchronize', 'before': 'a' * 40, 'pull_request': {'base': {'sha': 'b' * 40}}}
        self.assertEqual('b' * 40, PATHS.comparison_base('pull_request', event))
        self.assertEqual('a' * 40, PATHS.comparison_base('pull_request', event, previous_verified=True))
        with self.assertRaises(ValueError):
            PATHS.comparison_base('push', {'before': '--bad-sha'})

    def test_previous_ci_failure_cancellation_and_api_failure_are_conservative(self):
        event = {'action': 'synchronize', 'before': 'a' * 40}
        env = {'GITHUB_TOKEN': 'offline-fixture', 'GITHUB_REPOSITORY': 'fixture/repository'}
        for status, conclusion, expected in [('completed', 'success', True), ('completed', 'failure', False), ('completed', 'cancelled', False), ('in_progress', None, False)]:
            payload = {'workflow_runs': [{'head_sha': 'a' * 40, 'status': status, 'conclusion': conclusion}]}
            with self.subTest(status=status, conclusion=conclusion), patch.dict('os.environ', env), patch.object(PATHS.urllib.request, 'urlopen', return_value=io.StringIO(json.dumps(payload))):
                self.assertEqual(expected, PATHS.previous_quick_ci_succeeded('pull_request', event))
        payload = {'workflow_runs': [
            {'head_sha': 'a' * 40, 'status': 'completed', 'conclusion': 'failure'},
            {'head_sha': 'a' * 40, 'status': 'completed', 'conclusion': 'success'},
        ]}
        with patch.dict('os.environ', env), patch.object(PATHS.urllib.request, 'urlopen', return_value=io.StringIO(json.dumps(payload))):
            self.assertFalse(PATHS.previous_quick_ci_succeeded('pull_request', event))
        with patch.dict('os.environ', env), patch.object(PATHS.urllib.request, 'urlopen', side_effect=OSError('offline')):
            self.assertFalse(PATHS.previous_quick_ci_succeeded('pull_request', event))

    def test_real_git_delta_avoids_gradle_for_docs_but_preserves_native_changes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            def git(*args):
                return subprocess.check_output(['git', '-C', str(root), *args], text=True).strip()
            git('init', '-q')
            git('config', 'user.name', 'Offline CI fixture')
            git('config', 'user.email', 'fixture@example.invalid')
            (root / 'app').mkdir()
            (root / 'app/fixture.kt').write_text('native')
            git('add', '.')
            git('commit', '-qm', 'base')
            base = git('rev-parse', 'HEAD')
            (root / 'docs').mkdir()
            (root / 'docs/progress with space.md').write_text('checkpoint')
            git('add', '.')
            git('commit', '-qm', 'docs')
            with patch('os.getcwd', return_value=str(root)):
                # changed_paths runs real git, including NUL-delimited filenames.
                with patch.object(PATHS.subprocess, 'check_output', wraps=lambda args: subprocess.run(args, cwd=root, check=True, capture_output=True).stdout):
                    self.assertEqual({'android': False, 'web': False}, PATHS.classify(PATHS.changed_paths(base)))
            (root / 'app/fixture.kt').write_text('native change')
            git('add', '.')
            git('commit', '-qm', 'native')
            with patch.object(PATHS.subprocess, 'check_output', wraps=lambda args: subprocess.run(args, cwd=root, check=True, capture_output=True).stdout):
                self.assertEqual({'android': True, 'web': False}, PATHS.classify(PATHS.changed_paths(base)))
        self.assertEqual({'android': False, 'web': True}, PATHS.classify(['visual-lab/src/reference.tsx']))


if __name__ == '__main__':
    unittest.main()
