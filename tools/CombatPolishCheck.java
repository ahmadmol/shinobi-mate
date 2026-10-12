import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.audio.SoundSystem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.lang.management.ManagementFactory;
import javax.imageio.ImageIO;

/** Regression scenarios and a model soak with reproducible resource/performance measurements. */
public class CombatPolishCheck extends CombatCheck {
 static final List<String> names=List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover");
 static CombatProfile.Attack slow(CombatProfile.Attack p) {
  return new CombatProfile.Attack(p.id(),p.kind(),0,p.range(),p.hitbox(),p.damage(),1000,p.startup(),p.active(),p.recovery(),0,
    p.projectileSpeed(),p.knockback(),p.jumpHeight(),0,1,p.animation(),p.chargeAnimation(),p.effect(),p.cloneEffect(),p.recoveryAnimation(),p.chargeEffect(),p.polish());
 }
 static CombatProfile idle(CombatProfile p){return fixture(p,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,1000,10,1,1,0,0,0,0,0,1,"melee","charge","impact",false)));}
 static void regressions()throws Exception {
  var p=profile("NarutoShippuden");Rectangle area=new Rectangle(-1000,-100,1200,600);
  Body a=new Body(-800,500,1,area),b=new Body(900,500,1,new Rectangle(200,-100,1200,600));
  boolean rejected=false;try{new CombatMatch(a,p,b,p,new Random(1));}catch(IllegalArgumentException expected){rejected=true;}
  require(rejected&&b.x==900,"cross monitor start moved fighter");
  b=new Body(-700,500,1,new Rectangle(area));var changed=new CombatMatch(a,p,b,p,new Random(1));b.area.width-=10;changed.tick();
  require(changed.isStopped(),"second monitor/taskbar change retained battle");
  var asymmetric=new WeaponSystem.ProjectileSpec("kunai",100,100,-1,40,20,new Rectangle(5,-10,18,20),10,"projectile",true);
  require(asymmetric.sweep(100,60,100,1).equals(new Rectangle(37,90,58,20)),"left projectile asymmetric swept geometry");
  int combos=0;
  for(String name:names) {
   var profile=profile(name);var combo=profile.attacks().stream().filter(at->!at.polish().strikes().isEmpty()).findFirst().orElseThrow();
   a=new Body(200,500,1,new Rectangle(0,0,900,500));b=new Body(265,500,1,a.area);
   var match=new CombatMatch(a,fixture(profile,List.of(slow(combo))),b,idle(profile),new Random(1));
   int changes=0,hp=100;
   for(int i=0;i<100;i++){match.tick();if(match.b.hp<hp){changes++;hp=match.b.hp;}}
   int total=combo.polish().strikes().stream().mapToInt(CombatProfile.Strike::damage).sum();
   require(changes==combo.polish().strikes().size()&&match.b.hp==100-total,"combo windows duplicate/miss damage "+name+" changes="+changes+" hp="+match.b.hp);
   require(match.a.recentAttacks().size()==1,"cooldown did not include recovery "+name);combos++;
  }
  for(boolean left:List.of(false,true)) {
   var at=slow(p.attacks().stream().filter(t->t.id().equals("kunai")).findFirst().orElseThrow());
   a=new Body(left?500:200,500,1,new Rectangle(0,0,900,500));b=new Body(left?300:400,500,1,a.area);
   var m=new CombatMatch(a,fixture(p,List.of(at)),b,idle(p),new Random(1));
   for(int i=0;i<100;i++)m.tick();require(m.b.hp==100-at.damage(),"left/right real projectile damage "+left);
   m.stop();require(m.projectileCount()==0&&m.visuals().isEmpty(),"stop cleanup");
  }
  var heavy=slow(p.attacks().stream().filter(t->t.id().equals("rasengan")).findFirst().orElseThrow());
  a=new Body(200,500,1,new Rectangle(0,0,900,500));b=new Body(265,500,1,a.area);
  var pause=new CombatMatch(a,fixture(p,List.of(heavy)),b,idle(p),new Random(1));
  for(int t=0;t<100&&pause.b.hp==100;t++)pause.tick();require(pause.b.hp<100,"heavy did not hit");
  int age=pause.a.attackAge();double x=pause.b.x;pause.tick();pause.tick();
  require(pause.a.attackAge()==age&&pause.b.x==x,"80ms hit pause did not freeze model");
  pause.tick();require(pause.a.attackAge()>age,"impact pause never released");
  var sasuke=profile("SasukeShippuden");var counter=slow(sasuke.attacks().stream().filter(t->t.id().equals("counter")).findFirst().orElseThrow());
  var defense=new CombatProfile.Personality(1,1,0,.8,1,0,8,0,50,1,.8,.8,"spark","dust");
  var guarding=new CombatProfile(sasuke.root(),128,128,64,128,sasuke.hurtbox(),sasuke.speed(),defense,sasuke.animations(),List.of(counter),sasuke.effects());
  a=new Body(200,500,1,new Rectangle(0,0,900,500));b=new Body(265,500,1,a.area);
  var parry=new CombatMatch(a,fixture(p,List.of(heavy)),b,guarding,new Random(1){public double nextDouble(){return .5;}});
  var attackAge=CombatMatch.Fighter.class.getDeclaredField("attackAge");attackAge.setAccessible(true);
  for(int i=0;i<27;i++)parry.tick();
  parry.a.attack=heavy;parry.a.state=CombatState.SPECIAL_ATTACK;attackAge.setInt(parry.a,heavy.startup()-3);
  parry.b.attack=null;parry.b.state=CombatState.IDLE;
  for(int i=0;i<4;i++)parry.tick();
  require(parry.b.hp==100&&parry.a.attack==null,"startup read/timed melee parry failed");
  for(int i=0;i<20;i++)parry.tick();require(parry.b.recentAttacks().contains("counter"),"successful parry did not unlock counter");
  System.out.println("PASS cross-monitor admission, second-monitor change, asymmetric left sweep, "+combos+" real combos, two-direction projectiles, heavy hit pause");
 }
 static void assets()throws Exception {
  BufferedImage sheet=new BufferedImage(16*128,5*160,BufferedImage.TYPE_INT_RGB);
  Graphics2D g=sheet.createGraphics();g.setColor(new Color(29,32,45));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());int frames=0,moves=0;
  for(int row=0;row<names.size();row++) {
   var p=profile(names.get(row));var files=Files.list(p.root().resolve("combat/polish"));List<Path> paths;
   try(files){paths=files.sorted().toList();}
   require(paths.size()==16,"additional pose count");Set<Integer> hashes=new HashSet<>();
   for(int i=0;i<paths.size();i++) {
    var im=ImageIO.read(paths.get(i).toFile());require(im.getWidth()==128&&im.getHeight()==128,"canvas inconsistency");
    int pixels=0,hash=1,bottom=0;
    for(int y=0;y<128;y++)for(int x=0;x<128;x++){int c=im.getRGB(x,y);hash=31*hash+c;
     if((c>>>24)>12){pixels++;bottom=Math.max(bottom,y);require(x>0&&x<127,"horizontal crop");}}
    require(pixels>250&&pixels<12000&&bottom==127,"invalid transparency/baseline");require(hashes.add(hash),"duplicate new pose");frames++;
    g.drawImage(im,i*128,row*160+20,null);g.setColor(Color.WHITE);g.drawString(paths.get(i).getFileName().toString().replace(".png",""),i*128+2,row*160+153);
   }
   g.setColor(Color.WHITE);g.drawString(names.get(row),4,row*160+15);
   for(var at:p.attacks()) {
    for(String id:List.of(at.animation(),at.chargeAnimation(),at.recoveryAnimation()))
      require(p.animations().get(id).frames().stream().allMatch(f->f.image().startsWith("combat/polish/")||f.image().startsWith("combat/ninja093/")),"idle/walk attack fallback "+at.id());
    require(at.hitbox().width<128&&at.hitbox().height<128,"full canvas hitbox");moves++;
   }
   require(p.animations().get("ko").poseAt(999).equals(p.animations().get("ko").frames().getLast().image()),"KO repeats");
  }
  g.dispose();ImageIO.write(sheet,"png",Path.of("art/combat-polish/contact-sheet-0.9.2.png").toFile());
  System.out.println("PASS "+frames+" new transparent poses, baseline, unique frames; "+moves+" attacks use dedicated phase art");
 }
 static void soak()throws Exception {
  var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();var threads=ManagementFactory.getThreadMXBean();
  List<CombatProfile> profiles=new ArrayList<>();for(String name:names)profiles.add(profile(name));
  System.gc();Thread.sleep(100);long heapBefore=used(),cpu=os.getProcessCpuTime(),start=System.nanoTime();int beforeThreads=threads.getThreadCount();
  int battles=0,ticks=0,maxVfx=0,maxProjectiles=0;long worst=0;
  for(int seed=0;seed<20;seed++)for(int[] pair:List.of(new int[]{0,1},new int[]{2,3},new int[]{4,0}))for(boolean reverse:List.of(false,true)) {
   var pa=profiles.get(pair[0]);var pb=profiles.get(pair[1]);double sa=seed%2==0?1:.75,sb=seed%3==0?1.5:1;
   Rectangle area=new Rectangle(-1200,-200,1100,650);Body a=new Body(reverse?-450:-1000,450,sa,area),b=new Body(reverse?-1000:-450,450,sb,area);
   CombatMatch match=new CombatMatch(a,pa,b,pb,new Random(101+seed));int t=0;
   while(!match.isStopped()&&t++<5000) {long st=System.nanoTime();match.tick();worst=Math.max(worst,System.nanoTime()-st);ticks++;
    maxVfx=Math.max(maxVfx,match.visuals().size());maxProjectiles=Math.max(maxProjectiles,match.projectileCount());
    require(match.a.hp>=0&&match.b.hp>=0,"HP underflow");
    require(!match.isFinished()||match.a.hp>0||match.b.hp>0,"double KO");
   }
   require(match.isStopped()&&match.isFinished(),"soak battle stuck "+pa.root().getFileName()+" seed "+seed);
   require(match.projectileCount()==0&&match.visuals().isEmpty()&&match.a.attack==null&&match.b.attack==null,"soak retained resources");battles++;
  }
  long elapsed=System.nanoTime()-start,cpuUsed=os.getProcessCpuTime()-cpu;
  System.gc();Thread.sleep(100);long heapAfter=used();
  String json=String.format(Locale.ROOT,"{\"completeBattles\":%d,\"modelTicks\":%d,\"seconds\":%.3f,\"cpuSeconds\":%.3f,\"cpuMicrosPerTick\":%.3f,\"heapBeforeMiB\":%.3f,\"heapAfterMiB\":%.3f,\"threadsBefore\":%d,\"threadsAfter\":%d,\"maxVfx\":%d,\"maxProjectiles\":%d,\"worstTickMillis\":%.3f,\"newTimers\":0,\"postBattleProjectiles\":0,\"postBattleVfx\":0}",battles,ticks,elapsed/1e9,cpuUsed/1e9,cpuUsed/1e3/ticks,heapBefore/1048576.0,heapAfter/1048576.0,beforeThreads,threads.getThreadCount(),maxVfx,maxProjectiles,worst/1e6);
  Files.writeString(Path.of("output/combat-polish/soak.json"),json);System.out.println("PASS model soak: "+json);
 }
 static long used(){return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();}
 public static void main(String[]args){try {regressions();assets();soak();System.out.println("PASS "+checks+" combat polish assertions");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
