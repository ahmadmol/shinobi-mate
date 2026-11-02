import com.group_finity.mascot.*;
import com.group_finity.mascot.character.CharacterService;
import com.group_finity.mascot.audio.AudioManager;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import javax.swing.SwingUtilities;

/** Local QA attachment only; never part of the release. Uses application APIs. */
public class FinalQaAgent {
 public static void agentmain(String arg,Instrumentation unused)throws Exception {
  String[] parts=arg.split("\\|",2);Path out=Path.of(parts[0]);Main app=Main.getInstance();
  if(parts[1].equals("eight"))for(var p:new CharacterService().installed())if(new CharacterService().count(p.id())==0)new CharacterService().launch(p.id());
  long start=System.nanoTime();SwingUtilities.invokeAndWait(()->{});long edt=System.nanoTime()-start;
  String text="version="+BuildInfo.VERSION+"\ncount="+app.getManager().getCount()+"\nticks="+app.getManager().getLogicTicks()+"\nheapBytes="+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())+"\nthreads="+ManagementFactory.getThreadMXBean().getThreadCount()+"\nedtMillis="+edt/1e6+"\ntray="+java.awt.SystemTray.getSystemTray().getTrayIcons().length+"\naudio="+AudioManager.global().snapshot()+"\n";
  synchronized(app.getManager().getMascots()){for(var m:app.getManager().getMascots())text+=m.getImageSet()+" combat="+app.getManager().getCombat().active(m)+"\n";}
  Files.writeString(out,text);if(parts[1].equals("exit"))app.exit();
 }
}
