from pathlib import Path
import json,hashlib,subprocess,xml.etree.ElementTree as E
from collections import Counter
O=Path(__file__).resolve().parent;R=O.parents[2]
classes={
 'dev.bautista.spotlight.data.WindowSizePolicyTest':'second-checks/xml',
 'dev.bautista.spotlight.overlay.OverlayImeHeightTest':'second-checks/xml',
 'dev.bautista.spotlight.ui.OverlayWidthSettingsTest':'second-checks/xml',
 'dev.bautista.spotlight.ui.PuenteFrameTest':'first-checks/xml',
 'dev.bautista.spotlight.ui.WidthResponseTest':'first-checks/xml',
 'dev.bautista.spotlight.ui.SettingsReviewCaptureTest':'second-checks/xml',
 'dev.bautista.spotlight.ui.SettingsRefreshTest':'after/xml',
 'dev.bautista.spotlight.ui.SettingsWorkspaceTest':'navigation-final/xml',
}
summaries=[]
for name,folder in classes.items():
 p=O/folder/('TEST-'+name+'.xml');r=E.parse(p).getroot();assert int(r.attrib['failures'])==0 and int(r.attrib['errors'])==0,name
 summaries.append({'class':name,'xml':str(p.relative_to(O)),'tests':int(r.attrib['tests']),'failures':0,'cases':[x.attrib['name'] for x in r.findall('testcase')]})
issues=E.parse(O/'navigation-final/lint-results-debug.xml').getroot().findall('issue');counts=dict(Counter(i.attrib['severity'] for i in issues));assert not counts.get('Error',0) and not counts.get('Fatal',0)
base='42ac439936dd7e47ac906af3719eb273eef6b20f'
allowed={'MainActivity.kt','data/WindowSizePolicy.kt','overlay/OverlayService.kt','ui/GlassAppearance.kt','ui/PuenteFrame.kt','ui/SettingsHome.kt','ui/SettingsNavigation.kt','ui/WindowSizeSettings.kt'}
root='app/src/main/java/dev/bautista/spotlight/'
changed=subprocess.check_output(['git','diff','--name-only',base,'--','app/src/main'],cwd=R,text=True).splitlines();assert set(changed)=={root+p for p in allowed},changed
protected=['.github','scripts','gradle','app/build.gradle.kts','release.properties','app/src/main/AndroidManifest.xml','signing','docs/verification/function-lab-20261109','docs/verification/choice-catalogue-20261108']
assert not subprocess.check_output(['git','diff','--name-only',base,'--',*protected],cwd=R,text=True).strip()
refs={}
for ref in ['refs/heads/main','refs/heads/feat/preview68-visual-isolated-20261107','refs/heads/feat/preview69-c2-isolated-20261108','refs/heads/feat/preview70-puente-isolated-20261109']:
 refs[ref]=subprocess.check_output(['git','ls-remote','origin',ref],cwd=R,text=True).split()[0]
result={'baseline':base,'scope':'Compose/Robolectric JVM; not hardware','tests':summaries,'unique_passed_cases':sum(x['tests'] for x in summaries),'lint':{'Error':0,**counts},'missing_runtime':'API28 not cached; no successful download, no API28 coverage claimed','runtimes_used':[33,35],'product_files_changed':changed,'protected_paths_unchanged':protected,'preserved_remote_refs':refs,'apk_actions_installation':False,'images':[{ 'path':str(p.relative_to(O)),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for phase in ['before','after'] for p in sorted((O/phase).glob('*.png'))]}
assert result['unique_passed_cases']==40,result['unique_passed_cases']
(O/'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n');print('40 casos únicos correctos; lint0 errores; ocho archivos productivos permitidos y fuentes protegidas intactas.')
