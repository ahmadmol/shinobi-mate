"""Versioned Naruto/Sasuke choreography and original code-drawn weapon/VFX assets."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter
import xml.etree.ElementTree as ET
import math,shutil,json
ROOT=Path(__file__).resolve().parent.parent
def vfx(dest,kind,index):
 im=Image.new('RGBA',(96,96));g=ImageDraw.Draw(im);t=index/8
 blue=(94,204,255,230);white=(220,248,255,245)
 if kind=='kunai':
  g.line((51,48,72,48),fill=(151,45,55,255),width=6)
  for x in range(53,72,4):g.line((x,45,x-2,51),fill=(227,127,118,255),width=1)
  g.ellipse((71,41,84,55),outline=(156,170,193,255),width=3)
  g.polygon([(18,48),(48,39),(54,48),(48,57)],fill=(32,40,54,255),outline=(159,174,191,255))
  g.line((19,48,49,48),fill=(110,130,150,255),width=1)
  g.line((84,48,94,48),fill=(110,195,240,100),width=2)
  im=im.rotate((index%3-1)*4,resample=Image.Resampling.BICUBIC)
 elif kind=='shuriken':
  pts=[]
  for i in range(8):
   a=i*math.pi/4+index*math.pi/16;r=18 if i%2==0 else 6
   pts.append((48+math.cos(a)*r,48+math.sin(a)*r))
  g.polygon(pts,fill=(25,29,40,255),outline=(155,174,199,255));g.ellipse((45,45,51,51),fill=(0,0,0,0),outline=(130,147,169,255),width=1)
  g.line((68,48,88,48),fill=(126,196,244,90),width=2)
 elif kind in ('rasengan','chakra-aura'):
  r=18 if kind=='rasengan' else 25
  glow=Image.new('RGBA',(96,96));d=ImageDraw.Draw(glow);d.ellipse((48-r,48-r,48+r,48+r),fill=(45,160,255,110));im.alpha_composite(glow.filter(ImageFilter.GaussianBlur(7)))
  g=ImageDraw.Draw(im)
  for j in range(3):g.arc((48-r+j*4,48-r+j*4,48+r-j*4,48+r-j*4),index*45+j*100,index*45+j*100+210,fill=blue,width=2)
  if kind=='rasengan':g.ellipse((43,43,53,53),fill=white)
 elif kind in ('rasengan-burst','chidori-burst','electric-aura','electric-hand'):
  radius=(12+index*4) if 'burst' in kind else 20
  for j in range(8):
   a=j*math.pi/4+t*1.4
   pts=[(48+math.cos(a)*4,48+math.sin(a)*4),(48+math.cos(a+.2)*radius*.6,48+math.sin(a+.2)*radius*.6),(48+math.cos(a-.15)*radius*.7,48+math.sin(a-.15)*radius*.7),(48+math.cos(a)*radius,48+math.sin(a)*radius)]
   if kind=='rasengan-burst':g.arc((48-radius,48-radius,48+radius,48+radius),j*45+index*10,j*45+index*10+28,fill=(80,205,255,max(40,240-index*22)),width=3)
   else:g.line(pts,fill=blue,width=3);g.line(pts,fill=white,width=1)
  if 'burst' in kind:g.ellipse((44,44,52,52),fill=(225,250,255,max(20,240-index*25)))
 elif kind=='sword-arc':
  for j in range(3):g.arc((12+j*3,10+j*3,82-j*3,86-j*3),125+index*4,240+index*4,fill=(210,232,255,220-j*55),width=3-j)
 elif kind=='fire-ball':
  g.polygon([(22,48),(65,26),(58,40),(88,36),(70,50),(85,61),(55,58),(65,72)],fill=(255,102,35,200))
  g.ellipse((17,34,53,62),fill=(255,164,47,240));g.ellipse((22,39,46,57),fill=(255,243,160,255))
 elif kind=='ninja-whoosh':
  for j in range(3):g.line((12,38+j*9,78-j*10,38+j*9),fill=(173,207,241,130-j*25),width=2)
 im.save(dest/f'{kind}-{index}.png')
def build(name):
 pack=ROOT/'img'/name;art=ROOT/'art/ninja-polish'/name;xml=pack/'conf/combat.xml'
 baseline=art/'baseline-combat.xml'
 if not baseline.exists():shutil.copy2(xml,baseline)
 tree=ET.parse(baseline);root=tree.getroot();root.set('width','288');root.set('height','288');root.set('anchorX','144');root.set('anchorY','288')
 animations=root.find('animations');attacks=root.find('attacks');effects=root.find('effects')
 def anim(id,*frames):
  old=animations.find(f"animation[@id='{id}']")
  if old is not None:animations.remove(old)
  e=ET.SubElement(animations,'animation',id=id)
  for frame in frames:ET.SubElement(e,'frame',image='combat/ninja093/'+frame+'.png',ticks='3')
 def effect(id):
  old=effects.find(f"effect[@id='{id}']")
  if old is not None:effects.remove(old)
  e=ET.SubElement(effects,'effect',id=id,frameTicks='1')
  for i in range(8):ET.SubElement(e,'frame',image=f'combat/ninja093/vfx/{id}-{i}.png')
 def attack(id):return attacks.find(f"attack[@id='{id}']")
 def phases(id,start,active,recovery):
  a=attack(id)
  for key,suffix,frames in [('chargeAnimation','start',start),('animation','active',active),('recoveryAnimation','recover',recovery)]:
   anim(id+'-'+suffix,*frames);a.set(key,id+'-'+suffix)
 def combo(id,contacts,windups):
  a=attack(id)
  for i,s in enumerate(a.findall('strike')):
   anim(id+f'-contact-{i}',contacts[i]);anim(id+f'-windup-{i}',windups[i])
 # Rewrite every alias too: combat's larger feet anchor must never encounter a legacy 128px frame.
 fallback='punch-recover' if name.startswith('Naruto') else 'sword-recover'
 valid=set(p.stem for p in (pack/'combat/ninja093').glob('*.png'))
 for a in list(animations):
  for f in a:
   old=Path(f.get('image')).stem
   mapped={'stance':'idle','dodge-start':'approach','dodge-active':'dodge','knockback':'hit','new-victory':'victory','melee-start':'punch-start' if name.startswith('Naruto') else 'sword-start','melee-active':'punch-contact' if name.startswith('Naruto') else 'sword-contact','melee-recovery':fallback,'ranged-start':'kunai-start' if name.startswith('Naruto') else 'shuriken-start','ranged-active':'kunai-contact' if name.startswith('Naruto') else 'shuriken-contact','special-start':'rasengan-low' if name.startswith('Naruto') else 'chidori-low','special-active':'rasengan-contact' if name.startswith('Naruto') else 'chidori-contact','special-recovery':fallback}.get(old,old)
   f.set('image','combat/ninja093/'+(mapped if mapped in valid else fallback)+'.png')
 for state in ['idle','approach','dodge','block','hit','ko','victory']:anim(state,state)
 anim('knockback','hit');anim('recover',fallback,'idle')
 vdir=pack/'combat/ninja093/vfx';vdir.mkdir(exist_ok=True)
 kinds=['kunai','shuriken','rasengan','chakra-aura','rasengan-burst','chidori-burst','electric-aura','electric-hand','sword-arc','fire-ball','ninja-whoosh']
 for kind in kinds:
  effect(kind)
  for i in range(8):vfx(vdir,kind,i)
 for e in effects:
  if e.get('cloneImage'):e.set('cloneImage','combat/ninja093/'+('punch-contact' if name.startswith('Naruto') else 'sword-contact')+'.png')
 for a in attacks:
  for key,value in {'effectX':'38','effectY':'-52','chargeX':'0','chargeY':'-45','projectileX':'52','projectileY':'-44'}.items():a.set(key,value)
  box=a.find('hitbox')
  if float(a.get('projectileSpeed'))>0:
   box.attrib.update(x='-8',y='-52',width='16',height='16');a.set('effect','shuriken' if a.get('weapon')=='shuriken' else 'kunai' if a.get('weapon')=='kunai' else 'fire-ball')
  elif a.get('weapon')=='sword':box.attrib.update(x='18',y='-76',width='104',height='34');a.set('effect','sword-arc');a.set('effectX','64');a.set('effectY','-60')
  else:box.attrib.update(x='12',y='-72',width='52',height='42')
 if name.startswith('Naruto'):
  new=ET.fromstring(ET.tostring(attack('kunai')));new.set('id','shuriken');new.set('weapon','shuriken');new.set('effect','shuriken');new.set('cooldown','42');new.set('weight','0.8');new.set('damage','6');attacks.append(new)
  phases('shuriken',['shuriken-start'],['shuriken-contact'],['punch-recover'])
  combo('light-combo',['punch-contact','kick-contact'],['punch-start','kick-start']);combo('kick-combo',['kick-contact','jump-contact'],['kick-start','jump-start'])
  a=attack('rasengan');a.attrib.update(effect='rasengan',chargeEffect='chakra-aura',impactEffect='rasengan-burst',effectX='64',effectY='-42',startup='13',active='16',recovery='10',weight='1.35')
  a.find('hitbox').attrib.update(x='18',y='-62',width='58',height='42')
  phases('rasengan',['rasengan-low','rasengan-charge'],['rasengan-contact'],['rasengan-recover','punch-recover'])
  a=attack('kunai');a.find('hitbox').attrib.update(x='-13',y='-48',width='26',height='8')
  a=attack('shadow-clone');a.set('effect','ninja-whoosh');a.set('chargeEffect','chakra-aura')
  p=root.find('personality');p.set('aggression','0.9');p.set('technique','0.42');p.set('dodgeEffect','ninja-whoosh')
 else:
  combo('sword-combo',['sword-contact','backswing-contact','kick-contact'],['sword-start','backswing-start','kick-start'])
  phases('aerial-sword',['sword-start'],['jump-contact'],['sword-recover'])
  a=attack('chidori');a.attrib.update(effect='electric-hand',chargeEffect='electric-aura',impactEffect='chidori-burst',effectX='64',effectY='-43',startup='12',active='15',recovery='11',weight='1.15')
  a.find('hitbox').attrib.update(x='18',y='-62',width='64',height='42')
  phases('chidori',['chidori-low','chidori-charge'],['chidori-contact'],['chidori-recover','sword-recover'])
  a=attack('fire-style');a.set('projectileY','-52');a.find('hitbox').attrib.update(x='-16',y='-64',width='32',height='24')
  p=root.find('personality');p.set('aggression','0.58');p.set('technique','0.86');p.set('counter','0.55');p.set('dodgeEffect','ninja-whoosh')
 ET.indent(tree);tree.write(xml,encoding='utf-8',xml_declaration=True)
 # Keep the legacy working asset tree synchronized, without replacing its launcher/runtime.
 other=ROOT/'Shimeji-ee/img'/name
 shutil.copy2(xml,other/'conf/combat.xml');shutil.copytree(pack/'combat/ninja093',other/'combat/ninja093',dirs_exist_ok=True)
 print(name,len(list(attacks)),'attacks, 24 dedicated poses, 88 original VFX frames')
if __name__=='__main__':
 for name in ['NarutoShippuden','SasukeShippuden']:build(name)
