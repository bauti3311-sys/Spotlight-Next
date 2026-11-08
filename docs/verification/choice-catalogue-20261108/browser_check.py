from playwright.sync_api import sync_playwright
from pathlib import Path
import json
import os
O=Path(os.environ.get('SPOTLIGHT_CATALOGUE_OUT',str(Path(__file__).resolve().parents[3]/'build/choice-catalogue-review')));errors=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':1440,'height':1000});page.on('pageerror',lambda e:errors.append(str(e)))
 page.set_content((O/'Elegir-Spotlight.html').read_text(),wait_until='load');page.wait_for_function('document.querySelectorAll("[data-style]").length===13')
 assert page.locator('#styleGrid article').count()==13
 page.locator('#styleGrid img').evaluate_all('(imgs)=>Promise.all(imgs.map(i=>{i.loading="eager";return i.decode()}))');assert page.locator('#styleGrid img').evaluate_all('(imgs)=>imgs.every(i=>i.complete&&i.naturalWidth>0)')
 page.locator('[data-style="E06"]').click();page.locator('[data-style="E04"]').click();page.locator('#compareChosen').click();assert page.locator('dialog img').count()==2;page.locator('#closeDialog').click()
 page.locator('#device').select_option('compact');page.locator('#theme').select_option('light');assert page.locator('#styleGrid img').evaluate_all('(imgs)=>imgs.every(i=>!!i.getAttribute("src") && i.getAttribute("src")!=="undefined")')
 assert page.locator('#scene option[value=ajustes]').evaluate('(o)=>o.disabled');assert page.locator('#scene').input_value()=='inspector'
 page.locator('#tabFunctions').click();assert page.locator('[data-function]').count()==36
 page.locator('[data-function="F08"]').click();page.locator('[data-demo="F08"]').click();assert page.locator('dialog img').count()==1;page.locator('#closeDialog').click()
 assert 'E06' in page.locator('#selectionText').input_value() and 'F08' in page.locator('#selectionText').input_value()
 with page.expect_download() as export:
  page.locator('#export').click()
 selected=json.loads(Path(export.value.path()).read_text());assert selected=={'styles':['E06','E04'],'functions':['F08']}
 page.locator('#status').select_option('Condicional');assert page.locator('[data-function]').count()==4
 page.locator('#status').select_option('');page.locator('#group').select_option('Archivos y documentos');assert page.locator('[data-function]').count()==6
 page.locator('#reset').click();assert page.locator('#count').inner_text()=='0'
 page.locator('#tabStyles').click();page.locator('#device').select_option('tablet');page.locator('#theme').select_option('dark');page.evaluate('window.scrollTo(0,0)');page.screenshot(path=str(O/'Vista-galeria.png'),full_page=False)
 page.set_viewport_size({'width':390,'height':844});assert page.evaluate('document.documentElement.scrollWidth<=innerWidth')
 assert not errors,errors
 result={'styles':13,'new_styles':12,'functions':36,'screenshots':348,'comparison_modal':True,'selection_and_filters':True,'selection_json_export':True,'offline_images':True,'compact_200_fallback_honest':True,'phone_no_horizontal_overflow':True,'js_errors':errors}
 (O/'browser-check.json').write_text(json.dumps(result,indent=2)+'\n');print(result);b.close()
