# ShinobiMate

Latest local multi-monitor fix: `0.9.5-rc.3+monitor.1`. See [the reproduced falling/window collision defect and validation](MULTIMONITOR_HOTFIX.md). Artifacts were saved on C because D ran out of space; previous candidates are retained.

**ShinobiMate** is an open-source, dynamic Desktop Companion Engine that displays interactive characters on your desktop screen.

## Final acceptance review: 0.9.5-rc.3+ui.1 — local review build

[FINAL_RELEASE_GATE_CHECKLIST.md](FINAL_RELEASE_GATE_CHECKLIST.md) is the current acceptance decision. Three demonstrated Settings text-clipping defects were fixed for large fonts; no animation, combat or audio engine classes changed. The separate [UI review portable](dist/ShinobiMate-Portable-0.9.5-rc.3+ui.1.zip) and [UI review Setup](dist/ShinobiMate-Setup-0.9.5-rc.3+ui.1.exe) passed native UI and isolated installer checks. Original rc.3 artifacts and their evidence remain unchanged. The earlier 30-minute soak belongs to rc.3 and was not repeated for ui.1. Asset rights, upstream notices, trusted signing and production-identity clean-device acceptance still block production distribution.

## Current Windows candidate: 0.9.5-rc.3 — redistribution rights remain unresolved

The official project icon is `conf/ShinobiMate.ico`, with transparent 16, 32, 48, 64, 128 and 256 pixel images. It is embedded in ShinobiMate.exe and the Inno Setup installer, and used by application windows, the system tray and installer-created shortcuts.

Local artifacts: [Setup 0.9.5-rc.3](dist/ShinobiMate-Setup-0.9.5-rc.3.exe) and [portable ZIP](dist/ShinobiMate-Portable-0.9.5-rc.3.zip), with Java 21 included. The exact portable launcher was verified with bundled Java 21.0.8 in an isolated relocated profile. The native installer no longer depends on PowerShell or an execution-policy override. A separate-identity QA installer passed fresh install, active-app update/uninstall, data preservation and locked-file refusal. The existing real installation was preserved. These unsigned files have not been published; real-identity clean-device installation and redistribution rights remain release gates.

Current evidence report: [SHINOBIMATE_FINAL_PRODUCTION_QA.md](SHINOBIMATE_FINAL_PRODUCTION_QA.md). rc.3 shares monitor queries per engine frame, avoids unchanged native repaint requests, decodes manager previews off the EDT and immediately refreshes instance controls. Paired eight-character phases used about 26–34% less CPU while retaining 25Hz. The earlier 13.7Hz result did not reproduce in the new baseline; it is not presented as a controlled before/after gain. Full evidence and raw profiles are under `output/performance`.

Earlier finalization evidence and handoff: [FINAL_REPORT.md](output/finalization/FINAL_REPORT.md), [QA_MATRIX.md](output/finalization/QA_MATRIX.md), [HANDOFF.md](output/finalization/HANDOFF.md). Startup opens Character Manager with zero companions unless session restore is explicitly enabled. Combat remains manual. The eight packs are Naruto, Sasuke, Gojo, Sukuna, Asta, Tom, Jerry and Eren; Titan is Eren's form. Artwork redistribution rights and upstream licensing remain unresolved. Historical sections below apply only to their named older builds.

Build and verify with JDK 21: `tools/run-finalization-checks.ps1 -Jdk <JDK-directory>`. Add `-Native` for the native movement checks. Preserve private art locally with `tools/save-finalization-backup.ps1 -IncludePrivateAssets`; do not publish that archive.

## Key Features

- **Dynamic Character Packs:** Automatically detects and validates character packs placed in the `img/` directory. No hardcoded character lists!
- **Add & Reload Characters:** Easily add new character folders and reload character packs on-the-fly without restarting ShinobiMate.
- **Multilingual Support:** Fully localized in Arabic, English, and Chinese with RTL UI support.
- **DPI & Multi-Monitor Support:** Configurable menu scaling, FlatLaf themes and monitor-aware positioning. See the production audit for tested display configurations and native scaling limits.
- **Spiritual Cloud:** Optional per-mascot Arabic clouds with hover, click-to-pin, six-second message rotation and monitor-aware positioning. Add UTF-8 `conf/cloud_messages_ar.properties` to any pack. See [the Arabic implementation report](art/spiritual-cloud/README.md).
- **Product UI:** Arabic/English settings, character cards, first-run guide and shared dark/light themes. The legacy advanced settings and chooser remain accessible.
- **Usability:** Search/filter companions, responsive cards, aspect-safe onboarding previews, visible pending launches, settings controls that revert on save failure, and protection against accidental focus-session restart. See [the refinement report](art/product-roadmap/UX-REFINEMENTS.md).
- **Companion features:** Per-instance mood/mouse reactions, custom Unicode cloud messages, offline focus sessions, declared idle activities and friendly social gestures. Optional local cosmetic XP and daily/weekly challenges.
- **Safe packs:** Additive `manifest.json`, legacy folder adapter and staged `.characterpack`/`.zip` import with preview. HTTPS update metadata checking opens a release page only.

