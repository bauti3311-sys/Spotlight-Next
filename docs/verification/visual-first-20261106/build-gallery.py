"""Offline review artifact from original Compose PNG bytes. No image manipulation or network."""
from pathlib import Path
import base64,json,html,sys
ROOT=Path(__file__).resolve().parent
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/workspace/shared/downloads/spotlight-visual-first')
OUT.mkdir(parents=True,exist_ok=True)
data=json.loads((ROOT/'manifest.json').read_text())
esc=html.escape
profiles={
 'phone-dark':'Teléfono · oscuro · 390 × 760', 'phone-light':'Teléfono · claro · 390 × 760',
 'tablet-dark':'Referencia A9+ · oscuro · ventana 1000 × 720', 'tablet-light':'Referencia A9+ · claro · ventana 1000 × 720',
 'phone-dark-text200':'Compacta · oscuro · 330 dp · texto 200 %', 'phone-light-text200':'Compacta · claro · 330 dp · texto 200 %',
 'phone-dark-compact-ime':'Compacta · oscuro · texto 130 % · IME reservado 300 dp',
 'phone-light-compact-ime':'Compacta · claro · texto 130 % · IME reservado 300 dp'}
scenes={'inicio':'Inicio','resultados':'Búsqueda y resultados','inspector':'Inspector B aprobado','acciones':'Acciones contextuales','ajustes':'Ajustes','formulario':'Formulario de mensaje','documento':'Documento local TXT','multimedia':'Multimedia · sesión fixture','estado-error':'Estado de error · fixture'}
comments={
 'inicio':'Se conserva la superficie aprobada. Engine real sobre paquetes de laboratorio; máximo de sugerencias intacto.',
 'resultados':'Misma consulta, ranking y selección. Sin cambio propuesto de resultados.',
 'inspector':'Búsqueda prioritaria y administración plegable aprobadas. A y B muestran el mismo original; no se propone otro inspector.',
 'acciones':'Mismos comandos y estados. B ensaya ancho de lectura/controles; no ejecuta acciones.',
 'ajustes':'En tablet, B pasa los tres temas a dos filas y desplaza la opacidad. Recomendación: conservar ancho y selección de A.',
 'formulario':'Mismo número, texto y campo enfocado. B redondea los campos; recomiendo conservar tamaño de título A. El scroll puede variar al revelar el mismo campo.',
 'documento':'FilePreview real sobre TXT local de prueba; ningún archivo del usuario. B ensaya ancho de lectura, no una nueva tarjeta.',
 'multimedia':'Sesión sintética en pausa, sin audio. B no demuestra reproducción ni control de Spotify.',
 'estado-error':'Escena de componentes con datos ficticios. No es un fallo real de una cuenta ni una nueva capacidad.'}
status=[
 ('V01','Obsidian Glass 2.0','Avanzada','Conservar superficies y paleta; acabado periférico.'),
 ('V02','Inspector contextual','Base B aprobada','Preservar jerarquía; revisar sólo contextos especializados.'),
 ('V03','Acentos por aplicación','Avanzada','Mantener destino y contraste; no colorear todo el fondo.'),
 ('V04','Jerarquía de resultados','Avanzada','Conservar ranking, selección y objetivos táctiles.'),
 ('V05','Footer contextual','Implementado; aceptación pendiente','Conservar atajos; confirmar IME/etiquetas largas en equipo real.'),
 ('V06','Ventana adaptable','Parcial en aceptación','Simulación IME disponible; One UI/Gboard todavía pendientes.'),
 ('V07','Iconografía iOS','Avanzada por rol','No sustituir iconos. Evitar normalización global sin criterio.'),
 ('V08','Tarjetas contextuales','Parcial','Densidad y lectura; documento TXT y multimedia de laboratorio.'),
 ('V10','Microinteracciones','Parcial','B de opacidad 120 ms es una propuesta; sin promesas de FPS/RAM.')]
