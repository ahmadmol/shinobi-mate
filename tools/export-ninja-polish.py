"""Extract generated poses with a body anchor, never a weapon-inclusive crop anchor."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageOps
import json, shutil, sys
from collections import deque
ROOT=Path(__file__).resolve().parent.parent
NAMES={
'NarutoShippuden':'idle approach punch-start punch-contact punch-recover kick-start kick-contact kick-recover jump-start jump-contact rasengan-low rasengan-charge rasengan-contact rasengan-recover kunai-start kunai-contact shuriken-start shuriken-contact clone-sign dodge block hit ko victory'.split(),
'SasukeShippuden':'idle approach sword-start sword-contact sword-recover backswing-start backswing-contact kick-start kick-contact kick-recover chidori-low chidori-charge chidori-contact chidori-recover shuriken-start shuriken-contact fire-start fire-contact jump-contact dodge block hit ko victory'.split()}
# Reviewed pelvis positions in each source cell. Forward extensions do not shift the feet anchor.
PIVOTS={
'NarutoShippuden':[.50,.52,.53,.56,.50,.48,.56,.51,.49,.51,.51,.53,.51,.50,.52,.53,.53,.52,.52,.54,.50,.54,.55,.53],
'SasukeShippuden':[.52,.51,.56,.65,.54,.55,.65,.53,.54,.52,.54,.54,.58,.52,.54,.60,.52,.60,.61,.53,.52,.56,.55,.54]}
def export(name,source):
 art=ROOT/'art/ninja-polish'/name;dest=ROOT/'img'/name/'combat/ninja093'
 art.mkdir(parents=True,exist_ok=True);dest.mkdir(parents=True,exist_ok=True)
 shutil.copy2(source,art/'atlas.png');atlas=Image.open(source).convert('RGBA')
 assert atlas.getpixel((0,0))[3]==0,'Expected transparent alpha'
 width,height=atlas.size;cells=[]
 alpha=atlas.getchannel('A').tobytes()
 # Generated rows are not geometrically uniform. Find transparent valleys separately
 # for each column; uniform grid slicing would cut heads/feet on this atlas.
 nominal=[210,412,590,766,938] if name.startswith('Naruto') else [210,410,596,770,941]
 cuts=[]
 for col in range(4):
  x0=round(col*width/4);x1=round((col+1)*width/4)
  boundaries=[0]
  for center in nominal:
   values=[(sum(alpha[y*width+x]>12 for x in range(x0,x1)),abs(y-center),y) for y in range(center-15,center+16)]
   boundaries.append(min(values)[2])
  cuts.append(boundaries+[height])
 for i in range(24):
  col=i%4;row=i//4;x0=round(col*width/4);x1=round((col+1)*width/4)
  box=(x0,cuts[col][row],x1,cuts[col][row+1]);cell=atlas.crop(box)
  # Discard detached fragments from neighbouring cells and baked flying projectiles.
  mask=cell.getchannel('A').tobytes();seen=set();groups=[]
  for p,a in enumerate(mask):
   if a<=12 or p in seen:continue
   q=deque([p]);seen.add(p);group=set()
   while q:
    p=q.popleft();group.add(p);x=p%cell.width;y=p//cell.width
    for dy in (-1,0,1):
     for dx in (-1,0,1):
      xx=x+dx;yy=y+dy;n=yy*cell.width+xx
      if 0<=xx<cell.width and 0<=yy<cell.height and n not in seen and mask[n]>12:seen.add(n);q.append(n)
   groups.append(group)
  keep=max(groups,key=len);expanded=set(keep)
  for p in keep:
   for dy in (-1,0,1):
    for dx in (-1,0,1):expanded.add(p+dy*cell.width+dx)
  pixels=cell.load()
  for y in range(cell.height):
   for x in range(cell.width):
    if y*cell.width+x not in expanded:pixels[x,y]=(0,0,0,0)
  bbox=cell.getchannel('A').point(lambda a:255 if a>12 else 0).getbbox();assert bbox
  cells.append((cell,bbox,box))
 scale=104/(cells[0][1][3]-cells[0][1][1])
 meta=[];sheet=Image.new('RGB',(4*288,6*312),(28,31,43));draw=ImageDraw.Draw(sheet)
 for i,(cell,bbox,sourcebox) in enumerate(cells):
  crop=cell.crop(bbox);pivot=cell.width*PIVOTS[name][i]
  # Largest connected skin region is the face, excluding separate hands and orange cloth.
  skin=set()
  for y in range(cell.height):
   for x in range(cell.width):
    r,g,b,a=cell.getpixel((x,y))
    if a>100 and b>75 and g>110 and r>g*1.06 and g>b*1.15 and r-g<85:skin.add((x,y))
  faces=[]
  while skin:
   start=skin.pop();q=deque([start]);region=[start]
   while q:
    x,y=q.popleft()
    for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)):
     p=(x+dx,y+dy)
     if p in skin:skin.remove(p);q.append(p);region.append(p)
   faces.append(region)
  face=max(faces,key=len)
  fx0=min(p[0] for p in face);fx1=max(p[0] for p in face);fy1=max(p[1] for p in face)
  hair=[]
  for y in range(bbox[1],fy1):
   for x in range(max(bbox[0],fx0-40),min(bbox[2],fx1+45)):
    r,g,b,a=cell.getpixel((x,y))
    if a>100 and ((name.startswith('Naruto') and r>180 and g>150 and r<g*1.45 and b<105) or (name.startswith('Sasuke') and 15<r<65 and b<110 and b>r*1.15 and g>r*1.05 and b>g*1.05)):hair.append(x)
  if hair:scale=64/(max(hair)-min(hair)+1)
  if name=='SasukeShippuden' and i not in (22,):
   belt=[x for y in range(cell.height) for x in range(cell.width) if (lambda c:c[3]>100 and c[0]>55 and c[2]>c[0]*1.04 and c[0]>c[1]*1.15 and c[2]>c[1]*1.3)(cell.getpixel((x,y)))]
   if belt:pivot=(min(belt)+max(belt))/2
  size=(round(crop.width*scale),round(crop.height*scale));sprite=crop.resize(size,Image.Resampling.LANCZOS)
  px=round(144-(pivot-bbox[0])*scale);py=288-size[1]
  assert px>0 and px+size[0]<288 and py>0,(name,i,'clip',px,py,size)
  frame=Image.new('RGBA',(288,288));frame.alpha_composite(sprite,(px,py))
  # The two generated sword anticipations look right despite the atlas instruction.
  # Normalize their source orientation once; runtime then flips every pose uniformly.
  flipped=name=='SasukeShippuden' and NAMES[name][i] in ('sword-start','backswing-start')
  if flipped:frame=ImageOps.mirror(frame)
  frame.save(dest/(NAMES[name][i]+'.png'))
  # Bounds are retained for visual review; baseline is intentionally feet anchored.
  meta.append(dict(name=NAMES[name][i],sourceCell=i,crop=list(bbox),bodyPivotSource=pivot,scale=scale,canvas=288,anchor=[144,288],authoredFacing='left',orientationCorrected=flipped,bounds=list(frame.getbbox())))
  sheet.paste(frame,(i%4*288,i//4*312),frame);draw.text((i%4*288+6,i//4*312+291),NAMES[name][i],fill='white')
 (art/'frames.json').write_text(json.dumps(meta,indent=2),encoding='utf-8');sheet.save(art/'contact-sheet.png')
 print(name,atlas.size,'scale',scale,'24 frames')
if __name__=='__main__':export(sys.argv[1],Path(sys.argv[2]))

