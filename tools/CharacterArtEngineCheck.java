import java.awt.*;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;
import com.group_finity.mascot.*;
import com.group_finity.mascot.config.*;
import com.group_finity.mascot.imagesetchooser.*;

/** Uses the installed JAR with the existing in-memory environment adapter. */
public class CharacterArtEngineCheck extends PolishEngineCheck {
 static void sittingAndCrawling(Configuration c,String character)throws Exception {
  Rectangle w=factory.env.work.toRectangle();int mid=w.x+w.width/2,floor=w.y+w.height;
  cursor(w.x-500,w.y-500,0,0);
  for(String name:List.of("Stand","Sit","SitAndLookUp","SitAndLookAtMouse","SitAndSpinHeadAction","Sprawl")) {
   Mascot m=mascot(character,mid,floor);
   require(run(c,m,name,Map.of("Duration","40"),40,true)>0,name+" did not display");m.dispose();
  }
  for(int sign:List.of(-1,1)) {
   Mascot m=mascot(character,mid,floor);int target=mid+sign*50;
   int n=run(c,m,"Creep",Map.of("TargetX",Integer.toString(target)),600,true);
   require(m.getAnchor().x==target,"crawl missed target");require(n<600,"crawl did not finish");m.dispose();
  }
  // Legs intentionally hang below an edge anchor; exercise them above the floor.
  for(String name:List.of("SitWithLegsUp","SitWithLegsDown","SitAndDangleLegs")) {
   factory.env.ie.set(new Rectangle(w.x+70,floor-45,w.width-140,40));factory.env.ie.setVisible(true);
   Mascot m=mascot(character,mid,floor-45);
   require(run(c,m,name,Map.of("Duration","40"),40,true)>0,name+" did not display");m.dispose();
   factory.env.ie.setVisible(false);
  }
 }
 @SuppressWarnings("unchecked")
 static void cohort(List<String> names)throws Exception {
  factory.env.resize(new Rectangle(0,0,600,500));cursor(-500,-500,0,0);
  Field configField=Main.class.getDeclaredField("configurations");configField.setAccessible(true);
  Map<String,Configuration> configs=(Map<String,Configuration>)configField.get(Main.getInstance());
  Manager manager=new Manager();manager.setExitOnLastRemoved(false);
  for(String character:names) {
   Configuration c=config(character,"behaviors.xml");configs.put(character,c);
   Mascot m=mascot(character,300,500);m.setManager(manager);manager.getMascots().add(m);
  }
  require(manager.getCount()==names.size(),"wrong cohort size");
  for(Mascot m:manager.getMascots()) {
   Configuration c=configs.get(m.getImageSet());
   require(run(c,m,"Stand",Map.of("Duration","12"),12,true)>0,"cohort stand");
   run(c,m,"NinjaJump",Map.of(),120,true);
   require(m.getAnchor().y==500,"cohort hop baseline");
  }
  if(names.size()>1) {
   for(Mascot m:manager.getMascots()) {
    require(manager.hasOverlappingMascotsAtPoint(m.getAnchor()),"shared manager overlap detection");
    Configuration c=configs.get(m.getImageSet());
    require(run(c,m,"HugAction",Map.of(),100,true)>0,"cross-character hug did not display");
    require(m.getImage()!=null,"cross-character hug lost image");
   }
  }
  for(Mascot m:new ArrayList<>(manager.getMascots()))m.dispose();
  System.out.println("PASS cohort "+names+": shared manager, images, short jumps"+(names.size()>1?", cross-character hug":""));
 }
 static void chooser()throws Exception {
  for(String name:List.of("AstaBlackClover","NarutoShippuden","SasukeShippuden"))
   require(ImageSetChooser.isValidCharacterPack(new File("img",name)),"chooser rejected "+name);
  SwingUtilities.invokeAndWait(()->{
   try {
    ImageSetChooser chooser=new ImageSetChooser(null,false);
    Field sets=ImageSetChooser.class.getDeclaredField("imageSets");sets.setAccessible(true);
    Collection<?> names=(Collection<?>)sets.get(chooser);
    require(names.containsAll(List.of("AstaBlackClover","NarutoShippuden","SasukeShippuden")),"actual chooser list missing a character: "+names);
    chooser.dispose();
   } catch(Exception e){throw new RuntimeException(e);}
  });
  System.out.println("PASS actual Swing character chooser discovers all three packs");
 }
 public static void main(String[]args) {
  try {
   Logger.getLogger("").setLevel(Level.SEVERE);
   factory=new Factory();Field f=NativeFactory.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,factory);
   Field lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);
   lang.set(Main.getInstance(),ResourceBundle.getBundle("language",Locale.ENGLISH));
   time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
   for(String character:List.of("AstaBlackClover","NarutoShippuden","SasukeShippuden"))
    for(String profile:List.of("behaviors.xml","CalmBehavior.xml","MischievousBehavior.xml","ProfessionalBehavior.xml")) {
     Configuration c=config(character,profile);
     for(Rectangle area:List.of(new Rectangle(0,0,600,500),new Rectangle(-1920,-200,1920,1080),new Rectangle(100,80,320,240))) {
      factory.env.resize(area);tests(c,character);sittingAndCrawling(c,character);
     }
     System.out.println("PASS "+character+" / "+profile+": load, standing, walking, sitting, crawling, jump, drag, wall, ceiling, mouse reaction; three work areas");
    }
   cohort(List.of("AstaBlackClover"));
   cohort(List.of("AstaBlackClover","NarutoShippuden","SasukeShippuden"));
   chooser();
   System.out.println("PASS total: "+checks+" assertions across "+ticks+" installed-engine ticks. Movement uses in-memory windows; chooser is real Swing.");
   System.exit(0);
  }catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
