# ShinobiMate — Naruto & Sasuke Combat Polish — 0.9.3-beta

11 أكتوبر 2026. التحسينات الفنية تخص NarutoShippuden وSasukeShippuden. احتفظت بالمحرك الحالي، وأصلحت في النواة المشتركة حركة dash ومواضع VFX/projectiles/clone/HUD فقط. لم أنفذ git add/commit/push.

## 1. ما الذي كان خاطئًا في اتجاه ساسكي؟

ImagePairLoader يتوقع صورة مصدر متجهة لليسار، ويعكسها عند lookRight=true. أطلس ساسكي القديم خلط وقفات تنظر لليمين مع اتصالات سيف وتشيدوري لليسار. لذلك boolean وحساب hitbox صحيحان لا يكفيان لعرض جسم بالاتجاه الصحيح. توسيط crop التي تشمل السيف سبّب أيضًا إزاحة وتصغير الجسم. وكان dash المقفل يستطيع عبور الخصم ثم يبدو أنه يهاجم للخلف.

راجعت عينات من الفيديو المرفق كل ثماني ثوانٍ، من أصل92.8s؛ ظهرت هيئة سيف/جسم متضاربة الاتجاه وأدوات مبسطة جدًا. لم أدّع فحص كل إطار من الفيديو.

## 2. إصلاح القتال بالعكس

استبدلت جميع الصور المستخدمة فعليًا في قتال الشخصيتين، بما فيها الحالات العامة وaliases المساعدة، بمصادر ذات اتجاه موحد. مراجعة التوليد كشفت أن sword-start وbackswing-start الجديدتين بقيتا تنظران لليمين؛ صُححتا مرة واحدة عند التصدير، وسُجل orientationCorrected في frames.json. runtime يعكس المصدر الموحد فقط، مع تثبيت مرساة القدمين.

dash يقف قبل عبور الجسم الشخصي للخصم، ولا يتحرك عكسيًا إذا انتقل الهدف خلفه. اتجاه السيف/المؤثر/المقذوف/الصندوق من facing المقاتل نفسه، والاتجاه يبقى مقفلًا خلال الهجمة. اختبرت كل الهجمات على اليمين واليسار وهدفًا خلف المهاجم، مع مراجعة صور native.

## 3. أدوات وحركات ناروتو

Kunai جديدة: نصل ماسي معدني داكن، حافة فاتحة للوضوح، مقبض أحمر ملفوف وحلقة. أضفت Shuriken Throw مخصصة: أربع رؤوس داكنة وثقب وسط، ثماني مراحل دوران وأثر قصير. Rasengan كرة زرقاء ذات مركز أبيض وخطوط swirl، ممسوكة في الراحة.

الحركات: light-combo، kick-combo، kunai، shuriken الجديدة، shadow-clone، rasengan، aerial-ninja. كذلك رسوم approach/dodge/block/hit/KO/victory جديدة. Shadow Clone رسم مؤقت شفاف ولا ينشئ Mascot دائمًا. الأدوات المنطلقة منفصلة وتختفي عند contact أو نهاية عمرها.

## 4. أدوات وحركات ساسكي

Katana مستقيمة، hilt في اليد، تظهر في الهجمات والحراسة المناسبة وتختفي في الوقفة والرميات والتشيدوري. Shuriken واضحة بأربع رؤوس وثقب ودوران. Chidori شرر متفرع حاد أبيض/أزرق في اليد، مختلف عن الكرة الزرقاء.

الحركات: sword-combo بثلاث اتصالات، shuriken، fire-style، chidori، aerial-sword، counter بعد parry؛ مع quick step والحراسة والإصابة وKO والفوز. Sword Slash هو الاتصال الأول في sword-combo، دون إضافة ضربة مكررة بلا ضرورة.

## 5. Rasengan

شحنة منخفضة → كرة جاهزة في اليد الخلفية → اندفاع أمامي بالكرة → contact وانفجار دائري → recoil واستعادة قصيرة. startup520ms، active القصوى640ms، recovery400ms. عند الاتصال يدخل الهجوم الأحادي recovery مبكرًا ويغلق hitbox. الكرة موجودة في الرسمة، وswirl خفيف عند اليد، وrasengan-burst عند الإصابة. heavy hitstop80ms من نموذج القتال، دون sleep على EDT. cooldown والسجل الحديث يمنعان spam.

## 6. Chidori

قرفصاء شحن → كهرباء في اليد الخلفية → جسم مائل ويد ممتدة للأمام → شرر الإصابة → recoil. startup480ms، active القصوى600ms، recovery440ms. electric-hand قرب اليد وchidori-burst عند الاتصال، بدل انفجار دائري عام. dash يبقى في جهة الهدف الأصلية ويقف قبله.

