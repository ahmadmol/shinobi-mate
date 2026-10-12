import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.ui.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;
import java.io.*;
/** Installer integration fixture. This agent is never included in the shipped application. */
public class InstalledBetaAgent {
 public static void agentmain(String args, Instrumentation instrumentation)throws Exception {
  String[] parts=args.split("\\|",2);Path result=Path.of(parts[0]);String mode=parts[1];Main app=Main.getInstance();
  Files.writeString(result,"APP="+AppPaths.application()+"\nPROFILE="+AppPaths.userData()+"\nRUNTIME="+System.getProperty("java.home")+"\nLANGUAGE="+app.getProperties().getProperty("Language")+"\nCOUNT="+app.getManager().getCount()+"\n");
  if(mode.equals("manager"))SwingUtilities.invokeLater(CharacterManagerWindow::open);
  if(mode.equals("feedback")||mode.equals("hide-feedback"))SwingUtilities.invokeAndWait(()->{
   var window=java.util.Arrays.stream(java.awt.Window.getWindows()).filter(w->"ShinobiMateCharacters".equals(w.getName())).findFirst().orElseThrow();
   if(!window.isShowing())throw new AssertionError("installed Character Manager is hidden");
   if(java.util.Arrays.stream(java.awt.Window.getWindows()).anyMatch(w->w.isShowing()&&"ShinobiMateOnboarding".equals(w.getName())))throw new AssertionError("installed onboarding repeated");
   if(app.characterMenu(()->{}).getItemCount()!=5)throw new AssertionError("installed summon menu is incomplete");
   try{Files.writeString(result,"PASS installed manager visible, onboarding absent, five summon entries\n",StandardOpenOption.APPEND);}catch(IOException ex){throw new java.io.UncheckedIOException(ex);}
   if(mode.equals("hide-feedback"))window.setVisible(false);
  });
  if(mode.equals("exit"))SwingUtilities.invokeLater(app::exit);
  if(mode.equals("features"))new Thread(()->{
   try(PrintStream out=new PrintStream(Files.newOutputStream(result,StandardOpenOption.APPEND),true,java.nio.charset.StandardCharsets.UTF_8)){
    System.setOut(out);System.setErr(out);var service=new CharacterService();
    String[] packs={"NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover"};
    for(String id:packs)service.remove(id);Thread.sleep(250);
    for(String id:packs)if(!service.launch(id))throw new AssertionError("launch failed: "+id);
    Thread.sleep(900);System.setProperty("beta.attach","true");
    CombatDesktopCheck.main(new String[0]);System.clearProperty("beta.attach");out.println("PASS INSTALLED FEATURES");
   }catch(Throwable error){try{Files.writeString(result,"\nFAIL "+error+"\n",StandardOpenOption.APPEND);}catch(Exception ignored){}}
  },"InstalledBetaIntegration").start();
 }
}
