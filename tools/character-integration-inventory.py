from pathlib import Path
import json,hashlib,xml.etree.ElementTree as E
r=Path(__file__).resolve().parents[1];report={}
for name in ['Tom','Jerry','ErenYeager']:
    root=r/'img'/name;refs=[];combat=[]
    for filename in ['actions.xml','combat.xml']+(['titan.xml'] if name=='ErenYeager' else []):
        tree=E.parse(root/'conf'/filename)
        for elem in tree.iter():
            if elem.tag.rsplit('}',1)[-1]=='Pose' and elem.get('Image'):refs.append(elem.get('Image').lstrip('/'))
            if elem.tag=='animation':
                for f in elem.findall('frame'):refs.append(f.get('image'));combat.append(f.get('image'))
    for p in refs:assert (root/p).is_file(),(name,p)
    unique=set(refs);unique_art={p for p in unique if p.startswith('art/')}
    hashes={hashlib.sha256((root/p).read_bytes()).hexdigest() for p in unique_art}
    report[name]=dict(art_png_files=len(list((root/'art').rglob('*.png'))),used_unique_art_files=len(unique_art),used_distinct_art_pixel_files=len(hashes),combat_frame_references=len(combat),all_frame_references=len(refs),ordinary_actions=len(E.parse(root/'conf/actions.xml').findall('.//{http://www.group-finity.com/Mascot}Action')),sound_events=len(json.loads((root/'conf/sounds.json').read_text())['events']))
(r/'output/full-character-integration/inventory.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
