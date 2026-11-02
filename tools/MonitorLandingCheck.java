import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.environment.*;
import java.awt.*;import java.lang.reflect.*;import java.util.*;import java.util.List;

/** Real native renderer/actions with isolated profile; falling is simulated one engine step at a time. */
public class MonitorLandingCheck {
 public static void agentmain(String output,java.lang.instrument.Instrumentation ignored)throws Exception {
  var log=new java.io.PrintStream(new java.io.FileOutputStream(output),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(log);System.setErr(log);main(new String[]{"attached"});
 }
 public static void main(String[]args)throws Exception {
  try {
   var app=Main.getInstance();if(args.length==0)app.run();var manager=app.getManager();manager.stop();RuntimeDiagnostics.close();
   manager.getMascots().addAll(manager.getAdded());manager.getAdded().clear();
   var time=Mascot.class.getDeclaredField("time");time.setAccessible(true);int checks=0;
   var nativeEnv=NativeFactory.getInstance().getEnvironment();
   var monitors=GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
   if(monitors.length<2)throw new AssertionError("two connected monitors required");
   for(var pet:List.copyOf(manager.getMascots()))for(var monitor:monitors){
    Rectangle screen=monitor.getDefaultConfiguration().getBounds();Rectangle area=DesktopWorkArea.at(new Point(screen.x+screen.width/2,screen.y+screen.height/2));
    pet.setAnchor(new Point(area.x+area.width/2,area.y+120));pet.getEnvironment().refreshWorkArea();
    Action fall=app.getConfiguration(pet.getImageSet()).buildAction("Fall",Map.of());fall.init(pet);
    int steps=0;while(fall.hasNext()&&steps++<160){nativeEnv.tick();fall.next();time.setInt(pet,pet.getTime()+1);pet.apply();}
    if(!pet.getEnvironment().getFloor(true).isOn(pet.getAnchor())||pet.getAnchor().y!=area.y+area.height)throw new AssertionError("did not land "+pet.getImageSet()+" area="+area+" anchor="+pet.getAnchor()+" steps="+steps);
    for(int n=0;n<50;n++){nativeEnv.tick();if(!pet.getEnvironment().getFloor(true).isOn(pet.getAnchor()))throw new AssertionError("unstable floor");}checks++;
    for(boolean right:List.of(false,true)){
     Rectangle outer=DesktopWorkArea.at(new Point(right?1_000_000:-1_000_000,400));pet.setLookRight(right);pet.setAnchor(new Point(right?outer.x+outer.width:outer.x,outer.y+outer.height/2));pet.getEnvironment().refreshWorkArea();
     Action climb=app.getConfiguration(pet.getImageSet()).buildAction("ClimbWall",Map.of("TargetY",""+(outer.y+60)));climb.init(pet);
     int start=pet.getAnchor().y;for(int i=0;i<24&&climb.hasNext();i++){nativeEnv.tick();climb.next();time.setInt(pet,pet.getTime()+1);pet.apply();}
     if(pet.getAnchor().y>=start)throw new AssertionError("wall did not climb "+pet.getImageSet());checks++;
    }
    System.out.println("PASS "+pet.getImageSet()+" falling and stable landing "+area+"; both outer walls climbed");
   }
   if(checks!=48)throw new AssertionError("expected eight packs/two monitors; checks="+checks);
   manager.disposeAll();System.out.println("PASS "+checks+" native landing/wall cases; repeated transfers between monitors; no OS mouse drag synthesized");System.exit(0);
  }catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
