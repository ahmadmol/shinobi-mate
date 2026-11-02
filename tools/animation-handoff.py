"""Evidence and exact commissioning spec; this script creates no character artwork."""
from pathlib import Path
from PIL import Image,ImageDraw
import xml.etree.ElementTree as E,json,hashlib
root=Path(__file__).resolve().parents[1];out=root/'output/performance';out.mkdir(exist_ok=True)
ns={'m':'http://www.group-finity.com/Mascot'}
packs=['NarutoShippuden','SasukeShippuden','GojoSatoru','SukunaRyomen','AstaBlackClover','Tom','Jerry','ErenYeager']
actions=['Stand','Walk','Run','Sit','ClimbWall','ClimbCeiling']
sheet=Image.new('RGB',(160*7,165*8),(232,235,240));draw=ImageDraw.Draw(sheet)
evidence={};rows=[]
for y,name in enumerate(packs):
    pack=root/'img'/name;tree=E.parse(pack/'conf/actions.xml'); clips={a.get('Name'):a for a in tree.findall('.//m:Action',ns)}
    records={};draw.text((3,y*165+4),name,fill='black')
    for x,action in enumerate(actions,1):
        poses=[p for p in clips[action].iter() if p.get('Image')]
        files=list(dict.fromkeys(p.get('Image').lstrip('/') for p in poses));hashes=set()
        for rel in files:
            with Image.open(pack/rel) as im: hashes.add(hashlib.sha256(im.convert('RGBA').tobytes()).hexdigest())
        # Last pose bypasses cursor-reaction branches for idle/sit; contact sheet is evidence only.
        pose=poses[-1] if action in ('Stand','Sit') else poses[0]
        with Image.open(pack/pose.get('Image').lstrip('/')) as im:
            image=im.convert('RGBA');dims=list(image.size);image.thumbnail((128,128));sheet.paste(image,(x*160+10,y*165+20),image)
        anchors=sorted({p.get('ImageAnchor') for p in poses});records[action]={'files':files,'pixel_distinct':len(hashes),'anchors':anchors,'canvas':dims}
        draw.text((x*160+4,y*165+150),action,fill='black')
    evidence[name]=records
    dims=records['Stand']['canvas'];ground=records['Stand']['anchors'][0];wall=records['ClimbWall']['anchors'][0];ceiling=records['ClimbCeiling']['anchors'][0]
    priority='High' if name in ['ErenYeager','AstaBlackClover','Tom','Jerry'] else 'Medium'
    rows.append(f'|{name}|{priority}|{dims[0]}×{dims[1]}|{ground}|{wall}|{ceiling}|')