## 7. Sword Slash

تحضير واتصال أول → تحضير وbackswing → kick finish. ضرر النوافذ4+4+7؛ كل نافذة تضر مرة، وتراجع النهاية26px قبل scale والحدود. arc فاتحة قصيرة أمام السيف، تتبع صاحبه وتُلغى عند interruption. aerial-sword أصبح يستخدم اتصالًا هوائيًا مخصصًا. تحضيرا السيف مصححان الاتجاه في الحزمة النهائية.

## 8. Hitboxes

hurtbox بقي46×87 عند(-23,-95)، وليس canvas. melee ناروتو52×42 عند(12,-72). السيف104×34 عند(18,-76)، يغطي امتداد blade الأمامي دون الجذع. Rasengan58×42 عند(18,-62)، وChidori64×42 عند(18,-62)، بما يناسب اليد المنخفضة.

Kunai26×8 عند(-13,-48)، وshuriken16×16 عند(-8,-52)، وfire32×24 عند(-16,-64). المقذوف مستقل، بتصادم swept AABB اتجاهي. نقطة الإطلاق+52px باتجاه الخصم و-44px رأسيًا؛ fire عند-52px. لا damage لكل frame أو إصابة خلفية أو صندوق في recovery. حقول XML اختيارية لموضع المؤثر/الشحنة/المقذوف وimpactEffect، مع defaults القديمة للحزم الأخرى. الملحق يحتوي القيم الفعلية.

## 9. الرسوم وVFX والاتساق

48 pose جديدة،24 لكل شخصية، تشمل كل الحالات ومراحل الهجمات. استخدمت ImageGen المدمجة لإعادة الرسم، والصور المرفقة كمرجع لوضع الجسم والطاقة والسلاح فقط. لم أوزع صور المرجع أو أنسخها كإطارات. المصادر والأوامر في [prompts-0.9.3.json](prompts-0.9.3.json)، وatlas/frames.json/contact-sheet داخل مجلد كل شخصية.

canvas موحد288×288 وfeet anchor=(144,288)، مع baseline آخر صف. الفراغ الشفاف الأكبر يحتوي السيف دون تصغير الجسم كله. التصدير يعتمد ملامح الرأس/الجسم والحزام، بدل مركز crop شاملة السيف؛ فُصلت الصفوف بحسب حدودها الفعلية وأزيلت أجزاء الخلايا المجاورة. هذا توحيد للمرساة والحجم، وليس interpolation؛ بعض الانتقالات ما زالت محدودة بإطارات منفصلة.

176 صورة VFX أصلية مرسومة بالكود،96×96: ثماني مراحل لكل من kunai/shuriken/rasengan/chakra-aura/rasengan-burst/chidori-burst/electric-aura/electric-hand/sword-arc/fire-ball/ninja-whoosh في الحزمتين. وجود مجموعة في الحزمة لا يعني استعمالها في كل هجمات تلك الشخصية. الصور شفافة وقصيرة العمر وتستخدم overlay المباراة الحالية، دون Window لكل ضربة. HUD يعتمد ارتفاع الجسم بدل canvas الشفاف، وclone مثبت حول قدمه مع المرساة الجديدة.

## 10. AI ونتائج الاختبارات

ناروتو: aggression0.90، technique0.42، وزن Rasengan1.35، ضغط اقتراب/melee/قفز مع تنويع الرميات. ساسكي: aggression0.58، technique0.86، counter chance0.55 داخل نافذة parry المحدودة، وزن Chidori1.15. بقيت قيود الكلفة وcooldown والسجل الحديث؛ counter يحتاج نجاح الدفاع المناسب.

| الاختبار المطلوب | النتيجة |
|---|---|
| اتجاه Naruto/Sasuke | كل13 هجمة نجحت يمينًا ويسارًا؛ لا dash عبر الهدف أو إصابة خلفية |
| Naruto Kunai | نجحت جهة الإطلاق والتوقيت/contact؛ راجعت الحلقة والمقبض والنصل في native |
| Rasengan | شحنة/كرة/اندفاع/contact/استعادة، مع صور native للاتجاهين |
| Sword Slash | ثلاث نوافذ منفصلة؛ سيف ممسوك واتصال أمامي وarc؛ صور تحضير/نشاط/استعادة |
| Chidori | اندفاع أمامي وكهرباء اليد وشرر الاتصال؛ صور native في الاتجاهين |
| معارك متعددة | انتهت المعارك وعادت الشخصيات؛ projectiles=0 وtemporary VFX=0 بعد كل مباراة |

