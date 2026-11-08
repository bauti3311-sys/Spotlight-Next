from pathlib import Path
import json,textwrap,math
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from PIL import Image,ImageDraw,ImageFont
import os
ROOT=Path(__file__).resolve().parents[3];R=Path(__file__).resolve().parent/'screenshots';O=Path(os.environ.get('SPOTLIGHT_CATALOGUE_OUT',str(ROOT/'build/choice-catalogue-review')));data=json.loads((O/'catalogo.json').read_text());S=data['styles'];F=data['functions'];W,H=842,595
pdfmetrics.registerFont(TTFont('D','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
pdfmetrics.registerFont(TTFont('DB','/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))
c=canvas.Canvas(str(O/'Catalogo-Spotlight.pdf'),pagesize=(W,H));c.setTitle('Spotlight · 12 estéticas y 36 funciones para elegir')
def text(x,y,s,size=11,bold=False,color='#1C2635',width=100):
 c.setFillColor(HexColor(color));c.setFont('DB' if bold else 'D',size)
 for l in textwrap.wrap(s,width=width):c.drawString(x,y,l);y-=size*1.45
 return y
def base(title,sub):
 c.setFillColor(HexColor('#F4F5F7'));c.rect(0,0,W,H,fill=1,stroke=0)
 text(28,H-35,title,21,True,width=65);text(28,H-58,sub,9,color='#526171',width=125)
 text(28,13,'Prototipos Compose reales · laboratorio API33 · datos ficticios · no APK instalada · no funciones activadas',8,color='#526171',width=135)
base('Elegí tu Spotlight','12 direcciones estéticas, referencia actual y 36 mejoras funcionales viables. Ninguna decisión se aplica automáticamente.')
y=H-110
for s in S:
 text(30,y,s['id']+' · '+s['name'],12,True);text(260,y,s['tag'],11);y-=29
text(30,70,'Abrí Elegir-Spotlight.html para ampliar, cambiar escenas/temas y marcar favoritos.',11,True)
text(30,45,'Ejemplo de respuesta: “E01 barra, E09 materiales, E08 inspector; F02, F08 y F21”.',10)
c.showPage()
for s in S:
 base(s['id']+' · '+s['name'],s['tag']+' · comparación equivalente de tablet y teléfono; acabados propuestos, no implementados en producto.')
 for theme,x in [('dark',28),('light',444)]:
  text(x,H-84,'Oscuro' if theme=='dark' else 'Claro',10,True)
  c.drawImage(str(R/f'tablet-{theme}-inspector-{s["direction"]}.png'),x,231,width=370,height=266,preserveAspectRatio=True,anchor='c',mask='auto')
 for theme,x in [('dark',28),('light',135)]:
  c.drawImage(str(R/f'phone-{theme}-resultados-{s["direction"]}.png'),x,33,width=95,height=185,preserveAspectRatio=True,anchor='c',mask='auto')
 text(254,200,'Qué aporta',12,True);text(254,175,s['benefit'],11,width=76)
 text(254,127,'Qué puede incomodarte',12,True);text(254,103,s['tradeoff'],11,width=76)
 text(254,42,'Galería HTML: inicio, resultados, inspector, acciones, Ajustes y texto 200%.',9,color='#526171',width=98)
 c.showPage()
for group in list(dict.fromkeys(f['group'] for f in F)):
 base(group,'Opciones para elegir. Ejemplos visuales Compose completos en la galería HTML; no son capacidades ejecutadas.')
 for idx,f in enumerate(x for x in F if x['group']==group):
  x=28+(idx%2)*407;y=H-88-(idx//2)*164
  c.setFillColor(HexColor('#FFFFFF'));c.roundRect(x,y-149,385,149,10,fill=1,stroke=0)
  text(x+12,y-19,f['id']+' · '+f['title'],11,True,width=48)
  text(x+12,y-49,f['example'],10,color='#345781',width=56)
  text(x+12,y-72,f['value'],9,width=62)
  text(x+12,y-104,f['limit'],8,color='#765232',width=72)
  text(x+12,y-139,f['status']+' · esfuerzo '+f['effort'],8,True,color='#526171',width=65)
 c.showPage()
c.save()
# Contact sheet: unaltered source captures scaled onto a labelled review page.
font='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf';F1=ImageFont.truetype(font,23);F2=ImageFont.truetype(font,15)
CW,CH=440,375;sheet=Image.new('RGB',(CW*3,CH*4+75),'#101216');dr=ImageDraw.Draw(sheet);dr.text((18,14),'12 estilos · capturas de prototipos Compose',font=F1,fill='white');dr.text((18,46),'Temas variados en este resumen. Comparación equivalente de claro/oscuro dentro de la galería.',font=F2,fill='#bac6d5')
for i,s in enumerate(S[1:]):
 x=(i%3)*CW;y=(i//3)*CH+75
 theme='light' if s['id'] in ['E02','E04','E05','E12'] else 'dark'
 im=Image.open(R/f'tablet-{theme}-inspector-{s["direction"]}.png').convert('RGB');im.thumbnail((CW-16,300));sheet.paste(im,(x+8,y+8));dr.text((x+12,y+311),s['id']+' · '+s['name'],font=F1,fill='white');dr.text((x+12,y+343),s['tag']+(' · claro' if theme=='light' else ' · oscuro'),font=F2,fill='#bac6d5')
sheet.save(O/'Muestrario-esteticas.jpg',quality=92)
print('PDF pages:',1+len(S)+6)
