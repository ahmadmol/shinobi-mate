# Priority 5 — Mood and mouse interactions

Per-instance HAPPY/NORMAL/BORED/SLEEPY/EXCITED/ANNOYED models use monotonic idle times, a 20-second dwell, event holds, drag-burst counting, wake and explicit combat outcomes. Manifest mood/interaction references choose poses; core code contains no character-name branches.

Mouse hover/click/double-click/drag reactions share the existing six-pixel screen-coordinate gesture detector. Cloud pin/hide, hotspots, real dragging, right-click opponent selection and manual combat remain routed through Mascot. Cursor-facing/optional playful movement are settings. Neutral mood preserves the original resting pose. Reactions are limited to stationary supported surfaces; they do not replace jumping/climbing or combat frames.

Verification: clean Maven build plus MoodCheck and the actual Manager/Mascot integration checks. The model tests cover independent mood instances, idle/wake, slow transitions, repeat drags, win/loss, reaction expiry and threshold jitter. No new timer per mascot.

Limit: head tracking is supplied through pack LOOK_UP and cursor-facing references; arbitrary sprite deformation/eye IK is not implemented. Optional playful movement is small and bounded; existing Follow Cursor remains the approach behavior.

New source: mood/MoodState, interaction/InteractionState, character/CompanionFeatures; tools/MoodCheck.java. Modified: Mascot, CombatMatch, CombatEngine, PackManifest, SettingsDialog and five manifests.
