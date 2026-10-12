# SHINOBIMATE — CHARACTER & COMBAT POLISH — 0.9.2-beta

تاريخ التحقق: 11 أكتوبر 2026. تم تحسين المحرك الحالي وحزم الشخصيات، وبناء إصدار مستقل للاختبار. الحالة **PARTIAL** للشخصيات الخمس؛ التحسينات المنطقية مجتازة للاختبارات، لكن اتساق حجم الجسم ومركزه بين الرسوم القديمة والجديدة وبعض الحركات البصرية تحتاج جولة أخرى. لا أعتبر المرحلة مكتملة بصريًا.

## 1. مشكلات التدقيق الفعلية

التدقيق وخطة الحركة في [POLISH-PLAN.md](POLISH-PLAN.md). وُجدت مشكلات في هندسة المقذوف المتجه يسارًا، ومحاولة الدفاع وقت الاصطدام بدل قراءة البداية، وعدم وجود نافذة parry محددة، وتكرار الرسوم مع عمر الحالة بدل عمر مرحلة الهجمة، وعدم تقدم رسوم النهاية، وفحص شاشة طرف واحد فقط، ونقل الطرف الآخر إلى منطقة عمل مختلفة عند البداية. أسماء combo كانت تمثل ضربة واحدة، والتراجع الفعلي أقل من قيمة knockback المكتوبة، واختيار الهجمات بلا سجل حديث، والصوت مستنتجًا من أسماء المؤثرات. أظهرت الاختبارات الأصلية أيضًا اختلاف إحداثيات سطح المكتب المنطقية/الفيزيائية بعد انتهاء القتال، وتداخل المتفرجين مع المقاتلين، وخيط انتظار غير ضروري لكل Manager.

## 2. الإصلاحات

- احتفظت ببنية CombatEngine/CombatMatch الحالية. البداية 25 tick، أي 1000ms، والنهاية 90 tick، أي 3.6s، ثم استعادة السلوك العادي.
- تثبيت الاتجاه أثناء الهجمة، واستخدامه في المقذوف والمؤثر وhitbox؛ تحديث الاتجاه أثناء الحركة خارج الهجمة.
- معالجة الضرر لكل نافذة ضربة بسجل مستقل، وإلغاء hitbox والهجمة عند HIT/KO، وتصفير المقذوفات والمؤثرات عند النهاية.
- cooldown يبدأ بعد startup/active/recovery، وليس بالتوازي معها. سجل آخر أربع هجمات يقلل التكرار ويمنع ثالث تكرار متتالٍ عندما تتوفر بدائل.
- رفض بدء القتال بين منطقتي عمل مختلفتين دون نقل الشخصية، وإلغاء المباراة عند تغيير منطقة عمل أي طرف.
- فصل لطيف حسب الحجم، بحد حركة 4px لكل طرف في tick. الابتعاد خارج القتال يشمل المتفرجين القريبين من المقاتلين. هذا لا يضمن غياب كل تداخل عابر أثناء القفز أو في مناطق ضيقة جدًا.
- إصلاح أرضية Windows عبر إحداثيات Swing المنطقية وtaskbar insets، بدل خلط الإحداثيات الفيزيائية بعد القتال.
- HUD مصغر للاسم وHP فقط؛ يختفي عند النهاية. أيقونة سيفين مرسومة للقائمة العربية بدل الاعتماد على رمز Unicode.
- إزالة خيط الانتظار غير الضروري من Manager. لم أضف متاجر أو حسابات أو XP أو backend.

## 3. الشخصيات والحركات

