"""Publish original Compose captures, offline HTML and PDF. No image editing/network."""
from pathlib import Path
import base64, hashlib, html, json, shutil, sys, zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
ROOT=Path(__file__).resolve().parent
REPO=ROOT.parents[2]
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-visual-polish')
OUT.mkdir(parents=True,exist_ok=True)
m=json.loads((ROOT/'manifest.json').read_text())
def path(r,side):return REPO/r[side]
def uri(p):return 'data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()
def pic(p,label):return '<figure><button onclick="zoom(this)"><img loading="lazy" src="'+uri(p)+'" alt="'+html.escape(label)+'"></button><figcaption>'+html.escape(label)+'</figcaption></figure>'
profiles=sorted({r['profile'] for r in m['pairs']})
cards=[]
for r in m['pairs']:
 cards.append('<section class="card" data-profile="'+r['profile']+'"><h2>'+html.escape(r['scene'])+'</h2><p>'+html.escape(r['profile'])+' · '+('Sin diferencia de bytes; base protegida.' if r['before_sha256']==r['after_sha256'] else 'Cambio real en el render del componente.')+'</p><div class="pair">'+pic(path(r,'before'),'A · antes')+pic(path(r,'after'),'B · después integrado')+'</div></section>')
frames=''
for theme in ['dark','light']:
 frames+='<section class="motion"><h2>D5 · '+theme+'</h2><p>A: helper Compose de referencia 160 ms, sin llamadas productivas. B: experimento de test 120 ms. La ventana real abre en 140 ms desde alfa 0,92; no se modifica.</p><div class="film">'
 for t in [0,48,96,120,160,208]:
  frames+='<div><h3>'+str(t)+' ms</h3>'+pic(ROOT/'motion'/f'{theme}-A-{t}.png','A · referencia')+pic(ROOT/'motion'/f'{theme}-B-{t}.png','B · experimento')+'</div>'
 frames+='</div></section>'
