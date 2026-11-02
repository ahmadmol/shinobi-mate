# SHINOBIMATE FINAL PERFORMANCE & RELEASE QA — 0.9.5-rc.3

Date: 2026-11-02. **Local RC for evaluation; redistribution / Public Production v1.0 NOT approved.**
ملخص: أُنجز تحسين فعلي للأداء والرسم والواجهة والمثبّت، مع اختبار Windows حقيقي و30 دقيقة تحمل. لا توجد رسومات فنية جديدة في هذا الإصدار. حقوق الأصول وترخيص المصدر الأصلي ما زالت غير محسومة؛ لا تنشر الحزم أو النسخ الاحتياطية الخاصة.

## 1. Performance root cause

The supplied historical 608-second result (13.7Hz, 0.94 logical cores, heap 230MiB, maximum sampled EDT ping 124.9ms) is retained in `output/refinement/native/soak.csv`. **The 13.7Hz slowdown did not reproduce in the new rc.2 baseline.** There is no contemporaneous JFR of that older slow run, so its complete root cause cannot honestly be assigned to one function. Host load, elapsed behavior mix and earlier JAR identity differ; those are possible confounders, not proven explanations.

New real-process JFR recordings did identify repeated native monitor work: each `MascotEnvironment.getFloor/getWorkArea` called `DesktopWorkArea.at`, which re-enumerated graphics devices, obtained bounds/insets and sometimes queried SPI/GetSystemMetrics. One frame performs many such checks per character. The natural-eight baseline recorded 69 engine samples inclusive of `DesktopWorkArea.at`; Walk-eight recorded 45. Walk-eight native monitor-bounds leaf samples fell from 41 to 4 after optimization. These are stack sample counts including native waits, **not percentages of CPU time**.

