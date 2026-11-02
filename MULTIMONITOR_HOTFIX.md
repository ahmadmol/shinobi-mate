# Multi-monitor falling / window collision hotfix

2026-11-02 — local candidate `0.9.5-rc.3+monitor.1`. Original rc.3 and ui.1 artifacts are retained. This supersedes the earlier limited motion acceptance; forced grounded-state checks did not exercise falling collision.

## Confirmed causes and changes

* Windows collision used logical per-monitor work areas, but `getFloor(true)`/wall separator checks consulted physical monitor/primary-work-area boundaries. Valid taskbar-adjusted floors were rejected, producing endless fall/respawn. Separator tests now use the same logical work-area topology and reject only seams that continue into another monitor.
* With both interactive-window lists empty, the Windows selector rejected every window. Empty whitelist now permits normal eligible windows; explicit whitelists/blacklists still filter them. Own-process mascot/UI windows are excluded. Cache refresh now resets both lists. Maximized/minimized/cloaked windows remain excluded by the existing selector.
* Native window rectangles were physical pixels (example:200,225,625,375), while Java/character coordinates were logical (160,180,500,300). Window collision converts local offsets/sizes using the actual native-monitor/AWT-size ratio, including legacy DPI virtualization; window movement converts back. No Windows DPI/security policy changed.

## Validation

* Offline Maven `BUILD SUCCESS`.
* Before: the same real native fall fixture failed on Naruto: anchor y3148 instead of floor816 after160 ticks. Failure retained in `output/monitor-fix/before.log`.
* After: eight packs × two monitors: actual Fall action landing and50 stable floor checks, plus both outer-wall climb actions: **48 cases passed**. Work areas `(0,0,1536,816)` and `(-1440,0,1440,852)`. Repeated placements between monitors simulate the released positions; no OS mouse drag was synthesized.
* Separate native JFrame process on each monitor: default discovery and exact logical rectangle passed; all eight packs landed on its top and climbed its side. Evidence `window-final.log`.
* Regression: Locomotion8841 assertions; RuntimeStability11; Refinement298; ErenCombat948111 assertions /170893 natural fight ticks passed. These are automated checks, not new visual artwork approval.
* Exact packaged EXE/bundled runtime landing evidence is saved separately under the independent C directory; consult `bundled-native-landing.log` for its actual outcome.

## Artifacts and limits

The normal D-drive packaging attempt failed due to insufficient disk space. No user files were deleted. The new portable was assembled on C from the verified ui.1 native launcher/runtime/assets, replacing only the application JAR/build-info and version text. The launcher configuration and numeric Windows version0.9.5 are compatible and unchanged. Setup was compiled from that image with the existing installer recipe. No install/upgrade/uninstall test was repeated for this hotfix; previous QA applies only to its named builds.

Independent output: `<LOCAL_USER>/ShinobiMate-monitor-fix-20261102/artifacts/`.

Hot-plug, all monitor layouts/DPI arrangements, natural corner transfers, physical mouse drag and long soak on this build remain unverified. Artwork and rights/signing/upstream-notice gates remain open. This is a local fix candidate, not Production Ready.

بالعربية: أصلح هذا المرور حلقة السقوط الناتجة عن اختلاف حدود الأرض، واكتشاف النوافذ الافتراضي، وتحويل إحداثيات النوافذ بين البكسلات الفعلية والمنطقية. استخرج ZIP الجديد كاملًا إلى مجلد جديد وأغلق النسخة السابقة قبل تشغيله. لا تستبدل ملف EXE وحده؛ الإصلاح داخل app/ShinobiMate.jar. ملفات المستخدم الأصلية لم تُستخدم للاختبار.