def uri(name):return 'data:image/png;base64,'+base64.b64encode((ROOT/'images'/name).read_bytes()).decode()
def picture(name,label):return f'<figure><button class="image" onclick="zoom(this)"><img loading="lazy" src="{uri(name)}" alt="{esc(label)}"></button><figcaption>{esc(label)} · tocar para ampliar</figcaption></figure>'
rows={(r['profile'],r['scene'],r['variant']):r for r in data['renders']}
featured={(p,s) for p,s in [('phone-dark',s) for s in scenes]}
featured.update({('tablet-light','ajustes'),('phone-dark-text200','formulario'),('phone-light-compact-ime','inspector')})
cards=[]
for profile in profiles:
 for scene,title in scenes.items():
  a=rows[(profile,scene,'A')];b=rows.get((profile,scene,'B'),a)
  frozen=a['preserved']
  equal=(ROOT/'images'/a['file']).read_bytes()==(ROOT/'images'/b['file']).read_bytes()
  tag='Base aprobada · sin nueva variante' if frozen else ('Sin diferencias visibles en este perfil' if equal else 'Propuesta B · no integrada')
  caption=f"{profiles[profile]} · Compose/API33 · {a['font_scale']*100:.0f}% · IME {a['ime_reserved_dp']}dp"
  cards.append(f'''<section class="comparison" data-profile="{profile}" data-scene="{scene}" data-featured="{int((profile,scene) in featured)}">
  <h2>{esc(title)}</h2><p class="meta">{esc(caption)}</p><span class="tag">{tag}</span>
  <p>{esc(comments[scene])}</p><div class="pair">{picture(a['file'],'A · base vigente en fixture')}{picture(b['file'],'B · conservado' if frozen else 'B · ensayo visual de test')}</div>
  </section>''')
icon_html=''.join(f'<section class="icons"><h2>V07 · iconos {theme}</h2><p>Inventario renderizado con glyphs existentes. A: tamaños por contexto; B: 20 dp. No son pantallas de la app. Hits de 48 × 48 dp en ambas. Recomiendo preservar roles.</p><div class="pair">{picture("icons-"+theme+"-A.png","A · inventario por rol")}{picture("icons-"+theme+"-B.png","B · ensayo 20 dp")}</div></section>' for theme in ['dark','light'])
frames={v:[uri(f'motion-{v}-{t}.png') for t in [0,48,96,160,208]] for v in ['A','B']}
motion='''<section class="motion"><h2>V10 · entrada de superficie</h2><p>A: código actual, 160 ms + 6 dp y escala. B: opacidad 120 ms, sólo test. Fotogramas Compose reproducidos 4× más lento. No es una medición de fluidez en un dispositivo.</p><div class="pair">'''
for v in ['A','B']:
 motion+=f'<figure class="moving"><img id="motion{v}" src="{frames[v][-1]}" alt="Movimiento {v}"><figcaption>{v} · fotogramas reales</figcaption></figure>'
motion+='</div><div class="filmstrip">'
for v in ['A','B']:
 for i,t in [(0,0),(2,96),(4,208)]:motion+=f'<figure><img src="{frames[v][i]}" alt="{v} {t}ms"><figcaption>{v} · {t} ms</figcaption></figure>'
