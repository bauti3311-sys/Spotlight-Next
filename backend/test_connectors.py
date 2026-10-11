import base64
import io
import json
import os
import unittest
from unittest.mock import patch
import live


class ConnectorTests(unittest.TestCase):
    @patch.dict(os.environ, {'AVIATIONSTACK_API_KEY': 'test-not-a-secret'})
    def test_flight_preserves_real_dates_terminal_and_delay(self):
        payload={'data':[{'flight_date':'2026-10-09','flight_status':'active','departure':{'airport':'Ezeiza','scheduled':'2026-10-09T12:00:00+00:00','terminal':'A','delay':20},'arrival':{'airport':'Madrid','estimated':'2026-10-10T06:00:00+02:00'},'live':{'latitude':-20.5,'longitude':-40.2}}]}
        with patch.object(live,'fetch_json',return_value=payload):
            card=live.flight_card('AR1301')
        self.assertEqual('AR1301 · active',card['title'])
        self.assertIn({'label':'Retraso salida (min)','value':'20'},card['rows'])
        self.assertIn({'label':'Terminal de salida','value':'A'},card['rows'])
        self.assertIn('-20.5%2C-40.2',card['url'])

    @patch.dict(os.environ, {}, clear=True)
    def test_missing_provider_never_produces_fake_results(self):
        with self.assertRaises(RuntimeError):live.flight_card('AR1301')
        with self.assertRaises(RuntimeError):live.sports_card('Boca')
        with self.assertRaises(RuntimeError):live.transcribe({'name':'a.mp3','audio':'YQ=='})

    @patch.dict(os.environ, {'OPENAI_API_KEY':'test-not-a-secret','OPENAI_TRANSCRIBE_MODEL':'test-model'})
    def test_audio_upload_has_format_and_does_not_forward_filename_injections(self):
        response=io.BytesIO(b'{"text":"hola mundo"}')
        with patch('urllib.request.urlopen',return_value=response) as call:
            result=live.transcribe({'name':'bad"name.mp3','audio':base64.b64encode(b'fake-audio').decode()})
        self.assertEqual('hola mundo',result['text'])
        request=call.call_args.args[0]
        self.assertEqual('https://api.openai.com/v1/audio/transcriptions',request.full_url)
        self.assertIn(b'filename="audio.mp3"',request.data)
        self.assertNotIn(b'bad"name',request.data)
        self.assertIn(b'audio/mpeg',request.data)
        self.assertIn(b'fake-audio',request.data)

    @patch.dict(os.environ, {'OPENAI_API_KEY':'test-not-a-secret','OPENAI_TRANSCRIBE_MODEL':'test-model'})
    def test_audio_invalid_input_is_rejected_before_network(self):
        with patch('urllib.request.urlopen') as call:
            for value in ({'name':'a.exe','audio':'YQ=='},{'name':'a.mp3','audio':'not-base64!'},{'name':'a.mp3','audio':''}):
                with self.assertRaises(ValueError):live.transcribe(value)
            call.assert_not_called()

    @patch.dict(os.environ, {'API_FOOTBALL_KEY':'test-not-a-secret'})
    def test_sports_uses_real_fixture_scores_and_handles_absent_tables(self):
        fixture={'fixture':{'id':1,'date':'2026-10-09T18:00:00Z','status':{'short':'FT'}},'goals':{'home':2,'away':1},'teams':{'home':{'name':'Boca'},'away':{'name':'River'}},'league':{'id':128,'season':2026}}
        replies=[[{'team':{'id':6,'name':'Boca','country':'Argentina'}}],[],[fixture],[],[],[]]
        with patch.object(live,'football',side_effect=replies):card=live.sports_card('Boca')
        self.assertEqual('Boca',card['title'])
        self.assertTrue(any('Boca 2–1 River' in r['value'] for r in card['rows']))
        self.assertFalse(any(r['label']=='Tabla' for r in card['rows']))

if __name__=='__main__':unittest.main()
