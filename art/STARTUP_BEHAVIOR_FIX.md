# ShinobiMate — Startup behavior fix

تاريخ التنفيذ: 12 أكتوبر 2026. تم إصلاح بدء التشغيل بشكل Generic؛ لا توجد أسماء الشخصيات الخمس في `Main` أو سياسة الاستعادة الجديدة.

## ROOT CAUSE

`Main.run()` كان يقرأ `ActiveShimeji` ويحوّل كل اسم فيه إلى `createMascot()` دون خيار استعادة. ملف المستخدم الذي فُحص يحتوي الشخصيات الخمس، لذلك كانت تظهر جميعًا عند التشغيل.

`prepareCharacter()` كان يضيف الحزم التي حُمّلت مواردها إلى `imageSets` ويكتب هذه القائمة إلى `ActiveShimeji`. إزالة الشخصية لم تُزل اسمها من قائمة الموارد المحمّلة، لذلك لم يكن المفتاح يمثل الشخصيات الموجودة فعليًا. كان Onboarding أيضًا يكتب اختيارًا افتراضيًا إلى المفتاح نفسه.

مساران إضافيان كانا يستخدمان قائمة الموارد بمعنى قائمة الشخصيات: تغيير اللغة في الواجهة القديمة يعيد إنشاء كل `imageSets`، وgesture قديم في الـTray ينشئ نسخة من كل اسم محمّل. Legacy chooser كان يحدد كل الحزم عندما تكون قائمة الاختيار فارغة.

Character Manager نفسه لم يكن ينشئ Mascots أثناء بناء البطاقات. تعداد Installed وعرض previews بقيا منفصلين عن التشغيل. ملفات إعدادات التثبيت القديمة تحتوي قوائم افتراضية، لكن `SettingsStore` الحالي يقرأ ملف المستخدم؛ الإصلاح لا يعتمد على تنظيف أسماء بعينها أو تشغيل أول حزمة كـfallback.

## FIX

- الاستعادة اختيارية: `RestorePreviousCharacters=false` افتراضيًا، بما يشمل ملفات الإعدادات القديمة التي لا تحتوي المفتاح.
- سجل مستقل: `PreviousCharacters` يأتي من snapshot للشخصيات الحية وطلبات الإضافة التي تم قبولها بالفعل عند الخروج، قبل التخلص من النوافذ. لا يُستخرج من Installed أو loaded resources أو `ActiveShimeji` القديم.
- snapshot يستبعد الشخصيات التي أُزيلت ويحتفظ بالتكرار: نسختان من حزمة واحدة تُحفظان كنسختين. الاستعادة تراعي MaxCharacters وتتجاوز الحزم المفقودة دون تشغيل بديل تلقائي.
- `prepareCharacter()` يجهّز الموارد فقط. `ActiveShimeji` أصبح حالة الجلسة الحالية للـlegacy UI، ويُحدّث عند الإضافة/الإزالة/التخلص، ولا يقرر بدء التشغيل.
- تغيير اللغة يعيد فقط الشخصيات الموجودة فعليًا، مع عدد نسخها؛ تحميل الموارد أو تغيير اللغة لا يجعل حزمة غير نشطة نشطة.
- طلب spawn بلا اختيار، والـTray gesture القديم، يفتحان المدير. قائمة Tray Add Character تستمر في تشغيل الاسم الذي يختاره المستخدم فقط.
- Legacy chooser يبدأ دون اختيار كل الحزم؛ الاختيار اليدوي يعمل حتى لو كانت موارد الشخصية محمّلة مسبقًا.
- البطاقة تعرض `مثبّت • نشط: 0` / `Installed • Active: 0`. بعد التشغيل يصبح العدد 1 ويتغير الزر إلى `+ إضافة نسخة` / `+ Add instance`.
- خيار Startup في Settings: «استعادة الشخصيات السابقة عند بدء التشغيل» / «Restore previous characters on startup». تغييره لا ينشئ شخصيات فورًا؛ يطبق في التشغيل القادم.

