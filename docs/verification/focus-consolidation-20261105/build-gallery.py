from pathlib import Path
import json,shutil,hashlib,base64,html
from PIL import Image,ImageDraw,ImageFont
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
repo=Path(__file__).resolve().parents[3]; out=repo/'docs/verification/focus-consolidation-20261105'; caps=out/'captures';caps.mkdir(exist_ok=True)
for p in (repo/'app/build/focus-consolidation').glob('*.png'):shutil.copy2(p,caps/p.name)
font='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
f=ImageFont.truetype(font,18);small=ImageFont.truetype(font,14)
pairs=[]
for profile in ['phone-dark','phone-light','tablet-inspector-dark','tablet-inspector-light']:
 for scene in ['inspector','context']:
  files=[caps/f'{profile}-{scene}-{m}.png' for m in ['A','B']]; imgs=[Image.open(p).convert('RGBA') for p in files]
  widths=[i.width for i in imgs]; heights=[i.height for i in imgs]
  w=sum(widths)+56;h=max(heights)+180
  sheet=Image.new('RGB',(w,h),'#ececea');d=ImageDraw.Draw(sheet)
  d.text((18,14),'A · actual / B · propuesta Compose aislada',font=f,fill='#171a1e')
  d.text((18,42),'Mismos datos, ancho, tema y escala. No es una captura física.',font=small,fill='#45484d')
  left=18
  for idx,img in enumerate(imgs):
   d.text((left,70),['A · Preview64 (componente)','B · propuesta, no incluida en APK'][idx],font=small,fill='#171a1e')
   sheet.paste(img,(left,98),img);left+=img.width+20
  text='B: búsqueda visible; información y favoritos plegables. 382 → 360 dp.' if scene=='inspector' else 'B: app elegida y sitio web inequívocos. Altura conservada: 176 dp.'
  d.text((18,h-58),text,font=small,fill='#171a1e')
  d.text((18,h-32),'Medidas del fragmento de prueba; no certifican WindowManager ni Gboard.',font=small,fill='#45484d')
  name=f'{profile}-{scene}-AB.png';sheet.save(out/name)
  pairs.append({'profile':profile,'scene':scene,'A':files[0].name,'B':files[1].name,'sheet':name,'A_width':widths[0],'B_width':widths[1],'A_height':heights[0],'B_height':heights[1]})