| الشخصية | الحالة | الحركات والسلوك المتاح | ما بقي بصريًا |
|---|---|---|---|
| Naruto | PARTIAL | punch/kick combos، kunai، clone rush مؤقت، Rasengan charge/dash/contact، jump attack، dodge، victory | dodge قفزة/تراجع وليس backflip دورانيًا كاملًا؛ اتساق الانتقال بين الوقفات يحتاج مراجعة |
| Sasuke | PARTIAL | ثلاث ضربات sword combo، shuriken، fire، Chidori، aerial sword، quick step، counter بعد parry، victory | الجسم أصغر في بعض الرسوم الجديدة بسبب احتواء مدى السيف؛ اختلاف المركز عند الانتقال |
| Gojo | PARTIAL | melee combo، Blue pull، Red push، Purple نادر، دفاع محدود، تراجع أزرق سريع، taunt، victory | dodge ليس انتقالًا فوريًا؛ بعض مراحل الحركة تحتاج إطارات إضافية للنعومة |
| Sukuna | PARTIAL | melee combo، cursed slash، multi slash، heavy cursed، fire arrow، counter، dash، victory | مؤثرات slash هندسية خفيفة؛ نعومة الانتقالات لم تصل إلى جودة نهائية |
| Asta | PARTIAL | ثلاث ضربات sword combo، heavy sword، dash/jump sword، block/parry، anti-magic burst/dash، victory | تفاوت واضح لحجم الجسم مقابل الوقفة القديمة بسبب مساحة السيف؛ مركز الجسم يحتاج تصحيحًا |

كل الهجمات الـ28 تستخدم رسوم بداية/اتصال/استعادة مخصصة، ولا تعتمد هجماتها الرئيسية على Idle/Walk. لكن ذلك وحده لا يثبت اكتمال جودة الحركة. جدول الضربات والتوقيت والصوت الفعلي من XML مرفق في نهاية التقرير.

## 4. الأسلحة

WeaponSystem مستقل لهندسة المقذوف ودورة حياته: kunai، shuriken، sword، energy، slash، وnone. السيف ممسوك في رسوم الهجمة ويختفي بالرجوع إلى الوقفة المناسبة؛ ليس نافذة مستقلة عائمة. المقذوفات لها spawn/direction/speed/lifetime/hitbox/damage/effect/destroyOnHit. ليس هناك نظام تجهيز أسلحة أو مخزون جديد.

## 5. المقذوفات

الإطلاق عند القدم +32px باتجاه الخصم وارتفاع 60px، مع ضرب القيم في scale. عمر المقذوف min(250, ceil(range/speed)+2) tick. التصادم يستخدم swept AABB اتجاهيًا بين الموضع القديم والجديد، ويزيل المقذوف عند الاصطدام أو انتهاء العمر أو نهاية المباراة. Multi Slash يطلق مقذوفين مستقلين؛ clone rush رسم مؤقت متحرك، وليس Mascot جديدًا. قائمة المقذوفات وقيمها الفعلية في الملحق.

## 6. VFX

أُضيفت مجموعات chakra/cursed/anti-magic/purple/shockwave، أربع صور لكل مجموعة لكل حزمة: 100 صورة مؤثر جديدة. بقيت المجموعات الأصلية وأعيد استخدامها. جميعها شفافة وتُرسم في overlay واحدة لكل مباراة؛ لا تُفتح Window لكل ضربة. الشحنة والمؤثر النشط يتبعان صاحبهما ويُلغيان مع الهجمة، أما impact/dust/shockwave فلهما عمر محدود. لم أضف pool لكائنات VFX الصغيرة لأن العدد والعمر محدودان؛ overlay والصور يعاد استخدامهما. القائمة النهائية من XML في الملحق، دون blood/gore.

## 7. Sound Events

16 حدثًا أصليًا مولدًا حسابيًا: UI، GRUNT، KO، VICTORY، CHARGE، CHAKRA، ELECTRIC، FIRE، DARK، WHOOSH، SWORD، IMPACT، HEAVY، PARRY، BLOCK، DODGE. الهجمات تعرّف startupSound/attackSound/impactSound صراحة؛ الدفاع وKO والفوز لها hooks مستقلة. ثلاث variations لكل حدث ولكل شخصية، مع اختلاف طبقة/طابع الهوية. أصوات GRUNT/KO/VICTORY مؤثرات تركيبية placeholder، وليست تسجيل ممثل أو صوت بشري مقنع. لم أستخدم صوت أنمي أو موسيقى محمية.

## 8. Animations الجديدة

