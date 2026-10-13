"""Bounded delivery of one reviewed source; never reads signing secrets."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import urllib.request
import zipfile

REPO = "bauti3311-sys/Spotlight-Next"
SOURCE = "9bb9d0ca7a97e9798ae5197c3ee32958e643dd6d"
BASELINE_APK_SHA256 = "563b4f9f2ec6538ffd66ca11132bd73cdff6ac9680516112f76e69ef005c83b9"
REQUIRED = (".github/workflows/build-apk.yml", ".github/workflows/pdf-device-check.yml")


def select_checks(runs, sha):
    if sha != SOURCE:
        raise ValueError("This delivery authorizes only the pinned reviewed SHA")
    selected = []
    for path in REQUIRED:
        candidates = [r for r in runs if r.get("head_sha") == sha
                      and r.get("event") == "pull_request"
                      and r.get("path", "").split("@")[0] == path
                      and r.get("head_repository", {}).get("full_name") == REPO]
        if not candidates:
            raise ValueError(f"Missing source check: {path}")
        latest = max(candidates, key=lambda r: r["id"])
        if latest.get("status") != "completed" or latest.get("conclusion") != "success":
            raise ValueError(f"Latest source check did not succeed: {path}")
        selected.append({key: latest[key] for key in ("id", "path", "head_sha", "conclusion", "html_url")})
    return selected


def require_checks(sha):
    request = urllib.request.Request(
        f"https://api.github.com/repos/{REPO}/actions/runs?head_sha={sha}&event=pull_request&per_page=100",
        headers={"Authorization": "Bearer " + os.environ["GH_TOKEN"],
                 "Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2022-11-28"})
    with urllib.request.urlopen(request, timeout=30) as response:
        selected = select_checks(json.load(response)["workflow_runs"], sha)
    Path("verified-checks.json").write_text(json.dumps(selected, indent=2) + "\n")
    print("Both source checks succeeded on", sha)


def extract_baseline(archive, destination):
    with zipfile.ZipFile(archive) as z:
        candidates = [n for n in z.namelist() if n.endswith(".apk")]
        if len(candidates) != 1:
            raise ValueError("Historical artifact must contain exactly one APK")
        data = z.read(candidates[0])
    if hashlib.sha256(data).hexdigest() != BASELINE_APK_SHA256:
        raise ValueError("Baseline APK differs from the recorded distributed 2.0.0 APK")
    Path(destination).write_bytes(data)


def identity(badging):
    match = re.search(r"^package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging, re.M)
    if not match:
        raise ValueError("Missing APK identity")
    return match[1], int(match[2]), match[3]


def package(sha, baseline_badging, baseline_cert):
    if sha != SOURCE or subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip() != sha:
        raise ValueError("Checkout differs from the reviewed source")
    sys.path.insert(0, str(Path("scripts").resolve()))
    from verify_release import verify, verify_certificate
    old = Path(baseline_badging).read_text()
    if identity(old) != ("dev.bautista.spotlight.next", 27, "2.0.0"):
        raise ValueError("Wrong historical distribution identity")
    verify_certificate(Path(baseline_cert).read_text())
    verify(old, 26)
    signed = Path("app/build/outputs/verified-release/spotlight-next-release.apk")
    tools = Path(os.environ["ANDROID_HOME"]) / "build-tools/35.0.0"
    new = subprocess.check_output([str(tools / "aapt"), "dump", "badging", str(signed)], text=True)
    cert = subprocess.check_output([str(tools / "apksigner"), "verify", "--verbose", "--print-certs", str(signed)], text=True)
    if identity(new) != ("dev.bautista.spotlight.next", 28, "2.0.1"):
        raise ValueError("Wrong new release identity")
    report = verify(new, 27) + verify_certificate(cert)
    tracked = subprocess.check_output(["git", "ls-files"], text=True).splitlines()
    if any(p.endswith((".keystore", ".jks")) or p == "signing/credentials.properties" for p in tracked):
        raise ValueError("Private signing material must not appear in source ZIP")
    output = Path("app/build/outputs/delivery")
    output.mkdir(parents=True, exist_ok=True)
    apk = output / "Spotlight-Next-2.0.1-arm64-release.apk"
    shutil.copyfile(signed, apk)
    source = output / "Spotlight-Next-2.0.1-source-9bb9d0c.zip"
    subprocess.run(["git", "archive", "--format=zip", "--prefix=Spotlight-Next-2.0.1/", f"--output={source}", sha], check=True)
    with zipfile.ZipFile(source) as z:
        if z.comment.decode() != sha:
            raise ValueError("ZIP commit metadata differs from built source")
    manifest = {"source_sha": sha, "workflow_sha": os.environ.get("GITHUB_SHA"),
                "baseline_version": "2.0.0", "baseline_version_code": 27,
                "baseline_apk_sha256": BASELINE_APK_SHA256,
                "version": "2.0.1", "version_code": 28,
                "checks": json.loads(Path("verified-checks.json").read_text()),
                "artifacts": {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in (apk, source)},
                "physical_update_tested": False}
    (output / "delivery-manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    (output / "update-verification.txt").write_text(report + "Baseline APK bytes and certificate verified against distributed 2.0.0/27.\nPhysical installation on Galaxy Tab A9+ has not been performed.\n")
    (output / "INSTALACION.md").write_text(
        "# Spotlight Next 2.0.1 / 28\n\nInstalar el APK encima de Spotlight Next con la firma histórica, sin desinstalar. "
        "El paquete y certificado coinciden con la distribución 2.0.0/27 y el código aumenta a 28. "
        "No se efectuó una instalación física en la Galaxy Tab A9+.\n\n"
        f"APK y ZIP proceden del commit `{sha}`. Consultar delivery-manifest.json, update-verification.txt y los informes de CI.\n")
    print(report)


if __name__ == "__main__":
    command, *args = sys.argv[1:]
    if command == "checks":
        require_checks(*args)
    elif command == "baseline":
        extract_baseline(*args)
    elif command == "package":
        package(*args)
    else:
        raise ValueError("Unknown delivery command")
