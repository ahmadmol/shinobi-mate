import com.group_finity.mascot.*;
import com.group_finity.mascot.environment.DesktopWorkArea;
import com.group_finity.mascot.image.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import javax.swing.*;

/** Native regression for retained layered-window pixels, opacity, resize and work-area copies. */
public class PerformanceNativeCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static void edt(Runnable task)throws Exception{SwingUtilities.invokeAndWait(task);}
 public static void main(String[]args)throws Exception {
  try {
   Field platform=Main.class.getDeclaredField("platform");platform.setAccessible(true);platform.set(Main.getInstance(),com.group_finity.mascot.Platform.x86_64);
   DesktopWorkArea.refresh();Point point=new Point(300,300);Rectangle original=DesktopWorkArea.at(point);
   Rectangle copy=DesktopWorkArea.at(point);copy.setBounds(-1,-1,1,1);
   require(DesktopWorkArea.at(point).equals(original),"caller mutated shared monitor cache");
   Thread.sleep(120);require(DesktopWorkArea.at(point).equals(original),"stopped-engine fallback changed work area");
   TranslucentWindow window=NativeFactory.getInstance().newTransparentWindow();Component component=window.asComponent();
   var image=new BufferedImage(80,80,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.setColor(Color.MAGENTA);g.fillRect(0,0,80,80);g.dispose();
   NativeImage first=NativeFactory.getInstance().newNativeImage(image);var second=NativeFactory.getInstance().newNativeImage(new BufferedImage(80,80,BufferedImage.TYPE_INT_ARGB));
   Field painted=component.getClass().getDeclaredField("painted");painted.setAccessible(true);
   edt(()->{component.setBounds(original.x+150,original.y+150,80,80);window.setImage(first);component.setVisible(true);window.updateImage();});Thread.sleep(300);edt(()->{});
   Object initial=painted.get(component);require(initial!=null,"initial native upload failed");
   edt(()->{for(int i=0;i<200;i++)window.updateImage();});Thread.sleep(180);edt(()->{});
   require(painted.get(component)==initial,"identical image generated redundant native repaint");
   edt(()->{component.setLocation(original.x+260,original.y+180);window.updateImage();});Thread.sleep(180);
   require(component.getX()==original.x+260&&painted.get(component)!=null,"retained sprite did not move");
   Method alpha=component.getClass().getMethod("setAlpha",int.class);alpha.setAccessible(true);
   Object moved=painted.get(component);edt(()->{try{alpha.invoke(component,120);}catch(Exception ex){throw new RuntimeException(ex);}window.updateImage();});Thread.sleep(200);
   require(painted.get(component)!=moved,"opacity change not painted");
   Object opacity=painted.get(component);edt(()->{component.setSize(100,100);window.updateImage();});Thread.sleep(200);
   require(painted.get(component)!=opacity,"resized image not painted");
   Object resized=painted.get(component);edt(()->{window.setImage(second);window.updateImage();});Thread.sleep(200);
   require(painted.get(component)!=resized,"new animation frame not painted");
   edt(()->{component.setVisible(false);component.setVisible(true);window.updateImage();});Thread.sleep(200);
   require(painted.get(component)!=null&&component.isVisible(),"show/hide lost sprite");edt(window::dispose);
   System.out.println("PASS native work-area defensive copies/stale fallback; 200 unchanged redraws skipped; movement/opacity/resize/frame/show-hide retained");System.exit(0);
  }catch(Throwable failure){failure.printStackTrace();System.exit(1);}
 }
}
