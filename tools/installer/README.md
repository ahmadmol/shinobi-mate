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
Expected output: `dist/ShinobiMate-Setup-0.9.5-rc.3.exe`; checksums are refreshed automatically.
Use `-Compiler '<path to ISCC.exe>'` if the compiler is installed elsewhere.

Inno Setup 6.7.3 is installed on the development machine. See
`../../output/lifecycle-qa/REPORT-0.9.4-beta.md` for actual execution evidence and limitations.

rc.3 updates and uninstall use native Inno/Winsock code: authenticated loopback STOP
for 0.9.4+, followed by a 20-second cooperative wait for exclusive existing-file
access. No files are written during this check. Loaded/busy/inaccessible resources
block replacement; reparse points block traversal. No PowerShell script or
execution-policy override is used by Setup. `request-shutdown.ps1` remains a
development diagnostic tool, not an installer dependency. No RmShutdown, taskkill,
Stop-Process or generic Java process termination is used.
Older releases require Tray > Exit and Retry; a failed preparation leaves the
payload untouched. `CloseApplications=no` prevents Inno's independent automatic
shutdown path, including its force mode. `RestartApplications=no` avoids automatic
respawn; the optional postinstall launch goes through the normal single-instance guard.
Setup holds a session maintenance mutex during replacement; 0.9.4+ launch exits
promptly while it exists. Native preflight checks every installed resource except
Inno's own uninstaller files, which Inno manages itself. Unexpected third-party
file owners are logged and block installation; they are never closed.

SetupLogging and UninstallLogging are enabled. Pass `/LOG="absolute-path.log"`
for deterministic QA logs, including STOP acknowledgement and blocked resource
paths. Silent failures return nonzero and preserve the profile. Startup deletion
checks the shortcut's actual target or a quoted legacy batch target and preserves
another installation's startup entry.

Latest native lifecycle evidence: `../../output/performance/logs/qa-native-*.log`
and `qa-native-results.txt`. A test-only compile can override `/DAppGuid` and
`/DAppProfile` while keeping the payload identical; this avoids overwriting the
user's real uninstall registration/profile. The shipped build uses the defaults.
The older check-install-lifecycle script targets the older PowerShell design.
Test agents are excluded from the release. Test the real identity/signed release
on clean Windows 10/11 machines separately.

The existing bundled OpenJDK 21 jpackage launcher/runtime are reused for portable packaging. Renamed launcher/config behavior is verified with a real runtime smoke test; source/tests/logs are never shipped.
