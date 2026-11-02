import com.group_finity.mascot.*;
import java.awt.Window;
import java.net.Socket;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

/** Real Main.main and authenticated STOP while startup owns the modal onboarding loop. */
public class FirstRunStopCheck {
 public static void main(String[] args)throws Exception {
  Path profile=AppPaths.userData();Files.createDirectories(profile);
  AtomicBoolean observed=new AtomicBoolean(),sent=new AtomicBoolean();
  Runtime.getRuntime().addShutdownHook(new Thread(()->{
   try{if(observed.get()&&sent.get()&&!Files.readString(profile.resolve("settings.properties")).contains("OnboardingComplete=true"))
    Files.writeString(profile.resolve("first-run-stop-result.txt"),"PASS real visible first-run guide; invalid token rejected; authenticated STOP exits without completing onboarding or spawning characters\n");
   }catch(Exception e){e.printStackTrace();}
  }));
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL first-run STOP timeout");System.exit(2);}},20000);
  Timer timer=new Timer(40,e->{
   for(Window w:Window.getWindows())if(w.isShowing()&&"ShinobiMateOnboarding".equals(w.getName())&&observed.compareAndSet(false,true)) {
    if(Main.getInstance().getManager().getCount()!=0)throw new AssertionError("automatic spawn");
    ((Timer)e.getSource()).stop();
    new Thread(()->{try{
     String[] endpoint=Files.readString(profile.resolve("instance.activation")).split("\n");
     if(send(endpoint,"0".repeat(36))==1)throw new AssertionError("invalid STOP accepted");
     sent.set(true);if(send(endpoint,endpoint[1])!=1)throw new AssertionError("STOP not acknowledged");
    }catch(Throwable ex){ex.printStackTrace();System.exit(1);}},"first-run-stop-client").start();
   }
  });timer.start();Main.main(new String[0]);
 }
 static int send(String[] endpoint,String token)throws Exception {
  try(Socket client=new Socket("127.0.0.1",Integer.parseInt(endpoint[0]))){client.setSoTimeout(3000);
   client.getOutputStream().write((token+" STOP").getBytes(StandardCharsets.UTF_8));client.getOutputStream().flush();return client.getInputStream().read();}
 }
}
