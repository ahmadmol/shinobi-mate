# Priority 1 — Combat visual polish: DONE

The working combat model and its 40 ms tick remain intact. Five independent transparent atlases now supply 16 dedicated body poses each. Pack XML points combat animations at those poses. Startup, active and recovery use separate references; impacts remain separately timed VFX. Charge and a four-tick hit flash augment the existing impact/slash/energy/dust/spark/projectile effects.

Original generated atlases, generation prompts, crop coordinates, common scale and direction corrections are preserved under `art/combat-polish`. Run the exporter using Windows PowerShell 5.1 (System.Drawing), not PowerShell 7. The single-pose stages form a 3-stage sequence; these are not 16-frame continuous attack animations. Oversized swords use a smaller common scale across the entire character to preserve full weapon visibility.

Validation: Maven clean package BUILD SUCCESS; CombatCheck 300,867 assertions, 72,814 ticks across 144 seeded matches; CombatVisualCheck 380,661 assertions validates 80 unique alpha sprites, horizontal gutters and all 24 attack stages. Native Windows checks: five packs, Arabic popup, manual pair, bystanders, cloud pin/position, click-through overlays, stop/disposal and all three representative pairings. Desktop and contact-sheet screenshots inspected. No combat logic regression found.

Native render clipping is confined to the selected monitor work area; effects are still capped, decoded once per match, queued/coalesced and explicitly disposed. Additional pose payload is 80 small PNGs and 40 small VFX PNGs; source atlases are development artifacts, excluded from distribution.

New files: `tools/export-combat-atlas.ps1`, `tools/CombatVisualCheck.java`, `art/combat-polish/prompts.json`, five `atlas.png` and `frames.json` files, `contact-sheet.png`, and `img/<pack>/combat/poses/*.png` plus charge/hitflash VFX (mirrored in the portable tree). Modified source: CombatProfile, CombatMatch; modified data: five combat.xml profiles in both trees.

![Combat poses](../combat-polish/contact-sheet.png)
