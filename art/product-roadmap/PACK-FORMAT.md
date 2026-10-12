# Character pack v1

The format is additive to working Shimeji folders. Five existing packs use root PNG sprites plus `conf/actions.xml`, `conf/behaviors.xml`, `conf/combat.xml`, localized cloud properties, `combat/poses/` and `combat/vfx/`. New packs can reference subdirectories such as sprites/ from action/manifest data. The engine never selects features by a Naruto/Sasuke/Gojo/Sukuna/Asta name.

`manifest.json` contains id, displayName, version, author, description, preview, supportedLanguages, supportsCombat, supportsCloud, supportsMood and minimumShinobiMateVersion. Optional maps moodPoses/interactionPoses/idleActivities and the socialPoses list declare PNG references. IDs are ASCII letters/digits/dash/underscore, at most 64 characters; installed folder name equals manifest id. Engine compatibility uses major.minor.patch versions.

Absent manifests use a legacy adapter; legacy actions and behaviors remain authoritative. This v1 reads **combat.xml**, preserving the already working combat engine. `combat.json` alone is unsupported and rejected when combat is declared. Store/access models are separate from these local data capabilities; no entitlement service is connected.

Import `.characterpack`, `.zip` or existing `.smp` mascot payloads through Character Manager → Import pack. The importer extracts into a private staging directory inside img, validates, presents metadata/preview, then moves it into place only on explicit confirmation. Cancel cleans the staging directory. Existing IDs are refused rather than overwritten. `.smp` global localization/settings are not written to engine files.

Only png/xml/json/properties/wav/txt/md payloads are allowed. Limits: 2,048 files/entries, 16 MiB/file, 128 MiB expanded data, 2,048 px per PNG dimension and 32 million decoded PNG pixels per pack. Validation rejects path traversal, absolute paths, Windows ADS/device names, case duplicates, symlinks/paths resolving outside the root, executables and XML entities. Extension handling is case-insensitive. XML actions are restricted to built-in engine action classes; engine parsing also disables external entities.

Rhino expressions use safe standard objects, restricted Java class access and a cumulative deadline. This is an in-process boundary, not an operating-system sandbox. Validation checks XML structure and references/capabilities; final action/behavior semantics are still checked by the existing engine during load. Imported packs containing unsupported legacy custom Java action classes are intentionally refused.

Custom cloud text is in a separate conf/custom-cloud.properties layer; it is never exported over pack text. XP/challenges use conf/progress.properties. Preserve these and conf/settings.properties when updating the application.
