"""Isolated text-only experiment. No account linking, background sync, or bulk API."""
import abc
import hashlib
import hmac
import http.server
import json
import os
from pathlib import Path
import re
import secrets
import sqlite3
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid


class Rejected(ValueError):
    pass


class WhatsAppMessagingProvider(abc.ABC):
    @abc.abstractmethod
    def send(self, draft):
        """Only invoked by Coordinator after exact-draft human approval."""

    def disconnect(self):
        return False


class NotificationReplyProvider(WhatsAppMessagingProvider):
    def __init__(self, reply):
        self.reply = reply

    def send(self, draft):
        self.reply(draft['recipient'], draft['text'])
        return {'state': 'REQUESTED'}  # RemoteInput has no delivery receipt.


class PublicIntentMessagingProvider(WhatsAppMessagingProvider):
    def send(self, draft):
        return {'state': 'EXTERNAL_REVIEW', 'uri': 'https://wa.me/' + draft['recipient'][1:] +
                '?' + urllib.parse.urlencode({'text': draft['text']})}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None


class WuzapiTransport:
    """Contract pinned in RESEARCH.md. Only text send + explicit logout, never QR/login."""
    def __init__(self, endpoint, token):
        parsed = urllib.parse.urlsplit(endpoint)
        if (parsed.scheme != 'https' or not parsed.hostname or parsed.username or parsed.password or
                parsed.query or parsed.fragment or parsed.path not in ('', '/')):
            raise Rejected('Upstream requires a trusted HTTPS origin without credentials or path')
        if not isinstance(token, str) or len(token) < 32 or '\n' in token or '\r' in token:
            raise Rejected('Use a dedicated strong upstream user token')
        self.endpoint, self.token = endpoint.rstrip('/'), token
        # Standard certificate/hostname validation, no redirects and no implicit proxy.
        self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())

    def _post(self, path, body):
        req = urllib.request.Request(self.endpoint + path, json.dumps(body).encode(),
                                     {'Token': self.token, 'Content-Type': 'application/json'}, method='POST')
        with self.opener.open(req, timeout=10) as response:
            if response.status != 200:
                raise Rejected('Upstream did not accept request')
            raw = response.read(32769)
            if len(raw) > 32768:
                raise Rejected('Upstream response too large')
            value = json.loads(raw)
            if value.get('success') is not True:
                raise Rejected('Upstream response is not successful')
            data = value.get('data')
            return json.loads(data) if isinstance(data, str) else data

    def send(self, draft):
        data = self._post('/chat/send/text', {'Phone': draft['recipient'][1:], 'Body': draft['text'],
                                            'Id': draft['id'].replace('-', '').upper(), 'LinkPreview': False})
        if not isinstance(data, dict) or not isinstance(data.get('Id'), str) or not data['Id']:
            raise Rejected('Upstream acceptance lacks message ID')
        return {'state': 'ACCEPTED', 'message_id': data['Id']}

    def disconnect(self):
        self._post('/session/logout', {})
        return True


class LinkedDeviceMessagingProvider(WhatsAppMessagingProvider):
    def __init__(self, transport, *, consented=False):
        self.transport, self.enabled = transport, consented

    def send(self, draft):
        if not self.enabled:
            raise Rejected('Linked-device mode disabled; explicit risk/account consent required')
        return self.transport.send(draft)

    def disconnect(self):
        self.enabled = False  # Fail closed even when remote logout cannot be verified.
        return self.transport.disconnect()


