import com.group_finity.mascot.*;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.image.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.file.*;
import java.lang.management.ManagementFactory;
import java.util.*;
public final class PerformanceCheck {
 static class Idle implements Behavior {
  final String id;int age;
  Idle(String id){this.id=id;}
  public void init(Mascot m){this.m=m;}Mascot m;
  public void next(){age++;m.setImage(ImagePairs.getImage("/"+id+"/"+(age%20<17?"Stand.png":"Blink.png"),m.isLookRight()));}
  public void mousePressed(MouseEvent e){}public void mouseReleased(MouseEvent e){}
 }
 public static void main(String[] args)throws Exception {
  Path root=Path.of(System.getProperty("app.dir")).toAbsolutePath().normalize();
  var out=new java.io.PrintStream(Files.newOutputStream(root.resolve("performance-result.json")),true,java.nio.charset.StandardCharsets.UTF_8);
  Main app=Main.getInstance();app.run();Manager manager=app.getManager();manager.setExitOnLastRemoved(false);
  var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
  app.getProperties().setProperty("CloudEnabled","false");app.getProperties().setProperty("Mute","true");
  Thread.sleep(500);manager.stop();var pets=manager.getMascots();
  Rectangle area=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
  for(int i=0;i<pets.size();i++){var pet=pets.get(i);pet.setAnchor(new Point(area.x+180+i*200,area.y+area.height-60));pet.setBehavior(new Idle(pet.getImageSet()));pet.getBehavior().next();pet.apply();}
  manager.start();out.println("[");boolean comma=false;
  for(String profile:java.util.List.of("LOW","BALANCED","HIGH")){
   app.getProperties().setProperty("PerformanceProfile",profile);Thread.sleep(1000);
   long cpu=os.getProcessCpuTime(),start=System.nanoTime(),logic=manager.getLogicTicks(),render=manager.getRenderTicks();Thread.sleep(3000);
   long end=System.nanoTime(),logicEnd=manager.getLogicTicks(),renderEnd=manager.getRenderTicks(),cpuEnd=os.getProcessCpuTime();
   double seconds=(end-start)/1e9,percent=100*(cpuEnd-cpu)/(end-start);
   long heap=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
   int gdi=com.sun.jna.NativeLibrary.getInstance("user32").getFunction("GetGuiResources",com.sun.jna.Function.ALT_CONVENTION)
    .invokeInt(new Object[]{com.sun.jna.platform.win32.Kernel32.INSTANCE.GetCurrentProcess(),0});
   if(comma)out.println(",");comma=true;
   out.printf(java.util.Locale.ROOT,"{\"characters\":%d,\"profile\":\"%s\",\"seconds\":%.3f,\"logicHz\":%.2f,\"renderHz\":%.2f,\"cpuOneCorePercent\":%.2f,\"heapMiB\":%.2f,\"gdiHandles\":%d}",pets.size(),profile,seconds,
    (logicEnd-logic)/seconds,(renderEnd-render)/seconds,percent,heap/1048576.0,gdi);out.flush();
  }
  out.println("\n]");out.close();manager.stop();manager.disposeAll();System.exit(0);
 }
}
