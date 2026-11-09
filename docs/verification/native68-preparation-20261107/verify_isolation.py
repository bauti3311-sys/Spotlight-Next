"""Offline release isolation checks and existing guard exercised with labelled fixtures. No dispatch."""
import contextlib
import copy
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import unittest
from unittest.mock import patch

ROOT=Path(__file__).resolve().parents[3]
sys.path.insert(0,str(ROOT/'scripts'))
import preview_delivery_policy as policy
from test_preview_delivery import Native60PolicyTest
BASE='6918204238706d3c9bac826196ad3f1f272a51e7'
PR='214e00ac95e517717cc98dc05379bc5e4aa27773'
SOURCE='c51677b79446ce1dc8213553891d45897b14d726'
INDEX='app/src/main/java/dev/bautista/spotlight/index/FileIndex.kt'
def git(*args):
    return subprocess.check_output(['git',*args],cwd=ROOT,text=True).strip()

def freeze():
    manifest=policy.CONFIG
    paths=git('diff','--name-only',BASE,SOURCE).splitlines()
    assert manifest['source_sha']==SOURCE and manifest['functional_base_sha']==BASE
    assert manifest['version_code']==68 and manifest['previous_version_code']==67
    assert manifest['version_name']=='2.0.1-raycast-preview-native57-v68'
    assert set(manifest['allowed_delta_paths'])==set(paths) and len(paths)==10
    assert INDEX not in paths
    assert git('rev-parse',BASE+':'+INDEX)==git('rev-parse',SOURCE+':'+INDEX)
    main=[p for p in paths if p.startswith('app/src/main/')]
    assert len(main)==6 and all(git('rev-parse',SOURCE+':'+p)==git('rev-parse',PR+':'+p) for p in main)
    for name in ['DocumentCitations.kt','DocumentCitationState.kt']:
        result=subprocess.run(['git','cat-file','-e',SOURCE+':app/src/main/java/dev/bautista/spotlight/index/'+name],cwd=ROOT,capture_output=True)
        assert result.returncode!=0
    gradle=git('show',BASE+':app/build.gradle.kts').replace('native57-v67','native57-v68').replace('output.versionCode.set(67)','output.versionCode.set(68)')
    assert git('show',SOURCE+':app/build.gradle.kts')==gradle
    for p in ['app/src/main/java/dev/bautista/spotlight/data/Preferences.kt','app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt','app/src/main/AndroidManifest.xml','release.properties','.github/workflows/build-apk.yml']:
        assert git('rev-parse',BASE+':'+p)==git('rev-parse',SOURCE+':'+p)
    assert 'setDuration(140L)' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/overlay/OverlayWindowMotion.kt')
    assert 'focusInterface:Boolean=false' in git('show',SOURCE+':app/src/main/java/dev/bautista/spotlight/data/Preferences.kt')
    git('merge-base','--is-ancestor',BASE,SOURCE)
    print('OFFLINE: exact Preview67 base + six approved UI files + three test files + two version lines; preferences/140ms unchanged')
    return paths

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
    freeze()
    # Load only the new comparison checks; do not rediscover/repeat the imported suite.
    result=unittest.TextTestRunner(verbosity=2).run(unittest.defaultTestLoader.loadTestsFromTestCase(ExistingComparisonGuardTest))
    sys.exit(not result.wasSuccessful())
