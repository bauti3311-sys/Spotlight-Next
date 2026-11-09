from pathlib import Path
import json
from playwright.sync_api import sync_playwright
O=Path(__file__).resolve().parent
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);page=b.new_page(viewport={'width':1440,'height':1000});errors=[];requests=[]
 page.on('pageerror',lambda e:errors.append(str(e)));page.route('**/*',lambda r:(requests.append(r.request.url),r.abort()))
 page.set_content((O/'Antes-despues-Ajustes.html').read_text());assert page.locator('[data-scene]').count()==9
 for i in range(9):
  page.locator('[data-scene="'+str(i)+'"]').click()
  page.wait_for_function('[...document.images].every(i=>i.complete&&i.naturalWidth>0)')
  assert page.locator('button[aria-pressed=true]').count()==1
 page.set_viewport_size({'width':390,'height':844});assert page.evaluate('document.documentElement.scrollWidth<=innerWidth');assert not errors and not requests
 b.close()
(O/'gallery-checks.json').write_text(json.dumps({'comparisons':9,'all_original_images_decoded':True,'phone_no_horizontal_overflow':True,'javascript_errors':errors,'network_requests':requests,'loading':'set_content; file navigation not independently tested'},indent=2)+'\n')
print('Galería:9 pares nativos, imágenes y adaptación correctos; sin red.')
