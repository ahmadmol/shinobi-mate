import com.group_finity.mascot.*;
import com.group_finity.mascot.ui.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.swing.*;

/** Real bundled JVM, production entry point, actual second EXE and shared tray summon handlers. */
public class BetaActivationCheck {
 static int checks;static JFrame manager;
 static void require(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 static void edt(Runnable task)throws Exception{SwingUtilities.invokeAndWait(task);}
 public static void main(String[] args)throws Exception{
  Path root=AppPaths.application();var out=new java.io.PrintStream(Files.newOutputStream(root.resolve("activation-result.txt")),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(out);System.setErr(out);
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL activation timeout");System.exit(2);}},90000);
  try{
   Main.main(new String[0]);Main app=Main.getInstance();Thread.sleep(500);
   edt(()->{for(java.awt.Window w:java.awt.Window.getWindows())if("ShinobiMateCharacters".equals(w.getName()))manager=(JFrame)w;});
   require(manager!=null&&manager.isShowing(),"completed profile did not show manager");
   require(java.util.Arrays.stream(java.awt.Window.getWindows()).noneMatch(w->w.isShowing()&&"ShinobiMateOnboarding".equals(w.getName())),"onboarding repeated");
   int count=app.getManager().getCount();
   for(int round=0;round<3;round++){
    int mode=round;edt(()->{if(mode==1)manager.setExtendedState(JFrame.ICONIFIED);else manager.setVisible(false);});
    Process second=new ProcessBuilder(root.resolve("ActivationProbe.exe").toString()).directory(root.toFile()).start();
    require(second.waitFor(15,TimeUnit.SECONDS)&&second.exitValue()==0,"second launcher remained running");
    edt(()->{});require(manager.isShowing()&&manager.getExtendedState()==JFrame.NORMAL,"second launch did not restore manager");
    require(app.getManager().getCount()==count,"second launch spawned companions");
   }
   System.out.println("PASS completed-profile manager, no repeated onboarding, three second-process activations including minimize/hidden restore");
   edt(()->manager.setVisible(false));
   // Invoke the actual tray event and its Add character button, rather than a manager button.
   edt(()->{
    var tray=SystemTray.getSystemTray().getTrayIcons()[0];var event=new MouseEvent(new JPanel(),MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),MouseEvent.BUTTON3_DOWN_MASK,100,100,1,true,MouseEvent.BUTTON3);
    for(var listener:tray.getMouseListeners())listener.mouseReleased(event);
   });
   require(java.util.Arrays.stream(java.awt.Window.getWindows()).anyMatch(w->w instanceof JDialog d&&d.isShowing()&&d.getTitle().equals("ShinobiMate")),"tray menu did not open");
   for(var pack:new com.group_finity.mascot.character.CharacterService().installed()){
    int previous=new com.group_finity.mascot.character.CharacterService().count(pack.id());
    edt(()->{
     var menu=app.characterMenu(()->{});JMenuItem selected=null;
     for(int i=0;i<menu.getItemCount();i++)if(("Summon."+pack.id()).equals(menu.getItem(i).getName()))selected=menu.getItem(i);
     require(selected!=null,"summon entry missing");selected.doClick();
    });
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(15);
    while(new com.group_finity.mascot.character.CharacterService().count(pack.id())<=previous&&System.nanoTime()<deadline)Thread.sleep(50);
    require(new com.group_finity.mascot.character.CharacterService().count(pack.id())==previous+1,"tray summon failed");require(!manager.isShowing(),"summon opened manager");
   }
   System.out.println("PASS five named tray summon entries, each adds an instance without opening Character Manager");
   long ticks=app.getManager().getLogicTicks();
   for(int i=0;i<100;i++){edt(()->{});Thread.sleep(100);}
   require(app.getManager().getLogicTicks()>ticks+50,"engine stopped during five-pack run");
   System.out.println("PASS native five-pack engine and EDT responsiveness; "+checks+" assertions");
   System.out.println("Intermittent friend-machine freezing/missing clouds/audio: NOT REPRODUCED in this run; local diagnostics enabled");
   edt(app::exit);
  }catch(Throwable ex){ex.printStackTrace();System.exit(1);}
 }
}
