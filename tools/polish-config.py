"""Apply character-local, reversible behavior changes to the existing Shimeji configuration."""
from pathlib import Path
import xml.etree.ElementTree as E
import shutil, hashlib, json

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'output/behavior-polish'
NS='http://www.group-finity.com/Mascot'
E.register_namespace('',NS)
E.register_namespace('xsi','http://www.w3.org/2001/XMLSchema-instance')
def tag(n): return '{'+NS+'}'+n
def expr(s): return '#{'+s+'}'
GROUND='(mascot.environment.floor.isOn(mascot.anchor) || mascot.environment.activeIE.topBorder.isOn(mascot.anchor))'
NEAR='(Math.pow(mascot.environment.cursor.x-mascot.anchor.x,2)+Math.pow(mascot.environment.cursor.y-(mascot.anchor.y-68),2) < 120*120)'
INNER='mascot.anchor.x >= mascot.environment.workArea.left+64 && mascot.anchor.x <= mascot.environment.workArea.right-64'
SAFE=GROUND+' && '+INNER+' && mascot.anchor.y >= mascot.environment.workArea.top+152'
def pose(parent,image,duration=6,velocity='0,0',anchor='64,128'):
    return E.SubElement(parent,tag('Pose'),Image='/'+image+'.png',ImageAnchor=anchor,Velocity=velocity,Duration=str(duration))
def animation(action,frames,condition=None,anchor='64,128'):
    a=E.SubElement(action,tag('Animation'),**({'Condition':expr(condition)} if condition else {}))
    for image,dur,vel in frames: pose(a,image,dur,vel,anchor)
    return a
def replace_content(node):
    for c in list(node): node.remove(c)
def ref(parent,name,**attrs): return E.SubElement(parent,tag('ActionReference'),Name=name,**attrs)
def clamp(s,lo,hi): return 'Math.max('+lo+',Math.min('+hi+',('+s+')))'
def backup():
    dest=OUT/'originals'
    if dest.exists(): return
    for char in ['NarutoShippuden','SasukeShippuden']:
        shutil.copytree(ROOT/'Shimeji-ee/img'/char,dest/char)
    protected={str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (ROOT/'Shimeji-ee').rglob('*') if p.is_file() and ('img/NarutoShippuden/' not in p.as_posix() and 'img/SasukeShippuden/' not in p.as_posix()) and p.suffix in ('.png','.xml','.properties','.jar','.cfg')}
    (OUT/'protected-sha256.json').write_text(json.dumps(protected,indent=2))