80 pose جديدة، 16 لكل شخصية، إضافة إلى 80 pose الأصلية. ولّدتها أداة ImageGen المدمجة ثم فُصلت إلى PNG شفافة 128×128، مع baseline عند 128. الأصول والأوامر في [prompts-0.9.2.json](prompts-0.9.2.json)، وكل حزمة تحتوي `0.9.2/atlas.png` و`frames.json`. [لوحة المعاينة](contact-sheet-0.9.2.png).

نجحت اختبارات alpha، اختلاف الصور، الحدود وbaseline. هذه الاختبارات لا تثبت اتساق حجم الجسم أو مركزه: الإطارات ذات السيف الكبير مصغرة لاستيعابه، وبعض الانتقالات إلى الرسوم القديمة تقفز بصريًا. هذا سبب جوهري لحالة PARTIAL. رسوم الهجمات والنهاية غير دورية وتستخدم عمر المرحلة، لكن لم أضف interpolation أو مجموعة إطارات وسيطة كاملة لكل انتقال.

## 9. Hitboxes

hurtbox الجسم 46×87 عند (-23,-95) بالنسبة إلى القدم، مع flip/scale، وليس canvas كاملة. melee العادي أصبح 56×50 عند (12,-80) بدل 86×100 عند (14,-102)، والسيف 72×50. المقذوف أصبح 20×22 عند (-10,-71) بدل 24×32. الهجمة الأحادية تضرب مرة؛ combo تضرب مرة لكل strike مقصودة، ولا يوجد damage لكل frame. النوافذ الفعلية لكل هجمة مدرجة في الملحق.

## 10. Knockback والتوقيت

تراجع متناقص مُطبّع ليقترب مجموع الحركة من قيمة الهجمة، مع حدود منطقة العمل. نهايات combo فقط تحمل التراجع الأقوى. Blue يستخدم قيمة سالبة للسحب، Red دفع 70، Purple دفع 75، وتختلف الضربات الأخرى كما في الملحق. heavy impact يجمّد تقدم نموذج القتال لمدة tickين =80ms؛ لا sleep على EDT. القفز يكمل قوسه ثم يهبط إلى الأرضية المنطقية الصحيحة.

## 11. AI والدفاع

الاختيار يأخذ المسافة، الكلفة، cooldown، حالة الخصم/استعادته، HP والسجل الحديث. الأوزان مختلفة لكل شخصية كما في الملحق. يبدأ الدفاع قرب نهاية startup؛ الهجمات الثقيلة/special تزيد احتمال dodge. dodge له عمر 10 ticks وحماية فقط خلال العمر 1..7، وblock مدته 14 ticks، وparry ممكن في أول ثلاثة ticks فقط مع chance. نجاح parry على melee يقطع هجمة الخصم ويتيح counter؛ هجمات counter لا تكون متاحة عشوائيًا قبل ذلك. Gojo له defense cooldown=45 tick ودفاع محدود وقابل للإصابة. النظام احتمالي ومقيد بحالات، وليس تخطيطًا طويل المدى أو إعادة تمثيل دقيقة لشخصيات الأنمي.

## 12. Audio architecture

عامل daemon واحد مع queue بحد 32؛ الإنشاء والتشغيل خارج EDT. PCM مولّد ومخزّن، بحد 240 مجموعة شخصية/حدث/variation، دون قراءة ملف لكل تشغيل. حد 12 Clip أصليًا؛ يُعاد استخدام Clip المتوقف أو يغلق قبل تبديله. dedup/cooldown للصوت 70ms، وتجاهل الطلب عند ازدحام queue. انقطاع الجهاز لا يعطل القتال، وإعادة المحاولة بعد 30s. PAN وMASTER_GAIN عند دعم الجهاز. Master/Character/Combat/SFX وMute محفوظة بالإعدادات؛ gain الشخصية =master×character، والمؤثر =master×combat×SFX. إغلاق الخدمة يفرغ clips/cache. اختبار الصوت الفعلي فتح 4 Clips كحد مرصود مقابل الحد 12.

## 13. CPU / RAM والموارد

اختبار النموذج الإضافي: 58,490 tick، زمن 0.285s، CPU 1.078s، متوسط 18.433µs CPU/tick، وأبطأ tick 1.351ms. heap بعد GC انخفض من 4.062 إلى 2.359MiB، والخيوط 8→8، ولا timers جديدة. هذه أرقام نموذج سريع دون رسم native/audio كامل؛ لا تمثل تكلفة التطبيق على سطح المكتب.

