#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
binary="$root/.codex/tools/gitleaks/8.30.1/gitleaks"
if [[ ! -x "$binary" ]]; then echo 'Instalá Gitleaks: bash .codex/scripts/install-tool.sh gitleaks' >&2; exit 2; fi
mkdir -p "$root/.codex/artifacts"
exec "$binary" git "$root" --redact --report-format json --report-path "$root/.codex/artifacts/gitleaks.json"
