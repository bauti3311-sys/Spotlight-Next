from pathlib import Path
import json,textwrap
from reportlab.pdfgen import canvas
O=Path(__file__).resolve().parent;D=json.loads((O/'samples.json').read_text());c=canvas.Canvas(str(O/'Puente-Extremos.pdf'),pagesize=(1000,750))
c.setFont('Helvetica-Bold',24);c.drawString(28,710,'Puente / ocho acabados sin medias tintas');c.drawImage(str(O/'Comparacion-tablet.jpg'),20,210,width=960,height=410,preserveAspectRatio=True);c.setFont('Helvetica',13);c.drawString(28,160,'Capturas reales Compose. Combinaciones completas, no integradas en la app.');c.drawString(28,132,'Mismos datos y consulta; cambian color, letra, escala, relleno, densidad y material.');c.showPage()
for it in D['items'][:8]:
 c.setFont('Helvetica-Bold',22);c.drawString(28,710,it['id']+' / '+it['name']);c.setFont('Helvetica',11)
 for i,line in enumerate(textwrap.wrap(it['note'],130)):c.drawString(28,680-i*16,line)
 c.drawImage(str(O/f'originals/tablet-{it["id"]}-dark-acciones.png'),20,170,width=700,height=470,preserveAspectRatio=True,anchor='c');c.drawImage(str(O/f'originals/phone-{it["id"]}-dark-acciones.png'),740,125,width=235,height=510,preserveAspectRatio=True,anchor='c');c.setFont('Helvetica',10);c.drawString(28,80,'Laboratorio API33 / tablet1200x800dp / telefono412x860dp. Datos ficticios.');c.showPage()
c.setFont('Helvetica-Bold',22);c.drawString(28,710,'Transparencia / solo cambia la opacidad')
for i,it in enumerate(D['items'][8:]):
 x=20+(i%2)*490;y=365-(i//2)*330;c.setFont('Helvetica-Bold',15);c.drawString(x,y+285,it['name']);c.drawImage(str(O/f'originals/tablet-{it["id"]}-dark-acciones.png'),x,y-5,width=475,height=280,preserveAspectRatio=True,anchor='c')
c.setFont('Helvetica',11);c.drawString(28,18,'Menos opacidad = mas transparencia. 60% es un extremo, no recomendacion. Sin blur.');c.save()
