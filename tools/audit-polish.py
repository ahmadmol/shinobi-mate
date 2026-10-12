from pathlib import Path
from PIL import Image
import hashlib,json,xml.etree.ElementTree as E
root=Path(__file__).resolve().parents[1];out=root/'output/behavior-polish'
protected=json.loads((out/'protected-sha256.json').read_text())
assert all(hashlib.sha256((root/p).read_bytes()).hexdigest()==h for p,h in protected.items()),'Protected project file changed'
ns={'m':'http://www.group-finity.com/Mascot'};results={}
for char in ['NarutoShippuden','SasukeShippuden']:
 folder=root/'Shimeji-ee/img'/char;original=out/'originals'/char
 added=[];changed=[]
 for p in sorted(folder.rglob('*')):
  if not p.is_file():continue
  rel=p.relative_to(folder);before=original/rel
  if not before.exists():added.append(str(rel))
  elif p.read_bytes()!=before.read_bytes():changed.append(str(rel))
  if p.suffix=='.png':
   im=Image.open(p);assert im.size==(128,128) and im.mode=='RGBA',(char,p)
   assert im.getchannel('A').getextrema()[0]==0,(char,p,'no transparent pixels')
 t=E.parse(folder/'conf/actions.xml');actions={a.get('Name') for al in t.findall('m:ActionList',ns) for a in al.findall('m:Action',ns)}
 for a in t.getroot().iter():
  if a.tag.endswith('ActionReference'):assert a.get('Name') in actions,(char,a.attrib)
  if 'Image' in a.attrib:assert (folder/a.get('Image').lstrip('/')).exists(),(char,a.attrib)
 for p in (folder/'conf').glob('*.xml'):
  if p.name=='actions.xml':continue
  bt=E.parse(p);names={b.get('Name') for b in bt.findall('.//m:Behavior',ns)}
  for b in bt.findall('.//m:Behavior',ns):assert b.get('Action',b.get('Name')) in actions,(char,p,b.attrib)
  for b in bt.findall('.//m:BehaviorReference',ns):assert b.get('Name') in names,(char,p,b.attrib)
 assert not t.findall('.//m:ActionReference[@Name="Jumping"]',ns),'Unsafe legacy Jump remains referenced'
 results[char]={'changed':changed,'added':added,'total_pngs':len(list(folder.glob('*.png')))}
results['protected_files_verified']=len(protected)
(out/'changed-files.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results,indent=2))
