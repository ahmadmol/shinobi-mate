import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.Manager;
import com.group_finity.mascot.image.TranslucentWindow;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.file.*;

/** Full installed Main startup and native Windows rendering, using isolated settings. */
public class CharacterNativeSmoke {
 static Path result;
 static void log(String message)throws Exception {
  System.out.println(message);
  Files.writeString(result,message+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
 }
 public static void main(String[]args) {
  result=Path.of("..","native-"+(args.length>0&&args[0].contains("/")?"three":"asta")+"-java"+Runtime.version().feature()+".log");
  new Timer(true).schedule(new TimerTask(){public void run(){try{log("FAIL native startup timeout");}catch(Exception ignored){}System.exit(2);}},30000);
  try {
   log("START native test on Java "+Runtime.version());
   Main app=Main.getInstance();app.run();
   Method managerMethod=Main.class.getDeclaredMethod("getManager");managerMethod.setAccessible(true);
   Manager manager=(Manager)managerMethod.invoke(app);
   List<String> expected=List.of(args[0].split("/"));
   long until=System.currentTimeMillis()+10000;
   while(System.currentTimeMillis()<until && (manager.getCount()!=expected.size() || manager.getMascots().stream().anyMatch(m->m.getImage()==null)))Thread.sleep(50);
   if(manager.getCount()!=expected.size())throw new AssertionError("wrong native mascot count: "+manager.getCount());
   Method window=Mascot.class.getDeclaredMethod("getWindow");window.setAccessible(true);
   for(Mascot m:manager.getMascots()) {
    if(!expected.contains(m.getImageSet()))throw new AssertionError("unexpected character");
    if(m.getImage()==null)throw new AssertionError("missing rendered image");
    TranslucentWindow w=(TranslucentWindow)window.invoke(m);
    if(!w.getClass().getName().contains("win.WindowsTranslucentWindow"))throw new AssertionError("not a native Windows window");
    if(!w.asComponent().isDisplayable() || !w.asComponent().isVisible())throw new AssertionError("native window not visible");
    log("PASS native "+m.getImageSet()+" window="+w.getClass().getName()+" image="+m.getImage().image().getClass().getName());
   }
   Thread.sleep(3000);
   if(manager.getCount()!=expected.size())throw new AssertionError("character disappeared during native startup");
   manager.stop();manager.disposeAll();
   log("PASS full installed Main.run startup: "+expected);System.exit(0);
  }catch(Throwable e){e.printStackTrace();try{log("FAIL "+e);}catch(Exception ignored){}System.exit(1);}
 }
}
