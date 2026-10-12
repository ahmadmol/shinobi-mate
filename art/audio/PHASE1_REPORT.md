# SHINOBIMATE — PROFESSIONAL AUDIO & SOUND DESIGN / Phase 1

تاريخ التنفيذ: 12 أكتوبر 2026، Asia/Damascus. نطاق التنفيذ: محرك الصوت وNarutoShippuden وSasukeShippuden، مع إبقاء القتال والميزات الأخرى كما هي.

| النتيجة | الحالة |
|---|---|
| Naruto Audio | **COMPLETE** |
| Sasuke Audio | **COMPLETE** |
| Audio Engine | **PRODUCTION READY** لنطاق Phase 1 المختبر محليًا |

هذه الحالات تصف التنفيذ واختبارات التشغيل والموارد. تم فحص العرض بصريًا؛ اختيارك النهائي لمستوى الصوت وجودة المزج بالسماع يبقى ضمن تجربتك الشخصية. لم أستمع عبر أداة التقاط صوت للنظام، ولا أقدّم نتائج القياس بوصفها تقييمًا سمعيًا.

## Audio architecture

`AudioManager` هو المركز الوحيد للتشغيل. API يدعم event، pack ID، pan، `Handle`، و`Scope` لكل شخصية/مباراة. `SoundSystem` وملفات Sound القديمة واجهات توافق تمرر إلى المركز. لم تعد `Mascot.apply()` تشغل أصواتًا كل Frame، ولم يعد `AnimationBuilder` يفتح Clips أثناء تحميل الصور.

الفئات: MOVEMENT / COMBAT / WEAPON / VFX / CHARACTER / UI. لكل حدث id، variations، volume، cooldown، maxConcurrent، loop، priority، وحد أمان maxDurationMs. التعريف موجود في `conf/sounds.json` داخل الحزمة. لا يوجد شرط `if Naruto` أو `if Sasuke` في محرك التشغيل. `audio/common` مكتبة عامة وfallback للأحداث المشتركة والحزم الأخرى.

تفاصيل الصيغة وAPI موجودة في `art/audio/README.md`. جميع الأصول 155 ملف WAV أصليًا، مع مصدر وبصمة لكل ملف في `art/audio/asset-provenance.json`. لا توجد عينات خارجية أو مقاطع ممثلين أو حلقات أنمي أو OST.

## Naruto audio

هوية خفيفة وسريعة: أربع خطوات وأربع خطوات ركض، whoosh للقفز والهواء والمراوغة، هبوط وسحب وإفلات. Punch وKick لهما حركات وتنويعات إصابة، والـcombo يصدر صوت contact مرة واحدة لكل strike. Kunai له ready / throw / impact، وShuriken له spinning throw / impact. Clone له poof عند الظهور وpoof-out عند انتهاء الـVFX، مع maxConcurrent=1 وcooldown=250 ms.

Rasengan: **chakra_charge → rasengan_energy أثناء منتصف startup → rasengan_dash عند ACTIVE → rasengan_impact عند HIT CONFIRMED → energy_release عند انتهاء active**. الشحنة والـswirl طبقات طاقة دوّارة؛ impact يضيف دفعة منخفضة قصيرة. READY/CHARGE handles تتوقف عند بدء الاندفاع أو الإلغاء.

## Sasuke audio

الخطوات أخف وأهدأ، مع هوية crackle كهربائية وmetallic sword. Sword draw مستقل، وثلاث swing variations، وsword impact عند الإصابة فقط. الضربة الأخيرة في sword combo تستخدم Kick مستقلًا؛ أُزيل تكرار swing الأول الذي كان يصدر من بداية ACTIVE ومن أول strike معًا. Block وParry مختلفان طيفيًا عن الإصابة وعن بعضهما.

Shuriken: ready / throw / impact. Fire: **charge → release → impact** بثلاثة أصول مختلفة؛ لا يوجد travel loop.

