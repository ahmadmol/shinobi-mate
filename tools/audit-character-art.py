"""Check deployable art, references, runtime sync, and protected project files."""
from pathlib import Path
from PIL import Image
import hashlib
import json
import xml.etree.ElementTree as E

root=Path(__file__).resolve().parents[1]
out=root/'output/character-art-refresh'
ns={'m':'http://www.group-finity.com/Mascot'}
protected=json.loads((out/'protected-sha256.json').read_text(encoding='utf-8-sig'))
for rel,digest in protected.items():
    assert hashlib.sha256((root/rel).read_bytes()).hexdigest().upper()==digest, ('protected file changed',rel)
results={'protected_files_verified':len(protected),'characters':{}}
for char in ('AstaBlackClover','NarutoShippuden','SasukeShippuden'):
    folder=root/'img'/char
    runtime=root/'Shimeji-ee/img'/char
    frames=list(folder.glob('*.png'))
    assert len(frames)==59, (char,len(frames))
    assert len(list(runtime.glob('*.png')))==59
    pixel_hashes={}
    bounds={}
    for file in frames:
        im=Image.open(file)
        assert im.format=='PNG' and im.mode=='RGBA' and im.size==(128,128),(char,file)
        a=im.getchannel('A')
        low,high=a.getextrema()
        assert low==0 and high>=250,(char,file,'alpha')
        assert all(a.getpixel(p)==0 for p in ((0,0),(127,0),(0,127),(127,127))),(char,file,'opaque corner')
        assert file.read_bytes()==(runtime/file.name).read_bytes(),(char,file,'runtime mismatch')
        bounds[file.name]=a.point(lambda p:255 if p>16 else 0).getbbox()
        digest=hashlib.sha256(im.tobytes()).hexdigest()
        assert digest not in pixel_hashes,(char,file,'duplicate pixels',pixel_hashes.get(digest))
        pixel_hashes[digest]=file.name
    for name in ('Stand','Smile','Blink','MouseSurprise','WalkLeft','WalkPass1','WalkRight','WalkPass2'):
        assert bounds[name+'.png'][3]>=127,(char,name,'inconsistent foot baseline')
    for name in ('GrabWall','ClimbWallExtend','ClimbWallContract'):
        assert bounds[name+'.png'][0]>=16,(char,name,'outside wall anchor')
    for name in ('GrabCeiling','ClimbCeiling1','ClimbCeiling2'):
        assert bounds[name+'.png'][1]<=1,(char,name,'ceiling hands not on anchor')
    tree=E.parse(folder/'conf/actions.xml')
    actions={a.get('Name') for a in tree.findall('.//m:Action',ns)}
    for node in tree.getroot().iter():
        if 'Image' in node.attrib:
            assert (folder/node.get('Image').lstrip('/')).is_file(),(char,node.attrib)
        if node.tag.endswith('ActionReference'):
            assert node.get('Name') in actions,(char,node.attrib)
    for file in (folder/'conf').glob('*.xml'):
        assert file.read_bytes()==(runtime/'conf'/file.name).read_bytes(),(char,file,'configuration mismatch')
        if file.name=='actions.xml':continue
        bt=E.parse(file)
        names={b.get('Name') for b in bt.findall('.//m:Behavior',ns)}
        for b in bt.findall('.//m:Behavior',ns):
            assert b.get('Action',b.get('Name')) in actions,(char,file,b.attrib)
        for b in bt.findall('.//m:BehaviorReference',ns):
            assert b.get('Name') in names,(char,file,b.attrib)
    if char!='AstaBlackClover':
        for file in (folder/'conf').glob('*.xml'):
            before=out/'originals/img'/char/'conf'/file.name
            assert file.read_bytes()==before.read_bytes(),(char,file,'existing behavior changed')
        assert {f.name for f in frames}=={f.name for f in (out/'originals/img'/char).glob('*.png')}
    results['characters'][char]={'png_count':len(frames),'unique_frames':len(pixel_hashes),'alpha_bounds':bounds}
changes={}
for base in ('img','Shimeji-ee/img'):
    for char in results['characters']:
        folder=root/base/char
        original=out/'originals'/base/char
        created=[]
        modified=[]
        for file in sorted(folder.rglob('*')):
            if not file.is_file():continue
            before=original/file.relative_to(folder)
            if not before.exists():created.append(file.relative_to(root).as_posix())
            elif file.read_bytes()!=before.read_bytes():modified.append(file.relative_to(root).as_posix())
        changes[base+'/'+char]={'created':created,'modified':modified}
(out/'changed-files.json').write_text(json.dumps(changes,indent=2),encoding='utf-8')
(out/'audit.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps({'protected_files_verified':len(protected),'characters':{c:{k:v for k,v in d.items() if k!='alpha_bounds'} for c,d in results['characters'].items()}},indent=2))
