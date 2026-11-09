"""Original Compose captures -> local review artifacts; no image modification."""
from pathlib import Path
import base64,hashlib,html,json,sys,zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
ROOT=Path(__file__).resolve().parent
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-visual-footer')
OUT.mkdir(parents=True,exist_ok=True)
m=json.loads((ROOT/'manifest.json').read_text())
def img(p,label):return '<figure><button onclick="zoom(this)"><img src="data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()+'" alt="'+label+'"></button><figcaption>'+label+'</figcaption></figure>'
rows=''
for r in m['pairs']:
 rows+='<section><h2>'+html.escape(r['name'])+'</h2><p>'+('Base protegida: imágenes idénticas.' if r['before_sha256']==r['after_sha256'] else 'Cambio de distribución: el texto ampliado usa el dock compacto existente.')+'</p>'+img(ROOT/'before'/r['name'],'A · antes')+img(ROOT/'after'/r['name'],'B · después integrado')+'</section>'
for p in sorted((ROOT/'extra-validation').glob('*.png')):
 rows+='<section><h2>Validación adicional · '+html.escape(p.name)+'</h2>'+img(p,'Después · fixture de comprobación')+'</section>'
(OUT/'Spotlight-Footer.html').write_text('''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spotlight · footer adaptable</title><style>body{font:16px/1.5 system-ui;background:#eae8e2;color:#26282a;margin:0}main{max-width:1100px;margin:auto;padding:24px}header,section{padding:22px;background:#fffdf8;margin-bottom:24px;border-radius:14px}figure{margin:10px 0}button{background:#efede7;border:0;padding:8px;cursor:zoom-in;width:100%}img{max-width:100%;object-fit:contain}figcaption{font-size:13px}dialog{border:0;border-radius:12px;max-width:94vw;max-height:94vh}dialog img{max-height:85vh}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:10px}header,section{padding:12px}}</style><main><header><h1>Spotlight · footer adaptable</h1><p>Corrección acotada V05/V06: a 700 dp y texto 200 %, Acciones quedaba con ancho cero y la barra crecía verticalmente. La distribución compacta existente recupera las acciones sin cambiar callbacks ni atajos.</p><p>A/B: componente Compose real, API33/Robolectric, mismos datos, ancho y escala. Sin APK nueva ni validación One UI/Gboard. No mide FPS/RAM.</p><p>La escena normal de tablet y la de teléfono se conservan byte a byte. Las etiquetas auxiliares de teclado se omiten en modo compacto, sin desactivar los atajos.</p></header>'''+rows+'''<dialog onclick="this.close()"><img alt="Captura original"></dialog></main><script>function zoom(b){let d=document.querySelector('dialog');d.querySelector('img').src=b.querySelector('img').src;d.showModal()}</script></html>''')
pdfmetrics.registerFont(TTFont('DejaVu','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
c=canvas.Canvas(str(OUT/'Spotlight-Footer.pdf'),pagesize=(842,595))
def text(s,y,size=12):c.setFont('DejaVu',size);c.drawString(30,y,s)
text('Spotlight · footer adaptable',550,25)
for i,s in enumerate(['V05/V06: corregir el desbordamiento con texto ampliado.', 'A 700 dp / 200 %, Acciones tenía ancho cero; el dock medía 308 dp de alto.', 'Después: distribución compacta existente, sin nuevas acciones ni preferencias.', 'Ancho de Ajustes, tipografía, selección, Focus B y movimiento intactos.', 'Componentes Compose reales con fixtures: sin evidencia física nueva.', 'Capturas originales y hashes en manifest.json / ZIP.']):text(s,490-i*45,13)
c.showPage()
for r in m['pairs']:
 text(r['name'],558,18)
 for side,y in [('before',300),('after',60)]:
  text('A · antes' if side=='before' else 'B · después integrado',y+190)
  im=ImageReader(str(ROOT/side/r['name']));w,h=im.getSize();scale=min(780/w,175/h)
  c.drawImage(im,30,y,w*scale,h*scale,mask='auto')
 c.showPage()
c.save()
with zipfile.ZipFile(OUT/'Spotlight-Footer-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(ROOT.rglob('*.png')):z.write(p,str(p.relative_to(ROOT)))
 z.write(ROOT/'manifest.json','manifest.json')
 for p in [OUT/'Spotlight-Footer.html',OUT/'Spotlight-Footer.pdf']:z.write(p,p.name)
hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir() if p.suffix in ['.html','.pdf','.zip']}
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{v}  {k}\n' for k,v in hashes.items()))
(ROOT/'artifact-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
print(json.dumps(hashes,indent=2))
