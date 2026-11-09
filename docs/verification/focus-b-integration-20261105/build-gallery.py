from pathlib import Path
import shutil,json,hashlib,base64,csv
from PIL import Image,ImageDraw,ImageFont
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
repo=Path(__file__).resolve().parents[3];out=repo/'docs/verification/focus-b-integration-20261105'
after=out/'after';after.mkdir(exist_ok=True)
for p in (repo/'app/build/focus-b-integration/after').glob('*.png'):shutil.copy2(p,after/p.name)
full=out/'full-fixture';full.mkdir(exist_ok=True)
shutil.copy2(repo/'docs/verification/focus-integrated-20261105/panel/phone-light-focus-inspector.png',full/'before.png')
shutil.copy2(repo/'app/build/focus-integrated/phone-light-focus-inspector.png',full/'after.png')
shutil.copy2(repo/'docs/verification/focus-integrated-20261105/panel/phone-light-metrics.csv',full/'before-metrics.csv')
shutil.copy2(repo/'app/build/focus-integrated/phone-light-metrics.csv',full/'after-metrics.csv')
font='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf';f=ImageFont.truetype(font,18);small=ImageFont.truetype(font,14)
pairs=[]
for profile in ['phone-dark','phone-light','tablet-dark','tablet-light']:
 for scene in ['inspector','review']:
  pairs.append({'name':f'{profile}-{scene}','before':f'before/{profile}-{scene}.png','after':f'after/{profile}-{scene}.png','crop':354,'note':'Presupuesto de ventana330dp. API33 Compose; no Gboard real.'})
pairs.append({'name':'full-phone-light','before':'full-fixture/before.png','after':'full-fixture/after.png','crop':470,'note':'Mismo fixture integrado,360dp/claro/fontScale1/budget700:426→407dp de superficie.'})
for item in pairs:
 imgs=[Image.open(out/item[key]).convert('RGBA') for key in ['before','after']]
 assert imgs[0].size==imgs[1].size
 w=imgs[0].width;h=min(item['crop'],imgs[0].height)
 sheet=Image.new('RGB',(w*2+56,h+160),'#ececea');d=ImageDraw.Draw(sheet)
 d.text((18,12),'ANTES · fuente677d9d8 / DESPUÉS · B integrada en fuente',font=f,fill='#242629')
 d.text((18,40),'Capturas Compose reales. Sin otra APK; sin validación física.',font=small,fill='#4b4e54')
 for i,img in enumerate(imgs):
  x=18+i*(w+20);d.text((x,68),['ANTES','DESPUÉS'][i],font=small,fill='#242629')
  part=img.crop((0,0,w,h));sheet.paste(part,(x,92),part)
 d.text((18,h+108),item['note'],font=small,fill='#242629')
 d.text((18,h+133),'Recorte pareado de los originales completos, conservados con hashes.',font=small,fill='#4b4e54')
 sheet.save(out/(item['name']+'-compare.png'))
def data(p):return 'data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode()
records=[{**p,'a':data(out/p['before']),'b':data(out/p['after'])} for p in pairs]
html='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Focus B integrada · antes/después</title><style>body{font:16px system-ui;background:#eeece8;color:#252629;margin:24px auto;max-width:1150px;padding:16px}h1{font-size:28px}p{line-height:1.6}.note{background:#fffdf8;padding:16px;border:1px solid #cfc9be;border-radius:12px}select{font:inherit;padding:10px;margin:16px 0}main{display:grid;grid-template-columns:1fr 1fr;gap:20px}article{padding:12px;background:#d9d8d4;border-radius:12px}img{max-width:100%;height:auto;display:block}h2{font-size:18px}details{padding:12px;border:1px solid #cfc9be;margin:12px 0;border-radius:8px}summary{cursor:pointer}@media(max-width:650px){main{grid-template-columns:1fr}}</style><h1>Las dos B ya están integradas en fuente</h1><p class="note">Focus sigue desactivado por defecto y es reversible. Estas son capturas reales Compose/API33, con fixtures equivalentes, no fotografías de One UI. No se generó otra APK: tu Preview64 instalada conserva su fuente original. Las nuevas etiquetas no cambian la ejecución real.</p><label>Comparativa <select id="pick">'''
for i,p in enumerate(pairs):html+=f'<option value="{i}">{p["name"]}</option>'
html+='''</select></label><p id="note"></p><main><article><h2>Antes</h2><img id="a"></article><article><h2>Después</h2><img id="b"></article></main><p>Las ocho comparativas330dp se renderizaron antes de editar producción desde677d9d8 y después con el mismo test/fixtures. La escena móvil700dp reutiliza su captura E1–E5 archivada: el código visual era idéntico a677d9d8 y la escena del test conserva datos,consulta,selección,favorito,tema y tamaño. No se retocaron iconos ni textos: los iconos Android/monogramas son fixtures declarados.</p><h2>Texto Android al200%</h2><p>Validado en ambos temas y330dp. El inspector usa scroll para acceder a cada control; el footer puede abreviar su etiqueta visible y conserva texto semántico/acción. Las capturas siguientes muestran la búsqueda después de llevarla a la vista mediante scroll. No se comparan con un antes a otra escala.</p><main>'''
for theme in ['dark','light']:html+='<article><img src="'+data(after/f'phone-{theme}-font200-primary-visible.png')+'"></article>'
html+='''</main><h2>Comprobaciones</h2><p>15casos dirigidos distintos con resultado final correcto: perfiles/200%, acciones únicas, revocación, etiqueta de receptor basada en plan real, reversión, conservación de borrador/cursor y descarte de confirmación restaurada. El cambio de destinatario homónimo continúa protegido. OneUI,Gboard,TalkBack y efectos externos reales siguen pendientes.</p><script>const records='''+json.dumps(records)+''';const p=document.getElementById('pick');function update(){const x=records[+p.value];document.getElementById('a').src=x.a;document.getElementById('b').src=x.b;document.getElementById('note').textContent=x.note;}p.onchange=update;update();</script></html>'''
(out/'antes-despues.html').write_text(html)
pdf=canvas.Canvas(str(out/'Focus-B-Integrada.pdf'),pagesize=landscape(A4));W,H=landscape(A4)
for name,title in [('full-phone-light','Inspector integrado: busqueda visible, administracion plegable'),('phone-light-review','Revision: aplicacion receptora y sitio web separados'),('phone-dark-inspector','Ventana reducida330dp: contenido accesible por scroll')]:
 p=out/(name+'-compare.png');im=Image.open(p);s=min((W-32)/im.width,(H-64)/im.height)
 pdf.setFont('Helvetica-Bold',14);pdf.drawString(16,H-25,title);pdf.drawImage(str(p),(W-im.width*s)/2,20,width=im.width*s,height=im.height*s);pdf.showPage()
pdf.save()
paths=list((out/'before').glob('*.png'))+list(after.glob('*.png'))+list(full.glob('*.png'))
(out/'captures-sha256.json').write_text(json.dumps({str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in paths},indent=2)+'\n')
(out/'pairs.json').write_text(json.dumps([{k:v for k,v in p.items() if k not in ['a','b']} for p in pairs],indent=2)+'\n')
shared=Path('/workspace/shared/downloads/focus-b-integrated');shared.mkdir(exist_ok=True)
for name in ['antes-despues.html','Focus-B-Integrada.pdf','full-phone-light-compare.png','phone-light-review-compare.png','phone-dark-inspector-compare.png']:shutil.copy2(out/name,shared/name)
print(len(paths),'originales;9comparativas;HTML/PDFlistos')
