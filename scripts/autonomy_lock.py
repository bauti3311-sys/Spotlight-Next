#!/usr/bin/env python3
"""Shared Git reservation; fail closed, never reclaim by age or force-push."""
import argparse
import datetime
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import uuid

REF = 'refs/heads/locks/spotlight-autonomy'
NOTES = 'refs/notes/spotlight-autonomy'
ZERO = '0' * 40


class Reservation:
    def __init__(self, repository='.', remote='origin'):
        self.root = Path(repository).resolve()
        if not re.fullmatch(r'[A-Za-z0-9_.][A-Za-z0-9_.-]*', remote):
            raise ValueError('Use a configured remote name')
        self.remote = remote

    def git(self, *args, input=None):
        result = subprocess.run(['git', *args], cwd=self.root, input=input,
                                text=True, capture_output=True)
        if result.returncode:
            raise RuntimeError(f'git {args[0]} failed (exit {result.returncode})')
        return result.stdout.strip()

    def tip(self, ref):
        rows = self.git('ls-remote', '--refs', self.remote, ref).splitlines()
        return rows[0].split()[0] if rows else None

    def metadata(self, sha):
        try:
            raw = self.git('show', '-s', '--format=%B', sha)
        except RuntimeError:
            self.git('fetch', '--no-tags', self.remote, sha)
            raw = self.git('show', '-s', '--format=%B', sha)
        record = json.loads(raw)
        if not isinstance(record, dict) or not all(isinstance(record.get(k), str)
                                                   for k in ('owner', 'branch', 'checkpoint')):
            raise ValueError('Unknown reservation metadata; do not edit or delete it')
        if not re.fullmatch(r'[a-f0-9]{40}', record['checkpoint']):
            raise ValueError('Unknown checkpoint in reservation; do not delete it')
        return record

    def status(self):
        sha = self.tip(REF)
        record = self.metadata(sha) if sha else None
        return {'lease_sha': sha, 'reservation': {k: record.get(k) for k in
                ('owner', 'branch', 'checkpoint', 'state', 'date')} if record else None,
                'validated_checkpoint': self.tip('refs/heads/checkpoint/autonomous-next'),
                'notes': self.tip(NOTES)}

    def checkpoint(self, value):
        if value != 'HEAD' and not re.fullmatch(r'[a-fA-F0-9]{7,40}', value):
            raise ValueError('Checkpoint must be a commit SHA')
        return self.git('rev-parse', '--verify', value + '^{commit}')

    def push(self, commit, expected, publish_notes=False):
        # Check the server-advertised OLD object at negotiation, not only ls-remote.
        # Git's receive transaction then enforces that same old object atomically.
        # This closes the read/push race without any --force or --force-with-lease.
        original = Path(self.git('rev-parse', '--git-path', 'hooks/pre-push'))
        if not original.is_absolute():
            original = self.root / original
        original = str(original.resolve()) if os.access(original, os.X_OK) else None
        with tempfile.TemporaryDirectory(prefix='spotlight-push-') as directory:
            hook = Path(directory) / 'pre-push'
            hook.write_text(f'''#!{sys.executable}
import subprocess, sys
raw = sys.stdin.read()
rows = [line.split() for line in raw.splitlines()]
lease = [row for row in rows if len(row) == 4 and row[2] == {REF!r}]
if len(lease) != 1 or lease[0][3] != {expected!r}:
    print('Reservation changed at push negotiation; refusing mutation.', file=sys.stderr)
    sys.exit(1)
original = {original!r}
if original:
    sys.exit(subprocess.run([original, *sys.argv[1:]], input=raw, text=True).returncode)
''')
            hook.chmod(0o755)
            specs = [NOTES] if publish_notes else []
            specs.append((commit or '') + ':' + REF)
            self.git('-c', 'core.hooksPath=' + directory, 'push', '--atomic', self.remote, *specs)

    def write(self, record, parent):
        record['date'] = datetime.datetime.now(datetime.timezone.utc).isoformat()
        record['timezone'] = 'UTC'
        tree = self.git('rev-parse', record['checkpoint'] + '^{tree}')
        payload = {k: record[k] for k in ('owner', 'branch', 'checkpoint', 'state', 'date', 'timezone')}
        sha = self.git('commit-tree', tree, '-p', parent, input=json.dumps(payload) + '\n')
        return sha

    def acquire(self, owner, branch, checkpoint='HEAD'):
        if not re.fullmatch(r'[A-Za-z0-9_.-]{1,128}', owner):
            raise ValueError('Use a unique execution owner identifier')
        if not branch.startswith(('feat/', 'fix/', 'test/')):
            raise ValueError('Use an isolated feat/fix/test branch')
        self.git('check-ref-format', '--branch', branch)
        if self.tip(REF):
            raise RuntimeError('Reservation exists; do not edit or reclaim it')
        checkpoint = self.checkpoint(checkpoint)
        record = {'owner': owner, 'branch': branch, 'checkpoint': checkpoint, 'state': 'active'}
        sha = self.write(record, checkpoint)
        self.push(sha, ZERO)
        return dict(record, lease_sha=sha)

    def owned(self, owner, expected):
        if not re.fullmatch(r'[a-f0-9]{40}', expected) or self.tip(REF) != expected:
            raise RuntimeError('Reservation SHA changed or is absent')
        record = self.metadata(expected)
        if record['owner'] != owner:
            raise RuntimeError('Another execution owns this reservation')
        return record

    def update(self, owner, expected, checkpoint, state='checkpoint'):
        record = self.owned(owner, expected)
        checkpoint = self.checkpoint(checkpoint)
        self.git('merge-base', '--is-ancestor', record['checkpoint'], checkpoint)
        record.update(checkpoint=checkpoint, state=state)
        sha = self.write(record, expected)
        self.push(sha, expected)
        return dict(record, lease_sha=sha)

    def release(self, owner, expected, publish_notes=False):
        record = self.owned(owner, expected)
        self.push(None, expected, publish_notes)
        return dict(record, state='released', lease_sha=expected)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--remote', default='origin')
    commands = parser.add_subparsers(dest='command', required=True)
    commands.add_parser('status')
    acquire = commands.add_parser('acquire')
    acquire.add_argument('--owner', default='codex-' + str(uuid.uuid4()))
    acquire.add_argument('--branch', required=True)
    acquire.add_argument('--checkpoint', default='HEAD')
    for name in ('update', 'release'):
        child = commands.add_parser(name)
        child.add_argument('--owner', required=True)
        child.add_argument('--lease-sha', required=True)
        if name == 'update':
            child.add_argument('--checkpoint', required=True)
            child.add_argument('--state', default='checkpoint')
        else:
            child.add_argument('--publish-notes', action='store_true')
    args = parser.parse_args()
    reservation = Reservation(remote=args.remote)
    try:
        if args.command == 'status':
            result = reservation.status()
        elif args.command == 'acquire':
            result = reservation.acquire(args.owner, args.branch, args.checkpoint)
        elif args.command == 'update':
            result = reservation.update(args.owner, args.lease_sha, args.checkpoint, args.state)
        else:
            result = reservation.release(args.owner, args.lease_sha, args.publish_notes)
        print(json.dumps(result, indent=2))
    except (RuntimeError, ValueError) as error:
        print(str(error), file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
