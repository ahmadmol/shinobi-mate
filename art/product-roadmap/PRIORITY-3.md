# Priority 3 — Character manager and onboarding

Implemented a new bilingual card window with preview, installed/active counts, launch (including duplicate instances), desktop removal, details and pack reload. A CharacterService facade replaces reflective access from new UI. The original advanced chooser remains available. The manager hides to the tray when configured; its refresh timer stops while hidden/disposed.

Five-page onboarding supports language, first character, click/drag/menu guidance, cloud and manual combat. It persists completion only on the final button. Existing users with saved active packs are migrated without an unsolicited wizard. New distribution settings explicitly set completion false. Settings can replay the guide without replacing the active character selection.

Validation: clean Maven BUILD SUCCESS; CharacterCheck 20 assertions exercises the actual creation facade, five legacy packs, duplicate instance, admission limit/removal, RTL/LTR cards and all five wizard pages. UI snapshots inspected. Clipped English card buttons and an overstretched wizard language selector were identified and corrected; layouts use width-tracking cards and stacked controls.

Metadata currently uses the legacy adapter. Priority 4 replaces that source with validated manifests and extends details with capabilities. FREE/PREMIUM/OWNED/LOCKED models also belong there.

New source: character/{CharacterDescriptor,CharacterCatalog,CharacterService}, ui/{CharacterManagerWindow,OnboardingDialog}; tools/CharacterCheck.java. Modified: Main, SettingsDialog, Theme. Artifacts: screenshots/characters-ar.png, characters-en-GB.png, onboarding-ar.png.
