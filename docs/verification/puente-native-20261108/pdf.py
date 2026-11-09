from pathlib import Path
from reportlab.pdfgen import canvas
O=Path(__file__).resolve().parent;c=canvas.Canvas(str(O/'Puente-Nativo.pdf'),pagesize=(1100,760))
for profile in ['tablet-light','tablet-dark','phone-light','phone-dark','compact200-light','compact200-dark']:
 c.setFont('Helvetica-Bold',22);c.drawString(25,725,'Puente ligero / '+profile+' / Compose real')
 for i,phase in enumerate(['before','after']):
  x=20+545*i;c.setFont('Helvetica',14);c.drawString(x,685,'Antes / Focus Studio' if phase=='before' else 'Despues / Puente ligero');c.drawImage(str(O/f'{phase}/{profile}-inspector.png'),x,50,width=520,height=615,preserveAspectRatio=True,anchor='c')
 c.setFont('Helvetica',10);c.drawString(25,22,'Mismo fixture Android. API33/Robolectric; no prueba fisica ni APK. Validacion OneUI/Gboard pendiente.');c.showPage()
c.save()
