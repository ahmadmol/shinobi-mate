import com.group_finity.mascot.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.logging.*;

/** Exercise actual pack reload and configuration preparation concurrently, with lock inversion monitoring. */
public class ReloadConcurrencyCheck {
 public static void main(String[] args)throws Exception {
  Logger.getLogger("").setLevel(Level.SEVERE);
  var nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,new PolishEngineCheck.Factory());
  Main app=Main.getInstance();if(!app.prepareCharacter("ErenYeager"))throw new AssertionError("preload failed");
  var begin=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  var loading=pool.submit(()->{begin.await();for(int i=0;i<12;i++)if(!app.prepareCharacter("Tom"))throw new AssertionError("prepare failed");return true;});
  var reload=pool.submit(()->{begin.await();for(int i=0;i<12;i++){if(i%2==0)app.reloadAppearance();else if(!app.reloadCharacter("ErenYeager"))throw new AssertionError("reload failed");}return true;});
  begin.countDown();long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(40);
  while(!loading.isDone()||!reload.isDone()) {
   if(ManagementFactory.getThreadMXBean().findDeadlockedThreads()!=null)throw new AssertionError("deadlocked pack locks");
   if(System.nanoTime()>end)throw new AssertionError("concurrent operations timed out");Thread.sleep(20);
  }
  loading.get();reload.get();pool.shutdown();app.getManager().disposeAll();com.group_finity.mascot.audio.AudioManager.global().close();
  System.out.println("PASS 12 concurrent real reloads + 12 prepares; no deadlocked threads; no characters spawned");System.exit(0);
 }
}
