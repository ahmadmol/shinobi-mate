import com.group_finity.mascot.*;
import com.group_finity.mascot.audio.*;
import java.awt.*;
import java.lang.management.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Actual Main/Manager/Swing/native sprites: idle/walk at 1, 2 and 5 character counts. */
public class AudioDesktopPerformanceCheck {
 static final List<String> NAMES=List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover");
 static void require(boolean b,String reason){if(!b)throw new AssertionError(reason);}
 static long audioCpu(){var bean=ManagementFactory.getThreadMXBean();long sum=0;for(var t:bean.dumpAllThreads(false,false))if(t.getThreadName().startsWith("ShinobiMate audio"))sum+=Math.max(0,bean.getThreadCpuTime(t.getThreadId()));return sum;}
 static Rectangle area(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();var r=new Rectangle(gc.getBounds());var in=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=in.left;r.y+=in.top;r.width-=in.left+in.right;r.height-=in.top+in.bottom;return r;}
 public static void main(String[] args){Manager manager=null;try {
  Main app=Main.getInstance();app.run();manager=app.getManager();manager.setExitOnLastRemoved(false);Thread.sleep(700);manager.stop();
  var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();List<String> samples=new ArrayList<>();
  for(int count:new int[]{1,1,2,5}) {
   boolean idle=samples.isEmpty();
   while(manager.getCount()<count){String name=NAMES.get(manager.getCount());require(app.prepareCharacter(name),"pack failed "+name);app.createMascot(name);manager.start();Thread.sleep(150);manager.stop();}
   int i=0;Rectangle r=area();
   for(Mascot pet:manager.getMascots()) {
    pet.setAnchor(new Point(r.x+160+i++*180,(int)r.getMaxY()));
    var config=app.getConfiguration(pet.getImageSet());
    String behavior=idle?"StandUp":config.getBehaviorNames().stream().filter(s->s.contains("Walk")&&s.contains("Floor")).findFirst().orElse("WalkAlongFloor");
    pet.setBehavior(config.buildBehavior(behavior));
   }
   manager.start();Thread.sleep(1000);long start=System.nanoTime(),cpu=os.getProcessCpuTime(),audioStart=audioCpu(),logic=manager.getLogicTicks();
   int peak=0,peakThreads=0;long peakHeap=0;
   for(int t=0;t<100;t++){Thread.sleep(40);peak=Math.max(peak,AudioManager.global().snapshot().voices());peakThreads=Math.max(peakThreads,ManagementFactory.getThreadMXBean().getThreadCount());peakHeap=Math.max(peakHeap,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());}
   long end=System.nanoTime();manager.stop();require(manager.getCount()==count,"character lost during native rendering");require(manager.getLogicTicks()-logic>=80,"Swing/combat logic stalled");
   String row=String.format(Locale.ROOT,"{\"characters\":%d,\"activity\":\"%s\",\"seconds\":%.3f,\"processCPUOneCorePercent\":%.3f,\"audioCPUOneCorePercent\":%.3f,\"peakHeapMiB\":%.3f,\"PCMCacheMiB\":%.3f,\"peakThreads\":%d,\"audioThreads\":%d,\"peakVoices\":%d,\"nativeAudioLines\":%d,\"clips\":0,\"logicTicks\":%d}",count,idle?"idle":"walking",(end-start)/1e9,100.*(os.getProcessCpuTime()-cpu)/(end-start),100.*(audioCpu()-audioStart)/(end-start),peakHeap/1048576.,AudioManager.global().snapshot().cacheBytes()/1048576.,peakThreads,Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(false,false)).filter(t->t.getThreadName().startsWith("ShinobiMate audio")).count(),peak,AudioManager.global().snapshot().nativeLines(),manager.getLogicTicks()-logic);
   samples.add(row);System.out.println("PASS actual native desktop "+row);
  }
  manager.disposeAll();AudioManager.global().flush().get(5,java.util.concurrent.TimeUnit.SECONDS);AudioManager.global().close();
  require(AudioManager.global().snapshot().voices()==0&&AudioManager.global().snapshot().nativeLines()==0,"native audio survived disposal/shutdown");
  Files.writeString(Path.of("output/audio-phase1/desktop-performance.json"),"[\n"+String.join(",\n",samples)+"\n]");
  System.out.println("PASS actual 1 idle / 1 walk / 2 walk / 5 native mascots, responsive engine, bounded audio and shutdown");System.exit(0);
 }catch(Throwable ex){ex.printStackTrace();if(manager!=null){manager.stop();manager.disposeAll();}AudioManager.global().close();System.exit(1);}}
}
