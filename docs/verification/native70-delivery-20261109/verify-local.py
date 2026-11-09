"""Independent checks of downloaded artifacts; no build, signing, install or app execution."""
import hashlib,json,os,shutil,subprocess,sys
from pathlib import Path
sys.path.insert(0,'/workspace/Spotlight-Next/scripts')
import preview_delivery_verify as verify
from preview_delivery_policy import SOURCE,APK_NAME,ZIP_NAME,CERTIFICATE,VERSION_CODE,VERSION_NAME,PACKAGE
root=Path('/workspace/shared/downloads/spotlight-preview70/artifact')
tools=Path('/workspace/android-sdk/build-tools/35.0.0')
os.environ.update(GITHUB_WORKFLOW_SHA='824f3417951c20a0757a46ee27afe8eb7373b719',GITHUB_RUN_ID='42560494613',JAVA_HOME='/workspace/jdk17/jdk-17.0.21+9')
os.environ['PATH']=os.environ['JAVA_HOME']+'/bin:'+os.environ['PATH']
assert SOURCE=='143104ce8d83f6ad6b5b4acd80897db579816fcd' and VERSION_CODE==70
ci=root/'ci-evidence';ci.mkdir(exist_ok=True)
for name in ['signature.txt','package.txt','SHA256SUMS','SOURCE_COMMIT','WORKFLOW_COMMIT','provenance.json']:
 shutil.copy2(root/name,ci/name)
verify.main('signed',root,tools)
expected=Path('/workspace/scratch/preview70/source-expected.zip')
assert verify.sha(expected)==verify.sha(root/ZIP_NAME),'Source archive differs from independent exact git archive'
ci_sums={l.split()[1]:l.split()[0] for l in (ci/'SHA256SUMS').read_text().splitlines()}
hashes={p.name:verify.sha(p) for p in [root/APK_NAME,root/ZIP_NAME]}
assert ci_sums==hashes
assert (ci/'SOURCE_COMMIT').read_text().strip()==SOURCE
assert (ci/'WORKFLOW_COMMIT').read_text().strip()==os.environ['GITHUB_WORKFLOW_SHA']
prior=Path('/workspace/shared/downloads/spotlight-preview69/artifact/Spotlight-Next-Preview69-cbc956a.apk')
prior_signature=verify.run(tools/'apksigner','verify','--verbose','--print-certs',prior);verify.certificate(prior_signature)
prior_metadata=verify.run(tools/'aapt','dump','badging',prior)
assert f"package: name='{PACKAGE}' versionCode='69'" in prior_metadata
(root/'previous69-signature.txt').write_text(prior_signature)
result={'source_sha':SOURCE,'workflow_sha':os.environ['GITHUB_WORKFLOW_SHA'],'run_id':42560494613,
 'independent_local_apk_checks':'passed','hashes':hashes,'certificate_sha256':CERTIFICATE,'signers':1,'v2_verified':True,
 'package':PACKAGE,'version_code':VERSION_CODE,'version_name':VERSION_NAME,'debuggable':False,'abi':['arm64-v8a'],'sdk':[26,35],
 'zipalign_16KiB':True,'native_ELF_16KiB':True,'payload_matches_unsigned_provenance':True,
 'source_zip_matches_independent_git_archive_byte_for_byte':True,'same_certificate_as_locally_verified_Preview69':True,
 'update_compatibility':'expected: same package/certificate and 70>69; physical update NOT tested',
 'build_source_correspondence':'CI exact checkout + run provenance + source archive + signed/unsigned payload digests; no independent rebuild',
 'physical_execution_or_installation':False}
(root/'independent-verification.json').write_text(json.dumps(result,indent=2)+'\n')
import zipfile
with zipfile.ZipFile(root/ZIP_NAME) as z:
 prefix='Spotlight-Next-source-'+SOURCE[:7]+'/'
 assert not any(n.endswith('/DocumentCitations.kt') or n.endswith('/DocumentCitationState.kt') for n in z.namelist())
 index='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt'
 assert z.read(prefix+index)==subprocess.check_output(['git','show','cbc956ab56756d9ebdf7de05300fb0b5f5b14a19:'+index])
 for name in ['ApplicationInspector.kt','ContextSplitPanel.kt','GlassContextFooter.kt','PuenteFrame.kt','SpotlightPanel.kt','StudioSurface.kt']:
  path='app/src/main/java/dev/bautista/spotlight/ui/'+name
  assert z.read(prefix+path)==subprocess.check_output(['git','show','0b1441e85cc736026f5bee81e1c23eef869e70c1:'+path])
result['C5_absent_and_fileindex_matches_Preview69']=True
result['six_Puente_files_match_approved_implementation']=True
(root/'independent-verification.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
