#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
adb get-state >/dev/null || { echo 'Se requiere dispositivo para Perfetto.' >&2; exit 2; }
mkdir -p "$root/.codex/artifacts"
adb push "$root/.codex/perfetto/spotlight.cfg" /data/local/tmp/spotlight.cfg
printf 'Durante los siguientes 15 segundos: invocar, escribir, abrir/cerrar PDF y menú.\n'
adb shell perfetto --txt -c /data/local/tmp/spotlight.cfg -o /data/misc/perfetto-traces/spotlight.pftrace
adb pull /data/misc/perfetto-traces/spotlight.pftrace "$root/.codex/artifacts/spotlight.pftrace"
