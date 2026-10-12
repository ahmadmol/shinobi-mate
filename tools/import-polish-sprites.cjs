// Mechanical extraction, sizing and registration of ImageGen output; no artwork is drawn here.
const fs=require('fs'),path=require('path');
const deps='C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/';
const sharp=require(deps+'sharp'),{PNG}=require(deps+'pngjs');
const root=path.resolve(__dirname,'..');
const generated='C:/Users/molha/.codex/generated_images/01a1111e-58ee-7072-9cff-62fd0f4ea2e9/';
const names=['WalkLeft','WalkPass1','WalkRight','WalkPass2','Smile','Blink','MouseSurprise','DragWince','HandSign','NinjaJump','GrabCeiling','ClimbCeiling1'];
(async()=>{
 for(const [character,file] of [['NarutoShippuden','exec-7414c8c2-7f83-4751-b075-8effef71bc9f.png'],['SasukeShippuden','exec-a58a368d-dc7e-4d53-ba01-b4dc9ae4a524.png']]){
  const src=path.join(generated,file),p=PNG.sync.read(fs.readFileSync(src));
  const dest=path.join(root,'Shimeji-ee/img',character);
  fs.copyFileSync(src,path.join(root,'output/behavior-polish',character+'-source.png'));
  const runs=[];
  for(let c=0;c<4;c++){
   let r=[],start=-1;
   for(let y=0;y<=p.height;y++){let count=0;for(let x=c*p.width/4;y<p.height&&x<(c+1)*p.width/4;x++)if(p.data[(y*p.width+x)*4+3]>100)count++;
    if(count>3&&start<0)start=y;if(count<=3&&start>=0){if(y-start>100)r.push([start,y-1]);start=-1;}}
   if(r.length!==3)throw Error('Expected three separated sprites in column '+c);runs.push(r);
  }
  const results=[];
  for(let i=0;i<12;i++){
   const c=i%4,r=Math.floor(i/4),yr=runs[c][r];
   let x0=p.width,x1=0,y0=p.height,y1=0;
   for(let y=Math.max(0,yr[0]-3);y<=Math.min(p.height-1,yr[1]+3);y++)for(let x=c*p.width/4;x<(c+1)*p.width/4;x++)if(p.data[(y*p.width+x)*4+3]>32){x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y);}
   const ceiling=i>=10,targetH=i===9?88:110;
   const h=targetH,w=Math.round((x1-x0+1)*h/(y1-y0+1));
   const frame=await sharp(src).extract({left:x0,top:y0,width:x1-x0+1,height:y1-y0+1}).resize(w,h).toBuffer();
   const left=Math.round(64-w/2),top=ceiling?0:128-h;
   await sharp({create:{width:128,height:128,channels:4,background:'#00000000'}}).composite([{input:frame,left,top}]).png().toFile(path.join(dest,names[i]+'.png'));
   results.push({name:names[i],source:[x0,y0,x1,y1],placement:[left,top,w,h]});
  }
  fs.copyFileSync(path.join(dest,'GrabCeiling.png'),path.join(dest,'ClimbCeiling2.png'));
  fs.writeFileSync(path.join(root,'output/behavior-polish',character+'-registration.json'),JSON.stringify(results,null,2));
  console.log(character+': 13 sprites, 128x128 RGBA, feet baseline 128 / ceiling 0');
 }
})().catch(e=>{console.error(e);process.exit(1)});
