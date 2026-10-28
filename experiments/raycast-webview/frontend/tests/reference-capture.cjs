const {chromium}=require('playwright');const fs=require('node:fs');
(async()=>{
const b=await chromium.launch({headless:true,executablePath:'/tmp/spotlight-headless/chrome-headless-shell-linux64/chrome-headless-shell',args:['--no-sandbox']});
const p=await b.newPage({viewport:{width:720,height:464},colorScheme:'dark'});const errors=[];
p.on('pageerror',e=>errors.push(e.message));
await p.route('**/*',async route=>{try{
const u=new URL(route.request().url());
if(u.origin!=='https://raycast-nuxt-ui.hrcd.fr')return route.abort();
const r=await fetch(u);await route.fulfill({status:r.status,contentType:r.headers.get('content-type')||'application/octet-stream',body:Buffer.from(await r.arrayBuffer())});
}catch(e){await route.abort();}});
await p.goto('https://raycast-nuxt-ui.hrcd.fr/',{waitUntil:'networkidle',timeout:45000});
await p.getByRole('dialog').waitFor();
fs.writeFileSync('/tmp/spotlight-reference-metrics.json',JSON.stringify(await p.evaluate(()=>Object.fromEntries(['[role=dialog]','input','[role=option]','kbd'].map(sel=>{const el=document.querySelector(sel);const s=el&&getComputedStyle(el);return[sel,s&&Object.fromEntries(['backgroundColor','color','borderColor','borderWidth','borderRadius','height','fontFamily','fontSize','fontWeight','lineHeight','padding','boxShadow','backdropFilter'].map(k=>[k,s[k]]))]}))),null,2));
await p.screenshot({path:'/workspace/scratch/378073a3682d/raycast-upstream/evidence/reference-browser-dark.png'});
console.log(JSON.stringify({title:await p.title(),errors,text:await p.locator('body').innerText(),dialog:await p.getByRole('dialog').boundingBox()},null,2));await b.close();
})().catch(e=>{console.error(e);process.exit(1)});