NinjaPolishCheck:336,524 assertion،80 معركة مكتملة،40 seed×اتجاهين، ثلاث scales،42,056 tick. يغطي الإصابة الأمامية، ونقل هدف خلف المهاجم بعد بدء الهجمة، وحد الضرر لكل نافذة، وسلامة الأصول والمرساة وNaruto shuriken.

الانحدار: CombatCheck144 معركة/289,899 assertion؛ CombatPolishCheck120 معركة/449,690 assertion؛ CombatVisualCheck380,676 assertion؛ AudioCheck53 assertion، حد مرصود5 Clips مقابل سقف12. المجموع344 معركة نموذج مكتملة، منها168 Naruto/Sasuke تحديدًا. اختبارات الأزواج الأخرى تتحقق من النواة المشتركة دون تحسين رسومها.

Java21 المرفق: معركتان كاملتان للزوج بترتيبي البداية،1,896 assertion، ورصد VICTORY والاستعادة وإزالة overlay وإغلاق الصوت. NinjaNativeMoveCheck:16 activation منفصلة، أربع حركات لكل شخصية×اتجاهين،65 assertion، بصور charge/active/recovery. هذه حالات حركة وليست16 معركة إضافية. المجموع346 معركة كاملة مع المعركتين native.

المراجعة البصرية شملت buffers الأصلية للـnative sprites وطبقة Swing الفعلية في `output/ninja-polish/native-runtime/captures/*-owned-render.png`. التقاط Robot لسطح المكتب لا يُظهر هذه النوافذ على هذا النظام، لذلك لا أسمي تركيب buffers تصويرًا للشاشة. التشغيل والالتقاط آليان والمراجعة للصور مباشرة؛ لم أدّع جلسة لعب بشرية مستمرة أو اختبار عدة شاشات فعلية.

القياسات native مع خمس شخصيات:22 thread في العينتين،4 Clips كحد مرصود،GDI738→788،heap بعدGC98.860→118.554MiB،cachedSounds32→40. CPU نسبة نواة واحدة53.826% ثم26.777%، خلال نحو28 و31 ثانية، وتشمل الالتقاط والتحقق. canvas الأكبر يزيد ذاكرة الصور مقارنة128px. لا baseline متطابقة قبل/بعد أو اختبار ساعات، لذلك لا ادعاء بتحسين CPU بنسبة محددة أو خلو طويل المدة من التسرب. القياسات في native-soak.json.

## 11. هل الأدوات recognizable؟

نعم في عينات native المفحوصة بالحجم الفعلي: kunai بنصل/حلقة/مقبض، shuriken بأربع رؤوس وثقب، Rasengan كرة دوارة، Chidori شرر متفرع، وkatana في اليد باتجاه الضربة. تظهر دون logs. قراءة النصل الداكن تتأثر بخلفية سطح المكتب وScaling؛ أضفت حافة فاتحة لذلك.

## 12. البناء وحفظ الإصدارات

`mvn clean package`: **BUILD SUCCESS**، آخر بناء مصدر15:20:21 بالتوقيت المحلي،13.441s، bytecode21. تحقق native على runtime21 المرفق. Inno Setup:Successful compile63.906s. جرة staging تطابق target. فحص ZIP/launcher نجح:1,476 entry وruntime، دون source/tests/logs. ZIP النهائي يحتوي وقفتَي التحضير المصححتين وXML الجديد.

بقيت Setup/Portable0.9.0 و0.9.2 دون استبدال؛ hash0.9.2 مطابق للتسليم السابق. قارنت504 ملفات لGojo/Sukuna/Asta مع portable0.9.2، كلها مطابقة. لم أثبت installer أو أبدل التطبيق المثبت؛ اختبر الحزمة الجديدة.

## 13. التسليم

- Setup: `D:\programm\Shimeji-ee_2.0.1_Windows_Portable\dist\ShinobiMate-Setup-0.9.3-beta.exe`،49,652,841 byte.
- Portable: `D:\programm\Shimeji-ee_2.0.1_Windows_Portable\dist\ShinobiMate-Portable-0.9.3-beta.zip`،72,420,452 byte.
- Setup SHA256:`e9c6cfee2d8abc933c62a030a1f32a85d41a717370d86aa2eaac94b56eee0a0f`.
- Portable SHA256:`84c322f6111ff628510ff1f055eba29013fe2da13614232ce3d2fd83d31eca4e`.
- [معاينة Naruto](NarutoShippuden/contact-sheet.png)، [معاينة Sasuke](SasukeShippuden/contact-sheet.png)، [أوامر التوليد](prompts-0.9.3.json).
- XML النهائي في `img/<Character>/conf/combat.xml`، والأصول في `combat/ninja093/`، والسجلات في `output/ninja-polish/`.
- أدوات الأصول: `tools/export-ninja-polish.py` و`tools/build-ninja-polish.py`، مع Pillow. إعادة الفحص: `tools/check-ninja-polish.ps1 -Jdk <JDK>`؛ إعادة البناء بعد تعديل المصدر تستلزم إعادة packaging أيضًا.

