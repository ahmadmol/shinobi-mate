"""Inventory referenced artwork honestly: byte/pixel uniqueness is not proof of authored animation."""
from pathlib import Path
from PIL import Image
import hashlib, json, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
out=root/'output/finalization'
result={}
rows=[]
for pack in sorted((root/'img').iterdir()):
    if not pack.is_dir(): continue
    paths=set(); actions={}; animations={}
    for xml in (pack/'conf').glob('*.xml'):
        tree=ET.parse(xml)
        for n in tree.iter():
            if 'Image' in n.attrib: paths.add(n.attrib['Image'].lstrip('/'))
            if n.tag=='frame' and 'image' in n.attrib: paths.add(n.attrib['image'])
            if n.tag.endswith('Action') and n.get('Name'):
                refs=[p.get('Image','').lstrip('/') for p in n.iter() if p.get('Image')]
                if refs: actions[n.get('Name')]=refs
            if n.tag=='animation': animations[n.get('id')]=[f.get('image') for f in n]
    hashes={}; dims={}; missing=[]
    for rel in sorted(paths):
        p=pack/rel
        if not p.is_file(): missing.append(rel);continue
        with Image.open(p) as im:
            im=im.convert('RGBA');digest=hashlib.sha256(im.tobytes()).hexdigest()
            hashes.setdefault(digest,[]).append(rel);dims[rel]=list(im.size)
    reused=[v for v in hashes.values() if len(v)>1]
    clips={name:{'references':len(refs),'distinct_paths':len(set(refs))} for name,refs in animations.items()}
    result[pack.name]={'referenced_files':len(paths),'unique_pixel_images':len(hashes),'missing':missing,'duplicate_pixel_groups':reused,'dimensions':dims,'combat_clips':clips,'actions':actions}
    art='Existing illustrated/derived art; repeated clips are reuse. No new art authored in this mission.'
    if pack.name=='ErenYeager': art+=' Human/Titan clips; wall/ceiling contact artwork remains incomplete.'
    rows.append(f'| {pack.name} | {len(paths)} | {len(hashes)} | {len(reused)} | {len(missing)} | {art} |')
(out/'animation-inventory.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
(out/'CHARACTER_ANIMATION_MATRIX.md').write_text('# Character animation matrix\n\nMeasured XML-referenced pixel identities, not artistic authorship. Distinct PNG bytes or pixel hashes do not prove new animation; shifted/scaled/derived poses can remain the same source art. No replacement geometric artwork or new frame art was created.\n\n| Character | Referenced files | Unique pixel images | Duplicate groups | Missing references | Art status |\n|---|---:|---:|---:|---:|---|\n'+'\n'.join(rows)+'\n\nDetailed clip/reference/dimension inventory: `animation-inventory.json`. Combat and action names may share frame art. Authentic wall/ceiling transition poses and broad manual aesthetic approval remain open, especially Eren. Render canvas checks do not establish constant anatomical height or natural motion.\n',encoding='utf-8')
print('\n'.join(rows))