motion+='</div><h3>Reducción de movimiento</h3><p>En ambos casos, contenido visible sin transición en el fixture. El observador de la preferencia Android se conserva; no se validó aquí el sistema físico.</p><div class="pair">'+picture('motion-A-reduced.png','A · movimiento reducido')+picture('motion-B-reduced.png','B · movimiento reducido')+'</div></section>'
table='<table><thead><tr><th>ID</th><th>Mejora</th><th>Estado</th><th>Criterio</th></tr></thead><tbody>'+''.join('<tr>'+''.join(f'<td>{esc(cell)}</td>' for cell in row)+'</tr>' for row in status)+'</tbody></table>'
options=''.join(f'<option value="{key}">{esc(value)}</option>' for key,value in profiles.items())
scene_options='<option value="all">Todas las escenas</option>'+''.join(f'<option value="{key}">{esc(value)}</option>' for key,value in scenes.items())
style='''body{font:15px/1.5 system-ui,sans-serif;background:#eeece7;color:#252629;margin:0}main{max-width:1180px;margin:auto;padding:28px}h1{font-size:32px;margin:0 0 10px}h2{font-size:23px;margin:0 0 6px}h3{font-size:18px}header,.comparison,.icons,.motion{background:#fffdf8;border:1px solid #d2cec5;border-radius:16px;padding:24px;margin-bottom:24px}.meta,figcaption{color:#575a60;font-size:12px}.tag{display:inline-block;background:#e7e9e4;padding:3px 9px;border-radius:8px;font-size:12px}.pair{display:grid;grid-template-columns:1fr 1fr;gap:18px}figure{margin:0;text-align:center}.image{border:0;background:#e8e5df;padding:10px;width:100%;cursor:zoom-in;border-radius:12px}.image img{max-height:700px;width:100%;object-fit:contain}.pair figure img{max-width:100%}figcaption{padding:8px}button,select{font:inherit;min-height:44px;border:1px solid #aaa69e;border-radius:8px;padding:8px 12px;background:#faf9f5;color:#252629}.controls{display:flex;flex-wrap:wrap;gap:10px;position:sticky;top:0;z-index:2;background:#eeece7;padding:12px;border-radius:12px;margin-bottom:22px}.notice{border-left:4px solid #636f64;padding:12px;background:#f0f2ee}.filmstrip{display:grid;grid-template-columns:repeat(3,1fr);gap:8px}.filmstrip img{width:100%}table{border-collapse:collapse;width:100%;font-size:12px}th,td{border-bottom:1px solid #ddd8cf;text-align:left;padding:8px;vertical-align:top}dialog{max-width:96vw;max-height:95vh;border:0;border-radius:12px;padding:14px;background:#eeece7}dialog img{max-width:90vw;max-height:85vh;object-fit:contain}dialog::backdrop{background:#111c}.moving img{width:100%}@media(max-width:650px){main{padding:12px}.pair{gap:8px}header,.comparison,.icons,.motion{padding:14px}h1{font-size:25px}table{font-size:11px}}@media print{@page{size:A4;margin:12mm}body{background:white;font-size:10pt}main{padding:0;max-width:none}.controls,.screen-only,dialog{display:none!important}header{padding:12px}.comparison:not([data-featured="1"]){display:none!important}.comparison[data-featured="1"]{display:block!important}.comparison,.icons,.motion{break-before:page;break-inside:avoid;border:0;padding:0;margin:0 0 12px}.image{background:transparent;padding:0}.image img{max-height:220mm;width:auto;max-width:100%}.pair{gap:9mm}h2{font-size:16pt}.moving{display:none}figcaption{font-size:9pt}.filmstrip img{max-height:60mm;object-fit:contain}.motion .pair .image img{max-height:45mm}header table{font-size:8pt}header h1{font-size:24pt}.motion{break-inside:auto}}'''
script='''function filter(){let p=document.querySelector('#profile').value,s=document.querySelector('#scene').value;document.querySelectorAll('.comparison').forEach(e=>e.style.display=(e.dataset.profile===p&&(s==='all'||e.dataset.scene===s))?'block':'none')}function jump(p,s){document.querySelector('#profile').value=p;document.querySelector('#scene').value=s;filter();document.querySelector('.controls').scrollIntoView()}function zoom(button){let d=document.querySelector('dialog');d.querySelector('img').src=button.querySelector('img').src;d.showModal()}document.querySelectorAll('select').forEach(e=>e.addEventListener('change',filter));filter();let frame=0;function animate(){let n=frame;for(let v of ['A','B'])document.querySelector('#motion'+v).src=frames[v][n];frame=(frame+1)%5;setTimeout(animate,[192,192,256,192,800][n])}if(!matchMedia('(prefers-reduced-motion: reduce)').matches)animate();'''
page=f'''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Spotlight · Visual First · revisión 06/11</title><style>{style}</style><main><header><h1>Spotlight Next · Visual First</h1><p>Auditoría de V01, V02, V03, V04, V05, V06, V07, V08 y V10. Base f27c867 · PR21 borrador. Las funcionalidades C3–C8 están en pausa.</p><p class="notice"><strong>Capturas reales de Compose; no imágenes conceptuales ni APK nueva.</strong> Datos de laboratorio, API33, sin cuentas/audio. A usa componentes vigentes; B es una propuesta sólo de test. Inicio/resultados/inspector usan SpotlightPanel real. Las otras escenas montan componentes en un marco de laboratorio: no son screenshots íntegros de la Activity. No se alteró producción.</p><p>Galería: 72 pares en ocho perfiles, 136 PNG originales contando iconos y movimiento. Focus B se conserva. El PDF selecciona doce comparativas, más iconos y movimiento; el HTML contiene toda la matriz.</p>{table}<h2>Propuesta recomendada</h2><p><strong>Conservar la base A y Focus B. Tomar B sólo para radios de campos equivalentes; ensayar B de movimiento.</strong> Mantener ancho y selección de A en Ajustes y títulos de 17 sp. B completo reduce información visible en tablet: esa pérdida está mostrada, no ocultada.</p><p>Decisiones pendientes: radios10dp y entrada por opacidad120ms. El paquete de ancho/títulos/neutralización B no se recomienda globalmente. La aprobación se confirma en la conversación; la galería no activa preferencias.</p><p class="meta">Geist, temas, identidad y funcionalidades conservados. Monogramas de apps, documento TXT y sesión pausada son fixtures. IME reservado es simulación de oclusión, nunca Gboard real. One UI, física A9+/moto, PDF y métricas RAM/FPS pendientes.</p></header><nav class="controls"><select id="profile">{options}</select><select id="scene">{scene_options}</select><button onclick="jump('tablet-light','ajustes')">Ver coste de B en tablet</button><button onclick="jump('phone-dark','formulario')">Ver radios A/B</button><button onclick="jump('phone-light-text200','formulario')">Texto200%</button></nav>{''.join(cards)}{icon_html}{motion}<p class="meta">Generado con los PNG originales y manifest.json. Sin recursos remotos, dependencias ni red.</p></main><dialog><button onclick="this.parentNode.close()">Cerrar ampliación</button><br><img alt="Captura ampliada"></dialog><script>const frames={json.dumps(frames)};{script}</script></html>'''
page=page.replace(table,'<details><summary>Ver auditoría de las nueve V</summary>'+table+'</details>')
page=page.replace('<nav class="controls">','<nav class="controls" id="comparativas">')
page=page.replace('<h1>Spotlight Next · Visual First</h1>','<h1>Spotlight Next · Visual First</h1><p><a href="#comparativas">Ir a las comparativas reales A/B</a></p>')
(OUT/'Galeria-Visual-First.html').write_text(page)
print(str(OUT/'Galeria-Visual-First.html'))