النسخة النهائية Java21، خمس شخصيات وثلاث معارك: CPU نسبة نواة واحدة 51.545% /26.495% /30.640%، heap بعد GC 59.082 /74.093 /83.516MiB، 21 thread في العينات الثلاث، Clips حد مرصود3، cached sounds27→57→77. GDI760→953→1068 بسبب تحميل الصور والموارد تدريجيًا. أرقام CPU تشمل تكلفة تحقق والتقاط صور الاختبار، وليست مقارنة دقيقة مع الإصدار السابق. نمو heap/GDI القصير لا يثبت leak، لكنه أيضًا لا يسمح بادعاء اختبار تسرب طويل المدة. لا توجد baseline متطابقة قبل/بعد لقياس نسبة تحسين الأداء.

## 14. Soak والاختبارات

- CombatCheck: 144 معركة مكتملة، 48 لكل زوج مطلوب، ثلاث scales وإحداثيات شاشة سالبة، 292,268 assertion و71,573 tick.
- CombatPolishCheck: 120 معركة مكتملة إضافية، 20 seed ×3 أزواج ×اتجاهين، scales مختلطة، 452,450 assertion. تغطية المقذوف يمينًا/يسارًا، الفصل بين الشاشات، تغيير شاشة الطرف الثاني، cooldown، combos، parry/counter، 80ms pause، نهاية/KO/تنظيف.
- أقصى VFX=9 والمقذوفات=3 في هذا soak. بعد كل مباراة: projectiles=0 وVFX=0، ولا both KO أو حالة عالقة في الحالات المختبرة.
- CombatVisualCheck: 380,673 assertion للصور الأصلية الـ80 ومراحل الهجمات. الفحص الإضافي يثبت الصور الجديدة الـ80 ومراحل 28 هجمة.
- AudioCheck: 50 assertion، أصوات/variation/gain/mute/حد clips/close. SettingsCheck:20، RuntimeStabilityCheck:11.
- تشغيل Windows native: ست معارك تطوير على Java25، ثم ثلاث معارك نهائية على Java21 المرفق. النهائي نجح في 2,650 assertion، عبر القائمة العربية، ورصد VICTORY فعليًا، ثم استعادة السلوك وإزالة overlay وإغلاق الصوت.
- فحص التوزيع نجح: ZIP يحوي 1,252 entry وruntime، دون source/tests/logs؛ hash الجرة المعبأة يطابق target.

المراجعة البصرية اليدوية المتاحة كانت للـatlases وصور buffers الفعلية للشخصيات وطبقة HUD، المحفوظة في `output/combat-polish/native-runtime/captures/*-owned-render.png`. التقاط سطح المكتب عبر Robot أظهر خلفية وإشعارات دون الشخصيات native، لذلك لا أدعي تنفيذ مراجعة تفاعل يدوي كاملة على سطح المكتب. يلزم اختبارك التفاعلي للتحقق من الإحساس والنعومة وDPI والشاشات الواقعية. اختبارات الشاشات الآلية لا تعوض تحققًا بصريًا على عدة شاشات فعلية.

## 15. عدد المعارك

264 معركة آلية مكتملة للنموذج، إضافة إلى ست معارك native أثناء التطوير وثلاث بالنسخة النهائية: **273 معركة مكتملة ضمن مجموعات التحقق المذكورة**. لم أضم إعادة تشغيل الاختبارات أثناء التصحيح إلى هذا العدد. الأزواج: Naruto/Sasuke، Gojo/Sukuna، Asta/Naruto. السجلات في `output/combat-polish/`، والقياسات النهائية في `native-runtime/native-soak.json`.

## 16. نتيجة البناء

`mvn clean package`: BUILD SUCCESS، باستخدام JDK25 مع إصدار bytecode21. حزمة الإصدار تستخدم runtime Java21، وشُغلت به المعارك النهائية. نجح بناء portable وInno Setup. لم أجر تثبيت/إزالة تثبيت installer على نظام المستخدم؛ البناء وفحص المحتويات والتشغيل من الحزمة نجحت. لم أنفذ git add/commit/push، وحافظت على التعديلات السابقة غير المتعلقة بالمهمة.