frames+='<section><h2>D5 · reducción durante la entrada</h2><p>Contenido visible inmediatamente; reactivar animación no reproduce la entrada. Fixture, no validación física.</p>'+pic(ROOT/'motion'/'reduced-during-entry.png','B · reducción de movimiento')+'</section>'
text='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spotlight · pulido visual aprobado</title><style>body{font:16px/1.5 system-ui;background:#eae8e2;color:#26282a;margin:0}main{max-width:1200px;margin:auto;padding:24px}header,section{background:#fffdf8;padding:22px;margin-bottom:24px;border-radius:14px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:18px}figure{margin:0;text-align:center}button{border:0;background:#eeece7;cursor:zoom-in;width:100%;padding:8px}img{max-width:100%;max-height:720px;object-fit:contain}figcaption{font-size:13px;padding:8px}select{font:inherit;padding:10px;position:sticky;top:8px;z-index:2}.film{display:grid;grid-template-columns:repeat(3,1fr);gap:15px}.film img{max-height:230px}dialog{border:0;border-radius:12px;max-width:95vw;max-height:95vh}dialog img{max-height:88vh}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:10px}.pair{gap:6px}.film{grid-template-columns:repeat(2,1fr)}} </style><main><header><h1>Spotlight Next · pulido visual aprobado</h1><p>D1 integrado: radios 10 dp en campos y controles equivalentes de Focus. D2–D4 conservados: ancho, tipografía, contraste, selección e iconos por rol. PDF: paginación adaptable y controles visibles de 48 dp.</p><p>Capturas originales Compose/API33, mismos datos, temas y dimensiones. Inicio/resultados/inspector: SpotlightPanel. Periféricos: componentes reales aislados con fixtures. IME reservado: simulación; no prueba física One UI/Gboard. No APK nueva.</p><p>D5 sigue aislado en tests. No demuestra mejora de FPS/RAM ni autoriza una adopción global.</p></header><select id="profile" onchange="filter()">'''+''.join('<option'+(' selected' if p=='phone-dark' else '')+'>'+p+'</option>' for p in profiles)+'</select>'+''.join(cards)+frames+'''<dialog onclick="this.close()"><img alt="Captura original ampliada"></dialog></main><script>function filter(){document.querySelectorAll('.card').forEach(e=>e.hidden=e.dataset.profile!==document.querySelector('#profile').value)}function zoom(b){let d=document.querySelector('dialog');d.querySelector('img').src=b.querySelector('img').src;d.showModal()}filter()</script></html>'''
(OUT/'Spotlight-Visual-Polish.html').write_text(text)
pdfmetrics.registerFont(TTFont('DejaVu','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
c=canvas.Canvas(str(OUT/'Spotlight-Visual-Polish.pdf'),pagesize=(842,595))
def line(s,y,size=12):
 c.setFont('DejaVu',size);c.drawString(30,y,s)
def page(r):
 line(r['scene']+' · '+r['profile'],560,17)
 line('A · antes',530)
 c.drawString(435,530,'B · después integrado')
 for side,x in [('before',30),('after',435)]:
  img=ImageReader(str(path(r,side)));w,h=img.getSize();scale=min(375/w,470/h)
  c.drawImage(img,x+(375-w*scale)/2,40+(470-h*scale)/2,w*scale,h*scale,mask='auto')
 c.showPage()
line('Spotlight Next · Visual First',550,25)
for i,s in enumerate(['D1 integrado: radios 10 dp. D2–D4 conservan A.', 'Comparativas de componentes Compose reales con fixtures equivalentes.', 'PDF: corrección de desbordamiento a 330 dp / texto 200%.', 'Sin cambios de funciones, paquete, firma, Preview 65 o Focus B.', 'D5: experimento aislado; la ventana productiva sigue en 140/100 ms.', 'No hay dispositivo conectado: One UI/Gboard/FPS/RAM pendientes.', 'Galería HTML: 75 pares completos, ocho perfiles + paginación PDF.', 'Las imágenes originales y SHA-256 están en el ZIP/manifest.']):line(s,495-i*37,13)
c.showPage()
chosen=[('phone-dark','formulario'),('phone-light','formulario'),('tablet-light','ajustes'),('tablet-dark','ajustes'),('phone-dark-text200','formulario'),('phone-light-compact-ime','inspector'),('phone-dark','documento'),('phone-light','multimedia'),('pdf-330','dark-text200'),('pdf-330','light-text200')]
for p,s in chosen:
 page(next(r for r in m['pairs'] if r['profile']==p and r['scene']==s))
line('D5 · experimento; no adoptado',558,20)
line('A: helper de referencia 160 ms sin uso productivo. B: opacidad 120 ms de test.',530,12)
line('Ventana real: apertura 140 ms desde alfa 0,92; cierre 100 ms. Sin cambios.',507,12)
for row,v in enumerate(['A','B']):
 for col,t in enumerate([48,96,120]):
  p=ROOT/'motion'/f'dark-{v}-{t}.png';img=ImageReader(str(p));w,h=img.getSize();sc=min(240/w,175/h)
  x=35+col*265;y=285-row*215
  c.drawImage(img,x,y,w*sc,h*sc,mask='auto');c.setFont('DejaVu',11);c.drawString(x,y-18,f'{v} · {t} ms')
c.showPage();c.save()
with zipfile.ZipFile(OUT/'Spotlight-Visual-Polish-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 for name in ['Spotlight-Visual-Polish.html','Spotlight-Visual-Polish.pdf']:z.write(OUT/name,name)
 z.write(ROOT/'manifest.json','manifest.json')
 for p in sorted(set(path(r,s) for r in m['pairs'] for s in ['before','after'])):z.write(p,str(p.relative_to(REPO)))
 for p in sorted((ROOT/'motion').glob('*.png')):z.write(p,'motion/'+p.name)
print(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir() if p.is_file()},indent=2))
