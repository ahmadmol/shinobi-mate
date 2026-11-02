"""XML, manifest, asset aliases and original synthesized audio; no raster drawing."""
from pathlib import Path
import shutil,json,math,wave,struct,random
import xml.etree.ElementTree as E
R=Path(__file__).resolve().parents[1];NS='http://www.group-finity.com/Mascot';E.register_namespace('',NS)
def tag(n):return '{'+NS+'}'+n
def frames(folder,ids):return [f'art/{folder}/{i:02}.png' for i in ids]
def variants(folder):return [f'art/{folder}/{i}.png' for i in range(4)]
def tom(i):return f'art/Tom/{i:02}.png'
def jerry(i):return f'art/Jerry/{i:02}.png'
def h(i):return f'art/ErenHumanMotion/{i:02}.png'
def ti(name):return frames('titan-'+name,range(4))

tomseq=dict(idle=variants('idle'),walk=frames('TomMotion',range(4)),run=frames('TomMotion',range(4,8)),punch=[tom(8),tom(8),tom(9),tom(8)],kick=[tom(8),tom(10),tom(10),tom(8)],dodge=[tom(8),tom(3),tom(23),tom(8)],block=[tom(8),tom(20),tom(20),tom(8)],hit=[tom(8),tom(15),tom(18),tom(8)],knockback=[tom(11),tom(15)],ko=variants('ko'),victory=[tom(19),tom(16),tom(19)],jump=[tom(13),tom(6),tom(12),tom(13)],sit=[tom(23)],crouch=[tom(13)],pounce=[tom(13),tom(12),tom(12),tom(13)],angry=[tom(14)],surprised=[tom(15)],laugh=[tom(16),tom(19)],point=[tom(17)],stretch=[tom(21)],slide=[tom(22)],sneak=[tom(3),tom(23)],recover=[tom(8),tom(0)])
jseq=dict(idle=variants('idle'),walk=frames('JerryMotion',range(4)),run=frames('JerryMotion',range(4,8)),punch=frames('JerryMotion',range(8,12)),kick=frames('JerryMotion',range(12,16)),dodge=frames('JerryMotion',range(16,20)),counter=frames('JerryMotion',range(20,24)),block=[jerry(17),jerry(17)],hit=[jerry(14),jerry(15),jerry(8)],knockback=[jerry(11),jerry(14)],ko=variants('ko'),victory=[jerry(19),jerry(7),jerry(19)],jump=[jerry(13),jerry(6),jerry(7),jerry(20)],sit=[jerry(18)],crouch=[jerry(20)],cheese=[jerry(18)],laugh=[jerry(15),jerry(19)],surprised=[jerry(14)],point=[jerry(16)],slide=[jerry(21)],hide=[jerry(13),jerry(20)],sneak=[jerry(3),jerry(13)],recover=[jerry(8),jerry(0)])
hseq=dict(idle=variants('idle'),walk=frames('ErenHumanMotion',range(4)),run=frames('ErenHumanMotion',range(4,8)),punch=frames('ErenHumanMotion',range(8,12)),kick=frames('ErenHumanMotion',range(12,16)),dodge=frames('ErenHumanMotion',range(16,20)),block=['art/human-sheet/09.png'],hit=[h(16),h(18),h(19)],knockback=[h(18),h(19)],ko=variants('ko'),victory=['art/human-sheet/01.png',h(19)],jump=[h(17),h(5),h(6),h(17)],sit=['art/human-sheet/22.png'],crouch=[h(17)],recover=[h(11),h(8)],transforming=frames('ErenHumanMotion',range(20,24)))
tseq=dict(idle=ti('idle'),walk=ti('idle'),run=ti('heavy-punch'),punch=ti('heavy-punch'),double=ti('heavy-punch')+ti('heavy-punch')[1:],smash=ti('ground-smash'),roar=ti('roar'),dodge=ti('idle')[::-1],block=ti('heavy-punch')[:2],hit=ti('idle')[2:]+ti('idle')[:2],knockback=ti('idle')[::-1],ko=variants('titan-ko'),victory=ti('roar'),jump=ti('ground-smash'),recover=ti('heavy-punch')[3:]+ti('heavy-punch')[:1],transforming=ti('roar'))

