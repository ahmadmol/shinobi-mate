import com.group_finity.mascot.*;
import com.group_finity.mascot.audio.SoundSystem;
import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Real Windows rendering/audio, complete lifecycle and repeated native-resource sampling. */
public class CombatPolishDesktopCheck extends CombatDesktopCheck {
 static final List<String> names=CombatPolishCheck.names;
 static int gdi(){return com.sun.jna.NativeLibrary.getInstance("user32").getFunction("GetGuiResources",com.sun.jna.Function.ALT_CONVENTION).invokeInt(new Object[]{com.sun.jna.platform.win32.Kernel32.INSTANCE.GetCurrentProcess(),0});}
 static long heap(){return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();}
 static void capture(String name,Rectangle area)throws Exception {
  var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
  var dm=gc.getDevice().getDisplayMode();Rectangle pixels=new Rectangle(gc.getBounds().x,gc.getBounds().y,dm.getWidth(),dm.getHeight());
  ImageIO.write(new Robot().createScreenCapture(pixels),"png",Path.of("captures",name+".png").toFile());
  // Also record the actual owned sprite buffers and Swing overlay, independently of desktop capture visibility.
  Rectangle fighters=null;
  for(Mascot pet:manager.getMascots())if(manager.getCombat().active(pet))fighters=fighters==null?pet.getBounds():fighters.union(pet.getBounds());
  if(fighters==null)return;
  int originX=fighters.x-60,originY=fighters.y-60;
  var preview=new java.awt.image.BufferedImage(Math.max(480,Math.min(1200,fighters.width+120)),Math.max(240,fighters.height+100),java.awt.image.BufferedImage.TYPE_INT_RGB);
  var graphics=preview.createGraphics();graphics.setColor(new Color(29,32,45));graphics.fillRect(0,0,preview.getWidth(),preview.getHeight());
  for(Mascot pet:manager.getMascots())if(pet.getImage()!=null&&manager.getCombat().active(pet)) {
   var bitmap=pet.getImage().image();Field field=bitmap.getClass().getDeclaredField("managedImage");field.setAccessible(true);
   var image=(java.awt.image.BufferedImage)field.get(bitmap);Rectangle bounds=pet.getBounds();
   graphics.drawImage(image,bounds.x-originX,bounds.y-originY,bounds.width,bounds.height,null);
  }
  SwingUtilities.invokeAndWait(()->{for(Window window:Window.getWindows())if("ShinobiMateCombatEffects".equals(window.getName())&&window.isShowing()) {
   Graphics2D layer=(Graphics2D)graphics.create();layer.translate(window.getX()-originX,window.getY()-originY);((JWindow)window).getContentPane().printAll(layer);layer.dispose();
  }});
  graphics.dispose();ImageIO.write(preview,"png",Path.of("captures",name+"-owned-render.png").toFile());
 }
 public static void main(String[]args) {
  try {
   if(System.getProperty("native.log")!=null){var stream=new java.io.PrintStream(Files.newOutputStream(Path.of(System.getProperty("native.log"))),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(stream);System.setErr(stream);}
   Files.createDirectories(Path.of("captures"));Main app=Main.getInstance();app.run();
   Method get=Main.class.getDeclaredMethod("getManager");get.setAccessible(true);manager=(Manager)get.invoke(app);manager.setExitOnLastRemoved(false);
   Thread.sleep(500);manager.stop();tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
   mascotWindow=Mascot.class.getDeclaredMethod("getWindow");mascotWindow.setAccessible(true);
   require(manager.getCount()==5,"five-pack native startup");
   Map<String,Mascot> pets=new HashMap<>();for(Mascot m:manager.getMascots())pets.put(m.getImageSet(),m);
   var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
   List<String> samples=new ArrayList<>();Rectangle area=desktopArea();
   List<int[]> testedPairs=Boolean.getBoolean("native.ninja")?List.of(new int[]{0,1},new int[]{1,0}):List.of(new int[]{0,1},new int[]{2,3},new int[]{4,0});
   for(int round=0;round<Integer.getInteger("native.rounds",2);round++)for(int[] ids:testedPairs) {
    Mascot a=pets.get(names.get(ids[0])),b=pets.get(names.get(ids[1]));int index=0;
    for(Mascot m:pets.values()) {
     m.setAnchor(new Point(area.x+140+index++*160,(int)area.getMaxY()));m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();
    }
    a.setAnchor(new Point(area.x+300,(int)area.getMaxY()));b.setAnchor(new Point(area.x+530,(int)area.getMaxY()));
    // Invoke the actual menu action for the selected pair.
    SwingUtilities.invokeAndWait(()->{
     JMenu menu=manager.getCombat().menu(a,app.getLanguageBundle());require(menu.getIcon()!=null,"combat icon missing");JMenu opponents=(JMenu)menu.getItem(0);
     for(int i=0;i<opponents.getItemCount();i++)if(opponents.getItem(i).getText().endsWith("#"+b.getInstanceId())){opponents.getItem(i).doClick();return;}
     throw new AssertionError("selected opponent menu absent");
    });
    require(manager.getCombat().active(a)&&manager.getCombat().active(b),"menu start failed");
    long cpu=os.getProcessCpuTime(),start=System.nanoTime();int steps=0,maxClips=0,maxGdi=0;boolean attack=false,special=false,victory=false;
    while(manager.getCombat().active(a)&&steps++<3000) {
     pulse(1);maxClips=Math.max(maxClips,SoundSystem.clipCount());maxGdi=Math.max(maxGdi,gdi());
     require(component(a).isShowing()&&component(b).isShowing(),"native fighter disappeared");
     require(area.contains(component(a).getBounds())&&area.contains(component(b).getBounds()),"native taskbar/work-area violation: "+area+" a="+component(a).getBounds()+" b="+component(b).getBounds());
     String state=manager.getCombat().statistics(a)+manager.getCombat().statistics(b);
     victory|=state.contains("VICTORY");
     if(round==0) {
      if(steps==15)capture(names.get(ids[0])+"-intro",area);
      if(!attack&&state.contains("ATTACK")){capture(names.get(ids[0])+"-attack",area);attack=true;}
      if(!special&&state.contains("SPECIAL_ATTACK")){capture(names.get(ids[0])+"-special",area);special=true;}
      if(state.contains("VICTORY")&&steps%20==0)capture(names.get(ids[0])+"-end",area);
     }
    }
    require(steps<3000,"native match never ended");SwingUtilities.invokeAndWait(()->{});
    require(victory,"native pair stopped without a winner");
    require(!manager.getCombat().active(b)&&a.getBehavior()!=null&&b.getBehavior()!=null,"native restore failed");
    for(Window w:Window.getWindows())if("ShinobiMateCombatEffects".equals(w.getName()))require(!w.isDisplayable(),"overlay survived native battle");
    long end=System.nanoTime(),cpuEnd=os.getProcessCpuTime();System.gc();Thread.sleep(100);
    String sample=String.format(Locale.ROOT,"{\"round\":%d,\"pair\":\"%s vs %s\",\"ticks\":%d,\"seconds\":%.3f,\"cpuOneCorePercent\":%.3f,\"heapMiB\":%.3f,\"threads\":%d,\"gdi\":%d,\"peakGdi\":%d,\"openAudioClips\":%d,\"peakAudioClips\":%d,\"cachedSounds\":%d}",round,names.get(ids[0]),names.get(ids[1]),steps,(end-start)/1e9,100.0*(cpuEnd-cpu)/(end-start),heap()/1048576.0,ManagementFactory.getThreadMXBean().getThreadCount(),gdi(),maxGdi,SoundSystem.clipCount(),maxClips,SoundSystem.cacheCount());
    samples.add(sample);System.out.println("PASS native complete battle: "+sample);
   }
   Files.writeString(Path.of("native-soak.json"),"[\n"+String.join(",\n",samples)+"\n]");
   manager.disposeAll();SoundSystem.close();SwingUtilities.invokeAndWait(()->{});require(SoundSystem.clipCount()==0,"native audio close leaked");
   System.out.println("PASS "+samples.size()+" complete native battles, Arabic combat menu, 5 characters, work area, normal restoration, overlay disposal, audio close; "+assertions+" assertions");System.exit(0);
  }catch(Throwable ex){ex.printStackTrace();if(manager!=null){manager.stop();manager.disposeAll();}SoundSystem.close();System.exit(1);}
 }
}
