# ShinobiMate 0.9.0-beta — Production audit

التاريخ: 2026-10-10، Asia/Damascus. الحالة: **NOT READY**.

التغييرات والبناء والنسخة المحمولة مكتملة. العائق الرئيسي: Inno Setup غير مثبت،
ولذلك Setup.exe غير موجود. لم يُجرَ اختبار جهاز جديد مستقل أو VM نظيف. لم يُنشر
إصدار GitHub ولم تُنفّذ أوامر git add/commit/push. تعديلات المستخدم والشخصيات السابقة محفوظة.

## 1. Production audit findings

فُحصت مسارات Java والإعدادات والموارد والحزم وسكربتات البناء والتثبيت والترخيص.
المخزون: 238 ملفًا تحت src و663 ملف موارد تحت img وقت التدقيق. البحث النصي الكامل
محفوظ في `output/beta-full-text-audit.txt`، ومسارات التطوير في `output/beta-path-audit.txt`.

المشكلات الأصلية: إعدادات المستخدم ورسائل السحابة والتقدم والثيم داخل التطبيق؛
الصور والصوت والأيقونة والـchooser افترضت working directory؛ imports كتبت داخل img؛
إصدار 2.0.1 مكرر؛ سجل ShimejieeLog في working directory؛ قوائم debug/editor/licensing؛
عدم وجود قفل instance؛ بعض قراءات الإعدادات غير متسامحة؛ نصوص About وروابط قديمة؛
اختبارات كانت تكتب إعدادات داخل conf، ولم يكن JAR يستبعد فئات الاختبار.

## 2. Development-only issues fixed

- فصل profile التطبيق، وكتابة settings/theme ذرّيًا مع defaults وsanitization ونسخ
  احتياطي عند فساد صيغة settings. Restore Defaults يحافظ على اللغة والشخصية والدليل
  والرسائل والتقدم والحزم وخيار startup.
- فصل مسارات الحزم المضمّنة عن imported packs، وربط التحميل والـcloud والمزاج والقتال
  والصوت بمحلل مسارات واحد. لا تستبدل imports الموارد المضمّنة.
- استيراد folder/ZIP عبر staging والتحقق ثم rename؛ رفض traversal/absolute/ADS/devices
  والملفات التنفيذية والروابط والحزم الكبيرة والصور المفرطة وXXE والموارد المفقودة.
- تعابير الحزم محصورة في بيانات وعمليات رياضية وborder predicates: لا برامج JavaScript
  عامة أو loops/functions/assignments/eval/Java methods/reflection؛ wrappers للبيانات للقراءة فقط.
- حزمة تالفة لا تُسقط الشخصيات السليمة. أخطاء تحميل الشخصية ظاهرة، والحزم المرفوضة
  تظهر في حالة Character Manager. يبقى tray/manager متاحًا إذا لم يبقَ pack صالح.
- منع تشغيل process ثانية لنفس profile بقفل file OS؛ يحرر القفل عند crash/exit.
- إخفاء statistics/editor/package-creation/license developer controls افتراضيًا؛ استبعاد
  فئات الاختبار وLicenseFeatureDemo من JAR. بقيت classes/packages القديمة لحماية المحرك.
- ShinobiMate في launcher/tray/dialogs/About/installer/shortcuts؛ حفظ attribution القديم.
- اختيار monitor صحيح للقوائم وحدود نوافذ الإعدادات؛ بدون تغيير إعدادات Windows للمستخدم.
- إصلاح تعليق متقطع عند الإغلاق: التخلص من النوافذ خارج قفل قائمة الشخصيات وعلى Swing EDT،
  وإيقاف المحرك خارج EDT. اختُبر انتهاء native process بعد حزمة تالفة من main thread ومن EDT.
- سكربتات بناء production تقبل JDK عبر JAVA_HOME/-Jdk؛ لا تفترض Android Studio.
- ZIP مفلتر، versioned، مع metadata وLICENSE/NOTICE وتراخيص dependencies كلٌّ على حدة.

## 3. Hardcoded paths

لم يُعثر على اعتماد runtime على `D:\programm` أو `C:\Users\molha` في كود التطبيق
أو launcher النهائي. `app.dir=$APPDIR/..` نسبي لموقع التثبيت.

المسارات التطويرية التي وُجدت: default JDK في build/check scripts كان
`C:\Program Files\Android\Android Studio\jbr` وتمت إزالته؛ مثال قديم في
`art/spiritual-cloud/README.md`؛ مراجع artwork في `art/character-packs/prompts.json`
إلى `D:/programm/images (23).jpg` و`images (27).jpg` و`images.png`. هذه ملفات workflow
لا تُوزَّع ولا يقرأها runtime. مسارات C:/Windows/evil وfile:///C:/evil في اختبارات
أمنية مرفوضة فقط. مسار ISCC الافتراضي هو مسار toolchain قابل للتغيير على جهاز البناء.

