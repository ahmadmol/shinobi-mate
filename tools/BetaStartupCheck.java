import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.pack.*;
import java.nio.file.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.concurrent.atomic.*;

/** Real production Main.main() via the relocated Windows native launcher and bundled runtime. */
public class BetaStartupCheck {
 static Path result;static int checks;
 static void require(boolean value,String text){checks++;if(!value)throw new AssertionError(text);}
 static Component named(Container root,String name){for(Component c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container child){Component found=named(child,name);if(found!=null)return found;}}return null;}
 public static void main(String[] args)throws Exception {
  Path root=AppPaths.application();result=root.resolve("beta-startup-result.txt");
  PrintStream log=new PrintStream(Files.newOutputStream(result),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(log);System.setErr(log);
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL startup timeout");System.exit(2);}},60000);
  boolean first=Boolean.getBoolean("beta.firstRun");String wanted=System.getProperty("beta.language","ar");
  AtomicBoolean onboarding=new AtomicBoolean();
  javax.swing.Timer guide=new javax.swing.Timer(250,e->{
   for(Window window:Window.getWindows())if(window.isShowing()&&"ShinobiMateOnboarding".equals(window.getName())){
    try{
     onboarding.set(true);JComboBox<?> language=(JComboBox<?>)named((Container)window,"OnboardingLanguage");
     if(language!=null)language.setSelectedIndex(wanted.equals("ar")?0:1);
     JButton next=(JButton)named((Container)window,"OnboardingNext");if(next!=null)next.doClick();
    }catch(Throwable error){error.printStackTrace();System.exit(1);}
   }
  });guide.start();
  try {
   require(System.getProperty("java.home").startsWith(root.resolve("runtime").toString()),"external Java runtime");
   require(System.getenv("JAVA_HOME")==null&&System.getenv("PATH").indexOf("Android")==-1,"Java/Maven environment not removed");
   if(first)require(!Files.exists(SettingsStore.path()),"first-run profile was seeded");
   Main.main(args);guide.stop();Thread.sleep(1600);
   Main app=Main.getInstance();require(app.getManager().getCount()==0&&app.getManager().getAdded().isEmpty(),"startup spawned a character without selection");
   require(app.getProperties().getProperty("OnboardingComplete").equals("true"),"welcome not saved");
   require(app.getProperties().getProperty("Language").equals(wanted.equals("ar")?"ar":"en-GB"),"language not saved");
   if(first){require(onboarding.get(),"welcome missing");require(java.util.Arrays.stream(Window.getWindows()).anyMatch(w->w.isShowing()&&"ShinobiMateCharacters".equals(w.getName())),"first-run manager missing");}
   require(Files.isDirectory(AppPaths.packs())&&Files.isDirectory(AppPaths.userData().resolve("logs")),"profile missing");
   require(!Files.exists(root.resolve("conf/settings.properties")),"installation settings were created");
   require(java.util.Arrays.stream(SystemTray.getSystemTray().getTrayIcons()).anyMatch(t->t.getToolTip().equals("ShinobiMate")),"wrong tray brand");
   for(var m:app.getManager().getMascots())require(!app.getManager().getCombat().active(m),"automatic combat");
   var service=new com.group_finity.mascot.character.CharacterService();require(service.launch(service.installed().getFirst().id()),"manual launch failed");
   long launchUntil=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);while(app.getManager().getCount()==0&&System.nanoTime()<launchUntil)Thread.sleep(25);
   require(app.getManager().getCount()==1,"manual selection did not produce exactly one character");
   // A broken additional pack displays an error, while the active mascot keeps running.
   Path invalid=AppPaths.packs().resolve("BrokenBeta");Files.createDirectories(invalid.resolve("conf"));
   Files.writeString(invalid.resolve("conf/actions.xml"),"<invalid");Files.writeString(invalid.resolve("conf/behaviors.xml"),"<invalid");
   AtomicBoolean shown=new AtomicBoolean();javax.swing.Timer closeError=new javax.swing.Timer(50,e->{for(Window w:Window.getWindows())if(w instanceof JDialog&&w.isShowing()&&!"ShinobiMateOnboarding".equals(w.getName())){shown.set(true);w.dispose();}});closeError.start();
   try{require(!app.prepareCharacter("BrokenBeta"),"broken pack launched");}finally{closeError.stop();}
   require(shown.get()&&app.getManager().getCount()>0,"bad pack silently stopped application");
   System.out.println("PASS production native startup "+wanted+" firstRun="+first+"; "+checks+" assertions; runtime="+System.getProperty("java.version"));
   System.out.println("APP="+root+" PROFILE="+AppPaths.userData()+" CWD="+System.getProperty("user.dir"));
   if(wanted.equals("en-GB")){System.out.println("SHUTDOWN=Swing EDT");SwingUtilities.invokeAndWait(app::exit);}
   else{System.out.println("SHUTDOWN=main thread");app.exit();}
  }catch(Throwable error){error.printStackTrace();System.exit(1);}
 }
}