class Journal:
    """Durable at-most-one dispatch per request ID. No phone, text, token or sessions on disk."""
    def __init__(self, directory):
        self.directory = Path(directory)
        if self.directory.is_symlink():
            raise Rejected('Private directory cannot be a symlink')
        self.directory.mkdir(mode=0o700, parents=True, exist_ok=True)
        if self.directory.stat().st_mode & 0o077:
            raise Rejected('Private directory requires mode 0700')
        self.path = self.directory / 'receipts.db'
        if self.path.is_symlink():
            raise Rejected('Journal cannot be a symlink')
        fd = os.open(self.path, os.O_CREAT | os.O_RDWR, 0o600)
        os.close(fd)
        if self.path.stat().st_mode & 0o077:
            raise Rejected('Journal requires mode 0600')
        with self.connect() as db:
            db.execute('CREATE TABLE IF NOT EXISTS receipts (id TEXT PRIMARY KEY, digest TEXT, result TEXT)')

    def connect(self):
        return sqlite3.connect(self.path, timeout=5)

    def get(self, request_id):
        with self.connect() as db:
            row = db.execute('SELECT digest,result FROM receipts WHERE id=?', (request_id,)).fetchone()
        return (row[0], json.loads(row[1])) if row else None

    def reserve(self, request_id, digest, state='UNKNOWN'):
        with self.connect() as db:
            db.execute('BEGIN IMMEDIATE')
            row = db.execute('SELECT digest,result FROM receipts WHERE id=?', (request_id,)).fetchone()
            if row:
                if not hmac.compare_digest(row[0], digest):
                    raise Rejected('Request ID belongs to another draft')
                return json.loads(row[1]), False
            if db.execute('SELECT count(*) FROM receipts').fetchone()[0] >= 10000:
                raise Rejected('Journal full; do not remove unknown-send fences to retry')
            result = {'id': request_id, 'state': state}
            db.execute('INSERT INTO receipts VALUES (?,?,?)', (request_id, digest, json.dumps(result)))
        return result, True

    def finish(self, request_id, digest, result):
        with self.connect() as db:
            db.execute('UPDATE receipts SET result=? WHERE id=? AND digest=?',
                       (json.dumps(result), request_id, digest))


class Coordinator:
    def __init__(self, providers, journal, key, *, clock=time.monotonic, cooldown=5):
        if len(key) < 32:
            raise Rejected('Authentication key too short')
        self.providers, self.journal, self.key = providers, journal, key
        self.clock, self.cooldown = clock, cooldown
        self.pending, self.lock, self.last_send = {}, threading.RLock(), float('-inf')

    def prepare(self, provider, recipient, text):
        if provider not in self.providers:
            raise Rejected('Provider unavailable; never choose an automatic fallback')
        if not isinstance(recipient, str) or not re.fullmatch(r'\+[1-9][0-9]{6,14}', recipient):
            raise Rejected('Choose one exact E.164 recipient; names, groups and guessed LIDs unsupported')
        if not isinstance(text, str) or not text.strip() or len(text) > 8000:
            raise Rejected('Text must contain 1..8000 characters')
        with self.lock:
            self.pending = {k: v for k, v in self.pending.items() if v['expires'] > self.clock()}
            if len(self.pending) >= 20:
                raise Rejected('Too many pending drafts')
            draft = {'id': str(uuid.uuid4()), 'provider': provider, 'recipient': recipient, 'text': text}
            digest = hmac.new(self.key.encode(), json.dumps(draft, sort_keys=True).encode(), hashlib.sha256).hexdigest()
            review = {**draft, 'state': 'PREPARED', 'digest': digest, 'nonce': secrets.token_urlsafe(32)}
            self.pending[draft['id']] = {**review, 'expires': self.clock() + 120}
            return review  # Exact recipient/provider/text must be rendered by trusted human UI.

    def confirm(self, request_id, digest, nonce):
        with self.lock:
            existing = self.journal.get(request_id)
            if existing:
                if not hmac.compare_digest(existing[0], digest):
                    raise Rejected('Draft changed')
                return existing[1]
            review = self.pending.get(request_id)
            if (not review or review['expires'] <= self.clock() or
                    not hmac.compare_digest(review['digest'], digest) or
                    not hmac.compare_digest(review['nonce'], nonce)):
                raise Rejected('Human approval missing, changed or expired')
            provider = self.providers[review['provider']]
            if isinstance(provider, LinkedDeviceMessagingProvider) and not provider.enabled:
                raise Rejected('Linked-device mode disabled')
            if self.clock() - self.last_send < self.cooldown:
                raise Rejected('Single-send cooldown; bulk sending is unsupported')
            result, reserved = self.journal.reserve(request_id, digest)
            self.pending.pop(request_id, None)
            if not reserved:
                return result
            self.last_send = self.clock()
            try:
                receipt = provider.send(review)
                if receipt.get('state') not in ('ACCEPTED', 'REQUESTED', 'EXTERNAL_REVIEW', 'SIMULATED'):
                    raise Rejected('No verified delivery event adapter exists yet')
                result = {'id': request_id, 'state': receipt['state']}
                for field in ('message_id', 'uri'):
                    if field in receipt and isinstance(receipt[field], str):
                        result[field] = receipt[field]
            except Exception:
                # Timeout/crash may follow a real effect: never replay or switch provider.
                result = {'id': request_id, 'state': 'UNKNOWN'}
            self.journal.finish(request_id, digest, result)
            return result

    def cancel(self, request_id):
        with self.lock:
            review = self.pending.pop(request_id, None)
            if not review:
                raise Rejected('Only an unsubmitted draft can be cancelled')
            return self.journal.reserve(request_id, review['digest'], 'CANCELLED')[0]

    def disconnect(self):
        with self.lock:
            self.pending.clear()
            verified = None
            for provider in self.providers.values():
                if isinstance(provider, LinkedDeviceMessagingProvider):
                    try:
                        verified = provider.disconnect() and (verified is not False)
                    except Exception:
                        verified = False
            return {'state': 'DISABLED_LOCALLY', 'remote_logout_verified': verified}


