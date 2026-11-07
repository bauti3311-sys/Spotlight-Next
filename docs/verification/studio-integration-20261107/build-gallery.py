"""Before/after original production Compose PNGs. No retouching."""
from pathlib import Path
import base64,json,hashlib,sys,zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
R=Path(__file__).resolve().parent;O=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-studio-integration');O.mkdir(parents=True,exist_ok=True)
m=json.loads((R/'manifest.json').read_text())
def pic(p,label):return '<figure><button onclick="zoom(this)"><img loading="lazy" src="data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()+'" alt="'+label+'"></button><figcaption>'+label+'</figcaption></figure>'
sections=[]
for r in m['pairs']:
 sections.append('<section data-profile="'+r['profile']+'"><h2>'+r['scene']+' · '+r['profile']+'</h2><div class="pair">'+pic(R/'before'/r['file'],'Antes · Focus aprobado')+pic(R/'after'/r['file'],'Después · Studio integrado')+'</div></section>')
css='body{font:16px/1.5 system-ui;background:#eae8e2;color:#252629;margin:0}main{max-width:1250px;margin:auto;padding:24px}header,section{background:#fffdf8;padding:22px;margin-bottom:24px;border-radius:14px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:16px}figure{margin:0;text-align:center}button{width:100%;border:0;background:#eeece7;cursor:zoom-in;padding:6px}img{max-width:100%;max-height:800px;object-fit:contain}figcaption{font-size:13px}select{font:inherit;padding:10px;position:sticky;top:0;z-index:2}dialog{border:0;max-width:95vw;max-height:95vh}dialog img{max-height:88vh}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:8px}.pair{gap:5px}header,section{padding:12px}}'
profiles=sorted({r['profile'] for r in m['pairs']})
(O/'Studio-Integration.html').write_text('<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Studio integrado</title><style>'+css+'</style><main><header><h1>Spotlight Studio · integración aprobada</h1><p>Barra enmarcada, selección con acento discreto, inspector definido y materiales coherentes. Cuerpo A de Ajustes, Geist, D1 y acciones conservados.</p><p>Comparativas reales de producción Compose. Inicio/resultados/inspector: SpotlightPanel completo; ActionPanel/Ajustes: componentes productivos aislados. Datos PM/contexto sintéticos, sin efectos externos. API33, mismo tamaño/tema/estado. No son capturas de APK nueva ni pruebas One UI/Gboard.</p><p>Los márgenes decorativos ceden al ancho útil y texto200%; targets y legibilidad prioritarios. Sin blur, animación nueva o dependencias.140ms intactos, D5 aislado.</p></header><select id="profile" onchange="filter()">'+''.join('<option'+(' selected' if p=='tablet-dark' else '')+'>'+p+'</option>' for p in profiles)+'</select>'+''.join(sections)+'<dialog onclick="this.close()"><img alt="Original"></dialog></main><script>function filter(){document.querySelectorAll("section").forEach(e=>e.hidden=e.dataset.profile!==document.querySelector("#profile").value)}function zoom(b){let d=document.querySelector("dialog");d.querySelector("img").src=b.querySelector("img").src;d.showModal()}filter()</script></html>')
pdfmetrics.registerFont(TTFont('DV','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'));c=canvas.Canvas(str(O/'Studio-Integration.pdf'),pagesize=(1190,842))
def text(s,y,size=18):c.setFont('DV',size);c.drawString(30,y,s)
text('Spotlight Studio · antes / después',790,29)
for i,s in enumerate(['Integración productiva de B con cuerpo A en Ajustes.', 'Barra, selección, inspector y acciones visibles; sin cambiar motores o ejecución.', 'Acabados adaptables al ancho útil y texto ampliado; legacy reversible.', 'Mismos datos, dimensiones, temas y estados. Fixture PM explícito.', 'Sin APK/Actions, nueva animación ni métricas de hardware.']):text(s,720-i*65)
c.showPage()
for p,s in [('phone-dark','inicio'),('phone-light','resultados'),('tablet-dark','inspector'),('phone-dark-text200','inspector'),('phone-light','acciones'),('tablet-light','ajustes'),('phone-light-text200','ajustes')]:
 r=next(r for r in m['pairs'] if r['profile']==p and r['scene']==s);text(p+' · '+s,805,23);text('Antes',768);c.drawString(620,768,'Studio integrado')
 for side,x in [('before',30),('after',620)]:
  im=ImageReader(str(R/side/r['file']));w,h=im.getSize();sc=min(540/w,700/h);c.drawImage(im,x+(540-w*sc)/2,40,w*sc,h*sc,mask='auto')
 c.showPage()
c.save()
with zipfile.ZipFile(O/'Studio-Integration-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 for r in m['pairs']:
  for side in ['before','after']:z.write(R/side/r['file'],side+'/'+r['file'])
 z.write(R/'manifest.json','manifest.json')
 for f in ['Studio-Integration.html','Studio-Integration.pdf']:z.write(O/f,f)
hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in O.iterdir() if p.suffix in ['.html','.pdf','.zip']};(R/'artifact-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n');(O/'SHA256SUMS.txt').write_text(''.join(v+'  '+k+'\n' for k,v in hashes.items()))
print(len(m['pairs']),'pairs')
