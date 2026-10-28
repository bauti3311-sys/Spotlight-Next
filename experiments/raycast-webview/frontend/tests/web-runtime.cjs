const {chromium} = require('playwright');
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
(async () => {
 const browser = await chromium.launch({headless:true,executablePath:process.env.SPOTLIGHT_CHROMIUM || chromium.executablePath(),args:['--no-sandbox']});
 const page = await browser.newPage({viewport:{width:720,height:464}});
 const errors=[]; const requests=[]; const checks=[];
 page.on('pageerror', e=>errors.push(e.message));
 await page.route('**/*', async route=>{
  const url = new URL(route.request().url()); requests.push(url.href);
  if (url.origin !== 'https://spotlight.local') return route.abort();
  const file=path.join(process.cwd(),'.output/public',url.pathname==='/'?'index.html':decodeURIComponent(url.pathname));
  if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) return route.fulfill({status:404,body:''});
  const mime={'.js':'application/javascript','.css':'text/css','.html':'text/html','.json':'application/json','.png':'image/png','.svg':'image/svg+xml'};
  await route.fulfill({path:file,contentType:mime[path.extname(file)]||'application/octet-stream'});
 });
 await page.goto('https://spotlight.local/');
 await page.getByPlaceholder('Search for apps...').waitFor();
 assert.equal(await page.getByRole('option').count(),0); checks.push('production has no simulated results without native host');
 const syntheticIcon='data:image/svg+xml,'+encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"><rect width="20" height="20" rx="4" fill="#ef7034"/></svg>');
 await page.evaluate(icon=>{
  window.testRequests=[];
  const c = new MessageChannel();
  c.port1.onmessage = event=>{
   const m=JSON.parse(event.data); window.testRequests.push(m);
   if(m.op==='search') {
    const rows=[{id:'test-alpha',label:'Alpha',suffix:'',type:'Application',icon},{id:'test-beta',label:'Beta',suffix:'',type:'Application',icon}].filter(r=>r.label.toLowerCase().includes(m.query.toLowerCase()));
    c.port1.postMessage(JSON.stringify({v:1,id:m.id,rows}));
   }
  };
  window.postMessage('spotlight-native-v1',location.origin,[c.port2]);
 }, syntheticIcon);
 await page.getByRole('option').nth(1).waitFor();
 assert.equal(await page.getByRole('option').count(),2); checks.push('native result packet renders through original command palette');
 const input=page.getByPlaceholder('Search for apps...');
 await input.press('ArrowDown'); await input.press('ArrowDown'); await input.press('Enter');
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.op==='execute').length===1);
 let executed = await page.evaluate(()=>window.testRequests.filter(m=>m.op==='execute'));
 assert.equal(executed.length,1); assert.ok(['test-alpha','test-beta'].includes(executed[0].target)); checks.push('arrows and Enter produce one allowlisted launch message');
 await page.getByRole('option').filter({hasText:'Beta'}).click();
 executed = await page.evaluate(()=>window.testRequests.filter(m=>m.op==='execute'));
 assert.equal(executed.length,1); checks.push('touch-style selection does not launch an application');
 await page.getByRole('button',{name:/Actions/}).click();
 await page.getByRole('menuitem',{name:'Application Info'}).click();
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.op==='execute').length===2);
 executed=await page.evaluate(()=>window.testRequests.filter(m=>m.op==='execute'));
 assert.equal(executed.at(-1).action,'info'); assert.equal(executed.at(-1).target,'test-beta'); checks.push('ActionPanel routes Application Info to selected result');
 await input.fill('Alpha'); await page.getByRole('option').filter({hasText:'Alpha'}).waitFor();
 assert.equal(await page.getByRole('option').count(),1); checks.push('typing sends native query and replaces results');
 await input.press('Control+k'); await page.getByRole('menu').waitFor(); await input.press('Escape');
 await page.getByRole('menu').waitFor({state:'detached'});
 assert.equal(await page.getByRole('menu').count(),0);
 assert.equal(await page.evaluate(()=>window.testRequests.filter(m=>m.op==='close').length),0); checks.push('Escape dismisses ActionPanel before closing overlay');
 await input.press('Escape');
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.op==='close').length===1);
 assert.equal(await page.evaluate(()=>window.testRequests.filter(m=>m.op==='close').length),1); checks.push('Escape sends close request');
 assert.equal(errors.length,0,errors.join('\n'));
 assert.ok(requests.every(r=>r.startsWith('https://spotlight.local/'))); checks.push('all observed runtime resources use the local origin');
 await page.screenshot({path:'evidence/web-runtime-tablet.png'});
 await page.setViewportSize({width:390,height:464});
 await page.screenshot({path:'evidence/web-runtime-phone.png'});
 const report={environment:'Chromium browser with test-only MessagePort adapter; NOT Android WebView or installed apps',checks,errors,requests};
 fs.writeFileSync('evidence/web-runtime.json',JSON.stringify(report,null,2));
 console.log(JSON.stringify(report,null,2));
 await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