sheet.save(out/'animation-contact-sheet.png')
(out/'animation-evidence.json').write_text(json.dumps(evidence,indent=2),encoding='utf-8')
text='''# Animation artwork handoff / النواقص الفنية

No new artwork was created in rc.3. XML references are complete; these are quality replacement/commissioning targets, **not missing files**. Pixel identities and file counts do not establish independent drawing, natural motion or rights. Reuse, translation, scaling or warping must not be credited as new art. The contact sheet samples existing poses for review; it is not a new sprite sheet.

## Existing integration coordinates

|Pack|Priority|Transparent RGBA canvas px|Ground anchor x,y|Left-wall contact anchor x,y|Ceiling contact anchor x,y|
|---|---|---|---|---|---|
'''+ '\n'.join(rows)+'''

Canonical source faces LEFT; the engine mirrors it for RIGHT. Floor motions retain the listed ground anchor, consistent anatomical scale and baseline. Wall art faces the LEFT wall (mirror for the right wall), hands/feet touching the wall anchor; ceiling art uses the listed hand-contact anchor, and needs both travel directions through mirroring. Do not reposition anchors to hide anatomical size changes. Wall/corner transitions must explicitly interpolate contact, then switch to the destination anchor at the authored contact frame.

## Exact replacement clip targets — apply to EACH of the eight rows above

Counts below are new independently drawn frames to commission for the complete replacement clip, not "extra frames" inferred by subtracting currently reused PNGs. Keep each pack's canvas/anchors above. Validate against that character's existing style. These targets are a proposed art contract, not a claim that every existing frame is unusable.

|Action/integration target|New authored frames|Required poses/contact|Direction / anchor|
|---|---:|---|---|
|Walk|6|two contacts, two passing, two extremes; no whole-body skating|left; ground|
|Run / Dash|8|anticipation/contact/flight/passing on both feet; separate cycle from Walk|left; ground|
|Jump + Fall + landing|8|2 crouch/takeoff, 2 ascent/apex, 2 descent, 2 compress/recover|left; ground, same baseline at landing|
|Stand → Sit|4|bend, lower, settle, seated hold|left; ground|
|Sit → Stand / StandUp|4|lean, push, extend, stable standing|left; ground|
|ClimbWall|6|alternating hand/foot reach/contact with convincing grip|left wall; wall contact|
|Wall → Ceiling / GrabCeiling transition|4|reach corner, transfer hand, tuck/rotate body, establish ceiling grip|left wall to ceiling; explicit contact switch|
|ClimbCeiling|6|alternating hand reach, grip and body follow-through|left travel; ceiling contact|
|Ceiling release / recovery|4|release, drop anticipation, falling pose, landing recovery|left; ceiling to ground contact|
|Hit → recover|4|impact compression, recoil, regain balance, ready|left; ground|

Asta currently uses the same four Walk paths for Run; a run cycle is still required. Eren, Tom and Jerry each reference four paths for walk/run; these existing motion/derived files do not count as newly authored art in this mission. Their wall/ceiling clips still cycle a small set of contact poses. Naruto, Sasuke, Gojo and Sukuna also need art review of reused action/attack clips; functional regression success is not aesthetic approval.

## Character-specific combat replacement targets

Combat clips use the actual combat profile canvas and ground anchor: Naruto/Sasuke 288×288 at144,288; Jerry96×96 at48,96; other humans128×128 at64,128; Eren Titan256×256 at128,256. The table above describes desktop locomotion only. All face LEFT (mirror RIGHT), retaining body scale, foot baseline and weapon hand. Distinct Start/Impact/Recovery artwork must be separately reviewable.

|Character / move|New authored frames|Phase split|
|---|---:|---|
|Naruto — punch / kick / Rasengan|6 per move|2 start + 2 impact + 2 recovery|
|Sasuke — sword / Chidori|6 per move|2 start + 2 impact + 2 recovery; sword extends toward target|
|Gojo — palm strike / technique|6 per move|2 start + 2 impact + 2 recovery|
|Sukuna — slash / heavy strike|6 per move|2 start + 2 impact + 2 recovery|
|Asta — sword swing / anti-magic rush|8 per move|3 windup + 2 contact + 3 follow-through; preserve sword arc/hand|
|Tom — swing / rush|8 per move|3 windup + 2 impact + 3 comic recovery|
|Jerry — dodge-counter / aerial strike|6 per move|2 anticipate + 2 action + 2 settle; silhouette remains readable at 96px|
|Eren human — punch / kick / jump attack|6 per move|2 start + 2 impact + 2 recovery|

Eren Titan uses **256×256 RGBA**, ground anchor **128,256**, canonical LEFT. Commission Titan walk 8, run/approach 8, heavy punch 9 (3/3/3), ground smash 10 (3 windup/2 impact/5 recovery), roar 8 (2 inhale/4 roar/2 settle), hit/recover 6 (2/4), KO 6. Transformation contract: 8 human 128×128 at 64,128 (anticipation/steam), followed by 8 Titan 256×256 at 128,256 (reveal/landing/settle); synchronize both forms to the same world foot coordinate. De-transformation: 4 Titan frames then 4 human frames at the respective ground anchors. Existing transformation/ability frames remain shipped; these are quality replacement targets, not implemented art. No Titan wall/ceiling gameplay is currently required by combat; do not add it only to fill this list.

Acceptance: render at 100%, 150%, 200%, both directions; inspect ground/contact anchors, anatomical size, alpha padding and weapon orientation, at actual 25Hz timing. A human artist or authorized rightsholder must approve quality and redistribution. Natural continuous wall-to-ceiling motion and broad aesthetic approval remain open.
'''
(out/'ANIMATION_ARTWORK_BACKLOG.md').write_text(text,encoding='utf-8')
print('Wrote per-pack coordinates, per-action frame contract, evidence JSON and existing-art contact sheet.')
