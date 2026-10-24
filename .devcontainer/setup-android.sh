#!/usr/bin/env bash
# Run explicitly inside the Codespace. No signing, tests, APK, prebuild or credentials.
set -euo pipefail
root=/workspaces/.spotlight-tools
mkdir -p "$root/downloads" "$root/android-sdk/platforms" "$root/android-sdk/build-tools"
javac -version
# Fail before large downloads if official repositories are inaccessible.
curl --fail --silent --show-error --head --max-time 30 https://repo.maven.apache.org/maven2/ >/dev/null
curl --fail --silent --show-error --head --max-time 30 https://dl.google.com/android/maven2/master-index.xml >/dev/null
fetch() {
 local name="$1" url="$2" alg="$3" expected="$4"
 local dest="$root/downloads/$name"
 if [[ ! -f "$dest" ]]; then
  curl --fail --location --proto '=https' --proto-redir '=https' --retry 0 --max-time 600 "$url" -o "$dest.part"
  mv "$dest.part" "$dest"
 fi
 printf '%s  %s\n' "$expected" "$dest" | "${alg}sum" --check -
}
fetch gradle.zip https://github.com/gradle/gradle-distributions/releases/download/v8.10.2/gradle-8.10.2-bin.zip sha256 31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26
fetch platform.zip https://dl.google.com/android/repository/platform-35_r02.zip sha1 0bb560a90a7a2cbd0dd8348224d518b638fe7949
fetch build-tools.zip https://dl.google.com/android/repository/build-tools_r35_linux.zip sha1 2cfaa0bbb2336e9ec18ed3ecea84fa2e2af607bc
if [[ ! -x "$root/gradle-8.10.2/bin/gradle" ]]; then unzip -q "$root/downloads/gradle.zip" -d "$root"; fi
tmp=$(mktemp -d "$root/extract.XXXXXX")
trap 'rm -rf "$tmp"' EXIT
if [[ ! -f "$root/android-sdk/platforms/android-35/android.jar" ]]; then
 unzip -q "$root/downloads/platform.zip" -d "$tmp/platform"
 mv "$tmp/platform/"* "$root/android-sdk/platforms/android-35"
fi
if [[ ! -f "$root/android-sdk/build-tools/35.0.0/source.properties" ]]; then
 unzip -q "$root/downloads/build-tools.zip" -d "$tmp/build"
 mv "$tmp/build/"* "$root/android-sdk/build-tools/35.0.0"
fi
echo 'Official repository probes and archive checksums passed. SDK/Gradle stored under /workspaces.'
echo 'Android licenses/platform-tools may still require the normal SDK setup before a build.'
echo 'No signing key installed. Stop this Codespace after work; do not delete it to retain caches.'