## 17. ملفات التسليم

- Setup: `D:\programm\Shimeji-ee_2.0.1_Windows_Portable\dist\ShinobiMate-Setup-0.9.2-beta.exe`، 48,452,806 byte.
- Portable: `D:\programm\Shimeji-ee_2.0.1_Windows_Portable\dist\ShinobiMate-Portable-0.9.2-beta.zip`، 71,019,611 byte.
- Setup SHA256: `f935f3b139d779f7e54d20e6c78a0b34e2c61a7938195817be1ce34ecc1b8df7`.
- Portable SHA256: `96ac18b77402c33efe04097dcd748933d43e5bfa5a0df20cad04d87b904aa906`.

بقيت ملفات Setup/Portable 0.9.0 السابقة وتحققنا من عدم تغير SHA256. جرّب 0.9.2 من الحزمة الجديدة؛ لم أستبدل التطبيق المثبت أو launcher القديم في مكانه.

## ملحق القيم الفعلية

الملحق التالي مستخرج مباشرة من XML النهائي. كل tick=40ms. التوقيت المعروض startup/active/recovery؛ cooldown إضافي بعد انتهاء المراحل. damage الهجمة في combo هو مجموع strikes، وdamage لكل مقذوف عند وجود strikes متعددة.

<!-- GENERATED-VALUES -->

### NarutoShippuden

| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |
|---|---|---|---|---|---|---|---|---|---|
| light-combo | melee/none | 4@+0ms/120ms(kb=0), 7@+240ms/120ms(kb=24) | 0..100 | 160/360/280 | 880 | 22 | grunt/whoosh/impact | impact | 12,-80,56,50 |
| kunai | ranged/kunai | 7 | 100..420 | 200/80/320 | 1400 | 10 | grunt/whoosh/impact | projectile | -10,-71,20,22 |
| shadow-clone | ranged/energy | 12 | 85..280 | 400/120/480 | 3200 | 20 | grunt/whoosh/impact | chakra | -10,-71,20,22 |
| rasengan | special/none | 26 | 0..235 | 520/560/680 | 5600 | 65 | chakra/whoosh/heavy | chakra | 12,-80,56,50 |
| aerial-ninja | aerial/none | 13 | 0..160 | 240/480/480 | 1800 | 26 | grunt/whoosh/impact | slash | 12,-80,56,50 |
| kick-combo | melee/none | 5@+0ms/120ms(kb=0), 8@+240ms/120ms(kb=28) | 0..100 | 200/360/280 | 1280 | 22 | grunt/whoosh/impact | impact | 12,-80,56,50 |

المقذوفات (speed بوحدة px/tick قبل scale):

| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |
|---|---|---|---|---|---|---|
| kunai | kunai | 20 | 23 | 7 | 1 | false |
| shadow-clone | energy | 14 | 22 | 12 | 1 | true |

AI/defense: aggression=0.85; block=0.06; blockEffect=spark; blockReduction=0.55; counter=0.15; defenseCooldown=22; defenseCost=18; dodge=0.38; dodgeEffect=dust; dodgeSpeed=9; jumpHeight=35; projectileParry=0; taunt=0.0; technique=0.35

VFX IDs: anti-magic, blue, chakra, charge, cursed, dust, energy, fire, hitflash, impact, lightning, projectile, purple, red, shockwave, slash, spark

الإطارات الجديدة الـ16: clone-sign, jump-contact, jump-start, kick-contact, kick-recover, kick-start, kunai-contact, kunai-start, new-victory, punch-contact, punch-recover, punch-start, rasengan-charge, rasengan-contact, rasengan-low, rasengan-recover


### SasukeShippuden

| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |
|---|---|---|---|---|---|---|---|---|---|
| sword-combo | melee/sword | 4@+0ms/120ms(kb=0), 4@+240ms/120ms(kb=0), 7@+480ms/120ms(kb=26) | 0..105 | 200/600/400 | 1200 | 28 | sword/sword/impact | slash | 12,-80,72,50 |
| shuriken | ranged/shuriken | 9 | 95..440 | 200/80/400 | 1520 | 16 | grunt/whoosh/impact | projectile | -10,-71,20,22 |
| fire-style | ranged/energy | 16 | 110..340 | 480/120/560 | 3200 | 34 | fire/fire/heavy | fire | -10,-71,20,22 |
| chidori | special/none | 25 | 0..260 | 560/480/680 | 5600 | 58 | electric/whoosh/electric | lightning | 12,-80,56,50 |
| aerial-sword | aerial/sword | 14 | 0..155 | 280/480/480 | 2000 | 30 | sword/sword/impact | slash | 12,-80,72,50 |
| counter | melee/sword | 12 | 0..105 | 80/160/400 | 2000 | 28 | sword/sword/impact | slash | 12,-80,72,50 |

المقذوفات (speed بوحدة px/tick قبل scale):

| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |
|---|---|---|---|---|---|---|
| shuriken | shuriken | 24 | 21 | 9 | 1 | false |
| fire-style | energy | 13 | 29 | 16 | 1 | false |

AI/defense: aggression=0.5; block=0.13; blockEffect=spark; blockReduction=0.65; counter=0.65; defenseCooldown=20; defenseCost=20; dodge=0.3; dodgeEffect=dust; dodgeSpeed=12; jumpHeight=0; projectileParry=0; taunt=0.0; technique=0.65

VFX IDs: anti-magic, blue, chakra, charge, cursed, dust, energy, fire, hitflash, impact, lightning, projectile, purple, red, shockwave, slash, spark

الإطارات الجديدة الـ16: backswing-contact, backswing-start, chidori-charge, chidori-contact, chidori-low, chidori-recover, fire-contact, fire-start, kick-contact, kick-recover, kick-start, shuriken-contact, shuriken-start, sword-contact, sword-recover, sword-start


### GojoSatoru

| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |
|---|---|---|---|---|---|---|---|---|---|
| fast-melee | melee/none | 3@+0ms/120ms(kb=0), 3@+240ms/120ms(kb=0), 6@+480ms/120ms(kb=25) | 0..100 | 120/600/240 | 800 | 18 | grunt/whoosh/impact | impact | 12,-80,56,50 |
| blue-pull | ranged/energy | 11 | 90..350 | 360/80/480 | 2200 | -48 | chakra/chakra/impact | blue | -10,-71,20,22 |
| red-blast | ranged/energy | 14 | 90..360 | 440/80/560 | 2600 | 70 | chakra/whoosh/heavy | red | -10,-71,20,22 |
| blue-red-special | special/energy | 35 | 0..390 | 720/80/840 | 10000 | 75 | chakra/chakra/heavy | purple | -10,-71,20,22 |

المقذوفات (speed بوحدة px/tick قبل scale):

| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |
|---|---|---|---|---|---|---|
| blue-pull | energy | 16 | 24 | 11 | 1 | false |
| red-blast | energy | 18 | 22 | 14 | 1 | false |
| blue-red-special | energy | 16 | 27 | 35 | 1 | false |

AI/defense: aggression=0.4; block=0.29; blockEffect=blue; blockReduction=0.8; counter=0.25; defenseCooldown=45; defenseCost=27; dodge=0.24; dodgeEffect=blue; dodgeSpeed=13; jumpHeight=0; projectileParry=0; taunt=0.1; technique=0.9

VFX IDs: anti-magic, blue, chakra, charge, cursed, dust, energy, fire, hitflash, impact, lightning, projectile, purple, red, shockwave, slash, spark

الإطارات الجديدة الـ16: blue-contact, blue-start, elbow-contact, elbow-start, kick-contact, kick-recover, kick-start, punch-contact, punch-start, purple-charge, purple-contact, purple-low, purple-recover, red-contact, red-start, taunt


### SukunaRyomen

| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |
|---|---|---|---|---|---|---|---|---|---|
| melee-strike | melee/none | 4@+0ms/120ms(kb=0), 4@+240ms/120ms(kb=0), 8@+480ms/120ms(kb=36) | 0..100 | 160/600/400 | 1080 | 34 | grunt/whoosh/impact | impact | 12,-80,56,50 |
| cursed-slash | ranged/slash | 11 | 90..430 | 240/80/400 | 1520 | 24 | grunt/whoosh/impact | slash | -10,-71,20,22 |
| multi-slash | ranged/slash | 7@+0ms/120ms(kb=0), 10@+240ms/120ms(kb=38) | 65..290 | 400/360/560 | 2800 | 38 | grunt/whoosh/impact | slash | -10,-71,20,22 |
| heavy-cursed | special/none | 28 | 0..240 | 600/480/800 | 5600 | 70 | dark/whoosh/heavy | cursed | 12,-80,56,50 |
| counter-strike | melee/none | 12 | 0..105 | 120/200/320 | 1280 | 25 | grunt/whoosh/impact | slash | 12,-80,56,50 |
| fire-arrow | special/slash | 24 | 90..430 | 640/200/400 | 7200 | 24 | fire/fire/heavy | fire | -10,-71,20,22 |

المقذوفات (speed بوحدة px/tick قبل scale):

| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |
|---|---|---|---|---|---|---|
| cursed-slash | slash | 22 | 22 | 11 | 1 | false |
| multi-slash | slash | 16 | 21 | 7/10 | 2 | false |
| fire-arrow | slash | 18 | 26 | 24 | 1 | false |

AI/defense: aggression=0.95; block=0.08; blockEffect=spark; blockReduction=0.55; counter=0.7; defenseCooldown=22; defenseCost=18; dodge=0.18; dodgeEffect=dust; dodgeSpeed=8; jumpHeight=0; projectileParry=0; taunt=0.0; technique=0.55

VFX IDs: anti-magic, blue, chakra, charge, cursed, dust, energy, fire, hitflash, impact, lightning, projectile, purple, red, shockwave, slash, spark

الإطارات الجديدة الـ16: curse-contact, curse-start, dash-contact, fire-charge, fire-contact, fire-start, heavy-contact, heavy-recover, heavy-start, multi-contact, multi-start, new-victory, punch-contact, punch-start, slash-contact, slash-start


### AstaBlackClover

| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |
|---|---|---|---|---|---|---|---|---|---|
| sword-slash | melee/sword | 4@+0ms/120ms(kb=0), 5@+240ms/120ms(kb=0), 9@+480ms/120ms(kb=40) | 0..115 | 240/600/440 | 1200 | 32 | sword/sword/impact | slash | 12,-80,72,50 |
| heavy-sword | melee/sword | 20 | 0..120 | 600/320/720 | 2600 | 55 | sword/sword/heavy | impact | 12,-80,72,50 |
| dash-slash | dash/sword | 15 | 90..245 | 320/560/560 | 2200 | 40 | sword/sword/impact | slash | 12,-80,72,50 |
| jumping-sword | aerial/sword | 16 | 0..170 | 320/560/560 | 2200 | 40 | sword/sword/impact | slash | 12,-80,72,50 |
| anti-magic | special/sword | 29 | 0..180 | 720/440/920 | 5600 | 75 | dark/sword/heavy | anti-magic | 12,-80,72,50 |
| anti-magic-dash | dash/sword | 20 | 0..245 | 480/560/560 | 4000 | 40 | dark/sword/heavy | anti-magic | 12,-80,72,50 |

المقذوفات (speed بوحدة px/tick قبل scale):

| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |
|---|---|---|---|---|---|---|

AI/defense: aggression=0.8; block=0.3; blockEffect=spark; blockReduction=0.78; counter=0.4; defenseCooldown=24; defenseCost=22; dodge=0.24; dodgeEffect=dust; dodgeSpeed=8; jumpHeight=28; projectileParry=0.35; taunt=0.0; technique=0.25

VFX IDs: anti-magic, blue, chakra, charge, cursed, dust, energy, fire, hitflash, impact, lightning, projectile, purple, red, shockwave, slash, spark

الإطارات الجديدة الـ16: anti-contact, anti-start, backswing-contact, backswing-start, dash-contact, dash-start, heavy-contact, heavy-recover, heavy-start, heavy-swing, jump-contact, jump-start, new-victory, parry, sword-contact, sword-start
