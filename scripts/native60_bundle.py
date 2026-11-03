"""Package a single unsigned build + exact git archive; no signing input accepted."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
from native60_policy import SOURCE
from native60_verify import sha, payload, main as verify

source=Path(sys.argv[1]).resolve()
out=Path(sys.argv[2]).resolve()
tools=Path(sys.argv[3]).resolve()
if subprocess.check_output(['git','-C',str(source),'rev-parse','HEAD'],text=True).strip()!=SOURCE:
    sys.exit('Wrong approved source.')
subprocess.run(['git','-C',str(source),'diff','--exit-code'],check=True,capture_output=True)
candidates=list((source/'app/build/outputs/apk/raycastPreview').glob('*.apk'))
if len(candidates)!=1: sys.exit('Expected exactly one unsigned APK output.')
out.mkdir(parents=True,exist_ok=True)
shutil.copy2(candidates[0],out/'unsigned.apk')
subprocess.run(['git','-C',str(source),'archive','--format=zip','--prefix=Spotlight-Next-source-e9647c1/',
                '--output='+str(out/'source.zip'),SOURCE],check=True,capture_output=True)
info={'source_sha':SOURCE,'workflow_sha':os.environ['GITHUB_WORKFLOW_SHA'],'run_id':os.environ['GITHUB_RUN_ID'],
      'profile':'optimized','unsigned_sha256':sha(out/'unsigned.apk'),'source_zip_sha256':sha(out/'source.zip'),
      'payload':payload(out/'unsigned.apk')}
(out/'provenance.json').write_text(json.dumps(info,indent=2)+'\n')
verify('unsigned',out,tools)
