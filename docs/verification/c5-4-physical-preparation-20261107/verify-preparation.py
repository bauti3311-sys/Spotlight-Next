"""Read-only preparation checks; never runs Gradle, adb instrumentation, installs or downloads."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[3]
here = Path(__file__).resolve().parent
p = argparse.ArgumentParser()
p.add_argument("--compile-log", type=Path, required=True)
p.add_argument("--apk-inventory-before", type=Path, required=True)
args = p.parse_args()

def forbidden(task):
    if task.startswith(("assemble", "install", "uninstall", "connected", "sign", "validateSigning")):
        return True
    if task.startswith("package") and not task.endswith("Resources"):
        return True
    return task.startswith("bundle") and not task.endswith("Jar")

assert forbidden("packageDebug") and forbidden("packageDebugAndroidTest")
assert forbidden("connectedDebugAndroidTest") and forbidden("validateSigningDebug")
assert not forbidden("compileDebugAndroidTestKotlin")
assert not forbidden("packageDebugResources")
assert not forbidden("bundleDebugClassesToCompileJar")
plan = (here / "compile-dry-run.log").read_text()
tasks = re.findall(r"^:app:(\S+) SKIPPED$", plan, re.M)
assert tasks and "BUILD SUCCESSFUL" in plan
assert not any(forbidden(x) for x in tasks)
compiled = args.compile_log.read_text()
assert "BUILD SUCCESSFUL" in compiled
actual_tasks = re.findall(r"^> Task :app:(\S+)", compiled, re.M)
assert not any(forbidden(x) for x in actual_tasks)
before = json.loads(args.apk_inventory_before.read_text())
after = {str(f.relative_to(root)): [f.stat().st_size, f.stat().st_mtime_ns]
         for f in (root / "app/build").rglob("*.apk")}
assert before == after, "APK inventory changed"

asset = root / "app/src/androidTest/assets/c5"
sources = {
    "corpus.json": root / "app/src/test/resources/c5-3/corpus.json",
    "real-three-pages.pdf": root / "docs/verification/c5-2-provenance-20261107/pdf/real-three-pages.pdf",
    "ocr-raster-fixture.pdf": root / "docs/verification/c5-3-semantic-20261107/final/ocr-raster-fixture.pdf",
}
hashes = {}
for name, source in sources.items():
    assert (asset / name).read_bytes() == source.read_bytes(), name
    hashes[name] = hashlib.sha256(source.read_bytes()).hexdigest()
baseline = json.loads((root / "docs/verification/c5-3-semantic-20261107/final/evaluation.json").read_text())
assert baseline["lexical"]["tp"] == 7 and baseline["lexical"]["fn"] == 5
assert baseline["lexical"]["fp"] == 0 and baseline["semantic_quality"] is None
manifest = ET.parse(root / "app/src/androidTest/AndroidManifest.xml").getroot()
assert not manifest.findall("uses-permission"), "No new uses-permission declarations allowed"
provider = manifest.find("application/provider")
assert provider is not None
android = "{http://schemas.android.com/apk/res/android}"
assert provider.get(android + "permission") == "android.permission.MANAGE_DOCUMENTS"
assert provider.get(android + "authorities") == "dev.bautista.spotlight.c5.physicalfixtures"
subprocess.run(["git", "diff", "--exit-code", "d712a14", "--", "app/src/main",
                "app/build.gradle.kts", ".github/workflows", "scripts/preview_delivery.json",
                "release.properties"], cwd=root, check=True)
tests = sum(len(re.findall(r"@Test\s+fun\s+", f.read_text()))
            for f in (root / "app/src/androidTest/java/dev/bautista/spotlight/c5").glob("*.kt"))
assert tests == 5
print(json.dumps({"preparation": "VALIDATED", "prepared_instrumented_methods": tests,
                  "instrumented_methods_executed": 0, "apk_inventory_unchanged": True,
                  "new_apk_count": 0, "production_unchanged": True,
                  "c53_accepted_results_unchanged": True,
                  "physical_status": "NOT_EXECUTED", "semantic_metrics_new": None,
                  "asset_sha256": hashes}, indent=2))
