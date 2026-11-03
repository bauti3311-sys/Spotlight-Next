#!/usr/bin/env python3
"""Read-only scope/provenance checks; never compiles or runs product tests."""
from pathlib import Path
import hashlib
import json
import subprocess

BASE = '4fc33f2c918d3fe53a8bad7ac6571f2757630066'
BRIDGE = '00fec133c6de9f286aab02239928ef309ec63cb0'
ROOT = Path(__file__).resolve().parents[3]

def git(*args):
    return subprocess.check_output(['git', '-C', str(ROOT), *args], text=True).strip()

def main():
    assert git('branch', '--show-current') == 'feat/preview57-native-return-20261103'
    assert git('merge-base', BASE, 'HEAD') == BASE
    assert subprocess.run(['git', '-C', str(ROOT), 'merge-base', '--is-ancestor', BRIDGE, 'HEAD'],
                          capture_output=True).returncode == 1
    protected = ['app', '.github', 'build.gradle.kts', 'settings.gradle.kts',
                 'gradle', 'gradle.properties', 'gradlew', 'gradlew.bat', 'release.properties']
    assert not git('diff', '--name-only', BASE, '--', *protected), 'Protected source/config changed'
    assert not git('ls-files', '--others', '--exclude-standard', '--', *protected), 'Untracked app/config'
    app_tree = git('rev-parse', BASE + ':app')
    assert git('rev-parse', 'HEAD:app') == app_tree
    files = git('ls-tree', '-r', '--name-only', BASE, '--', 'app').splitlines()
    assert not any('LocalRaycastWebView' in p or '/assets/raycast-web/' in p for p in files)
    origin = json.loads((ROOT/'docs/verification/native57-return-20261103/skills-origin.json').read_text())
    skill_names = {'spotlight-api-contracts', 'spotlight-openui-forms', 'spotlight-ragflow-documents'}
    assert {Path(row['path']).parts[2] for row in origin['files']} == skill_names
    assert len(origin['files']) == 7
    adapted = []
    for row in origin['files']:
        original = subprocess.check_output(['git', '-C', str(ROOT), 'show', BRIDGE + ':' + row['path']])
        assert hashlib.sha256(original).hexdigest() == row['sourceSha256']
        assert git('rev-parse', BRIDGE + ':' + row['path']) == row['sourceBlob']
        current_path = row['path'].replace('bridge-contract.json', 'native-contract.json')
        data = (ROOT/current_path).read_bytes()
        if current_path.endswith('.json'):
            json.loads(data)
        if current_path.endswith('/SKILL.md'):
            assert data.startswith(b'---\nname: ') and b'description:' in data
        adapted.append({'path': current_path, 'sha256': hashlib.sha256(data).hexdigest()})
    assert not (ROOT/'.agents/skills/spotlight-api-contracts/references/bridge-contract.json').exists()
    return {'kind': 'static provenance and scope only; no Kotlin/runtime tests executed',
            'base': BASE, 'workingBranch': git('branch', '--show-current'),
            'appTreeIdenticalToBase': app_tree, 'protectedSourceConfigDiff': [],
            'bridgeHeadIsAncestor': False, 'recoveredSourceFiles': 7, 'adaptedFiles': adapted,
            'gradleRuns': 0, 'apkBuilds': 0, 'actionsDispatched': 0}

if __name__ == '__main__':
    print(json.dumps(main(), ensure_ascii=False, indent=2))
