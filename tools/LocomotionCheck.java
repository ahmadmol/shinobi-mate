import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.audio.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

/** Real actions + avoidance against deterministic desktop/window adapters. */
public class LocomotionCheck extends PolishEngineCheck {
 static Map<String,Configuration> configs;
 static final List<String> NAMES=List.of("ErenYeager","Tom","Jerry","NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover");
 static class Straight implements Behavior {
  final Configuration config;final int direction;Action action;Mascot pet;
  Straight(Configuration c,int dir){config=c;direction=dir;}
  public void init(Mascot p){pet=p;try{pet.setLookRight(direction>0);action=config.buildAction(direction==0?"Stand":"Walk",Map.of("Duration","10000","TargetX",Integer.toString(direction>0?factory.env.work.getRight()-100:factory.env.work.getLeft()+100)));action.init(p);}catch(Exception ex){throw new RuntimeException(ex);}}
  public void next(){try{action.next();}catch(Exception ex){throw new RuntimeException(ex);}}
  public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}
 }
 static Mascot pet(Manager manager,String name,int x,int dir)throws Exception {
  Mascot m=mascot(name,x,900);m.setManager(manager);manager.getMascots().add(m);m.setBehavior(new Straight(configs.get(name),dir));m.getBehavior().next();return m;
 }
 static void step(Manager manager)throws Exception {
  for(Mascot m:List.copyOf(manager.getMascots()))if(!m.isPaused()&&!m.isDragging()){
   m.getBehavior().next();time.setInt(m,m.getTime()+1);
  }
  DesktopAvoidance.tick(manager);
 }
 static void stoppedLeader(String name,int dir)throws Exception {
  Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  Mascot leader=pet(manager,"ErenYeager",800,0),walker=pet(manager,name,dir>0?450:1150,dir);
  Point fixed=new Point(leader.getAnchor());boolean pass=false,air=false;int overlap=0;
  for(int t=0;t<240;t++) {
   step(manager);pass|=walker.getBehavior() instanceof PedestrianPass;air|=walker.getAnchor().y<800;
   require(leader.getAnchor().equals(fixed),"stationary leader displaced");
   if(alphaBounds(walker).intersects(alphaBounds(leader))){overlap++;System.out.println("OVERLAP tick="+t+" pos="+walker.getAnchor()+" walker="+alphaBounds(walker)+" leader="+alphaBounds(leader));}
  }
  require(pass&&air,"no actual overtaking jump: "+name+" dir="+dir);
  require((walker.getAnchor().x-leader.getAnchor().x)*dir>80,"walker did not get past leader: "+name+walker.getAnchor());
  require(overlap==0,"art rectangles overlapped in overtake: "+name+" frames="+overlap);
  manager.disposeAll();System.out.println("PASS stationary leader / "+name+" facing="+dir+" overtake, resume, no sprite overlap");
 }
 static void headOn()throws Exception {
  Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  Mascot a=pet(manager,"Tom",450,1),b=pet(manager,"Jerry",1150,-1);boolean changed=false;
  for(int t=0;t<190;t++){step(manager);changed|=!a.isLookRight()||b.isLookRight();require(!alphaBounds(a).intersects(alphaBounds(b)),"head-on visual overlap at "+t+" "+a.getAnchor()+b.getAnchor());}
  require(changed&&Math.abs(a.getAnchor().x-b.getAnchor().x)>80,"head-on walkers did not reroute");manager.disposeAll();System.out.println("PASS head-on Tom/Jerry reroute without overlap");
 }
 static void boundary()throws Exception {
  Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  Mascot leader=pet(manager,"Tom",1470,0),walker=pet(manager,"ErenYeager",1250,1);boolean turned=false;
  for(int t=0;t<140;t++){step(manager);turned|=!walker.isLookRight();require(walker.getAnchor().x>=64&&walker.getAnchor().x<=1536,"boundary escape");}
  require(turned&&walker.getAnchor().x<1250,"no persistent turn at blocked edge");manager.disposeAll();System.out.println("PASS blocked edge changes direction and continues");
 }
 static void queue()throws Exception {
  factory.env.resize(new Rectangle(0,0,2400,900));Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  Mascot leader=pet(manager,"ErenYeager",1400,0);leader.setPaused(true);
  List<Mascot> walkers=List.of(pet(manager,"Tom",1100,1),pet(manager,"Jerry",800,1),pet(manager,"NarutoShippuden",500,1),pet(manager,"SasukeShippuden",200,1));
  int passes=0;Map<Mascot,Behavior> seen=new IdentityHashMap<>();
  for(int t=0;t<420;t++){
   step(manager);for(Mascot m:walkers)if(m.getBehavior() instanceof PedestrianPass){passes++;if(seen.put(m,m.getBehavior())!=m.getBehavior())System.out.println("QUEUE t="+t+" "+m.getImageSet()+" "+m.getBehavior());}
   var all=manager.getMascots();for(int i=0;i<all.size();i++)for(int j=i+1;j<all.size();j++)require(!alphaBounds(all.get(i)).intersects(alphaBounds(all.get(j))),"queue visual overlap tick="+t+" "+all.get(i).getImageSet()+all.get(i).getAnchor()+" / "+all.get(j).getImageSet()+all.get(j).getAnchor());
   require(leader.getAnchor().x==1400,"paused leader displaced");
  }
  require(passes>0&&walkers.stream().anyMatch(m->m.getAnchor().x>1500),"queue never passed leader");manager.disposeAll();factory.env.resize(new Rectangle(0,0,1600,900));System.out.println("PASS four moving followers / paused leader: no visual overlaps, overtaking and progress");
 }
 @SuppressWarnings("unchecked") public static void main(String[] args){try {
  Logger.getLogger("").setLevel(Level.SEVERE);factory=new Factory();factory.env.resize(new Rectangle(0,0,1600,900));
  Field nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,factory);
  var app=Main.getInstance();app.getProperties().setProperty("Sounds","false");app.getProperties().setProperty("Scaling","1.0");app.getProperties().setProperty("MovementSpeed","1.0");
  Field lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);lang.set(app,ResourceBundle.getBundle("language",Locale.ENGLISH));
  time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
  Field cf=Main.class.getDeclaredField("configurations");cf.setAccessible(true);configs=(Map<String,Configuration>)cf.get(app);
  for(String name:NAMES)configs.put(name,config(name,"behaviors.xml"));cursor(-500,-500,0,0);
  for(String name:NAMES)for(int dir:new int[]{1,-1})stoppedLeader(name,dir);
  headOn();boundary();queue();
  System.out.println("PASS locomotion assertions="+checks);AudioManager.global().close();System.exit(0);
 }catch(Throwable ex){ex.printStackTrace();AudioManager.global().close();System.exit(1);}}
}
