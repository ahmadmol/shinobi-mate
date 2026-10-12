# ShinobiMate official icon integration

Built on 2026-10-12, version 0.9.3-beta.

Source: `C:/Users/molha/Downloads/Neon Chibi Ninja Chat Icon.png` (1254 × 1254, RGBA).
The artwork is preserved without cropping or background replacement. Lanczos resizing retains transparency.
The Windows ICO contains 16, 32, 48, 64, 128 and 256 pixel images.

## Added or modified project files

All paths below are relative to `D:/programm/Shimeji-ee_2.0.1_Windows_Portable`.

- `conf/ShinobiMate.ico` — new canonical Windows icon.
- `conf/icon.ico` — compatibility copy.
- `img/icon.ico` — compatibility copy.
- `img/icon.png` — transparent 256 pixel Java fallback.
- `src/main/resources/icons/ShinobiMate-16.png`
- `src/main/resources/icons/ShinobiMate-32.png`
- `src/main/resources/icons/ShinobiMate-48.png`
- `src/main/resources/icons/ShinobiMate-64.png`
- `src/main/resources/icons/ShinobiMate-128.png`
- `src/main/resources/icons/ShinobiMate-256.png`
- `src/main/java/com/group_finity/mascot/ui/AppIcon.java` — shared resource loader and window/tray selection.
- `src/main/java/com/group_finity/mascot/ui/Theme.java` — applies icons to product windows.
- `src/main/java/com/group_finity/mascot/Main.java` — official tray artwork, owner frame and tray menu icons.
- `src/main/java/com/group_finity/mascot/InformationWindow.java`
- `src/main/java/com/group_finity/mascot/SettingsWindow.java`
- `src/main/java/com/group_finity/mascot/imagesetchooser/ImageSetChooser.java`
- `src/main/java/com/group_finity/mascot/virtual/VirtualEnvironment.java`
- `tools/build-app-icon.py` — reproducible source conversion; requires Pillow.
- `tools/BuildPortableTool.java` — passes the canonical ICO to jpackage.
- `tools/build-portable.ps1` — ships ShinobiMate.ico and the PNG fallback.
- `tools/installer/ShinobiMate.iss` — SetupIconFile and explicit icons for Start Menu, desktop and startup shortcuts.
- `art/branding/ICON-INTEGRATION.md` — this report.

The existing optional desktop shortcut task is preserved. Start Menu and desktop shortcuts use the new icon when created by this Setup. The installer was built but was not installed as part of this task.

## Build results

- `target/ShinobiMate.jar`
- `output/release-image/ShinobiMate-Portable/` — rebuilt app image, including `ShinobiMate.exe` and `ShinobiMate.ico`.
- `dist/ShinobiMate-Portable-0.9.3-beta.zip`
- `dist/ShinobiMate-Setup-0.9.3-beta.exe`
- `dist/checksums.txt` — refreshed release hashes.
- `output/portable-package-info.json` — refreshed packaging metadata.

The previous portable image/ZIP was retained by the existing build script in `output/packaging-backups/20261012-163848-653/`.

## Validation

- Maven `clean package`: BUILD SUCCESS. Development JDK: Android Studio JBR 25.0.3, compiled with `--release 21`; packaged runtime remains OpenJDK 21.0.8.
- jpackage: exit 0.
- Inno Setup: successful compile.
- Checked all six ICO entries for exact dimensions and RGBA alpha range 0–255.
- Compared each embedded Windows icon resource in both ShinobiMate.exe and Setup against the corresponding canonical ICO bytes: all six entries match exactly.
- Verified the packaged JAR contains all six PNG resources and the shipped ICO matches the canonical source.
- Java check: JFrame/JDialog receive all six RGBA icons; tray source loads for the system tray size.

Validation scripts and logs, under `output/`:

- `VerifyAppIcon.java`, `VerifyAppIcon.class`
- `verify-icon-resources.py`
- `icon-maven-build.log`
- `icon-portable-build.log`
- `portable-package-tool.log`
- `icon-installer-build.log`
- `icon-verification.log`

Setup SHA-256: `7ed937ecdbfe74e1d3d025e2a690b6d4f75698408de0be01d2f18e982a0fffa1`.

No git add, commit or push commands were run. Existing unrelated working-tree edits were retained.
