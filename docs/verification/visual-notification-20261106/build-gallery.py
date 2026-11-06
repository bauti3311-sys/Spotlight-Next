"""Offline review of original Compose notification captures. No image editing/network."""
from pathlib import Path
import base64,hashlib,html,json,sys,zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
ROOT=Path(__file__).resolve().parent
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-visual-notification')
OUT.mkdir(parents=True,exist_ok=True)
m=json.loads((ROOT/'manifest.json').read_text())
def img(p,label):return '<figure><button onclick="zoom(this)"><img src="data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()+'" alt="'+label+'"></button><figcaption>'+label+'</figcaption></figure>'
rows=''
for r in m['pairs']:
 rows+='<section><h2>'+html.escape(r['name'])+'</h2><p>'+('Base protegida: PNG idénticos.' if r['before_sha256']==r['after_sha256'] else 'Las acciones pasan completas a la siguiente fila cuando falta espacio.')+'</p><div class="pair">'+img(ROOT/'before'/r['name'],'A · antes')+img(ROOT/'after'/r['name'],'B · después integrado')+'</div></section>'
(OUT/'Spotlight-Notification.html').write_text('''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spotlight · tarjeta de notificación</title><style>body{font:16px/1.5 system-ui;background:#eae8e2;color:#26282a;margin:0}main{max-width:1200px;margin:auto;padding:24px}header,section{padding:22px;background:#fffdf8;margin-bottom:24px;border-radius:14px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:16px}figure{margin:0;text-align:center}button{background:#efede7;border:0;padding:8px;cursor:zoom-in;width:100%}img{width:100%;max-height:780px;object-fit:contain}figcaption{font-size:13px}dialog{border:0;border-radius:12px;max-width:94vw;max-height:94vh}dialog img{max-height:85vh;width:auto}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:10px}header,section{padding:12px}.pair{gap:6px}}</style><main><header><h1>Spotlight · acciones de notificación visibles</h1><p>V06/V08: la fila fija ocultaba Volver incluso a 390 dp y texto normal. A 330 dp/200 % también desaparecía Sugerir respuesta con IA. La corrección permite envolver las acciones, conservando orden y comportamiento.</p><p>Componente NotificationPreview real; notificación sintética, sin cuenta, envíos, IA remota ni aplicación abierta. API33/Robolectric, densidad1, mismo texto/fecha/tema/ancho. El desplazamiento vertical pertenece al fixture, no es un nuevo flujo.</p><p>Sin APK nueva ni aceptación física One UI/Gboard. D5 no adoptado; funciones C3–C8 en pausa.</p></header>'''+rows+'''<dialog onclick="this.close()"><img alt="Captura original"></dialog></main><script>function zoom(b){let d=document.querySelector('dialog');d.querySelector('img').src=b.querySelector('img').src;d.showModal()}</script></html>''')
pdfmetrics.registerFont(TTFont('DejaVu','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
c=canvas.Canvas(str(OUT/'Spotlight-Notification.pdf'),pagesize=(842,595))
def text(s,y,size=12):c.setFont('DejaVu',size);c.drawString(30,y,s)
text('Spotlight · acciones de notificación',550,25)
for i,s in enumerate(['V06/V08: corregir acciones con ancho cero en teléfono.', 'A 390 dp / 100 %, Volver desaparecía. A 330 dp / 200 %, también Sugerir respuesta.', 'Después: ajuste de líneas, mismos botones, orden, permisos y callbacks.', 'No se envían mensajes ni se consulta IA o una cuenta real en estas pruebas.', 'Compose real / fixture sintético: sin nueva evidencia física.', 'PNG originales y hashes en el ZIP. Tablet normal conserva la distribución.']):text(s,490-i*45,12)
c.showPage()
for r in m['pairs']:
 text(r['name'],560,17);text('A · antes',530);c.drawString(435,530,'B · después integrado')
 for side,x in [('before',30),('after',435)]:
  im=ImageReader(str(ROOT/side/r['name']));w,h=im.getSize();scale=min(375/w,470/h)
  c.drawImage(im,x+(375-w*scale)/2,35,w*scale,h*scale,mask='auto')
 c.showPage()
c.save()
with zipfile.ZipFile(OUT/'Spotlight-Notification-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(ROOT.rglob('*.png')):z.write(p,str(p.relative_to(ROOT)))
 z.write(ROOT/'manifest.json','manifest.json')
 for p in [OUT/'Spotlight-Notification.html',OUT/'Spotlight-Notification.pdf']:z.write(p,p.name)
hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir() if p.suffix in ['.html','.pdf','.zip']}
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{v}  {k}\n' for k,v in hashes.items()))
(ROOT/'artifact-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
print(json.dumps(hashes,indent=2))
