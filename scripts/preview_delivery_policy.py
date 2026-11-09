"""Read-only dispatch/lease/replay guard. No signing inputs, builds or mutations."""
import json
import os
import re
import subprocess
import sys

REPOSITORY = 'bauti3311-sys/Spotlight-Next'
OWNER = 'bauti3311-sys'
BRANCH = 'feat/preview57-native-return-20261103'
from pathlib import Path
CONFIG = json.loads(Path(__file__).with_name('preview_delivery.json').read_text())
SOURCE = CONFIG['source_sha']
VERSION_CODE = CONFIG['version_code']
if not re.fullmatch('[0-9a-f]{40}', SOURCE) or not isinstance(VERSION_CODE, int) or VERSION_CODE <= CONFIG['previous_version_code'] or CONFIG['budget_usd'] != 0 or CONFIG['stop_on_limit'] is not True:
    raise ValueError('Invalid approved release manifest.')
APK_NAME = f'Spotlight-Next-Preview{VERSION_CODE}-{SOURCE[:7]}.apk'
ZIP_NAME = f'Spotlight-Next-source-{SOURCE[:7]}.zip'
CERTIFICATE = '885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba'
PACKAGE = 'dev.bautista.spotlight.next.raycastpreview'
VERSION_NAME = CONFIG['version_name']
ACK = f'AUTHORIZE_PREVIEW_{VERSION_CODE}_ONCE'


def validate(env, runs, remote_lease, reservation):
    expected = env.get('REVIEWED_WORKFLOW_SHA', '')
    lease = env.get('RESERVATION_LEASE_SHA', '')
    checks = [
        env.get('GITHUB_REPOSITORY') == REPOSITORY,
        env.get('GITHUB_ACTOR') == OWNER,
        env.get('GITHUB_TRIGGERING_ACTOR') == OWNER,
        env.get('GITHUB_EVENT_NAME') == 'workflow_dispatch',
        env.get('GITHUB_REF') == 'refs/heads/' + BRANCH,
        env.get('GITHUB_WORKFLOW_REF') == REPOSITORY + '/.github/workflows/build-apk.yml@refs/heads/' + BRANCH,
        env.get('GITHUB_RUN_ATTEMPT') == '1',
        env.get('REQUESTED_SOURCE_SHA') == SOURCE,
        env.get('PREVIEW_CONFIRMATION') == ACK,
        re.fullmatch('[0-9a-f]{40}', expected) is not None,
        expected == env.get('GITHUB_WORKFLOW_SHA') == env.get('GITHUB_SHA'),
        re.fullmatch('[0-9a-f]{40}', lease) is not None,
        remote_lease == lease,
        reservation.get('owner') == env.get('RESERVATION_OWNER') and bool(env.get('RESERVATION_OWNER')),
        reservation.get('branch') == BRANCH,
        reservation.get('checkpoint') == expected,
    ]
    if not all(checks):
        raise ValueError('Dispatch identity, approved revisions or current reservation do not match.')
    number = int(env['GITHUB_RUN_NUMBER'])
    current = [r for r in runs if str(r.get('id')) == env['GITHUB_RUN_ID']]
    if len(current) != 1 or current[0].get('head_sha') != expected or current[0].get('run_number') != number:
        raise ValueError('Current run is not visible in the authoritative run history.')
    # Concurrency serializes this branch. A second dispatch of the reviewed workflow
    # revision is rejected, including after failure/cancellation. Reruns also fail above.
    if any((r.get('display_title') == 'native-delivery ' + SOURCE or r.get('head_sha') == expected) and r.get('run_number', number) < number for r in runs):
        raise ValueError('This reviewed workflow revision already had an attempt; new approval required.')


def api(path):
    result = subprocess.run(['gh', 'api', path], text=True, capture_output=True)
    if result.returncode:
        raise RuntimeError('Cannot verify GitHub policy metadata; no signing permitted.')
    return json.loads(result.stdout)


def main():
    runs = []
    for page in range(1, 101):
        batch = api(f'repos/{REPOSITORY}/actions/workflows/build-apk.yml/runs?event=workflow_dispatch&branch={BRANCH}&per_page=100&page={page}')['workflow_runs']
        runs.extend(batch)
        if len(batch) < 100:
            break
    else:
        raise RuntimeError('Run history exceeds bounded policy lookup; manual review required.')
    ref = api(f'repos/{REPOSITORY}/git/ref/heads/locks/spotlight-autonomy')['object']['sha']
    record = json.loads(api(f'repos/{REPOSITORY}/git/commits/{ref}')['message'])
    validate(os.environ, runs, ref, record)
    if CONFIG['package'] != PACKAGE or CONFIG['certificate_sha256'] != CERTIFICATE:
        raise ValueError('Preview identity cannot change.')
    comparison = api(f"repos/{REPOSITORY}/compare/{CONFIG['functional_base_sha']}...{SOURCE}")
    if comparison.get('status') not in ('ahead', 'identical') or any(f['filename'] not in set(CONFIG['allowed_delta_paths']) for f in comparison.get('files', [])):
        raise ValueError('Source changes exceed the approved C1 delivery metadata.')
    print('Approved source, workflow revision, owner, one-attempt history and live reservation verified.')


if __name__ == '__main__':
    try:
        main()
    except (ValueError, RuntimeError, KeyError, TypeError):
        sys.exit('Preview delivery policy failed closed; no signing permitted.')
