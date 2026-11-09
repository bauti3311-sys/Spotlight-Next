#!/usr/bin/env python3
"""Package existing NATIVE captures; never draws or substitutes application UI.
Requires Pillow in the review environment only (no Android dependency).
Run after FocusIntegratedSceneTest, FocusSettingsSceneTest, FocusContentSceneTest,
and FocusAiSceneTest. Historical references remain immutable.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import shutil, hashlib, json
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/verification/focus-integrated-20261105'
OUT.mkdir(exist_ok=True)
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',16)
for source,target in [('focus-integrated','panel'),('focus-settings','settings'),('focus-content/focus','content'),('focus-files','files')]:
 dst=OUT/target;dst.mkdir(exist_ok=True)
 for p in (ROOT/'app/build'/source).glob('*'):
  if p.suffix in ['.png','.csv']:shutil.copyfile(p,dst/p.name)
pages=[]
def grid(name,title,items,columns):
 images=[Image.open(p).convert('RGB') for label,p in items]
 width=480 if images[0].width>600 else 360
 height=max(round(i.height*width/i.width) for i in images)
 rows=(len(items)+columns-1)//columns
 canvas=Image.new('RGB',(columns*width,58+rows*(height+42)), '#202124');d=ImageDraw.Draw(canvas)
 d.text((12,10),title,font=font,fill='white')
 d.text((12,32),'Compose NATIVE | fixtures explicitos | sin APK ni validacion fisica',font=font,fill='#bfc1c7')
 for n,((label,p),im) in enumerate(zip(items,images)):
  x=n%columns*width;y=58+n//columns*(height+42)
  d.text((x+8,y+8),label,font=font,fill='white')
  canvas.paste(im.resize((width,round(im.height*width/im.width)),Image.Resampling.LANCZOS),(x,y+42))
 dst=OUT/'comparisons'/f'{name}.png';dst.parent.mkdir(exist_ok=True);canvas.save(dst);pages.append(canvas)
 return dst
links=[]
for profile in ['tablet-dark','tablet-light','phone-dark','phone-light']:
 for scene in ['home','inspector']:
  name=f'{profile}-{scene}'
  grid(name,f'{profile}: {scene}',[("Recuperacion actual",OUT/'panel'/f'{profile}-legacy-{scene}.png'),("Focus productivo (opt-in)",OUT/'panel'/f'{profile}-focus-{scene}.png')],2)
  links.append(f'### {name}\n\n![{name}](comparisons/{name}.png)\n')
 grid(f'{profile}-context',f'{profile}: Vivaldi / android tablet',[
  ('Preview63 archivada',ROOT/'docs/verification/focus-search-20261105'/f'{profile}-preview63-05-context-vivaldi.png'),
  ('bb7096c: referencia aislada',ROOT/'docs/verification/focus-product-finish-20261105'/f'{profile}-after-04-vivaldi-prepared.png'),
  ('Focus: panel productivo',OUT/'panel'/f'{profile}-focus-vivaldi-review.png')],3)
 links.append(f'### {profile}: contexto, referencia historica y produccion\n\n![{profile}](comparisons/{profile}-context.png)\n')
 grid(f'{profile}-settings',f'{profile}: Ajustes / Apariencia',[
  ('Recuperacion actual',OUT/'settings'/f'{profile}-legacy-appearance.png'),
  ('Focus: Activity productiva',OUT/'settings'/f'{profile}-2.png')],2)
 links.append(f'### {profile}: Ajustes\n\n![{profile}](comparisons/{profile}-settings.png)\n')
 grid(f'{profile}-content',f'{profile}: componentes existentes',[(s,OUT/'content'/f'{profile}-{s}.png') for s in ['spotify','files','quicklook-runtime-unavailable','actions','whatsapp','ai-result']],2)
 links.append(f'### {profile}: multimedia, archivos, error PDF, acciones, app, IA\n\n![{profile}](comparisons/{profile}-content.png)\n')
md='''# Galería de integración productiva Focus

Capturas de SpotlightPanel y MainActivity reales mediante Compose NATIVE/Roborazzi. No son los prototipos aislados anteriores. La preferencia productiva es reversible y aún no se activa por defecto. No se generó APK ni se usó Actions.

**Lectura de evidencia:** recuperación actual y Focus comparten revisión, contenido y dimensiones. Las columnas Preview63 y bb7096c son referencias Compose archivadas, identificadas; no se afirma que sus fixtures históricos sean idénticos en todos los metadatos. Monogramas S/V son iconos sintéticos de fixture entregados por el PackageManager simulado, no marcas reales. Spotify/session y respuesta IA son fixtures; el PDF es local autorizado y su renderizador nativo no está disponible en JVM. Los estados de error se muestran completos, sin una página falsa. La escena IME usa presupuesto330dp, no Gboard real.

settings/tablet-opacity-73.png conserva la captura de persistencia de la ronda E4 anterior al ajuste final de contraste; no es una comparación visual final.

La comparación de nueva geometría se presenta antes de adoptar Focus por defecto; la interfaz anterior y todos los checkpoints siguen disponibles.

'''+ '\n'.join(links)
for area in ['panel','settings','content','files']:
 md+=f'\n## Originales: {area}\n\n'+ '\n'.join(f'- [{p.name}]({area}/{p.name})' for p in sorted((OUT/area).glob('*.png')))+'\n'
(OUT/'GALERIA.md').write_text(md)
# Export-only PDF/contact sheets: all UI pixels originate in the PNG captures above.
pages[0].save(OUT/'Comparativa-Focus.pdf',save_all=True,append_images=pages[1:],resolution=120)
manifest={str(p.relative_to(OUT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.rglob('*.png'))}
(OUT/'captures-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
print(json.dumps({'originals':sum(len(list((OUT/a).glob('*.png'))) for a in ['panel','settings','content','files']),'comparisonSheets':len(pages),'sha256Entries':len(manifest)}))
