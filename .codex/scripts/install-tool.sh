#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
case "${1:-}" in
  maestro)
    version=2.11.0
    archive=maestro.zip
    url=https://github.com/mobile-dev-inc/Maestro/releases/download/cli-2.11.0/maestro.zip
    digest=5384593cb4e7a106489e75a821d157dd43f4e438df6bc308b72e82c685e1283a
    ;;
  gitleaks)
    version=8.30.1
    archive=gitleaks_8.30.1_linux_x64.tar.gz
    url=https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_linux_x64.tar.gz
    digest=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb
    ;;
  *) echo 'Uso: install-tool.sh maestro|gitleaks (una herramienta por vez)' >&2; exit 2 ;;
esac
destination="$root/.codex/tools/$1/$version"
if [[ -f "$destination/.verified-download" ]]; then printf '%s ya verificado: %s\n' "$1" "$destination"; exit 0; fi
mkdir -p "$destination"
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT
curl --fail --location --silent --show-error "$url" -o "$scratch/$archive"
printf '%s  %s\n' "$digest" "$scratch/$archive" | sha256sum --check --status
if [[ "$1" == maestro ]]; then unzip -q "$scratch/$archive" -d "$destination"; else tar -xzf "$scratch/$archive" -C "$destination" gitleaks LICENSE; fi
printf '%s\n' "$digest" > "$destination/.verified-download"
printf 'Herramienta local verificada: %s %s\n' "$1" "$version"
