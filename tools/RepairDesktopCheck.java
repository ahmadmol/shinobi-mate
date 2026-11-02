import com.group_finity.mascot.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.audio.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Real Main/Windows renderer/CombatEngine with screen recordings at the normal 40ms tick. */
public class RepairDesktopCheck {
 static Main app;static Manager manager;static Mascot a,b;static CombatMatch match;
 static int pair,ticks;static javax.swing.Timer timer;static Process recorder;
 static JFrame backdrop;
 static boolean titan,damaged;static Set<String> moves=new TreeSet<>();
 static Path out=Path.of(System.getProperty("repair.output"));
 static String[][] pairs=Boolean.getBoolean("repair.cartoon")?new String[][]{{"Tom","Jerry"},{"Jerry","Tom"},{"Tom","Jerry"}}:
   new String[][]{{"ErenYeager","NarutoShippuden"},{"ErenYeager","SasukeShippuden"},{"ErenYeager","Tom"}};
 static Rectangle area(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());Insets i=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=i.left;r.y+=i.top;r.width-=i.left+i.right;r.height-=i.top+i.bottom;return r;}
 static void require(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static void place(Mascot pet,int x)throws Exception{var r=area();pet.setPaused(false);pet.setAnchor(new Point(x,r.y+r.height));pet.getEnvironment().refreshWorkArea();pet.setBehavior(app.getConfiguration(pet.getImageSet()).buildBehavior("StandUp"));pet.getBehavior().next();pet.apply();}
 static Mascot get(String id){return manager.getMascots().stream().filter(m->m.getImageSet().equals(id)).findFirst().orElseThrow();}
 static CombatMatch current()throws Exception{var f=CombatEngine.class.getDeclaredField("pairs");f.setAccessible(true);var p=((List<?>)f.get(manager.getCombat())).getFirst();var method=p.getClass().getDeclaredMethod("match");method.setAccessible(true);return (CombatMatch)method.invoke(p);}
 static void record(String name)throws Exception{
  var r=area();var device=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();var gc=device.getDefaultConfiguration();var dm=device.getDisplayMode();var tx=java.awt.geom.AffineTransform.getScaleInstance((double)dm.getWidth()/gc.getBounds().width,(double)dm.getHeight()/gc.getBounds().height);r=new Rectangle((int)Math.round(r.x*tx.getScaleX()),(int)Math.round(r.y*tx.getScaleY()),(int)Math.round(r.width*tx.getScaleX()),(int)Math.round(r.height*tx.getScaleY()));int h=Math.min(650,r.height)/2*2;int w=r.width/2*2;
  recorder=new ProcessBuilder(System.getProperty("repair.ffmpeg"),"-hide_banner","-y","-f","gdigrab","-framerate","20","-offset_x",""+r.x,"-offset_y",""+(r.y+r.height-h),"-video_size",w+"x"+h,"-i","desktop","-c:v","libx264","-preset","ultrafast","-crf","23","-pix_fmt","yuv420p",out.resolve(name+".mp4").toString()).redirectError(out.resolve(name+"-capture.log").toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
 }
 static void endRecord()throws Exception{if(recorder!=null){recorder.getOutputStream().write('q');recorder.getOutputStream().flush();require(recorder.waitFor(8,java.util.concurrent.TimeUnit.SECONDS)&&recorder.exitValue()==0,"recording failed");recorder=null;}}
 static void visible(Mascot m,boolean show)throws Exception{var method=Mascot.class.getDeclaredMethod("getWindow");method.setAccessible(true);((com.group_finity.mascot.image.TranslucentWindow)method.invoke(m)).asComponent().setVisible(show);}
 static void start()throws Exception{
  a=get(pairs[pair][0]);b=get(pairs[pair][1]);var r=area();place(a,r.x+r.width/2-240);place(b,r.x+r.width/2+240);
  if(backdrop==null){backdrop=new JFrame();backdrop.setUndecorated(true);backdrop.getContentPane().setBackground(new Color(30,36,47));backdrop.setBounds(r.x,r.y+r.height-520,r.width,520);var label=new JLabel("ShinobiMate — native Windows QA / real CombatEngine / 40 ms",SwingConstants.CENTER);label.setForeground(Color.WHITE);label.setVerticalAlignment(SwingConstants.TOP);backdrop.add(label);backdrop.setVisible(true);}
  for(var pet:manager.getMascots())if(pet!=a&&pet!=b)visible(pet,false);
  // This JVM reports logical bounds but uses physical coordinates for native windows.
  // Cover the full physical primary screen so recordings contain no other applications.
  var display=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
  backdrop.setBounds(0,0,display.getWidth(),display.getHeight());
  visible(a,true);visible(b,true);
  require(manager.getCombat().start(a,b),"native combat did not start");match=current();ticks=0;titan=false;damaged=false;moves.clear();
  record(pairs[pair][0]+"-vs-"+pairs[pair][1]+"-"+pair);
  System.out.println("START "+Arrays.toString(pairs[pair])+" area="+area()+" desktopDPI="+Toolkit.getDefaultToolkit().getScreenResolution());
 }
 static void step(){try{
  int hp=match.a.hp,enemyHp=match.b.hp;manager.getCombat().tick();a.apply();b.apply();ticks++;
  if(!match.isStopped())require(match.a.hp<=hp,"HP increased in fight");
  require(area().contains(a.getBounds())&&area().contains(b.getBounds()),"native render outside safe work area");
  if(match.a.form==CombatMatch.Form.TITAN){titan=true;if(match.b.hp<enemyHp)damaged=true;if(match.a.attack!=null)moves.add(match.a.attack.id());}
  if(ticks%100==0){var os=(com.sun.management.OperatingSystemMXBean)java.lang.management.ManagementFactory.getOperatingSystemMXBean();System.out.println("SAMPLE tick="+ticks+" processCpu="+os.getProcessCpuLoad()+" heapMiB="+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576)+" state="+match.a.state+" hp="+match.a.hp+"/"+match.b.hp);}
  require(ticks<5000,"native fight timeout");
  if(match.isStopped()){
   require(match.isFinished(),"native fight cancelled");
   if(a.getImageSet().equals("ErenYeager")){require(titan&&damaged,"Titan failed to deal actual damage");require(a.getImage().size().height==(int)Math.round(128*Double.parseDouble(app.getProperties().getProperty("Scaling"))),"human reset size");}
   endRecord();System.out.println("PASS native "+Arrays.toString(pairs[pair])+" ticks="+ticks+" ErenTitan="+titan+" titanDamage="+damaged+" moves="+moves+" winner="+(match.winner()==match.a.body?a.getImageSet():b.getImageSet()));
   if(++pair<pairs.length)start();else{timer.stop();manager.disposeAll();AudioManager.global().close();System.out.println("PASS native suite complete");System.exit(0);}
  }
 }catch(Throwable e){fail(e);}}
 static void fail(Throwable e){e.printStackTrace();try{endRecord();}catch(Exception ignored){}if(manager!=null)manager.disposeAll();System.exit(1);}
 public static void main(String[]args){try{
  Files.createDirectories(out);app=Main.getInstance();app.run();var ready=new javax.swing.Timer(1400,e->{try{manager=app.getManager();manager.stop();RuntimeDiagnostics.close();manager.setExitOnLastRemoved(false);start();timer=new javax.swing.Timer(40,v->step());timer.start();}catch(Throwable ex){fail(ex);}});ready.setRepeats(false);ready.start();
 }catch(Throwable e){fail(e);}}
}
