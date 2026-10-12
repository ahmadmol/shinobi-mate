# Priority 4 — Packs, import and future content models: DONE

Added bounded JSON data parsing, versioned manifests, capability/reference/version validation, a legacy adapter and five additive manifests. Layout stays compatible: actions/behaviors and combat XML v1 are authoritative; sprites may be at the root or referenced below sprites/. The illustrative combat.json is not implemented and is explicitly rejected when it is the only combat configuration.

Imports accept .characterpack/.zip and adapt existing .smp payloads. They validate in a private staging folder, show actual preview/metadata, require the user's final UI confirmation, and rename the pack into img/. Existing pack IDs are never overwritten. Cancel/error removes staging. Legacy package installation now routes through the same importer; archive localization is kept inside the pack rather than merged into engine language files.

Limits: 2,048 entries, 16 MiB/member, 128 MiB expanded archive, 32 million decoded PNG pixels per pack. Reject traversal, absolute paths, ADS/device names, trailing dots/spaces, duplicate Windows paths, executables, symlinks, oversized members, XML entities and missing capabilities. Built-in action classes are the only loadable actions. Engine XML loading also disables external entities.

Rhino expression evaluation now uses safe standard objects, restricted Java class visibility and a cumulative evaluation deadline. This closes direct Java/Packages access and the prior non-cumulative loop timeout. Guidance checked against the [Rhino maintainers' security discussion](https://github.com/mozilla/rhino/discussions/1045). This is an in-process expression boundary; it does not promise OS isolation or immunity from every possible memory-exhaustion expression.

Validation: clean Maven BUILD SUCCESS; PackCheck 32 assertions covers the five manifests, actual staged import/install, refusal to overwrite, cleanup, old folder adaptation, traversal/absolute/ADS/device/executable/case duplicate/XXE/bomb/invalid IDs, Java exposure and infinite loops. The complete combat regression still passes 300,867 assertions. No regression in existing math/mascot expressions was observed.

Future content vocabulary: CHARACTER/ANIMATION/THEME/COMBAT_PACK; FREE/PREMIUM/OWNED/LOCKED; AVAILABLE/INSTALLED. Models only, no store, payments, backend or combat-stat bonuses.

New source: pack/{JsonData,PackPaths,PackManifest,PackValidator,SafePackImporter,SecureXml}, character/ContentAccess, ui/PackImportDialog; tools/PackCheck.java; five manifest.json files in both trees. Modified: CharacterCatalog, CharacterManagerWindow, Main, ActionBuilder, Script, MascotPackageManager.
