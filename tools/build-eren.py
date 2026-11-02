"""Reproducible first-version Eren sprites and synthesized SFX. No reference pixels/audio copied.
Art is intentionally placeholder, awaiting dedicated artist-authored motion frames.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import math, random, json, wave, struct, shutil, re
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
pack=ROOT/'img/ErenYeager'
(pack/'conf').mkdir(parents=True,exist_ok=True)
(pack/'combat/human').mkdir(parents=True,exist_ok=True)
(pack/'combat/titan').mkdir(parents=True,exist_ok=True)
(pack/'combat/fx').mkdir(parents=True,exist_ok=True)

def sprite(titan,pose,phase):
    im=Image.new('RGBA',(512,512));d=ImageDraw.Draw(im)
    # Draw at four times human resolution. Titan outputs twice the human size.
    s=4
    def pts(v):return [(int(x*s),int(y*s)) for x,y in v]
    def poly(v,c):d.polygon(pts(v),fill=c);d.line(pts(v+[v[0]]),fill='#211d20',width=5,joint='curve')
    def ell(b,c,outline='#211d20'):d.ellipse(tuple(int(v*s) for v in b),fill=c,outline=outline,width=5)
    def line(v,c='#493429',w=1.2):d.line(pts(v),fill=c,width=int(w*s),joint='curve')
    skin='#b99568' if titan else '#e3b894';shade='#876645' if titan else '#bb8d6c';hair='#302923'
    bob=phase if pose in ['approach','idle'] else 0
    # Whole body adjustment is bounded and keeps transparent canvas/feet anchor stable.
    tilt=-4 if pose in ['dash','rush','dodge'] else 0
    cx=64+tilt;hy=30-bob-(3 if pose=='roar' else 0)
    if pose=='ko':
        poly([(20,117),(36,99),(74,96),(106,108),(107,122),(30,125)],skin if titan else '#26282d')
        ell((16,92,49,122),skin);poly([(15,104),(17,89),(43,88),(52,101),(39,99)],hair)
        line([(24,106),(32,108)]);return im.resize((256 if titan else 128,)*2,Image.Resampling.LANCZOS)
    legspread=7+phase*2 if pose in ['approach','dash','rush'] else 7
    legcolor=skin if titan else '#25272a'
    poly([(cx-18,82),(cx-1,82),(cx-7,105),(cx-11-legspread,121),(cx-25-legspread,121),(cx-17,100)],legcolor)
    poly([(cx+1,82),(cx+19,82),(cx+22,103),(cx+25+legspread,121),(cx+10+legspread,121),(cx+5,103)],legcolor)
    if pose=='kick':poly([(cx-8,85),(cx+3,96),(cx-32,104),(cx-47,98),(cx-43,89),(cx-24,93)],legcolor)
    if not titan:
        poly([(cx-17,47),(cx+17,46),(cx+20,86),(cx-20,87)],'#a7a29b')
        poly([(cx-18,43),(cx-7,47),(cx-12,83),(cx-21,104),(cx-31,96),(cx-24,56)],'#303137')
        poly([(cx+10,46),(cx+19,44),(cx+26,58),(cx+30,100),(cx+14,94),(cx+9,73)],'#303137')
        line([(cx-7,50),(cx-1,56),(cx+5,50)],'#40362f')
    else:
        poly([(cx-18,44),(cx-29,52),(cx-21,65),(cx-14,85),(cx+14,85),(cx+24,62),(cx+27,50),(cx+16,43)],skin)
        for offset in [-1,1]:
            x=cx+offset*11;ell((x-9,49,x+8,62),skin);line([(x-8,61),(cx,63)])
        for y in [65,72,79]:
            poly([(cx-11,y),(cx-2,y-2),(cx-1,y+5),(cx-9,y+6)],shade)
            poly([(cx+1,y-2),(cx+11,y),(cx+9,y+6),(cx+1,y+5)],skin)
        line([(cx,62),(cx,84)])
        for x in [cx-23,cx+21]:line([(x,67),(x+2,75),(cx+(-13 if x<cx else 13),82)])
    arm=skin if titan else '#303137'
    left=[(cx-20,51),(cx-29,54),(cx-31,75),(cx-21,78),(cx-17,64)]
    right=[(cx+19,50),(cx+27,54),(cx+29,77),(cx+20,81),(cx+16,61)]
    if pose in ['punch','double','jump']:
        left=[(cx-20,51),(cx-32,55),(cx-47-phase*2,62),(cx-48-phase*2,71),(cx-27,70),(cx-16,61)]
    if pose in ['smash','charge']:
        left=[(cx-20,50),(cx-29,38),(cx-26,24),(cx-16,26),(cx-17,43),(cx-9,54)]
    if pose=='transforming' and not titan:
        left=[(cx-20,51),(cx-31,58),(cx-14,64),(cx-11,51),(cx-16,47)]
        right=[(cx+19,50),(cx+29,57),(cx+13,66),(cx+4,53),(cx+10,47)]
    if pose=='smash' and phase>0:
        left=[(cx-20,51),(cx-32,65),(cx-30,103),(cx-19,103),(cx-20,67)]
    if pose in ['block','dodge']:
        left=[(cx-20,51),(cx-32,54),(cx-33,40),(cx-25,36),(cx-22,48)]
    if pose in ['recover','hit','knockback']:
        left=[(cx-20,51),(cx-34,56),(cx-41,72),(cx-31,76),(cx-19,61)]
    if pose in ['roar','victory']:
        left=[(cx-20,51),(cx-33,58),(cx-34,79),(cx-24,85),(cx-23,65)]
        right=[(cx+19,50),(cx+32,56),(cx+35,78),(cx+25,84),(cx+23,65)]
    poly(left,arm);poly(right,arm)
    lx,ly=left[3];ell((lx-4,ly-7,lx+6,ly+3),skin)
    ell((cx+20,75,cx+31,86),skin)
    poly([(cx-6,40),(cx+7,40),(cx+9,49),(cx-8,50)],skin)
    ell((cx-21,hy-18,cx+19,hy+20),skin)
    poly([(cx-24,hy-4),(cx-19,hy-22),(cx-4,hy-27),(cx+14,hy-21),(cx+22,hy-7),(cx+19,hy+17),(cx+12,hy+6),(cx+9,hy-15),(cx-3,hy-17),(cx-16,hy-8)],hair)
    if not titan:ell((cx+14,hy-16,cx+27,hy-5),hair)
    else:
        poly([(cx+14,hy-10),(cx+25,hy+6),(cx+19,hy+30),(cx+12,hy+12)],hair)
        poly([(cx-17,hy-14),(cx-28,hy+6),(cx-15,hy+2),(cx-13,hy-9)],hair)
    # Determined teal eyes, knitted brow, Titan visible teeth.
    for x in [cx-13,cx+1]:
        poly([(x,hy+1),(x+9,hy+3),(x+8,hy+7),(x+1,hy+6)],'#eee9d6')
        ell((x+2,hy+2,x+6,hy+7),'#70b5a0')
        line([(x-1,hy-1),(x+9,hy+1)],'#242322',1.8)
    line([(cx-5,hy+7),(cx-8,hy+11),(cx-4,hy+12)])
    if titan:
        mouth=(cx-14,hy+12,cx+10,hy+(25+phase*2 if pose in ['roar','transforming','victory'] else 21))
        ell(mouth,'#402727');d.rectangle(tuple(int(v*s) for v in (mouth[0]+3,mouth[1]+1,mouth[2]-3,mouth[1]+4)),fill='#efdfbf')
        for x in range(cx-9,cx+9,4):line([(x,hy+13),(x,hy+17)],'#605644',.7)
        if pose in ['roar','transforming','victory']:ell((cx-7,hy+23,cx+5,hy+28),'#a45d56')
    else:line([(cx-9,hy+15),(cx+2,hy+15)],'#523c32')
    if pose=='hit':line([(cx-15,hy),(cx-7,hy+4)]);line([(cx+1,hy+2),(cx+9,hy)])
    return im.resize((256 if titan else 128,)*2,Image.Resampling.LANCZOS)

poses=['idle','approach','dodge','block','hit','knockback','recover','ko','victory','charge','transforming','punch','kick','dash','jump','double','rush','smash','roar']
for titan in [False,True]:
    for pose in poses:
        for frame in range(3):sprite(titan,pose,frame).save(pack/f'combat/{"titan" if titan else "human"}/{pose}-{frame}.png')

# Reuse behavior definitions, replacing every ordinary pose with newly drawn human art.
source=ROOT/'img/AstaBlackClover'
for file in (source/'conf').glob('*.xml'):
    if file.name=='combat.xml':continue
    shutil.copyfile(file,pack/'conf'/file.name)
for file in (source/'conf').glob('cloud_messages*'):shutil.copyfile(file,pack/'conf'/file.name)
names=set()
for file in (pack/'conf').glob('*.xml'):names.update(re.findall(r'Image="/([^\"]+\.png)"',file.read_text(encoding='utf-8')))
for name in names|{'Stand.png','Preview.png','Smile.png','HandSign.png','DragWince.png','Sit.png','SitLegsDown.png','SitLookUp.png','MouseSurprise.png'}:
    pose='approach' if 'Walk' in name else 'hit' if 'Wince' in name else 'charge' if 'Hand' in name else 'idle'
    sprite(False,pose,0).save(pack/name)
manifest={'id':'ErenYeager','displayName':'Eren Yeager','version':'0.1.0','author':'ShinobiMate','description':'Human to Attack Titan; first version with procedural placeholder art.','preview':'Preview.png','supportedLanguages':['ar','en'],'supportsCombat':True,'supportsCloud':True,'supportsMood':True,'minimumShinobiMateVersion':'0.9.0'}
manifest.update({key:value for key,value in json.loads((source/'manifest.json').read_text(encoding='utf-8')).items() if key in ['moodPoses','idleActivities','socialPoses','interactionPoses']})
(pack/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')

fx=['impact','dust','spark','transform-burst','transform-smoke','transform-lightning','roar-wave']
for name in fx:
    for phase in range(4):
        im=Image.new('RGBA',(256,256));d=ImageDraw.Draw(im);r=22+phase*18
        if name in ['dust','transform-smoke']:
            for i in range(8):
                x=128+math.cos(i*1.7)*r;y=128+math.sin(i*1.7)*r*.6;d.ellipse((x-32,y-24,x+32,y+24),fill=(145,148,148,110-phase*15))
        elif name=='transform-lightning':
            for side in [-1,1]:d.line([(128+side*40,20),(128+side*65,76),(128+side*31,100),(128+side*76,165),(128+side*55,223)],fill=(244,205,99,200),width=5)
        else:
            d.ellipse((128-r,128-r,128+r,128+r),outline=(240,190,88,180-phase*25),width=6)
            if name!='roar-wave':
                for i in range(8):
                    a=i*math.pi/4;d.line((128+math.cos(a)*r*.6,128+math.sin(a)*r*.6,128+math.cos(a)*r*1.3,128+math.sin(a)*r*1.3),fill=(244,205,115,160),width=4)
        im.save(pack/f'combat/fx/{name}-{phase}.png')

def profile(titan):
    size=256 if titan else 128;form='titan' if titan else 'human'
    root=ET.Element('combat',version='1',width=str(size),height=str(size),anchorX=str(size//2),anchorY=str(size),movementSpeed='2.6' if titan else '5.8')
    ET.SubElement(root,'hurtbox',x='-42' if titan else '-21',y='-218' if titan else '-106',width='84' if titan else '42',height='204' if titan else '99')
    ET.SubElement(root,'personality',aggression='.88',technique='.25',dodge='.08' if titan else '.28',block='.18',counter='.08',taunt='0',dodgeSpeed='4' if titan else '9',jumpHeight='0' if titan else '24',defenseCooldown='32',defenseCost='20',blockReduction='.55' if titan else '.4')
    animations=ET.SubElement(root,'animations')
    for pose in poses:
        a=ET.SubElement(animations,'animation',id=pose)
        if pose=='transforming' and not titan:
            ET.SubElement(a,'frame',image='combat/human/transforming-0.png',ticks='12')
            ET.SubElement(a,'frame',image='combat/human/hidden.png',ticks='14')
            continue
        for f in range(3):
            ticks=4 if titan else 3
            attrs=dict(image=f'combat/{form}/{pose}-{f}.png',ticks=str(ticks))
            if pose=='approach' and f==0:attrs['soundEvent']='movement.footstep'
            ET.SubElement(a,'frame',**attrs)
    effects=ET.SubElement(root,'effects')
    for fxid in fx:
        fxnode=ET.SubElement(effects,'effect',id=fxid,frameTicks='3')
        for f in range(4):ET.SubElement(fxnode,'frame',image=f'combat/fx/{fxid}-{f}.png')
    attacks=ET.SubElement(root,'attacks')
    def move(id,kind,anim,damage,startup,active,recovery,range,speed=0,knock=18,jump=0,box=None,combo=False):
        a=ET.SubElement(attacks,'attack',id=id,kind=kind,minRange='0',range=str(range),damage=str(damage),cooldown=str(40 if titan else 24),startup=str(startup),active=str(active),recovery=str(recovery),speed=str(speed),projectileSpeed='0',knockback=str(knock),jumpHeight=str(jump),cost='10' if kind=='special' else '0',weight='.8' if kind=='special' else '1.2',animation=anim,chargeAnimation='charge',recoveryAnimation='recover',effect='roar-wave' if anim=='roar' else 'impact',chargeEffect='dust',startupSound='charge',attackSound='titan.roar' if anim=='roar' else 'whoosh',impactSound='impact',effectY='-80' if titan else '-60',effectX='60' if titan else '30')
        x,y,w,h=box or ((28,-150,110,100) if titan else (12,-78,68,64))
        ET.SubElement(a,'hitbox',x=str(x),y=str(y),width=str(w),height=str(h))
        if combo:
            for at in [0,6]:ET.SubElement(a,'strike',at=str(at),duration='3',damage=str(damage),knockback=str(knock//2),animation=anim,windup='charge')
    if titan:
        move('heavy-punch','melee','punch',18,14,5,18,160,knock=46)
        move('double-punch','melee','double',11,16,10,22,160,knock=34,combo=True)
        move('forward-rush','dash','rush',16,16,12,24,300,speed=8,knock=50)
        move('ground-smash','special','smash',22,22,5,26,190,knock=55,box=(-95,-36,190,38))
        move('roar','special','roar',4,16,6,20,210,knock=24,box=(-105,-170,210,166))
    else:
        move('punch-combo','melee','punch',5,4,10,8,94,combo=True)
        move('kick','melee','kick',8,5,4,9,104,knock=23)
        move('short-dash','dash','dash',6,4,8,10,220,speed=11)
        move('jump-attack','aerial','jump',9,6,10,12,125,speed=5,jump=36)
        ET.SubElement(root,'transformation',profile='conf/titan.xml',transformOncePerBattle='true',transformationHpThreshold='0.5',prepareTicks='12',revealTick='26',landingTick='38',duration='48')
    ET.indent(root);ET.ElementTree(root).write(pack/'conf'/('titan.xml' if titan else 'combat.xml'),encoding='utf-8',xml_declaration=True)
profile(False);profile(True)
Image.new('RGBA',(128,128)).save(pack/'combat/human/hidden.png')

# Original low-volume synthesized sounds, no anime sampling.
rng=random.Random(71);events={}
for event in ['movement.footstep','movement.jump','movement.landing','movement.landingHeavy','transform.charge','transform.burst','titan.roar','charge','whoosh','impact','grunt','dodge','block','parry','ko','victory','combat.knockback']:
    dur=1.1 if event=='titan.roar' else .48 if event=='transform.charge' else .2
    rate=22050;data=[]
    for i in range(int(rate*dur)):
        t=i/rate;envelope=math.sin(math.pi*t/dur)**.8
        if event=='titan.roar':v=(math.sin(2*math.pi*74*t+3*math.sin(t*15))+.45*math.sin(2*math.pi*148*t)+rng.uniform(-.35,.35))*.23
        elif event=='transform.charge':v=math.sin(2*math.pi*(130*t+240*t*t))*.22
        else:v=(rng.uniform(-1,1)*.35+math.sin(2*math.pi*95*t)*.4)*.4
        data.append(struct.pack('<h',int(max(-1,min(1,v*envelope))*32767)))
    filename='sounds/'+event.replace('.','-')+'.wav';(pack/'sounds').mkdir(exist_ok=True)
    with wave.open(str(pack/filename),'wb') as out:out.setparams((1,2,rate,0,'NONE','not compressed'));out.writeframes(b''.join(data))
    events[event]=dict(category='MOVEMENT' if event.startswith('movement') else 'COMBAT',variations=[filename],volume=.4,cooldown=200,maxConcurrent=2,loop=False,priority=30,maxDurationMs=1800)
(pack/'conf/sounds.json').write_text(json.dumps(dict(version=1,events=events),indent=2),encoding='utf-8')

preview=Image.new('RGB',(960,340),'#202631');d=ImageDraw.Draw(preview)
for i,(titan,pose) in enumerate([(False,'idle'),(False,'punch'),(False,'transforming'),(True,'roar'),(True,'punch'),(True,'smash')]):
    art=sprite(titan,pose,1);x=[20,150,280,420,600,780][i];art.thumbnail((170,256));preview.paste(art,(x,290-art.height),art);d.text((x,305),('TITAN ' if titan else 'HUMAN ')+pose,fill='white')
(ROOT/'output/eren').mkdir(parents=True,exist_ok=True);preview.save(ROOT/'output/eren/preview.png')
print('Generated ErenYeager: procedural placeholder frames, 2x Titan, original SFX')
