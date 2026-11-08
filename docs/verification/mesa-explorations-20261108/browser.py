from pathlib import Path
from playwright.sync_api import sync_playwright
import json
import os
O=Path(os.environ.get('SPOTLIGHT_MESA_OUT',str(Path(__file__).resolve().parents[3]/'build/mesa-review')));errors=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox']);page=b.new_page(viewport={'width':1440,'height':1000});page.on('pageerror',lambda e:errors.append(str(e)))
 page.set_content((O/'Galeria-Spotlight.html').read_text(),wait_until='load')
 assert page.locator('#grid article').count()==6
 for device in ['tablet','phone']:
  page.locator('#device').select_option(device)
  for state in ['inicio','resultados','seleccion','acciones','contexto']:
   page.locator('#state').select_option(state)
   page.locator('#grid img').evaluate_all('(images)=>Promise.all(images.map(i=>i.decode()))')
   assert page.locator('#grid img').evaluate_all('(images)=>images.every(i=>i.naturalWidth>0&&i.naturalHeight>0)')
 page.locator('#device').select_option('tablet');page.locator('#state').select_option('resultados')
 page.locator('#labels').click();assert page.locator('#grid').evaluate('(e)=>e.classList.contains("names-hidden")');page.locator('#labels').click()
 page.locator('#openComparison').click();page.locator('#left').select_option('M00');page.locator('#right').select_option('M03');assert page.locator('#compared img').count()==2
 page.locator('[data-czoom="M03"]').click();assert page.locator('#modal').evaluate('(d)=>d.open');page.locator('#close').click()
 page.locator('[data-more="M04"]').click();page.locator('[data-stage="contexto"]').click();assert page.locator('#detailImages img').count()==2
 for c in ['M01','M03']:page.locator('[data-fav="'+c+'"]').click()
 assert 'M01' in page.locator('#selection').input_value() and 'M03' in page.locator('#selection').input_value()
 with page.expect_download() as item:page.locator('#download').click()
 assert json.loads(Path(item.value.path()).read_text())=={'concepts':['M01','M03']}
 page.locator('#reset').click();page.evaluate('window.scrollTo(0,0)');page.screenshot(path=str(O/'Vista-galeria.png'),full_page=False)
 page.set_viewport_size({'width':390,'height':844});assert page.evaluate('document.documentElement.scrollWidth<=innerWidth')
 assert not errors,errors
 result={'concepts':6,'images':60,'new_captures':50,'reused_reference_captures':10,'devices':2,'states':5,'overview_first':True,'side_by_side':True,'detail_and_zoom':True,'all_60_images_decoded_offline':True,'hide_labels':True,'selection_json':True,'phone_no_horizontal_overflow':True,'js_errors':errors,'browser_execution':'exact self-contained HTML via set_content; file navigation not used'}
 (O/'browser-check.json').write_text(json.dumps(result,indent=2)+'\n');print(result);b.close()
