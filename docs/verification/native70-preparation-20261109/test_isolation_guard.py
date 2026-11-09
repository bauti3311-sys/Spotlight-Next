import sys,os,copy,json,contextlib,io,unittest
from pathlib import Path
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[3]
sys.path.insert(0,str(ROOT/'scripts'))
import preview_delivery_policy as policy
from test_preview_delivery import Native60PolicyTest
BASE='cbc956ab56756d9ebdf7de05300fb0b5f5b14a19'
SOURCE='143104ce8d83f6ad6b5b4acd80897db579816fcd'
INDEX='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt'
import subprocess
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT,text=True).strip()

class ExistingComparisonGuardTest(unittest.TestCase):
    def setUp(self):
        self.paths=git('diff','--name-only',BASE,SOURCE).splitlines()
        self.comparison={'status':'ahead','files':[{'filename':p} for p in self.paths]}
        self.fixture=Native60PolicyTest();self.fixture.setUp()
    def check(self):
        f=self.fixture
        def api(path):
            if '/actions/workflows/' in path:return {'workflow_runs':f.runs}
            if path.endswith('/git/ref/heads/locks/spotlight-autonomy'):return {'object':{'sha':f.lease}}
            if path.endswith('/git/commits/'+f.lease):return {'message':json.dumps(f.record)}
            if path.endswith('/compare/'+BASE+'...'+SOURCE):return self.comparison
            raise AssertionError('Unexpected API path; no real network: '+path)
        with patch.dict(os.environ,f.env,clear=True),patch.object(policy,'api',api),contextlib.redirect_stdout(io.StringIO()):
            policy.main()
    def test_isolated_source_passes_existing_guard_with_fixture_dispatch_only(self):self.check()
    def test_fileindex_delta_is_rejected(self):
        self.comparison['files'].append({'filename':INDEX})
        with self.assertRaises(ValueError):self.check()
    def test_diverged_history_is_rejected(self):
        self.comparison['status']='diverged'
        with self.assertRaises(ValueError):self.check()


if __name__=='__main__':
 result=unittest.TextTestRunner(verbosity=2).run(unittest.defaultTestLoader.loadTestsFromTestCase(ExistingComparisonGuardTest))
 sys.exit(not result.wasSuccessful())