Chidori: **chidori_charge + chidori_electric_loop أثناء startup → إيقافهما عند ACTIVE → chidori_dash → chidori_impact عند collision فقط → electric_decay عند انتهاء active**. loop ينتهي كذلك عند الإصابة، parry، الإلغاء، pause، KO، إزالة الشخصية، reload، نهاية المباراة، أو حد الأمان 1800 ms. عند استئناف charge بعد pause يبدأ loop فقط للمدة المتبقية من مرحلة الشحن.

## Movement / combat / weapons / sync

Walk وRun وDash يستخدمون `SoundEvent` على poses تماس القدم؛ الصوت ينبعث عند دخول pose فقط. NinjaHop: takeoff / air / landing، والهبوط مربوط بإطار BounceCompress عند العودة إلى الأرض. Jump actions تصدر حدث takeoff مرة واحدة؛ Fall يصدر landing مرة واحدة عند اكتشاف الأرض. Drag يبدأ وينتهي على تغير الحالة فقط. Combat approach يدعم marker في frame الأنيميشن. Combat dodge مربوط بدخول حالة المراوغة.

STARTUP يصدر عند بدء الهجمة، ACTIVE عند تحرير السلاح/بدء الاندفاع، IMPACT بعد collision ناجح. الـimpact يستخدم حزمة المهاجم وموضع الضحية؛ كان المسار القديم يستخدم حزمة الضحية. الضربة التي تفشل أو تصادف Dodge لا تصدر impact. Block/Parry لهما أصواتهما عند التصادم، ولا يصدر Block لمجرد اتخاذ وضع الدفاع. Knockback وheavy landing وKO وVictory أحداث محددة، وليست متكررة أثناء البقاء على الأرض. القتال الحالي يستخدم knockback أرضيًا؛ صوت heavy landing يتبع نهاية تعافي هذا المسار الحالي دون إضافة حركة جديدة للمحرك.

المكتبة العامة تضم punch_light_01/02، punch_heavy_01، kick_01/02، hit_body_01/02، heavy_hit، block، parry، dodge_whoosh، knockback، landing_heavy، KO، victory. لا يوجد صوت flight طويل للأسلحة.

## Limits / cache / threading / cleanup

- أقصى عدد عالمي: **12 software voices**؛ Movement=3، UI=2، Character=2، Combat/Weapon/VFX=8 لكل فئة، مع تطبيق الحد العالمي أيضًا.
- الأولوية: footsteps منخفضة، combat متوسطة، heavy/KO مرتفعة، signature=95. الصوت الأعلى يستبدل الأقل؛ الخطوة لا تقطع signature. حدود كل event تمنع تكدس clones/impacts.
- Cooldown pack/event مشتركة بين نسخ الشخصية: walk=240 ms، run=170 ms، dodge=220 ms، drag/landing=240 ms؛ melee/weapon/signature حسب جدول الأحداث أدناه.
- اختيار عشوائي يستثني الملف السابق عندما توجد variations متعددة. أربع variations فعلية لكل walk/run، وثلاث للسيف.
- PCM LRU: **128 ملفًا / 8 MiB**. WAV input بحد 2 MiB، decoded PCM بحد 1 MiB للملف. metadata لآخر 32 حزمة؛ failed files تتراجع 30 ثانية. لا قراءة من القرص في كل hit.
- عامل ثابت للتعريفات/cache/arbitration، وعامل ثابت للمِكسر. لا decoding، file I/O صوتي، أو line.write على Swing EDT. طلبات play بحد 64، مع ثمانية أماكن محجوزة من movement flood. الطلب الأقدم من 180 ms يُسقط، وطلبات تنظيف الموارد وتحديث الصوت تُدمج.
- **Clip count = 0**. جميع الأصوات تمزج PCM في **SourceDataLine واحدة**، stereo 22050 Hz / PCM16. Gain وpan برمجيان، لذا لا يعتمد عمل sliders على دعم hardware controls. pan محدود ±0.6.
- buffer الجهاز 4096 bytes (~46 ms). إيقاف loop يمنع إنتاجه اللاحق فور معالجة الإلغاء؛ قد تبقى البيانات التي أُرسلت بالفعل في buffer لهذه المدة القصيرة.
- Handles/Scopes تلغي حتى الطلب الذي لم يبدأ. إزالة الشخصية/نهاية المباراة تغلق voices الخاصة بها؛ reload يلغي voices وPCM/metadata للحزمة؛ Streams تُغلق بـtry-with-resources. الخط الصوتي يغلق بعد 400 ms صمت، وعلى خروج التطبيق، ويتوقف العاملان عند الخروج.
- تهيئة صامتة لمرة واحدة أثناء preload تقلل تأخر فتح الجهاز الصوتي لأول مرة، وتبقى خارج EDT.
- الملف المفقود أو event مجهول يسجل warning محدودًا ويستمر القتال. الصيغة غير الصحيحة تعطل الصوت الاختياري فقط.

