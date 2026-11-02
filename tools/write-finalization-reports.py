"""Generate local release handoff from measured artifacts. Never publish or assert installer success."""
from pathlib import Path
import hashlib, json, zipfile, shutil
root=Path(__file__).resolve().parents[1]; out=root/'output/finalization'; dist=root/'dist'
version='0.9.5-rc.1'
def write(name,text): (out/name).write_text(text.strip()+'\n',encoding='utf-8')
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
artifacts=[dist/f'ShinobiMate-Portable-{version}.zip',dist/f'ShinobiMate-Setup-{version}.exe',root/'target/ShinobiMate.jar']
hashes={str(p.relative_to(root)).replace('\\','/'):sha(p) for p in artifacts}
write('artifact-hashes.json',json.dumps(hashes,indent=2))
(dist/f'checksums-{version}.txt').write_text('\n'.join(f'{hashes[str(p.relative_to(root)).replace(chr(92),"/")]}  {p.name}' for p in artifacts)+'\n',encoding='utf-8')
write('BUG_FIX_REPORT.md','''# Bug fixes — 2026-11-02

This mission used the current heavily modified working tree, not HEAD's old initial snapshot. Previous Eren/art/combat repairs are preserved and validated, not claimed as newly implemented today.

| Issue | Evidence / root cause | Change | Verification |
|---|---|---|---|
| Reload/load may deadlock | prepareCharacter/loadConfiguration lock Main then mascot list; reloadCharacter previously locked list then Main | Make reloadCharacter synchronized on Main before taking list lock | ReloadConcurrencyCheck: 12 actual reloads + 12 prepares, deadlock monitor; final JAR PASS |
| STOP during first-run guide hangs | run holds lifecycleLock while waiting for modal EDT onboarding; shutdown waits same lock | Dispose onboarding on EDT before waiting for lifecycleLock; guard before constructing/showing onboarding and continuing startup | FirstRunStopCheck on real Main: visible guide, wrong STOP rejected, authenticated STOP exits without onboarding completion; final JAR PASS |
| Late queued spawn during shutdown | loader readiness remained true after exiting flag; creation could race cleanup | Reject prepare/create after exit begins; check again under mascot-list lock before admission; readiness includes exiting | Startup/shutdown/concurrent checks; no automatic spawn regression |
| Characters cover manager buttons | Observed in actual Windows screenshot with eight active companions | Shared CompanionLayering policy lowers mascot and combat overlay topmost while application controls show, restores configured preference when hidden | LayeringCheck real manager/dialog show/hide PASS; final native visual follow-up recorded separately |
| Old checks fail before exercising functionality | ProductionCheck pinned 0.9.0; CharacterCheck/UiRefinementCheck bypassed startup readiness; Eren check assumed retired combat/human path; RuntimeStabilityCheck reflected deleted Clip worker | Update fixtures, use current idle clip and AudioManager injectable backend; product fixture includes common audio | Current suite executed; test maintenance is not counted as a production combat fix |
| Build writes fail | D: had 0 bytes; Git status could not write index | Move only explicitly resolved generated packaging-backups to C:, preserve every old file | About 5.2 GB moved; successful clean builds, source archives and release files |
| Exact unsigned Windows executables blocked | Code Integrity 3033/3077, policy signing requirement | No protection change; preserve evidence and mark release gate BLOCKED | Exact final setup and portable launch attempts logged; installation/uninstall NOT VERIFIED |

New production changes: Main.java, Mascot.java, CombatEffects.java, ui/CompanionLayering.java, pom.xml (0.9.5-rc.1). Supporting changes: .gitignore (dist exclusion), NOTICE.md (all eight packs), README.md, tools/build-portable.ps1 (correct first-run instructions), tools/check-product.ps1, tools/ProductionCheck.java, tools/CharacterCheck.java, tools/UiRefinementCheck.java, tools/ErenCombatCheck.java, tools/RuntimeStabilityCheck.java, tools/FullCharacterDesktopCheck.java. Added QA, inventory, report and backup scripts are listed in MISSION_FILES.txt. No new artwork, sound asset, combat balance change or upstream dependency upgrade was made today.
''')
write('ASSET_LICENSE_MATRIX.md','''# Asset and license matrix

This is a local provenance inventory, not legal clearance. No publication or upload occurred.

| Component | Local evidence | Rights status / gate |
|---|---|---|
| Modified Shimeji/Shimeji-ee engine | LICENSE, NOTICE.md, preserved package names | Exact upstream revision/notices need reconciliation; current LICENSE wording has known inconsistencies |
| NarutoShippuden / SasukeShippuden | img folders, art source sheets, pack manifests | Character/design/trademark and source artwork redistribution permission UNKNOWN |
| GojoSatoru / SukunaRyomen | img folders and existing combat art | Redistribution permission UNKNOWN |
| AstaBlackClover | img and art/character-packs | Redistribution permission UNKNOWN |
| Tom / Jerry | img and source art | Redistribution permission UNKNOWN; private testing is not a redistribution license |
| ErenYeager, human and Titan | img/ErenYeager and art sources | Redistribution permission UNKNOWN |
| Procedural sound effects | tools/build-audio-phase1.py, art/audio/asset-provenance.json | Locally documented original synthesis; no show voices/music copied in this mission. Formal ownership/provenance confirmation remains required |
| App icon/branding | conf/ShinobiMate.ico, art/branding | Existing local branding; external rights not independently verified |
| JNA 5.17.0, Rhino 1.7.14, FlatLaf 3.6, AbsoluteLayout RELEASE270 | POM and preserved per-dependency licenses in portable licenses/ | Third-party; retain and review actual notices. No CVE clearance claimed |
| Bundled Java 21.0.8 | runtime/release and runtime/legal/ | Existing runtime; provenance, redistribution terms and update/security review remain open. QA also used installed JBR 21.0.11 |
| Inno Setup compiler | Existing local Inno Setup 6 installation | Compiler available; commercial terms/signing review not completed |

Private development backup contains rights-sensitive art. Keep it local. The source-only archive contains no character packs; neither archive should be described as legally cleared for publication.
''')
write('KNOWN_ISSUES.md','''# Known issues / نواقص معروفة

- **Release blocker:** exact new unsigned EXE and Setup are blocked on this Windows host by Application Control / Code Integrity. No policy was disabled. No signing identity or authorized clean VM was available. Exact final installation, upgrade and uninstall are NOT VERIFIED.
- **Rights blocker:** all eight character packs and precise upstream licensing remain uncleared for redistribution. Do not upload protected artwork or publish a public release.
- Eren wall/ceiling grips and natural connected transition artwork remain incomplete. Existing pose reuse/derived frames are reported honestly; no new animation was authored today.
- Balance remains uneven: Eren/Naruto 17/96 wins; Eren/Sasuke 41/96; Eren/Tom 74/96. Tom/Jerry 52/160 vs 108/160. Jerry/Sasuke 0/72 in a separate regression sample. These are seeded model samples, not GUI win-rate claims.
- Native action tests force stand/sit/wall/ceiling entries; they do not prove continuous natural wall-to-ceiling traversal. 100/150% are app Scaling, not a physical Windows DPI switch.
- No second physical monitor, clean Windows 10/11 machine, formal 30–60 minute final-build soak or subjective listening approval was completed. Read QA_MATRIX for the actual shorter soak and its build hash.
- Automated audio device/PCM/timing/volume tests passed; playback counters do not establish subjective sound quality. Voice lines/music were not added.
- Runtime 21.0.8 and pinned dependency security/provenance review remain open. No claim of a current vulnerability-free dependency set.
- This repository contains many earlier uncommitted art/runtime changes. The local source checkpoint deliberately excludes pack art and generated distributions. Restore the private development archive for the complete current assets.

لا تُعطّل حماية Windows. النسخة مناسبة للتطوير والاختبار المحلي عبر JDK معتمد، وليست معتمدة للتوزيع حاليًا. حقوق الصور والتوقيع واختبار التثبيت والإزالة ما زالت بوابات إصدار مفتوحة.
''')
write('PUBLIC_EDITION_PLAN.md','''# Public edition plan

Keep current anime/cartoon packs and private development archives local. Before any public push/release: reconcile exact upstream source/license and full repository history; document dependency/runtime notices; obtain asset permissions or prepare an engine-only distribution with original licensed mascots; review branding; obtain an approved signing identity; sign launcher/setup/uninstaller as supported and test the exact signed hashes on clean Windows. Do not infer permission from generated/redrawn recognizable characters. Do not force push or rewrite this user's history as a shortcut.

An engine-only public edition needs a usable empty Character Manager and import guidance, no embedded protected art, sanitized repository history and reviewed icon/screenshots. It is a separate future packaging task; no public edition was published here.
''')
write('HANDOFF.md',f'''# Handoff — {version}

Workspace: `{root}`. Source code remains in place. Final portable image: `output/release-image/ShinobiMate-Portable/ShinobiMate.exe`. ZIP and Setup: `dist/`. All are local/private candidates, not approved distributions.

Use a development JDK 21. On this host: `C:\\Users\\molha\\.jdks\\jbr-21.0.11`. In PowerShell from project root:

```powershell
$env:JAVA_HOME='C:\\Users\\molha\\.jdks\\jbr-21.0.11'
& tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package
& tools/run-finalization-checks.ps1 -Jdk $env:JAVA_HOME -SkipBuild
& tools/run-finalization-checks.ps1 -Jdk $env:JAVA_HOME -SkipBuild -Native
& tools/build-portable.ps1 -Jdk $env:JAVA_HOME -SkipBuild
& tools/build-installer.ps1
& tools/save-finalization-backup.ps1
& tools/save-finalization-backup.ps1 -IncludePrivateAssets
```

Prerequisites: JDK 21, Maven bundled under tools/maven (source ZIP intentionally excludes Maven binaries; recover from this workspace or obtain official Maven), Maven dependency cache/network, existing img/audio/conf assets from PRIVATE development backup, original bundled runtime in existing Shimeji-ee/runtime or portable artifact, Inno Setup compiler for Setup only. Python inventory requires Pillow. No compiler is silently downloaded. Do not overwrite earlier versioned releases; scripts back up replaced staging images.

For permitted developer QA, `java --enable-native-access=ALL-UNNAMED -Dapp.dir=<absolute-resources-root> -Dapp.userDataDir=<isolated-test-profile> -jar <absolute-path-to-ShinobiMate.jar>` runs the actual Main. This is a developer invocation and does not certify the blocked native launcher or its bundled JVM. Never disable Windows security to launch the EXE.

First run: choose language, Character Manager opens, zero automatic companions. Launch chosen cards or tray entries. Restore is off by default; enable it explicitly in Settings to restore a saved session. Right-click a companion → Combat → choose a specific opponent. Stop/rematch remain explicit. Eren resets to human at fight end. Settings includes mute and five percentage volume sliders; Focus and Spiritual Cloud remain available.

User data: `%LOCALAPPDATA%\\ShinobiMate`; close via tray Exit before backup/update. Preserve the whole directory (packs, settings, progress, messages). 0.9.2/0.9.3 need Tray Exit then installer Retry. Never install over the live user installation during QA. Rollback: exit, restore previous application files and backed-up profile; do not delete user data. Uninstall should preserve profile unless the user deliberately selects deletion. That final path still needs exact-artifact QA on an authorized machine.

Next gates: approved signing/allowed clean host; exact-hash install/first run/second shortcut/upgrade/uninstall; 30–60 minute final-build soak; physical DPI/multi-monitor; subjective listening/art review; pack licensing/upstream reconciliation. See FINAL_REPORT and logs.

No push executed. After explicit future authorization AND clearance of all included repository history/assets, review `git status`, `git diff --cached --stat`, and the local branch before `git push -u origin codex/finalization-20261102`. Do not run that command while licensing/history gates remain open.
''')
write('QA_MATRIX.md','''# QA matrix — actual execution

Final build: JDK 21.0.11, Maven clean package. Native Windows tests use actual Java/Swing/native bitmap windows, mostly programmatic app/action/menu APIs. Model tests use the production combat engine with in-memory bodies. Neither category is a clean-machine installer test.

| Area | Actual result | Evidence / limits |
|---|---|---|
| Clean Maven build | PASS | logs/final-build.log; Maven itself has no Surefire test suite; standalone checks run separately |
| Lifecycle / first-run STOP / locks | PASS final JAR | FirstRunStopCheck, ReloadConcurrencyCheck, RuntimeStabilityCheck; intentionally injected stalls are not real hangs |
| First/subsequent launch, restore opt-in, duplicate/missing history, second activation | PASS bilingual 11-mode matrix | logs/startup-matrix.log and final-full-suite.log; real Main and spawned second JVM, not final EXE |
| Eight-pack catalog, actions, combat regression | PASS | FullCharacterCheck, CombatCheck, ErenCombatCheck, LocomotionCheck logs |
| Eren and cartoon combat | PASS functional; balance PARTIAL | RepairCombatCheck: 96 fights per requested Eren opponent; 160 Tom/Jerry; both directions/scales; no miss impact |
| Native stand/sit/wall/ceiling | PASS on pre-layering build with identical motion code | FinalMotionCheck-1.0-retry.log and -1.5.log; 5120 sampled checks each; eight packs, both facings, ten entries per action; no continuous transition claim |
| Native fights | PASS final JAR | FullCharacterDesktopCheck-final.log: 5571 checks, eight present, three solos, five complete matchups, natural transformation/reset, actual menu dispatch, drag and 600 observed audio events; accelerated tick, not human played |
| UI layering | PASS final policy/GUI test; visual follow-up inconclusive | LayeringCheck real manager/dialog show/hide; actual screenshot review found original defect. Later computer-use capture returned occluded/background content, so no final screenshot PASS is claimed |
| Arabic/English cards, settings, imports, Focus | PASS | Product suite real Swing controls; RTL/LTR, filters, quotas, persistence and failed-save rollback |
| Audio | PASS automated/device; subjective NOT TESTED | ProfessionalAudioCheck 638 assertions; AudioCheck native mixer close; final-suite logs. Existing walking footsteps intentionally silent; other movement/combat events retained |
| Spiritual Cloud | PASS automated | 11593 assertions, exact texts and 10000 simulated placements; no complete human review of every message on this build |
| Import safety/privacy | PASS tested cases | PackCheck 36, PackImportCheck 26, ProductionCheck 37; traversal, ADS/device names, executable extensions, duplicate paths, XXE, size limits, corrupt settings, restricted expressions and bounded sanitized logs. Not a penetration-test certification |
| Physical display / DPI | PARTIAL | Current host display observed; DpiBetaCheck simulated 96/120/144 menu DPI and negative-origin monitors; no OS DPI change or physical second display |
| Computer-use review | PARTIAL | Actual manager RTL/8 counts reviewed via computer-use skill. Coordinate clicks did not establish Settings navigation; programmatic GUI test results are distinguished from human input |
| Native portable final EXE | BLOCKED | Unmodified relocated ZIP EXE rejected by Application Control. Installed-JDK QA is not a bundled-runtime PASS |
| Exact Setup install/upgrade/uninstall | BLOCKED / NOT VERIFIED | Exact unsigned Setup /HELP launch blocked before installer code; logs/exact-setup-security.txt and final-code-integrity.txt. Existing installed app/registry/profile untouched |
| Soak | See SOAK_REPORT.md | Shorter natural eight-character run; exact JAR hash and measured time recorded there; no 30–60 minute claim |
| Rights/signing/public release | BLOCKED | ASSET_LICENSE_MATRIX and existing NOTICE; no public upload/push |

Failed intermediate tests were not counted as passes: stale Eren frame-path assertion, stale fixture startup readiness, deleted audio worker reflection, and initial native fixture with pending additions. Their fixes and later runs are preserved in source/logs. Native tests that deliberately stop Manager can generate watchdog ENGINE_DELAY; motion check now closes that monitor explicitly.
''')
write('STATUS.md','''# Finalization status

Decision: **NOT READY — BLOCKERS: Windows Application Control/signing, exact installer lifecycle, asset/upstream rights.**

| Phase | Status | Evidence / next step |
|---|---|---|
| 0 baseline/backup | PASS | Current source preserved; clean build; full D: issue resolved by moving only generated backups to C: |
| 1 lifecycle | PARTIAL | New startup STOP, lock ordering, shutdown admission and bilingual reactivation checks PASS; native EXE policy gate blocked |
| 2 animation/Eren | PARTIAL | Functional transformation/attacks/reset and native canvas checks PASS; no new art, wall/ceiling artistry incomplete |
| 3 combat | PARTIAL | All eight functional regression; required matchup samples PASS; uneven balance documented |
| 4 UX/audio | PARTIAL | Layering fix, RTL/LTR/settings/focus/cloud and audio tests PASS; listening/art approval open |
| 5 security/compatibility | PARTIAL | Tested import/privacy cases PASS; signing/rights/clean-device/physical DPI open |
| 6 artifacts/handoff | PARTIAL | Portable, Setup, checksums, docs, backups and local source checkpoint prepared; exact Windows install/uninstall not certified |
''')
notes='''# ShinobiMate 0.9.5-rc.1 — local release candidate

**NOT READY for distribution.** Windows Application Control blocks the unsigned final launcher/Setup on this host; exact install/upgrade/uninstall need an authorized allowed machine and signing review. Eight character packs and upstream licensing are not cleared for redistribution. Nothing was published.

Changes: fix pack reload lock ordering; make authenticated STOP close first-run onboarding without deadlock; reject new character creation during shutdown; lower companions/combat overlays behind application controls; preserve zero-spawn/manual-combat policy. Preserve and regression-test prior Eren/Tom/Jerry animation/combat fixes. Update obsolete QA fixtures and add reproducible backup/inventory/lifecycle checks. No new artwork/audio or balance changes were made in this mission.

Tested: clean build, standalone engine/security/settings/audio checks, bilingual startup/restore/reactivation, native eight-character state entries at app scales 100/150%, five native matchups, transformation/reset. Known limits: incomplete climbing transitions, uneven matchups, subjective audio/art review, physical DPI/second monitor, final-binary 30–60 minute soak, rights and installer gates.

تم بناء مرشح 0.9.5-rc.1 محليًا، مع إصلاح تزامن تحميل الحزم والإغلاق أثناء الإعداد الأولي وترتيب النوافذ. يبدأ التطبيق بمدير الشخصيات ولا يشغّل شخصيات تلقائيًا إلا عند تفعيل الاستعادة، والقتال يدوي فقط. اختُبرت الشخصيات الثماني والتحول والصوت والإعدادات وفق مصفوفة QA. لم تُنشأ رسوم جديدة. **التوزيع غير معتمد حاليًا** بسبب منع Windows للملفات غير الموقعة، وعدم اكتمال اختبار التثبيت والإزالة وحقوق الأصول. لا تُعطّل حماية الجهاز.
'''
(dist/f'RELEASE_NOTES-{version}.md').write_text(notes,encoding='utf-8')
for name in ('KNOWN_ISSUES.md','HANDOFF.md','ASSET_LICENSE_MATRIX.md'):
    shutil.copyfile(out/name,dist/f'{name[:-3]}-{version}.md')
print('Wrote release notes, checksums and finalization reports for actual files.')