# Base screenshots are untouched archived renders whose product code is identical to Preview64.
base=repo/'docs/verification/focus-integrated-20261105'
baselines=[('Inicio','panel/phone-dark-focus-home.png'),('Resultados','content/phone-light-search.png'),('Inspector tablet','panel/tablet-dark-focus-inspector.png'),('Spotify: fixture sin audio','content/tablet-dark-spotify.png'),('WhatsApp actual','content/phone-dark-whatsapp.png'),('Apariencia','settings/phone-light-2.png')]
def data(p):return 'data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()
data_pairs=[{**p,'a':data(caps/p['A']),'b':data(caps/p['B'])} for p in pairs]
basehtml=''.join('<details><summary>'+html.escape(title)+'</summary><img class="baseline" src="'+data(base/file)+'"></details>' for title,file in baselines)
body='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Spotlight Focus · revisión desde Preview64</title><style>body{font:16px system-ui;background:#efeeeb;color:#252629;margin:0;padding:24px;max-width:1100px;margin:auto}h1{font-size:28px;margin-bottom:8px}.note{padding:16px;background:#fffdf8;border:1px solid #cfc9be;border-radius:12px;line-height:1.55}select,button{font:inherit;padding:10px;margin:16px 8px 16px 0}main{display:grid;grid-template-columns:1fr 1fr;gap:20px}article{background:#d9d8d4;padding:14px;border-radius:12px}img{display:block;max-width:100%;height:auto}h2{font-size:17px}p{line-height:1.6}.baseline{max-height:700px}details{margin:12px 0;padding:12px;border:1px solid #cfc9be;border-radius:8px}summary{cursor:pointer;font-weight:600}small{display:block;color:#555;margin:12px 0}@media(max-width:650px){main{grid-template-columns:1fr}body{padding:14px}}</style><h1>Focus: más útil, sin empezar de nuevo</h1><p class="note"><b>Propuestas para decidir, no una nueva APK.</b> A usa componentes de la implementación de Preview64. B es una composición de prueba aislada. Capturas reales Compose/Robolectric API33, escala1. Datos ficticios equivalentes e icono Android de fixture; no son Vivaldi real, Gboard ni One UI.</p><label>Perfil <select id="profile"><option value="phone-dark">Teléfono · oscuro · 360dp</option><option value="phone-light">Teléfono · claro · 360dp</option><option value="tablet-inspector-dark">Fragmento inspector tablet · oscuro · 470dp</option><option value="tablet-inspector-light">Fragmento inspector tablet · claro · 470dp</option></select></label><label>Recorrido <select id="scene"><option value="inspector">Encontrar qué puedo hacer</option><option value="context">Saber dónde se abrirá la búsqueda</option></select></label><p id="explanation"></p><main><article><h2>A · actual</h2><img id="a"><small id="am"></small></article><article><h2>B · propuesta no adoptada</h2><img id="b"><small id="bm"></small></article></main><p class="note"><b>Recomendación:</b> conservar barra, Geist, paletas, iconografía y ejecutores. Dar prioridad a la búsqueda disponible y separar app de sitio web. Mantener información/favoritos accesibles y la vuelta a la interfaz anterior. La adopción requiere tu decisión y una integración posterior con disponibilidad real, deduplicación y pruebas de altura reducida; estas vistas no activan capacidades.</p><h2>ActionPanel: componente real, no ausente</h2><p>La captura anterior de la ventana principal dejaba fuera el popup. Estas imágenes muestran el ActionPanel actual. Sus pruebas verifican Tab, Shift+Tab, Enter y Escape. No son una propuesta B ni una validación de ventanas One UI.</p><main>'''
body+=''.join('<article><img src="'+data(caps/f'actual-action-panel-{t}.png')+'"></article>' for t in ['dark','light'])
body+='</main><h2>Contexto visual existente</h2><p>Renders E1–E5 archivados, con código de producto equivalente a Preview64. Ajustes aún muestra el número63 de su fecha de captura; no se lo retocó. Spotify es una sesión sintética sin audio. No son fotos de la APK instalada.</p>'+basehtml
body+='''<h2>Qué cambió de verdad</h2><p>Únicamente se corrigió un defecto reproducido: al elegir otro contacto homónimo, la confirmación anterior se invalida y se exige revisar otra vez. La corrección es fuente para una futura entrega; no está en tu APK64 instalada. Ningún mensaje real fue enviado.</p><script>const profile=document.getElementById('profile'),scene=document.getElementById('scene'),a=document.getElementById('a'),b=document.getElementById('b'),am=document.getElementById('am'),bm=document.getElementById('bm'),explanation=document.getElementById('explanation');const pairs='''+json.dumps(data_pairs)+''';function update(){const x=pairs.find(x=>x.profile===profile.value&&x.scene===scene.value);a.src=x.a;b.src=x.b;am.textContent=x.A_width+' × '+x.A_height+' dp';bm.textContent=x.B_width+' × '+x.B_height+' dp';explanation.textContent=scene.value==='inspector'?'B hace visible Buscar en Vivaldi y pliega la administración: 22dp menos en este fragmento. El coste es un toque para información/favoritos.':'B distingue la aplicación elegida del sitio web. Mismo borrador, mismo plan de acción y mismo tamaño; no cambia la ruta ni elimina la revisión.';}profile.onchange=update;scene.onchange=update;update();</script></html>'''
(out/'comparativas.html').write_text(body)
# A small PDF with representative sheets; no image generation or retouching.
pdf=canvas.Canvas(str(out/'Revision-Focus-Preview64.pdf'),pagesize=landscape(A4));W,H=landscape(A4)
for name,title in [('phone-dark-inspector-AB.png','1. Accion util antes que administracion'),('phone-light-context-AB.png','2. App elegida y sitio web separados'),('tablet-inspector-light-inspector-AB.png','3. Misma propuesta en ancho de inspector tablet')]:
 im=Image.open(out/name);scale=min((W-40)/im.width,(H-65)/im.height)
 pdf.setFont('Helvetica-Bold',15);pdf.drawString(20,H-24,title)
 pdf.drawImage(str(out/name),(W-im.width*scale)/2,24,width=im.width*scale,height=im.height*scale)
 pdf.showPage()
pdf.setFont('Helvetica-Bold',15);pdf.drawString(20,H-24,'4. ActionPanel actual: evidencia del componente correcto')
for i,t in enumerate(['dark','light']):
 p=caps/f'actual-action-panel-{t}.png';im=Image.open(p);scale=min((W/2-40)/im.width,(H-100)/im.height)
 pdf.drawImage(str(p),20+i*W/2,H-60-im.height*scale,width=im.width*scale,height=im.height*scale)
pdf.setFont('Helvetica',10);pdf.drawString(20,28,'Compose/Robolectric; sin dispositivo real. Propuestas B no implementadas en la APK64.');pdf.save()
(out/'pairs.json').write_text(json.dumps(pairs,indent=2)+'\n')
(out/'captures-sha256.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in caps.glob('*.png')},indent=2)+'\n')
shared=Path('/workspace/shared/downloads/focus-review64');shared.mkdir(exist_ok=True)
for name in ['comparativas.html','Revision-Focus-Preview64.pdf','phone-dark-inspector-AB.png','phone-light-context-AB.png','tablet-inspector-light-inspector-AB.png']:shutil.copy2(out/name,shared/name)
print('18 originales Compose, 8 laminas A/B, HTML autocontenido y PDF de4paginas preparados.')
