from pathlib import Path
import shutil
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
NS='http://www.group-finity.com/Mascot'
ET.register_namespace('',NS)
climbing={'ClimbWallExtend.png':2,'ClimbWallContract.png':3,'GrabWall.png':4,
          'ClimbCeiling1.png':5,'ClimbCeiling2.png':6,'GrabCeiling.png':7}
for name in ('ErenYeager','Tom','Jerry'):
    pack=ROOT/'img'/name
    for alias,frame in climbing.items():
        shutil.copyfile(pack/'art/locomotion-v2'/f'{frame:02}.png',pack/alias)
    if name=='ErenYeager':
        # Earlier aliases mixed an adult sheet with a chibi motion model.
        # Keep the whole everyday model coherent, including cursor-reactive Sit branches.
        for alias in pack.glob('*.png'):
            if alias.name in climbing: continue
            source=pack/'art/locomotion-v2/00.png'
            if alias.name.startswith('Sit'): source=pack/'art/locomotion-v2/01.png'
            elif alias.stem in ('NinjaJump','Falling','BounceExtend'): source=pack/'art/ErenHumanMotion/06.png'
            elif alias.stem in ('JumpStart','BounceCompress','CreepContract','PullUpPrepare'): source=pack/'art/ErenHumanMotion/17.png'
            elif alias.stem in ('CreepExtend','PullUpEffort','PullUpHalf'): source=pack/'art/ErenHumanMotion/22.png'
            elif alias.stem in ('DragWince','StruggleHard3'): source=pack/'art/ErenHumanMotion/18.png'
            elif alias.stem.startswith('Walk'):
                source=pack/'art/ErenHumanMotion'/f"{('WalkLeft','WalkPass1','WalkRight','WalkPass2').index(alias.stem):02}.png"
            shutil.copyfile(source,alias)
        for i in range(4): shutil.copyfile(pack/'art/locomotion-v2/00.png',pack/'art/idle'/f'{i}.png')
    path=pack/'conf/actions.xml'
    tree=ET.parse(path)
    for pose in tree.iter('{'+NS+'}Pose'):
        filename=pose.get('Image','').split('/')[-1]
        if filename in climbing:
            size=96 if name=='Jerry' else 128
            pose.set('ImageAnchor',f'{size//2},2' if 'Ceiling' in filename else f'8,{size}')
    if name=='Jerry':
        corner=next(a for a in tree.iter('{'+NS+'}Action') if a.get('Name')=='ClimbAlongWall')
        for action in corner:
            if action.get('Name')=='ClimbWall': action.set('TargetY','#{mascot.environment.workArea.top+96}')
            elif action.get('Name')=='Offset':
                action.set('X','${mascot.lookRight ? -48 : 48}')
                action.set('Y','-96')
    tree.write(path,encoding='utf-8',xml_declaration=True)

# Remove the walking trigger in all shipped packs as well as the runtime guard.
# Running, jumping, landing, voices and combat are deliberately retained.
for path in (ROOT/'img').glob('*/conf/actions.xml'):
    text=path.read_text(encoding='utf-8')
    text=text.replace(' SoundEvent="movement.footstep"','')
    path.write_text(text,encoding='utf-8')
print('Integrated coherent Eren model, wall/ceiling poses and removed walking triggers in every pack')
