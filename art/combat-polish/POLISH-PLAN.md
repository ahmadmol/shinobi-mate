# Character & Combat Polish 0.9.2

Audit before implementation: current engine has once-per-activation damage, bounded effects,
per-move cooldowns, locked attack facing and dedicated 16-pose character atlases. Preserve these.

Defects: left projectile geometry ignores facing; defense is retried at impact; no timed parry;
attack animation uses state age and loops; hitstun can be replaced by impact-time defense;
victory/KO frame age does not advance; only fighter A's monitor is checked; cross-monitor
start clamps B into A's screen; repeated attacks have no history; combo IDs deal one hit;
knockback decay travels less than its configured displacement; impact has no hit pause;
audio infers events from VFX names and uses identical sine cues; HUD lacks names.

Animation plan: preserve the existing 128x128 / (64,128) feet anchor and left-facing art.
Add eight dedicated poses per character: combo second windup/contact, combo finisher
windup/contact, signature anticipation/contact, secondary technique anticipation/contact.
Use existing recovery, dodge, block, hit, KO and victory poses. Timelines are phase-local,
non-looping for attacks and terminal poses. Each combo contact has its own short active
window and damage ledger; startup, anticipation between contacts and recovery cannot hit.
Weapons belong to the hand in attack poses; thrown weapons are independent projectiles.

Combat changes: same-work-area admission; monitor-change cancellation on either fighter;
gentle personal space with per-body scaling; weighted recent history; early startup reaction
with heavy threat weighting; three-tick parry window; 80ms heavy impact pause in model ticks;
bounded original synthesized event variants on one audio worker; no new timers per battle.

Validation: geometry and interruption regressions, phase/asset validation, seeded complete
battles for all three requested pairs, native desktop rendering and visual review, audio
resource stress, Maven clean package, versioned portable and installer without replacing 0.9.0.
