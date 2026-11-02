import com.group_finity.mascot.*;
import com.group_finity.mascot.character.CharacterService;
import javax.swing.*;
import java.nio.file.*;
import java.lang.instrument.Instrumentation;
/** Test-only attachment. Never shipped. Calls the same application Exit used by the tray. */
public class LifecycleAgent {
 public static void agentmain(String arg, Instrumentation instrumentation)throws Exception {
  String[] parts=arg.split("\\|",2);Path log=Path.of(parts[0]);Main app=Main.getInstance();
  try {
  if(parts[1].equals("five")){
   var service=new CharacterService();
   for(String id:new String[]{"NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover"})
    if(app.getManager().getMascots().stream().noneMatch(m->m.getImageSet().equals(id)))service.launch(id);
   Thread.sleep(1500);
   SwingUtilities.invokeAndWait(()->{for(var w:java.awt.Window.getWindows())if("ShinobiMateCharacters".equals(w.getName()))w.setVisible(false);});
  }
  if(parts[1].equals("combat")){
   app.getManager().stop();
   synchronized(app.getManager().getMascots()){
    var list=app.getManager().getMascots();
    var area=java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    list.get(0).setAnchor(new java.awt.Point(area.x+150,area.y+area.height-100));
    list.get(1).setAnchor(new java.awt.Point(area.x+450,area.y+area.height-100));
    if(!app.getManager().getCombat().start(list.get(0),list.get(1)))throw new AssertionError("combat failed");
   }
   com.group_finity.mascot.focus.FocusService.start();
   app.getManager().start();
  }
  Files.writeString(log,"version="+BuildInfo.VERSION+"\npid="+ProcessHandle.current().pid()+"\ncount="+app.getManager().getCount()+"\ntray="+java.awt.SystemTray.getSystemTray().getTrayIcons().length+"\nprofile="+AppPaths.userData()+"\napplication="+AppPaths.application()+"\ncombat="+app.getManager().getCombat().active(app.getManager().getMascots().get(0))+"\nfocus="+com.group_finity.mascot.focus.FocusService.session().phase()+"\n");
  if(parts[1].equals("exit"))SwingUtilities.invokeLater(app::exit);
  }catch(Throwable failure){
   try(var writer=new java.io.PrintWriter(Files.newBufferedWriter(log))){failure.printStackTrace(writer);}
   throw new RuntimeException(failure);
  }
 }
}
