import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


class DeviceDriverOrderTest(unittest.TestCase):
    def test_cold_invocation_occurs_after_driver_install_and_driver_survives_commands(self):
        source = Path(__file__).resolve().parents[1] / '.codex/scripts/device-checks.sh'
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            scripts = root / '.codex/scripts'
            scripts.mkdir(parents=True)
            shutil.copy(source, scripts / 'device-checks.sh')
            # Model Maestro 2.11.0's documented default: uninstall on command close.
            (scripts / 'maestro.sh').write_text('''set -e
printf 'driver:%s\\n' "$*" >> "$SPOTLIGHT_DRIVER_EVENTS"
touch "$SPOTLIGHT_DRIVER_READY"
if [[ "$*" != *--reinstall-driver=false* ]]; then
  rm "$SPOTLIGHT_DRIVER_READY"
fi
''')
            binary = root / 'bin'
            binary.mkdir()
            adb = binary / 'adb'
            adb.write_text('''#!/usr/bin/env bash
set -e
if [[ "$*" == *"am start"* ]]; then
  [[ -f "$SPOTLIGHT_DRIVER_READY" ]] || exit 86
  printf 'invoke\\n' >> "$SPOTLIGHT_DRIVER_EVENTS"
fi
''')
            adb.chmod(0o755)
            events = root / 'events'
            environment = os.environ.copy()
            environment.update(PATH=str(binary) + os.pathsep + environment['PATH'], CI='false',
                SPOTLIGHT_DRIVER_READY=str(root / 'ready'), SPOTLIGHT_DRIVER_EVENTS=str(events))
            result = subprocess.run(['bash', str(scripts / 'device-checks.sh')], env=environment,
                capture_output=True, text=True, timeout=10)
            self.assertEqual(0, result.returncode, result.stderr)
            lines = events.read_text().splitlines()
            self.assertIn('driver-ready.yaml', lines[0])
            self.assertEqual('invoke', lines[1])
            self.assertEqual(2, lines.count('invoke'))
            self.assertEqual(5, sum(line.startswith('driver:') for line in lines))
            self.assertTrue((root / 'ready').exists())
