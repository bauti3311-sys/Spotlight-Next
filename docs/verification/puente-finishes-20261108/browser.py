from pathlib import Path
import json
from playwright.sync_api import sync_playwright
O=Path(__file__).resolve().parent
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);page=b.new_page(viewport={'width':1440,'height':1000});errors=[];page.on('pageerror',lambda e:errors.append(str(e)));page.set_content((O/'Galeria-Puente.html').read_text(),wait_until='load')
 counts={}
 for group,n in [('color',8),('type',5),('space',3),('material',3)]:
  page.select_option('#group',group);assert page.locator('.card').count()==n;counts[group]=n
  assert page.locator('#theme').is_disabled()==(group!='color')
  page.click('#compare');assert page.locator('#modal img').count()==2;page.keyboard.press('Escape')
 page.select_option('#group','color');page.select_option('#theme','light');page.select_option('#scene','contexto');page.select_option('#device','phone');page.locator('[data-pick=C05]').click();assert page.locator('#summary').inner_text().startswith('C05')
 with page.expect_download() as info:page.click('#export')
 exported=json.loads(Path(info.value.path()).read_text());assert exported['chosen']['color']=='C05'
 page.click('#reference');assert page.locator('#modal img').get_attribute('src').startswith('data:image/png');page.keyboard.press('Escape')
 decoded=page.evaluate('''async()=>{let images=Object.values(D.images);await Promise.all(images.map(src=>{let i=new Image();i.src=src;return i.decode()}));return images.length}''');assert decoded==88
 page.set_viewport_size({'width':390,'height':844});assert page.evaluate('document.documentElement.scrollWidth<=innerWidth');assert not errors
 (O/'browser-check.json').write_text(json.dumps({'counts':counts,'decoded_images':decoded,'same_axis_comparison':True,'unsupported_controls_disabled':True,'reference_zoom':True,'export':exported,'width390_no_overflow':True,'js_errors':errors,'loading':'set_content of exact self-contained HTML; file navigation not tested'},indent=2))
 b.close()
print('Browser checks passed; 88 images decoded.')
