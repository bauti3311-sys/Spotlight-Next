from pathlib import Path
import json
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
O=Path(__file__).resolve().parent;D=json.loads((O/'samples.json').read_text());c=canvas.Canvas(str(O/'Puente-Acabados.pdf'),pagesize=(1000,750))
for theme in ['dark','light']:
 c.setFont('Helvetica-Bold',20);c.drawString(28,718,'Puente / 8 paletas / '+theme);c.drawImage(str(O/f'Paletas-{theme}.jpg'),28,25,width=944,height=680,preserveAspectRatio=True,anchor='c');c.showPage()
for group,title in [('type','Tipografias'),('space','Espaciados'),('material','Superficies')]:
 items=[i for i in D['items'] if i['group']==group]
 for j in range(0,len(items),2):
  c.setFont('Helvetica-Bold',20);c.drawString(28,718,'Puente / '+title+' / oscuro / acciones')
  for k,it in enumerate(items[j:j+2]):
   x=28+485*k;c.setFont('Helvetica-Bold',14);c.drawString(x,682,it['id']+' '+it['name']);c.drawImage(str(O/f'originals/tablet-{it["id"]}-dark-acciones.png'),x,330,width=460,height=320,preserveAspectRatio=True,anchor='c');c.drawImage(str(O/f'originals/phone-{it["id"]}-dark-acciones.png'),x+135,25,width=160,height=300,preserveAspectRatio=True,anchor='c')
  c.showPage()
c.setFont('Helvetica-Bold',22);c.drawString(28,700,'Como elegir');c.setFont('Helvetica',13)
for y,line in enumerate(['Cada eje cambia por separado: no es una mezcla final renderizada.','Base: C01 Grafito + T01 Geist + E02 Equilibrado + A02 Suave.','Elegir codigos, por ejemplo C05 + T02 + E02 + A01.','86 capturas nuevas + 2 referencias exactas en la galeria HTML.','Laboratorio Compose / Robolectric API 33; datos ficticios.','Sin APK, integracion productiva ni validacion fisica.','Las paletas tienen dos temas y escenas; los otros ejes, oscuro/acciones.','El PDF resume; la galeria offline permite ampliar todas las capturas.']):c.drawString(28,650-y*30,line)
c.save()
