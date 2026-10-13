#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
binary="$root/.codex/tools/maestro/2.11.0/maestro/bin/maestro"
if [[ ! -x "$binary" ]]; then echo 'Instalá Maestro: bash .codex/scripts/install-tool.sh maestro' >&2; exit 2; fi
export MAESTRO_CLI_NO_ANALYTICS=1
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true
exec "$binary" "$@"
