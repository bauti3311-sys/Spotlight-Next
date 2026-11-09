from pathlib import Path
import subprocess,json
R=Path(__file__).resolve().parents[3];BASE='cbc956ab56756d9ebdf7de05300fb0b5f5b14a19';SOURCE='143104ce8d83f6ad6b5b4acd80897db579816fcd';NATIVE='0b1441e85cc736026f5bee81e1c23eef869e70c1'
def git(*args):return subprocess.check_output(['git',*args],cwd=R,text=True).strip()
paths=git('diff','--name-only',BASE,SOURCE).splitlines();expected=['app/src/main/java/dev/bautista/spotlight/ui/ApplicationInspector.kt', 'app/src/main/java/dev/bautista/spotlight/ui/ContextSplitPanel.kt', 'app/src/main/java/dev/bautista/spotlight/ui/GlassContextFooter.kt', 'app/src/main/java/dev/bautista/spotlight/ui/SpotlightPanel.kt', 'app/src/main/java/dev/bautista/spotlight/ui/StudioSurface.kt', 'app/src/main/java/dev/bautista/spotlight/ui/PuenteFrame.kt', 'app/src/test/java/dev/bautista/spotlight/ui/PuenteFrameTest.kt', 'app/src/test/java/dev/bautista/spotlight/ui/PuenteNativeIntegrationTest.kt', 'app/build.gradle.kts'];assert set(paths)==set(expected) and len(paths)==9
for p in paths:
 if p!='app/build.gradle.kts':assert git('rev-parse',SOURCE+':'+p)==git('rev-parse',NATIVE+':'+p),p
expected_build=git('show',BASE+':app/build.gradle.kts').replace('native57-v69','native57-v70').replace('output.versionCode.set(69)','output.versionCode.set(70)');assert git('show',SOURCE+':app/build.gradle.kts')==expected_build
for name in ['DocumentCitationState.kt','DocumentCitations.kt']:
 assert subprocess.run(['git','cat-file','-e',SOURCE+':app/src/main/java/dev/bautista/spotlight/index/'+name],cwd=R,capture_output=True).returncode!=0
index='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt';assert git('rev-parse',SOURCE+':'+index)==git('rev-parse',BASE+':'+index)
for path in ['app/src/main/java/dev/bautista/spotlight/data/Preferences.kt','app/src/main/AndroidManifest.xml','app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt','release.properties','.github/workflows/build-apk.yml']:
 assert git('rev-parse',SOURCE+':'+path)==git('rev-parse',BASE+':'+path),path
assert 'focusInterface:Boolean=false' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/data/Preferences.kt')
assert 'setDuration(140L)' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt')
git('merge-base','--is-ancestor',BASE,SOURCE)
report={'source':SOURCE,'base':BASE,'native_implementation':NATIVE,'allowed_paths':paths,'C5_absent':True,'fileindex_blob_unchanged':True,'preserved':'all other tracked paths identical to Preview69, including permissions/signing/package/dependencies/preferences/ranking/actions/140ms'}
Path(__file__).with_name('isolation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
