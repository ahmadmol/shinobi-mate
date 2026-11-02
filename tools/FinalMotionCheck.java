import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Accelerated native renderer/action QA, forced state entries (not natural wall-to-ceiling transitions). */
public class FinalMotionCheck {
 static Main app;static Manager manager;static List<Mascot> pets;static int index,repeat,phase,age,checks;static boolean right;
 static Action action;static Field time;static Method window;static javax.swing.Timer timer;static Dimension expected;
 static final String[] names={"Stand","Sit","ClimbWall","ClimbCeiling"};
 static Rectangle area(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();var r=new Rectangle(gc.getBounds());var i=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=i.left;r.y+=i.top;r.width-=i.left+i.right;r.height-=i.top+i.bottom;return r;}
 static Mascot pet(){return pets.get(index);}
 static void start()throws Exception {
  var m=pet();var r=area();int x=r.x+r.width/2,y=(int)r.getMaxY();var vars=new HashMap<String,String>();String name=names[phase];
  if(phase==2){x=right?(int)r.getMaxX():r.x;y=r.y+r.height/2;vars.put("TargetY",""+(r.y+80));}
  if(phase==3){y=r.y;vars.put("TargetX",""+(x+(right?120:-120)));}
  m.setLookRight(right);m.setAnchor(new Point(x,y));m.getEnvironment().refreshWorkArea();
  action=app.getConfiguration(m.getImageSet()).buildAction(name,vars);action.init(m);age=0;
 }
 static void step(){try {
  var m=pet();NativeFactory.getInstance().getEnvironment().tick();if(action.hasNext())action.next();time.setInt(m,m.getTime()+1);m.apply();age++;
  if(m.getImage()==null)throw new AssertionError("missing bitmap");var size=m.getImage().size();
  if(expected==null)expected=new Dimension(size);if(!expected.equals(size))throw new AssertionError("canvas changed "+m.getImageSet()+" "+names[phase]+" "+expected+" -> "+size);
  if(!m.getBounds().intersects(area()))throw new AssertionError("offscreen "+m.getImageSet()+" "+names[phase]);checks++;
  if(age>=8){
   if(++phase==4){phase=0;if(++repeat==10){repeat=0;
    System.out.println("PASS native "+m.getImageSet()+" facing="+right+" scale="+app.getProperties().getProperty("Scaling")+" canvas="+size+" 10x stand/sit/wall/ceiling state entries");
    if(!right)right=true;else{((TranslucentWindow)window.invoke(m)).asComponent().setVisible(false);right=false;expected=null;if(++index==pets.size()){
     timer.stop();manager.disposeAll();com.group_finity.mascot.audio.AudioManager.global().close();System.out.println("PASS native motion checks="+checks+"; forced state entries, natural continuous wall-to-ceiling transition NOT VERIFIED");System.exit(0);
    }}
   }}start();
  }
 }catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 public static void main(String[]args)throws Exception {
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL native motion timeout");System.exit(2);}},150000);
  app=Main.getInstance();app.run();manager=app.getManager();manager.stop();
  RuntimeDiagnostics.close();
  manager.getMascots().addAll(manager.getAdded());manager.getAdded().clear();pets=new ArrayList<>(manager.getMascots());
  if(pets.size()!=8)throw new AssertionError("expected eight restored fixture characters");
  time=Mascot.class.getDeclaredField("time");time.setAccessible(true);window=Mascot.class.getDeclaredMethod("getWindow");window.setAccessible(true);
  SwingUtilities.invokeAndWait(()->{try{
   for(Window w:Window.getWindows())if("ShinobiMateCharacters".equals(w.getName()))w.setVisible(false);
   for(var m:pets)((TranslucentWindow)window.invoke(m)).asComponent().setVisible(false);start();timer=new javax.swing.Timer(8,e->step());timer.start();
  }catch(Exception ex){throw new RuntimeException(ex);}});
 }
}
