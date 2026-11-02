import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.audio.AudioManager;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.combat.PedestrianPass;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Windows Main, actual actions/native renderer, then complete normal Manager ticks. */
public class LocomotionDesktopCheck {
 static Main app;static Manager manager;static JFrame background;static JLabel label;
 static final List<String> names=List.of("ErenYeager","Tom","Jerry");
 static final List<Mascot> pets=new ArrayList<>();static final Map<Mascot,Action> actions=new IdentityHashMap<>();
 static Path out=Path.of(System.getProperty("repair.output"));static String ffmpeg=System.getProperty("repair.ffmpeg");
 static Method tick;static Field time;static int phase,age;static javax.swing.Timer timer;static Process recorder;
 static boolean passed,turned;static double scale;static Rectangle area;
 static void require(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 static void record(String file)throws Exception {
  var display=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
  recorder=new ProcessBuilder(ffmpeg,"-hide_banner","-y","-f","gdigrab","-framerate","20","-offset_x","0","-offset_y","0","-video_size",display.getWidth()+"x"+display.getHeight(),"-i","desktop","-c:v","libx264","-preset","ultrafast","-crf","23","-pix_fmt","yuv420p",out.resolve(file+".mp4").toString()).redirectError(out.resolve(file+"-capture.log").toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
 }
 static void stopRecord()throws Exception {
  if(recorder!=null){recorder.getOutputStream().write('q');recorder.getOutputStream().flush();require(recorder.waitFor(8,java.util.concurrent.TimeUnit.SECONDS)&&recorder.exitValue()==0,"recording failed");recorder=null;}
 }
 static void place(Mascot m,int x,int y,boolean right)throws Exception {
  m.setPaused(false);m.setLookRight(right);m.setAnchor(new Point(x,y));m.getEnvironment().refreshWorkArea();
  m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));
 }
 static void walk(Mascot m,int dir)throws Exception {
  m.setLookRight(dir>0);var config=app.getConfiguration(m.getImageSet());
  int margin=(int)(75*scale),target=dir>0?(int)area.getMaxX()-margin:area.x+margin;
  m.setBehavior(new UserBehavior("WalkAlongWorkAreaFloor",config.buildAction("Walk",Map.of("TargetX",""+target,"Duration","10000")),config));
 }
 static void begin()throws Exception {
  age=0;actions.clear();
  for(Mascot m:pets){m.setPaused(true);RepairDesktopCheck.visible(m,false);}
  if(phase<7) {
   String action=phase<2?"ClimbWall":phase<4?"ClimbCeiling":phase==4?"Stand":phase==5?"Sit":"Walk";
   label.setText("Actual Windows sprites | "+action+" | scale="+scale+" | phase="+phase);
   for(int i=0;i<pets.size();i++) {
    Mascot m=pets.get(i);boolean right=phase==1||phase==3||phase==6;
    int x=area.x+area.width*(i+1)/4,y=(int)area.getMaxY();Map<String,String> params=new HashMap<>();
    if(phase<2){x=right?(int)area.getMaxX():area.x;y=area.y+area.height*(i+2)/5;params.put("TargetY",""+(y-100));}
    else if(phase<4){y=area.y;params.put("TargetX",""+(x+(right?150:-150)));}
    else if(phase==6)params.put("TargetX",""+(x+140));
    place(m,x,y,right);Action a=app.getConfiguration(m.getImageSet()).buildAction(action,params);a.init(m);actions.put(m,a);RepairDesktopCheck.visible(m,true);
   }
  }else {
   if(phase==7){stopRecord();record("pedestrian-passing");}
   label.setText("Actual Main / Manager | "+(phase==7?"stationary leader and followers":phase==8?"opposing walkers":"blocked edge")+" | scale="+scale);
   for(int i=0;i<pets.size();i++){
    Mascot m=pets.get(i);int x=area.x+(int)(area.width*(phase==7?(i==0?.68:i==1?.48:.26):phase==8?(i==0?.8:i==1?.3:.68):(i==0?.80:i==1?.95:.2)));
    place(m,x,(int)area.getMaxY(),true);RepairDesktopCheck.visible(m,!(phase==8&&i==0||phase==9&&i==2));
    if(phase==7&&i>0)walk(m,1);
    if(phase==8&&i>0)walk(m,i==1?1:-1);
    if(phase==9&&i==0)walk(m,1);
    if(phase==8&&i==0||phase==9&&i==2)m.setPaused(true);
   }
  }
  System.out.println("START phase="+phase+" "+label.getText());
 }
 static void step(){try {
  if(phase<7) {
   NativeFactory.getInstance().getEnvironment().tick();
   for(var entry:actions.entrySet()) {
    Mascot m=entry.getKey();if(entry.getValue().hasNext())entry.getValue().next();time.setInt(m,m.getTime()+1);m.apply();
    int size=(int)Math.round((m.getImageSet().equals("Jerry")?96:128)*scale);require(m.getImage().size().width==size,"render scale changed "+m.getImageSet());
    require(m.getBounds().intersects(area),"sprite completely outside screen");
   }
  }else {
   tick.invoke(manager);
   for(Mascot m:pets){passed|=m.getBehavior() instanceof PedestrianPass&&m.getAnchor().y<area.getMaxY()-60*scale;}
   if(phase==9)turned|=!pets.getFirst().isLookRight();
  }
  if(++age>=(phase<7?72:phase==9?150:240)) {
   System.out.println("PASS native phase="+phase+" size/facing/actions and responsive ticks="+age);
   if(++phase==10){require(passed,"no native overtake");require(turned,"no native edge turn");timer.stop();stopRecord();manager.disposeAll();AudioManager.global().close();background.dispose();System.out.println("PASS native locomotion suite complete; actual overtake and turn");System.exit(0);}begin();
  }
 }catch(Throwable ex){fail(ex);}}
 static void fail(Throwable ex){ex.printStackTrace();try{stopRecord();}catch(Exception ignored){}if(manager!=null)manager.disposeAll();System.exit(1);}
 public static void main(String[] args){try {
  Files.createDirectories(out);app=Main.getInstance();app.run();
  var ready=new javax.swing.Timer(1600,e->{try {
   manager=app.getManager();manager.stop();RuntimeDiagnostics.close();manager.setExitOnLastRemoved(false);
   var screen=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
   area=com.group_finity.mascot.environment.DesktopWorkArea.at(new Point(screen.x+screen.width/2,screen.y+screen.height/2));
   System.out.println("NATIVE_WORK_AREA "+area);
   scale=Double.parseDouble(app.getProperties().getProperty("Scaling"));
   for(String name:names)pets.add(manager.getMascots().stream().filter(m->m.getImageSet().equals(name)).findFirst().orElseThrow());
   tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
   background=new JFrame();background.setUndecorated(true);background.setAlwaysOnTop(true);background.getContentPane().setBackground(new Color(30,36,47));label=new JLabel("Native QA",SwingConstants.CENTER);label.setForeground(Color.WHITE);background.add(label,BorderLayout.CENTER);
   var display=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();background.setBounds(0,0,display.getWidth(),display.getHeight());background.setVisible(true);
   if(Boolean.getBoolean("repair.trafficOnly"))phase=7;
   else record("climbing-sitting");
   begin();timer=new javax.swing.Timer(40,v->step());timer.start();
  }catch(Throwable ex){fail(ex);}});ready.setRepeats(false);ready.start();
 }catch(Throwable ex){fail(ex);}}
}
