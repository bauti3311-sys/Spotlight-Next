from pathlib import Path
import json
from playwright.sync_api import sync_playwright
O=Path(__file__).resolve().parent
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);page=b.new_page(viewport={'width':1440,'height':1000});errors=[];page.on('pageerror',lambda e:errors.append(str(e)));page.set_content((O/'Galeria-Puente-Nativo.html').read_text(),wait_until='load')
 for profile in ['tablet-light','tablet-dark','phone-light','phone-dark','compact200-light','compact200-dark']:
  page.select_option('#profile',profile)
  for scene in ['resultados','inspector']:
   page.select_option('#scene',scene);assert page.locator('#pair img').count()==2
 for dev in ['tablet','phone']:
  for theme in ['light','dark']:
   page.select_option('#webprofile',dev);page.select_option('#webtheme',theme);assert page.locator('#reference img').count()==2
 page.locator('#pair img').first.click();assert page.locator('#modal').is_visible();page.keyboard.press('Escape');assert not page.locator('#modal').is_visible()
 count=page.evaluate('''async()=>{let v=Object.values(D.images);await Promise.all(v.map(src=>{let i=new Image();i.src=src;return i.decode()}));return v.length}''');assert count==28
 page.set_viewport_size({'width':390,'height':844});assert page.evaluate('document.documentElement.scrollWidth<=innerWidth');assert not errors
 (O/'browser-check.json').write_text(json.dumps({'images_decoded':count,'profiles':6,'scenes':2,'web_reference_comparisons':4,'zoom_escape':True,'390px_no_overflow':True,'errors':errors,'navigation':'set_content of exact self-contained HTML; file navigation not tested'},indent=2));b.close()
print('28 images, before/after and reference controls, zoom and narrow gallery passed.')
