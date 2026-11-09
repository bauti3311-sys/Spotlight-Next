"""Independent checks of downloaded artifacts; no build, signing, install or app execution."""
import hashlib,json,os,shutil,subprocess,sys
from pathlib import Path
sys.path.insert(0,'/workspace/Spotlight-Next/scripts')
import preview_delivery_verify as verify
from preview_delivery_policy import SOURCE,APK_NAME,ZIP_NAME,CERTIFICATE,VERSION_CODE,VERSION_NAME,PACKAGE
root=Path('/workspace/shared/downloads/spotlight-preview71/artifact')
tools=Path('/workspace/android-sdk/build-tools/35.0.0')
os.environ.update(GITHUB_WORKFLOW_SHA='604544872a70a609e2307ce0c61af6edb99b2ccb',GITHUB_RUN_ID='42635574367',JAVA_HOME='/workspace/jdk17/jdk-17.0.21+9')
os.environ['PATH']=os.environ['JAVA_HOME']+'/bin:'+os.environ['PATH']
assert SOURCE=='b8f8e268dd59e6410ec57a102a0e35231db01b18' and VERSION_CODE==71
ci=root/'ci-evidence';ci.mkdir(exist_ok=True)
for name in ['signature.txt','package.txt','SHA256SUMS','SOURCE_COMMIT','WORKFLOW_COMMIT','provenance.json']:
 shutil.copy2(root/name,ci/name)
verify.main('signed',root,tools)
expected=Path('/workspace/scratch/preview71/source-expected.zip')
assert verify.sha(expected)==verify.sha(root/ZIP_NAME),'Source archive differs from independent exact git archive'
ci_sums={l.split()[1]:l.split()[0] for l in (ci/'SHA256SUMS').read_text().splitlines()}
hashes={p.name:verify.sha(p) for p in [root/APK_NAME,root/ZIP_NAME]}
assert ci_sums==hashes
assert (ci/'SOURCE_COMMIT').read_text().strip()==SOURCE
assert (ci/'WORKFLOW_COMMIT').read_text().strip()==os.environ['GITHUB_WORKFLOW_SHA']
prior=Path('/workspace/shared/downloads/spotlight-preview70/artifact/Spotlight-Next-Preview70-143104c.apk')
prior_signature=verify.run(tools/'apksigner','verify','--verbose','--print-certs',prior);verify.certificate(prior_signature)
prior_metadata=verify.run(tools/'aapt','dump','badging',prior)
assert f"package: name='{PACKAGE}' versionCode='70'" in prior_metadata
(root/'previous70-signature.txt').write_text(prior_signature)
result={'source_sha':SOURCE,'workflow_sha':os.environ['GITHUB_WORKFLOW_SHA'],'run_id':42635574367,
 'independent_local_apk_checks':'passed','hashes':hashes,'certificate_sha256':CERTIFICATE,'signers':1,'v2_verified':True,
 'package':PACKAGE,'version_code':VERSION_CODE,'version_name':VERSION_NAME,'debuggable':False,'abi':['arm64-v8a'],'sdk':[26,35],
 'zipalign_16KiB':True,'native_ELF_16KiB':True,'payload_matches_unsigned_provenance':True,
 'source_zip_matches_independent_git_archive_byte_for_byte':True,'same_certificate_as_locally_verified_Preview70':True,
 'update_compatibility':'expected: same package/certificate and 71>70; physical update NOT tested',
 'build_source_correspondence':'CI exact checkout + run provenance + source archive + signed/unsigned payload digests; no independent rebuild',
 'physical_execution_or_installation':False}
(root/'independent-verification.json').write_text(json.dumps(result,indent=2)+'\n')
import zipfile
with zipfile.ZipFile(root/ZIP_NAME) as z:
 prefix='Spotlight-Next-source-'+SOURCE[:7]+'/'
 assert not any(n.endswith('/DocumentCitations.kt') or n.endswith('/DocumentCitationState.kt') for n in z.namelist())
 index='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt'
 assert z.read(prefix+index)==subprocess.check_output(['git','show','143104ce8d83f6ad6b5b4acd80897db579816fcd:'+index])
 paths=subprocess.check_output(['git','diff','--name-only','42ac439936dd7e47ac906af3719eb273eef6b20f','867a3f5132004110a8c3ea39c03bba1430b12f2e','--','app/src/main'],text=True).splitlines()
 for path in paths:
  assert z.read(prefix+path)==subprocess.check_output(['git','show','867a3f5132004110a8c3ea39c03bba1430b12f2e:'+path])
result['C5_absent_and_fileindex_matches_Preview70']=True
result['eight_settings_width_files_match_approved_implementation']=True
(root/'independent-verification.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
