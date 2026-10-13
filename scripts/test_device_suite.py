import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


class DeviceSuiteTest(unittest.TestCase):
    def test_every_stage_runs_and_any_failure_keeps_suite_red(self):
        source = Path(__file__).resolve().parents[1] / '.codex/scripts/device-suite.sh'
        for maestro, native, capture in [(0, 0, 0), (1, 0, 0), (0, 1, 0), (0, 0, 1)]:
            with self.subTest(maestro=maestro, native=native, capture=capture), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                scripts = root / '.codex/scripts'
                scripts.mkdir(parents=True)
                shutil.copy(source, scripts / 'device-suite.sh')
                (scripts / 'device-checks.sh').write_text('printf "maestro\\n" >> "$SPOTLIGHT_TEST_EVENTS"\nexit "$SPOTLIGHT_MAESTRO_STATUS"\n')
                (root / 'gradlew').write_text('printf "native\\n" >> "$SPOTLIGHT_TEST_EVENTS"\nexit "$SPOTLIGHT_NATIVE_STATUS"\n')
                binary = root / 'bin'
                binary.mkdir()
                adb = binary / 'adb'
                adb.write_text('#!/usr/bin/env bash\nprintf "capture\\n" >> "$SPOTLIGHT_TEST_EVENTS"\nexit "$SPOTLIGHT_CAPTURE_STATUS"\n')
                adb.chmod(0o755)
                events = root / 'events.txt'
                environment = os.environ.copy()
                environment.update(PATH=str(binary) + os.pathsep + environment['PATH'],
                    SPOTLIGHT_TEST_EVENTS=str(events), SPOTLIGHT_MAESTRO_STATUS=str(maestro),
                    SPOTLIGHT_NATIVE_STATUS=str(native), SPOTLIGHT_CAPTURE_STATUS=str(capture))
                result = subprocess.run(['bash', str(scripts / 'device-suite.sh')], env=environment,
                    capture_output=True, text=True, timeout=10)
                self.assertEqual(0 if (maestro, native, capture) == (0, 0, 0) else 1, result.returncode, result.stderr)
                self.assertEqual(['maestro', 'native', 'capture'], events.read_text().splitlines())


if __name__ == '__main__':
    unittest.main()
