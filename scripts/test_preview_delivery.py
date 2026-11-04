"""No workflow dispatch, APK build, key generation or private signing input in tests."""
import copy
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
import zipfile
import yaml
from preview_delivery_policy import SOURCE, REPOSITORY, BRANCH, OWNER, ACK, CERTIFICATE, validate
from preview_delivery_verify import metadata, certificate, source_archive, safe_paths

ROOT=Path(__file__).resolve().parents[1]

class Native60PolicyTest(unittest.TestCase):
    def setUp(self):
        self.wf='a'*40;self.lease='b'*40
        self.env={'GITHUB_REPOSITORY':REPOSITORY,'GITHUB_ACTOR':OWNER,'GITHUB_TRIGGERING_ACTOR':OWNER,
            'GITHUB_EVENT_NAME':'workflow_dispatch','GITHUB_REF':'refs/heads/'+BRANCH,
            'GITHUB_WORKFLOW_REF':REPOSITORY+'/.github/workflows/build-apk.yml@refs/heads/'+BRANCH,
            'GITHUB_RUN_ATTEMPT':'1','REQUESTED_SOURCE_SHA':SOURCE,'PREVIEW_CONFIRMATION':ACK,
            'REVIEWED_WORKFLOW_SHA':self.wf,'GITHUB_WORKFLOW_SHA':self.wf,'GITHUB_SHA':self.wf,
            'RESERVATION_OWNER':'test-delivery-owner','RESERVATION_LEASE_SHA':self.lease,
            'GITHUB_RUN_ID':'123','GITHUB_RUN_NUMBER':'10'}
        self.record={'owner':'test-delivery-owner','branch':BRANCH,'checkpoint':self.wf}
        self.runs=[{'id':123,'head_sha':self.wf,'run_number':10}]
    def check(self):validate(self.env,self.runs,self.lease,self.record)
    def test_exact_owner_revisions_live_lease_and_first_attempt_pass(self):self.check()
    def test_wrong_actor_repository_branch_event_or_source_never_pass(self):
        for key in ['GITHUB_REPOSITORY','GITHUB_ACTOR','GITHUB_TRIGGERING_ACTOR','GITHUB_EVENT_NAME',
                    'GITHUB_REF','GITHUB_WORKFLOW_REF','REQUESTED_SOURCE_SHA','PREVIEW_CONFIRMATION']:
            with self.subTest(key=key):
                saved=self.env[key];self.env[key]='unapproved'
                with self.assertRaises(ValueError):self.check()
                self.env[key]=saved
    def test_reviewed_workflow_cannot_be_replaced_by_current_branch_head(self):
        self.env['GITHUB_WORKFLOW_SHA']='c'*40
        with self.assertRaises(ValueError):self.check()
    def test_reruns_are_rejected(self):
        self.env['GITHUB_RUN_ATTEMPT']='2'
        with self.assertRaises(ValueError):self.check()
    def test_second_fresh_dispatch_after_even_a_failed_attempt_is_rejected(self):
        self.runs.append({'id':122,'head_sha':self.wf,'run_number':9,'conclusion':'failure'})
        with self.assertRaises(ValueError):self.check()
    def test_missing_current_run_history_fails_closed(self):
        self.runs=[]
        with self.assertRaises(ValueError):self.check()
    def test_foreign_or_updated_reservation_fails_closed(self):
        for key,value in [('owner','other-owner'),('branch','main'),('checkpoint','c'*40)]:
            with self.subTest(key=key):
                original=self.record[key];self.record[key]=value
                with self.assertRaises(ValueError):self.check()
                self.record[key]=original
        with self.assertRaises(ValueError):validate(self.env,self.runs,'d'*40,self.record)

