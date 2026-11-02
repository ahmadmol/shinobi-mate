"""Apply reviewed combat/anchor fixes after base pack generation; never rescale an individual PNG."""
from pathlib import Path
import shutil
import xml.etree.ElementTree as E

ROOT = Path(__file__).resolve().parents[1]
NS = 'http://www.group-finity.com/Mascot'
E.register_namespace('', NS)

def combat(name, filename, changes):
    path = ROOT / 'img' / name / 'conf' / filename
    tree = E.parse(path)
    for selector, attrs in changes:
        node = tree.getroot() if selector == '.' else tree.find(selector)
        if node is None:
            raise ValueError(selector)
        node.attrib.update({k: str(v) for k, v in attrs.items()})
    E.indent(tree)
    tree.write(path, encoding='utf-8', xml_declaration=True)

combat('ErenYeager', 'titan.xml', [('.', dict(movementSpeed=4.6)),
    ("attacks/attack[@id='heavy-punch']", dict(startup=9)),
    ("attacks/attack[@id='double-punch']", dict(startup=10)),
    ("attacks/attack[@id='forward-rush']", dict(startup=10, active=16, speed=13)),
    ("attacks/attack[@id='ground-smash']", dict(startup=14)),
    ("attacks/attack[@id='roar']", dict(startup=10))])

combat('Tom', 'combat.xml', [
    ('personality', dict(aggression='.92', dodge='.14', block='.28', technique='.3')),
    ("attacks/attack[@id='heavy-cartoon-punch']", dict(damage=18, startup=6, recovery=11, cooldown=24)),
    ("attacks/attack[@id='kick']", dict(damage=16, startup=6, recovery=10, cooldown=24)),
    ("attacks/attack[@id='pounce']", dict(range=240, speed=12, startup=6, active=14, recovery=14, jumpHeight=18, weight='1.6'))])
combat('Jerry', 'combat.xml', [
    ('personality', dict(aggression='.72', technique='.6', defenseCooldown=20)),
    ("attacks/attack[@id='counterattack']", dict(range=150, speed=8, active=8, damage=8, cost=8)),
    ("attacks/attack[@id='escape-reengage']", dict(kind='retreat', range=140, active=12, weight='.45', cooldown=40))])

# All seated aliases use the shared-sheet-scale extraction, at the same feet pivot.
root = ROOT / 'img/ErenYeager'
for file in root.glob('Sit*.png'):
    shutil.copyfile(root / 'art/human-sheet/22.png', file)
# Eliminate the existing vertical breathing stretch for Eren's standing model.
for i in range(4):
    shutil.copyfile(root / 'art/human-sheet/00.png', root / f'art/idle/{i}.png')

# Border pivots are contact points, while combat continues to use its feet anchor.
# Dedicated climbing artwork is still missing; retain the existing art explicitly.
path = root / 'conf/actions.xml'
tree = E.parse(path)
for pose in tree.iter('{'+NS+'}Pose'):
    image = pose.get('Image', '')
    if any(x in image for x in ('GrabCeiling', 'ClimbCeiling')):
        pose.set('ImageAnchor', '64,24')
    elif any(x in image for x in ('GrabWall', 'ClimbWall')):
        pose.set('ImageAnchor', '44,128')
tree.write(path, encoding='utf-8', xml_declaration=True)
print('Applied Eren/Tom/Jerry balance, contact pivots and consistent sheet scale aliases')

# Later locomotion artwork supersedes the old adult-sheet aliases and contact pivots.
if (root / 'art/locomotion-v2/00.png').exists():
    import runpy
    runpy.run_path(str(ROOT / 'tools/integrate-locomotion.py'))
