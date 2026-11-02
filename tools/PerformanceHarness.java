import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.character.CharacterService;
import com.group_finity.mascot.audio.AudioManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.management.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import javax.swing.*;
import jdk.jfr.*;

/** Same controlled native actions, production 40ms manager, rendering and enabled services before/after. */
public class PerformanceHarness {
 static Main app;static Manager manager;static final List<String> names=List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover","Tom","Jerry","ErenYeager");
 static Path out=Path.of(System.getProperty("perf.output"));
 static class Controlled implements Behavior {
  final String name;Mascot pet;Action action;boolean right;
  Controlled(String name,boolean right){this.name=name;this.right=right;}
  void reset()throws Exception{var r=pet.getEnvironment().getWorkArea().toRectangle();pet.setLookRight(right);action=app.getConfiguration(pet.getImageSet()).buildAction(name,Map.of("Duration","100000","TargetX",Integer.toString(right?(int)r.getMaxX()-80:r.x+80)));action.init(pet);}
  public void init(Mascot p)throws com.group_finity.mascot.exception.CantBeAliveException{pet=p;try{reset();}catch(Exception e){throw new com.group_finity.mascot.exception.CantBeAliveException("benchmark action",e);}}
  public void next()throws com.group_finity.mascot.exception.CantBeAliveException{try{if(!action.hasNext()){right=!right;reset();}action.next();}catch(Exception e){throw new com.group_finity.mascot.exception.CantBeAliveException("benchmark action",e);}}
  public void mousePressed(MouseEvent e){}public void mouseReleased(MouseEvent e){}
 }
 static void phase(String activity,int count,int seconds,boolean profile)throws Exception {
  var service=new CharacterService();for(int i=0;i<count;i++)if(service.count(names.get(i))==0&&!service.launch(names.get(i)))throw new AssertionError("spawn "+names.get(i));
  long deadline=System.nanoTime()+20_000_000_000L;while(manager.getCount()!=count&&System.nanoTime()<deadline)Thread.sleep(40);
  if(manager.getCount()!=count)throw new AssertionError("count "+manager.getCount());manager.stop();
  var r=com.group_finity.mascot.environment.DesktopWorkArea.at(new Point(500,500));
  synchronized(manager.getMascots()){int index=0;for(var p:manager.getMascots()){
   p.setAnchor(new Point(r.x+90+(index++)*(r.width-180)/Math.max(1,count-1),(int)r.getMaxY()));p.getEnvironment().refreshWorkArea();p.setBehavior(new Controlled(activity.equals("natural")?"Walk":activity,true));
   if(activity.equals("natural"))p.setBehavior(app.getConfiguration(p.getImageSet()).buildBehavior("StandUp"));
  }}manager.start();Thread.sleep(3000);
  var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();var tm=ManagementFactory.getThreadMXBean();
  List<String> rows=new ArrayList<>(List.of("seconds,ticks,cpuNanos,heapBytes,threads,edtMillis,voices,cacheBytes"));
  try(Recording rec=new Recording(Configuration.getConfiguration("profile"))){
   rec.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(10));rec.enable("jdk.NativeMethodSample").withPeriod(Duration.ofMillis(10));
   if(profile)rec.start();long start=System.nanoTime(),cpu=os.getProcessCpuTime(),ticks=manager.getLogicTicks();
   for(int i=0;i<=seconds;i++){long ping=System.nanoTime();SwingUtilities.invokeAndWait(()->{});double edt=(System.nanoTime()-ping)/1e6;var audio=AudioManager.global().snapshot();
    rows.add(String.format(Locale.ROOT,"%.6f,%d,%d,%d,%d,%.3f,%d,%d",(System.nanoTime()-start)/1e9,manager.getLogicTicks()-ticks,os.getProcessCpuTime()-cpu,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory(),tm.getThreadCount(),edt,audio.voices(),audio.cacheBytes()));
    Files.write(out.resolve(activity+"-"+count+".csv"),rows);if(i<seconds)Thread.sleep(1000);
   }
   double elapsed=(System.nanoTime()-start)/1e9;System.out.printf(Locale.ROOT,"RESULT %s count=%d seconds=%.3f ticksPerSecond=%.3f cpuCores=%.3f%n",activity,count,elapsed,(manager.getLogicTicks()-ticks)/elapsed,(os.getProcessCpuTime()-cpu)/1e9/elapsed);
   if(profile){rec.stop();rec.dump(out.resolve(activity+"-"+count+".jfr"));}
  }
 }
 public static void main(String[]args)throws Exception {
  try{Files.createDirectories(out);Main.main(new String[0]);app=Main.getInstance();manager=app.getManager();SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if(w instanceof com.group_finity.mascot.ui.CharacterManagerWindow)w.setVisible(false);});
   String only=System.getProperty("perf.only","");if(only.isEmpty()){phase("Stand",0,10,false);phase("Stand",1,20,false);phase("Walk",1,20,false);phase("Stand",8,30,true);phase("Walk",8,40,true);phase("natural",8,40,true);}else phase(only,Integer.getInteger("perf.count",8),Integer.getInteger("perf.seconds",40),true);
   System.out.println("PASS native benchmark completed; production logic target unchanged");app.exit();
  }catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
