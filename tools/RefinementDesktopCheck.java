import com.group_finity.mascot.*;
import com.group_finity.mascot.audio.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.settings.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.lang.management.*;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Native Windows QA using real Main and its normal engine thread, with isolated profile only. */
public class RefinementDesktopCheck {
 static final Path out=Path.of(System.getProperty("refinement.output","output/refinement/native")).toAbsolutePath();
 static Main app;static Manager manager;static int checks;static List<String> report=new ArrayList<>();
 static void require(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
 static void pass(String text)throws Exception{report.add("PASS "+text);Files.write(out.resolve("results.txt"),report);System.out.println("PASS "+text);}
 static void edt(Runnable r)throws Exception{SwingUtilities.invokeAndWait(r);}
 static void waitFor(java.util.function.BooleanSupplier ready,int seconds,String reason)throws Exception{long end=System.nanoTime()+seconds*1_000_000_000L;while(!ready.getAsBoolean()&&System.nanoTime()<end)Thread.sleep(30);require(ready.getAsBoolean(),reason);}
 static Component named(Container root,String name){for(var c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container inner){var found=named(inner,name);if(found!=null)return found;}}return null;}
 static void capture(Window w,String name)throws Exception{w.validate();var im=new java.awt.image.BufferedImage(w.getWidth(),w.getHeight(),2);var g=im.createGraphics();w.paint(g);g.dispose();ImageIO.write(im,"png",out.resolve(name+".png").toFile());}
 static Mascot pet(String id){synchronized(manager.getMascots()){return manager.getMascots().stream().filter(m->m.getImageSet().equals(id)).findFirst().orElseThrow();}}
 static CombatMatch match()throws Exception{var f=CombatEngine.class.getDeclaredField("pairs");f.setAccessible(true);Object pair=((List<?>)f.get(manager.getCombat())).getFirst();var get=pair.getClass().getDeclaredMethod("match");get.setAccessible(true);return (CombatMatch)get.invoke(pair);}
 static void fight(String one,String two)throws Exception{
  var a=pet(one);var b=pet(two);manager.stop();
  Rectangle r=a.getEnvironment().getWorkArea().toRectangle();
  synchronized(manager.getMascots()){for(var m:List.of(a,b)){m.setPaused(false);m.setAnchor(new Point(r.x+r.width/2+(m==a?-200:200),(int)r.getMaxY()));m.getEnvironment().refreshWorkArea();m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));}
   require(manager.getCombat().start(a,b),"explicit combat request rejected");}
  var fight=match();manager.start();boolean titan=false,damaged=false;Set<String> moves=new TreeSet<>();int hp=100;long end=System.nanoTime()+90_000_000_000L;
  while(!fight.isStopped()&&System.nanoTime()<end){synchronized(manager.getMascots()){
   if(fight.a.form==CombatMatch.Form.TITAN){titan=true;if(fight.b.hp<hp)damaged=true;if(fight.a.attack!=null)moves.add(fight.a.attack.id());}hp=fight.b.hp;
  }Thread.sleep(10);}
  require(fight.isFinished()&&fight.isStopped(),"natural engine fight did not finish");
  if(one.equals("ErenYeager")){require(titan&&damaged,"Titan did not fight effectively");require(fight.a.form==CombatMatch.Form.HUMAN,"Eren did not reset");}
  pass("normal 40 ms engine "+one+" vs "+two+" complete; titan="+titan+" titanDamage="+damaged+" moves="+moves);
 }
 public static void main(String[]args)throws Exception{
  Files.createDirectories(out);long seconds=Long.getLong("refinement.soakSeconds",600L);
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL native QA timeout");System.exit(2);}},(seconds+240)*1000);
  try{
   Main.main(new String[0]);app=Main.getInstance();manager=app.getManager();Thread.sleep(800);
   require(manager.getCount()==0,"startup spawned a character");pass("actual Main startup: manager visible, zero characters, isolated profile");
   CharacterService service=new CharacterService();require(service.launch("NarutoShippuden"),"solo launch");waitFor(()->manager.getCount()==1,10,"solo pending");Thread.sleep(1500);pass("one natural companion responsive");
   for(var p:service.installed())if(service.count(p.id())==0)require(service.launch(p.id()),"launch "+p.id());
   waitFor(()->manager.getCount()==8,15,"eight pending");synchronized(manager.getMascots()){for(var m:manager.getMascots())require(!manager.getCombat().active(m),"automatic combat");}pass("eight installed companions, no automatic combat");
   for(String language:List.of("ar","en-GB"))for(String theme:List.of("dark","light"))edt(()->{try{
    app.getProperties().setProperty("Language",language);app.getProperties().setProperty("FlatLafTheme",theme);app.refreshProductLanguage();app.refreshProductTheme();
    CharacterManagerWindow.open();for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow)capture(w,"manager-"+theme+"-"+language);
    SettingsDialog dialog=new SettingsDialog(null);dialog.setVisible(true);var nav=(JList<?>)named(dialog,"SettingsNavigation");require(nav.getModel().getSize()==10,"settings sections");nav.setSelectedIndex(4);capture(dialog,"settings-audio-"+theme+"-"+language);require(!CompanionLayering.alwaysOnTop(),"mascots cover settings");dialog.dispose();
   }catch(Exception ex){throw new RuntimeException(ex);}});
   pass("real Swing dark/light and Arabic/English sidebar/cards captured; layering policy enforced");
   edt(()->{for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow)w.setVisible(false);});
   fight("ErenYeager","NarutoShippuden");fight("Tom","Jerry");fight("Tom","Jerry");
   var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();long start=System.nanoTime();
   List<String> csv=new ArrayList<>(List.of("seconds,cpuNanos,heapBytes,javaThreads,displayableWindows,voices,loops,cacheBytes,audioFailures,engineTicks,edtMillis"));
   for(long elapsed=0;elapsed<=seconds;elapsed+=15){long ping=System.nanoTime();edt(()->{});double latency=(System.nanoTime()-ping)/1e6;var audio=AudioManager.global().snapshot();
    require(manager.getCount()==8,"companion lost during soak");require(audio.voices()<=12&&audio.cacheBytes()<=AudioManager.MAX_CACHE_BYTES,"audio bounds");
    long ticks=manager.getLogicTicks();Thread.sleep(150);require(manager.getLogicTicks()>ticks,"engine stopped");
    csv.add(String.format(Locale.ROOT,"%.3f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.3f",(System.nanoTime()-start)/1e9,os.getProcessCpuTime(),Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory(),ManagementFactory.getThreadMXBean().getThreadCount(),Arrays.stream(Window.getWindows()).filter(Window::isDisplayable).count(),audio.voices(),audio.loops(),audio.cacheBytes(),audio.failures(),manager.getLogicTicks(),latency));Files.write(out.resolve("soak.csv"),csv);
    if(elapsed<seconds)Thread.sleep(15000);
   }
   AudioManager.global().stopAll();AudioManager.global().flush().get();require(AudioManager.global().snapshot().loops()==0,"stuck loop");pass("completed "+seconds+" second normal-engine eight-character soak; "+checks+" checks; see soak.csv; installed JDK, not bundled EXE");
   app.exit();
  }catch(Throwable error){error.printStackTrace();Files.writeString(out.resolve("FAILED.txt"),error.toString());System.exit(1);}
 }
}
