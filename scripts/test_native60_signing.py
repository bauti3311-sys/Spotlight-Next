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
from native60_policy import SOURCE, REPOSITORY, BRANCH, OWNER, ACK, CERTIFICATE, validate
from native60_verify import metadata, certificate, source_archive, safe_paths

ROOT=Path(__file__).resolve().parents[1]

class Native60PolicyTest(unittest.TestCase):
    def setUp(self):
        self.wf='a'*40;self.lease='b'*40
        self.env={'GITHUB_REPOSITORY':REPOSITORY,'GITHUB_ACTOR':OWNER,'GITHUB_TRIGGERING_ACTOR':OWNER,
            'GITHUB_EVENT_NAME':'workflow_dispatch','GITHUB_REF':'refs/heads/'+BRANCH,
            'GITHUB_WORKFLOW_REF':REPOSITORY+'/.github/workflows/build-apk.yml@refs/heads/'+BRANCH,
            'GITHUB_RUN_ATTEMPT':'1','REQUESTED_SOURCE_SHA':SOURCE,'NATIVE60_CONFIRMATION':ACK,
            'REVIEWED_WORKFLOW_SHA':self.wf,'GITHUB_WORKFLOW_SHA':self.wf,'GITHUB_SHA':self.wf,
            'RESERVATION_OWNER':'test-native60-owner','RESERVATION_LEASE_SHA':self.lease,
            'GITHUB_RUN_ID':'123','GITHUB_RUN_NUMBER':'10'}
        self.record={'owner':'test-native60-owner','branch':BRANCH,'checkpoint':self.wf}
        self.runs=[{'id':123,'head_sha':self.wf,'run_number':10}]
    def check(self):validate(self.env,self.runs,self.lease,self.record)
    def test_exact_owner_revisions_live_lease_and_first_attempt_pass(self):self.check()
    def test_wrong_actor_repository_branch_event_or_source_never_pass(self):
        for key in ['GITHUB_REPOSITORY','GITHUB_ACTOR','GITHUB_TRIGGERING_ACTOR','GITHUB_EVENT_NAME',
                    'GITHUB_REF','GITHUB_WORKFLOW_REF','REQUESTED_SOURCE_SHA','NATIVE60_CONFIRMATION']:
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
        return "package: name='dev.bautista.spotlight.next.raycastpreview' versionCode='60' versionName='2.0.1-raycast-preview-native57-v60'\nsdkVersion:'26'\ntargetSdkVersion:'35'\nnative-code: 'arm64-v8a'\n"
    def test_correct_native60_metadata(self):metadata(self.badging())
    def test_old_main_debuggable_sdk_or_wrong_abi_rejected(self):
        samples=[self.badging().replace("versionCode='60'","versionCode='56'"),
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

class Native60WorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):cls.doc=yaml.safe_load((ROOT/'.github/workflows/build-apk.yml').read_text())
    def test_manual_only_existing_workflow_and_read_only_native_jobs(self):
        trigger=self.doc.get('on',self.doc.get(True))
        self.assertEqual({'workflow_dispatch'},set(trigger))
        self.assertEqual('directed-tests',trigger['workflow_dispatch']['inputs']['mode']['default'])
        jobs=self.doc['jobs']
        for name in ['native60-preflight','native60-unsigned','native60-sign']:
            self.assertEqual({'contents':'read','actions':'read'},jobs[name]['permissions'])
            self.assertEqual('ubuntu-24.04',jobs[name]['runs-on'])
        self.assertIn("github.actor == 'bauti3311-sys'",jobs['native60-preflight']['if'])
        self.assertIn("github.triggering_actor == 'bauti3311-sys'",jobs['native60-preflight']['if'])
    def test_unsigned_build_has_no_signing_secret_and_no_retry_or_experimental(self):
        job=self.doc['jobs']['native60-unsigned'];text=json.dumps(job)
        self.assertNotIn('secrets.',text)
        self.assertNotIn('optimize=false',text)
        self.assertEqual('native60-preflight',job['needs'])
        self.assertEqual(1,text.count(':app:assembleRaycastPreview'))
        self.assertIn(SOURCE,text)
    def test_signer_never_checks_out_app_or_runs_gradle(self):
        job=self.doc['jobs']['native60-sign'];text=json.dumps(job)
        self.assertEqual('native60-unsigned',job['needs'])
        self.assertNotIn('gradlew',text)
        checkouts=[s for s in job['steps'] if s.get('uses','').startswith('actions/checkout@')]
        self.assertEqual(1,len(checkouts));self.assertEqual('scripts',checkouts[0]['with']['sparse-checkout'])
        self.assertEqual('${{ github.workflow_sha }}',checkouts[0]['with']['ref'])
        self.assertNotIn('source/',text)
    def test_all_new_actions_are_immutable_commit_pins(self):
        import re
        for name,job in self.doc['jobs'].items():
            if name.startswith('native60-'):
                for step in job['steps']:
                    if 'uses' in step:self.assertRegex(step['uses'],r'@[0-9a-f]{40}$')
    def test_private_inputs_are_step_scoped_and_cleanup_is_always(self):
        for name in ['native60-preflight','native60-sign']:
            job=self.doc['jobs'][name]
            self.assertNotIn('secrets.',json.dumps(job.get('env',{})))
            self.assertTrue(any(s.get('if')=='always()' and 'native60-signing.' in s.get('run','') for s in job['steps']))
    def test_missing_signing_inputs_fail_before_private_files_exist(self):
        with tempfile.TemporaryDirectory() as temp:
            env={k:v for k,v in os.environ.items() if not k.startswith('RAYCAST_PREVIEW_')}
            env['RUNNER_TEMP']=temp
            result=subprocess.run(['bash',str(ROOT/'scripts/native60_sign.sh'),'preflight'],env=env,text=True,capture_output=True)
            self.assertNotEqual(0,result.returncode);self.assertEqual([],list(Path(temp).iterdir()))
    def test_termux_never_extracts_into_a_shared_or_arbitrary_home(self):
        with tempfile.TemporaryDirectory() as temp:
            archive=Path(temp)/'fixture.zip'
            with zipfile.ZipFile(archive,'w') as z:z.writestr('SpotlightNext/signing/spotlight-debug.keystore','not-a-key')
            env=dict(os.environ,HOME=temp)
            result=subprocess.run(['bash',str(ROOT/'scripts/native60_termux_preflight.sh'),str(archive)],env=env,capture_output=True)
            self.assertNotEqual(0,result.returncode)
            self.assertEqual([archive],list(Path(temp).iterdir()))
    def test_termux_missing_archive_creates_no_key_or_output(self):
        with tempfile.TemporaryDirectory() as temp:
            result=subprocess.run(['bash',str(ROOT/'scripts/native60_termux_preflight.sh'),str(Path(temp)/'missing.zip')],capture_output=True)
            self.assertNotEqual(0,result.returncode);self.assertEqual([],list(Path(temp).iterdir()))

if __name__=='__main__':unittest.main()
