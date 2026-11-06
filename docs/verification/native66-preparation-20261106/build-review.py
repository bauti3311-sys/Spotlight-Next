"""Consolidated original captures; historical pairs clearly distinguished from current audit."""
from pathlib import Path
import base64,hashlib,html,json,sys,zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
ROOT=Path(__file__).resolve().parent;REPO=ROOT.parents[2]
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-preview66-preparation')
OUT.mkdir(parents=True,exist_ok=True)
m=json.loads((ROOT/'visual-manifest.json').read_text())
def picture(file,label):return '<figure><button onclick="zoom(this)"><img src="data:image/png;base64,'+base64.b64encode((REPO/file).read_bytes()).decode()+'" alt="'+html.escape(label)+'"></button><figcaption>'+html.escape(label)+'</figcaption></figure>'
rows=''
for r in m['historical_approved_pairs']:
 rows+='<section><h2>'+html.escape(r['title'])+'</h2><p>Comparativa histórica aprobada, conservada sin retoque. No es una nueva APK66.</p><div class="pair">'+picture(r['before'],'Antes de la corrección aprobada')+picture(r['after'],'Corrección incluida en la fuente candidata')+'</div></section>'
for r in m['audit']:rows+='<section><h2>'+html.escape(r['caption'])+'</h2><p>Auditoría de componentes productivos con datos sintéticos; sin nuevo cambio de diseño.</p>'+picture(r['file'],'Auditoría actual · sin APK ni dispositivo físico')+'</section>'
(OUT/'Preview66-Visual.html').write_text('''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spotlight · candidata Preview66</title><style>body{font:16px/1.5 system-ui;background:#eae8e2;color:#26282a;margin:0}main{max-width:1150px;margin:auto;padding:24px}header,section{padding:22px;background:#fffdf8;margin-bottom:24px;border-radius:14px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:16px}figure{margin:0;text-align:center}button{background:#efede7;border:0;padding:8px;cursor:zoom-in;width:100%}img{max-width:100%;max-height:780px;object-fit:contain}figcaption{font-size:13px}dialog{border:0;border-radius:12px;max-width:94vw;max-height:94vh}dialog img{max-height:85vh}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:10px}header,section{padding:12px}.pair{gap:6px}}</style><main><header><h1>Spotlight Next · candidata Preview66</h1><p>Consolidación Visual First: FocusB, radiosD1, footer adaptable, paginaciónPDF y acciones de notificación visibles. No hay diseño nuevo; D5 no adoptado.</p><p>Fuente candidata con versionCode66 preparada; sin APK, firma nueva ni Actions. Las tres comparativas son originales de checkpoints previos aprobados. Las cuatro capturas nuevas prueban un presupuesto interior266dp, texto200%, componentes reales con fixtures, en ambos temas.</p><p>OneUI/Gboard, ventanas reales, datos instalados, PDF real y fluidez siguen pendientes. Ninguna captura demuestra reproducción, envío o cuenta real.</p></header>'''+rows+'''<dialog onclick="this.close()"><img alt="Captura original"></dialog></main><script>function zoom(b){let d=document.querySelector('dialog');d.querySelector('img').src=b.querySelector('img').src;d.showModal()}</script></html>''')
pdfmetrics.registerFont(TTFont('DejaVu','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
c=canvas.Canvas(str(OUT/'Preview66-Visual.pdf'),pagesize=(842,595))
def text(s,y,size=12):c.setFont('DejaVu',size);c.drawString(30,y,s)
def draw(file,x,y,w,h):
 im=ImageReader(str(REPO/file));iw,ih=im.getSize();sc=min(w/iw,h/ih);c.drawImage(im,x+(w-iw*sc)/2,y,iw*sc,ih*sc,mask='auto')
text('Spotlight · candidata Preview66',550,25)
for i,s in enumerate(['Visual First consolidado; no APK ni Actions ejecutadas.', 'Identidad Preview: code66; paquete y certificado histórico conservados.', 'Focus B, D1, footer, PDF y notificaciones preservados.', 'D5 sigue aislado: la ventana real abre140ms/cierra100ms.', 'Tres comparativas históricas originales y cuatro capturas nuevas de auditoría.', 'No hay nuevo cambio visual ni evidencia física OneUI/Gboard/FPS/RAM.']):text(s,490-i*45,13)
c.showPage()
for r in m['historical_approved_pairs']:
 text(r['title']+' · histórico aprobado',560,17);text('Antes',530);c.drawString(435,530,'Incluido en candidata')
 for side,x in [('before',30),('after',435)]:draw(r[side],x,35,375,470)
 c.showPage()
for r in m['audit']:
 text(r['caption'].split(' · ')[0]+' · auditoría actual',560,17);text('330dp/200% · interior266dp · fixture de componentes, no APK',530,12)
 draw(r['file'],30,30,780,470);c.showPage()
c.save()
with zipfile.ZipFile(OUT/'Preview66-Visual-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 files={r['file'] for r in m['audit']}
 for r in m['historical_approved_pairs']:files.update([r['before'],r['after']])
 for file in sorted(files):z.write(REPO/file,file)
 z.write(ROOT/'visual-manifest.json','visual-manifest.json')
 for p in [OUT/'Preview66-Visual.html',OUT/'Preview66-Visual.pdf']:z.write(p,p.name)
hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir() if p.suffix in ['.html','.pdf','.zip']}
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{v}  {k}\n' for k,v in hashes.items()))
(ROOT/'artifact-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
print(json.dumps(hashes,indent=2))
