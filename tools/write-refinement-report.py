"""Local RC handoff; distinguishes engine/GUI/source QA from blocked unsigned Windows artifacts."""
from pathlib import Path
import csv, hashlib, json, subprocess, xml.etree.ElementTree as E
r=Path(__file__).resolve().parents[1]; o=r/'output/refinement'; d=r/'dist'
version=E.parse(r/'pom.xml').getroot().find('{http://maven.apache.org/POM/4.0.0}version').text
files=[d/f'ShinobiMate-Portable-{version}.zip',d/f'ShinobiMate-Setup-{version}.exe',r/'target/ShinobiMate.jar']
hashes={str(p.relative_to(r)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
(d/f'checksums-{version}.txt').write_text(''.join(f'{h}  {Path(p).name}\n' for p,h in hashes.items()),encoding='utf-8')
samples=list(csv.DictReader((o/'native/soak.csv').open(encoding='utf-8-sig')))
a,b=samples[0],samples[-1]; elapsed=float(b['seconds'])-float(a['seconds'])
cpu=(int(b['cpuNanos'])-int(a['cpuNanos']))/1e9/elapsed
logic=(int(b['engineTicks'])-int(a['engineTicks']))/elapsed
changed=subprocess.check_output(['git','diff','--name-only','ce5278d','--','src','tools','pom.xml','README.md'],cwd=r,text=True).splitlines()
changed += ['tools/RefinementCheck.java','tools/RefinementUiCheck.java','tools/RefinementDesktopCheck.java','tools/write-refinement-report.py','tools/BundledRefinementAgent.java']
commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=r,text=True).strip()
note=f'''# ShinobiMate {version} — refinement candidate

NOT READY for distribution: Windows Setup Application Control/signing and exact installer lifecycle remain blocked; character asset and upstream rights are unresolved. No public push/release was performed. Existing rc.1 artifacts are preserved.

Implemented: ten-section sidebar settings with RTL/LTR, dark/light button roles, percentage size/opacity controls applied off EDT, responsive character cards, remove-one vs remove-all, independent per-pose anchors with shared bounded bitmap cache, consistent appearance reload lock order, audio flush/close race and cleanup isolation, melee rear-facing guard, UI overlap protection for companions/cloud/VFX. No new character art or audio files, no balance retuning or framework migration.

تم تنفيذ تحسينات فعلية للواجهات والحركة والتزامن والصوت والقتال. الإصدار مرشح محلي قابل للبناء والتطوير، لكنه غير معتمد للتوزيع بسبب الحماية وحقوق الأصول وعدم اكتمال اختبار المثبّت والإزالة. لا تعطّل حماية Windows.
'''
(d/f'RELEASE_NOTES-{version}.md').write_text(note,encoding='utf-8')
report=f'''# SHINOBIMATE FINAL PRODUCTION QA — {version}

Date: 2026-11-02 (Asia/Damascus). Workspace: `{r}`. Branch: `codex/refinement-20261102`.

**Decision: NOT READY — BLOCKERS: Windows signing/Application Control, exact installer lifecycle, asset/upstream license clearance.**

أُنجزت تحسينات كود وواجهات واختبارات فعلية، وليس تقريرًا فقط. بقيت النسخ القديمة وبيانات المستخدم محفوظة. لا نشر أو Push أو تغيير لسياسات الحماية. هذا التقرير يفصل نجاح الكود عن اعتماد ملفات Windows للتوزيع.

## 1. Initial Audit Findings

Started from rc.1, HEAD ce5278d, with numerous earlier unstaged artwork/runtime changes. Fresh PRIVATE snapshot was made before edits and copied to `C:/Users/molha/ShinobiMate-refinement-20261102`. Previous source/history/asset backups remain intact. Java 21 + Swing + Maven retained. No dependency changes or broad engine rewrite.

Confirmed: crowded nine-tab Settings; ambiguous remove-all button; PNG cache stores one anchor despite five packs reusing BounceCompress with anchors64,104 and64,128; reloadAppearance still inverted Main/list lock order; flush could return an uncompleted future if close raced control submission; failing voice close could skip subsequent resource cleanup. Desktop review also exposed weak button contrast, clipped two-button rows at large fonts and incomplete topmost-only control protection.

## 2. Root Causes

| Severity | Root cause | Resolution |
|---|---|---|
| P1 | Pose anchor bundled into first cached PNG entry | AnimationBuilder passes scaled anchor to each Pose; weak geometry wrappers share native pixels without pinning evicted images |
| P1 | reloadAppearance took list lock before synchronized Main loader | synchronized Main entry before list lock; appearance/reload UI work runs off EDT |
| P1 | Audio close rejects control task silently; cleanup stops on first voice exception | observable control admission, terminal flush result, per-voice cleanup isolation |
| P1 | UI topmost flag alone does not guarantee controls stay unobscured | intersecting companion/cloud/VFX windows temporarily hidden while controls show; behavior continues and visuals resume outside controls |
| P2 | Large-font card minimum ignored actual button text width | column count uses current button font metrics; primary/secondary colors refreshed per theme |
| P2 | Font scaling derived from potentially previously scaled Label font | scale from absolute16pt base; repeated-theme test |
| P2 | Bidi isolate characters rendered as boxes on host font | remove unsupported isolate glyphs from summary |

## 3. Bugs Fixed

Implemented the rows above. Wide non-projectile melee swings also reject targets behind locked attack facing. Projectiles retain launch trajectory. Existing hit-once, startup/active/recovery, HP, balance profiles and manual-only admission remain. Removed two provably write-only startup fields. Intermediate compile errors in alignment and a test-only inherited Window name were corrected before the successful build; no failed check is counted as PASS.

## 4. UI/UX Improvements

Ten sidebar sections: General, Characters, Appearance, Combat, Audio, Cloud, Performance, Focus, Startup, About. Keyboard-selectable sidebar, scrollable pages, Arabic on the right, English on the left; clear labels and accessible names. Size/opacity show percentages; expensive updates disable only their control while a SwingWorker applies them. Five separate audio sliders/mute persist. Idle activity uses Calm/Balanced/Playful labels. Existing advanced settings remain available.

Character Manager: illustrated aspect-safe136px previews, accent primary buttons, active counts, search/filter, responsive1–3 columns based on actual text metrics, separate Remove one / Remove all (never deletes a pack), empty state, cleaner metadata and asynchronous pack reload. Dark/light and RTL/LTR actual Swing screenshots are under `output/refinement/screenshots` and native directories. UI icons remain embedded; no emoji added.

## 5. Animation Improvements

Real root-cause anchor correction, not per-frame resizing. Tested eight existing pack PNGs at75/100/150% and both facings, floor→ceiling→floor with the same native bitmap and distinct anchors. Five real existing BounceCompress definitions demonstrate the cache conflict. Weak pose wrappers preserve bounded bitmap retention. Native state-entry tests:5120 samples each at100/150%, all eight, both facings, ten repetitions of Stand/Sit/Wall/Ceiling. Those run before the last UI-only corrections; no motion/anchor code changed afterwards.

PARTIAL: no new authored artwork; repeated/derived images are not described as new animation. Natural connected wall/ceiling transitions, anatomical consistency and foot sliding still need art review, especially Eren. Fixed canvas size does not prove perfect animation. No geometric mascot replacements.

## 6. Combat Improvements

Rear-facing melee guard and two-direction wide-hitbox regression added. All prior production engine tests run again: startup/active/recovery, collision, once-per-hit, miss without impact, cooldown, projectiles, separation, bounds, stop/rematch. Manual combat policy unchanged. Distinct existing personalities retained; no forced50% tuning. Model samples: Eren/Naruto17/96, Eren/Sasuke41/96, Eren/Tom74/96; Tom/Jerry52/160 vs108/160. Jerry/Sasuke0/72 in separate regression remains a balance weakness.

## 7. Eren Improvements

Preserved HUMAN→TRANSFORMING→TITAN in one Mascot. Existing fixes are validated, not falsely claimed as new art/balance work. All five Titan moves contact in both directions at100/150%; transformation HP is not refilled. All natural transformations in required model samples land damage (96/96,96/96,95/95). Real normal-engine Eren/Naruto completed with Titan damage and moves double-punch/forward-rush/heavy-punch, then reset. Ground Smash/Roar completion is covered by model/contact tests; this native round did not show every move. Native final rerun evidence is logged separately.

## 8. Audio Improvements

1800 concurrent flush/close calls; failed voice close no longer prevents second voice/backend/cache cleanup. Existing ProfessionalAudioCheck638 assertions plus AudioCheck cover mixing limits, loop cancellation, PCM cache/device closure, attack timing/miss silence and five slider persistence. No show voices/music or new sound files. Existing walking footsteps remain intentionally silent; run/jump/land/drag/combat cues retained. No subjective listening approval claimed.

## 9. Performance Results

Continuous normal-engine soak: {elapsed:.2f}s measured after native workload (600s requested). Exact JAR SHA before final UI-only corrections: `{(o/'native/soak-jar-sha256.txt').read_text().strip()}`. Eight companions, explicit controlled fights beforehand, no automatic combat; Java threads {a['javaThreads']}→{b['javaThreads']}; displayable windows {a['displayableWindows']}→{b['displayableWindows']}; audio failures{b['audioFailures']}, final voices{b['voices']}/loops{b['loops']}. Peak heap{max(int(s['heapBytes']) for s in samples)/1048576:.1f}MiB; heap GC fluctuations are not retained-heap leak proof. Average process CPU{cpu:.2f} logical cores; observed logic rate{logic:.1f}ticks/s under concurrent host load, below intended25Hz. Maximum sampled EDT ping{max(float(s['edtMillis']) for s in samples):.1f}ms. These are real host observations, not clean benchmarks. No30–60minute claim; longer profiling remains open. Final-code short native rerun is separate from this pre-final-UI soak.

## 10. Character-by-Character Status

| Character | Verified function | Remaining |
|---|---|---|
| Naruto | movement/anchors/combat/weapons regression | art/balance review |
| Sasuke | movement/anchors/sword/projectiles/counter regression | very strong vsJerry |
| Gojo | movement/anchors/defense/energy regression | wider native art review |
| Sukuna | movement/anchors/slash/combat regression | wider art/balance review |
| Asta | movement/anchors/heavy sword regression | wider art/balance review |
| Tom | movement/pounce/four native cartoon rounds across this session's runs | Jerry-favored matchup |
| Jerry | movement/dodge/counter/retreat regression | Sasuke-favored matchup |
| Eren | human/Titan/5moves/contact/reset/rematch | connected climbing art and uneven matchups |

## 11. Automated Test Results

PASS: RefinementCheck298 assertions (anchors/audio race/removal/facing). PASS existing Combat284920, RepairCombat240806, FullCharacter552223, Eren948111, Locomotion8841, ProfessionalAudio638. PASS11 product checks including malformed/corrupt settings, malicious pack traversal/ADS/device names/XXE/bounds, RTL/LTR import UI, Focus/Cloud and persistence. PASS11 bilingual startup/restore modes, second JVM activation, authenticated STOP, concurrent loading/appearance reload and fault injection. Final UI/font/contrast/async/masking tests and final regression logs under output/refinement/logs distinguish last build from earlier runs. Maven itself has no Surefire suite; these standalone checks are explicitly executed.

## 12. Windows Native Test Results

Actual Main, Swing windows, native sprites, normal engine thread:0/1/8companions, dark/light RTL/LTR, explicit Eren/Naruto and two Tom/Jerry fights, ten-minute soak, graceful exit. Final code also repeated this native workload plus a30-second soak; Ground Smash was observed in its Titan round. RefinementUiCheck passed53 assertions and24 repeated theme refreshes; final masking/facing/anchor/audio tests passed. Native forced motion all eight at two scales also passed. Installed JBR21.0.11 used for long native engine checks. The exact final unmodified portable EXE also ran from C: with Java/Maven removed from PATH; loaded bundled Java21.0.8 was verified. Test-only attachment completed first-run guide (zero companions), manually summoned eight and inspected manager/tray. A second exact EXE exited with one launcher/JVM pair, followed by authenticated STOP and exclusive file-lock checks for JAR/EXE/jvm.dll/profile lock. logs/bundled-acceptance.txt. Native combat on the bundled runtime was not separately repeated. Computer-use screenshot review confirmed the final actual portable manager with eight active companions and no overlays over its controls. Coordinate input did not hit the intended Settings button at host DPI, so manual navigation acceptance remains PARTIAL; programmatic GUI actions are identified separately. No physical second monitor or OS DPI switch; app scaling tests are not physical DPI tests. No new video recorded.

## 13. Installer Status

Portable ZIP with bundled Java and Inno Setup are built. Exact final artifact attempts, signatures and Code Integrity evidence: output/refinement/logs/final-security.txt and final-code-integrity.txt. If blocked, install/upgrade/uninstall are **BLOCKED/NOT TESTED**, never PASS from compile. No Windows policies were changed and no original installed app/user profile/registration was used for QA. A signing identity or authorized allowed clean host is still needed. No security workaround is provided.

## 14. Files Changed

'''+''.join(f'- `{name}`\n' for name in sorted(set(changed)))+f'''
Only intended source/scripts/docs enter the local commit. Existing art/runtime changes remain unstaged and preserved in private backup. No new packs, engine rewrite, network service or framework.

## 15. Remaining Issues

P0 release gates: rights/upstream notice reconciliation and blocked unsigned Windows distribution. P1/PARTIAL art: Eren/climbing transitions lack original suitable frames; public-quality animation review incomplete. P2: uneven character balance; eight-character CPU/logic-rate optimization and longer final-build soak; native multi-monitor/DPI and subjective audio review. Runtime21.0.8 provenance/update/security and dependency vulnerability clearance were not performed. No production-ready label.

## 16. Git Commits and Backup Paths

Baseline ce5278d on codex/finalization-20261102; current working branch codex/refinement-20261102. Commit when generated: `{commit}`; delivery commit IDs and exact ZIP/bundle names are recorded in output/refinement/BACKUPS.md after review. Source ZIP + PRIVATE development ZIP + Git bundle + QA handoff copied outside workspace to C:/Users/molha/ShinobiMate-refinement-20261102. Private art is not licensed for publication. Original rc.1 backups and artifacts remain. No push.

## 17. Portable/Setup Output Paths

'''+''.join(f'- `{r/name}` — SHA256 `{value}`\n' for name,value in hashes.items())+f'''
- EXE image: `{r/'output/release-image/ShinobiMate-Portable/ShinobiMate.exe'}`
- Notes/checksums/install guide/known issues in dist/, exact source/private backup filenames in output/refinement/BACKUPS.md.

## 18. Final Production Readiness Decision

**NOT READY for friends/public distribution until signing/native artifact gates and rights are resolved.** Source remains buildable and locally testable with an approved JDK. Publicv1.0 also needs clean-device exact-hash install/upgrade/uninstall, longer stable performance testing and final art/listening review. لا تعطّل الحماية ولا تنشر الأصول المحمية.

Reproduce with JDK21 and Maven: `tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package`; `tools/run-finalization-checks.ps1 -Jdk <JDK> -SkipBuild`; compile/run tools/RefinementUiCheck.java with its base classes; native test main `RefinementDesktopCheck` accepts `-Drefinement.soakSeconds=600`, always set an isolated `-Dapp.userDataDir`. Packaging: tools/build-portable.ps1 -SkipBuild then tools/build-installer.ps1. Restore source and private assets together; bundled Maven/runtime can be recovered from existing local project/portable, since source archives exclude heavy runtime/build outputs. Exit before update; keep %LOCALAPPDATA%/ShinobiMate; 0.9.2/0.9.3 require Tray Exit + Retry. Never delete user data as a repair step.
'''
(r/'SHINOBIMATE_FINAL_PRODUCTION_QA.md').write_text(report,encoding='utf-8')
(d/f'FINAL_QA-{version}.md').write_text(report,encoding='utf-8')
print('Wrote report and actual-artifact checksums for',version)