# Print a static subset: no timers, lazy loading or hidden images from the interactive view.
print_page=page.replace(''.join(cards),''.join(c for c in cards if 'data-featured="1"' in c)).replace('loading="lazy"','loading="eager"')
print_page=print_page[:print_page.rfind('<script>')]+'</html>'
(OUT/'Revision-imprimible.html').write_text(print_page)

# PDF embeds the untouched source images; ReportLab is an existing workspace tool, not an app dependency.
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
from reportlab.lib.utils import ImageReader,simpleSplit
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
pdfmetrics.registerFont(TTFont('ReviewSans','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
W,H=landscape(A4)
pdf=canvas.Canvas(str(OUT/'Visual-First-Revision.pdf'),pagesize=(W,H))
pdf.setTitle('Spotlight Next · Visual First · auditoría y comparativas')
def text(x,y,value,size=10,width=None):
 pdf.setFont('ReviewSans',size);pdf.setFillColorRGB(.14,.15,.16)
 lines=simpleSplit(value,'ReviewSans',size,width) if width else [value]
 for line in lines:pdf.drawString(x,y,line);y-=size*1.4
 return y
def footer():text(28,18,'Compose/API33 · fixtures explícitos · no dispositivo físico ni APK · base f27c867',8)
def original(name,x,y,w,h):
 reader=ImageReader(str(ROOT/'images'/name));iw,ih=reader.getSize();scale=min(w/iw,h/ih)
 pdf.drawImage(reader,x+(w-iw*scale)/2,y+(h-ih*scale)/2,width=iw*scale,height=ih*scale,mask='auto')
y=text(28,H-38,'Spotlight Next · Visual First',23)
y=text(28,y-6,'Auditoría de nueve V · decisiones pendientes · 06/11/2026',12)
y=text(28,y-14,'Base aprobada conservada. Sin cambios productivos; C3–C8 pausados. HTML: matriz completa de 72 pares en ocho perfiles.',10,W-56)
y-=14
for v,title,state,criterion in status:
 y=text(28,y,f'{v} · {title} — {state}',11)
 y=text(42,y,criterion,9,W-70)-7
y-=5
y=text(28,y,'Recomiendo: radios B en campos equivalentes; conservar ancho, títulos y selección de A; ensayar entrada B de 120 ms.',10,W-56)
y=text(28,y-4,'Datos sintéticos y tamaños lógicos. No se retocaron imágenes. El PDF selecciona 12 pares; el HTML permite ampliar todos los originales.',9,W-56)
footer();pdf.showPage()
ordered=[('phone-dark',s) for s in scenes]+[('tablet-light','ajustes'),('phone-dark-text200','formulario'),('phone-light-compact-ime','inspector')]
for profile,scene in ordered:
 a=rows[(profile,scene,'A')];b=rows.get((profile,scene,'B'),a)
 y=text(28,H-32,scenes[scene],18)
 y=text(28,y-4,profiles[profile],10)
 y=text(28,y-5,comments[scene],9,(W-56)*.82)
 text(28,y-12,'A · base en fixture',11)
 text(W/2+12,y-12,'B · conservado' if a['preserved'] else 'B · propuesta sólo de test',11)
 h=y-76;original(a['file'],24,36,W/2-38,h);original(b['file'],W/2+14,36,W/2-38,h)
 footer();pdf.showPage()
for theme in ['dark','light']:
 text(28,H-34,f'V07 · iconos {theme}',18)
 text(28,H-62,'Inventario por rol vs ensayo 20 dp. Assets existentes; hits de 48 × 48 dp en ambos. No son pantallas de la app.',10,W-56)
 text(28,H-90,'A · tamaños de contextos reales',11);text(W/2+12,H-90,'B · todos 20 dp',11)
 original(f'icons-{theme}-A.png',24,38,W/2-38,H-144);original(f'icons-{theme}-B.png',W/2+14,38,W/2-38,H-144)
 footer();pdf.showPage()
text(28,H-34,'V10 · fotogramas Compose reales',18)
text(28,H-59,'A: entrada actual 160 ms + 6 dp/escala. B: opacidad 120 ms. No es una medición de FPS, jank ni consumo.',10,W-56)
for row,v in enumerate(['A','B']):
 top=H-90-row*155
 for col,t in enumerate([0,96,208]):
  x=28+col*((W-56)/3);text(x,top,f'{v} · {t} ms',9)
  original(f'motion-{v}-{t}.png',x,top-136,(W-70)/3,124)
text(28,154,'Movimiento reducido: contenido visible sin transición en ambos fixtures.',10)
original('motion-A-reduced.png',28,37,W/2-42,105);original('motion-B-reduced.png',W/2+14,37,W/2-42,105)
footer();pdf.showPage();pdf.save()
print(str(OUT/'Visual-First-Revision.pdf'))
