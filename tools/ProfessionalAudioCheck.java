import com.group_finity.mascot.*;
import com.group_finity.mascot.audio.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.settings.*;
import java.awt.Rectangle;
import java.lang.management.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.swing.*;

/** Real WAV decoding plus deterministic device arbitration and real-device accelerated battle soak. */
public class ProfessionalAudioCheck extends CombatPolishCheck {
 static final Path REPORT=Path.of("output/audio-phase1");
 static final List<String> phase1=List.of("NarutoShippuden","SasukeShippuden");
 static void drain(AudioManager m)throws Exception{m.flush().get(10,TimeUnit.SECONDS);}
 static class FakeVoice implements AudioManager.Voice {
  boolean running,closed;byte[] pcm;float gain,pan;boolean loop;
  public void start(byte[] pcm,boolean loop,float gain,float pan){require(!SwingUtilities.isEventDispatchThread(),"native operation on EDT");this.pcm=pcm;this.loop=loop;this.gain=gain;this.pan=pan;running=true;}
  public boolean running(){return running;}
  public void volume(float gain){this.gain=gain;}
  public void close(){running=false;closed=true;}
 }
 static SoundEvent event(String id,SoundEvent.Category category,int priority){return new SoundEvent(id,category,List.of(Path.of("audio/common/sounds/combat/impact.wav")),.5,0,12,true,priority,5000);}
 static void engine()throws Exception {
  var props=Main.getInstance().getProperties();props.putAll(SettingsStore.defaults());props.setProperty("Sounds","true");props.setProperty("CombatSound","true");
  List<FakeVoice> output=Collections.synchronizedList(new ArrayList<>());
  AudioManager m=new AudioManager(()->{var v=new FakeVoice();output.add(v);return v;});
  List<AudioManager.Playback> history=Collections.synchronizedList(new ArrayList<>());m.observe(history::add);
  try {
   // Actual call from Swing. File decoding, line operations and callbacks must run on the worker.
   SwingUtilities.invokeAndWait(()->m.playCharacter("NarutoShippuden","movement.footstep",new AudioManager.Scope(),-.3));drain(m);
   require(m.snapshot().played()==1,"EDT enqueue did not play");require(history.getFirst().thread().equals("ShinobiMate audio"),"wrong worker");
   for(int i=0;i<500;i++)m.playCharacter("NarutoShippuden","movement.footstep",null,0);drain(m);
   require(m.snapshot().played()==1,"footstep cooldown spam");require(m.snapshot().peakPending()<=AudioManager.MAX_PENDING,"pending overflow");
   m.stopAll();drain(m);require(m.snapshot().voices()==0,"stopAll leaked");
   for(int i=0;i<3;i++)m.play(event("move"+i,SoundEvent.Category.MOVEMENT,10),null,0);
   for(int i=0;i<8;i++)m.play(event("vfx"+i,SoundEvent.Category.VFX,i==0?95:40),null,0);
   m.play(event("ui-full",SoundEvent.Category.UI,20),null,0);drain(m);
   require(m.snapshot().voices()==12,"global/category admission failure");
   FakeVoice protectedVoice=output.get(4); // footstep at 0, then 3 movement, then first VFX.
   m.play(event("signature-impact",SoundEvent.Category.VFX,95),null,.6);drain(m);
   require(m.snapshot().voices()==12&&m.snapshot().evicted()==1,"signature did not preempt lower priority");
   m.play(event("extra-step",SoundEvent.Category.MOVEMENT,10),null,0);drain(m);
   require(!protectedVoice.closed,"footstep evicted signature");
   m.stopAll();drain(m);
   Path root=Path.of("img/NarutoShippuden");var footsteps=AudioPack.load(root).get("movement.footstep");
   SoundEvent variation=new SoundEvent("variation-check",SoundEvent.Category.MOVEMENT,footsteps.variations(),.4,0,1,false,10,2000);
   history.clear();
   for(int i=0;i<32;i++){var scope=new AudioManager.Scope();m.play(variation,scope,0);drain(m);m.stop(scope);drain(m);}
   require(history.size()==32,"variation playback missing");
   for(int i=1;i<history.size();i++)require(!history.get(i).file().equals(history.get(i-1).file()),"immediate variation repeated");
   require(history.stream().map(AudioManager.Playback::file).distinct().count()==4,"not all footstep variations used");
   var scope=new AudioManager.Scope();var handle=m.playCharacter("SasukeShippuden","signature.chidori.loop",scope,0);drain(m);
   require(m.snapshot().loops()==1,"Chidori loop missing");m.stop(handle);drain(m);require(m.snapshot().loops()==0,"handle cancellation retained loop");
   scope=new AudioManager.Scope();m.playCharacter("SasukeShippuden","signature.chidori.loop",scope,0);m.stop(scope);drain(m);require(m.snapshot().loops()==0,"queued loop survived owner cancellation");
   // Reload invalidates cache and closes every voice using that root.
   scope=new AudioManager.Scope();m.playCharacter("NarutoShippuden","signature.rasengan.impact",scope,0);drain(m);m.reload("NarutoShippuden");drain(m);
   require(m.snapshot().voices()==0,"reload retained pack voice");
   scope=new AudioManager.Scope();m.play(event("volume-check",SoundEvent.Category.COMBAT,50),scope,0);drain(m);
   props.setProperty("MasterVolume","50");props.setProperty("CombatVolume","20");m.refresh();drain(m);
   require(Math.abs(output.getLast().gain-.05)<.001,"live multiplicative volume");
   props.setProperty("Mute","true");m.refresh();drain(m);require(m.snapshot().voices()==0,"mute retained voice");
   for(var c:SoundEvent.Category.values())require(AudioManager.gain(c)==0,"category mute "+c);
   props.setProperty("Mute","false");props.setProperty("MovementVolume","30");props.setProperty("UIVolume","10");
   require(Math.abs(AudioManager.gain(SoundEvent.Category.MOVEMENT)-.15)<.001,"movement gain");require(Math.abs(AudioManager.gain(SoundEvent.Category.UI)-.05)<.001,"UI gain");
   long failures=m.snapshot().failures();
   var missing=new SoundEvent("missing-check",SoundEvent.Category.COMBAT,List.of(root.resolve("sounds/combat/absent.wav")),.5,0,1,false,50,1000);
   for(int i=0;i<8;i++){m.play(missing,null,0);drain(m);}require(m.snapshot().failures()==failures+1,"missing files retried every hit");
   m.play(root,"missing.event",null,0);drain(m);require(m.snapshot().voices()==0,"missing event created voice");
   // Decode enough distinct real WAVs to force count-bounded LRU eviction.
   for(Path wav:Files.readAllLines(REPORT.resolve("wav-files.txt")).stream().map(Path::of).toList()) {
    var s=new AudioManager.Scope();m.play(new SoundEvent("cache-check",SoundEvent.Category.COMBAT,List.of(wav),.2,0,1,false,30,1000),s,0);drain(m);m.stop(s);drain(m);
   }
   require(m.snapshot().cacheFiles()<=128&&m.snapshot().cacheBytes()<=8*1024*1024,"cache unbounded");
   System.out.println("PASS device arbitration: priority, category/global limits, 500-event flood, cancellation, reload, mute, volumes, missing-file backoff, 32 nonrepeating steps, bounded LRU, EDT isolation "+m.snapshot());
  }finally{m.close();}
  require(m.snapshot().voices()==0&&m.snapshot().cacheFiles()==0,"close retained resources");
  require(output.stream().allMatch(v->v.closed),"output voice leaked");
 }
 static void persistence()throws Exception {
  var p=SettingsStore.defaults();for(String key:List.of("MasterVolume","CharacterVolume","MovementVolume","CombatVolume","UIVolume"))p.setProperty(key,"37");p.setProperty("Mute","true");
  Path file=REPORT.resolve("settings-roundtrip.properties");SettingsStore.save(p,file);Properties loaded=new Properties();try(var in=Files.newInputStream(file)){loaded.load(in);}
  for(String key:List.of("MasterVolume","CharacterVolume","MovementVolume","CombatVolume","UIVolume","Mute"))require(p.getProperty(key).equals(loaded.getProperty(key)),"persistence "+key);
  p.setProperty("UIVolume","NaN");p.setProperty("MovementVolume","-900");SettingsStore.sanitize(p);require(p.getProperty("UIVolume").equals("30")&&p.getProperty("MovementVolume").equals("0"),"volume validation");
  SwingUtilities.invokeAndWait(()->{var dialog=new com.group_finity.mascot.ui.SettingsDialog(null);
   List<JSlider> sliders=new ArrayList<>();findSliders(dialog,sliders);require(sliders.size()==5,"expected five volume sliders");
   for(var slider:sliders)require(slider.getMinimum()==0&&slider.getMaximum()==100,"percent slider range");dialog.dispose();});
  System.out.println("PASS five accessible percent sliders, mute and volume settings persistence/validation");
 }
 static void findSliders(java.awt.Container c,List<JSlider> result){for(var child:c.getComponents()){if(child instanceof JSlider s)result.add(s);if(child instanceof java.awt.Container inner)findSliders(inner,result);}}
 static class RecordingMascot extends Mascot {
  final List<String> emitted=new ArrayList<>();
  RecordingMascot(String name){super(name);}
  @Override public void playAudio(String event){emitted.add(event);}
 }
 static void movement()throws Exception {
  factory=new Factory();var singleton=NativeFactory.class.getDeclaredField("instance");singleton.setAccessible(true);singleton.set(null,factory);
  time=Mascot.class.getDeclaredField("time");time.setAccessible(true);cursor(-1000,-1000,0,0);
  for(String name:phase1){var c=config(name,"behaviors.xml");var pet=new RecordingMascot(name);pet.setAnchor(new java.awt.Point(300,500));pet.getEnvironment().refreshWorkArea();
   int n=run(c,pet,"Walk",Map.of("TargetX","400"),100,false);
   require(pet.emitted.stream().allMatch("movement.footstep"::equals),"walk emitted unrelated events");
   require(pet.emitted.size()<n/3&&pet.emitted.size()>=3,"footsteps emitted per tick / absent "+name+" "+pet.emitted);
   pet.emitted.clear();pet.setAnchor(new java.awt.Point(300,500));run(c,pet,"NinjaHop",Map.of(),100,false);
   require(pet.emitted.equals(List.of("movement.jump","movement.air","movement.landing")),"hop markers out of order/repeated "+pet.emitted);
   require(pet.getAnchor().y==500,"landing marker not at contact");pet.emitted.clear();pet.setDragging(true);pet.setDragging(true);pet.setDragging(false);pet.setDragging(false);
   require(pet.emitted.equals(List.of("movement.drag.start","movement.drag.release")),"drag transitions spammed");pet.dispose();
  }
  System.out.println("PASS actual Naruto/Sasuke walk pose-entry markers, hop takeoff/air/contact sequence and drag transition deduplication");
 }
 static class RecordingBody extends Body {
  List<String> sounds=new ArrayList<>();List<String> characters=new ArrayList<>();int stopped,chargeStops;
  RecordingBody(double x,Rectangle area){super(x,area.getMaxY(),1,area);}
  public void sound(String e,String c,double p){sounds.add(e);characters.add(c);}
  public void stopChargeSounds(){chargeStops++;}
  public void stopSounds(){stopped++;}
 }
 static void synchronization()throws Exception {
  for(String name:phase1){var p=profile(name);
   for(var at:p.attacks())for(boolean hit:List.of(false,true)) {
    var area=new Rectangle(0,0,1200,700);var a=new RecordingBody(200,area);var b=new RecordingBody(hit?270:850,area);
    var selected=new CombatProfile.Attack(at.id().equals("counter")?"audio-test":at.id(),at.kind(),0,1000,at.hitbox(),at.damage(),1000,at.startup(),at.active(),at.recovery(),0,
      at.projectileSpeed(),at.knockback(),at.jumpHeight(),0,1,at.animation(),at.chargeAnimation(),at.effect(),at.cloneEffect(),at.recoveryAnimation(),at.chargeEffect(),at.polish());
    // Targets are beyond projectile range on a miss, with all movement frozen.
    if(!hit&&at.projectile())selected=new CombatProfile.Attack(selected.id(),selected.kind(),0,300,selected.hitbox(),selected.damage(),1000,selected.startup(),selected.active(),selected.recovery(),0,selected.projectileSpeed(),selected.knockback(),selected.jumpHeight(),0,1,selected.animation(),selected.chargeAnimation(),selected.effect(),selected.cloneEffect(),selected.recoveryAnimation(),selected.chargeEffect(),selected.polish());
    if(!hit&&at.projectile())b.x=270;
    var match=new CombatMatch(a,fixture(p,List.of(selected)),b,idle(p),new Random(1));boolean attack=false,impact=false,start=false;
    for(int t=0;t<110;t++) {
     int previousHP=match.b.hp;int size=a.sounds.size();match.tick();
     if(!hit&&at.projectile()&&match.a.attack!=null&&match.a.attackAge()==1){match.b.x=850;b.x=850;}
     for(int i=size;i<a.sounds.size();i++){
      String event=a.sounds.get(i);
      if(event.equals(at.polish().startupSound())&&!event.isBlank()){start=true;require(match.a.attackAge()<=1,"startup outside first charge frame "+at.id());}
      if(event.equals(at.polish().attackSound())&&!event.isBlank()){attack=true;require(match.a.attackAge()>at.startup(),"attack before active frame "+at.id());}
      if(event.equals(at.polish().impactSound())&&!event.isBlank()){impact=true;require(match.b.hp<previousHP,"impact without hit confirmation "+at.id());require(a.characters.get(i).equals(name),"impact used victim's pack "+at.id());}
     }
    }
    require(start,"startup absent "+at.id());if(at.polish().strikes().isEmpty()&&!at.polish().attackSound().isBlank())require(attack,"release absent "+at.id());
    if(!hit)require(!impact,"miss emitted impact "+at.id());
    match.stop();require(a.stopped==1&&b.stopped==1,"scope lifecycle missing");
   }
  }
  // Multi-strike sword: no duplicate first swing; each contact has one swing, final kick differs.
  var p=profile("SasukeShippuden");var at=slow(p.attacks().stream().filter(a->a.id().equals("sword-combo")).findFirst().orElseThrow());
  var area=new Rectangle(0,0,1200,700);var a=new RecordingBody(200,area);var b=new RecordingBody(270,area);
  var match=new CombatMatch(a,fixture(p,List.of(at)),b,idle(p),new Random(1));for(int i=0;i<85;i++)match.tick();
  require(Collections.frequency(a.sounds,"weapon.sword.swing")==2&&Collections.frequency(a.sounds,"combat.kick")==1,"duplicate/wrong sword combo swing");
  match.stop();
  System.out.println("PASS every Naruto/Sasuke attack startup/active/hit timing, miss silence, source-owned impact, distinct sword combo contacts and scope cleanup");
 }
 static class SoundBody extends Body {
  final AudioManager.Scope scope=new AudioManager.Scope();final List<AudioManager.Handle> charge=new ArrayList<>();
  SoundBody(double x,Rectangle area){super(x,area.getMaxY(),1,area);}
  public void sound(String event,String name,double pan){AudioManager.global().playCharacter(name,event,scope,pan);}
  public void chargeSound(String event,String name,double pan){charge.add(AudioManager.global().playCharacter(name,event,scope,pan));}
  public void stopChargeSounds(){for(var h:charge)AudioManager.global().stop(h);charge.clear();}
  public void stopSounds(){stopChargeSounds();AudioManager.global().stop(scope);}
 }
 static long heap(){return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();}
 static long audioCpu(){var bean=ManagementFactory.getThreadMXBean();long sum=0;for(var t:bean.dumpAllThreads(false,false))if(t.getThreadName().startsWith("ShinobiMate audio"))sum+=Math.max(0,bean.getThreadCpuTime(t.getThreadId()));return sum;}
 static void nativeTest()throws Exception {
  var props=Main.getInstance().getProperties();props.putAll(SettingsStore.defaults());props.setProperty("Sounds","true");props.setProperty("CombatSound","true");
  var audio=AudioManager.global();List<AudioManager.Playback> trace=Collections.synchronizedList(new ArrayList<>());audio.observe(trace::add);
  for(String name:phase1)audio.preload(name);drain(audio);
  var probe=new AudioManager.Scope();audio.playCharacter("SasukeShippuden","signature.chidori.loop",probe,0);drain(audio);
  require(audio.snapshot().loops()==1,"No native audio device/loop opened: "+audio.snapshot());audio.stop(probe);drain(audio);require(audio.snapshot().voices()==0,"native loop did not close");
  var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();List<String> metrics=new ArrayList<>();
  for(int count:new int[]{0,1,2,5}) {
   var scopes=new ArrayList<AudioManager.Scope>();for(int i=0;i<count;i++)scopes.add(new AudioManager.Scope());
   long cpu=os.getProcessCpuTime(),audioStart=audioCpu(),start=System.nanoTime();int peak=0;
   for(int tick=0;tick<100;tick++) {
    if(tick%6==0)for(int i=0;i<count;i++)audio.playCharacter(names.get(i),"movement.footstep",scopes.get(i),(i-2)*.12);
    Thread.sleep(40);peak=Math.max(peak,audio.snapshot().voices());
   }
   long end=System.nanoTime();for(var scope:scopes)audio.stop(scope);drain(audio);
   String metric=String.format(Locale.ROOT,"{\"characters\":%d,\"seconds\":%.3f,\"processCPUOneCorePercent\":%.3f,\"audioCPUOneCorePercent\":%.3f,\"heapMiB\":%.3f,\"cacheMiB\":%.3f,\"threads\":%d,\"peakVoices\":%d,\"remainingVoices\":%d,\"nativeLines\":%d,\"clips\":0}",count,(end-start)/1e9,100.0*(os.getProcessCpuTime()-cpu)/(end-start),100.0*(audioCpu()-audioStart)/(end-start),heap()/1048576.,audio.snapshot().cacheBytes()/1048576.,ManagementFactory.getThreadMXBean().getThreadCount(),peak,audio.snapshot().voices(),audio.snapshot().nativeLines());metrics.add(metric);System.out.println("PERF "+metric);
  }
  var pa=profile(phase1.get(0));var pb=profile(phase1.get(1));int rounds=Integer.getInteger("audio.rounds",30);int maxThreads=0;
  List<String> soak=new ArrayList<>();
  for(int round=0;round<rounds;round++) {
   var area=new Rectangle(0,0,1200,700);var a=new SoundBody(300,area);var b=new SoundBody(590,area);
   var match=new CombatMatch(a,pa,b,pb,new Random(round));int tick=0,peak=0;long start=System.nanoTime();
   while(!match.isStopped()&&tick++<4000){match.tick();Thread.sleep(4);peak=Math.max(peak,audio.snapshot().voices());maxThreads=Math.max(maxThreads,ManagementFactory.getThreadMXBean().getThreadCount());}
   require(match.isFinished()&&match.isStopped(),"native soak battle did not complete "+round);drain(audio);
   require(audio.snapshot().voices()==0&&audio.snapshot().loops()==0,"stuck/leaked audio after battle "+round);
   Thread.sleep(450);drain(audio);require(audio.snapshot().nativeLines()==0,"idle native line leaked after battle "+round);
   require(audio.snapshot().peakVoices()<=12,"native pool exceeded limit");
   String metric=String.format(Locale.ROOT,"{\"round\":%d,\"ticks\":%d,\"seconds\":%.3f,\"voicesAfter\":%d,\"loopsAfter\":%d,\"peakVoices\":%d,\"threads\":%d,\"heapMiB\":%.3f}",round,tick,(System.nanoTime()-start)/1e9,audio.snapshot().voices(),audio.snapshot().loops(),peak,ManagementFactory.getThreadMXBean().getThreadCount(),heap()/1048576.);
   soak.add(metric);if(round%5==0)System.out.println("SOAK "+metric);
  }
  // Explicit charge expiry with no stop request is also bounded.
  var scope=new AudioManager.Scope();audio.playCharacter("SasukeShippuden","signature.chidori.loop",scope,0);drain(audio);Thread.sleep(1950);drain(audio);require(audio.snapshot().loops()==0,"loop safety expiry failed");
  audio.stop(scope);drain(audio);Thread.sleep(1000);
  long workers=Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(false,false)).filter(t->t.getThreadName().equals("ShinobiMate audio")).count();require(workers==1,"audio executor thread leak");
  var observed=new TreeSet<String>();synchronized(trace){for(var e:trace)observed.add(e.id());}
  require(observed.containsAll(List.of("signature.rasengan.charge","signature.rasengan.dash","signature.rasengan.impact","signature.chidori.charge","signature.chidori.dash","signature.chidori.impact","weapon.sword.draw","weapon.sword.swing","weapon.shuriken.throw","weapon.kunai.throw","ko")),"native cues not exercised "+observed);
  Files.writeString(REPORT.resolve("performance.json"),"[\n"+String.join(",\n",metrics)+"\n]");
  Files.writeString(REPORT.resolve("native-soak.json"),"[\n"+String.join(",\n",soak)+"\n]");
  Files.writeString(REPORT.resolve("native-observed-events.txt"),String.join("\n",observed));
  System.out.println("PASS native device, 0/1/2/5-character metrics, "+rounds+" accelerated complete Naruto/Sasuke battles, zero voices/loops after every end, one worker, peak threads="+maxThreads+"; "+audio.snapshot());
  audio.close();require(audio.snapshot().voices()==0&&audio.snapshot().cacheBytes()==0,"global close leak");
 }
 public static void main(String[] args){try {
  Files.createDirectories(REPORT);Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
  if(args.length>0&&args[0].equals("native"))nativeTest();
  else {
   List<String> files=new ArrayList<>();for(Path root:List.of(Path.of("audio/common"),Path.of("img/NarutoShippuden"),Path.of("img/SasukeShippuden"))) {
    var events=AudioPack.load(root);require(!events.isEmpty(),"no events "+root);
    for(var event:events.values())for(Path file:event.variations())try(AudioInputStream in=AudioSystem.getAudioInputStream(file.toFile())){require(in.getFrameLength()>0&&in.getFormat().getSampleRate()==22050,"bad WAV "+file);files.add(file.toString());}
   }
   Files.write(REPORT.resolve("wav-files.txt"),files);engine();persistence();movement();synchronization();regressions();
   AudioManager.global().close();
  }
  System.out.println("PASS professional audio: "+checks+" assertions");System.exit(0);
 }catch(Throwable ex){ex.printStackTrace();AudioManager.global().close();System.exit(1);}}
}
