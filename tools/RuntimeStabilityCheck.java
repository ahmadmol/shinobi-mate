import com.group_finity.mascot.*;
import com.group_finity.mascot.audio.SoundSystem;
import com.group_finity.mascot.behavior.Behavior;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.Clip;
import javax.swing.SwingUtilities;

/** Fault injection verifies engine isolation, lock ordering, slow audio and observable EDT stalls. */
public class RuntimeStabilityCheck extends CombatCheck {
 static void drain()throws Exception{var field=SoundSystem.class.getDeclaredField("worker");field.setAccessible(true);((ExecutorService)field.get(null)).submit(()->{}).get(5,TimeUnit.SECONDS);}
 public static void main(String[] args){try{test();System.out.println("PASS engine fault isolation/render lock order, slow/failing audio isolation, watchdog stack diagnostics; "+checks+" assertions");System.exit(0);}catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 @SuppressWarnings("unchecked")static void test()throws Exception{
  ProductionLogging.initialize();var manager=Main.getInstance().getManager();manager.setExitOnLastRemoved(false);
  factory=new Factory(){public com.group_finity.mascot.image.TranslucentWindow newTransparentWindow(){return new Window(){public void updateImage(){require(!Thread.holdsLock(manager.getMascots()),"render holds mascot lock");try{SwingUtilities.invokeAndWait(()->manager.getCount());}catch(Exception ex){throw new RuntimeException(ex);}}};}};
  Field nativeField=NativeFactory.class.getDeclaredField("instance");nativeField.setAccessible(true);nativeField.set(null,factory);
  var image=new com.group_finity.mascot.image.MascotImage(new java.awt.image.BufferedImage(8,8,2),new java.awt.Point(4,8));
  Mascot healthy=new Mascot("NarutoShippuden"),faulty=new Mascot("SasukeShippuden");healthy.setImage(image);healthy.setAnchor(new java.awt.Point(200,500));
  faulty.setBehavior(new Behavior(){public void init(Mascot m){}public void next(){throw new IllegalStateException("fixture failure");}public void mousePressed(java.awt.event.MouseEvent e){}public void mouseReleased(java.awt.event.MouseEvent e){}});
  manager.add(healthy);manager.add(faulty);var tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);tick.invoke(manager);tick.invoke(manager);
  require(manager.getCount()==1&&healthy.getManager()==manager&&faulty.getManager()==null,"one faulty mascot stopped healthy engine");
  var props=Main.getInstance().getProperties();props.setProperty("Mute","false");props.setProperty("Sounds","true");drain();
  var field=SoundSystem.class.getDeclaredField("clips");field.setAccessible(true);var clips=(Map<Integer,Clip>)field.get(null);
  CountDownLatch blocked=new CountDownLatch(1),release=new CountDownLatch(1);java.util.concurrent.atomic.AtomicBoolean once=new java.util.concurrent.atomic.AtomicBoolean();
  Clip slow=(Clip)Proxy.newProxyInstance(Clip.class.getClassLoader(),new Class[]{Clip.class},(proxy,method,values)->{
   if(method.getName().equals("setFramePosition")&&once.compareAndSet(false,true)){blocked.countDown();release.await(5,TimeUnit.SECONDS);}
   if(method.getName().equals("start"))throw new IllegalStateException("audio device failure fixture");
   Class<?> type=method.getReturnType();return type==boolean.class?false:type==int.class?0:type==long.class?0L:type==float.class?0f:null;
  });clips.put(0,slow);
  Field keys=SoundSystem.class.getDeclaredField("clipKeys");keys.setAccessible(true);((Map<Integer,String>)keys.get(null)).put(0,"UI:"+Math.floorMod("generic".hashCode(),5)+":0");
  long begin=System.nanoTime();SoundSystem.play(SoundSystem.Category.UI);require(System.nanoTime()-begin<TimeUnit.MILLISECONDS.toNanos(100),"slow audio blocked caller");require(blocked.await(2,TimeUnit.SECONDS),"slow audio fixture did not execute");
  long ticks=manager.getLogicTicks();tick.invoke(manager);SwingUtilities.invokeAndWait(()->{});require(manager.getLogicTicks()>ticks,"slow audio blocked engine");release.countDown();drain();
  SoundSystem.close();
  manager.start();RuntimeDiagnostics.start(manager);
  CountDownLatch stallDone=new CountDownLatch(1);SwingUtilities.invokeLater(()->{try{Thread.sleep(7000);}catch(InterruptedException ex){Thread.currentThread().interrupt();}finally{stallDone.countDown();}});
  require(stallDone.await(9,TimeUnit.SECONDS),"diagnostic stall fixture timeout");manager.stop();manager.disposeAll();
  java.util.logging.LogManager.getLogManager().getLogger("").getHandlers()[0].flush();
  String log=java.nio.file.Files.readString(AppPaths.userData().resolve("logs/shinobimate.log.0"));
  require(log.contains("DIAG EDT_DELAY")&&log.contains("RuntimeStabilityCheck"),"stall diagnostics absent");require(log.contains("DIAG AUDIO_PLAYBACK_FAILED"),"audio failure diagnostics absent");require(!log.contains("audio device failure fixture")&&!log.contains("fixture failure"),"private exception messages leaked");
 }
}
