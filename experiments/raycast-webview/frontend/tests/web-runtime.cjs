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
 await page.evaluate(()=>{
  window.foreignRequests=[];const c=new MessageChannel();
  c.port1.onmessage=e=>window.foreignRequests.push(e.data);
  window.dispatchEvent(new MessageEvent('message',{origin:'https://foreign.example',source:window,
   data:JSON.stringify({v:2,type:'bridge.ready',sessionId:'foreign-handshake'}),ports:[c.port2]}));
 });
 await page.waitForTimeout(50);
 assert.equal(await page.evaluate(()=>window.foreignRequests.length),0);
 checks.push('foreign-origin handshake cannot acquire the native client');
 const syntheticIcon='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a6WQAAAAASUVORK5CYII=';
 await page.evaluate(icon=>{
  window.testRequests=[]; window.testPort = undefined; window.testIcon=icon;
  const c = new MessageChannel();
  c.port1.onmessage = event=>{
   const m=JSON.parse(event.data); window.testRequests.push(m);
   if(m.type==='search.query') {
    const rows=[{id:'test-alpha',label:'Alpha',suffix:'',type:'Application',icon},{id:'test-beta',label:'Beta',suffix:'',type:'Application',icon}].filter(r=>r.label.toLowerCase().includes(m.payload.text.toLowerCase()));
    c.port1.postMessage(JSON.stringify({v:2,sessionId:m.sessionId,requestId:m.requestId,type:m.type,status:'ok',data:{items:rows}}));
   } else c.port1.postMessage(JSON.stringify({v:2,sessionId:m.sessionId,requestId:m.requestId,type:m.type,status:'ok',data:m.type==='result.inspect'?{selected:m.payload.target}:m.type==='action.execute'?{state:'requested'}:{}}));
  };
  window.testPort=c.port1;
  window.postMessage(JSON.stringify({v:2,type:'bridge.ready',sessionId:'test-session'}),location.origin,[c.port2]);
 }, syntheticIcon);
 await page.getByRole('option').nth(1).waitFor();
 fs.writeFileSync('/tmp/spotlight-prototype-metrics.json',JSON.stringify(await page.evaluate(()=>Object.fromEntries(['[role=dialog]','input','[role=option]','kbd'].map(sel=>{const el=document.querySelector(sel);const s=el&&getComputedStyle(el);return[sel,s&&Object.fromEntries(['backgroundColor','color','borderColor','borderWidth','borderRadius','height','fontFamily','fontSize','fontWeight','lineHeight','padding','boxShadow','backdropFilter'].map(k=>[k,s[k]]))]}))),null,2));
 assert.equal(await page.getByRole('option').count(),2); checks.push('native result packet renders through original command palette');
 await page.screenshot({path:'evidence/contract-matched-tablet.png',animations:'disabled'});
 await page.setViewportSize({width:390,height:464});
 await page.screenshot({path:'evidence/contract-matched-phone.png',animations:'disabled'});
 await page.setViewportSize({width:720,height:464});
 const input=page.getByPlaceholder('Search for apps...');
 await input.press('ArrowDown'); await input.press('ArrowDown'); await input.press('Enter');
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.type==='action.execute').length===1);
 let executed = await page.evaluate(()=>window.testRequests.filter(m=>m.type==='action.execute'));
 assert.equal(executed.length,1); assert.ok(['test-alpha','test-beta'].includes(executed[0].payload.target)); checks.push('arrows and Enter produce one allowlisted launch message');
 const inspectionsBeforeTouch=await page.evaluate(()=>window.testRequests.filter(m=>m.type==='result.inspect').length);
 await page.getByRole('option').filter({hasText:'Beta'}).click();
 await page.waitForFunction(n=>window.testRequests.filter(m=>m.type==='result.inspect').length>n,inspectionsBeforeTouch);
 executed = await page.evaluate(()=>window.testRequests.filter(m=>m.type==='action.execute'));
 assert.equal(executed.length,1); checks.push('touch-style selection does not launch an application');
 await page.getByRole('button',{name:/Actions/}).click();
 await page.getByRole('menuitem',{name:'Application Info'}).click();
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.type==='action.execute').length===2);
 executed=await page.evaluate(()=>window.testRequests.filter(m=>m.type==='action.execute'));
 assert.equal(executed.at(-1).payload.action,'info'); assert.equal(executed.at(-1).payload.target,'test-beta'); checks.push('ActionPanel routes Application Info to selected result');
 await input.fill('Alpha'); await page.getByRole('option').filter({hasText:'Alpha'}).waitFor();
 assert.equal(await page.getByRole('option').count(),1); checks.push('typing sends native query and replaces results');
 await input.press('Tab');await page.getByRole('menu').waitFor();
 await page.waitForFunction(()=>document.activeElement?.getAttribute('role')==='menuitem');
 const firstFocused=await page.evaluate(()=>document.activeElement.textContent);
 await page.keyboard.press('Tab');
 await page.waitForFunction(old=>document.activeElement.textContent!==old,firstFocused);
 await page.keyboard.press('Shift+Tab');
 await page.waitForFunction(old=>document.activeElement.textContent===old,firstFocused);
 assert.equal(await page.evaluate(()=>window.testRequests.filter(m=>m.type==='action.execute').length),2);
 await input.press('Escape');await page.getByRole('menu').waitFor({state:'detached'});
 checks.push('Tab opens contextual actions without executing and Escape restores the palette');
 await input.press('Control+k'); await page.getByRole('menu').waitFor(); await input.press('Escape');
 await page.getByRole('menu').waitFor({state:'detached'});
 assert.equal(await page.getByRole('menu').count(),0);
 assert.equal(await page.evaluate(()=>window.testRequests.filter(m=>m.type==='overlay.close').length),0); checks.push('Escape dismisses ActionPanel before closing overlay');
 await input.press('Escape');
 await page.waitForFunction(()=>window.testRequests.filter(m=>m.type==='overlay.close').length===1);
 assert.equal(await page.evaluate(()=>window.testRequests.filter(m=>m.type==='overlay.close').length),1); checks.push('Escape sends close request');
 // A stale success/error must not resurrect rows during the debounce window.
 await input.fill('Beta'); await page.getByRole('option').filter({hasText:'Beta'}).waitFor();
 await page.evaluate(()=>{
  const old=window.testRequests.filter(m=>m.type==='search.query').at(-1);
  const input=document.querySelector('input');
  input.value='Alpha';input.dispatchEvent(new Event('input',{bubbles:true}));
  queueMicrotask(()=>{
   window.testPort.postMessage(JSON.stringify({v:2,sessionId:old.sessionId,requestId:old.requestId,type:old.type,status:'error',error:{code:'INTERNAL',message:'STALE ERROR'}}));
  });
 });
 await page.getByRole('option').filter({hasText:'Alpha'}).waitFor();
 assert.equal(await page.getByRole('option').count(),1);
 assert.equal(await page.getByText('STALE ERROR',{exact:true}).count(),0);
 checks.push('query changes invalidate old replies and correlated old errors');
 await page.evaluate(()=>{
  window.testPort.postMessage('not-json');
  window.testPort.postMessage(JSON.stringify({v:2,sessionId:'foreign-session',requestId:'999',type:'search.query',status:'ok',data:{items:[]}}));
  window.testPort.postMessage(JSON.stringify({v:2,sessionId:'test-session',requestId:'999',type:'search.query',status:'ok',data:{items:[]}}));
 });
 await page.waitForTimeout(80);
 assert.equal(await page.getByRole('option').count(),1);
 checks.push('malformed, foreign-session and unknown-ID replies cannot replace results');
 // This fixture actually overflows the palette, so wheel input must scroll.
 await page.evaluate(()=>{
  const c=new MessageChannel();window.scrollRequests=[];
  c.port1.onmessage=e=>{
   const m=JSON.parse(e.data);window.scrollRequests.push(m);
   const data=m.type==='search.query'?{items:Array.from({length:30},(_,i)=>({id:'scroll-'+i,label:'Scroll app '+i,suffix:'',type:'Application',icon:window.testIcon}))}
    :m.type==='result.inspect'?{selected:m.payload.target}:m.type==='action.execute'?{state:'requested'}:{};
   c.port1.postMessage(JSON.stringify({v:2,sessionId:m.sessionId,requestId:m.requestId,type:m.type,status:'ok',data}));
  };
  window.postMessage(JSON.stringify({v:2,type:'bridge.ready',sessionId:'scroll-session'}),location.origin,[c.port2]);
 });
 await page.getByRole('option').filter({hasText:'Scroll app 0'}).waitFor();
 await page.getByRole('option').first().hover();await page.mouse.wheel(0,450);
 await page.waitForFunction(()=>Array.from(document.querySelectorAll('*')).some(el=>el.scrollTop>0));
 assert.equal(await page.evaluate(()=>window.scrollRequests.filter(m=>m.type==='action.execute').length),0);
 checks.push('actual overflowing result scroll never requests a launch');
 // Reconnect resets IDs and never retries an effect on the new port.
 await page.evaluate(()=>{
  const c=new MessageChannel();window.reconnected=[];
  c.port1.onmessage=e=>{
   const m=JSON.parse(e.data);window.reconnected.push(m);
   c.port1.postMessage(JSON.stringify({v:2,sessionId:m.sessionId,requestId:m.requestId,type:m.type,status:'ok',data:m.type==='search.query'?{items:[]}:{}}));
  };
  window.postMessage(JSON.stringify({v:2,type:'bridge.ready',sessionId:'new-session'}),location.origin,[c.port2]);
 });
 await page.getByText('No applications found',{exact:true}).waitFor();
 const reconnect=await page.evaluate(()=>window.reconnected);
 assert.equal(reconnect[0].requestId,'1');assert.equal(reconnect[0].sessionId,'new-session');
 assert.ok(reconnect.every(m=>m.type!=='action.execute'));
 checks.push('reconnect starts a new session and never retries an effect');
 await page.evaluate(()=>{
  const c=new MessageChannel();window.unknownOutcome=[];
  c.port1.onmessage=e=>{
   const m=JSON.parse(e.data);window.unknownOutcome.push(m);
   if(m.type==='action.execute')return;
   const data=m.type==='search.query'?{items:[{id:'timeout-app',label:'Timeout fixture',suffix:'',type:'Application',icon:window.testIcon}]}:m.type==='result.inspect'?{selected:m.payload.target}:{};
   c.port1.postMessage(JSON.stringify({v:2,sessionId:m.sessionId,requestId:m.requestId,type:m.type,status:'ok',data}));
  };
  window.postMessage(JSON.stringify({v:2,type:'bridge.ready',sessionId:'timeout-session'}),location.origin,[c.port2]);
 });
 await page.getByRole('option').filter({hasText:'Timeout fixture'}).waitFor();
 await input.press('Enter');await input.press('Enter');
 await page.getByText('Action outcome unknown. Search again before retrying.',{exact:true}).waitFor({timeout:10000});
 assert.equal(await page.evaluate(()=>window.unknownOutcome.filter(m=>m.type==='action.execute').length),1);
 await page.waitForTimeout(150);
 assert.equal(await page.evaluate(()=>window.unknownOutcome.filter(m=>m.type==='action.execute').length),1);
 checks.push('duplicate Enter and unknown action outcome never trigger an automatic retry');
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