## Settings / UI

تبويب Audio: Master / Character / Movement / Combat / UI، جميعها sliders 0–100% مع label للنسبة، وMute All. يتم ضرب Master × category × event volume. Character يشمل ردود الفعل/KO/Victory؛ Weapon/VFX يتبعان Combat. تحفظ الإعدادات في `%LOCALAPPDATA%\ShinobiMate\settings.properties`، مع تحقق من القيمة وكتابة ذرية؛ كل الاختبارات استخدمت profiles منفصلة ولم تبدل إعداداتك الشخصية.

UI cues خافتة: click، فتح Character Manager، اختيار الشخصية، spawn، فتح Settings، اختيار Combat، start، end. لها cooldown وحد تزامن مستقلان.

## Validation

- ProfessionalAudioCheck: **639 assertions** على البناء النهائي؛ verified real WAV decoding، priority، category/global limits، flood 500 event، mute/volume، missing-file backoff، LRU، cancellation، reload، EDT isolation، sliders/persistence، actual walk/hop/drag markers، وتوقيت كل هجمات Naruto/Sasuke.
- Native audio: **30 مباريات كاملة متتالية** Naruto vs Sasuke، بتحديث model مع wait=4 ms لتسريع الضغط. **126 assertions**. هذه ليست 30 مباراة بزمن اللعب الطبيعي؛ الفحص المرئي استخدم 40 ms الفعلية.
- Combat regression: **144 مباراة seeded** عبر ثلاث scales مع **289899 assertions / 70813 ticks**.
- Ninja regression: **80 مباراة كاملة** Naruto/Sasuke مع **336524 assertions / 42056 ticks**، واتجاهين واختبارات frames/collision/cleanup.
- Portable runtime smoke: **ثلاثة تشغيلات باردة متتالية PASS على Java 21.0.8 المضمنة**، مع جهاز الصوت الفعلي، Chidori loop، Rasengan sequence، وإغلاق الخط/PCM عند الخروج. الأدلة `runtime-smoke-1.log` إلى `runtime-smoke-3.log`.
- Audio compatibility check: PASS، بما يشمل mute، voices، PCM وnative line cleanup.
- Actual desktop: Naruto idle، Naruto walking، Naruto+Sasuke walking، ثم خمس شخصيات native/Swing؛ 101–102 logic ticks خلال نحو أربع ثوانٍ لكل عينة، دون فقدان شخصية أو stall في الاختبار.
- Visual QA: مشاهدة panel حي وملفات owned-render للشحن والإصابة لـRasengan/Chidori والسيف وKunai/Shuriken/Fire. الأدلة تحت `output/audio-phase1/visual`؛ السجل `timeline.csv` يربط state/frame مع أحداث worker. لم يُسجل صوت جهاز المستخدم ولم أستمع إليه عبر الأداة.

## CPU / RAM: actual desktop

