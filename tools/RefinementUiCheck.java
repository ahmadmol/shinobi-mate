import com.group_finity.mascot.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.character.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Exercise sidebar/RTL/theme switching, font growth, button contrast and real async appearance reload. */
public class RefinementUiCheck extends UiRefinementCheck {
 static Path out=Path.of("output/refinement/screenshots");static SettingsDialog settings;
 static void capture(java.awt.Window w,String name)throws Exception{w.validate();var im=new java.awt.image.BufferedImage(w.getWidth(),w.getHeight(),2);var g=im.createGraphics();w.paint(g);g.dispose();ImageIO.write(im,"png",out.resolve(name+".png").toFile());}
 static double luminance(Color c){double[] rgb={c.getRed()/255.,c.getGreen()/255.,c.getBlue()/255.};for(int i=0;i<3;i++)rgb[i]=rgb[i]<=.04045?rgb[i]/12.92:Math.pow((rgb[i]+.055)/1.055,2.4);return .2126*rgb[0]+.7152*rgb[1]+.0722*rgb[2];}
 static void contrast(JButton b){double f=luminance(b.getForeground()),bg=luminance(b.getBackground());require((Math.max(f,bg)+.05)/(Math.min(f,bg)+.05)>4,"secondary text contrast too low");}
 public static void main(String[]args){try{
  Files.createDirectories(out);var app=Main.getInstance();var nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,new Factory());
  var ready=Main.class.getDeclaredField("characterLoadingReady");ready.setAccessible(true);ready.setBoolean(app,true);
  for(var p:new CharacterService().installed())require(app.prepareCharacter(p.id()),"pack preparation");
  app.getProperties().setProperty("MenuDPI","120");
  SwingUtilities.invokeAndWait(()->{try{
   for(String language:java.util.List.of("ar","en-GB"))for(String theme:java.util.List.of("dark","light")){
    app.getProperties().setProperty("Language",language);app.getProperties().setProperty("FlatLafTheme",theme);app.refreshProductLanguage();
    for(int i=0;i<6;i++){app.refreshProductTheme();require(UIManager.getFont("defaultFont").getSize()==20,"font grew on theme reload");}
    CharacterManagerWindow w=new CharacterManagerWindow();w.setVisible(true);w.setSize(1000,780);w.validate();capture(w,"manager-"+theme+"-"+language);
    var primary=button(w,"Launch.NarutoShippuden");require(primary.isEnabled()&&Theme.ACCENT.equals(primary.getBackground()),"primary launch lost hierarchy");
    contrast(button(w,"Remove.NarutoShippuden"));
    var summary=(JLabel)named(w,"CharacterSummary");require(!summary.getText().contains("\u2066")&&!summary.getText().contains("\u2069"),"unsupported isolate glyphs");
    w.dispose();SettingsDialog d=new SettingsDialog(null);d.setVisible(true);var nav=(JList<?>)named(d,"SettingsNavigation");require(nav.getModel().getSize()==10,"ten sidebar sections missing");
    for(int i=0;i<10;i++){nav.setSelectedIndex(i);d.validate();if(i==1||i==4)capture(d,"settings-"+i+"-"+theme+"-"+language);}
    require(d.getComponentOrientation().isLeftToRight()==!language.equals("ar"),"RTL orientation");d.dispose();
   }
   settings=new SettingsDialog(null);settings.setVisible(true);
  }catch(Exception ex){throw new RuntimeException(ex);}});
  CountDownLatch held=new CountDownLatch(1),release=new CountDownLatch(1);
  Thread lock=new Thread(()->{synchronized(app){held.countDown();try{release.await(5,TimeUnit.SECONDS);}catch(InterruptedException ex){Thread.currentThread().interrupt();}}});lock.start();held.await();
  SwingUtilities.invokeAndWait(()->((JSlider)named(settings,"Scaling")).setValue(150));
  CompletableFuture<Void> ping=new CompletableFuture<>();SwingUtilities.invokeLater(()->ping.complete(null));ping.get(2,TimeUnit.SECONDS);release.countDown();lock.join();
  long deadline=System.nanoTime()+30_000_000_000L;while(!((JSlider)named(settings,"Scaling")).isEnabled()&&System.nanoTime()<deadline)Thread.sleep(50);
  require(((JSlider)named(settings,"Scaling")).isEnabled()&&app.getProperties().getProperty("Scaling").equals("1.5"),"async scale did not complete");
  SwingUtilities.invokeAndWait(()->settings.dispose());app.getManager().disposeAll();com.group_finity.mascot.audio.AudioManager.global().close();
  System.out.println("PASS 24 theme refreshes at 125% without font growth; dark/light RTL/LTR sidebar/cards/contrast; async scale reload leaves EDT responsive; assertions="+checks);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