## STARTUP FLOW

```text
First run
  Onboarding: اختيار اللغة
  → فتح Character Manager
  → 0 Mascots
  → اختيار المستخدم «تشغيل»

Subsequent launch — Restore OFF (default)
  Character Manager مباشرة
  → 0 Mascots، حتى مع وجود حزم أو سجل سابق

Subsequent launch — Restore ON
  استعادة snapshot السابقة فقط
  → Character Manager
  → عرض أعداد النسخ الفعلية
```

Onboarding الأول صفحة لغة واحدة؛ دليل المساعدة الاختياري الذي يعاد فتحه من الإعدادات يحتفظ بصفحاته الخمس، دون حفظ اختيار تلقائي أو تشغيل شخصية. First Run لا يستعيد شخصيات حتى لو احتوى ملف الإعدادات على history وrestore ON.

Character Manager يفتح كذلك مع `--background` حسب السلوك الجديد المطلوب. إغلاق نافذته يخفيها ويبقي التطبيق في الـTray، حتى عند وجود صفر شخصيات. إعادة تشغيل نسخة ثانية تفتح نافذة المدير الموجودة دون زيادة عدد Mascots.

## FILES CHANGED

| ملف runtime | التغيير |
|---|---|
| `src/main/java/com/group_finity/mascot/Main.java` | مسار startup، فصل تحميل الموارد، snapshot قبل exit، تغيير اللغة، إزالة fallback/gesture التلقائي |
| `src/main/java/com/group_finity/mascot/Manager.java` | تحديث الحالة الفعلية عند add/remove/dispose |
| `src/main/java/com/group_finity/mascot/settings/CharacterSession.java` | سياسة استعادة Generic وsnapshot يحتفظ بالنسخ؛ ملف جديد |
| `src/main/java/com/group_finity/mascot/settings/SettingsStore.java` | Restore OFF وhistory مستقل والتحقق من خيار الاستعادة |
| `src/main/java/com/group_finity/mascot/settings/SettingsService.java` | الاحتفاظ بـhistory عند reset defaults، مع إعادة Restore إلى OFF |
| `src/main/java/com/group_finity/mascot/ui/SettingsDialog.java` | خيار الاستعادة بالعربية والإنجليزية |
| `src/main/java/com/group_finity/mascot/ui/OnboardingDialog.java` | اللغة ثم handoff؛ حذف كتابة الشخصية الافتراضية |
| `src/main/java/com/group_finity/mascot/ui/CharacterManagerWindow.java` | Active: 0، عدد النسخ، وزر + إضافة نسخة |
| `src/main/java/com/group_finity/mascot/imagesetchooser/ImageSetChooser.java` | legacy selection من الجلسة الحقيقية، ودون select-all افتراضي أو كتابة loaded IDs |

الاختبارات الجديدة: `tools/StartupBehaviorCheck.java` و`tools/check-startup-behavior.ps1`. حُدثت توقعات `CharacterCheck` و`BetaStartupCheck` للسلوك الجديد، وfixtures بدء تشغيل اختبارات combat/monitor لتطلب restore صراحة. `AudioVisualQA` أصبح يجهز شخصيات الاختبار بإضافة يدوية صريحة. تم تحديث assertion قديم في `SettingsCheck` كان يتوقع ثمانية تبويبات قبل إضافة Audio في المرحلة السابقة؛ الواجهة الحالية تسعة تبويبات.

ملفات توزيع/أدلة: `target/ShinobiMate.jar` و`Shimeji-ee/app/Shimeji-ee.jar`، هذا التقرير، وسجلات الاختبارات تحت `output/startup-fix`. لم يتم تعديل حزم الشخصيات أو ملفات الصوت في هذا الإصلاح.

## TEST RESULTS