الأرقام التالية نسب CPU من **نواة واحدة** وليست نسبة الجهاز كله. عينات قصيرة (~4 s)؛ process CPU يشمل كل الرسم/المنطق وتهيئة/JIT، وaudio CPU مجموع وقت العاملين الصوتيين فقط. Heap يشمل الصور وSwing، ولا يمثل ذاكرة الصوت وحده. أصوات حركة Phase 1 تخص Naruto/Sasuke؛ وجود الشخصيات الثلاث الأخرى يختبر تحميل ورسم خمس شخصيات، بينما اختبار الصوت العام أدناه يدفع أحداث حركة من الخمس أيضًا.

| الحالة | CPU العملية، % نواة واحدة | CPU الصوت، % نواة واحدة | Heap peak MiB | PCM MiB | خيوط العملية | خيوط الصوت | أعلى Voices | Lines | Clips |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 — idle | 49.372 | 0.000 | 45.622 | 0.746 | 20 | 2 | 0 | 0 | 0 |
| 1 — walking | 26.357 | 0.382 | 49.056 | 0.746 | 20 | 2 | 1 | 0 | 0 |
| 2 — walking | 26.874 | 0.768 | 78.268 | 1.602 | 20 | 2 | 2 | 1 | 0 |
| 5 — walking | 46.409 | 0.384 | 130.366 | 1.602 | 20 | 2 | 2 | 1 | 0 |

## CPU / RAM: audio-only event stress

الـaudio event stress يستخدم إخراج الجهاز الحقيقي ويولد أحداثًا لـ0/1/2/5 actors، دون تكلفة رسم الشخصيات. الفرق بين عينتي 1 و2 يتأثر بالإحماء/JIT؛ لا ينبغي استنتاج scaling خطي من عينة أربع ثوانٍ.

| الحالة | CPU العملية، % نواة واحدة | CPU الصوت، % نواة واحدة | Heap peak MiB | PCM MiB | خيوط العملية | خيوط الصوت | أعلى Voices | Lines | Clips |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 0 — audio events | 3.444 | 1.148 | 29.920 | 0.755 | 12 | 2 | 0 | 0 | 0 |
| 1 — audio events | 11.163 | 2.310 | 30.960 | 0.755 | 12 | 2 | 1 | 1 | 0 |
| 2 — audio events | 4.999 | 1.538 | 31.920 | 0.786 | 12 | 2 | 2 | 1 | 0 |
| 5 — audio events | 6.532 | 2.306 | 33.928 | 0.816 | 12 | 2 | 3 | 1 | 0 |

## Soak / native lines

- المباريات: 30؛ مجموع التحديثات: 14921.
- بعد **كل** مباراة: voices=0 وloops=0، ثم nativeLines=0 بعد idle grace. عند الخروج: PCM cache=0، lines=0، voices=0.
- أعلى voices=12، أعلى pending=7؛ final cache=90 files / 1.489 MiB. حدود LRU اختبرت أيضًا بـ155 WAV فعليًا، وبقي cache ≤128.
- 3458 أحداث شُغلت، 1173 أُسقطت بسبب الحدود/cooldown، 175 استبدالًا بالأولوية. failures=0 وdeviceFailures=0 في اختبار الجهاز النهائي.
- خيوط عملية soak بقيت عادة 12، مع قمة عابرة 13؛ عامل الصوت واحد والمِكسر واحد. Desktop بقي peakThreads=20 وaudioThreads=2 مع 1/2/5 شخصيات. لا إنشاء thread لكل صوت.
- Heap أثناء soak يتذبذب مع allocations/GC؛ PCM الفعلي bounded وليس اتجاه Heap الخام دليلًا على صوت متسرب.
- نسخة Clip الأولية كشفت توقفًا أثناء الضغط، لذلك استبدلت قبل التسليم بالمِكسر ذي الخط الواحد؛ النتائج أعلاه تخص المحرك النهائي.

## Missing / placeholders

لا توجد ملفات SFX لازمة مفقودة لـPhase 1. رد `grunt` حاليًا **generic generated breath placeholder**؛ لا أصوات أصلية أو تقليد لممثلين. attack/hit/victory reactions قابلة للتبديل داخل الحزمة؛ bespoke voiced reactions ليست ضمن المطلوب الإلزامي. Drag Move الاختياري لم يفعّل. حزم Gojo/Sukuna/Asta تستفيد من fallback والأحداث العامة الحالية، ولم يتم تنفيذ Sound Identity كامل لها في هذه المرحلة.

