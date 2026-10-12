const fs=require('fs');
const {PNG}=require('C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/pngjs');
for(const name of ['exec-7414c8c2-7f83-4751-b075-8effef71bc9f','exec-a58a368d-dc7e-4d53-ba01-b4dc9ae4a524']){
 const p=PNG.sync.read(fs.readFileSync('C:/Users/molha/.codex/generated_images/01a1111e-58ee-7072-9cff-62fd0f4ea2e9/'+name+'.png'));
 console.log(name,p.width,p.height);
 for(let c=0;c<4;c++){let runs=[],start=-1;for(let y=0;y<=p.height;y++){let count=0;for(let x=Math.floor(c*p.width/4);y<p.height&&x<Math.floor((c+1)*p.width/4);x++)if(p.data[(y*p.width+x)*4+3]>100)count++;if(count>3&&start<0)start=y;if(count<=3&&start>=0){runs.push([start,y-1]);start=-1;}}console.log(c,runs);}
}
