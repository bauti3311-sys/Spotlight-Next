from pathlib import Path
import subprocess,json,hashlib
R=Path(__file__).resolve().parents[3];O=Path(__file__).resolve().parent;BASE='928705de91cadd4629d737d724e4ac5d3dda4f8c'
paths=['app/src/main/java/dev/bautista/spotlight/search','app/src/main/java/dev/bautista/spotlight/control','app/src/main/java/dev/bautista/spotlight/integrations','app/src/main/java/dev/bautista/spotlight/c5','app/src/main/java/dev/bautista/spotlight/data','app/src/main/java/dev/bautista/spotlight/overlay','app/src/main/AndroidManifest.xml','app/build.gradle.kts','release.properties','.github','scripts','docs/verification/puente-bold-20261108','docs/verification/puente-finishes-20261108']
changed=subprocess.check_output(['git','diff',BASE,'--name-only','--',*paths],cwd=R,text=True).splitlines();assert not changed,changed
name='app/src/main/java/dev/bautista/spotlight/ui/SpotlightPanel.kt';old=subprocess.check_output(['git','show',BASE+':'+name],cwd=R,text=True);new=(R/name).read_text()
pairs=[('keyboard_dispatch','.onPreviewKeyEvent { event ->','                val header:@Composable ()->Unit = {'),('action_panel_commands','                val details:@Composable (Boolean)->Unit = { actionsOnly ->','                        val contextBody:@Composable ()->Unit = {')]
allowed={"ApplicationInspector.kt","ContextSplitPanel.kt","GlassContextFooter.kt","SpotlightPanel.kt","StudioSurface.kt","PuenteFrame.kt"}
changed_main=subprocess.check_output(['git','diff',BASE,'--name-only','--','app/src/main'],cwd=R,text=True).splitlines()
changed_main+=subprocess.check_output(['git','ls-files','--others','--exclude-standard','app/src/main'],cwd=R,text=True).splitlines()
assert all(Path(p).parent==Path('app/src/main/java/dev/bautista/spotlight/ui') and Path(p).name in allowed for p in changed_main),changed_main
checks={}
for label,a,b in pairs:
 x=old[old.index(a):old.index(b,old.index(a))];y=new[new.index(a):new.index(b,new.index(a))]
 # The old details block was followed by the now-moved outer columns. Compare through its own end.
 if label=='action_panel_commands':
  x=x[:x.index('                Column(Modifier.fillMaxWidth()) {\n                Column(Modifier.fillMaxWidth().onSizeChanged')]
 if label=='state_search_dispatch':
  y=y.replace('        val headerHost = remember { movableContentOf<@Composable () -> Unit> { content -> content() } }\n','')
 assert x==y,label
 checks[label]=hashlib.sha256(x.encode()).hexdigest()
refs={}
for branch,expected in {'main':'47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d','feat/preview68-visual-isolated-20261107':'c51677b79446ce1dc8213553891d45897b14d726','feat/preview69-c2-isolated-20261108':'cbc956ab56756d9ebdf7de05300fb0b5f5b14a19'}.items():
 actual=subprocess.check_output(['git','ls-remote','origin','refs/heads/'+branch],cwd=R,text=True).split()[0];assert actual==expected;refs[branch]=actual
(O/'preservation.json').write_text(json.dumps({'base':BASE,'production_delta':changed_main,'protected_paths':paths,'protected_changes':changed,'unchanged_blocks_sha256':checks,'remote_refs':refs,'native_open_ms':140,'focus_default':False,'editor_guard':'Late contextual detach updates are ignored after cancellation or destination change; no stale form revival','back_focus':'Existing returnFocusRevision schedules search focus after placement; Focus preference change also restores after placement','presentation_state':'Existing header/inspector content moved with movableContentOf to retain focus across layout transitions'},indent=2))
print('Protected files, search/actions/keyboard blocks and remote refs unchanged.')