## Build / delivery

`mvn clean package`: **BUILD SUCCESS** باستخدام Maven 3.9.9 المرفق وJDK مع target Java 21. سجل البناء النهائي: `output/audio-phase1/build.log`، BUILD SUCCESS بتاريخ 12 أكتوبر 2026، 11:59:59 (+03:00)، خلال 12.680 ثانية. تحذيرات shading الموجودة تتعلق بالـmanifest/license/module-info ولا تمنع النجاح.

الـJAR الناتج: `target/ShinobiMate.jar`. تُحدث النسخة المحلية `Shimeji-ee/app/Shimeji-ee.jar` وحزم Naruto/Sasuke الصوتية و`Shimeji-ee/audio/common` لتجربتها من launcher الموجود. نسخة الـJAR السابقة محفوظة في `output/audio-phase1/previous-Shimeji-ee.jar`.

لم أنفذ git add أو git commit أو git push. لم أنشر build أو أغير installer المثبت. لم أضف Store أو XP أو Marketplace أو Backend أو Achievements، ولم أعد تصميم منطق القتال.

لإعادة الاختبارات: `tools/check-audio-phase1.ps1 -Jdk <JDK> -Native -Desktop`. الأدلة الرقمية: `performance.json`، `desktop-performance.json`، `native-soak.json`، `native-observed-events.txt` داخل `output/audio-phase1`.

## Complete sound-event inventory

### common: 44 أحداث

| Event | Category | Variations | Volume | Cooldown ms | Concurrent | Loop | Priority |
|---|---|---:|---:|---:|---:|---|---:|
| `movement.footstep` | MOVEMENT | 4 | 0.4 | 240 | 2 | False | 10 |
| `movement.run` | MOVEMENT | 4 | 0.45 | 170 | 2 | False | 12 |
| `movement.jump` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.air` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landing` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landingHeavy` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 25 |
| `movement.drag.start` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.drag.release` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `dodge` | MOVEMENT | 2 | 0.55 | 220 | 2 | False | 35 |
| `combat.punch` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.kick` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.light` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.heavy` | COMBAT | 1 | 0.7 | 140 | 2 | False | 85 |
| `combat.start` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `block` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `parry` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `combat.knockback` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `ko` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 85 |
| `victory` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 55 |
| `weapon.ready` | WEAPON | 1 | 0.5 | 160 | 2 | False | 50 |
| `weapon.kunai.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.kunai.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `weapon.shuriken.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.shuriken.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `combat.punch.heavy` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `impact` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `heavy` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `charge` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `chakra` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `electric` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `fire` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `dark` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `whoosh` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `sword` | COMBAT | 1 | 0.65 | 110 | 2 | False | 50 |
| `grunt` | CHARACTER | 1 | 0.65 | 110 | 2 | False | 50 |
| `ui` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.click` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.manager.open` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.character.select` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.character.spawn` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.settings.open` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.combat.select` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.combat.start` | UI | 1 | 0.22 | 180 | 2 | False | 20 |
| `ui.combat.end` | UI | 1 | 0.22 | 180 | 2 | False | 20 |

### NarutoShippuden: 31 أحداث

