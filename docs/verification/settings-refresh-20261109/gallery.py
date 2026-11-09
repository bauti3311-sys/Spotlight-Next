"""Compare untouched native Compose/Robolectric captures. No reconstructed UI."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import base64,json,hashlib,zipfile,shutil
O=Path(__file__).resolve().parent;R=O.parents[2];S=Path('/workspace/shared/downloads/spotlight-ajustes');S.mkdir(exist_ok=True,parents=True)
scenes=[('tablet-light-home','Tablet · Inicio · claro'),('tablet-dark-home','Tablet · Inicio · oscuro'),('tablet-light-appearance','Tablet · Apariencia · claro'),('tablet-dark-appearance','Tablet · Apariencia · oscuro'),('phone-light-home','Teléfono · Inicio'),('phone-light-appearance','Teléfono · Apariencia'),('phone-dark-200-appearance','Teléfono · texto 200 %'),('width-narrow','Puente · ancho mínimo'),('width-wide','Puente · ancho máximo')]
D=[]
for id,title in scenes:
 pair={'id':id,'title':title}
 for phase in ['before','after']:
  p=O/phase/(id+'.png');assert p.exists(),p
  pair[phase]='data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()
 D.append(pair)
html='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Spotlight · Ajustes nuevos</title><style>*{box-sizing:border-box}body{margin:0;padding:28px;background:#e6eef2;color:#203c4a;font:16px/1.5 system-ui}main{max-width:1500px;margin:auto}h1{font-size:38px;letter-spacing:-.04em}p{max-width:950px}nav{display:flex;flex-wrap:wrap;gap:8px;margin:25px 0}button{font:inherit;background:#f6fbfd;color:#203c4a;border:1px solid #9bb7c4;border-radius:9px;padding:12px;cursor:pointer}button[aria-pressed=true]{background:#205b70;color:white}.pair{display:grid;grid-template-columns:1fr 1fr;gap:18px}.pair section{min-width:0}img{display:block;width:100%;height:auto;max-height:80vh;object-fit:contain;object-position:top;background:#d0dce2;border-radius:12px}small{color:#4e6977}.note{padding:16px;border:1px solid #b7cbd4;border-radius:12px;background:#f4f9fc}a{color:#205b70}@media(max-width:650px){body{padding:16px}.pair{grid-template-columns:1fr}h1{font-size:29px}}</style><main><small>SPOTLIGHT NEXT · IMPLEMENTACIÓN NATIVA · 09/11/2026</small><h1>Ajustes claros. Ancho que responde.</h1><p>Control de ancho prioritario, secciones directas en teléfono y navegación lateral en tablet. Texto, espaciado y transparencias bajo demanda. Preferencias y accesos conservados.</p><div class="note"><b>Capturas Compose reales en Robolectric, no APK ni dispositivo físico.</b> Cada comparación conserva perfil, tema, escala y datos. En teléfono cambió también el recorrido: ahora se entra por secciones visibles. El ancho del buscador ya no queda bloqueado por un máximo interno de 560 dp al elegir un tamaño manual.</div><nav id="nav"></nav><h2 id="title"></h2><div class="pair"><section><h3>Antes</h3><img id="before" alt="Antes, Compose"/></section><section><h3>Después</h3><img id="after" alt="Después, Compose"/></section></div><p><small>PNG originales sin retoque, dentro del repositorio y ZIP. Los montajes y PDF sólo redimensionan y rotulan fuera de las capturas. One UI, Gboard y WindowManager físico siguen pendientes. No se generó otra APK.</small></p></main><script>const scenes=__DATA__;let current=0;function show(i){current=i;document.querySelector('#title').textContent=scenes[i].title;for(let k of ['before','after'])document.querySelector('#'+k).src=scenes[i][k];document.querySelector('#nav').innerHTML=scenes.map((x,n)=>'<button data-scene="'+n+'" aria-pressed="'+(n===i)+'">'+x.title+'</button>').join('')}document.querySelector('#nav').onclick=e=>{let b=e.target.closest('[data-scene]');if(b)show(Number(b.dataset.scene))};show(2);</script></html>'''.replace('__DATA__',json.dumps(D,ensure_ascii=False).replace('<','\\u003c'))
(O/'Antes-despues-Ajustes.html').write_text(html)
font=R/'app/src/main/res/font/geist_regular.ttf';bold=R/'app/src/main/res/font/geist_semibold.ttf';pdfmetrics.registerFont(TTFont('Geist',str(font)));pdfmetrics.registerFont(TTFont('GeistBold',str(bold)))
W,H=landscape(A4);pdf=canvas.Canvas(str(O/'Ajustes-antes-despues.pdf'),pagesize=(W,H));pdf.setTitle('Spotlight · Ajustes y reparación de ancho')
for id,title in [scenes[2],scenes[5],scenes[6],scenes[8]]:
 pdf.setFillColorRGB(.90,.94,.96);pdf.rect(0,0,W,H,fill=1,stroke=0);pdf.setFillColorRGB(.12,.24,.3);pdf.setFont('GeistBold',20);pdf.drawString(24,H-36,title)
 for j,phase in enumerate(['before','after']):
  im=Image.open(O/phase/(id+'.png'));maxw=(W-72)/2;maxh=H-125;s=min(maxw/im.width,maxh/im.height);x=24+j*(maxw+24)
  pdf.setFont('GeistBold',12);pdf.drawString(x,H-66,'Antes' if phase=='before' else 'Después');pdf.drawImage(ImageReader(im),x,H-80-im.height*s,im.width*s,im.height*s)
 pdf.setFont('Geist',9);pdf.drawString(24,22,'Compose/Robolectric · capturas originales · sin APK ni validación física');pdf.showPage()
pdf.save()
for id,title in [scenes[2],scenes[5],scenes[8]]:
 imgs=[Image.open(O/p/(id+'.png')).convert('RGB') for p in ['before','after']];thumbs=[]
 for im in imgs:im.thumbnail((900,900));thumbs.append(im)
 w=sum(im.width for im in thumbs)+60;h=max(im.height for im in thumbs)+100;out=Image.new('RGB',(w,h),'#e6eef2');draw=ImageDraw.Draw(out);f=ImageFont.truetype(str(bold),24);x=20
 for label,im in zip(['ANTES · Compose','DESPUÉS · Compose'],thumbs):draw.text((x,15),label,font=f,fill='#203c4a');out.paste(im,(x,60));x+=im.width+20
 out.save(O/(id+'-comparacion.jpg'),quality=94)
files=[p for p in O.rglob('*') if p.is_file() and p.name not in ['Ajustes-native-offline.zip','SHA256SUMS'] and '__pycache__' not in p.parts]
(O/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+str(p.relative_to(O))+'\n' for p in sorted(files)))
with zipfile.ZipFile(O/'Ajustes-native-offline.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in files+[O/'SHA256SUMS']:z.write(p,p.relative_to(O))
for p in [O/'Antes-despues-Ajustes.html',O/'Ajustes-antes-despues.pdf',O/'Ajustes-native-offline.zip',*O.glob('*-comparacion.jpg')]:shutil.copy2(p,S/p.name)
print('Galería de capturas nativas, PDF4p y ZIP listos.')
