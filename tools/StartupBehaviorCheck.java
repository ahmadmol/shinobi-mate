import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.ui.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;

/** Each mode uses a fresh JVM, real Main.main(), native sprites and a shared isolated profile. */
public class StartupBehaviorCheck {
 static int checks;static JFrame window;static Main app;static CharacterService service=new CharacterService();
 static void require(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void edt(Runnable task)throws Exception{SwingUtilities.invokeAndWait(task);}
 static Component named(Container root,String name){for(Component child:root.getComponents()){if(name.equals(child.getName()))return child;if(child instanceof Container c){Component result=named(c,name);if(result!=null)return result;}}return null;}
 static void waitFor(java.util.function.BooleanSupplier condition,String reason)throws Exception {
  long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);while(!condition.getAsBoolean()&&System.nanoTime()<until)Thread.sleep(25);require(condition.getAsBoolean(),reason);
 }
 static int total(){return CharacterSession.snapshot(app.getManager()).size();}
 static void refresh()throws Exception{edt(()->((CharacterManagerWindow)window).reload());}
 static void clickLaunch(String id)throws Exception {
  int before=service.count(id);edt(()->((JButton)named(window,"Launch."+id)).doClick());waitFor(()->service.count(id)==before+1,"manual launch "+id);Thread.sleep(100);refresh();
 }
 static void cards(String language)throws Exception {
  edt(()->{for(var p:service.installed()){var label=(JLabel)named(window,"Status."+p.id());require(label!=null,"card missing "+p.id());require(label.getText().contains("Active: 0")||label.getText().contains("نشط: 0"),"card isn't inactive "+p.id());}
   require(window.getComponentOrientation().isLeftToRight()==!language.equals("ar"),"manager language orientation");});
 }
 static void setting(boolean enabled)throws Exception {
  edt(()->{var d=new SettingsDialog(window);JCheckBox toggle=(JCheckBox)named(d,CharacterSession.RESTORE);require(toggle!=null,"restore toggle absent");if(toggle.isSelected()!=enabled)toggle.doClick();require(toggle.isSelected()==enabled,"restore toggle failed");d.dispose();});
  require(Boolean.parseBoolean(SettingsStore.load().getProperty(CharacterSession.RESTORE))==enabled,"restore option not persisted");
 }
 static void manualPair()throws Exception {
  clickLaunch("NarutoShippuden");require(total()==1&&service.count("NarutoShippuden")==1,"Naruto selection spawned other packs");
  edt(()->{require(((JButton)named(window,"Launch.NarutoShippuden")).getText().contains("instance")||((JButton)named(window,"Launch.NarutoShippuden")).getText().contains("نسخة"),"add-instance label missing");});
  clickLaunch("SasukeShippuden");require(total()==2&&service.count("SasukeShippuden")==1,"Sasuke selection spawned other packs");
  for(String other:List.of("GojoSatoru","SukunaRyomen","AstaBlackClover"))require(service.count(other)==0,"unselected pack active "+other);
 }
 static void activation()throws Exception {
  int before=total();edt(()->window.setVisible(false));
  var command=new ArrayList<String>();String nativeLauncher=System.getProperty("startup.activationLauncher");
  if(nativeLauncher!=null)command.add(nativeLauncher);
  else {command.add(Path.of(System.getProperty("java.home"),"bin/java.exe").toString());
   command.add("--enable-native-access=ALL-UNNAMED");command.add("-Dapp.dir="+AppPaths.application());command.add("-Dapp.userDataDir="+AppPaths.userData());command.add("-cp");command.add(System.getProperty("java.class.path"));command.add("com.group_finity.mascot.Main");}
  Process second=new ProcessBuilder(command).directory(AppPaths.application().toFile()).redirectErrorStream(true).start();
  require(second.waitFor(12,TimeUnit.SECONDS)&&second.exitValue()==0,"second instance did not activate/exit");
  waitFor(()->window.isShowing(),"second instance didn't reopen manager");require(total()==before,"activation spawned mascots");
 }
 static void trayAndDuplicates()throws Exception {
  edt(()->window.setVisible(false));int before=service.count("NarutoShippuden");
  edt(()->{JMenu menu=app.characterMenu(()->{});for(int i=0;i<menu.getItemCount();i++)if("Summon.NarutoShippuden".equals(menu.getItem(i).getName())){menu.getItem(i).doClick();return;}throw new AssertionError("tray entry missing");});
  waitFor(()->service.count("NarutoShippuden")==before+1,"tray add-instance failed");Thread.sleep(150);require(!window.isShowing(),"tray selection reopened manager");require(total()==3,"duplicate wasn't one extra instance");
  app.getManager().remainOne("NarutoShippuden");waitFor(()->service.count("NarutoShippuden")==1,"duplicate removal failed");
  app.getManager().stop();Mascot a=null,b=null;for(var m:app.getManager().getMascots())if(m.getManager()==app.getManager()){if(m.getImageSet().equals("NarutoShippuden"))a=m;if(m.getImageSet().equals("SasukeShippuden"))b=m;}
  require(a!=null&&b!=null,"live pair not applied to manager");
  var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();Rectangle bounds=new Rectangle(gc.getBounds());Insets in=Toolkit.getDefaultToolkit().getScreenInsets(gc);int floor=bounds.y+bounds.height-in.bottom;
  a.setAnchor(new Point(bounds.x+300,floor));b.setAnchor(new Point(bounds.x+500,floor));
  a.setBehavior(app.getConfiguration(a.getImageSet()).buildBehavior("StandUp"));b.setBehavior(app.getConfiguration(b.getImageSet()).buildBehavior("StandUp"));
  require(app.getManager().getCombat().start(a,b),"manual pair combat regressed");app.getManager().start();Thread.sleep(120);app.getManager().getCombat().stop(a);require(!app.getManager().getCombat().active(b),"combat cleanup regressed");
  edt(()->app.requestManager());waitFor(()->window.isShowing(),"tray/activation manager reopening");
 }
 static void genericPolicy() {
  Properties p=SettingsStore.defaults();p.setProperty("ActiveShimeji","PackOne/PackTwo");p.setProperty(CharacterSession.HISTORY,"PackOne/PackOne/PackTwo");
  require(CharacterSession.restore(p,false).isEmpty(),"legacy loaded-pack list restored by default");p.setProperty(CharacterSession.RESTORE,"true");
  require(CharacterSession.restore(p,false).equals(List.of("PackOne","PackOne","PackTwo")),"generic restore lost duplicate instances");require(CharacterSession.restore(p,true).isEmpty(),"first run restored characters");
  p.setProperty("MaxCharacters","2");require(CharacterSession.restore(p,false).size()==2,"restore bypassed cap");p.setProperty(CharacterSession.RESTORE,"invalid");SettingsStore.sanitize(p);require(CharacterSession.restore(p,false).isEmpty(),"malformed opt-in enabled restore");
 }
 static void legacy()throws Exception {
  require(total()==0,"legacy ActiveShimeji caused startup spawn");
  require(app.prepareCharacter("NarutoShippuden"),"prepare resources failed");require(total()==0&&app.getProperties().getProperty("ActiveShimeji","").isEmpty(),"resource loading became active");
  edt(()->{var chooser=new com.group_finity.mascot.imagesetchooser.ImageSetChooser(null,false);try {
   for(String field:List.of("jList1","jList2")){var f=chooser.getClass().getDeclaredField(field);f.setAccessible(true);require(((JList<?>)f.get(chooser)).getSelectedIndices().length==0,"legacy chooser selected every installed pack");}
   chooser.dispose();
  }catch(Exception ex){throw new RuntimeException(ex);}});
  var language=Main.class.getDeclaredMethod("updateLanguage",String.class);language.setAccessible(true);edt(()->{try{language.invoke(app,"en-GB");}catch(Exception ex){throw new RuntimeException(ex);}});
  require(total()==0,"language refresh spawned loaded resources");
  var selection=Main.class.getDeclaredMethod("setActiveImageSets",ArrayList.class);selection.setAccessible(true);selection.invoke(app,new ArrayList<>(List.of("NarutoShippuden")));waitFor(()->total()==1,"legacy explicit selection failed after preload");
  service.remove("NarutoShippuden");require(total()==0,"legacy removal missed active instance");app.createMascot();Thread.sleep(150);require(total()==0,"no-argument random fallback spawned a pack");
 }
 public static void main(String[] args)throws Exception {
  String mode=args[0],language=args.length>1?args[1]:"en-GB";var seen=new AtomicBoolean();var uiFailure=new AtomicReference<Throwable>();
  Path log=AppPaths.userData().resolve(mode+"-result.log");Files.createDirectories(log.getParent());var stream=new java.io.PrintStream(Files.newOutputStream(log),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(stream);System.setErr(stream);
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL startup test timeout");System.exit(2);}},60000);
  var guide=new javax.swing.Timer(40,e->{for(Window w:Window.getWindows())if(w.isShowing()&&"ShinobiMateOnboarding".equals(w.getName()))try {
   require(mode.equals("first"),"onboarding repeated");require(CharacterSession.snapshot(Main.getInstance().getManager()).isEmpty(),"mascot active during onboarding");seen.set(true);
   JComboBox<?> choice=(JComboBox<?>)named((Container)w,"OnboardingLanguage");choice.setSelectedIndex(language.equals("ar")?0:1);((JButton)named((Container)w,"OnboardingNext")).doClick();
  }catch(Throwable ex){uiFailure.set(ex);w.dispose();}});guide.start();
  try {
   genericPolicy();Main.main(mode.equals("legacy")?new String[]{"--background"}:new String[0]);guide.stop();app=Main.getInstance();
   if(uiFailure.get()!=null)throw new AssertionError(uiFailure.get());Thread.sleep(300);
   edt(()->{for(Window w:Window.getWindows())if("ShinobiMateCharacters".equals(w.getName()))window=(JFrame)w;});require(window!=null&&window.isShowing(),"manager wasn't opened on launch");
   require(seen.get()==mode.equals("first"),"wrong first/subsequent onboarding behavior");require(service.installed().size()>=5,"installed packs missing");
   if(mode.equals("on")||mode.equals("duplicates")){int copies=mode.equals("duplicates")?2:1;require(total()==copies+1&&service.count("NarutoShippuden")==copies&&service.count("SasukeShippuden")==1,"restore wasn't the saved live session");activation();setting(false);}
   else {
    require(total()==0,"automatic characters with restore off / first run");cards(language);
    if(mode.equals("first")){require(!Boolean.parseBoolean(app.getProperties().getProperty(CharacterSession.RESTORE)),"restore default on");activation();manualPair();trayAndDuplicates();}
    if(mode.equals("off")){activation();setting(true);require(total()==0,"enabling restore spawned immediately");manualPair();}
    if(mode.equals("legacy"))legacy();
   }
   int expected=total();app.saveCharacterSession();var saved=SettingsStore.load();require(saved.getProperty(CharacterSession.HISTORY,"").equals(String.join("/",CharacterSession.snapshot(app.getManager()))),"saved history includes loaded/unselected/disposed packs");
   System.out.println("PASS "+mode+" language="+language+" firstRun="+seen.get()+" finalLive="+expected+" restore="+saved.getProperty(CharacterSession.RESTORE)+" history="+saved.getProperty(CharacterSession.HISTORY)+" assertions="+checks);
   app.exit();
  }catch(Throwable ex){ex.printStackTrace();System.exit(1);}
 }
}