## 4. User-data location

`%LOCALAPPDATA%\ShinobiMate`، وعلى جهاز التدقيق افتراضيًا
`C:\Users\molha\AppData\Local\ShinobiMate`.

- settings.properties، theme.properties، custom-cloud.properties، progress.properties، license.properties.
- packs/: user-installed character packs؛ logs/: سجلات الإنتاج؛ instance.lock: ملف قفل instance.
- Application Files: مجلد التثبيت؛ الهدف per-user هو `%LOCALAPPDATA%\Programs\ShinobiMate`.
- لا يُنسخ conf/settings.properties من جهاز التطوير ولا تُكتب settings داخل Program Files.
- لا تُرحّل إعدادات development تلقائيًا؛ أول تشغيل يبدأ بقيم افتراضية نظيفة.
- اختبارات التطوير تستخدم `-Dapp.userDataDir` مع profiles معزولة؛ هذا غير موجود في launcher المُوزَّع.

## 5. Runtime and logging

Java/OpenJDK 21.0.8 مضمّنة في runtime/، مع msvcp140/vcruntime140 DLLs وتراخيص runtime/legal.
المشغّل ناتج standard jpackage app-image؛ modules من runtime المضمّنة كما كانت، وليست
JRE مصغّرة بـjlink. لا يحتاج المستخدم Java/JDK/JAVA_HOME/Maven/IDE.

launcher: Xms64M، Xmx512M، enable-native-access=ALL-UNNAMED. metadata الكاملة
0.9.0-beta مصدرها Maven؛ Windows executable numeric version هو 0.9.0.

logs/shinobimate.log.0/.1/.2: ثلاثة ملفات، حوالي 1 MiB لكل ملف. WARNING/SEVERE فقط؛
لا تُسجل قيم settings أو نصوص الرسائل أو tokens/passwords أو عناوين نوافذ سطح المكتب.
يُسجل الوقت والseverity وlogger/method ونوع exception وstack frames، ويُحذف message
الحر الذي قد يتضمن محتوى حساسًا. اختبار 16000 warning تحقق من rotation وغياب secret marker.

## 6. Build result and centralized metadata

`tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package`: **BUILD SUCCESS**.
البناء النهائي انتهى 2026-10-10 10:58:06 +03:00. `output/beta-build.log` يحفظ النتيجة.
Source of truth: pom.xml؛ build-info.properties مُفلتر من Maven ويستهلكه About والتغليف
وinstaller. minimum version في manifests يعلن توافق pack مع 0.9.0.

تحذيرات shade عن MANIFEST/LICENSE المتداخلة متوقعة؛ تم حفظ notices الأصلية من كل JAR
في licenses/ لتجنب ضياع التراخيص بسبب دمج الملفات بنفس الاسم.

## 7. Installer status