class Native60ArtifactTest(unittest.TestCase):
    def badging(self):
        return "package: name='dev.bautista.spotlight.next.raycastpreview' versionCode='61' versionName='2.0.1-raycast-preview-native57-v61'\nsdkVersion:'26'\ntargetSdkVersion:'35'\nnative-code: 'arm64-v8a'\n"
    def test_correct_delivery_metadata(self):metadata(self.badging())
    def test_old_main_debuggable_sdk_or_wrong_abi_rejected(self):
        samples=[self.badging().replace("versionCode='61'","versionCode='56'"),
                 self.badging().replace('.raycastpreview',''),self.badging()+'application-debuggable\n',
                 self.badging().replace("sdkVersion:'26'","sdkVersion:'35'"),
                 self.badging().replace("'arm64-v8a'","'x86_64'")]
        for sample in samples:
            with self.subTest(sample=sample),self.assertRaises(ValueError):metadata(sample)
    def test_single_historical_v2_certificate(self):
        certificate('Verified using v2 scheme (APK Signature Scheme v2): true\nSigner #1 certificate SHA-256 digest: '+CERTIFICATE+'\n')
    def test_main_wrong_duplicate_or_unsigned_signature_is_rejected(self):
        good='Verified using v2 scheme (APK Signature Scheme v2): true\nSigner #1 certificate SHA-256 digest: '+CERTIFICATE+'\n'
        for report in [good.replace(CERTIFICATE,'0'*64),good+good,good.replace(': true',': false'),'']:
            with self.assertRaises(ValueError):certificate(report)
    def test_exact_source_comment_and_no_private_material(self):
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'fixture.zip'
            with zipfile.ZipFile(path,'w') as z:z.comment=SOURCE.encode();z.writestr('source/public.txt','fixture')
            source_archive(path)
            with zipfile.ZipFile(path,'a') as z:z.comment=b'0'*40
            with self.assertRaises(ValueError):source_archive(path)
    def test_private_paths_traversal_and_symlinks_are_rejected(self):
        for name in ['source/signing/credentials.properties','source/signing/key.jks','../outside.txt']:
            with tempfile.TemporaryDirectory() as temp:
                p=Path(temp)/'fixture.zip'
                with zipfile.ZipFile(p,'w') as z:z.writestr(name,'not-a-key')
                with zipfile.ZipFile(p) as z,self.assertRaises(ValueError):safe_paths(z)
        with tempfile.TemporaryDirectory() as temp:
            p=Path(temp)/'fixture.zip';info=zipfile.ZipInfo('source/link');info.external_attr=0o120777<<16
            with zipfile.ZipFile(p,'w') as z:z.writestr(info,'fixture')
            with zipfile.ZipFile(p) as z,self.assertRaises(ValueError):safe_paths(z)

class PreviewDeliveryWorkflowTest(unittest.TestCase):
    def setUp(self):
        self.doc=yaml.safe_load((ROOT/'.github/workflows/build-apk.yml').read_text())
    def test_old_jobs_and_scripts_unchanged(self):
        old=yaml.safe_load(subprocess.check_output(['git','show','11b647c:.github/workflows/build-apk.yml'],cwd=ROOT,text=True))
        for name,job in old['jobs'].items():self.assertEqual(job,self.doc['jobs'][name])
        result=subprocess.run(['git','diff','--exit-code','11b647c','--','scripts/native60*','scripts/Native60SignerPreflight.java'],cwd=ROOT,capture_output=True)
        self.assertEqual(0,result.returncode)
    def test_three_jobs_isolate_unsigned_from_signing_inputs(self):
        jobs=self.doc['jobs']
        for name in ['delivery-preflight','delivery-unsigned','delivery-sign']:
            self.assertEqual({'contents':'read','actions':'read'},jobs[name]['permissions'])
            self.assertEqual('ubuntu-24.04',jobs[name]['runs-on'])
            for step in jobs[name]['steps']:
                if 'uses' in step:self.assertRegex(step['uses'],r'@[a-f0-9]{40}$')
        self.assertIn("inputs.mode == 'native-delivery'",jobs['delivery-preflight']['if'])
        build=json.dumps(jobs['delivery-unsigned'])
        self.assertNotIn('secrets.',build);self.assertEqual(1,build.count(':app:assembleRaycastPreview'))
        self.assertNotIn('optimize=false',build)
        sign=json.dumps(jobs['delivery-sign']);self.assertNotIn('gradlew',sign)
        for name in ['delivery-preflight','delivery-sign']:
            self.assertTrue(any(s.get('if')=='always()' and 'preview-delivery-signing.' in s.get('run','') for s in jobs[name]['steps']))
    def test_previous_same_source_attempt_rejected_across_workflow_revisions(self):
        fixture=Native60PolicyTest();fixture.setUp()
        fixture.runs.append({'head_sha':'c'*40,'run_number':9,'display_title':'native-delivery '+SOURCE,'conclusion':'failure'})
        with self.assertRaises(ValueError):fixture.check()
    def test_missing_secrets_create_no_signing_files(self):
        with tempfile.TemporaryDirectory() as temp:
            env={k:v for k,v in os.environ.items() if not k.startswith('RAYCAST_PREVIEW_')};env['RUNNER_TEMP']=temp
            result=subprocess.run(['bash',str(ROOT/'scripts/preview_delivery_sign.sh'),'preflight'],env=env,capture_output=True)
            self.assertNotEqual(0,result.returncode);self.assertEqual([],list(Path(temp).iterdir()))
if __name__=='__main__':unittest.main()