| Event | Category | Variations | Volume | Cooldown ms | Concurrent | Loop | Priority |
|---|---|---:|---:|---:|---:|---|---:|
| `movement.footstep` | MOVEMENT | 4 | 0.4 | 240 | 2 | False | 10 |
| `movement.run` | MOVEMENT | 4 | 0.45 | 170 | 2 | False | 12 |
| `movement.jump` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.air` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landing` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landingHeavy` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 25 |
| `movement.drag.start` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.drag.release` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `dodge` | MOVEMENT | 2 | 0.55 | 220 | 2 | False | 35 |
| `combat.punch` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.kick` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.light` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.heavy` | COMBAT | 1 | 0.7 | 140 | 2 | False | 85 |
| `combat.start` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `block` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `parry` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `combat.knockback` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `ko` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 85 |
| `victory` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 55 |
| `weapon.ready` | WEAPON | 1 | 0.5 | 160 | 2 | False | 50 |
| `weapon.kunai.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.kunai.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `weapon.shuriken.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.shuriken.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `vfx.clone.in` | VFX | 1 | 0.6 | 250 | 1 | False | 50 |
| `vfx.clone.out` | VFX | 1 | 0.4 | 250 | 1 | False | 50 |
| `signature.rasengan.charge` | VFX | 1 | 0.7 | 220 | 2 | False | 95 |
| `signature.rasengan.ready` | VFX | 1 | 0.7 | 220 | 2 | False | 95 |
| `signature.rasengan.dash` | VFX | 1 | 0.7 | 220 | 2 | False | 95 |
| `signature.rasengan.impact` | VFX | 1 | 0.9 | 220 | 2 | False | 95 |
| `signature.rasengan.after` | VFX | 1 | 0.7 | 220 | 2 | False | 95 |

### SasukeShippuden: 35 أحداث

| Event | Category | Variations | Volume | Cooldown ms | Concurrent | Loop | Priority |
|---|---|---:|---:|---:|---:|---|---:|
| `movement.footstep` | MOVEMENT | 4 | 0.4 | 240 | 2 | False | 10 |
| `movement.run` | MOVEMENT | 4 | 0.45 | 170 | 2 | False | 12 |
| `movement.jump` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.air` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landing` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.landingHeavy` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 25 |
| `movement.drag.start` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `movement.drag.release` | MOVEMENT | 1 | 0.4 | 240 | 2 | False | 15 |
| `dodge` | MOVEMENT | 2 | 0.55 | 220 | 2 | False | 35 |
| `combat.punch` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.kick` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.light` | COMBAT | 2 | 0.7 | 140 | 2 | False | 55 |
| `combat.body.heavy` | COMBAT | 1 | 0.7 | 140 | 2 | False | 85 |
| `combat.start` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `block` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `parry` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `combat.knockback` | COMBAT | 1 | 0.7 | 140 | 2 | False | 55 |
| `ko` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 85 |
| `victory` | CHARACTER | 1 | 0.7 | 600 | 2 | False | 55 |
| `weapon.ready` | WEAPON | 1 | 0.5 | 160 | 2 | False | 50 |
| `weapon.kunai.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.kunai.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `weapon.shuriken.throw` | WEAPON | 2 | 0.65 | 110 | 2 | False | 50 |
| `weapon.shuriken.impact` | WEAPON | 2 | 0.65 | 110 | 2 | False | 65 |
| `weapon.sword.draw` | WEAPON | 1 | 0.6 | 220 | 2 | False | 50 |
| `weapon.sword.swing` | WEAPON | 3 | 0.75 | 140 | 2 | False | 60 |
| `weapon.sword.impact` | WEAPON | 2 | 0.8 | 120 | 2 | False | 70 |
| `signature.chidori.charge` | VFX | 1 | 0.65 | 200 | 2 | False | 95 |
| `signature.chidori.loop` | VFX | 1 | 0.65 | 200 | 2 | True | 95 |
| `signature.chidori.dash` | VFX | 1 | 0.65 | 200 | 2 | False | 95 |
| `signature.chidori.impact` | VFX | 1 | 0.85 | 200 | 2 | False | 95 |
| `signature.chidori.after` | VFX | 1 | 0.65 | 200 | 2 | False | 95 |
| `signature.fire.charge` | VFX | 1 | 0.75 | 220 | 2 | False | 80 |
| `signature.fire.release` | VFX | 1 | 0.75 | 220 | 2 | False | 80 |
| `signature.fire.impact` | VFX | 1 | 0.75 | 220 | 2 | False | 80 |

