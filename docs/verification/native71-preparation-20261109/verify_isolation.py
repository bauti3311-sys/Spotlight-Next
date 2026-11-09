from pathlib import Path
import subprocess,json
R=Path(__file__).resolve().parents[3];BASE='143104ce8d83f6ad6b5b4acd80897db579816fcd';SOURCE='b8f8e268dd59e6410ec57a102a0e35231db01b18';NATIVE='867a3f5132004110a8c3ea39c03bba1430b12f2e';BEFORE='42ac439936dd7e47ac906af3719eb273eef6b20f'
def git(*args):return subprocess.check_output(['git',*args],cwd=R,text=True).strip()
expected=git('diff','--name-only',BEFORE,NATIVE,'--','app/src/main','app/src/test').splitlines()+['app/build.gradle.kts'];paths=git('diff','--name-only',BASE,SOURCE).splitlines();assert set(paths)==set(expected) and len(paths)==13
for p in paths:
 if p!='app/build.gradle.kts':assert git('rev-parse',SOURCE+':'+p)==git('rev-parse',NATIVE+':'+p),p
expected_build=git('show',BASE+':app/build.gradle.kts').replace('native57-v70','native57-v71').replace('output.versionCode.set(70)','output.versionCode.set(71)');assert git('show',SOURCE+':app/build.gradle.kts')==expected_build
for name in ['DocumentCitationState.kt','DocumentCitations.kt']:
 assert subprocess.run(['git','cat-file','-e',SOURCE+':app/src/main/java/dev/bautista/spotlight/index/'+name],cwd=R,capture_output=True).returncode!=0
index='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt';assert git('rev-parse',SOURCE+':'+index)==git('rev-parse',BASE+':'+index)
for path in ['app/src/main/java/dev/bautista/spotlight/data/Preferences.kt','app/src/main/AndroidManifest.xml','app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt','release.properties','.github/workflows/build-apk.yml']:
 assert git('rev-parse',SOURCE+':'+path)==git('rev-parse',BASE+':'+path),path
assert 'focusInterface:Boolean=false' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/data/Preferences.kt')
assert 'setDuration(140L)' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt')
git('merge-base','--is-ancestor',BASE,SOURCE)
report={'source':SOURCE,'base':BASE,'approved_implementation':NATIVE,'allowed_paths':paths,'C5_absent':True,'fileindex_blob_unchanged':True,'preserved':'all other paths identical to Preview70; package/certificate configuration, permissions, dependencies, preferences, ranking/actions and 140ms unchanged'}
Path(__file__).with_name('isolation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
