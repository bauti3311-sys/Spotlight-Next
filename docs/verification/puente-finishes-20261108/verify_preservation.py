from pathlib import Path
import subprocess,json,hashlib
R=Path(__file__).resolve().parents[3];O=Path(__file__).resolve().parent;BASE='4b2b9f8da6e99a1cfd71f9633e3d70d8aec92383'
protected=['app/src/main','app/build.gradle.kts','.github','scripts','release.properties','docs/verification/choice-catalogue-20261108','docs/verification/creative-directions-20261108','docs/verification/mesa-explorations-20261108','app/src/test/java/dev/bautista/spotlight/ui/MesaExplorationsGalleryTest.kt','app/src/test/java/dev/bautista/spotlight/ui/CreativeDirectionsGalleryTest.kt']
changed=subprocess.check_output(['git','diff',BASE,'--name-only','--',*protected],cwd=R,text=True).splitlines();assert not changed,changed
for it in json.loads((O/'reference-reuse.json').read_text()):
 assert hashlib.sha256((R/it['source']).read_bytes()).hexdigest()==it['sha256']==hashlib.sha256((O/'originals'/it['copy']).read_bytes()).hexdigest()
refs={}
for ref,expected in {'main':'47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d','feat/preview68-visual-isolated-20261107':'c51677b79446ce1dc8213553891d45897b14d726','feat/preview69-c2-isolated-20261108':'cbc956ab56756d9ebdf7de05300fb0b5f5b14a19'}.items():
 actual=subprocess.check_output(['git','ls-remote','origin','refs/heads/'+ref],cwd=R,text=True).split()[0];assert actual==expected;refs[ref]=actual
(O/'preservation.json').write_text(json.dumps({'base':BASE,'protected_paths':protected,'changed_protected_paths':changed,'references_exact':True,'remote_refs':refs},indent=2))
print('Protected trees and remote refs unchanged.')
