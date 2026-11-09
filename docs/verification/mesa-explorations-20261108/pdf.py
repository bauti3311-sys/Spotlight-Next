from pathlib import Path
import json,textwrap
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import os
import os
ROOT=Path(__file__).resolve().parents[3];OUT=Path(os.environ.get('SPOTLIGHT_MESA_OUT',str(ROOT/'build/mesa-review')));RAW=OUT/'Originales-Compose';D=json.loads((OUT/'conceptos.json').read_text())
pdfmetrics.registerFont(TTFont('D','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'));pdfmetrics.registerFont(TTFont('DB','/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))
W,H=842,595;c=canvas.Canvas(str(OUT/'Mesa-y-mas.pdf'),pagesize=(W,H));c.setTitle('Mesa y cinco alternativas · laboratorio Compose')
def text(x,y,s,size=10,bold=False,width=100,color='#1E2B3B'):
 c.setFillColor(HexColor(color));c.setFont('DB' if bold else 'D',size)
 for line in textwrap.wrap(s,width=width):c.drawString(x,y,line);y-=size*1.4
 return y
def base(title,subtitle):
 c.setFillColor(HexColor('#F3F5F7'));c.rect(0,0,W,H,fill=1,stroke=0);text(25,561,title,21,True,width=65);text(25,537,subtitle,9,width=128,color='#526274')
 text(25,13,'Laboratorio Compose/API33 · datos ficticios · no APK · sin efectos externos · cinco estados completos en la galería HTML',8,width=135,color='#526274')
base('Mesa y cinco caminos nuevos.','Mismo documento de ejemplo: tres zonas / escenario / índice / dos superficies / tablero / bandas.')
c.drawImage(str(OUT/'Comparacion-tablet.jpg'),25,55,width=790,height=441,preserveAspectRatio=True,anchor='c');c.showPage()
base('Seis adaptaciones al teléfono','No son miniaturas de un escritorio ni capturas del G86 real: perfiles de laboratorio 412×860dp.')
c.drawImage(str(OUT/'Comparacion-phone.jpg'),470,35,width=330,height=485,preserveAspectRatio=True,anchor='c')
y=485
for p in D['concepts']:
 y=text(28,y,p['id']+' · '+p['name'],14,True,width=45);y=text(28,y-4,p['phone'],11,width=56)-19
text(28,70,'Para comparar primero contra Mesa: Puente y Atril. Ninguna propuesta adoptada.',11,True,width=54);c.showPage()
for p in D['concepts']:
 base(p['id']+' · '+p['name'],p['tag'])
 for state,x in [('resultados',25),('contexto',445)]:
  text(x,515,'Búsqueda' if state=='resultados' else 'Vista contextual',10,True)
  c.drawImage(str(RAW/f'tablet-{p["id"]}-{state}.png'),x,249,width=370,height=247,preserveAspectRatio=True,anchor='c')
 for state,x in [('resultados',25),('contexto',132)]:c.drawImage(str(RAW/f'phone-{p["id"]}-{state}.png'),x,30,width=90,height=188,preserveAspectRatio=True,anchor='c')
 y=text(252,217,'Lo que cambia de verdad',12,True);y=text(252,y-6,p['form'],10,width=86)
 y=text(252,y-13,'A favor: '+p['pros'][0],10,True,width=82);y=text(252,y-8,'A considerar: '+p['cons'][0],10,width=86)
 text(252,70,p['risk'],9,width=98,color='#6B553E');text(252,36,'Inicio, búsqueda, selección, acciones e inspector: ampliar en HTML u originales.',8,width=114,color='#526274');c.showPage()
c.save();print('PDF summary:8 pages')
