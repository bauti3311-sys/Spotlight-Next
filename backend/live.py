"""Optional server-side connectors. No keys are embedded in the APK.
The server must remain on localhost behind authenticated private HTTPS/VPN.
"""
import base64
import datetime as dt
import json
import os
import re
import urllib.parse
import urllib.request
import uuid


def fetch_json(url, headers=None):
    request = urllib.request.Request(url, headers=headers or {})
    with urllib.request.urlopen(request, timeout=25) as response:
        payload = response.read(2_000_001)
        if len(payload) > 2_000_000:
            raise ValueError('upstream response too large')
        return json.loads(payload)


def flight_card(query):
    if not re.fullmatch(r'[A-Z]{2}\d{1,4}', query):
        raise ValueError('invalid flight number')
    key = os.environ.get('AVIATIONSTACK_API_KEY')
    if not key:
        raise RuntimeError('flight provider is not configured')
    data = fetch_json('https://api.aviationstack.com/v1/flights?' + urllib.parse.urlencode({'access_key': key, 'flight_iata': query, 'limit': 100}))
    if data.get('error'):
        raise RuntimeError('flight provider rejected request')
    flights = data.get('data', [])
    if not flights:
        raise ValueError('no flight data')
    flights = sorted(flights, key=lambda item: item.get('flight_date', ''), reverse=True)
    item = flights[0]
    departure = item.get('departure') or {}
    arrival = item.get('arrival') or {}
    rows = []
    for label, value in [('Fecha', item.get('flight_date')), ('Origen', departure.get('airport')), ('Destino', arrival.get('airport')), ('Salida programada', departure.get('scheduled')), ('Salida estimada', departure.get('estimated')), ('Llegada programada', arrival.get('scheduled')), ('Llegada estimada', arrival.get('estimated')), ('Retraso salida (min)', departure.get('delay')), ('Terminal de salida', departure.get('terminal')), ('Puerta de salida', departure.get('gate')), ('Terminal de llegada', arrival.get('terminal')), ('Puerta de llegada', arrival.get('gate'))]:
        if value is not None:
            rows.append({'label': label, 'value': str(value)})
    live = item.get('live') or {}
    lat, lon = live.get('latitude'), live.get('longitude')
    url = ('https://www.google.com/maps/search/?api=1&query=' + urllib.parse.quote(f'{lat},{lon}')) if lat is not None and lon is not None else 'https://www.google.com/search?q=' + urllib.parse.quote(query + ' flight status')
    return {'title': query + ' · ' + str(item.get('flight_status', 'estado no disponible')), 'source': 'Aviationstack · consultado', 'updated_at': dt.datetime.now(dt.timezone.utc).isoformat(), 'body': 'Información del proveedor para la fecha indicada. Verificá los datos con la aerolínea. El mapa abre la posición informada si está disponible; no simula una posición en vivo.', 'url': url, 'rows': rows}


def football(path, params, key):
    data = fetch_json('https://v3.football.api-sports.io/' + path + '?' + urllib.parse.urlencode(params), {'x-apisports-key': key})
    if data.get('errors'):
        raise RuntimeError('sports provider rejected request')
    return data.get('response') or []


