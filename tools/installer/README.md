# Reputable Windows installer toolchain

The installer recipe targets **Inno Setup 6.7.3**, with a minimum of 6.3 for the `x64os` architecture identifier. Install the official compiler (including ISCC.exe) from [JR Software's downloads](https://jrsoftware.org/isdl.php). The expected path is `C:\Program Files (x86)\Inno Setup 6\ISCC.exe`. No installer compiler is downloaded automatically and no substitute installer is created.

Vendor references: [compiler command line](https://jrsoftware.org/ishelp/index.php?topic=compilercmdline), [per-user privilege mode](https://jrsoftware.org/ishelp/index.php?topic=setup_privilegesrequired), [tasks](https://jrsoftware.org/ishelp/index.php?topic=taskssection), [shortcuts](https://jrsoftware.org/ishelp/index.php?topic=iconssection).

Build the filtered portable distribution first. The recipe packages `output/release-image/ShinobiMate-Portable`, restricts installation to Windows 10/11 x64, installs per user under LocalAppData/Programs, creates a Start Menu entry, offers unchecked desktop/startup shortcut tasks, and supplies a standard uninstaller. Settings, custom messages, progress, imported packs and logs live separately under `%LOCALAPPDATA%\ShinobiMate` and survive upgrades/uninstall by default. The uninstall dialog offers an unchecked Delete user data option; silent uninstall always preserves data. That optional deletion uses Inno's DelTree, which [does not traverse reparse points](https://jrsoftware.org/ishelp/topic_isxfunc_deltree.htm). The application is unsigned.

```powershell
# Development only: supply your own JDK 21+ path.
$env:JAVA_HOME = '<your JDK directory>'
& tools/build-portable.ps1
& tools/check-beta-startup.ps1
& tools/check-distribution.ps1 -Performance
& tools/build-installer.ps1
```

The release/version/publisher are read from Maven-generated `app/build-info.properties`.
Expected output: `dist/ShinobiMate-Setup-0.9.0-beta.exe`; checksums are refreshed automatically.
Use `-Compiler '<path to ISCC.exe>'` if the compiler is installed elsewhere.

The compiler is absent on the audited development machine. The recipe, including its
custom uninstall dialog, has **not been compiled or install/uninstall tested**.
After installing the compiler, test: fresh install, shortcuts, unchecked startup,
startup enable/disable through Settings, launch after install, update preserving
profile data, uninstall retaining data, explicit data deletion, and silent uninstall.
Then test Setup.exe on another Windows computer before calling the release ready.

The existing bundled OpenJDK 21 jpackage launcher/runtime are reused for portable packaging. Renamed launcher/config behavior is verified with a real runtime smoke test; source/tests/logs are never shipped.
