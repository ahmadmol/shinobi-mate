import com.group_finity.mascot.*;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.focus.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.ui.*;
import java.awt.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Test-only attachment to exact portable payload; never included in a release image. */
public class ReleaseAcceptanceAgent {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static void edt(Runnable r)throws Exception{SwingUtilities.invokeAndWait(r);}
 static Component named(Container root,String name){for(Component c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container child){var found=named(child,name);if(found!=null)return found;}}return null;}
 static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 static void capture(Window window,Path path)throws Exception{var image=new java.awt.image.BufferedImage(window.getWidth(),window.getHeight(),2);var graphics=image.createGraphics();window.paint(graphics);graphics.dispose();ImageIO.write(image,"png",path.toFile());}
 public static void agentmain(String arg,Instrumentation unused)throws Exception {
  String[] parts=arg.split("\\|",2);Path log=Path.of(parts[0]);String mode=parts[1];Main app=Main.getInstance();Manager manager=app.getManager();StringBuilder result=new StringBuilder();
  try {
   result.append("version=").append(BuildInfo.VERSION).append(" java=").append(System.getProperty("java.version")).append(" count=").append(manager.getCount()).append('\n');
   if(mode.equals("hide")||mode.equals("features"))edt(()->{for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow)w.setVisible(false);});
   if(mode.equals("features")){
    Mascot pet; synchronized(manager.getMascots()){pet=manager.getMascots().stream().filter(m->m.getImageSet().equals("NarutoShippuden")).findFirst().orElseThrow();}
    manager.stop();synchronized(manager.getMascots()){var area=pet.getEnvironment().getWorkArea().toRectangle();pet.setPaused(false);pet.setAnchor(new Point(area.x+area.width/2,(int)area.getMaxY()));pet.getEnvironment().refreshWorkArea();pet.setBehavior(app.getConfiguration(pet.getImageSet()).buildBehavior("StandUp"));}manager.start();Thread.sleep(900);
    var cloud=(SpiritualCloud)field(pet,"spiritualCloud");require(cloud.isEnabled(),"cloud unavailable");cloud.mouseEnter();Thread.sleep(700);
    edt(()->{try{Window w=(Window)field(cloud,"window");require(w!=null&&w.isVisible(),"native hover cloud not visible");require(!w.isFocusableWindow(),"cloud steals focus");capture(w,log.resolveSibling("bundled-cloud.png"));}catch(Exception e){throw new RuntimeException(e);}});
    cloud.click();cloud.mouseExit();Thread.sleep(200);edt(()->{try{require(((CloudState)field(cloud,"state")).visibility()==CloudState.Visibility.PINNED,"pin lost on exit");}catch(Exception e){throw new RuntimeException(e);}});cloud.click();Thread.sleep(600);
    edt(()->{try{require(!((Window)field(cloud,"window")).isVisible(),"cloud failed to hide");}catch(Exception e){throw new RuntimeException(e);}});
    result.append("PASS native cloud hover/fade/pin/exit/hide and nonfocusable window\n");
    app.getProperties().setProperty("FocusReminders","false");
    final FocusDialog[] dialog=new FocusDialog[1];edt(()->{dialog[0]=new FocusDialog(null);dialog[0].setVisible(true);((JButton)named(dialog[0],"FocusStart")).doClick();});
    require(FocusService.session().phase()==FocusSession.Phase.FOCUS,"focus UI start");long remaining=FocusService.session().remaining(FocusService.now());Thread.sleep(300);require(FocusService.session().remaining(FocusService.now())<remaining,"real countdown frozen");
    edt(()->((JButton)named(dialog[0],"FocusPause")).doClick());long paused=FocusService.session().remaining(FocusService.now());Thread.sleep(300);require(FocusService.session().phase()==FocusSession.Phase.PAUSED&&FocusService.session().remaining(FocusService.now())==paused,"pause countdown changed");
    edt(()->((JButton)named(dialog[0],"FocusPause")).doClick());require(FocusService.session().phase()==FocusSession.Phase.FOCUS,"focus UI resume");edt(()->{((JButton)named(dialog[0],"FocusStop")).doClick();dialog[0].dispose();});require(FocusService.session().phase()==FocusSession.Phase.IDLE,"focus UI stop");
    result.append("PASS native Focus dialog start/real countdown/pause/resume/stop\n");
    SettingsService.set("MasterVolume","23");SettingsService.set("Mute","false");app.saveConfigFile();var props=new java.util.Properties();try(var input=Files.newInputStream(AppPaths.userData().resolve("settings.properties"))){props.load(input);}require("23".equals(props.getProperty("MasterVolume")),"audio persistence failed");
    var audio=com.group_finity.mascot.audio.AudioManager.global().flush().get();require(audio.failures()==0,"native audio failures");result.append("PASS audio settings persisted; failures=").append(audio.failures()).append('\n');
   }
   if(mode.equals("restart")){require(manager.getCount()==0,"restart automatically restored characters");require("23".equals(app.getProperties().getProperty("MasterVolume")),"restart lost audio preference");result.append("PASS restart: zero characters and MasterVolume=23 preserved\n");}
   edt(()->{for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow)result.append("managerShowing=").append(w.isShowing()).append('\n');});
   require(java.util.Arrays.stream(SystemTray.getSystemTray().getTrayIcons()).count()==1,"duplicate tray icon");
   Files.writeString(log,result.toString());
  }catch(Throwable error){Files.writeString(log,result+"FAIL "+error);throw new RuntimeException(error);}
 }
}
