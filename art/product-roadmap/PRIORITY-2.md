# Priority 2 — Settings and performance

Verified subphase: typed bounded reads, atomic write-through persistence with Unicode/legacy keys preserved, eight bilingual settings sections, shared tokens, independent render gate, LOW/BALANCED/HIGH/CUSTOM profiles, cloud interval/text/opacity/enable, movement multiplier, maximum instance admission, scale/opacity reload, theme, combat enable/speed/HP/VFX, startup shortcut via existing Windows service, audio category volumes and mute. The old advanced settings remain accessible.

Maven clean package BUILD SUCCESS. SettingsCheck validates persistence, invalid values, gain/mute, language orientation and profile invariance: 500 identical simulation ticks in each profile; LOW renders 200 times, all default others 500. Combat regression: 300,867 assertions and 72,814 ticks still pass. Screenshots are rendered from real Swing dialogs, not mockups.

Performance profiles never gate environment or behavior/combat logic. LOW is 10 render FPS, 8 VFX, no cloud shadows. BALANCED is 25 FPS/24 VFX; HIGH 25/48. The original logic engine is 25 Hz, so HIGH does not fabricate 60 Hz interpolated body frames. Custom FPS is bounded to 5–25. Render queues coalesce. A visible HP/energy overlay is now click-through and monitor-clamped.

Audio uses four small, bounded, reusable synthesized placeholder cues, original PCM. Existing pack audio is volume-controlled too. Playback is optional if the audio device is unavailable.

Remaining dependent controls: idle activity level is consumed in Priority 6; minimize-to-tray is consumed by the new character manager in Priority 3. Therefore overall Phase 4 is PARTIAL until those integrations land. Phase 5's core behavior is verified; real hardware CPU/memory figures remain to be measured after full integration.

New source: settings/{SettingsStore,RuntimeSettings,SettingsService}, performance/{PerformanceProfile,RenderGate}, ui/{Theme,SettingsDialog}, audio/SoundSystem. Modified: Main, Manager, Mascot, animation/Pose, cloud/{CloudState,CloudPanel,SpiritualCloud}, combat/{CombatEngine,CombatEffects}, sound/Sounds. Check: tools/SettingsCheck.java.
