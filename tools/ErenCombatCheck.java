import com.group_finity.mascot.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.config.Configuration;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Seeded natural fights plus exact transformation lifecycle and Mascot/renderer integration. */
public class ErenCombatCheck extends CombatCheck {
 static final List<String> OPPONENTS=List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover");
 static void fights() throws Exception {
  var eren=profile("ErenYeager");
  for(String name:OPPONENTS) {
   int changed=0,wins=0;
   for(int seed=0;seed<20;seed++)for(double scale:List.of(.75,1.0,1.5)) {
    Rectangle area=new Rectangle(-1000,-150,1200,700);
    Body eb=new Body(-800,550,scale,area),ob=new Body(-100,550,scale,area);
    // Exercise both update orders.
    boolean first=seed%2==0;
    var match=first?new CombatMatch(eb,eren,ob,profile(name),new Random(seed)):new CombatMatch(ob,profile(name),eb,eren,new Random(seed));
    var f=first?match.a:match.b;var enemy=first?match.b:match.a;
    require(f.form==CombatMatch.Form.HUMAN&&f.profile==eren,"not human at start");
    boolean seen=false,titan=false;int hpDuring=-1;
    for(int t=0;t<4000&&!match.isStopped();t++) {
     match.tick();combatTicks++;
     if(t<25)require(f.form==CombatMatch.Form.HUMAN,"immediate transformation");
     require(f.transformations<=1,"transformed twice");
     if(f.state==CombatState.TRANSFORMING) {
      if(!seen){require(f.hp<=50&&f.hp>0,"wrong threshold");hpDuring=f.hp;seen=true;}
      require(f.attack==null&&f.hurtbox().isEmpty(),"active transformation attack/hurtbox");
      require(f.hp==hpDuring,"damage spam during transformation");
     }
     if(f.form==CombatMatch.Form.TITAN) {titan=true;require(f.profile==eren.transformation().target(),"wrong Titan profile");}
     if(!match.isStopped()) {
      for(var fighter:List.of(f,enemy)) {
       var p=fighter.profile;double s=fighter.body.scale();
       require(area.contains(new Rectangle((int)Math.round(fighter.x-p.anchorX()*s),(int)Math.round(fighter.renderY()-p.anchorY()*s),(int)Math.round(p.width()*s),(int)Math.round(p.height()*s))),"sprite outside desktop");
      }
     }
     require(match.visuals().size()<=48&&match.projectileCount()<=8,"unbounded effects");
     for(var v:match.visuals())require(v.profile.effects().containsKey(v.effect),"VFX belongs to wrong profile: "+v.effect);
    }
    require(match.isFinished()&&match.isStopped(),"stuck match: "+name+" seed "+seed);
    if(seen){changed++;require(titan,"never reached Titan");}
    if(match.winner()==eb)wins++;
    require(f.form==CombatMatch.Form.HUMAN&&f.profile==eren&&f.hp==100,"not reset to human");
    require(eren.animations().get("idle").frames().stream().anyMatch(frame->frame.image().equals(eb.image)),"Titan sprite survives stop");
    require(match.visuals().isEmpty()&&match.projectileCount()==0,"effects survived end");
    var rematch=new CombatMatch(eb,eren,ob,profile(name),new Random(seed));
    require(rematch.a.form==CombatMatch.Form.HUMAN&&rematch.a.transformations==0,"rematch state leak");rematch.stop();
   }
   require(changed>0,"no natural transformations: "+name);
   System.out.println("PASS "+name+" vs Eren: 60 fights, 3 scales, both update orders; transforms="+changed+", Eren wins="+wins);
  }
 }
 static void lifecycle() throws Exception {
  var p=profile("ErenYeager");Rectangle area=new Rectangle(0,0,1200,700);
  Body eb=new Body(300,700,1,area),ob=new Body(900,700,1,area);
  var match=new CombatMatch(eb,p,ob,profile("NarutoShippuden"),new Random(22));
  for(int i=0;i<26;i++)match.tick();match.a.hp=51;match.tick();require(match.a.form==CombatMatch.Form.HUMAN,"threshold >50");
  match.a.hp=50;match.tick();require(match.a.state==CombatState.TRANSFORMING,"threshold <=50");
  for(int i=0;i<13;i++)match.tick();require(eb.image.endsWith("hidden.png"),"human not hidden in burst");
  int hp=match.a.hp;String image=eb.image;eb.suspended=true;
  for(int i=0;i<80;i++)match.tick();require(match.a.hp==hp&&eb.image.equals(image)&&match.a.profile==p,"pause advanced transformation");eb.suspended=false;
  for(int i=0;i<40;i++)match.tick();require(match.a.form==CombatMatch.Form.TITAN,"pause resume stuck");
  require(match.a.hp<=50,"HP refilled at transformation");
  match.stop();require(match.a.form==CombatMatch.Form.HUMAN,"explicit stop not reset");
  var cancel=new CombatMatch(eb,p,ob,profile("NarutoShippuden"),new Random(22));for(int i=0;i<26;i++)cancel.tick();cancel.a.hp=50;cancel.tick();eb.available=false;cancel.tick();require(cancel.isStopped()&&cancel.a.form==CombatMatch.Form.HUMAN,"removal during transformation");eb.available=true;
  Path temp=Files.createTempDirectory("eren-threshold");Files.createDirectories(temp.resolve("conf"));
  // Test configurable threshold with the same immutable form data, independent of XML default.
  var configured=new CombatProfile(p.root(),p.width(),p.height(),p.anchorX(),p.anchorY(),p.hurtbox(),p.speed(),p.personality(),p.animations(),p.attacks(),p.effects(),new CombatProfile.Transformation(p.transformation().target(),.75,true,12,26,38,48));
  var custom=new CombatMatch(eb,configured,ob,profile("NarutoShippuden"),new Random(1));for(int i=0;i<26;i++)custom.tick();custom.a.hp=75;custom.tick();require(custom.a.state==CombatState.TRANSFORMING,"configured threshold ignored");custom.stop();Files.delete(temp.resolve("conf"));Files.delete(temp);
  var tooSmall=new Body(100,200,1,new Rectangle(0,0,450,220));boolean rejected=false;try{new CombatMatch(tooSmall,p,new Body(300,200,1,tooSmall.area),profile("NarutoShippuden"),new Random(1));}catch(IllegalArgumentException e){rejected=true;}require(rejected,"future form exceeds monitor");
  System.out.println("PASS exact threshold, hidden human, invulnerability, pause/resume, cancellation, HP retention, configurable threshold, future form bounds");
 }
 @SuppressWarnings("unchecked") static void integrationEren() throws Exception {
  factory=new Factory();factory.env.resize(new Rectangle(0,0,1200,700));Field nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,factory);
  Main app=Main.getInstance();Field configsField=Main.class.getDeclaredField("configurations");configsField.setAccessible(true);var configs=(Map<String,Configuration>)configsField.get(app);
  configs.put("ErenYeager",config("ErenYeager","behaviors.xml"));configs.put("NarutoShippuden",config("NarutoShippuden","behaviors.xml"));
  Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  Mascot e=new Mascot("ErenYeager"),n=new Mascot("NarutoShippuden");
  for(var m:List.of(e,n)){m.setManager(manager);manager.getMascots().add(m);m.setAnchor(new Point(m==e?300:800,700));m.getEnvironment().refreshWorkArea();m.setBehavior(configs.get(m.getImageSet()).buildBehavior("StandUp"));}
  require(manager.getCombat().start(e,n),"actual Mascot start rejected pack");
  Field pairs=CombatEngine.class.getDeclaredField("pairs");pairs.setAccessible(true);Object pair=((List<?>)pairs.get(manager.getCombat())).getFirst();Method get=pair.getClass().getDeclaredMethod("match");get.setAccessible(true);CombatMatch match=(CombatMatch)get.invoke(pair);
  for(int i=0;i<26;i++)manager.getCombat().tick();match.a.hp=50;
  var sameBody=match.a.body;
  for(int i=0;i<70&&match.a.form!=CombatMatch.Form.TITAN;i++)manager.getCombat().tick();
  require(match.a.form==CombatMatch.Form.TITAN,"Mascot integration transform failed: "+match.a.state+" stopped="+match.isStopped()+" suspended="+match.a.body.suspended());require(match.a.body==sameBody,"instance replaced");
  require(e.getImage().size().height==256,"renderer has wrong Titan size/anchor");
  manager.getCombat().stop(e);require(e.getImage().size().height==128,"renderer not reset to human");
  require(manager.getCombat().start(e,n),"actual rematch failed");manager.getCombat().stop(n);manager.disposeAll();SwingUtilities.invokeAndWait(()->{});
  System.out.println("PASS actual Mascot/CombatEngine start, profile preload, 256px Titan renderer, 128px reset, rematch");
 }
 static void moves() throws Exception {
  var p=profile("ErenYeager");
  require(com.group_finity.mascot.pack.PackManifest.read(p.root(),"ErenYeager").combat(),"pack absent from character catalog");
  for(var form:p.forms())for(var move:form.attacks()) {
   var idle=fixture(form,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,100,10,1,1,0,0,0,0,0,1,"punch","charge","impact",false)));
   Body a=new Body(350,700,1,new Rectangle(0,0,1400,700)),b=new Body(430,700,1,a.area);
   var match=new CombatMatch(a,fixture(form,List.of(move)),b,idle,new Random(2));Set<String> images=new HashSet<>();
   for(int i=0;i<300;i++){match.tick();if(a.image!=null)images.add(a.image);}
   for(String phase:List.of(move.chargeAnimation(),move.animation(),move.recoveryAnimation()))require(form.animations().get(phase).frames().stream().anyMatch(f->images.contains(f.image())),"unseen move phase: "+move.id()+"/"+phase);
   match.stop();
  }
  for(var form:p.forms())for(var anim:form.animations().values())for(var frame:anim.frames()){
   var im=ImageIO.read(form.root().resolve(frame.image()).toFile());require(im!=null&&im.getWidth()==form.width()&&im.getHeight()==form.height(),"invalid sprite dimensions");
  }
  System.out.println("PASS all 9 moves: startup, active, recovery; all human/Titan sprite dimensions");
 }
 public static void main(String[] args) {
  try{Logger.getLogger("").setLevel(Level.SEVERE);moves();lifecycle();fights();integrationEren();System.out.println("PASS Eren: "+checks+" assertions, "+combatTicks+" natural fight ticks");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
