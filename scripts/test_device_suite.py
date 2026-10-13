import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


class DeviceSuiteTest(unittest.TestCase):
    def test_every_stage_runs_and_any_failure_keeps_suite_red(self):
        source = Path(__file__).resolve().parents[1] / '.codex/scripts/device-suite.sh'
        for maestro, native, capture, driver in [(0, 0, 0, 0), (1, 0, 0, 0), (0, 1, 0, 0), (0, 0, 1, 0), (0, 0, 0, 1)]:
            with self.subTest(maestro=maestro, native=native, capture=capture, driver=driver), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                scripts = root / '.codex/scripts'
                scripts.mkdir(parents=True)
                shutil.copy(source, scripts / 'device-suite.sh')
                (scripts / 'device-checks.sh').write_text('printf "maestro\\n" >> "$SPOTLIGHT_TEST_EVENTS"\nexit "$SPOTLIGHT_MAESTRO_STATUS"\n')
                (root / 'gradlew').write_text('printf "native\\n" >> "$SPOTLIGHT_TEST_EVENTS"\nprintf "%s\\n" "$@" > "$SPOTLIGHT_TEST_NATIVE_ARGS"\nexit "$SPOTLIGHT_NATIVE_STATUS"\n')
                binary = root / 'bin'
                binary.mkdir()
                adb = binary / 'adb'
                adb.write_text('''#!/usr/bin/env bash
if [[ "$1 $2 $3" == "shell am force-stop" ]]; then
  printf 'driver-stop:%s\\n' "$4" >> "$SPOTLIGHT_TEST_EVENTS"
  exit "$SPOTLIGHT_DRIVER_STOP_STATUS"
fi
printf 'capture\\n' >> "$SPOTLIGHT_TEST_EVENTS"
exit "$SPOTLIGHT_CAPTURE_STATUS"
''')
                adb.chmod(0o755)
                events = root / 'events.txt'
                environment = os.environ.copy()
                environment.update(PATH=str(binary) + os.pathsep + environment['PATH'],
                    SPOTLIGHT_TEST_EVENTS=str(events), SPOTLIGHT_TEST_NATIVE_ARGS=str(root / "native-args.txt"), SPOTLIGHT_MAESTRO_STATUS=str(maestro),
                    SPOTLIGHT_NATIVE_STATUS=str(native), SPOTLIGHT_DRIVER_STOP_STATUS=str(driver), SPOTLIGHT_CAPTURE_STATUS=str(capture))
                result = subprocess.run(['bash', str(scripts / 'device-suite.sh')], env=environment,
                    capture_output=True, text=True, timeout=10)
                self.assertEqual(0 if (maestro, native, capture, driver) == (0, 0, 0, 0) else 1, result.returncode, result.stderr)
                self.assertEqual(['maestro', 'driver-stop:dev.mobile.maestro', 'driver-stop:dev.mobile.maestro.test', 'native', 'capture'], events.read_text().splitlines())
                native_arguments = (root / 'native-args.txt').read_text().splitlines()
                self.assertIn('-Pandroid.testInstrumentationRunnerArguments.class=dev.bautista.spotlight.OverlayGeometryDeviceTest,dev.bautista.spotlight.PdfNativeDeviceTest', native_arguments)


if __name__ == '__main__':
    unittest.main()
