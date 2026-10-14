import concurrent.futures
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('autonomy_lock', Path(__file__).with_name('autonomy_lock.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ReservationTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix='git reservation ')
        self.root = Path(self.temporary.name)
        self.remote = self.root / 'remote.git'
        subprocess.run(['git', 'init', '--bare', str(self.remote)], check=True, capture_output=True)
        self.repositories = []
        for name in ('first', 'second'):
            folder = self.root / name
            subprocess.run(['git', 'clone', str(self.remote), str(folder)], check=True, capture_output=True)
            for key, value in [('user.name', 'Test'), ('user.email', 'test@example.invalid')]:
                self.git(folder, 'config', key, value)
            self.repositories.append(folder)
        self.first, self.second = self.repositories
        (self.first / 'fixture.txt').write_text('offline fixture\n')
        self.git(self.first, 'add', '.')
        self.git(self.first, 'commit', '-m', 'fixture')
        self.base = self.git(self.first, 'rev-parse', 'HEAD')
        self.git(self.first, 'push', 'origin', 'HEAD:refs/heads/main')
        self.git(self.second, 'fetch', 'origin')
        self.a = module.Reservation(self.first)
        self.b = module.Reservation(self.second)

    def tearDown(self):
        self.temporary.cleanup()

    def git(self, folder, *args):
        return subprocess.check_output(['git', *args], cwd=folder, text=True, stderr=subprocess.DEVNULL).strip()

    def acquire(self, reservation, owner):
        return reservation.acquire(owner, 'feat/offline-test', self.base)

    def test_acquire_records_and_only_owner_can_release(self):
        lease = self.acquire(self.a, 'owner-a')
        self.assertEqual(self.base, lease['checkpoint'])
        self.assertEqual('owner-a', self.b.status()['reservation']['owner'])
        with self.assertRaises(RuntimeError):
            self.b.release('owner-b', lease['lease_sha'])
        self.assertEqual(lease['lease_sha'], self.a.tip(module.REF))
        self.a.release('owner-a', lease['lease_sha'])
        self.assertIsNone(self.b.tip(module.REF))

    def test_two_independent_repositories_have_exactly_one_winner(self):
        def attempt(item):
            reservation, owner = item
            try:
                return self.acquire(reservation, owner)
            except RuntimeError:
                return None
        with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
            leases = list(executor.map(attempt, [(self.a, 'owner-a'), (self.b, 'owner-b')]))
        winners = [lease for lease in leases if lease]
        self.assertEqual(1, len(winners))
        self.assertEqual(winners[0]['owner'], self.a.status()['reservation']['owner'])

    def test_negotiation_guard_rejects_changed_ref_even_if_push_could_fast_forward(self):
        lease = self.acquire(self.a, 'owner-a')
        record = dict(lease, owner='owner-b')
        child = self.a.write(record, lease['lease_sha'])
        with self.assertRaises(RuntimeError):
            self.a.push(child, module.ZERO)
        self.assertEqual(lease['lease_sha'], self.a.tip(module.REF))
        with self.assertRaises(RuntimeError):
            self.a.push(None, self.base)
        self.assertEqual(lease['lease_sha'], self.a.tip(module.REF))

    def test_checkpoint_update_is_forward_only_and_stale_release_fails(self):
        lease = self.acquire(self.a, 'owner-a')
        (self.first / 'fixture.txt').write_text('next safe checkpoint\n')
        self.git(self.first, 'commit', '-am', 'next checkpoint')
        checkpoint = self.git(self.first, 'rev-parse', 'HEAD')
        updated = self.a.update('owner-a', lease['lease_sha'], checkpoint)
        self.assertEqual(checkpoint, self.a.status()['reservation']['checkpoint'])
        with self.assertRaises(RuntimeError):
            self.a.release('owner-a', lease['lease_sha'])
        with self.assertRaises(RuntimeError):
            self.a.update('owner-a', updated['lease_sha'], self.base)
        self.assertEqual(updated['lease_sha'], self.a.tip(module.REF))

    def test_existing_security_hook_still_controls_push(self):
        hook = self.first / '.git/hooks/pre-push'
        hook.write_text('#!/bin/sh\nexit 1\n')
        hook.chmod(0o755)
        with self.assertRaises(RuntimeError):
            self.acquire(self.a, 'owner-a')
        self.assertIsNone(self.a.tip(module.REF))

    def test_configured_security_hook_is_not_bypassed(self):
        directory = self.first / 'custom hooks'
        directory.mkdir()
        hook = directory / 'pre-push'
        hook.write_text('#!/bin/sh\nexit 1\n')
        hook.chmod(0o755)
        self.git(self.first, 'config', 'core.hooksPath', str(directory))
        with self.assertRaises(RuntimeError):
            self.acquire(self.a, 'owner-a')
        self.assertIsNone(self.a.tip(module.REF))

    def test_invalid_metadata_is_not_reclaimed(self):
        tree = self.a.git('rev-parse', self.base + '^{tree}')
        sha = self.a.git('commit-tree', tree, '-p', self.base, input='unknown holder\n')
        self.a.git('push', 'origin', sha + ':' + module.REF)
        with self.assertRaises(ValueError):
            self.a.status()
        with self.assertRaises(RuntimeError):
            self.acquire(self.b, 'owner-b')
        self.assertEqual(sha, self.a.tip(module.REF))

    def test_age_never_allows_reclaiming_an_existing_owner(self):
        lease = self.acquire(self.a, 'owner-a')
        record = dict(lease, date='1970-01-01T00:00:00Z')
        tree = self.a.git('rev-parse', self.base + '^{tree}')
        sha = self.a.git('commit-tree', tree, '-p', lease['lease_sha'], input=json.dumps(record) + '\n')
        self.a.git('push', 'origin', sha + ':' + module.REF)
        with self.assertRaises(RuntimeError):
            self.acquire(self.b, 'owner-b')
        self.assertEqual(sha, self.a.tip(module.REF))

    def test_atomic_notes_failure_does_not_release_reservation(self):
        lease = self.acquire(self.a, 'owner-a')
        self.a.git('notes', '--ref=spotlight-autonomy', 'add', '-m', 'first', self.base)
        self.a.git('push', 'origin', module.NOTES)
        self.b.git('fetch', 'origin', module.NOTES + ':' + module.NOTES)
        self.b.git('notes', '--ref=spotlight-autonomy', 'append', '-m', 'remote evidence', self.base)
        self.b.git('push', 'origin', module.NOTES)
        self.a.git('notes', '--ref=spotlight-autonomy', 'append', '-m', 'local evidence', self.base)
        with self.assertRaises(RuntimeError):
            self.a.release('owner-a', lease['lease_sha'], publish_notes=True)
        self.assertEqual(lease['lease_sha'], self.a.tip(module.REF))


if __name__ == '__main__':
    unittest.main()
