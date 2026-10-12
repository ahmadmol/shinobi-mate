import com.group_finity.mascot.*;
import com.group_finity.mascot.focus.*;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.image.*;
import com.group_finity.mascot.character.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
public class FocusCheck extends CharacterCheck {
 public static void main(String[] args){
  try{testFocus();System.out.println("PASS offline focus transitions/pause, Unicode CRUD, quiet behavior, idle level, friendly social, UI; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 @SuppressWarnings("unchecked")
 static void testFocus()throws Exception {
  FocusSession f=new FocusSession();f.start(0,25000,5000);require(f.quiet(),"focus not quiet");require(f.tick(24999)==FocusSession.Event.NONE,"early focus end");
  f.pause(10000);require(f.remaining(50000)==15000,"pause countdown");f.resume(50000);require(f.tick(65000)==FocusSession.Event.FOCUS_COMPLETED,"focus transition");
  require(f.phase()==FocusSession.Phase.BREAK&&!f.quiet(),"break quiet flag");require(f.tick(70000)==FocusSession.Event.BREAK_COMPLETED,"break transition");
  require(f.tick(1000000)==FocusSession.Event.FOCUS_COMPLETED&&f.tick(1000000)==FocusSession.Event.NONE,"overnight catch-up events");f.stop();require(f.phase()==FocusSession.Phase.IDLE,"stop");
  System.setProperty("app.dir",Path.of("output/product-ui").toAbsolutePath().toString());
  Path custom=CustomMessages.path();var entries=java.util.List.of(new CustomMessages.Entry(1,"مرحبا 🌙 Hello 日本語",true),new CustomMessages.Entry(2,"A quiet thought",false));
  CustomMessages.save(entries);require(CustomMessages.load().equals(entries),"Unicode custom messages lost");
  CustomMessages.save(java.util.List.of(new CustomMessages.Entry(1,"Edited ✓",true)));require(CustomMessages.load().size()==1,"delete/edit failed");
  var packBefore=Files.readAllBytes(Path.of("img/NarutoShippuden/conf/cloud_messages_ar.properties"));
  require(CloudMessages.load("NarutoShippuden").contains("Edited ✓"),"custom layer not merged");
  require(Arrays.equals(packBefore,Files.readAllBytes(Path.of("img/NarutoShippuden/conf/cloud_messages_ar.properties"))),"pack messages overwritten");
  var main=Main.getInstance();main.getProperties().setProperty("CustomCloudEnabled","false");require(!CloudMessages.load("NarutoShippuden").contains("Edited ✓"),"custom layer disable");
  main.getProperties().setProperty("CustomCloudEnabled","true");
  factory=new Factory();Field nativeField=NativeFactory.class.getDeclaredField("instance");nativeField.setAccessible(true);nativeField.set(null,factory);
  Field language=Main.class.getDeclaredField("languageBundle");language.setAccessible(true);language.set(main,ResourceBundle.getBundle("language",Locale.forLanguageTag("ar"),new com.joconner.i18n.ConfDirectoryUtf8Control(new java.io.File("conf"))));
  Field configurations=Main.class.getDeclaredField("configurations");configurations.setAccessible(true);var configs=(Map<String,com.group_finity.mascot.config.Configuration>)configurations.get(main);
  Manager manager=main.getManager();manager.setExitOnLastRemoved(false);
  Counter counter=new Counter();
  for(String name:java.util.List.of("NarutoShippuden","SasukeShippuden")){
   configs.put(name,config(name,"behaviors.xml"));Mascot m=new Mascot(name);m.setAnchor(new Point(name.startsWith("Naruto")?200:320,500));m.setManager(manager);manager.getMascots().add(m);m.setBehavior(counter);
   ImagePairLoader.load("/"+name+"/Stand.png",null,new Point(64,128),1,ImagePairLoader.Filter.BICUBIC,1);m.setImage(ImagePairs.getImage("/"+name+"/Stand.png",false));
  }
  Method tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
  FocusService.session().start(FocusService.now(),25000,5000);for(int i=0;i<40;i++)tick.invoke(manager);
  require(counter.ticks<=24,"focus did not calm resting behaviors");int calmTicks=counter.ticks;FocusService.session().stop();for(int i=0;i<5;i++)tick.invoke(manager);require(counter.ticks>calmTicks,"focus stop did not restore behavior");
  Mascot first=manager.getMascots().getFirst();var features=first.getCompanionFeatures();
  Field last=features.mood().getClass().getDeclaredField("lastInteraction");last.setAccessible(true);last.setLong(features.mood(),CompanionFeatures.now()-120000);
  Field next=CompanionFeatures.class.getDeclaredField("nextIdle");next.setAccessible(true);next.setLong(features,0);tick.invoke(manager);require(features.holdBehavior(),"declared idle activity not held");
  main.getProperties().setProperty("IdleActivityLevel","0");require(!features.holdBehavior(),"idle level zero did not disable activity");
  Field social=Manager.class.getDeclaredField("social");social.setAccessible(true);Object socialService=social.get(manager);
  Field due=socialService.getClass().getDeclaredField("next");due.setAccessible(true);due.setLong(socialService,0);tick.invoke(manager);
  require(manager.getMascots().stream().allMatch(m->m.getCompanionFeatures().holdBehavior()),"friendly gesture not requested for both");
  require(manager.getMascots().stream().noneMatch(m->manager.getCombat().active(m)),"social started combat");
  SwingUtilities.invokeAndWait(()->{
   try{com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
    for(String lang:java.util.List.of("ar","en-GB")){
     main.getProperties().setProperty("Language",lang);FocusDialog d=new FocusDialog(null);d.setVisible(true);snapshot(d,"focus-"+lang);
     button(d,"FocusStart").doClick();require(FocusService.session().phase()==FocusSession.Phase.FOCUS,"focus UI start");
     button(d,"FocusPause").doClick();require(FocusService.session().phase()==FocusSession.Phase.PAUSED,"focus UI pause");
     button(d,"FocusStop").doClick();require(FocusService.session().phase()==FocusSession.Phase.IDLE,"focus UI stop");d.dispose();
    }
    CloudEditorDialog editor=new CloudEditorDialog(null);editor.setVisible(true);snapshot(editor,"custom-messages");editor.dispose();
   }catch(Exception ex){throw new RuntimeException(ex);}
  });
  manager.disposeAll();Files.deleteIfExists(custom);
 }
}
