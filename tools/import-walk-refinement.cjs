const fs=require('fs'),path=require('path');
const deps='C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/';
const sharp=require(deps+'sharp'),{PNG}=require(deps+'pngjs');
const root=path.resolve(__dirname,'..'),src='C:/Users/molha/.codex/generated_images/01a1111e-58ee-7072-9cff-62fd0f4ea2e9/exec-5cf571f7-92bf-4caa-a29a-cf804b720ec0.png';
(async()=>{
 const p=PNG.sync.read(fs.readFileSync(src));fs.copyFileSync(src,path.join(root,'output/behavior-polish/walk-refinement-source.png'));
 for(let row=0;row<2;row++){
  const character=['NarutoShippuden','SasukeShippuden'][row];
  for(let col=0;col<4;col++){
   const l=Math.floor(col*p.width/4),r=Math.floor((col+1)*p.width/4),t=Math.floor(row*p.height/2),b=Math.floor((row+1)*p.height/2);
   let x0=r,x1=l,y0=b,y1=t;
   for(let y=t;y<b;y++)for(let x=l;x<r;x++)if(p.data[(y*p.width+x)*4+3]>32){x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y);}
   let w=Math.round((x1-x0+1)*110/(y1-y0+1));
   const cut=await sharp(src).extract({left:x0,top:y0,width:x1-x0+1,height:y1-y0+1}).resize(w,110).toBuffer();
   await sharp({create:{width:128,height:128,channels:4,background:'#00000000'}}).composite([{input:cut,left:Math.round(64-w/2),top:18}]).png().toFile(path.join(root,'Shimeji-ee/img',character,['WalkLeft','WalkPass1','WalkRight','WalkPass2'][col]+'.png'));
  }
  console.log(character+': contact/passing/contact/passing gait imported');
 }
})().catch(e=>{console.error(e);process.exit(1)});
