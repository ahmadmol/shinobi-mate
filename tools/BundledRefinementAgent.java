import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import java.awt.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;

/** Test-only attachment to the exact, unmodified packaged launcher/JVM. Never shipped. */
public class BundledRefinementAgent {
 static Component named(Container root,String name){for(Component c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container inner){var found=named(inner,name);if(found!=null)return found;}}return null;}
 public static void agentmain(String arg,Instrumentation unused)throws Exception {
  String[] fields=arg.split("\\|",2);Path log=Path.of(fields[0]);Main app=Main.getInstance();String mode=fields[1];StringBuilder text=new StringBuilder();
  text.append("version=").append(BuildInfo.VERSION).append("\njava=").append(System.getProperty("java.version")).append("\njava.home=").append(System.getProperty("java.home")).append("\nprofile=").append(AppPaths.userData()).append("\n");
  if(mode.equals("first")){
   if(app.getManager().getCount()!=0)throw new AssertionError("automatic startup spawn");
   SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if("ShinobiMateOnboarding".equals(w.getName())){text.append("onboardingShowing=").append(w.isShowing()).append('\n');((JButton)named(w,"OnboardingNext")).doClick();}});
   long end=System.nanoTime()+20_000_000_000L;while(!app.isCharacterLoadingReady()&&System.nanoTime()<end)Thread.sleep(40);
   if(!app.isCharacterLoadingReady()||app.getManager().getCount()!=0)throw new AssertionError("manager startup not ready or automatic spawn");
  }
  if(mode.equals("eight")){for(var p:new CharacterService().installed())if(new CharacterService().count(p.id())==0&&!new CharacterService().launch(p.id()))throw new AssertionError("spawn failed "+p.id());Thread.sleep(1500);}
  text.append("count=").append(app.getManager().getCount()).append("\ntray=").append(SystemTray.getSystemTray().getTrayIcons().length).append("\n");
  SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if("ShinobiMateCharacters".equals(w.getName()))text.append("managerShowing=").append(w.isShowing()).append('\n');});
  Files.writeString(log,text.toString());
 }
}
