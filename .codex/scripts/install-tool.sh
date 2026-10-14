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
  actionlint)
    version=1.7.12
    archive=actionlint_1.7.12_linux_amd64.tar.gz
    url=https://github.com/rhysd/actionlint/releases/download/v1.7.12/actionlint_1.7.12_linux_amd64.tar.gz
    digest=8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8
    ;;
  perfetto)
    version=58.2
    archive=linux-amd64.zip
    url=https://github.com/google/perfetto/releases/download/v58.2/linux-amd64.zip
    digest=32c739f71b2d39721afd294c0b038f2499a44a71b5b6bcdd85b83faca0b240b9
    ;;
  gitleaks)
    version=8.30.1
    archive=gitleaks_8.30.1_linux_x64.tar.gz
    url=https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_linux_x64.tar.gz
    digest=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb
    ;;
  *) echo 'Uso: install-tool.sh maestro|gitleaks|perfetto|actionlint (una herramienta por vez)' >&2; exit 2 ;;
esac
destination="$root/.codex/tools/$1/$version"
if [[ -f "$destination/.verified-download" ]]; then
  if [[ "$1" == perfetto ]]; then chmod +x "$destination/linux-amd64/trace_processor_shell"; fi
  printf '%s ya verificado: %s\n' "$1" "$destination"; exit 0; fi
mkdir -p "$destination"
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT
curl --fail --location --silent --show-error "$url" -o "$scratch/$archive"
printf '%s  %s\n' "$digest" "$scratch/$archive" | sha256sum --check --status
if [[ "$1" == maestro ]]; then
  unzip -q "$scratch/$archive" -d "$destination"
elif [[ "$1" == perfetto ]]; then
  unzip -q "$scratch/$archive" linux-amd64/trace_processor_shell -d "$destination"
  chmod +x "$destination/linux-amd64/trace_processor_shell"
elif [[ "$1" == actionlint ]]; then
  tar -xzf "$scratch/$archive" -C "$destination" actionlint LICENSE.txt
else
  tar -xzf "$scratch/$archive" -C "$destination" gitleaks LICENSE
fi
printf '%s\n' "$digest" > "$destination/.verified-download"
printf 'Herramienta local verificada: %s %s\n' "$1" "$version"
