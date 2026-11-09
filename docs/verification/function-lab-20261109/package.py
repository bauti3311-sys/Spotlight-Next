"""Publish review artifacts from original, labelled browser screenshots."""
from pathlib import Path
from PIL import Image,ImageOps,ImageDraw,ImageFont
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.utils import ImageReader
import json,textwrap,hashlib,zipfile,shutil
O=Path(__file__).resolve().parent; R=O.parents[2]
D=json.loads((O/'funciones.json').read_text()); fs=D['functions']
font=R/'app/src/main/res/font/geist_regular.ttf';semi=R/'app/src/main/res/font/geist_semibold.ttf'
pdfmetrics.registerFont(TTFont('Geist',str(font)));pdfmetrics.registerFont(TTFont('GeistBold',str(semi)))
W,H=A4; pdf=canvas.Canvas(str(O/'Funciones-para-elegir.pdf'),pagesize=A4)
pdf.setTitle('Spotlight Next · 36 funciones para elegir · Laboratorio')
def text(x,y,s,size=10,bold=False,color='#193c4b'):
 pdf.setFillColor(color);pdf.setFont('GeistBold' if bold else 'Geist',size);pdf.drawString(x,y,s)
def para(x,y,s,width=88,size=9):
 for line in textwrap.wrap(s,width):text(x,y,line,size);y-=size*1.4
 return y
def page(title,sub):
 pdf.setFillColor('#e5eef0');pdf.rect(0,0,W,H,fill=1,stroke=0)
 text(36,H-35,'SPOTLIGHT NEXT / LABORATORIO FUNCIONAL',9,True)
 text(36,H-77,title,25,True);para(36,H-102,sub,90,9)
 text(36,25,'Sin acciones Android · datos ficticios · no nuevas funciones instaladas',8)
def pic(name,x,y,w,h):
 im=Image.open(O/'originales'/name);iw,ih=im.size;s=min(w/iw,h/ih);pdf.drawImage(ImageReader(im),x,y+h-ih*s,width=iw*s,height=ih*s)
page('Qué vale la pena sumar','36 ideas conservan los IDs del catálogo anterior. Elegir no implica implementar. Puente ligero se mantiene; acá decidimos qué tareas resolver.')
pic('01-catalogo-tablet.png',36,300,W-72,390)
y=260
for s in ['Mi primera selección: F08 comparar textos, F12 limpiar enlaces, F34 colecciones y F33 búsquedas por proyecto.','También recomiendo F01 historial de cuentas, F02 conversores favoritos, F07 texto y F36 diagnóstico de disponibilidad.','Las demos de cálculo y texto funcionan localmente en el navegador. Las de Android ilustran recorridos con fixtures; no acceden a tu equipo.','C3/C4 y C5 conservan sus bloqueos. No se reparó Spotify ni se añadieron permisos, modelos, servicios o APK.']:
 y=para(36,y,s,89,10)-13
pdf.showPage()
for group in dict.fromkeys(x['group'] for x in fs):
 page(group,'Base existente y ampliación propuesta se distinguen. Esfuerzo relativo: no es una estimación de calendario ni prueba de funcionamiento físico.')
 y=H-145
 for x in [x for x in fs if x['group']==group]:
  text(36,y,x['id']+' · '+x['title'],13,True);y-=19
  y=para(36,y,x['value'],99,9)
  y=para(36,y,'Base / delta: '+x['base'],105,8)
  y=para(36,y,'Límite: '+x['permission'],105,8)
  text(36,y,x['status']+' · esfuerzo '+x['effort'].lower()+(' · PAUSADA / CONDICIONAL' if x['paused'] else ''),8,True);y-=25
 pdf.showPage()
for title,sub,names in [
 ('Probá resultados, no promesas','Capturas originales Chromium de laboratorio. El cálculo y la comparación funcionan sobre entradas editables.', ['F08-tablet.png','F12-tablet.png']),
 ('Antes de actuar, revisar','Los recibos son ficticios. Una solicitud a Android nunca equivale por sí sola a un efecto externo completado.', ['F25-tablet.png','F26-tablet.png']),
 ('Organizar sin aumentar el ruido','Colecciones es una propuesta nueva. Los recursos siempre se elegirían de uno en uno; sin aperturas masivas.', ['F34-tablet.png','F34-tablet-oscuro.png'])]:
 page(title,sub)
 pic(names[0],36,405,W-72,295);pic(names[1],36,80,W-72,295)
 pdf.showPage()
page('Cómo elegir','Abrí Elegir-funciones.html sin conexión. La selección vive en memoria hasta cerrar o actualizar; exportala para conservarla.')
y=H-155
for s in ['1. Mirá las 36 o empezá por las 8 recomendadas.','2. Abrí «Probar recorrido»: cambiá entradas o recorré la revisión ficticia.','3. Consultá qué existe, el coste, privacidad y condiciones pendientes.','4. Tocá «Elegir», ordená prioridades y descargá Mi-seleccion-Spotlight.json.','5. Mandá ese archivo o los códigos F que preferís. La implementación se decidirá por etapas.']:
 y=para(36,y,s,83,12)-22
y=para(36,y-10,'Validación: 211 comprobaciones de navegador; tablet, teléfono, 330 px y desborde con texto ampliado. No prueban Compose, Android, Gboard, SAF, OCR, MediaSession ni una acción externa.',90,10)-20
para(36,y,'Viabilidad: reutilizar los motores y contratos indicados en funciones.json. Las propuestas son extensiones posibles, no un compromiso de que las 36 estén listas para producción. C5 no está en Preview70.',90,10)
pdf.save()
# Overview from six unmodified browser captures, only resized and captioned externally.
names=['F08','F12','F21','F26','F34','F36'];sheet=Image.new('RGB',(1500,1980),'#e5eef0');draw=ImageDraw.Draw(sheet);big=ImageFont.truetype(str(semi),36);small=ImageFont.truetype(str(font),22)
draw.text((32,25),'Spotlight · Funciones para elegir',font=big,fill='#193c4b');draw.text((32,80),'6 ejemplos / 36 opciones interactivas · laboratorio web',font=small,fill='#546f7b')
for n,id in enumerate(names):
 x=30+(n%2)*740;y=145+(n//2)*600
 im=Image.open(O/'originales'/(id+'-tablet.png')).convert('RGB');im.thumbnail((710,545));sheet.paste(im,(x,y));t=next(f['title'] for f in fs if f['id']==id);draw.text((x,y+550),id+' · '+t,font=small,fill='#193c4b')
sheet.save(O/'Seis-ejemplos.jpg',quality=92)
# The deliverable is standalone; original files remain in repo too.
checks=json.loads((O/'checks.json').read_text());assert checks['passed']==checks['total']==211
files=[p for p in O.rglob('*') if p.is_file() and p.name not in ['SHA256SUMS','Spotlight-Funciones-Offline.zip'] and '__pycache__' not in p.parts]
(O/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+str(p.relative_to(O))+'\n' for p in sorted(files)))
with zipfile.ZipFile(O/'Spotlight-Funciones-Offline.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in files+[O/'SHA256SUMS']:z.write(p,p.relative_to(O))
DEST=Path('/workspace/shared/downloads/spotlight-funciones');DEST.mkdir(parents=True,exist_ok=True)
for name in ['Elegir-funciones.html','Funciones-para-elegir.pdf','Seis-ejemplos.jpg','Spotlight-Funciones-Offline.zip','SHA256SUMS']:shutil.copy2(O/name,DEST/name)
print('PDF 11 páginas, 19 PNG originales, vista general y ZIP offline publicados en '+str(DEST))
