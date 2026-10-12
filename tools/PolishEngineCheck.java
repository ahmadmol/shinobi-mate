import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.xml.parsers.*;
import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.config.*;
import com.group_finity.mascot.environment.*;
import com.group_finity.mascot.image.*;

/** Runs the unmodified installed engine with an in-memory window/environment adapter. */
public class PolishEngineCheck {
 static class Pixels implements NativeImage { final BufferedImage pixels; Pixels(BufferedImage p){pixels=p;} }
 static class Window implements TranslucentWindow {
  final Canvas component=new Canvas(); public Component asComponent(){return component;}
  public void setImage(NativeImage i){} public void updateImage(){} public void dispose(){} public void setAlwaysOnTop(boolean b){}
 }
 static class TestEnvironment extends Environment {
  final Area work=new Area(),ie=new Area();
  TestEnvironment(){resize(new Rectangle(0,0,600,500));ie.setVisible(false);}
  void resize(Rectangle r){work.set(r);work.setVisible(true);screen.set(r);screen.setVisible(true);}
  protected Area getWorkArea(){return work;} public Area getActiveIE(){return ie;}
  public String getActiveIETitle(){return "";} public long getActiveWindowId(){return 0;}
  public void moveActiveIE(Point p){} public void restoreIE(){} public void refreshCache(){} public void dispose(){}
  public void init(){} public void tick(){} public Collection<Area> getScreens(){return List.of(work);}
 }
 static class Factory extends NativeFactory {
  final TestEnvironment env=new TestEnvironment();public Environment getEnvironment(){return env;}
  public NativeImage newNativeImage(BufferedImage p){return new Pixels(p);}
  public TranslucentWindow newTransparentWindow(){return new Window();}
 }
 static Factory factory;static Field time; static int checks=0,ticks=0;
 static void require(boolean b,String reason){checks++;if(!b)throw new AssertionError(reason);}
 static Mascot mascot(String c,int x,int y)throws Exception{
  Mascot m=new Mascot(c);m.setManager(new Manager());m.setAnchor(new Point(x,y));m.getEnvironment().refreshWorkArea();return m;
 }
 static Configuration config(String character,String profile)throws Exception{
  var d=DocumentBuilderFactory.newInstance();d.setNamespaceAware(false);
  Configuration c=new Configuration();
  for(String file:List.of("actions.xml",profile))c.load(new Entry(d.newDocumentBuilder().parse(Path.of("img",character,"conf",file).toFile()).getDocumentElement()),character);
  c.validate();for(String name:c.getBehaviorNames())c.buildBehavior(name);
  return c;
 }
 static Rectangle alphaBounds(Mascot m){
  MascotImage im=m.getImage();if(im==null)return null;
  BufferedImage p=((Pixels)im.image()).pixels;int l=p.getWidth(),t=p.getHeight(),r=-1,b=-1;
  for(int y=0;y<p.getHeight();y++)for(int x=0;x<p.getWidth();x++)if((p.getRGB(x,y)>>>24)>16){l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);}
  return new Rectangle(m.getAnchor().x-im.center().x+l,m.getAnchor().y-im.center().y+t,r-l+1,b-t+1);
 }
 static void visible(Mascot m,String label){Rectangle r=alphaBounds(m);if(r!=null)require(factory.env.work.toRectangle().contains(r),label+": pixels outside work area "+r+" anchor "+m.getAnchor());}
 static int run(Configuration c,Mascot m,String name,Map<String,String> params,int limit,boolean bounds)throws Exception{
  Action a=c.buildAction(name,params);a.init(m);int n=0;
  while(n<limit&&a.hasNext()){
   a.next();ticks++;if(bounds)visible(m,name);n++;time.setInt(m,m.getTime()+1);
  }
  return n;
 }
 static void cursor(int x,int y,int dx,int dy){factory.env.cursor.setX(x);factory.env.cursor.setY(y);factory.env.cursor.setDx(dx);factory.env.cursor.setDy(dy);}
 static boolean effective(Configuration c,String name,Mascot m)throws Exception{
  Field f=Configuration.class.getDeclaredField("behaviorBuilders");f.setAccessible(true);
  BehaviorBuilder b=(BehaviorBuilder)((Map<?,?>)f.get(c)).get(name);
  var vars=new com.group_finity.mascot.script.VariableMap();vars.put("mascot",m);return b.isEffective(vars);
 }
 static void tests(Configuration c,String character)throws Exception{
  Rectangle w=factory.env.work.toRectangle();int mid=w.x+w.width/2,floor=w.y+w.height;
  cursor(w.x-500,w.y-500,0,0);
  for(String name:List.of("Walk","Run","Dash"))for(int sign:List.of(-1,1)){
   Mascot m=mascot(character,mid,floor);int target=mid+sign*100;
   int n=run(c,m,name,Map.of("TargetX",Integer.toString(target)),300,true);
   require(m.getAnchor().x==target,name+" missed target");require(m.isLookRight()==(sign==1),name+" facing wrong");require(n<300,name+" failed to finish");m.dispose();
  }
  Mascot m=mascot(character,mid,floor);
  run(c,m,"NinjaJump",Map.of(),150,true);require(m.getAnchor().y==floor,"hop must return to original baseline: "+m.getAnchor()+" expected "+floor);m.dispose();
  m=mascot(character,mid,w.y);run(c,m,"GrabCeiling",Map.of("Duration","24"),30,true);
  require(m.getImage().center().y==0,"ceiling anchor must be zero");
  run(c,m,"ClimbCeiling",Map.of("TargetX",Integer.toString(mid+80)),200,true);require(m.getAnchor().x==mid+80,"ceiling target");m.dispose();
  m=mascot(character,mid,w.y);run(c,m,"FallFromCeiling",Map.of(),300,true);require(m.getAnchor().y==floor,"ceiling fall must land");m.dispose();
  for(boolean right:List.of(false,true)){
   m=mascot(character,right?w.x+w.width:w.x,Math.max(w.y+128,floor-64));m.setLookRight(right);
   run(c,m,"ClimbAlongWall",Map.of(),4000,true);
   require(m.getAnchor().y==w.y,"wall must reach ceiling "+m.getAnchor()+" expected "+w.y+" work "+w);m.dispose();
  }
  for(int x:List.of(w.x-100,w.x,w.x+w.width,w.x+w.width+100)){
   m=mascot(character,x,w.y-200);cursor(mid,floor-60,1000,-1000);
   run(c,m,"Thrown",Map.of(),500,true);require(m.getAnchor().y<=floor,"throw overshot floor");m.dispose();
  }
  m=mascot(character,mid,floor);cursor(mid,floor-68,0,0);
  require(effective(c,"MouseReaction",m),"near mouse behavior unavailable");
  run(c,m,"MouseReaction",Map.of(),70,true);
  cursor(w.x-500,w.y-500,0,0);require(!effective(c,"MouseReaction",m),"far cursor should not trigger");m.dispose();
  m=mascot(character,mid,floor);cursor(w.x-500,w.y-500,0,0);run(c,m,"Stand",Map.of("Duration","12"),12,true);NativeImage neutral=m.getImage().image();
  cursor(mid,floor-68,0,0);run(c,m,"Stand",Map.of("Duration","12"),12,true);require(neutral!=m.getImage().image(),"stand should react to proximity");
  cursor(mid,floor-68,100,100);run(c,m,"MouseEmote",Map.of(),12,true);m.dispose();
  cursor(w.x-500,w.y-500,0,0);
  for(String name:List.of("CuteIdle","JumpFromLeftEdgeOfIE","JumpFromRightEdgeOfIE","WalkLeftAlongIEAndJump","WalkRightAlongIEAndJump","JumpFromLeftWall","JumpFromRightWall","PullUp","Divided")){
   m=mascot(character,mid,floor);run(c,m,name,Map.of(),300,true);m.dispose();
  }
  m=mascot(character,mid,floor-80);m.setCursorPosition(new Point(mid,floor-80));cursor(mid,floor-80,0,0);
  run(c,m,"Pinched",Map.of("Duration","25"),25,false);require(m.getImage()!=null,"drag pose missing");m.dispose();
 }
 public static void main(String[]args){
  try{
   Logger.getLogger("").setLevel(Level.SEVERE);for(Handler h:Logger.getLogger("").getHandlers())h.setLevel(Level.SEVERE);
   factory=new Factory();Field f=NativeFactory.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,factory);
   Field lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);lang.set(Main.getInstance(),ResourceBundle.getBundle("language",Locale.ENGLISH));
   time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
   for(String character:List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen"))for(String profile:List.of("behaviors.xml","CalmBehavior.xml","MischievousBehavior.xml","ProfessionalBehavior.xml")){
    Configuration c=config(character,profile);
    for(Rectangle area:List.of(new Rectangle(0,0,600,500),new Rectangle(-1920,-200,1920,1080),new Rectangle(100,80,320,240))){factory.env.resize(area);tests(c,character);}
    System.out.println("PASS "+character+" / "+profile+" : engine load, directions, ceiling, jumps, throw recovery, cursor reactions, three work areas");
   }
   System.out.println("PASS total: "+checks+" assertions across "+ticks+" installed-engine ticks; native window replaced by in-memory adapter.");System.exit(0);
  }catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
