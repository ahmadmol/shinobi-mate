import com.group_finity.mascot.*;
import com.group_finity.mascot.animation.*;
import com.group_finity.mascot.config.*;
import com.group_finity.mascot.image.*;
import com.group_finity.mascot.audio.*;
import com.group_finity.mascot.character.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.xml.parsers.*;

/** Actual builder/shared image geometry, audio close races and exact one/all instance removal. */
public class RefinementCheck extends CombatCheck {
 static Animation animation(String pack,int x,int y)throws Exception {
  String xml="<Animation><Pose Image=\"/Stand.png\" ImageAnchor=\""+x+","+y+"\" Velocity=\"0,0\" Duration=\"5\" /></Animation>";
  var doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  return new AnimationBuilder(ResourceBundle.getBundle("schema",Locale.ENGLISH),new Entry(doc.getDocumentElement()),pack).buildAnimation();
 }
 static void anchors()throws Exception {
  for(String pack:List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover","Tom","Jerry","ErenYeager"))for(double scale:List.of(.75,1.,1.5)){
   ImagePairs.clear();Main.getInstance().getProperties().setProperty("Scaling",Double.toString(scale));
   var floor=animation(pack,40,100);var ceiling=animation(pack,20,0);Mascot pet=new Mascot(pack);pet.setAnchor(new Point(500,500));
   for(boolean right:List.of(false,true)){
    pet.setLookRight(right);floor.next(pet,0);var before=pet.getImage();
    ceiling.next(pet,0);var after=pet.getImage();
    require(before.image()==after.image(),"anchor correction decoded another bitmap");require(before.size().equals(after.size()),"pose changed scale");
    require(before.center().y==(int)Math.round(100*scale)&&after.center().y==0,"first cached anchor overrides next pose");
    require(after.center().x==(right?after.size().width-(int)Math.round(20*scale):(int)Math.round(20*scale)),"mirrored anchor");
    floor.next(pet,0);require(pet.getImage().center().equals(before.center()),"reverse transition lost anchor");
   }pet.dispose();
  }ImagePairs.clear();
  System.out.println("PASS eight packs / 3 scales / both facings: floor-ceiling-floor share bitmap and preserve independent pose anchors");
 }
 static void audio()throws Exception {
  try(var pool=Executors.newFixedThreadPool(2)){
   for(int i=0;i<60;i++){
    AudioManager m=new AudioManager(()->{throw new AssertionError("no playback requested");});var gate=new CountDownLatch(1);
    var flushing=pool.submit(()->{gate.await();for(int n=0;n<30;n++)m.flush().get(2,TimeUnit.SECONDS);return true;});
    var closing=pool.submit(()->{gate.await();m.close();return true;});gate.countDown();flushing.get(5,TimeUnit.SECONDS);closing.get(5,TimeUnit.SECONDS);
   }
  }
  AtomicInteger closes=new AtomicInteger();AtomicBoolean backendClosed=new AtomicBoolean();
  AudioManager m=new AudioManager(new AudioManager.Backend(){
   public AudioManager.Voice open(){return new AudioManager.Voice(){public void start(byte[] pcm,boolean loop,float gain,float pan){}public boolean running(){return true;}public void volume(float gain){}public void close(){if(closes.incrementAndGet()==1)throw new IllegalStateException("fixture close");}};}
   public void close(){backendClosed.set(true);}
  });
  var props=Main.getInstance().getProperties();props.setProperty("Mute","false");props.setProperty("Sounds","true");
  for(int i=0;i<2;i++)m.play(new SoundEvent("close-test-"+i,SoundEvent.Category.UI,List.of(Path.of("audio/common/sounds/combat/impact.wav")),.2,0,2,true,20,5000),null,0);
  require(m.flush().get(3,TimeUnit.SECONDS).voices()==2,"cleanup fixture voices absent");m.close();
  require(closes.get()==2&&backendClosed.get()&&m.snapshot().voices()==0&&m.snapshot().cacheFiles()==0,"one failed voice prevented cleanup");
  System.out.println("PASS 1800 concurrent audio flush/close requests and failed voice close isolation");
 }
 static void removal()throws Exception {
  var manager=Main.getInstance().getManager();manager.setExitOnLastRemoved(false);
  for(int i=0;i<3;i++)manager.add(new Mascot("Tom"));manager.add(new Mascot("Jerry"));
  var service=new CharacterService();service.removeOne("Tom");require(service.count("Tom")==2&&service.count("Jerry")==1,"remove-one affects other instances");
  service.remove("Tom");require(service.count("Tom")==0&&service.count("Jerry")==1,"remove-all affects another pack");manager.disposeAll();
  System.out.println("PASS remove one/all handles pending instances without deleting pack files");
 }
 static void facing()throws Exception {
  var p=profile("Tom");var q=profile("Jerry");
  var attack=new com.group_finity.mascot.combat.CombatProfile.Attack("test-swing","melee",0,250,new Rectangle(-100,-140,200,140),20,1000,12,10,12,0,0,0,0,0,1,"punch","charge","impact",false);
  var idle=new com.group_finity.mascot.combat.CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,1000,10,1,1,0,0,0,0,0,1,"punch","charge","impact",false);
  for(boolean right:List.of(false,true)){
   Rectangle area=new Rectangle(0,0,1400,800);var a=new Body(600,800,1,area);var b=new Body(right?660:540,800,1,area);
   var m=new com.group_finity.mascot.combat.CombatMatch(a,fixture(p,List.of(attack)),b,fixture(q,List.of(idle)),new Random(8));
   for(int i=0;i<120&&m.a.attack==null;i++)m.tick();require(m.a.attack!=null,"swing fixture did not start");
   m.a.right=right;m.b.x=right?m.a.x-50:m.a.x+50;
   for(int i=0;i<25&&m.a.attack!=null;i++){m.b.x=right?m.a.x-50:m.a.x+50;m.tick();require(m.b.hp==100,"locked melee hit an opponent behind attacker");}
   require(m.visuals().stream().noneMatch(v->v.effect.equals("impact")),"back-facing miss emitted impact");m.stop();
  }
  System.out.println("PASS wide melee hitbox cannot damage a target behind locked facing in either direction");
 }
 public static void main(String[]args){try{
  java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);
  var nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,new Factory());
  anchors();audio();removal();facing();AudioManager.global().close();System.out.println("PASS refinement assertions="+checks);System.exit(0);
 }catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
}
