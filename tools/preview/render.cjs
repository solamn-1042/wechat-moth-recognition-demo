// Documentation renderer: source WXML/WXSS + Page.data, not a WeChat runtime.
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm'),http=require('node:http');
const {chromium}=require('playwright');
const root=path.resolve(__dirname,'../..'), mini=path.join(root,'miniapp'), dest=path.join(root,'docs/images');
const read=p=>fs.readFileSync(p,'utf8');
const app=JSON.parse(read(path.join(mini,'app.json')));
const fixture=JSON.parse(read(path.join(dest,'inference-sample.json')));
const screens=[['home','index'],['upload','upload'],['result','upload'],['knowledge','moth-info'],['login','login']];
const renderer=String.raw`
const payload=JSON.parse(document.getElementById('payload').textContent);
const evaluate=(s,ctx)=>Function('ctx','with(ctx){return ('+s+')}')(ctx);
const expr=(s,ctx)=>evaluate(s.replace(/^\{\{|\}\}$/g,''),ctx);
const interpolate=(s,ctx)=>s.replace(/\{\{([\s\S]*?)\}\}/g,(_,x)=>String(evaluate(x,ctx)??''));
function children(parent,ctx){
  const fragment=document.createDocumentFragment(); let previous=false;
  for(const node of parent.childNodes){
    if(node.nodeType===3){fragment.append(document.createTextNode(interpolate(node.textContent,ctx)));continue;}
    if(node.nodeType!==1)continue;
    const loop=node.getAttribute('wx:for');
    if(loop){
      expr(loop,ctx).forEach((item,index)=>{const copy=node.cloneNode(true);copy.removeAttribute('wx:for');const box=document.createElement('div');box.append(copy);fragment.append(children(box,{...ctx,item,index}));});continue;
    }
    if(node.hasAttribute('wx:if')){previous=!!expr(node.getAttribute('wx:if'),ctx);if(!previous)continue;}
    else if(node.hasAttribute('wx:else')){if(previous)continue;}
    if(node.tagName.toLowerCase()==='block'){fragment.append(children(node,ctx));continue;}
    const tags={'view':'div','text':'span','image':'img','scroll-view':'div','swiper':'div','swiper-item':'section'};
    const old=node.tagName.toLowerCase();const el=document.createElement(tags[old]||old);
    for(const a of node.attributes){
      if(a.name.startsWith('wx:')||a.name.startsWith('bind'))continue;
      el.setAttribute(a.name,interpolate(a.value,ctx));
    }
    if(old==='swiper-item'&&Array.from(parent.children).indexOf(node)!==(ctx.currentTab||0))continue;
    if(old==='image'){el.style.objectFit=node.getAttribute('mode')==='aspectFill'?'cover':'contain';}
    if(old==='scroll-view')el.style.overflow=node.getAttribute('scroll-y')?'auto':'hidden';
    el.append(children(node,ctx));fragment.append(el);
  }
  return fragment;
}
const parser=new DOMParser();
// HTML parser treats <image> as img, so protect the component name first.
const template=parser.parseFromString(payload.wxml.replace(/<image\b/g,'<wximage').replace(/<\/image>/g,'</wximage>'),'text/html');
for(const n of template.querySelectorAll('wximage')){const e=template.createElement('image');for(const a of n.attributes)e.setAttribute(a.name,a.value);n.replaceWith(e);}
document.getElementById('page').append(children(template.body,payload.data));
`;
function documentFor(name,page){
 let definition;vm.runInNewContext(read(path.join(mini,'pages',page,page+'.js')),{Page:x=>definition=x,require:()=>({BASE_URL:''}),console:{log(){}}});
 let data=JSON.parse(JSON.stringify(definition.data));
 if(name==='upload')data.imageList='/pages/data/spodoptera_frugiperda.jpg';
 if(name==='result'){
   data.imageList='/docs/images/inference-sample.jpg';data.showResult=true;
   data.detections=fixture.detections.map(x=>({...x,confidencePercent:(x.confidence*100).toFixed(1)+'%',widthClass:'width-'+Math.round(x.confidence*10)*10}));
   data.mainDetection=data.detections.find(x=>x.class===fixture.main_detection.class);
 }
 let css=read(path.join(mini,'app.wxss'))+'\n'+read(path.join(mini,'pages',page,page+'.wxss'));
 css=css.replace(/(-?\d*\.?\d+)rpx/g,(_,x)=>Number(x)/7.5+'vw').replace(/(?<![\w-])image(?![\w-])/g,'img');
 const payload=JSON.stringify({wxml:read(path.join(mini,'pages',page,page+'.wxml')),data}).replace(/</g,'\\u003c');
 const tabs=name==='home'?'<footer class="tabs">'+app.tabBar.list.map(x=>'<div><img src="/'+(x.text==='首页'?x.selectedIconPath:x.iconPath)+'"><span>'+x.text+'</span></div>').join('')+'</footer>':'';
 return '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{margin:0;font-family:"Microsoft YaHei",sans-serif;color:#111;overflow-x:hidden}button{font:inherit;display:block;text-align:center;border:0;line-height:1.6;box-sizing:border-box}input{font:inherit;box-sizing:border-box}img{display:inline-block}'+css+'.nav{height:44px;background:'+app.window.navigationBarBackgroundColor+';display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:600}.tabs{display:flex;justify-content:space-around;background:white;border-top:1px solid #ddd;height:55px;align-items:center}.tabs div{display:flex;flex-direction:column;align-items:center;font-size:11px;gap:3px}.tabs img{width:24px;height:24px}.container{min-height:calc(100vh - '+(name==='home'?99:44)+'px)}</style><div class="nav">'+app.window.navigationBarTitleText+'</div><main id="page"></main>'+tabs+'<script id="payload" type="application/json">'+payload+'</script><script>'+renderer+'</script></html>';
}
(async()=>{
 fs.mkdirSync(dest,{recursive:true});
 const pages=Object.fromEntries(screens.map(([n,p])=>['/'+n,documentFor(n,p)]));
 const server=http.createServer((req,res)=>{
   const url=new URL(req.url,'http://localhost').pathname;
   if(pages[url]){res.setHeader('Content-Type','text/html; charset=utf-8');res.end(pages[url]);return;}
   const file=path.resolve(url.startsWith('/docs/')?root:mini,'.'+url);
   if(!(file.startsWith(root+path.sep))||!fs.existsSync(file)||!fs.statSync(file).isFile()){res.writeHead(404);res.end();return;}
   res.setHeader('Content-Type',file.endsWith('.png')?'image/png':'image/jpeg');res.end(fs.readFileSync(file));
 });
 await new Promise(r=>server.listen(0,'127.0.0.1',r));
 let browser;
 try{
   browser=await chromium.launch({headless:true,...(process.env.PREVIEW_BROWSER_CHANNEL?{channel:process.env.PREVIEW_BROWSER_CHANNEL}:{})});
   const context=await browser.newContext({viewport:{width:390,height:844},deviceScaleFactor:2});
   const page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
   const records=[];
   for(const [name] of screens){
     await page.goto('http://127.0.0.1:'+server.address().port+'/'+name);
     await page.evaluate(()=>Promise.all([...document.images].map(im=>im.decode())));
     if(errors.length)throw Error(errors.join('\n'));
     const body=await page.locator('#page').innerText();if(!body.trim()||body.includes('{{'))throw Error('Unrendered template '+name);
     await page.screenshot({path:path.join(dest,name+'.png'),fullPage:false});
     records.push({name,width:390,height:844,image:name+'.png',source:'miniapp/pages/'+screens.find(x=>x[0]===name)[1]});
     console.log('rendered',name);
   }
   fs.writeFileSync(path.join(dest,'preview-manifest.json'),JSON.stringify({renderer:'Browser adaptation of WXML/WXSS and initial Page.data; no WeChat login, camera or network runtime',screens:records},null,2)+'\n');
 }finally{if(browser)await browser.close();server.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
