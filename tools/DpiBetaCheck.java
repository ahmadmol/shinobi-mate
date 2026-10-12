import com.group_finity.mascot.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.cloud.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
/** Synthetic menu DPI/layout cases; records actual connected displays without changing OS settings. */
public class DpiBetaCheck {
 public static void main(String[]args)throws Exception {
  System.out.println("ACTUAL display count="+GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length+" toolkit DPI="+Toolkit.getDefaultToolkit().getScreenResolution());
  for(var device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())System.out.println("DISPLAY "+device.getDefaultConfiguration().getBounds()+" transform="+device.getDefaultConfiguration().getDefaultTransform());
  SwingUtilities.invokeAndWait(()->{
   try {
    var app=Main.getInstance();Path evidence=Path.of("art/release/screenshots");Files.createDirectories(evidence);
    for(int dpi:new int[]{96,120,144})for(String language:new String[]{"ar","en-GB"}) {
     app.getProperties().setProperty("MenuDPI",Integer.toString(dpi));app.getProperties().setProperty("Language",language);app.refreshProductLanguage();app.refreshProductTheme();
     SettingsDialog settings=new SettingsDialog(null);settings.setVisible(true);Rectangle bounds=settings.getGraphicsConfiguration().getBounds();
     if(!bounds.contains(settings.getBounds()))throw new AssertionError("settings outside monitor");
     if(settings.getComponentOrientation().isLeftToRight()!=language.equals("en-GB"))throw new AssertionError("settings orientation");
     BufferedImage image=new BufferedImage(settings.getWidth(),settings.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();settings.paint(graphics);graphics.dispose();ImageIO.write(image,"png",evidence.resolve("settings-"+dpi+"-"+language+".png").toFile());settings.dispose();
     double scale=dpi/96.0;var monitors=java.util.List.of(new Rectangle(-1920,0,1920,1040),new Rectangle(0,0,2560,1400));
     for(Rectangle mascot:java.util.List.of(new Rectangle(-1900,0,128,128),new Rectangle(-100,1000,128,128),new Rectangle(2490,1350,128,128))){
      Rectangle monitor=CloudPlacement.monitorFor(mascot,monitors),cloud=CloudPlacement.place(mascot,new Dimension((int)(320*scale),(int)(140*scale)),monitor);
      if(!monitor.contains(cloud))throw new AssertionError("scaled cloud outside synthetic monitor");
     }
     System.out.println("PASS synthetic "+dpi+" DPI ("+Math.round(scale*100)+"%) "+language+": real settings window, RTL/LTR, negative-origin two-monitor cloud placements");
    }
   }catch(Exception error){throw new RuntimeException(error);}
  });
  System.out.println("LIMIT: synthetic DPI cases do not prove native Windows 125%/150% scaling or a physical second monitor");System.exit(0);
 }
}
