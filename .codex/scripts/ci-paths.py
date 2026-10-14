#!/usr/bin/env python3
"""Select costly quick checks from the actual push delta; never from PR titles/labels."""
import json
import os
from pathlib import Path
import re
import subprocess
import urllib.request


def comparison_base(event_name, event, previous_verified=False):
    if event_name == 'workflow_dispatch':
        return None
    if event_name == 'pull_request':
        candidate = (event.get('before') if event.get('action') == 'synchronize' and previous_verified
                     else event['pull_request']['base']['sha'])
    elif event_name == 'push':
        candidate = event.get('before')
    else:
        raise ValueError(f'Unsupported event: {event_name}')
    if not candidate or candidate == '0' * 40:
        return '4b825dc642cb6eb9a060e54bf8d69288fbee4904'
    if not re.fullmatch(r'[0-9a-fA-F]{40}', candidate):
        raise ValueError('Invalid comparison SHA; refusing to skip checks')
    return candidate


def classify(paths):
    android = any(p.startswith(('app/', 'gradle/')) or p in {
        'build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'gradlew', 'gradlew.bat',
        '.codex/scripts/doctor.sh', 'scripts/release_identity.py',
    } for p in paths)
    web = any(p.startswith('visual-lab/') for p in paths)
    return {'android': android, 'web': web}


def changed_paths(base):
    raw = subprocess.check_output(['git', 'diff', '--name-only', '-z', base, 'HEAD', '--'])
    return [p.decode('utf-8', errors='surrogateescape') for p in raw.split(b'\0') if p]



def previous_quick_ci_succeeded(event_name, event):
    if event_name != 'pull_request' or event.get('action') != 'synchronize':
        return False
    sha = event.get('before', '')
    token = os.environ.get('GITHUB_TOKEN')
    repository = os.environ.get('GITHUB_REPOSITORY', '')
    if not token or not re.fullmatch(r'[0-9a-fA-F]{40}', sha) or not re.fullmatch(r'[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+', repository):
        return False
    url = f'https://api.github.com/repos/{repository}/actions/workflows/codex-tooling.yml/runs?head_sha={sha}&event=pull_request&per_page=10'
    request = urllib.request.Request(url, headers={'Authorization': f'Bearer {token}', 'Accept': 'application/vnd.github+json'})
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            runs = json.load(response)['workflow_runs']
        # An obsolete/cancelled/failed previous run is never evidence for skipping.
        latest = next((run for run in runs if run['head_sha'] == sha), None)
        return latest is not None and latest['status'] == 'completed' and latest['conclusion'] == 'success'
    except (OSError, ValueError, KeyError):
        print('Previous CI could not be confirmed; checking the full PR diff.')
        return False


def main():
    event = json.loads(Path(os.environ['GITHUB_EVENT_PATH']).read_text())
    previous_verified = previous_quick_ci_succeeded(os.environ['GITHUB_EVENT_NAME'], event)
    base = comparison_base(os.environ['GITHUB_EVENT_NAME'], event, previous_verified)
    paths = changed_paths(base) if base is not None else []
    selected = classify(paths) if base is not None else {'android': True, 'web': True}
    with open(os.environ['GITHUB_OUTPUT'], 'a') as output:
        for key, value in selected.items():
            output.write(f'{key}={str(value).lower()}\n')
    print(json.dumps({'base': base, 'previous_verified': previous_verified, 'changed': paths, 'checks': selected}, ensure_ascii=True))
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as summary:
        summary.write('## Alcance de CI rápida\n\n')
        summary.write(f"- Android unitarias/lint/build/baselines: {selected['android']}\n")
        summary.write(f"- Laboratorio web: {selected['web']}\n")
        summary.write('- Python, análisis de workflows y secretos: siempre.\n')
        summary.write('- Emuladores/PDF instrumentado: NO ejecutados aquí; activación manual independiente.\n')


if __name__ == '__main__':
    main()