## ShinobiMate 0.9.0-beta — private Friends Beta candidate

Extract `dist/ShinobiMate-Portable-0.9.0-beta.zip` and run `ShinobiMate.exe`. The Java 21 runtime is included; users do not need Java, Maven, an IDE or the source folder. First launch selects a language and companion and opens the Character Manager. Manual combat uses right click → Combat → Choose opponent. Settings, custom messages, progression, imported packs and bounded logs are stored in `%LOCALAPPDATA%\ShinobiMate`. One application instance per user profile can contain multiple mascots.

The main intended distribution is `ShinobiMate-Setup-0.9.0-beta.exe`. It has **not been generated** because Inno Setup is absent. This candidate is **NOT READY** for Friends Beta distribution until the installer is compiled and tested on another Windows machine. See [the production audit](art/release/PRODUCTION-AUDIT-0.9.0-beta.md) and [installer requirements](tools/installer/README.md). No public release has been published.

Product implementation, phase status, screenshots, test results and limitations are in [the final product report](art/product-roadmap/REPORT.md). Exact file inventory: [FILES.csv](art/product-roadmap/FILES.csv).

From PowerShell at the repository root:

```powershell
& tools/check-product.ps1
& tools/build-portable.ps1 -SkipBuild
& tools/check-beta-startup.ps1
& tools/check-distribution.ps1 -Performance
& tools/build-installer.ps1
```

Set `JAVA_HOME` to a development JDK 21+ or pass `-Jdk` explicitly. `check-product.ps1` performs `mvn clean package` and engine/UI/security checks in separate test profiles. The packaging script produces a genuine jpackage app-image under `output/release-image/` and a versioned ZIP; it does not patch an existing JAR. `check-beta-startup.ps1` tests real first-run startup in Arabic/English, connected monitors, the actual production entry point and the profile lock from relocated copies with Java/Maven removed from PATH. These simulations are not a clean VM test.

The Inno Setup recipe is prepared; generating and verifying Setup.exe needs the external compiler described in [tools/installer/README.md](tools/installer/README.md). Update checking needs the publisher's official HTTPS metadata/release URL in Settings → About; no publisher URL is invented or automatic update installed.

## Quick Start (Building from Source)

Manual 1v1 combat supports Naruto, Sasuke, Gojo, Sukuna and Asta, with dedicated combat poses and stage-specific charge/impact/recovery:
right click a pet → Combat → Choose opponent. Packs provide `conf/combat.xml`;
battles never start automatically. See [the combat implementation report](art/combat/README.md)
for profile fields, move lists, asset limitations and reproducible checks.

### Requirements
- JDK 21 (the release target)
- Apache Maven (or bundled Maven in `tools/maven/apache-maven-3.9.9/bin/mvn`)

### Building
To clean and build the executable fat JAR:

```bash
mvn clean package
```

The compiled binary will be generated at:
```text
target/ShinobiMate.jar
```

### Default Character Packs
- `NarutoShippuden`
- `SasukeShippuden`
- `AstaBlackClover`

These three packs share 128×128 transparent chibi art. Editable atlas sources,
frame mappings, and the reproducible export workflow are in
`art/character-packs/README.md`.

Import character packs from the Character Manager or use Add in the legacy chooser. Imported packs live in `%LOCALAPPDATA%\ShinobiMate\packs`; bundled `img/` resources remain application files. Pack data allows resources and bounded legacy expressions; arbitrary scripts, executables and Java methods are rejected.

## License
The existing LICENSE is retained verbatim. Upstream licensing/attribution needs reconciliation; see [NOTICE.md](NOTICE.md). Dependency notices are preserved individually under `licenses/` in the distribution and Java runtime notices under `runtime/legal/`. The core engine's license does not grant rights to the eight anime/cartoon character packs. Asset permissions and exact upstream notices require review before wider redistribution or a public commercial release.