def audio(root,character):
    events={};rng=random.Random(912);rate=22050
    for event in ['movement.footstep','movement.run','movement.jump','movement.landing','movement.landingHeavy','whoosh','impact','grunt','dodge','block','parry','charge','ko','victory','combat.knockback','transform.charge','transform.burst','titan.roar','titan.step','titan.punch','titan.smash']:
        dur=1.1 if event=='titan.roar' else .45 if 'charge' in event else .13 if character=='Jerry' else .22
        freq=210 if character=='Jerry' else 95 if character=='Tom' else 110
        if event.startswith('titan.') or 'Heavy' in event:freq=65
        data=[]
        for i in range(int(rate*dur)):
            t=i/rate;env=math.sin(math.pi*t/dur)**.8
            signal=math.sin(2*math.pi*(freq*t+35*t*t))*.25+rng.uniform(-1,1)*.11
            if event=='titan.roar':signal=(math.sin(2*math.pi*68*t+3*math.sin(13*t))+.4*math.sin(2*math.pi*136*t)+rng.uniform(-.25,.25))*.22
            data.append(struct.pack('<h',int(signal*env*32760)))
        path='sounds/'+event+'.wav';(root/'sounds').mkdir(exist_ok=True)
        with wave.open(str(root/path),'wb') as f:f.setparams((1,2,rate,0,'NONE','not compressed'));f.writeframes(b''.join(data))
        events[event]=dict(category='MOVEMENT' if event.startswith('movement') or event=='titan.step' else 'COMBAT',variations=[path],volume=.35,cooldown=150,maxConcurrent=2,loop=False,priority=30,maxDurationMs=1800)
    (root/'conf/sounds.json').write_text(json.dumps(dict(version=1,events=events),indent=2))

