import com.group_finity.mascot.*;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.image.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import javax.swing.*;
import java.io.*;

/** Real native mascot/cloud/popup/VFX checks on every connected monitor. */
public class MultiMonitorBetaCheck extends CombatDesktopCheck {
 public static void main(String[]args) {
  try {
  Path root=AppPaths.application();PrintStream output=new PrintStream(Files.newOutputStream(root.resolve("multi-monitor-result.txt")),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(output);System.setErr(output);
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL multi-monitor timeout");System.exit(2);}},60000);
   ProductionLogging.initialize();Main app=Main.getInstance();app.run();manager=app.getManager();Thread.sleep(600);manager.stop();
   mascotWindow=Mascot.class.getDeclaredMethod("getWindow");mascotWindow.setAccessible(true);tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
   cloudField=Mascot.class.getDeclaredField("spiritualCloud");cloudField.setAccessible(true);cloudWindow=SpiritualCloud.class.getDeclaredField("window");cloudWindow.setAccessible(true);cloudRender=SpiritualCloud.class.getDeclaredMethod("render");cloudRender.setAccessible(true);
   var pets=java.util.List.copyOf(manager.getMascots());require(pets.size()>=2,"two mascots required");Mascot a=pets.get(0),b=pets.get(1);
   var displays=GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
   if(displays.length<2){System.out.println("SKIP physical second monitor unavailable; run on a dual-monitor host");manager.disposeAll();System.exit(0);return;}
   for(var device:displays) {
    var gc=device.getDefaultConfiguration();Rectangle work=new Rectangle(gc.getBounds());Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);work.x+=insets.left;work.y+=insets.top;work.width-=insets.left+insets.right;work.height-=insets.top+insets.bottom;
    System.out.println("MONITOR "+work+" transform="+gc.getDefaultTransform());
    for(int i=0;i<2;i++) {
     Mascot m=i==0?a:b;m.setAnchor(new Point(work.x+180+i*(work.width-360),work.y+work.height));
     m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();
     if(device==displays[0])SwingUtilities.invokeAndWait(()->{try{cloud(m).click();}catch(Exception error){throw new RuntimeException(error);}});
     SwingUtilities.invokeAndWait(()->{try{cloudRender.invoke(cloud(m));}catch(Exception error){throw new RuntimeException(error);}});
     require(work.contains(component(m).getBounds()),"mascot outside selected monitor");
     require(work.contains(((JWindow)cloudWindow.get(cloud(m))).getBounds()),"cloud outside selected monitor");
    }
    SwingUtilities.invokeAndWait(()->{try{Component c=component(a);Point p=c.getLocationOnScreen();c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),0,64,64,p.x+64,p.y+64,1,true,3));}catch(Exception error){throw new RuntimeException(error);}});
    SwingUtilities.invokeAndWait(()->{
     JPopupMenu actual=null;for(MenuElement e:MenuSelectionManager.defaultManager().getSelectedPath())if(e instanceof JPopupMenu menu)actual=menu;
     require(actual!=null,"native popup absent");Point p=actual.getLocationOnScreen();require(work.contains(new Rectangle(p,actual.getSize())),"popup outside selected monitor");actual.setVisible(false);MenuSelectionManager.defaultManager().clearSelectedPath();
    });
    require(manager.getCombat().start(a,b),"manual pair refused on monitor");boolean vfx=false;
    for(int frame=0;frame<75;frame++) {
     pulse(1);require(work.contains(component(a).getBounds())&&work.contains(component(b).getBounds()),"fighter crossed monitor boundary");
     for(Window window:Window.getWindows())if("ShinobiMateCombatEffects".equals(window.getName())&&window.isShowing()){vfx=true;require(work.contains(window.getBounds()),"VFX outside selected monitor");}
     SwingUtilities.invokeAndWait(()->{try{cloudRender.invoke(cloud(a));cloudRender.invoke(cloud(b));}catch(Exception error){throw new RuntimeException(error);}});
     require(work.contains(((JWindow)cloudWindow.get(cloud(a))).getBounds()),"combat cloud outside monitor");
    }
    require(vfx,"no VFX on selected monitor");manager.getCombat().stop(a);SwingUtilities.invokeAndWait(()->{});
    System.out.println("PASS native monitor: mascots, pinned clouds, right-click popup, manual combat/VFX, stop; "+work);
   }
   System.out.println("PASS "+displays.length+" physical monitors, "+assertions+" assertions; 150% OS scaling was not changed/tested");manager.disposeAll();System.exit(0);
  }catch(Throwable error){error.printStackTrace();if(manager!=null){manager.stop();manager.disposeAll();}System.exit(1);}
 }
}
