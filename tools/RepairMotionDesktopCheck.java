import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Exercise actual action implementations and native bitmap anchors on Windows. */
public class RepairMotionDesktopCheck {
 static Main app;static Manager manager;static Mascot pet;static Action action;static Field time;
 static int index,ticks;static javax.swing.Timer timer;
 static String[] names={"Stand","Sit","Walk","Run","Jumping","Falling","ClimbWall","ClimbCeiling","Falling"};
 static Path out=Path.of(System.getProperty("repair.output"));
 static void start()throws Exception {
  Rectangle r=RepairDesktopCheck.area();String n=names[index%names.length];boolean right=index>=names.length;
  pet.setLookRight(right);int x=r.x+r.width/2,y=r.y+r.height;
  Map<String,String> vars=new HashMap<>();
  if(n.equals("Jumping")){vars.put("TargetX",""+(x+(right?200:-200)));vars.put("TargetY",""+(y-180));}
  if(n.equals("ClimbWall")){x=right?(int)r.getMaxX():r.x;y=r.y+r.height/2;vars.put("TargetY",""+(r.y+160));}
  if(n.equals("ClimbCeiling")){y=r.y;vars.put("TargetX",""+(x+(right?200:-200)));}
  if(n.equals("Falling"))y=r.y+180;
  pet.setAnchor(new Point(x,y));pet.getEnvironment().refreshWorkArea();
  action=app.getConfiguration("ErenYeager").buildAction(n,vars);action.init(pet);ticks=0;
 }
 static void step(){try {
  NativeFactory.getInstance().getEnvironment().tick();
  if(action.hasNext())action.next();time.setInt(pet,pet.getTime()+1);pet.apply();ticks++;
  if(pet.getImage().size().width!=(int)Math.round(128*Double.parseDouble(app.getProperties().getProperty("Scaling"))))throw new AssertionError("Render scale changed");
  if(ticks==12){var bounds=pet.getBounds().intersection(RepairDesktopCheck.area());if(bounds.isEmpty())throw new AssertionError("sprite completely offscreen");
   var device=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();var logical=device.getDefaultConfiguration().getBounds();var dm=device.getDisplayMode();double sx=(double)dm.getWidth()/logical.width,sy=(double)dm.getHeight()/logical.height;
   String output=out.resolve(names[index%names.length]+"-"+(index>=names.length?"right":"left")+".png").toString();
   int px=(int)Math.floor(bounds.x*sx),py=(int)Math.floor(bounds.y*sy);
   int pw=Math.min(dm.getWidth()-px,(int)Math.ceil(bounds.getMaxX()*sx)-px),ph=Math.min(dm.getHeight()-py,(int)Math.ceil(bounds.getMaxY()*sy)-py);
   var capture=new ProcessBuilder(System.getProperty("repair.ffmpeg"),"-hide_banner","-loglevel","error","-y","-f","gdigrab","-offset_x",""+px,"-offset_y",""+py,"-video_size",pw+"x"+ph,"-i","desktop","-frames:v","1","-update","1",output).redirectError(ProcessBuilder.Redirect.INHERIT).start();
   if(!capture.waitFor(4,java.util.concurrent.TimeUnit.SECONDS)||capture.exitValue()!=0)throw new AssertionError("native capture failed");}
  if(ticks>=45){System.out.println("PASS native action="+names[index%names.length]+" facing="+(index>=names.length?"right":"left")+" anchor="+pet.getAnchor()+" bitmap="+pet.getImage().size());if(++index==names.length*2){timer.stop();manager.disposeAll();System.out.println("PASS 18 native motion scenarios (wall/ceiling contact art remains incomplete)");System.exit(0);}start();}
 }catch(Throwable e){e.printStackTrace();manager.disposeAll();System.exit(1);}}
 public static void main(String[]args){try {
  Files.createDirectories(out);app=Main.getInstance();app.run();var ready=new javax.swing.Timer(1400,e->{try{manager=app.getManager();manager.stop();pet=manager.getMascots().stream().filter(m->m.getImageSet().equals("ErenYeager")).findFirst().orElseThrow();time=Mascot.class.getDeclaredField("time");time.setAccessible(true);start();timer=new javax.swing.Timer(40,v->step());timer.start();}catch(Throwable ex){ex.printStackTrace();System.exit(1);}});ready.setRepeats(false);ready.start();
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
