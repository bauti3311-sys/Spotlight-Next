"""Development relay, private-network use only. Public auth is intentionally not implemented.
Run behind an authenticated private VPN + HTTPS reverse proxy, never expose port 8787.
OPENAI_API_KEY and OPENAI_MODEL are required on the SERVER, not in the Android app.
"""
import json
import os
import threading
import urllib.request
import urllib.parse
from live import flight_card, sports_card, transcribe
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

slots = threading.BoundedSemaphore(2)

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass  # no prompts, identities or credentials in access logs

    def send_json(self, value, status=200):
        body=json.dumps(value, ensure_ascii=False).encode()
        self.send_response(status)
        self.send_header('Content-Type','application/json; charset=utf-8')
        self.send_header('Cache-Control','no-store')
        self.send_header('Content-Length',str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        request=urllib.parse.urlparse(self.path)
        if request.path!='/live':
            self.send_error(404); return
        if not slots.acquire(blocking=False):
            self.send_error(429); return
        try:
            params=urllib.parse.parse_qs(request.query)
            kind=params.get('type',[''])[0];query=params.get('q',[''])[0].strip()
            if not 1 <= len(query) <= 100 or kind not in ('flight','sports'):
                self.send_json({'error':'invalid_query'},400); return
            value=flight_card(query.upper()) if kind=='flight' else sports_card(query)
            self.send_json(value)
        except ValueError:
            self.send_json({'error':'not_found_or_invalid_input'},404)
        except RuntimeError:
            self.send_json({'error':'provider_not_configured_or_unavailable'},503)
        except Exception:
            self.send_json({'error':'upstream_error'},502)
        finally:
            slots.release()

    def do_POST(self):
        if self.path == '/transcribe':
            if not slots.acquire(blocking=False):
                self.send_error(429); return
            try:
                length=int(self.headers.get('Content-Length','0'))
                if not 0 < length <= 28_000_000:
                    self.send_error(413); return
                self.send_json(transcribe(json.loads(self.rfile.read(length))))
            except ValueError:
                self.send_json({'error':'invalid_audio'},400)
            except RuntimeError:
                self.send_json({'error':'provider_not_configured_or_unavailable'},503)
            except Exception:
                self.send_json({'error':'upstream_error'},502)
            finally:
                slots.release()
            return
        if self.path != '/chat':
            self.send_error(404)
            return
        if not slots.acquire(blocking=False):
            self.send_error(429)
            return
        streaming = False
        try:
            length = int(self.headers.get('Content-Length', '0'))
            if not 0 < length <= 32768:
                self.send_error(413)
                return
            data = json.loads(self.rfile.read(length))
            messages = data.get('messages', [])
            if not isinstance(messages, list) or not 1 <= len(messages) <= 12:
                self.send_error(400)
                return
            if any(not isinstance(m, dict) or m.get('role') not in ('user','assistant') or not isinstance(m.get('content'), str) or len(m['content']) > 10000 for m in messages):
                self.send_error(400)
                return
            key = os.environ.get('OPENAI_API_KEY')
            model = os.environ.get('OPENAI_MODEL')
            if not key or not model:
                self.send_error(503)
                return
            body = json.dumps({'model':model,'input':messages,'stream':True,'store':False,'max_output_tokens':1024}).encode()
            request = urllib.request.Request('https://api.openai.com/v1/responses',data=body,headers={'Authorization':'Bearer '+key,'Content-Type':'application/json'})
            with urllib.request.urlopen(request,timeout=90) as response:
                self.send_response(200)
                self.send_header('Content-Type','text/event-stream; charset=utf-8')
                self.send_header('Cache-Control','no-store')
                self.send_header('X-Accel-Buffering','no')
                self.end_headers()
                streaming = True
                for line in response:
                    if not line.startswith(b'data:'):
                        continue
                    event = json.loads(line[5:])
                    if event.get('type') == 'response.output_text.delta':
                        self.wfile.write(('data: '+json.dumps({'delta':event['delta']})+'\n\n').encode())
                        self.wfile.flush()
                    elif event.get('type') in ('response.failed','error','response.incomplete'):
                        self.wfile.write(b'data: {"error":"provider_error"}\n\n')
                        self.wfile.flush()
                        break
                self.wfile.write(b'data: [DONE]\n\n')
                self.wfile.flush()
        except (BrokenPipeError,ConnectionResetError):
            pass
        except Exception:
            # Avoid printing upstream bodies which can contain user data.
            try:
                if streaming:
                    self.wfile.write(b'data: {"error":"upstream_error"}\n\ndata: [DONE]\n\n')
                    self.wfile.flush()
                else:
                    self.send_error(502)
            except OSError:
                pass
        finally:
            slots.release()

if __name__ == '__main__':
    ThreadingHTTPServer(('127.0.0.1',8787),Handler).serve_forever()
