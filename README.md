# ShinobiMate

**ShinobiMate** is an open-source, dynamic Desktop Companion Engine that displays interactive characters on your desktop screen.

## Current Windows build: 0.9.3-beta

The official project icon is `conf/ShinobiMate.ico`, with transparent 16, 32, 48, 64, 128 and 256 pixel images. It is embedded in ShinobiMate.exe and the Inno Setup installer, and used by application windows, the system tray and installer-created shortcuts.

Download [ShinobiMate Setup 0.9.3-beta](dist/ShinobiMate-Setup-0.9.3-beta.exe) or [the portable ZIP](dist/ShinobiMate-Portable-0.9.3-beta.zip). Both include the Java runtime. The desktop shortcut is an optional installer task.

Maven packaging, the portable build and Inno Setup compilation succeeded on 2026-10-12. The embedded icon resources were verified in both executables; see [the icon integration report](art/branding/ICON-INTEGRATION.md). Earlier release notes below describe their respective historical candidates.

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
- JDK 21 or higher (JDK 25 recommended)
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
The existing LICENSE is retained verbatim. Upstream licensing/attribution needs reconciliation; see [NOTICE.md](NOTICE.md). Dependency notices are preserved individually under `licenses/` in the distribution and Java runtime notices under `runtime/legal/`. The core engine's license does not grant rights to the five anime-inspired character packs. Asset permissions and exact upstream notices require review before wider redistribution or a public commercial release.