def ordinary(name,seq,size):
    root=R/'img'/name;(root/'conf').mkdir(parents=True,exist_ok=True)
    for p in (R/'img/AstaBlackClover/conf').glob('*.xml'):
        if p.name!='combat.xml':shutil.copyfile(p,root/'conf'/p.name)
    for p in (R/'img/AstaBlackClover/conf').glob('cloud_messages*'):shutil.copyfile(p,root/'conf'/p.name)
    tree=E.parse(root/'conf/actions.xml');doc=tree.getroot();aliases={}
    def choose(image):
        n=image.lower()
        return seq['walk'][0] if 'walk' in n else seq['crouch'][0] if 'lie' in n or 'sprawl' in n else seq['sit'][0] if 'sit' in n else seq['hit'][0] if any(t in n for t in ['drag','wince','struggle']) else seq.get('surprised',seq['block'])[0] if 'surprise' in n else seq.get('laugh',seq['victory'])[0] if 'smile' in n or 'hug' in n else seq['block'][0] if 'hand' in n else seq['idle'][0]
    for pose in doc.iter(tag('Pose')):
        image=pose.get('Image','').lstrip('/')
        if image:
            aliases[image]=choose(image);pose.set('ImageAnchor',f'{size//2},{size}')
    def setanim(action,frames,velocity='0,0',ticks=4):
        for a in list(action.findall(tag('Animation'))):action.remove(a)
        a=E.SubElement(action,tag('Animation'))
        for i,f in enumerate(frames):
            attrs=dict(Image='/'+f,ImageAnchor=f'{size//2},{size}',Velocity=velocity,Duration=str(ticks))
            if action.get('Name') in ['Walk','Run','Dash','Creep'] and i in [0,2]:attrs['SoundEvent']='movement.footstep' if action.get('Name')=='Walk' else 'movement.run'
            E.SubElement(a,tag('Pose'),attrs)
    for action in doc.iter(tag('Action')):
        n=action.get('Name','')
        if n in ['Walk','Run','Dash','Creep']:
            speed=(3 if name=='Jerry' else 2) if n=='Walk' else (7 if name=='Jerry' else 5) if n=='Dash' else (5 if name=='Jerry' else 3) if n=='Run' else 1
            setanim(action,seq['walk'] if n=='Walk' else seq.get('sneak',seq['dodge']) if n=='Creep' else seq['run'],f'-{speed},0',4 if n=='Walk' else 3)
        elif n=='Stand':setanim(action,seq['idle'],ticks=9)
        elif n in ['Jumping','Falling']:setanim(action,seq['jump'][1:3] if n=='Jumping' else seq['jump'][2:3],ticks=5)
        elif n=='Bouncing':setanim(action,seq['jump'][3:]+seq['idle'][:1],ticks=4)
    # Explicit, manually selectable acting/combat animations. Frequency zero means no automatic action.
    actionlist=doc.find(tag('ActionList'));beh=E.parse(root/'conf/behaviors.xml');bl=beh.getroot().find(tag('BehaviorList'))
    for key,fs in seq.items():
        action=E.SubElement(actionlist,tag('Action'),dict(Name=name+'_'+key,Type='Animate',BorderType='Floor'))
        setanim(action,fs,ticks=5);E.SubElement(bl,tag('Behavior'),dict(Name=name+'_'+key,Frequency='0'))
    tree.write(root/'conf/actions.xml',encoding='utf-8',xml_declaration=True);beh.write(root/'conf/behaviors.xml',encoding='utf-8',xml_declaration=True)
    # Other behavior modes share actions and gain the same manual animations.
    for mode in ['CalmBehavior.xml','MischievousBehavior.xml','ProfessionalBehavior.xml']:
        p=root/'conf'/mode
        if p.exists():
            mt=E.parse(p);ml=mt.getroot().find(tag('BehaviorList'))
            for key in seq:E.SubElement(ml,tag('Behavior'),dict(Name=name+'_'+key,Frequency='0'))
            mt.write(p,encoding='utf-8',xml_declaration=True)
    for image,src in aliases.items():
        target=root/image;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(root/src,target)
    shutil.copyfile(root/seq['idle'][0],root/'Preview.png')
    manifest=dict(id=name,displayName={'Tom':'Tom','Jerry':'Jerry','ErenYeager':'Eren Yeager'}[name],version='0.2.0',author='ShinobiMate',description='Illustrated desktop actions and manual combat. Some acting transitions remain key-pose based.',preview='Preview.png',supportedLanguages=['ar','en'],supportsCombat=True,supportsCloud=True,supportsMood=True,minimumShinobiMateVersion='0.9.0',moodPoses=dict(NORMAL=seq['idle'][0],HAPPY=seq['victory'][0],ANNOYED=seq['hit'][0],SLEEPY=seq['sit'][0]),interactionPoses=dict(CLICK=seq['victory'][0],DOUBLE_CLICK=seq['block'][0],DRAG=seq['hit'][0],HOVER=seq.get('surprised',seq['idle'])[0]),idleActivities=dict(sit=seq['sit'][0],stretch=seq.get('stretch',seq['idle'])[0],look=seq['idle'][0]),socialPoses=[seq['victory'][0],seq['idle'][0]])
    (root/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8');audio(root,name)

def combat(name,seq,size,titan=False):
    root=R/'img'/name
    width=84 if titan else 30 if name=='Jerry' else 46
    height=204 if titan else 70 if name=='Jerry' else 100
    c=E.Element('combat',version='1',width=str(size),height=str(size),anchorX=str(size//2),anchorY=str(size),movementSpeed='2.6' if titan else '8' if name=='Jerry' else '5.8' if name=='ErenYeager' else '5')
    E.SubElement(c,'hurtbox',x=str(-width//2),y=str(-height-6),width=str(width),height=str(height))
    E.SubElement(c,'personality',aggression='.85',technique='.35',dodge='.34' if name=='Jerry' else '.12' if titan else '.22',block='.08' if name=='Jerry' else '.2',counter='.35' if name=='Jerry' else '.12',taunt='0',dodgeSpeed='12' if name=='Jerry' else '4' if titan else '8',jumpHeight='16' if name=='Jerry' else '0' if titan else '24',defenseCooldown='24',defenseCost='18',blockReduction='.55')
    animations=E.SubElement(c,'animations')
    allseq={**seq,'approach':seq['walk'],'charge':seq['punch'][:2],'rush':seq['run'],'dash':seq['run'],'recover':seq['recover']}
    for key,fs in allseq.items():
        a=E.SubElement(animations,'animation',id=key)
        for i,f in enumerate(fs):
            attrs=dict(image=f,ticks=str(4 if titan else 3))
            if key=='approach' and i in [0,2]:attrs['soundEvent']='titan.step' if titan else 'movement.footstep'
            E.SubElement(a,'frame',attrs)
    if name=='ErenYeager':
        trans=next(a for a in animations if a.get('id')=='transforming');trans.clear();trans.set('id','transforming')
        if not titan:
            for f in seq['transforming'][:3]:E.SubElement(trans,'frame',image=f,ticks='4')
            E.SubElement(trans,'frame',image='combat/human/hidden.png',ticks='14')
        else:
            for f in ti('roar'):E.SubElement(trans,'frame',image=f,ticks='3')
            E.SubElement(trans,'frame',image=ti('ground-smash')[2],ticks='4');E.SubElement(trans,'frame',image=ti('roar')[2],ticks='6')
    fx=E.SubElement(c,'effects')
    old=E.parse(R/'output/full-character-integration/backups/ErenYeager/conf/combat.xml').getroot()
    for effect in old.find('effects'):
        fx.append(effect)
        for frame in effect:
            p=frame.get('image');target=root/p
            if not target.exists():target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/'img/ErenYeager'/p,target)
    attacks=E.SubElement(c,'attacks')
    def move(id,kind,anim,damage,startup,active,recovery,reach,speed=0,knock=20,jump=0,hitbox=None,combo=False):
        st=id+'-start';ac=id+'-active';rec=id+'-recovery'
        base=seq.get(anim,seq['punch'])
        for key,fs in [(st,base[:2]),(ac,base[2:3] or base),(rec,base[3:] or seq['recover'])]:
            a=E.SubElement(animations,'animation',id=key)
            for f in fs:E.SubElement(a,'frame',image=f,ticks='3')
        a=E.SubElement(attacks,'attack',id=id,kind=kind,minRange='0',range=str(reach),damage=str(damage),cooldown='16' if name=='Jerry' else '28' if not titan else '42',startup=str(startup),active=str(active),recovery=str(recovery),speed=str(speed),projectileSpeed='0',knockback=str(knock),jumpHeight=str(jump),cost='10' if kind=='special' else '0',weight='1.2',animation=ac,chargeAnimation=st,recoveryAnimation=rec,effect='roar-wave' if anim=='roar' else 'impact',chargeEffect='dust',startupSound='charge',attackSound='titan.roar' if anim=='roar' else 'titan.punch' if titan else 'whoosh',impactSound='titan.smash' if anim=='smash' else 'impact',effectY='-70' if titan else '-45',effectX='45' if titan else '25')
        x,y,w,h=hitbox or ((25,-140,115,100) if titan else (9,-60,62,52) if name=='Jerry' else (14,-86,85,72))
        E.SubElement(a,'hitbox',x=str(x),y=str(y),width=str(w),height=str(h))
        if combo:
            for at in [0,6]:E.SubElement(a,'strike',at=str(at),duration='3',damage=str(damage),knockback=str(knock//2),animation=ac,windup=st)
    if titan:
        move('heavy-punch','melee','punch',18,14,5,18,170,knock=46)
        move('double-punch','melee','double',10,16,10,22,170,knock=38,combo=True)
        move('forward-rush','dash','punch',16,16,12,24,310,speed=8,knock=50)
        move('ground-smash','special','smash',22,22,5,26,190,knock=55,hitbox=(-95,-35,190,37))
        move('roar','special','roar',4,16,6,20,210,knock=24,hitbox=(-105,-170,210,166))
    elif name=='Tom':
        move('heavy-cartoon-punch','melee','punch',16,5,5,9,116,knock=35)
        move('kick','melee','kick',14,5,5,9,124,knock=33)
        move('pounce','dash','pounce',15,7,10,13,270,speed=8,knock=38)
        move('jump-attack','aerial','jump',12,6,10,11,160,speed=4,jump=34)
    elif name=='Jerry':
        move('short-punch','melee','punch',6,3,4,5,85,knock=13)
        move('fast-kick','melee','kick',7,4,4,6,94,knock=18)
        move('counterattack','melee','counter',10,3,5,6,102,speed=3,knock=22)
        move('escape-reengage','dash','dodge',5,4,8,7,230,speed=12,knock=12)
    else:
        move('punch-combo','melee','punch',5,4,10,8,100,combo=True)
        move('kick','melee','kick',8,5,4,9,115,knock=23)
        move('short-dash','dash','punch',6,4,8,10,235,speed=11)
        move('jump-attack','aerial','jump',9,6,10,12,145,speed=5,jump=36)
        E.SubElement(c,'transformation',profile='conf/titan.xml',transformOncePerBattle='true',transformationHpThreshold='0.5',prepareTicks='12',revealTick='26',landingTick='38',duration='48')
    E.indent(c);E.ElementTree(c).write(root/'conf'/('titan.xml' if titan else 'combat.xml'),encoding='utf-8',xml_declaration=True)

tseq['walk']=frames('titan-walk',range(8))
tseq['run']=tseq['walk']
for name,seq,size in [('Tom',tomseq,128),('Jerry',jseq,96),('ErenYeager',hseq,128)]:ordinary(name,seq,size);combat(name,seq,size)
combat('ErenYeager',tseq,256,True)
(R/'output/full-character-integration/motion-map.json').write_text(json.dumps(dict(Tom=tomseq,Jerry=jseq,ErenHuman=hseq,ErenTitan=tseq),indent=2))
print('Built illustrated packs, 8-character catalog, manual action behaviors, combat and original audio')
