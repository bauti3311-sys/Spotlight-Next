"""Tests of the actual local runner's safety/filters, not Android/Gradle tests."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
RUNNER = ROOT / 'scripts/test-d28-directed.sh'

class DirectedRunnerTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        sdk = self.root / 'sdk'
        (sdk / 'platforms/android-35').mkdir(parents=True)
        bin_dir = self.root / 'bin'
        bin_dir.mkdir(parents=True)
        java_stub = bin_dir / 'java'
        java_stub.write_text('#!/usr/bin/env sh\nexit 0\n')
        java_stub.chmod(0o700)
        self.log = self.root / 'arguments.json'
        fake = self.root / 'gradle-stub'
        fake.write_text('#!/usr/bin/env python3\nimport json,os,sys\nfrom pathlib import Path\nPath(os.environ["SPOTLIGHT_TEST_ARGUMENT_LOG"]).write_text(json.dumps(sys.argv[1:]))\n')
        fake.chmod(0o700)
        self.env = dict(os.environ, ANDROID_HOME=str(sdk), ANDROID_SDK_ROOT=str(sdk),
                        PATH=f"{bin_dir}:{os.environ.get('PATH', '')}",
                        SPOTLIGHT_GRADLE_BIN=str(fake), SPOTLIGHT_TEST_ARGUMENT_LOG=str(self.log),
                        GRADLE_USER_HOME=str(self.root / 'empty-cache'))
    def run_group(self, *args, env=None):
        return subprocess.run(['bash', str(RUNNER), *args], cwd=ROOT,
                              env=self.env if env is None else env,
                              text=True, capture_output=True, timeout=5)
    def arguments(self):
        return json.loads(self.log.read_text())
    def test_discovery_is_limited_to_exact_selected_classes(self):
        result = self.run_group('ui')
        self.assertEqual(0, result.returncode, result.stderr)
        args = self.arguments()
        names = [args[i + 1] for i, value in enumerate(args) if value == '--tests']
        self.assertIn('-PspotlightDirectedClasses=' + ','.join(names), args)
    def test_default_runs_only_ui_regressions_offline_without_apk_or_publish(self):
        result = self.run_group()
        self.assertEqual(0, result.returncode, result.stderr)
        args = self.arguments()
        self.assertIn('--offline', args)
        self.assertIn('scripts/directed-tests.init.gradle', args)
        self.assertIn('--max-workers=1', args)
        self.assertEqual(':app:testDebugUnitTest', args[0])
        names = [args[i + 1] for i, value in enumerate(args) if value == '--tests']
        self.assertEqual(4, len(names))
        self.assertTrue(all(name.startswith('dev.bautista.spotlight.ui.') for name in names))
        self.assertFalse(any('assemble' in arg or 'publish' in arg for arg in args))
    def test_spotify_group_is_bounded_and_does_not_repeat_ui_or_historical_suites(self):
        result = self.run_group('spotify')
        self.assertEqual(0, result.returncode, result.stderr)
        args = self.arguments()
        names = [args[i + 1] for i, value in enumerate(args) if value == '--tests']
        self.assertEqual(6, len(names))
        self.assertIn('dev.bautista.spotlight.integrations.spotify.SpotifyLocalPlayerTest', names)
        self.assertTrue(all('.integrations.spotify.' in name for name in names))
    def test_unknown_group_never_invokes_runner(self):
        result = self.run_group('deploy')
        self.assertEqual(2, result.returncode)
        self.assertFalse(self.log.exists())
    def test_missing_sdk_does_not_start_build(self):
        if (ROOT / 'local.properties').exists():
            self.skipTest('Host has local.properties; SDK guard cannot be isolated without editing it')
        env = dict(self.env, ANDROID_HOME=str(self.root / 'missing'), ANDROID_SDK_ROOT=str(self.root / 'missing'))
        result = self.run_group('d28', env=env)
        self.assertEqual(2, result.returncode)
        self.assertIn('SDK Android 35', result.stderr)
        self.assertFalse(self.log.exists())
    def test_missing_distribution_is_not_downloaded(self):
        env = dict(self.env)
        env.pop('SPOTLIGHT_GRADLE_BIN')
        result = self.run_group('cast', env=env)
        self.assertEqual(2, result.returncode)
        self.assertIn('No se descargará', result.stderr)
        self.assertFalse(self.log.exists())

if __name__ == '__main__':
    unittest.main(verbosity=2)
