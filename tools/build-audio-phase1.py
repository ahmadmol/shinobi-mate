"""Reproducible original ShinobiMate SFX. No recordings, external samples, speech, or anime audio.
Standard library only; fixed seeds, short envelopes, mono PCM16/22050 WAVs.
Regenerates only Phase 1 audio assets and their audio annotations, preserving other pack data.
"""
from pathlib import Path
import array, hashlib, json, math, random, re, shutil, wave
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RATE = 22050

def synth(path, kind, identity=0, variant=0):
    durations={'step':.09,'run':.08,'ui':.065,'charge':.48,'spin':.32,'electric':.42,
               'electric_loop':.24,'energy':.38,'fire':.38,'ko':.38,'victory':.4,'impact':.19,
               'heavy':.28,'metal':.16,'draw':.20,'poof':.18,'decay':.17,'whoosh':.17,'sword':.19,'breath':.12,
               'rasengan_hit':.32,'electric_dash':.24,'electric_hit':.29,'parry':.11,
               'fire_charge':.42,'fire_release':.32,'fire_impact':.28,'block':.14}
    asset_seed=int(hashlib.sha256(path.name.encode()).hexdigest()[:8],16)
    dur=durations[kind];n=int(RATE*dur);rng=random.Random(1709+identity*59+variant*101+sum(map(ord,kind))+asset_seed)
    pcm=array.array('h');low=0;mid=0;phase=0;prev=0
    softer=.78 if identity==2 else 1
    for i in range(n):
        t=i/RATE;u=i/max(1,n-1);white=rng.uniform(-1,1)
        low+=.09*(white-low);mid+=.43*(white-mid);band=mid-low
        fade=min(1,i/110,(n-1-i)/180)
        env=math.sin(math.pi*u)**1.4
        if kind in ('step','run'):
            env=math.exp(-u*9)*min(1,i/35)
            sample=(.4*low+.25*band+.22*math.sin(2*math.pi*(120+variant*11+identity*15)*t))*env
        elif kind in ('whoosh','sword','draw','poof','breath'):
            env=math.exp(-((u-.38)/.24)**2)
            sample=(band*.9+low*.4)*env
            if kind in ('sword','draw'):sample+=.07*math.sin(2*math.pi*(1700+variant*190)*t)*math.exp(-u*5)
            if kind=='poof':sample+=low*.65*math.exp(-u*5)
            if kind=='breath':sample*=.6
        elif kind in ('impact','heavy','ko','metal','block','parry'):
            env=math.exp(-u*(8 if kind=='metal' else 6))*min(1,i/28)
            freq=65 if kind in ('heavy','ko') else 125
            sample=(.8*low+.6*band+math.sin(2*math.pi*(freq-35*u)*t)*.45)*env
            if kind=='metal':sample+=.17*(math.sin(2*math.pi*2317*t)+.4*math.sin(2*math.pi*3671*t))*math.exp(-u*8)
            if kind=='block':sample+=.16*math.sin(2*math.pi*760*t)*math.exp(-u*9)
            if kind=='parry':sample=.3*(math.sin(2*math.pi*3217*t)+.5*math.sin(2*math.pi*5171*t))*math.exp(-u*10)+band*.5*env
        elif kind in ('charge','energy','spin','rasengan_hit'):
            # Rotating chakra: band-limited air, accelerating swirl and harmonics, not an explosion.
            freq=220+variant*15+(110 if kind=='energy' else 260)*u
            phase+=2*math.pi*freq/RATE
            rotation=.55+.45*math.sin(2*math.pi*(13*t+19*t*t))
            sample=(.24*math.sin(phase)+.11*math.sin(phase*2)+.85*band)*rotation*env
            if kind=='rasengan_hit':sample+=(.7*low+.45*math.sin(2*math.pi*(90-35*u)*t))*math.exp(-u*6)*min(1,i/30)
        elif kind in ('electric','electric_loop','electric_dash','electric_hit'):
            # Compact crackles with electrical harmonics; seamless micro-loop faded at boundaries.
            crack=white if rng.random()<.095 else band*.2
            sample=(.32*crack+.13*math.sin(2*math.pi*840*t)+.13*math.sin(2*math.pi*2130*t))
            sample*=.55+.45*math.sin(2*math.pi*25*t)**2
            if kind!='electric_loop':sample*=env
            if kind=='electric_dash':sample+=.7*band*env
            if kind=='electric_hit':sample+=(.7*low+.45*math.sin(2*math.pi*(78-25*u)*t))*math.exp(-u*6)*min(1,i/30)
        elif kind.startswith('fire'):
            sample=(1.7*low+.25*white*(rng.random()<.025))*env
            if kind=='fire_charge':sample*=u
            if kind=='fire_release':sample+=band*.8*env
            if kind=='fire_impact':sample+=(low*.8+.4*math.sin(2*math.pi*(80-25*u)*t))*math.exp(-u*7)*min(1,i/25)
        elif kind=='decay':sample=(.5*band+.12*math.sin(2*math.pi*640*t))*math.exp(-u*7)
        elif kind in ('ui','victory'):
            sample=.18*(math.sin(2*math.pi*(740+variant*100)*t)+.25*math.sin(2*math.pi*1480*t))*math.exp(-u*7)
            if kind=='victory':sample+=.1*math.sin(2*math.pi*(880 if u>.5 else 660)*t)*env
        sample*=fade*softer
        pcm.append(int(max(-.95,min(.95,sample))*18000))
    path.parent.mkdir(parents=True,exist_ok=True)
    with wave.open(str(path),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(RATE);out.writeframes(pcm.tobytes())

def pack(root,identity):
    events={}
    def event(id,folder,name,kind,category='COMBAT',variants=1,volume=.65,cooldown=110,limit=2,priority=50,loop=False):
        paths=[]
        for v in range(variants):
            path=f'sounds/{folder}/{name}'+(f'_{v+1:02}' if variants>1 else '')+'.wav'
            synth(root/path,kind,identity,v);paths.append(path)
        events[id]={'category':category,'variations':paths,'volume':volume,'cooldown':cooldown,
                    'maxConcurrent':limit,'loop':loop,'priority':priority,'maxDurationMs':1800 if loop else 3000}
    event('movement.footstep','movement','footstep','step','MOVEMENT',4,.40,240,2,10)
    event('movement.run','movement','run','run','MOVEMENT',4,.45,170,2,12)
    for id,name,kind in [('jump','jump_whoosh','whoosh'),('air','air_whoosh','whoosh'),('landing','landing','impact'),
                         ('landingHeavy','landing_heavy','heavy'),('drag.start','drag_start','whoosh'),('drag.release','drag_release','poof')]:
        event('movement.'+id,'movement',name,kind,'MOVEMENT',volume=.40,cooldown=240,priority=25 if id=='landingHeavy' else 15)
    event('dodge','movement','dodge_whoosh','whoosh','MOVEMENT',2,.55,220,2,35)
    for id,name,kind,variants in [('combat.punch','punch_light','whoosh',2),('combat.kick','kick','whoosh',2),
                ('combat.body.light','hit_body','impact',2),('combat.body.heavy','heavy_hit','heavy',1),
                ('combat.start','melee_ready','whoosh',1),('block','block','block',1),('parry','parry','parry',1),
                ('combat.knockback','knockback','whoosh',1),('ko','KO','ko',1),('victory','victory','victory',1)]:
        event(id,'combat',name,kind,'CHARACTER' if id in ('ko','victory') else 'COMBAT',variants,
              volume=.7,cooldown=140 if id not in ('ko','victory') else 600,priority=85 if id in ('ko','combat.body.heavy') else 55)
    event('weapon.ready','weapons','weapon_ready','draw','WEAPON',volume=.5,cooldown=160)
    event('weapon.kunai.throw','weapons','kunai_throw','whoosh','WEAPON',2)
    event('weapon.kunai.impact','weapons','kunai_impact','metal','WEAPON',2,priority=65)
    event('weapon.shuriken.throw','weapons','shuriken_spin' if identity==1 else 'shuriken_throw','spin','WEAPON',2)
    event('weapon.shuriken.impact','weapons','shuriken_impact','metal','WEAPON',2,priority=65)
    if identity==1:
        event('vfx.clone.in','signature','clone_poof','poof','VFX',volume=.6,cooldown=250,limit=1)
        event('vfx.clone.out','signature','clone_poof_out','poof','VFX',volume=.4,cooldown=250,limit=1)
        for id,name,kind in [('charge','chakra_charge','charge'),('ready','rasengan_energy','energy'),
                ('dash','rasengan_dash','spin'),('impact','rasengan_impact','rasengan_hit'),('after','energy_release','decay')]:
            event('signature.rasengan.'+id,'signature',name,kind,'VFX',volume=.9 if id=='impact' else .7,cooldown=220,limit=2,priority=95)
    if identity==2:
        event('weapon.sword.draw','weapons','sword_draw','draw','WEAPON',volume=.6,cooldown=220)
        event('weapon.sword.swing','weapons','sword_swing','sword','WEAPON',3,.75,140,2,60)
        event('weapon.sword.impact','weapons','sword_impact','metal','WEAPON',2,.8,120,2,70)
        for id,name,kind in [('charge','chidori_charge','electric'),('loop','chidori_electric_loop','electric_loop'),
                 ('dash','chidori_dash','electric_dash'),('impact','chidori_impact','electric_hit'),('after','electric_decay','decay')]:
            event('signature.chidori.'+id,'signature',name,kind,'VFX',volume=.85 if id=='impact' else .65,cooldown=200,limit=2,priority=95,loop=id=='loop')
        for id,name in [('charge','fire_charge'),('release','fire_release'),('impact','fire_impact')]:
            event('signature.fire.'+id,'signature',name,'fire_'+('release' if id=='release' else 'impact' if id=='impact' else 'charge'),'VFX',volume=.75,cooldown=220,priority=80)
    if identity==0:
        for id,name,kind in [('combat.punch.heavy','punch_heavy_01','whoosh'),('impact','impact','impact'),('heavy','heavy','heavy'),
                 ('charge','charge','charge'),('chakra','chakra','energy'),('electric','electric','electric'),('fire','fire','fire'),
                 ('dark','dark','fire'),('whoosh','whoosh','whoosh'),('sword','sword','sword'),('grunt','generic_breath','breath')]:
            event(id,'combat',name,kind,'CHARACTER' if id=='grunt' else 'COMBAT')
        for index,id in enumerate(['ui','ui.click','ui.manager.open','ui.character.select','ui.character.spawn','ui.settings.open',
                                  'ui.combat.select','ui.combat.start','ui.combat.end']):
            event(id,'ui',id.replace('.','_'),'ui','UI',volume=.22,cooldown=180,limit=2,priority=20)
    # Legacy cues and optional reaction architecture fall back to the common pack.
    (root/'conf').mkdir(parents=True,exist_ok=True)
    (root/'conf/sounds.json').write_text(json.dumps({'version':1,'events':events},indent=2)+'\n',encoding='utf-8')
    return events

def annotations(root,identity):
    # Keep exact original XML formatting; only annotate pose markers and attack audio attributes.
    actions=root/'conf/actions.xml';s=actions.read_text(encoding='utf-8-sig')
    def action(m):
        text=m.group(0);name=re.search(r'Name="([^"]+)"',text).group(1)
        if name in ('Walk','Run','Dash'):
            id='movement.footstep' if name=='Walk' else 'movement.run'
            def pose(p):
                v=p.group(0)
                if '/WalkLeft.png' in v or '/WalkRight.png' in v:
                    v=re.sub(r'\sSoundEvent="[^"]*"','',v).replace('/>',f'SoundEvent="{id}" />')
                return v
            text=re.sub(r'<Pose\b[^>]+/>',pose,text)
        if name=='NinjaHop':
            poses=list(re.finditer(r'<Pose\b[^>]+/>',text))
            for index,id in reversed([(1,'movement.jump'),(4,'movement.air'),(8,'movement.landing')]):
                p=poses[index];v=re.sub(r'\sSoundEvent="[^"]*"','',p.group(0)).replace('/>',f'SoundEvent="{id}" />')
                text=text[:p.start()]+v+text[p.end():]
        return text
    s=re.sub(r'<Action\b[^>]*\bName="[^"]+"[^>]*>.*?</Action>',action,s,flags=re.S)
    actions.write_text(s,encoding='utf-8')
    combat=root/'conf/combat.xml';s=combat.read_text(encoding='utf-8')
    mapping={'light-combo':('combat.start','combat.punch','combat.body.light'),
       'kick-combo':('combat.start','combat.kick','combat.body.heavy'),
       'kunai':('weapon.ready','weapon.kunai.throw','weapon.kunai.impact'),
       'shuriken':('weapon.ready','weapon.shuriken.throw','weapon.shuriken.impact'),
       'shadow-clone':('charge','', 'combat.body.light'),
       'rasengan':('signature.rasengan.charge','signature.rasengan.dash','signature.rasengan.impact'),
       'aerial-ninja':('combat.start','combat.kick','combat.body.heavy'),
       'sword-combo':('weapon.sword.draw','weapon.sword.swing','weapon.sword.impact'),
       'aerial-sword':('weapon.sword.draw','weapon.sword.swing','weapon.sword.impact'),
       'counter':('weapon.sword.draw','weapon.sword.swing','weapon.sword.impact'),
       'fire-style':('signature.fire.charge','signature.fire.release','signature.fire.impact'),
       'chidori':('signature.chidori.charge','signature.chidori.dash','signature.chidori.impact')}
    def attack(m):
        text=m.group(0);head=text[:text.index('>')];id=re.search(r'\bid="([^"]+)"',head).group(1)
        for attr,value in zip(['startupSound','attackSound','impactSound'],mapping[id]):
            head=re.sub(fr'{attr}="[^"]*"',f'{attr}="{value}"',head)
        extras={'rasengan':{'readySound':'signature.rasengan.ready','afterSound':'signature.rasengan.after'},
                'chidori':{'loopSound':'signature.chidori.loop','afterSound':'signature.chidori.after'}}.get(id,{})
        for key,value in extras.items():
            head=re.sub(fr'\s{key}="[^"]*"','',head);head+=f' {key}="{value}"'
        text=head+text[text.index('>'):]
        if id=='sword-combo':
            strikes=list(re.finditer(r'<strike\b[^>]+/>',text))
            for index,p in reversed(list(enumerate(strikes))):
                kind='combat.kick' if index==2 else 'weapon.sword.swing';impact='combat.body.heavy' if index==2 else 'weapon.sword.impact'
                v=re.sub(r'\s(attackSound|impactSound)="[^"]*"','',p.group(0)).replace('/>',f'attackSound="{kind}" impactSound="{impact}" />')
                text=text[:p.start()]+v+text[p.end():]
        return text
    s=re.sub(r'<attack\b[^>]+>.*?</attack>',attack,s,flags=re.S)
    # Existing approach art has a repeating animation cycle; its marker is emitted once on entry.
    s=re.sub(r'(<animation id="approach">)(.*?)(</animation>)',
             lambda m:m.group(1)+re.sub(r'<frame\b[^>]+/>',lambda f:re.sub(r'\ssoundEvent="[^"]*"','',f.group(0)).replace('/>','soundEvent="movement.run" />'),m.group(2))+m.group(3),s,flags=re.S)
    combat.write_text(s,encoding='utf-8')

if __name__=='__main__':
    roots=[ROOT/'audio/common',ROOT/'img/NarutoShippuden',ROOT/'img/SasukeShippuden']
    counts=[]
    for identity,root in enumerate(roots):
        events=pack(root,identity);counts.append((root.name,len(events)))
        if identity:annotations(root,identity)
    assets=[]
    for root in roots:
        for wav in sorted((root/'sounds').rglob('*.wav')):
            assets.append({'path':str(wav.relative_to(ROOT)).replace('\\','/'),'sha256':hashlib.sha256(wav.read_bytes()).hexdigest(),
                           'source':'Original procedural ShinobiMate synthesis; tools/build-audio-phase1.py','external':False})
    dest=ROOT/'art/audio';dest.mkdir(parents=True,exist_ok=True)
    (dest/'asset-provenance.json').write_text(json.dumps(assets,indent=2)+'\n',encoding='utf-8')
    for name in ['NarutoShippuden','SasukeShippuden']:
        portable=ROOT/'Shimeji-ee/img'/name
        shutil.copytree(ROOT/'img'/name/'sounds',portable/'sounds',dirs_exist_ok=True)
        for file in ['sounds.json','actions.xml','combat.xml']:shutil.copy2(ROOT/'img'/name/'conf'/file,portable/'conf'/file)
    shutil.copytree(ROOT/'audio',ROOT/'Shimeji-ee/audio',dirs_exist_ok=True)
    print('Generated original SFX:',counts,'WAV files:',len(assets))
