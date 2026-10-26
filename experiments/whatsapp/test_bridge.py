import concurrent.futures
import json
from pathlib import Path
import tempfile
import threading
import unittest
import urllib.error
import urllib.request
from bridge import (Coordinator, Journal, Rejected, LinkedDeviceMessagingProvider,
                    NotificationReplyProvider, PublicIntentMessagingProvider, WuzapiTransport, fixture_server)

KEY = 'fixture-only-key-never-used-in-production-1234'
PHONE = '+5491112345678'


class Transport:
    def __init__(self):
        self.calls = 0
        self.logouts = 0
        self.fail = False

    def send(self, draft):
        self.calls += 1
        if self.fail:
            raise TimeoutError('uncertain result')
        return {'state': 'ACCEPTED', 'message_id': 'controlled-fixture'}

    def disconnect(self):
        self.logouts += 1
        return True


class BridgeTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.clock = 10.0
        self.transport = Transport()
        self.provider = LinkedDeviceMessagingProvider(self.transport, consented=True)
        self.engine = self.engine_for(self.provider)

    def engine_for(self, provider):
        return Coordinator({'linked': provider, 'intent': PublicIntentMessagingProvider()},
                           Journal(Path(self.temp.name) / 'private'), KEY, clock=lambda: self.clock)

    def draft(self, **kwargs):
        return self.engine.prepare(kwargs.get('provider', 'linked'), kwargs.get('recipient', PHONE), kwargs.get('text', 'Llego en diez minutos'))

    def confirm(self, draft):
        return self.engine.confirm(draft['id'], draft['digest'], draft['nonce'])

    def test_disabled_by_default_and_no_fallback(self):
        self.provider = LinkedDeviceMessagingProvider(self.transport)
        self.engine = self.engine_for(self.provider)
        with self.assertRaises(Rejected):
            self.confirm(self.draft())
        self.assertEqual(0, self.transport.calls)

    def test_preparation_never_sends_and_approval_is_exact(self):
        draft = self.draft()
        self.assertEqual(0, self.transport.calls)
        with self.assertRaises(Rejected):
            self.engine.confirm(draft['id'], 'changed-text-digest', draft['nonce'])
        with self.assertRaises(Rejected):
            self.engine.confirm(draft['id'], draft['digest'], 'wrong-nonce')
        self.assertEqual('ACCEPTED', self.confirm(draft)['state'])
        self.assertNotIn('delivered', self.confirm(draft))
        self.assertEqual(1, self.transport.calls)

    def test_expired_and_cancelled_drafts_do_not_execute(self):
        draft = self.draft();self.clock += 121
        with self.assertRaises(Rejected): self.confirm(draft)
        draft = self.draft();self.engine.cancel(draft['id'])
        self.assertEqual('CANCELLED', self.confirm(draft)['state'])
        self.assertEqual(0, self.transport.calls)

    def test_rewritten_text_requires_new_review_and_cancels_old_draft(self):
        old=self.draft(text='Llego en diez minutos')
        self.engine.cancel(old['id'])
        new=self.draft(text='Llego en diez minutos, gracias por esperar')
        self.assertEqual('CANCELLED',self.confirm(old)['state'])
        with self.assertRaises(Rejected):
            self.engine.confirm(new['id'],new['digest'],old['nonce'])
        self.assertEqual(0,self.transport.calls)
        self.assertEqual('ACCEPTED',self.confirm(new)['state'])

    def test_duplicate_concurrent_confirmations_send_once(self):
        draft = self.draft()
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
            results = list(pool.map(lambda _: self.confirm(draft), range(6)))
        self.assertEqual(1, self.transport.calls)
        self.assertTrue(all(v['state'] == 'ACCEPTED' for v in results))

    def test_unknown_after_network_failure_is_never_retried_even_after_restart(self):
        draft = self.draft();self.transport.fail = True
        self.assertEqual('UNKNOWN', self.confirm(draft)['state'])
        self.engine = self.engine_for(self.provider)
        self.assertEqual('UNKNOWN', self.confirm(draft)['state'])
        self.assertEqual(1, self.transport.calls)

    def test_unverified_delivery_claim_is_not_accepted_as_delivered(self):
        self.transport.send=lambda draft:{'state':'DELIVERED'}
        self.assertEqual('UNKNOWN',self.confirm(self.draft())['state'])

    def test_crash_fence_before_dispatch_remains_unknown(self):
        draft = self.draft()
        self.engine.journal.reserve(draft['id'], draft['digest'])
        self.engine = self.engine_for(self.provider)
        self.assertEqual('UNKNOWN', self.confirm(draft)['state'])
        self.assertEqual(0, self.transport.calls)

    def test_bulk_names_groups_and_oversized_text_rejected(self):
        for recipient in ['Juan', [PHONE, PHONE], '12000@g.us', '123@lid', 'https://wa.me/123']:
            with self.subTest(recipient=recipient), self.assertRaises(Rejected): self.draft(recipient=recipient)
        for text in ['', ' '*4, 'x'*8001]:
            with self.assertRaises(Rejected): self.draft(text=text)
        self.confirm(self.draft())
        with self.assertRaises(Rejected): self.confirm(self.draft())
        self.assertEqual(1, self.transport.calls)

    def test_private_journal_contains_no_text_phone_or_key(self):
        draft = self.draft();self.confirm(draft)
        raw = self.engine.journal.path.read_bytes()
        for secret in [draft['text'], PHONE, KEY, draft['nonce']]:
            self.assertNotIn(secret.encode(), raw)
        self.assertEqual(0o600, self.engine.journal.path.stat().st_mode & 0o777)

    def test_disconnect_disables_and_drops_approvals(self):
        draft = self.draft()
        self.assertTrue(self.engine.disconnect()['remote_logout_verified'])
        self.assertFalse(self.provider.enabled)
        self.assertEqual(1, self.transport.logouts)
        with self.assertRaises(Rejected): self.confirm(draft)
        self.assertEqual(0, self.transport.calls)

    def test_logout_failure_still_disables_locally(self):
        self.transport.disconnect = lambda: (_ for _ in ()).throw(TimeoutError())
        self.assertFalse(self.engine.disconnect()['remote_logout_verified'])
        self.assertFalse(self.provider.enabled)

    def test_remoteinput_is_requested_not_delivered_and_public_intent_needs_external_review(self):
        calls=[]
        p=NotificationReplyProvider(lambda recipient,text:calls.append((recipient,text)))
        self.assertEqual('REQUESTED',p.send({'recipient':PHONE,'text':'OK'})['state'])
        draft=self.draft(provider='intent',text='Hola & ¿cómo estás?')
        result=self.confirm(draft)
        self.assertEqual('EXTERNAL_REVIEW',result['state'])
        self.assertIn('Hola+%26+',result['uri'])
        self.assertEqual(0,self.transport.calls)

    def test_upstream_requires_https_no_userinfo_or_query(self):
        for endpoint in ['http://localhost:8080','https://user:password@example.com','https://example.com/?token=x','https://example.com/path']:
            with self.assertRaises(Rejected): WuzapiTransport(endpoint,KEY)

    def test_http_auth_origin_and_explicit_approval(self):
        server=fixture_server(self.engine)
        thread=threading.Thread(target=server.serve_forever,daemon=True);thread.start()
        self.addCleanup(server.server_close);self.addCleanup(server.shutdown)
        base='http://127.0.0.1:%d'%server.server_port
        def post(path,body,token=KEY,origin=None):
            headers={'Content-Type':'application/json','Authorization':'Bearer '+token}
            if origin:headers['Origin']=origin
            req=urllib.request.Request(base+path,json.dumps(body).encode(),headers)
            with urllib.request.build_opener(urllib.request.ProxyHandler({})).open(req) as response:return json.load(response)
        body={'provider':'linked','recipient':PHONE,'text':'Texto exacto'}
        with self.assertRaises(urllib.error.HTTPError) as error:post('/draft',body,token='wrong')
        self.assertEqual(403,error.exception.code)
        with self.assertRaises(urllib.error.HTTPError):post('/draft',body,origin='https://untrusted.example')
        review=post('/draft',body)
        self.assertEqual(0,self.transport.calls)
        receipt=post('/confirm',{k:review[k] for k in ['id','digest','nonce']})
        self.assertEqual('ACCEPTED',receipt['state']);self.assertEqual(1,self.transport.calls)
        self.assertEqual(receipt,post('/status',{'id':review['id']}))

    def test_wuzapi_wire_contract_against_controlled_http_fixture(self):
        # No real WuzAPI instance/account is involved. Only HTTP shape is exercised.
        from http.server import BaseHTTPRequestHandler,ThreadingHTTPServer
        captured=[]
        class Upstream(BaseHTTPRequestHandler):
            def log_message(self,*args):pass
            def do_POST(self):
                captured.append((self.path,self.headers.get('Token'),json.loads(self.rfile.read(int(self.headers['Content-Length'])))))
                data=json.dumps({'success':True,'data':json.dumps({'Id':'fixture-message','Details':'Sent'})}).encode()
                self.send_response(200);self.send_header('Content-Length',str(len(data)));self.end_headers();self.wfile.write(data)
        server=ThreadingHTTPServer(('127.0.0.1',0),Upstream)
        threading.Thread(target=server.serve_forever,daemon=True).start()
        self.addCleanup(server.server_close);self.addCleanup(server.shutdown)
        adapter=WuzapiTransport('https://configured.example',KEY)
        adapter.endpoint='http://127.0.0.1:%d'%server.server_port  # Test-only local upstream.
        receipt=adapter.send({'id':'request-1','recipient':PHONE,'text':'Hola'})
        self.assertEqual('ACCEPTED',receipt['state'])
        self.assertEqual('/chat/send/text',captured[0][0]);self.assertEqual(KEY,captured[0][1])
        self.assertEqual({'Phone':PHONE[1:],'Body':'Hola','Id':'REQUEST1','LinkPreview':False},captured[0][2])
        self.assertTrue(adapter.disconnect());self.assertEqual('/session/logout',captured[-1][0])

    def test_redirect_is_not_followed(self):
        from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
        paths=[]
        class Redirect(BaseHTTPRequestHandler):
            def log_message(self,*args):pass
            def do_POST(self):
                paths.append(self.path)
                self.send_response(307)
                self.send_header('Location','http://127.0.0.1:%d/credential-sink'%self.server.server_port)
                self.end_headers()
        server=ThreadingHTTPServer(('127.0.0.1',0),Redirect)
        threading.Thread(target=server.serve_forever,daemon=True).start()
        self.addCleanup(server.server_close);self.addCleanup(server.shutdown)
        adapter=WuzapiTransport('https://configured.example',KEY)
        adapter.endpoint='http://127.0.0.1:%d'%server.server_port
        with self.assertRaises(urllib.error.HTTPError) as error:
            adapter.send({'id':'fixture','recipient':PHONE,'text':'Hola'})
        self.assertEqual(307,error.exception.code)
        self.assertEqual(['/chat/send/text'],paths)



if __name__ == '__main__':unittest.main(verbosity=2)