| الاختبار المطلوب | النتيجة |
|---|---|
| 1. Clean first install → language → manager → 0 Mascots | PASS بالعربية والإنجليزية؛ تحقق من صفر أثناء Onboarding وبعده |
| 2. اختيار Naruto → Naruto فقط | PASS عبر زر البطاقة الحقيقي |
| 3. اختيار Sasuke → Naruto + Sasuke؛ الأخرى صفر | PASS |
| 4. Exit/reopen مع Restore OFF → manager وصفر | PASS في JVM جديدة على profile الجلسة نفسها |
| 5. تفعيل Restore، تشغيل الزوج، exit/reopen → الزوج فقط | PASS؛ التفعيل وحده لا يعمل spawn |
| 6. تعطيل Restore ثم reopen → صفر | PASS |

اختبارات إضافية PASS:

- 11 تشغيلًا مستقلًا في matrix العربية/الإنجليزية والحالات الإضافية، **299 assertions**.
- استعادة Naruto مرتين وSasuke مرة: ثلاث شخصيات فقط، مع حفظ عدد النسخ.
- profile قديم بـActiveShimeji للشخصيات الخمس وAlwaysShowShimejiChooser=true: صفر، والمدير ظاهر، حتى مع --background.
- حزمة محفوظة لم تعد مثبتة: تُتجاوز، ولا تشغّل حزمة أخرى بدلها.
- إعداد موارد حزمة، ثم تغيير اللغة، ثم legacy chooser: لا spawn حتى اختيار صريح.
- Tray Add Character، نسخة إضافية، وإزالة النسخة الإضافية.
- Second-instance activation مع صفر ومع شخصيات نشطة، دون duplicate spawn.
- أربعة تشغيلات أخرى عبر المشغّل الأصلي و**Java 21.0.8 المضمّنة**: first/off/on/off-again؛ **116 assertions**، مع EXE ثانية حقيقية للتفعيل.
- CombatCheck: **289899 assertions / 70813 ticks / 144 مباريات seeded**، إضافة إلى actual Manager/Mascot integration.
- ProfessionalAudioCheck: **639 assertions**، مع timing وcache/cancellation وmute/volume وmovement markers.
- AudioCheck: PASS؛ PCM/voices/native line cleanup.
- SpiritualCloudCheck: **11593 assertions**، 100 نص مطابق، hover/pin/drag، و10000 موضع، وتخطيطات عربية.
- CharacterCheck: **28 assertions**، خمسة packs، نسخ متعددة، limits/removal، وبطاقات ثنائية اللغة.
- UiRefinementCheck: **33 assertions**، search/filter، pending counts، واجهة responsive، RTL/LTR، وrollback.
- SettingsCheck: **26 assertions**، persistence، audio gain، profiles، RTL/LTR، وخيار restore OFF ثم ON ثم OFF.

كل الاختبارات استخدمت profiles منفصلة تحت `output/startup-fix`؛ لم تُعدّل إعدادات المستخدم المعتادة أو إعداد Windows auto-start. بعض اختبارات الواجهة تقرأ حالة اختصار auto-start فقط.

لإعادة matrix: `tools/check-startup-behavior.ps1 -Jdk <JDK-21-or-newer>`. مسار آخر matrix ومسار فحص الـruntime موجودان في `output/startup-fix/current-matrix.txt` و`current-runtime-profile.txt`.

## BUILD

`mvn clean package`: **BUILD SUCCESS**. سجل البناء النهائي: `output/startup-fix/build.log`.

النسخة المحلية تُحدث لتجربتها من `Shimeji-ee/Shimeji-ee.exe`. أغلق النسخة السابقة بالكامل من الـTray قبل تجربة السلوك الجديد؛ تشغيل EXE ثانية أثناء عمل النسخة القديمة يفعّل عمليتها الموجودة.

لم يتم تنفيذ `git add` أو `git commit` أو `git push`، ولم يُعد تصميم Combat أو Audio أو Spiritual Cloud.
