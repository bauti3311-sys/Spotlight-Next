from pathlib import Path
import json
from PIL import Image,ImageDraw
O=Path(__file__).resolve().parent;items=json.loads((O/'samples.json').read_text())['items'][:8]
for dev in ['tablet','phone']:
 W,H=(1600,680) if dev=='tablet' else (1200,1460);cw=W//4;ch=(H-60)//2;sheet=Image.new('RGB',(W,H),'#111923');d=ImageDraw.Draw(sheet);d.text((20,16),f'PUENTE / OCHO ACABADOS / {dev.upper()} / CAPTURAS COMPOSE',fill='white')
 for idx,it in enumerate(items):
  x=(idx%4)*cw;y=60+(idx//4)*ch;im=Image.open(O/f'originals/{dev}-{it["id"]}-dark-acciones.png').convert('RGB');im.thumbnail((cw-16,ch-45));sheet.paste(im,(x+(cw-im.width)//2,y+32));d.text((x+10,y),it['id']+' '+it['name'],fill='white')
 sheet.save(O/f'Comparacion-{dev}.jpg',quality=94)
