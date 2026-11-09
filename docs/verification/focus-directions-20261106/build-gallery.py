"""Original Compose bytes only, no generated imagery or pixel edits."""
from pathlib import Path
import base64,json,html,hashlib,sys,zipfile
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
R=Path(__file__).resolve().parent;O=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-focus-directions');O.mkdir(parents=True,exist_ok=True)
m=json.loads((R/'manifest.json').read_text());rows=m['renders'];groups={}
for r in rows:groups.setdefault((r['profile'],r['scene']),{})[r['direction']]=r['file']
def pic(f,label):return '<figure><button onclick="zoom(this)"><img loading="lazy" src="data:image/png;base64,'+base64.b64encode((R/'images'/f).read_bytes()).decode()+'" alt="'+label+'"></button><figcaption>'+label+'</figcaption></figure>'
notes={'inicio':'Tres señales útiles del proveedor real sobre fixtures: contexto, acción reciente y una app. Sin relleno alfabético ni cambio del motor.','resultados':'Mismos resultados y orden. A usa indicador discreto; B suma contorno y tinte suave al seleccionado. Títulos, metadatos e iconos conservan sus roles.','inspector':'Búsqueda prioritaria y administración plegable aprobadas. A delimita el plano; B lo encuadra y refuerza su acento contextual. B consume más ancho interior.','acciones':'ActionPanel real: mismos comandos, filtro y estados. B propone un contenedor definido; no se agregan acciones.','ajustes':'Distribución nativa de tablet, ancho completo y tres temas en fila. B propone una superficie para el grupo; sin alterar la selección Geist ni las preferencias.'}
cards=[]
for (p,s),v in sorted(groups.items()):
 cards.append('<section data-profile="'+p+'" data-scene="'+s+'"><h2>'+p+' · '+s+'</h2><p>'+notes[s]+'</p><div class="pair">'+pic(v['A'],'A · Focus Refinado · propuesta')+pic(v['B'],'B · Focus Studio · propuesta')+'</div><details><summary>Referencia C · componentes actuales en el mismo fixture</summary>'+pic(v['C'],'C · control de componentes, no captura de APK')+'</details></section>')
style='body{font:16px/1.5 system-ui;background:#eae8e2;color:#252629;margin:0}main{max-width:1250px;margin:auto;padding:24px}header,section{padding:22px;background:#fffdf8;margin-bottom:24px;border-radius:14px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:14px}figure{margin:0;text-align:center}button{background:#eeece7;border:0;width:100%;padding:6px;cursor:zoom-in}img{max-width:100%;max-height:800px;object-fit:contain}figcaption{font-size:13px;padding:6px}.controls{position:sticky;top:0;background:#eae8e2;padding:12px;z-index:2}select{font:inherit;padding:10px}details{margin-top:20px}dialog{border:0;max-width:95vw;max-height:95vh}dialog img{max-height:88vh}dialog::backdrop{background:#111b}@media(max-width:650px){main{padding:8px}.pair{gap:5px}header,section{padding:12px}}'
profiles=sorted({p for p,s in groups})
header='<h1>Spotlight Focus · dos direcciones</h1><p><b>Propuestas Compose aisladas. No integradas en la aplicación.</b> La base Focus de Preview66 se conserva. A ordena el acabado; B hace más perceptibles el campo de búsqueda, el plano del inspector y el panel de acciones.</p><p>Recomendación: B como dirección visual, conservando la distribución de Ajustes y sin adoptar D5. Ventaja: identidad más visible con componentes existentes. Coste: 16dp menos de ancho en búsqueda y más espacio interior para marcos; revisar teléfono y texto200%.</p><p>Comparaciones equivalentes: mismos datos, orden, dimensiones, temas, selección y capacidades. El fixture compone componentes productivos; no ejecuta SpotlightPanel completo, WindowManager, NativeLaunchSession o Gboard. Los iconos con letras son PM sintéticos. Las alturas son encuadres de comparación, no una nueva política de ventana.</p><p>Geist, D1, acentos por aplicación y Focus B preservados. La animación productiva140ms permanece; no se ensaya otra animación aquí ni se afirma mejora de FPS/RAM.</p>'
script="function filter(){let p=document.querySelector('#profile').value,s=document.querySelector('#scene').value;document.querySelectorAll('section').forEach(e=>e.hidden=e.dataset.profile!==p||(s!=='all'&&e.dataset.scene!==s))}function zoom(b){let d=document.querySelector('dialog');d.querySelector('img').src=b.querySelector('img').src;d.showModal()}filter()"
(O/'Focus-Directions.html').write_text('<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spotlight Focus A/B</title><style>'+style+'</style><main><header>'+header+'</header><div class="controls"><select id="profile" onchange="filter()">'+''.join('<option'+(' selected' if p=='tablet-dark' else '')+'>'+p+'</option>' for p in profiles)+'</select><select id="scene" onchange="filter()"><option value="all">Todas las escenas</option>'+''.join('<option>'+s+'</option>' for s in notes)+'</select></div>'+''.join(cards)+'<dialog onclick="this.close()"><img alt="Captura original"></dialog></main><script>'+script+'</script></html>')
pdfmetrics.registerFont(TTFont('DV','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'));c=canvas.Canvas(str(O/'Focus-Directions.pdf'),pagesize=(1190,842))
def text(s,y,size=16):c.setFont('DV',size);c.drawString(30,y,s)
text('Spotlight Focus · A Refinado / B Studio',790,29)
for i,t in enumerate(['Propuestas reales Compose, aisladas en tests. Ninguna variante adoptada.', 'A: jerarquía y superficies discretas. B: búsqueda enmarcada e inspector definido.', 'Recomiendo B para una evolución visible; su coste es el espacio interior adicional.', 'Mismos componentes, motores y datos; no es una captura de APK o hardware.', 'Focus B, Geist, D1, ranking, temas y animación140ms conservados.', 'La galería HTML permite ampliar, comparar con C y revisar texto200%.']):text(t,720-i*60,19)
c.showPage()
featured=[('phone-dark','inicio'),('phone-light','resultados'),('tablet-dark','inspector'),('phone-dark','inspector'),('phone-light','acciones'),('tablet-light','ajustes'),('phone-light','ajustes')]
for p,s in featured:
 text(p+' · '+s,805,23);text('A · Focus Refinado',768);c.drawString(620,768,'B · Focus Studio')
 for d,x in [('A',30),('B',620)]:
  im=ImageReader(str(R/'images'/groups[(p,s)][d]));w,h=im.getSize();sc=min(540/w,700/h);c.drawImage(im,x+(540-w*sc)/2,40,w*sc,h*sc,mask='auto')
 c.showPage()
c.save()
with zipfile.ZipFile(O/'Focus-Directions-originals.zip','w',zipfile.ZIP_DEFLATED) as z:
 for r in rows:z.write(R/'images'/r['file'],'images/'+r['file'])
 z.write(R/'manifest.json','manifest.json')
 for f in ['Focus-Directions.html','Focus-Directions.pdf']:z.write(O/f,f)
hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in O.iterdir() if p.suffix in ['.html','.pdf','.zip']};(R/'artifact-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n');(O/'SHA256SUMS.txt').write_text(''.join(v+'  '+k+'\n' for k,v in hashes.items()))
print(len(rows),'PNG',len(groups),'comparisons')