Rendering also requested an EDT repaint every render tick even for an unchanged native bitmap. The native window now retains its last successfully painted image/alpha/logical size/graphics configuration. Identical requests are omitted; native window movement retains its bitmap and AWT expose/resize still paints. A native regression proved 200 repeated requests did not repaint, while movement, frame, opacity, resize and show/hide still worked. The JNA UpdateLayeredWindow return type was corrected to BOOL/int so failed uploads are retried, following [Microsoft's API contract](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-updatelayeredwindow).

Animation updates, border/collision queries, rendering, repaint paths, image caches, audio threads and timers were reviewed. Existing bounded decoded/native-size image caches remain enabled; no image decoding was found in the recurring native paint path. Rhino scope/reflection and native window reshape remain secondary costs. Audio native samples include mixer waits and were not misclassified as CPU work. The engine still uses 40ms/25Hz; no timer resolution, logic target, combat function, cloud, audio or activity setting was reduced to manufacture a gain.

## 2. Before / after benchmark

Same Windows host, JBR 21.0.11, -Xmx512m, BALANCED, 100% character scale, enabled audio/cloud, isolated profiles, same unmodified `PerformanceHarness` class, sequential runs. The baseline JAR was copied and SHA verified before editing: `6f3a5400cd272a1486a6d686919d46833875f023c102338008c74c85a2ae463d`. Each phase warms up 3 seconds, then measures 10/20/30/40 seconds as recorded in CSV. JFR runs on the eight-character phases in both variants. The manager is hidden after startup. Production companion features may replace a requested initial action; these labels do not claim fixed deterministic motion for the entire phase. Random behavior, JIT, GC and other desktop processes limit precision; this is one paired run, not a statistical guarantee.

|Phase / initial action|Before Hz|After Hz|CPU before, cores|CPU after, cores|CPU change|
|---|---:|---:|---:|---:|---:|
|natural-8|24.991|25.003|0.353|0.244|-30.9%|
|Stand-0|25.056|24.948|0.051|0.065|+27.2%|
|Stand-1|24.964|25.011|0.164|0.161|-1.9%|
|Stand-8|25.005|25.026|0.276|0.183|-33.8%|
|Walk-1|24.954|25.009|0.161|0.140|-13.0%|
|Walk-8|24.996|25.000|0.402|0.298|-25.8%|

Raw CSV/JFR: `output/performance/before/` and `after/`; derived values: `benchmarks.json`; stacks: `profile-comparison.txt`. The eight-character phases used approximately 26–34% less process CPU in this pair and maintained 25Hz. The zero-character phase increased by ~0.014 cores, which is included rather than hidden. Short-run heap peaks did not materially improve (natural 147.2 →154.4MiB); no memory-reduction claim is made. Walk-eight maximum sampled EDT ping was 1.166 →1.316ms, so no across-the-board EDT improvement is claimed. Samples at one-second intervals do not bound every event latency.

## 3. Bugs fixed

- Shared immutable monitor/work-area snapshots, refreshed every production Windows engine tick; defensive rectangles prevent callers mutating the cache. A 100ms stale fallback serves stopped-engine/startup callers. Actual monitor hot-plug/mixed-DPI migration still needs device testing.
- Skipped identical layered-window repaint requests while preserving frame, opacity, size, configuration and exposure invalidation; native upload failure cannot mark the surface current.
- Character Manager preview ImageIO decoding and scaling moved to cancellable SwingWorkers, cached per pack/generation. Filter changes cannot deliver a stale icon into the wrong card; disposed/reloaded windows discard stale jobs.
- Coalesced active-instance notifications immediately update launch/removal controls, fixing the observed disabled Remove button after a rapid successful launch; the 750ms refresh remains as a fallback.
- Focused buttons/settings controls scroll into view; Ctrl+F search, Ctrl+, settings and Escape close/hide bindings added.
- Installer maintenance no longer executes PowerShell scripts or requests `ExecutionPolicy Bypass`. Native Winsock sends the existing authenticated loopback STOP command, then exclusive existing-file access checks wait for payload release. No process is killed, no security policy is changed, and reparse points block traversal. Startup deletion checks ownership so another installation's shortcut is preserved.

## 4. UI/UX verification

Real Main/Swing tests covered zero startup characters, all eight previews, search/clear/no-results, active/capability filtering, add three Eren instances, Remove One 3→2 and Remove All 2→0, all ten settings pages, Arabic RTL/English LTR, dark/light, 125% font setting and narrow 620px windows. Resize captures wait for asynchronous layout; the initial pre-settle capture is not treated as a product defect. Button text-width checks passed in the settled layout. The narrow viewport scrolls; full tall cards need scrolling.

`RefinementUiCheck` passed 53 assertions, including 24 theme refreshes without cumulative font growth and asynchronous appearance reload leaving the EDT responsive. Native card/settings captures are in `ui-rc3/`. The computer-use skill was used for a real keyboard Ctrl+F / Eren search and screenshot (`ui-final/sky-search.jpg`). Later the helper lost the correct foreground target; input stopped, so Settings-shortcut mouse/keyboard completion via that helper is **not** claimed. Its partial accessibility tree and DPI capture discrepancy limit that evidence. Native Swing screenshots and programmatic controls are separately identified. Broad 100/150/200% Windows display-DPI and mixed-monitor testing remain open; font scaling is not equivalent to OS DPI testing.

## 5. Animation and combat limits / eight-character status

No protected artwork/config/audio assets were modified in this pass and no new artistic frames were authored. Existing art remains reused where documented. Canvas/anchor tests do not prove anatomical scale or aesthetic quality. `ANIMATION_ARTWORK_BACKLOG.md` and `animation-evidence.json` give per-pack dimensions, floor/wall/ceiling anchors, directions and exact proposed replacement frame counts for every movement and key attack. They distinguish replacement commissioning targets from missing files. The contact sheet samples existing art only.

|Character|Functional regression|Remaining art / native qualification|
|---|---|---|
|Naruto|movement, combat, both directions/scales PASS|run reuses walk; natural corner/art review open|
|Sasuke|movement, weapon/combat regression PASS|run/contact/attack phase art review open|
|Gojo|movement, techniques regression PASS|ceiling art uses an inverted silhouette; contact quality needs review|
|Sukuna|movement, attacks regression PASS|ceiling/body contact and reused phases need art review|
|Asta|movement, sword/combat regression PASS|Run uses the same four Walk files; dedicated run/weapon arc art required|
|Tom|movement and deterministic combat PASS; two native Tom/Jerry bouts finished|derived/reused motion and comic attack/recovery need art review|
|Jerry|96px canvas, movement and combat PASS; same native bouts|small silhouette/contact/recovery art needs review|
|Eren|human/Titan deterministic tests PASS; native Eren/Naruto transformed, damaged opponent, finished and returned HUMAN|native run observed double-punch/forward-rush; every special move was not observed in that bout. Wall/ceiling/transition and Titan phase art remain limited|

The full suite passed 284,920 combat assertions; 240,806 repair assertions; 552,223 full-character assertions /200,698 combat ticks /143 transformations; 948,111 Eren assertions /170,893 fight ticks; 8,841 locomotion assertions; 638 professional audio assertions; plus runtime fault isolation, reload concurrency, layering, 298 refinement assertions, UI, 11 product checks and the bilingual startup/restore matrix. These large totals are automated checks, not manual visual approvals. Native forced stand/sit/wall/ceiling state entries passed 5,120 checks at each of 100% and150%, all eight, both directions. Continuous natural wall-to-ceiling transfer was **not verified**. Manual-only combat was preserved and no new characters/features were added.

## 6. Real native application QA

The final-code JAR SHA is `e4da420a430c781c3372dcde6f73ab3a94912febf66db36b7c018689a1209948`. The normal production engine completed an actual 1800-second eight-character soak; measured CSV elapsed 1818.5s, logic 24.993Hz, process CPU 0.158 logical cores, sampled peak heap 251.6MiB, Java threads 32–32, maximum sampled EDT ping 4.615ms. OS monitor peak working set 545.4MiB and private bytes 635.4MiB; these include native/JVM memory and differ from heap. Audio failures 0, peak voices 2, loops after final stop=0. Build/installer work occurred concurrently during the soak; this is a real loaded-desktop stability test, not a controlled paired benchmark. CSV updates were 15s apart and do not prove absence of sub-sample spikes. Source-run evidence: `soak-final/` and logs. The source QA harness uses the installed JDK, not the bundled EXE.

**Exact portable launcher separately tested**, unchanged EXE/cfg/JAR, relocated under C:, Java/Maven absent from child PATH and JAVA_HOME empty. Module inspection and test-only attachment confirmed bundled Java21.0.8 and its jvm.dll. First-run guide was completed via an attached QA JButton, with zero automatic characters; eight packs then launched through the production facade. Native cloud hover/fade/pin/exit/hide and nonfocusable overlay passed. Focus dialog start/countdown/pause/resume/stop passed. Audio MasterVolume23 persisted, failures0. The second exact EXE exited0, reopened a deliberately hidden manager, retained eight characters and one tray. Authenticated STOP released both launcher/JVM and exclusive EXE/JAR/jvm.dll/profile locks. Restart reopened with zero characters and MasterVolume23. Test agents never ship. Hearing quality/speaker output was not manually judged.

## 7. Final build / installer

Maven **BUILD SUCCESS**, Java21, shaded application and final rc.3 metadata. Portable ZIP CRC/content SHA checked against the tested payload; bundled legal notices/runtime legal files retained. No tests, source, logs or agent JARs are shipped. Inno Setup compiled the final recipe successfully.

Earlier rc.2 Application Control failure is historical. This pass's rc.3 Setup /HELP launched, but the first isolated install using the old maintenance design failed exit7: Windows PowerShell5.1 reported script execution disabled (Operational event4100). No policy changed. The dependency was removed rather than bypassed.

The **native-maintenance installer QA variant** has identical payload/recipe with a different AppGuid and AppProfile to protect the existing real 0.9.3 registration. Fresh install exit0; installed bundled EXE/zero-character startup PASS; update while running exit0 with authenticated STOP acknowledgement1 and observed JAR release; restart/preferences PASS; uninstall while running exit0; profile fixture retained; QA registration/payload removed. A held-JAR negative update refused exit7 after the bounded wait, with payload SHA unchanged and the lock owner still alive. Real startup shortcut, user settings SHA and 0.9.3 registration remained unchanged. Evidence: `logs/qa-native-*.log`, `qa-native-results.txt`, `installer-original-state.json`. This is **not** a claim that the final public AppId was installed over the user's original installation. Clean-device real-identity installation/upgrade/uninstall, optional delete-data UI, and Windows10/11 policy coverage remain release gates. The artifacts are unsigned.

- Portable: `dist/ShinobiMate-Portable-0.9.5-rc.3.zip` — SHA256 `f95d112b9dcaf52bfdf1c7555fb4942bc8c8761f19aca9f3ec4a85ae0fd10b31`
- Setup: `dist/ShinobiMate-Setup-0.9.5-rc.3.exe` — SHA256 `b42f4415d6ae91bc6c26ad10a7602f6ac3ef1a8410330788f77e5bcb0f147aa4`
- EXE: `output/release-image/ShinobiMate-Portable/ShinobiMate.exe` (also extracted C: copy)
- Independent copies and exact tested portable: `<LOCAL_USER>/ShinobiMate-performance-20261102/`

Native installer APIs follow [Winsock initialization](https://learn.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-wsastartup) and [CreateFile sharing/access semantics](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew). Inno supports ownership [Check predicates](https://jrsoftware.org/ishelp/topic_scriptcheck.htm) for [additional uninstall deletion](https://jrsoftware.org/ishelp/topic_uninstalldeletesection.htm).

## 8. Distribution blockers / decision

**No public Production v1.0 and no unrestricted friends distribution yet.** The eight character/voice/artwork provenance and redistribution permissions remain unverified; local manifest author strings are not proof. The preserved core LICENSE has unresolved upstream notice/lineage discrepancies (see NOTICE.md). Signing/clean-device real-identity installer QA and mixed-DPI/native corner-transition/art approval also remain. Nothing was uploaded to GitHub and Windows protection/Application Control policies were not altered. The best current outcome is a locally runnable, recoverable RC suitable for private evaluation by the owner; sharing protected packs requires rights clearance.

## 9. Preservation / Git

See `PRESERVATION.md` for final local commits, source/private/build-tool/QA backups, Git bundle verification, ZIP CRC/byte comparisons, SHA manifests and independent C: copies. Source backup alone excludes art/binary toolchain; restore it with the PRIVATE assets and build essentials plus portable runtime as documented. Pre-existing user artwork/config/runtime edits remain unstaged and were preserved privately. No force/reset/clean/push was used.

## 10. Changed files

Production: `pom.xml`; Main; DesktopWorkArea; WindowsEnvironment; WindowsTranslucentWindow; win/jna/User32; CharacterManagerWindow; SettingsDialog; Theme; `tools/installer/ShinobiMate.iss`. QA/handoff tooling: PerformanceHarness, PerformanceNativeCheck, PerformanceUiCheck, ReleaseAcceptanceAgent, profile-summary.py, performance-results.py, animation-handoff.py, monitor-native-process.ps1, write-performance-report.py. Documentation: README, installer README, this report and output/performance notes/matrix/guides. No new artwork, sound assets or combat balance changes.
