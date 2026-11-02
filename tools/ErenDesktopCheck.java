import com.group_finity.mascot.*;
import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Isolated native desktop test; Swing ticks keep the transformation nonblocking. */
public class ErenDesktopCheck {
 static Manager manager;static Mascot eren,opponent;static List<Mascot> opponents;
 static CombatMatch match;static int pairIndex,ticks,assertions;static javax.swing.Timer timer;
 static void require(boolean value,String reason){assertions++;if(!value)throw new AssertionError(reason);}
 static CombatMatch current()throws Exception {
  Field field=CombatEngine.class.getDeclaredField("pairs");field.setAccessible(true);
  Object pair=((List<?>)field.get(manager.getCombat())).getFirst();Method m=pair.getClass().getDeclaredMethod("match");m.setAccessible(true);return (CombatMatch)m.invoke(pair);
 }
 static Rectangle area(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());Insets i=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=i.left;r.y+=i.top;r.width-=i.left+i.right;r.height-=i.top+i.bottom;return r;}
 static void capture(String phase)throws Exception {
  Rectangle r=eren.getBounds().union(opponent.getBounds());r.grow(40,40);r=r.intersection(area());
  Path out=Path.of(System.getProperty("eren.output"),"native-"+phase+".png");
  ImageIO.write(new Robot().createScreenCapture(r),"png",out.toFile());
 }
 static void startPair()throws Exception {
  opponent=opponents.get(pairIndex);Rectangle r=area();
  for(var pet:List.of(eren,opponent)) {
   pet.setPaused(false);pet.setAnchor(new Point(r.x+(pet==eren?360:800),r.y+r.height));
   pet.setBehavior(Main.getInstance().getConfiguration(pet.getImageSet()).buildBehavior("StandUp"));pet.getBehavior().next();pet.apply();
  }
  require(manager.getCombat().start(eren,opponent),"native pair start");match=current();ticks=0;
  require(match.a.form==CombatMatch.Form.HUMAN,"native start not human");
 }
 static void step() {
  try {
   manager.getCombat().tick();eren.apply();opponent.apply();ticks++;
   require(area().contains(eren.getBounds()),"native Eren outside monitor");
   if(ticks==26){require(match.a.form==CombatMatch.Form.HUMAN,"native immediate transform");if(pairIndex==0)capture("human");match.a.hp=50;}
   if(pairIndex==0&&ticks==42)capture("burst");
   if(pairIndex==0&&ticks==57)capture("titan-reveal");
   if(ticks==85) {
    require(match.a.form==CombatMatch.Form.TITAN,"native transformation stuck");
    require(eren.getImage().size().height==256,"native Titan wrong size");if(pairIndex==0)capture("titan");
    manager.getCombat().stop(eren);require(eren.getImage().size().height==128,"native reset failed");
    require(manager.getCombat().start(eren,opponent),"native rematch failed");require(current().a.form==CombatMatch.Form.HUMAN,"native rematch not human");manager.getCombat().stop(eren);
    System.out.println("PASS native "+opponent.getImageSet()+" vs Eren: human, transformation, 2x Titan, bounds, reset/rematch");
    if(++pairIndex==opponents.size()){timer.stop();manager.disposeAll();System.out.println("PASS native Eren: "+assertions+" assertions");System.exit(0);}else startPair();
   }
  }catch(Throwable e){fail(e);}
 }
 static void fail(Throwable e){e.printStackTrace();if(timer!=null)timer.stop();if(manager!=null){manager.stop();manager.disposeAll();}System.exit(1);}
 public static void main(String[] args) {
  try {
   new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){fail(new AssertionError("native timeout"));}},45000);
   Main app=Main.getInstance();app.run();
   javax.swing.Timer ready=new javax.swing.Timer(1200,event->{try{
    Method get=Main.class.getDeclaredMethod("getManager");get.setAccessible(true);manager=(Manager)get.invoke(app);manager.stop();
    eren=manager.getMascots().stream().filter(m->m.getImageSet().equals("ErenYeager")).findFirst().orElseThrow();
    opponents=manager.getMascots().stream().filter(m->m!=eren).toList();require(opponents.size()==5,"not all 5 native opponents loaded");
    startPair();timer=new javax.swing.Timer(8,e->step());timer.start();
   }catch(Throwable e){fail(e);}});ready.setRepeats(false);ready.start();
  }catch(Throwable e){fail(e);}
 }
}