## حالة الشخصيتين

**NarutoShippuden: POLISHED ضمن نطاق هذه المرحلة.** أسلحة مميزة، Rasengan واضح، shuriken مضافة، phases مخصصة، واتجاهات/contact/cleanup مجتازة.

**SasukeShippuden: POLISHED ضمن نطاق هذه المرحلة.** اتجاه المصدر موحد، تحضيرا السيف مصححان، katana/Chidori/shuriken مقروءة، والـdash لا يعبر الخصم باتجاه مقفل. التصنيف لا يعني نعومة سينمائية كاملة أو ضمان كل DPI؛ عدد الوقفات الحالي محدود، وإحساس اللعب على جهازك قابل للمراجعة.

## ملحق القيم من XML النهائي

كل tick=40ms. cooldown بعد مراحل الهجمة. combos تستخدم strikes المفصلة مرة لكل نافذة؛ damage الأساسي ليس damage لكل frame.

<!-- GENERATED VALUES -->

### NarutoShippuden

| الهجمة | السلاح | damage / نوافذ strikes | min..max range | startup/active/recovery ms | cooldown ms | knockback | VFX / impact | صوت startup/release/impact |
|---|---|---|---|---|---|---|---|---|
| light-combo | none | 4@+0ms/120ms(kb=0), 7@+240ms/120ms(kb=24) | 0..100 | 160/360/280 | 880 | 22 | impact/impact | grunt/whoosh/impact |
| kunai | kunai | 7 | 100..420 | 200/80/320 | 1400 | 10 | kunai/impact | grunt/whoosh/impact |
| shadow-clone | energy | 12 | 85..280 | 400/120/480 | 3200 | 20 | ninja-whoosh/impact | grunt/whoosh/impact |
| rasengan | none | 26 | 0..235 | 520/640/400 | 5600 | 65 | rasengan/rasengan-burst | chakra/whoosh/heavy |
| aerial-ninja | none | 13 | 0..160 | 240/480/480 | 1800 | 26 | slash/impact | grunt/whoosh/impact |
| kick-combo | none | 5@+0ms/120ms(kb=0), 8@+240ms/120ms(kb=28) | 0..100 | 200/360/280 | 1280 | 22 | impact/impact | grunt/whoosh/impact |
| shuriken | shuriken | 6 | 100..420 | 200/80/320 | 1680 | 10 | shuriken/impact | grunt/whoosh/impact |

المرساة الجديدة:288×288، القدم(144,288). الإطارات الـ24:

approach, block, clone-sign, dodge, hit, idle, jump-contact, jump-start, kick-contact, kick-recover, kick-start, ko, kunai-contact, kunai-start, punch-contact, punch-recover, punch-start, rasengan-charge, rasengan-contact, rasengan-low, rasengan-recover, shuriken-contact, shuriken-start, victory

### SasukeShippuden

| الهجمة | السلاح | damage / نوافذ strikes | min..max range | startup/active/recovery ms | cooldown ms | knockback | VFX / impact | صوت startup/release/impact |
|---|---|---|---|---|---|---|---|---|
| sword-combo | sword | 4@+0ms/120ms(kb=0), 4@+240ms/120ms(kb=0), 7@+480ms/120ms(kb=26) | 0..105 | 200/600/400 | 1200 | 28 | sword-arc/impact | sword/sword/impact |
| shuriken | shuriken | 9 | 95..440 | 200/80/400 | 1520 | 16 | shuriken/impact | grunt/whoosh/impact |
| fire-style | energy | 16 | 110..340 | 480/120/560 | 3200 | 34 | fire-ball/impact | fire/fire/heavy |
| chidori | none | 25 | 0..260 | 480/600/440 | 5600 | 58 | electric-hand/chidori-burst | electric/whoosh/electric |
| aerial-sword | sword | 14 | 0..155 | 280/480/480 | 2000 | 30 | sword-arc/impact | sword/sword/impact |
| counter | sword | 12 | 0..105 | 80/160/400 | 2000 | 28 | sword-arc/impact | sword/sword/impact |

المرساة الجديدة:288×288، القدم(144,288). الإطارات الـ24:

approach, backswing-contact, backswing-start, block, chidori-charge, chidori-contact, chidori-low, chidori-recover, dodge, fire-contact, fire-start, hit, idle, jump-contact, kick-contact, kick-recover, kick-start, ko, shuriken-contact, shuriken-start, sword-contact, sword-recover, sword-start, victory