def apply():
  for char in ['NarutoShippuden','SasukeShippuden']:
    folder=ROOT/'Shimeji-ee/img'/char/'conf'
    original=OUT/'originals'/char/'conf'
    tree=E.parse(original/'actions.xml'); root=tree.getroot()
    actions={a.get('Name'):a for al in root.findall(tag('ActionList')) for a in al.findall(tag('Action'))}
    al=root.findall(tag('ActionList'))[-1]
    for n in ['Walk','Run','Dash','FindAndHug','WalkWithIe','RunWithIe']:
        a=actions[n]; replace_content(a)
        speed=2 if n=='Walk' else 3 if n in ('Run','FindAndHug','WalkWithIe') else 4
        animation(a,[(x,6 if speed==2 else 4,f'-{speed},0') for x in ['WalkLeft','WalkPass1','WalkRight','WalkPass2']])
    for n in ['GrabCeiling','ClimbCeiling']:
        for p in actions[n].iter(tag('Pose')): p.set('ImageAnchor','64,0')
    for n in ['GrabWall','ClimbWall']:
        for p in actions[n].iter(tag('Pose')): p.set('ImageAnchor','16,128')
    actions['Jumping'].set('VelocityParam','8')
    # The built-in Jump overshoots vertically by half the horizontal distance.
    # Its former calls now use an explicitly bounded, short visual hop.
    jump_names=[n for n in actions if ('Jump' in n and n!='Jumping') or n in ('PullUp','PullUpShimeji','Divided')]
    for n in jump_names:
        a=actions[n];replace_content(a);a.set('Type','Sequence');a.set('Loop','false')
        ref(a,'NinjaJump',Condition='${'+SAFE+'}')
        ref(a,'VisibleReset');ref(a,'Falling');ref(a,'Bouncing',Condition=expr(GROUND))
    for n in ['ThrowIEFromLeft','ThrowIEFromRight','WalkAndThrowIEFromRight','WalkAndThrowIEFromLeft']:
        a=actions[n];replace_content(a);ref(a,'NinjaJump',Condition='${'+SAFE+'}');ref(a,'VisibleReset');ref(a,'Falling')
    # Fix typo, precedence, and keep floor/window movement targets inside the work area.
    for a in root.iter():
        for key,value in list(a.attrib.items()):
            value=value.replace('Math.random*100','Math.random()*100').replace('(Math.random()*mascot.environment.activeIE.width-128)','(Math.random()*Math.max(0,mascot.environment.activeIE.width-128))')
            if key=='TargetX' and value.startswith(('#{','${')) and a.get('Name') not in ('ClimbWall',):
                # Wall attachment needs the physical screen boundary; keep those targets.
                if value not in ('#{mascot.environment.workArea.left}','#{mascot.environment.workArea.right}'):
                    value='${'+clamp(value[2:-1],'mascot.environment.workArea.left+64','mascot.environment.workArea.right-64')+'}'
            if key=='InitialVX': value='${mascot.anchor.x < mascot.environment.workArea.left+96 || mascot.anchor.x > mascot.environment.workArea.right-96 ? 0 : '+clamp(value[2:-1] if value.startswith(('#{','${')) else value,'-2','2')+'}'
            if key=='InitialVY': value='${mascot.anchor.y < mascot.environment.workArea.top+152 ? 0 : '+clamp(value[2:-1] if value.startswith(('#{','${')) else value,'-6','12')+'}'
            a.set(key,value)
    # Correct the foot-anchor to ceiling-anchor transition, showing no floor pose above screen.
    a=actions['ClimbAlongWall'];replace_content(a)
    ref(a,'ClimbWall',TargetY='#{mascot.environment.workArea.top+128}')
    ref(a,'Offset',X='${mascot.lookRight ? -64 : 64}',Y='-128');ref(a,'GrabCeiling',Duration='12')
    ref(a,'ClimbCeiling',TargetX='${mascot.environment.workArea.left+64+Math.random()*Math.max(0,mascot.environment.workArea.width-128)}')
    for n in ['Fall','Thrown']:
        a=actions[n];a.insert(0,E.Element(tag('ActionReference'),Name='VisibleReset'))
    for n in ['FallFromCeiling','FallFromWall']:
        a=actions[n];replace_content(a);ref(a,'VisibleReset');ref(a,'Falling');ref(a,'Bouncing',Condition=expr(GROUND))
    a=E.SubElement(al,tag('Action'),Name='VisibleReset',Type='Embedded',Class='com.group_finity.mascot.action.Offset',
        X='${'+clamp('mascot.anchor.x','mascot.environment.workArea.left+64','mascot.environment.workArea.right-64')+'-mascot.anchor.x}',
        Y='${'+clamp('mascot.anchor.y','mascot.environment.workArea.top+128','mascot.environment.workArea.bottom')+'-mascot.anchor.y}')
    # Distance is measured from body/head center, not feet. Every idle reevaluates it each tick.
    for n in ['Stand','Sit','SitAndLookAtMouse']:
        a=actions[n]
        react=E.Element(tag('Animation'),Condition=expr(GROUND+' && '+NEAR))
        for im,d in [('MouseSurprise',10),('Blink',3),('Smile',25),('Blink',3)]:pose(react,im,d)
        a.insert(0,react)
    for n in ['Walk','Run','Dash','Creep']:
        actions[n].set('Condition',expr('!('+GROUND+' && '+NEAR+')'))
    for n in ['StandUp','SitDown','LieDown','SitWhileDanglingLegs','HoldOntoWall','HoldOntoCeiling']:
        for r in actions[n].iter(tag('ActionReference')):
            if 'Duration' in r.attrib:r.set('Duration','${60+Math.random()*60}')
    # Dragged is already an engine mouse handler. Preserve its foot/pendulum mechanics.
    for n in ['Pinched','Resisting']:
        for p in actions[n].iter(tag('Pose')):p.set('Image','/DragWince.png' if p.get('Image')!='/Stand.png' else '/MouseSurprise.png')
    a=E.SubElement(al,tag('Action'),Name='MouseEmote',Type='Stay',BorderType='Floor',Duration='48',Condition=expr(NEAR))
    animation(a,[('DragWince',12,'0,0'),('Blink',3,'0,0'),('MouseSurprise',12,'0,0')], 'Math.abs(mascot.environment.cursor.dx)+Math.abs(mascot.environment.cursor.dy)>18')
    animation(a,[('MouseSurprise',8,'0,0'),('Blink',3,'0,0'),('Smile',28,'0,0'),('Blink',3,'0,0')])
    a=E.SubElement(al,tag('Action'),Name='MouseReaction',Type='Sequence',Loop='false',Condition=expr(GROUND+' && '+NEAR))
    ref(a,'Look',LookRight='${mascot.anchor.x < mascot.environment.cursor.x}');ref(a,'MouseEmote')
    a=E.SubElement(al,tag('Action'),Name='CuteIdle',Type='Animate',BorderType='Floor')
    animation(a,[('Smile',25,'0,0'),('Blink',3,'0,0'),('Smile',25,'0,0'),('HandSign',25,'0,0'),('Blink',3,'0,0')])
    a=E.SubElement(al,tag('Action'),Name='NinjaHop',Type='Animate')
    animation(a,[('JumpStart',4,'0,0'),('NinjaJump',3,'0,-3'),('NinjaJump',3,'0,-2'),('NinjaJump',3,'0,-1'),('NinjaJump',3,'0,0'),('NinjaJump',3,'0,1'),('NinjaJump',3,'0,2'),('NinjaJump',3,'0,3'),('BounceCompress',4,'0,0'),('Smile',12,'0,0')])
    a=E.SubElement(al,tag('Action'),Name='NinjaJump',Type='Sequence',Loop='false',Condition='${'+SAFE+'}')
    ref(a,'NinjaHop');ref(a,'Stand',Duration='20')
    # Explicit mouse chase no longer uses unbounded dash destinations or window jumps.
    a=actions['ChaseMouse'];replace_content(a)
    ref(a,'VisibleReset');ref(a,'Falling')
    ref(a,'Walk',TargetX='${'+clamp('mascot.environment.cursor.x','mascot.environment.workArea.left+64','mascot.environment.workArea.right-64')+'}')
    ref(a,'MouseReaction',Condition=expr(GROUND+' && '+NEAR))
    E.indent(tree,space='\t');tree.write(folder/'actions.xml',encoding='utf-8',xml_declaration=True)
    for f in ['behaviors.xml','CalmBehavior.xml','MischievousBehavior.xml','ProfessionalBehavior.xml']:
        bt=E.parse(original/f);bl=bt.getroot().find(tag('BehaviorList'))
        # Preserve old behavior names for compatibility, but stop random unsafe window leaps/breeding.
        for b in bl.iter():
            n=b.get('Name','')
            if n in jump_names or 'ThrowIE' in n or n=='SplitIntoTwo':b.set('Frequency','0')
        for n,freq,condition in [('MouseReaction',800,GROUND+' && '+NEAR),('CuteIdle',90,GROUND+' && !'+NEAR),('NinjaJump',35,SAFE+' && !'+NEAR)]:
            E.SubElement(bl,tag('Behavior'),Name=n,Frequency=str(freq),Condition=expr(condition))
        for b in bl.iter(tag('Behavior')):
            if b.get('Name')=='SitAndFaceMouse':
                for nb in b.findall(tag('NextBehavior')):nb.set('Add','true')
        E.indent(bt,space='\t');bt.write(folder/f,encoding='utf-8',xml_declaration=True)
    print(char+': actions and all four behavior profiles updated')

if __name__=='__main__':
    import sys
    backup()
    if '--backup-only' not in sys.argv:apply()