def handler(coordinator):
    class Handler(http.server.BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass  # Never log message bodies, tokens, contacts, QR codes or sessions.

        def do_POST(self):
            self.connection.settimeout(3)
            if (not hmac.compare_digest(self.headers.get('Authorization', ''), 'Bearer ' + coordinator.key)
                    or self.headers.get('Origin') or self.headers.get('Host') != '%s:%s' % self.server.server_address):
                self.send_error(403);return
            try:
                length = int(self.headers.get('Content-Length', '0'))
                if self.headers.get_content_type() != 'application/json' or not 0 < length <= 32768:
                    raise Rejected('Invalid bounded JSON body')
                body = json.loads(self.rfile.read(length))
                if not isinstance(body, dict):
                    raise Rejected('One object required')
                if self.path == '/draft' and set(body) == {'provider', 'recipient', 'text'}:
                    result = coordinator.prepare(**body)
                elif self.path == '/confirm' and set(body) == {'id', 'digest', 'nonce'}:
                    result = coordinator.confirm(body['id'], body['digest'], body['nonce'])
                elif self.path == '/cancel' and set(body) == {'id'}:
                    result = coordinator.cancel(body['id'])
                elif self.path == '/status' and set(body) == {'id'}:
                    record = coordinator.journal.get(body['id'])
                    result = record[1] if record else {'state': 'NOT_SUBMITTED'}
                elif self.path == '/disconnect' and set(body) == {'confirmed'} and body['confirmed'] is True:
                    result = coordinator.disconnect()
                else:
                    raise Rejected('Unsupported operation or fields')
                data = json.dumps(result).encode()
                self.send_response(200);self.send_header('Content-Type', 'application/json')
                self.send_header('Cache-Control', 'no-store');self.send_header('Content-Length', str(len(data)))
                self.end_headers();self.wfile.write(data)
            except (ValueError, TypeError, KeyError, OSError, sqlite3.Error):
                self.send_error(409, 'Request rejected; inspect locally without retrying a send')
    return Handler


class LoopbackServer(http.server.ThreadingHTTPServer):
    daemon_threads = True
    def __init__(self, *args):
        self.slots = threading.BoundedSemaphore(4)
        super().__init__(*args)

    def process_request(self, request, client_address):
        if not self.slots.acquire(blocking=False):
            self.shutdown_request(request)
            return
        try:
            super().process_request(request, client_address)
        except Exception:
            self.slots.release()
            raise

    def process_request_thread(self, request, client_address):
        try:
            super().process_request_thread(request, client_address)
        finally:
            self.slots.release()


def fixture_server(coordinator, port=0):
    return LoopbackServer(('127.0.0.1', port), handler(coordinator))


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description='Loopback-only fixture server; no real WhatsApp client is started.')
    parser.add_argument('--fixture', action='store_true', required=True)
    args = parser.parse_args()
    key = os.environ.get('SPOTLIGHT_WA_TOKEN', '')
    # CLI intentionally cannot enable/link a real account. Actual adapter used only in controlled tests.
    class Fixture(WhatsAppMessagingProvider):
        def send(self, draft):
            return {'state': 'SIMULATED', 'message_id': 'fixture-' + draft['id']}
    engine = Coordinator({'fixture': Fixture(), 'intent': PublicIntentMessagingProvider()},
                         Journal(Path.home() / '.local/state/spotlight-wa-poc'), key)
    server = fixture_server(engine)
    print('Fixture-only service on 127.0.0.1:%d; no account connected' % server.server_port, flush=True)
    server.serve_forever()