def sports_card(query):
    key = os.environ.get('API_FOOTBALL_KEY')
    if not key:
        raise RuntimeError('sports provider is not configured')
    teams = football('teams', {'search': query}, key)
    if not teams:
        raise ValueError('team not found')
    team = next((x for x in teams if (x.get('team') or {}).get('country') == 'Argentina'), teams[0])['team']
    identifier = team['id']
    live = football('fixtures', {'team': identifier, 'live': 'all'}, key)
    last = football('fixtures', {'team': identifier, 'last': 3}, key)
    upcoming = football('fixtures', {'team': identifier, 'next': 3}, key)
    rows = []
    for label, fixtures in [('En vivo', live), ('Últimos', last), ('Próximos', upcoming)]:
        for item in fixtures:
            fixture = item.get('fixture') or {}
            goals = item.get('goals') or {}
            teams = item.get('teams') or {}
            home, away = (teams.get('home') or {}).get('name', '?'), (teams.get('away') or {}).get('name', '?')
            score = (str(goals.get('home')) + '–' + str(goals.get('away'))) if goals.get('home') is not None else 'por jugar'
            status = (fixture.get('status') or {}).get('short', '')
            rows.append({'label': label, 'value': f'{home} {score} {away} · {status} · {fixture.get("date", "")} '})
    recent = (live or last)
    if recent:
        league = recent[0].get('league') or {}
        standings = football('standings', {'league': league.get('id'), 'season': league.get('season')}, key) if league.get('id') else []
        for item in standings:
            for group in (item.get('league') or {}).get('standings', []):
                for standing in group:
                    if standing.get('team', {}).get('id') == identifier or int(standing.get('rank', 999)) <= 5:
                        rows.append({'label': 'Tabla', 'value': f'{standing.get("rank")} · {standing.get("team", {}).get("name")} · {standing.get("points")} pts · {standing.get("all", {}).get("played")} PJ'})
        fixture_id = recent[0].get('fixture', {}).get('id')
        statistics = football('fixtures/statistics', {'fixture': fixture_id, 'team': identifier}, key) if fixture_id else []
        for group in statistics:
            for stat in group.get('statistics', [])[:10]:
                if stat.get('value') is not None:
                    rows.append({'label': str(stat.get('type')), 'value': str(stat['value'])})
    return {'title': team['name'], 'source': 'API-FOOTBALL · consultado', 'updated_at': dt.datetime.now(dt.timezone.utc).isoformat(), 'body': 'Fútbol. Marcadores, agenda y estadísticas según disponibilidad y permisos de la cuenta del proveedor. No se inventan datos ausentes.', 'rows': rows[:40], 'url': 'https://www.google.com/search?q=' + urllib.parse.quote(team['name'] + ' resultados')}


def transcribe(data):
    key, model = os.environ.get('OPENAI_API_KEY'), os.environ.get('OPENAI_TRANSCRIBE_MODEL')
    if not key or not model:
        raise RuntimeError('transcription provider is not configured')
    name = data.get('name', '')
    if not isinstance(name, str):
        raise ValueError('invalid filename')
    ext = name.rsplit('.', 1)[-1].lower()
    formats = {'mp3': 'audio/mpeg', 'm4a': 'audio/mp4', 'mp4': 'audio/mp4', 'wav': 'audio/wav', 'ogg': 'audio/ogg', 'webm': 'audio/webm', 'flac': 'audio/flac', 'mpeg': 'audio/mpeg', 'mpga': 'audio/mpeg'}
    if ext not in formats:
        raise ValueError('unsupported audio format')
    encoded = data.get('audio')
    if not isinstance(encoded, str) or len(encoded) > 26_666_672:
        raise ValueError('invalid audio size')
    audio = base64.b64decode(encoded, validate=True)
    if not 0 < len(audio) <= 20_000_000:
        raise ValueError('invalid audio size')
    boundary = 'spotlight-' + uuid.uuid4().hex
    chunks = []
    for field, value in [('model', model), ('response_format', 'json')]:
        chunks.append((f'--{boundary}\r\nContent-Disposition: form-data; name="{field}"\r\n\r\n{value}\r\n').encode())
    chunks.append((f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="audio.{ext}"\r\nContent-Type: {formats[ext]}\r\n\r\n').encode() + audio + b'\r\n')
    chunks.append(f'--{boundary}--\r\n'.encode())
    request = urllib.request.Request('https://api.openai.com/v1/audio/transcriptions', data=b''.join(chunks), headers={'Authorization': 'Bearer ' + key, 'Content-Type': 'multipart/form-data; boundary=' + boundary})
    with urllib.request.urlopen(request, timeout=150) as response:
        result = json.loads(response.read(200_001))
        text = result.get('text')
        if not isinstance(text, str) or not text.strip():
            raise RuntimeError('empty transcription')
        return {'text': text[:100000]}