**BLOCKED / NOT BUILT**. ثبّت **Inno Setup 6.7.3** الرسمي مع ISCC.exe من
[JR Software](https://jrsoftware.org/isdl.php)؛ الوصفة تحتاج 6.3+ بسبب x64os.
المسار المتوقع: `C:\Program Files (x86)\Inno Setup 6\ISCC.exe`.
بعد التثبيت شغّل `tools/build-installer.ps1`، أو مرر -Compiler عند اختلاف المسار.

الوصفة المعدلة: Windows 10/11 x64، per-user، runtime، Start Menu، Desktop/startup
اختياريان وغير محددين، Launch after install، uninstall، حفظ profile افتراضيًا،
وخيار حذف البيانات غير المحدد. silent uninstall يحتفظ بالبيانات. DelTree في الوصفة
[لا يعبر reparse points](https://jrsoftware.org/ishelp/topic_isxfunc_deltree.htm).
الوصفة كلها، بما فيها dialog حذف البيانات، **لم تُترجم ولم تُختبر**.

يلزم اختبار fresh install/update/uninstall، الاختصارات، startup اختياري وتعطيله من
Settings، حفظ user data وحذفه باختيار صريح، ثم Setup على جهاز آخر. لا يوجد workaround
أو تنزيل تلقائي لمثبّت بديل.

## 8. Portable status

**BUILT / TESTED**. ZIP يضم 1072 entry وحجمه 69,501,732 byte، دون source/target/IDE/tests/logs.
حُفظ staging خارج dist في output/release-image/ShinobiMate-Portable؛ النسخة القديمة
غير المرقمة نُقلت إلى output/packaging-backups بدل حذفها.

## 9. Exact outputs

Workspace: `D:\programm\Shimeji-ee_2.0.1_Windows_Portable`.

| Path relative to workspace | Status |
|---|---|
| dist/ShinobiMate-Setup-0.9.0-beta.exe | غير موجود؛ compiler blocker |
| dist/ShinobiMate-Portable-0.9.0-beta.zip | موجود ومختبر |
| dist/checksums.txt | SHA-256 للملف الموجود فقط |
| dist/RELEASE-NOTES-0.9.0-beta.md | عربي/إنجليزي |
| output/release-image/ShinobiMate-Portable/ShinobiMate.exe | staging installer/portable |
| target/ShinobiMate.jar | JAR النهائي |

## 10. SHA-256

Portable: `d62b7cdb53d8dfd2b88a092e1296dabdd978c731110b20db1659c605ae9f7fe9`.

JAR: `76a2f0de80ae6a4eeb9f0813b140439fa6b6ee31eb1472748a535e39721bea2b`.

Setup: لا توجد checksum لملف لم يتم إنشاؤه. tools/write-checksums.ps1 يحدث القائمة بعد بناء Setup.

## 11. Windows / clean-machine scenarios

- Windows 11 Home x64، build 10.0.26300. لم يُختبر Windows 10 مستقلًا.
- package نُسخ خارج D: إلى TEMP؛ profiles جديدة؛ working directory لا علاقة له بالمشروع.
- Java/Maven/IDE أزيلت من PATH وJAVA_HOME unset. لا يملك package source أو Maven.
- التشغيل الأول الحقيقي عبر native launcher، العربية والإنجليزية، إنشاء directories،
  الدليل/اللغة/الشخصية/manager/save، وحزمة تالفة مع استمرار التطبيق: PASS.
- native shutdown بعد عرض خطأ pack تالف، من main thread (ar) ومن Swing EDT (en-GB):
  PASS، process exit code 0 ضمن مهلة الاختبار، دون System.exit بديل في مسار النجاح.
- Main الحقيقي دون test entry point: process استمرت، وحُمل JVM من copied runtime/bin/server/jvm.dll.
  conf/img read-only؛ لم تُكتب settings في installation. process ثانية رفضها OS profile lock.
- شاشتان فعليتان: primary Windows 125% وsecondary 100% حسب graphics configuration قبل
  تهيئة compatibility mode؛ البرنامج يحتفظ بـDPI compatibility mode السابق وتظهر
  إحداثياته 1.0 (primary 1920x1080؛ secondary -1440,0,1440x900).
- اختبار مباشر mascot/cloud/right-click popup/manual combat/VFX/stop على الشاشتين: PASS.
- Settings Arabic/English عند MenuDPI 96/120/144، مع فحص صور 150% بصريًا، ومحاكاة
  placement شاشتين بإحداثيات سالبة: PASS. native Windows 150% لم يُختبر ولم نغير scaling المستخدم.
- ليس هذا جهازًا نظيفًا أو VM مستقلًا؛ runtime قد يستفيد من مكونات Windows الموجودة.
  اختبار Setup على جهاز آخر ما زال مطلوبًا، ولا يُعتبر ناجحًا بالتخمين.

## 12. Current features regression

المجموعة الكاملة: **17 product checks PASS**، إضافة native distribution/startup/monitor/DPI checks.
أُعيدت المجموعة الكاملة على JAR النهائي بعد إصلاح theme والإغلاق، بما فيها malformed
Unicode وfont/color/menu-opacity غير الصالحة. اختبارات native startup/distribution/monitor
أُعيدت على package النهائي. صور DPI وعينات CPU/heap القصيرة أُخذت قبل إصلاح الإغلاق؛
هذه ليست قياسات أداء جديدة للبناء الأخير ولا soak test.

| Feature | Result / evidence |
|---|---|
| Character Chooser / Manager / first-run guide | PASS، Swing الحقيقي، اللغتان؛ CharacterCheck وCharacterArtEngineCheck |
| Dynamic packs / Add / Reload | PASS، اكتشاف واستيراد archive/folder staging، cancel/confirm وreload؛ PackCheck/PackImportCheck/ProductionCheck/CombatCheck |
| Arabic RTL / English LTR / Settings / Restore Defaults | PASS، sanitize، rollback عند فشل save، backup corrupt settings؛ SettingsCheck/UiRefinementCheck/ProductionCheck |
| Drag / Right Click / Follow Cursor | PASS، engine checks وnative popup، drag interrupts combat |
| Spiritual Cloud / Custom Messages | PASS، 100 نص، 5 packs، hover/pin/rotation، UTF-8 CRUD، placements؛ SpiritualCloudCheck/FocusCheck |
| Combat / opponent selection / stop / rematch | PASS، Manual Only، no auto battle/bystander fight؛ CombatCheck/native launcher |
| Five current characters | PASS، Naruto/Sasuke/Gojo/Sukuna/Asta، جميع profiles/assets؛ native Naruto-Sasuke، Gojo-Sukuna، Asta-Naruto |
| Mood / Mouse interactions | PASS، per-instance moods، hover/click/double/drag/arbitration؛ MoodCheck |
| Focus Mode | PASS، offline transitions/pause/quiet behavior وrestart protection؛ FocusCheck/UiRefinementCheck |
| Performance Profiles | PASS، logic ثابت ~25Hz، LOW render ~10Hz، Balanced/High ~25Hz، native 1/5 characters |
| Audio | PASS، 4 native clip categories، volumes/mute/close؛ AudioCheck |
| Start with Windows | native temporary shortcut creation/target/working directory/unicode quoting PASS؛ لم يُعدل Startup الحقيقي |
| Single instance / logs / safe import | PASS، process guard، rotation/privacy، adversarial archives وsafe expressions |
| Multi-monitor/DPI | PASS في البيئة الحالية ومحاكاة 150%؛ غير بديل لتغطية كل Windows/display combinations |

أدلة: output/beta-complete-regression.log؛ output/portable-smoke-result.txt؛
output/beta-startup-ar.txt؛ output/beta-startup-en-GB.txt؛ output/beta-actual-launcher.txt؛
output/beta-multi-monitor.txt؛ output/beta-dpi.log؛ output/beta-distribution-final.log؛ output/beta-native.log؛
output/performance-1.json وperformance-5.json؛ output/beta-settings-final.log؛ art/release/screenshots.

## 13. Known issues / limits

- Setup compiler absent؛ لا fresh install/update/uninstall verification.
- لا جهاز آخر/VM نظيف، Windows 10 أو native 150% coverage.
- binaries unsigned؛ Windows trust/SmartScreen behavior غير متحقق منه على جهاز آخر.
- لا official publisher update metadata URL؛ لا تحديث آلي، إعداد URL اختياري قديم محفوظ.
- Runtime 21.0.8 ومصدره inherited runtime؛ provenance/security maintenance بحاجة إلى مراجعة قبل release واسع.
- legacy pack expressions غير المطابقة للsubset الآمن تُرفض عمدًا؛ packs الحالية الخمس نجحت.
- user data القديم داخل development conf لا يُرحّل تلقائيًا؛ لا تُستورد تفضيلات المطور إلى beta.
- اختبارات CPU/heap قصيرة وليست soak test طويلًا؛ ليس هناك ضمان عدم تسرب موارد بعد ساعات.

## 14. License / attribution / character assets

LICENSE بقي verbatim؛ NOTICE.md جديد يحتفظ بالأسماء والإسناد ويرصد نقص الترخيص.
README القديم وصف BSD-2-Clause/Zlib، بينما
[توثيق upstream fork](https://github.com/gil/shimeji-ee) يذكر New BSD وzlib/libpng.
LICENSE الحالي يذكر ShinobiMate فقط وفيه عبارة غير طبيعية `IN CONTRACT mechanically`.
يجب استعادة original notices وربطها بالـupstream revision الصحيح؛ لم نستبدلها بترخيص مفترض.

تراخيص JNA/JNA Platform/Rhino/FlatLaf/IntelliJ Themes/AbsoluteLayout محفوظة لكل JAR؛
runtime/legal محفوظ. يلزم مراجعة dependency/runtime provenance والإشعارات وأي التزامات
إعادة توزيع، ومراجعة ترخيص Inno عند الاستعمال التجاري.

**Core engine منفصل عن character assets**. Naruto/Sasuke/Gojo/Sukuna/Asta باقية للتجربة
الخاصة دون حذف أو تغيير الرسومات. مصدر/مؤلف/ترخيص/حقوق شخصيات وصور وأسماء وأصوات
غير موثقة بالكامل. ترخيص المحرك لا يمنح حقوق تلك الأصول. يلزم أذونات مثبتة أو أصول
مرخصة قبل public commercial release، مع مراجعة الإسناد قبل توزيع أوسع.

## 15. Friends Beta readiness

**NOT READY**. Portable جاهزة للاختبار المحلي، لكن الحزمة الأساسية المطلوبة Setup.exe
غير موجودة. يجب تثبيت ISCC ثم بناء/اختبار installer وجهاز مستقل، مع معالجة مراجعة
الإسناد المناسبة للتوزيع. لا يُنشر GitHub Release أو commit/push قبل اختبار المستخدم للـSetup.
