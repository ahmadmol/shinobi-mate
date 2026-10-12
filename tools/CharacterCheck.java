import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
public class CharacterCheck extends CombatCheck {
 public static void main(String[] args) {
  try{testCharacters();System.out.println("PASS real character facade, five packs, duplicate instances, limit/removal, bilingual cards, first-run language handoff and optional five-page help; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 static void snapshot(java.awt.Window window,String file)throws Exception {
  RepaintManager.currentManager(window).validateInvalidComponents();window.validate();
  var image=new BufferedImage(window.getWidth(),window.getHeight(),BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();window.paint(g);g.dispose();
  ImageIO.write(image,"png",Path.of("art/product-roadmap/screenshots/"+file+".png").toFile());
 }
 static JButton button(Container root,String name) {
  for(Component c:root.getComponents()){if(c instanceof JButton b&&name.equals(b.getName()))return b;if(c instanceof Container child){var b=button(child,name);if(b!=null)return b;}}
  return null;
 }
 @SuppressWarnings("unchecked")
 static void testCharacters()throws Exception {
  System.setProperty("app.dir",Path.of("output/product-ui").toAbsolutePath().toString());
  factory=new Factory();Field nativeField=NativeFactory.class.getDeclaredField("instance");nativeField.setAccessible(true);nativeField.set(null,factory);
  Main app=Main.getInstance();var manager=app.getManager();manager.setExitOnLastRemoved(false);
  Field lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);
  lang.set(app,ResourceBundle.getBundle("language",Locale.forLanguageTag("ar"),new com.joconner.i18n.ConfDirectoryUtf8Control(new java.io.File("conf"))));
  Field configurations=Main.class.getDeclaredField("configurations");configurations.setAccessible(true);var configs=(Map<String,com.group_finity.mascot.config.Configuration>)configurations.get(app);
  var service=new CharacterService();var packs=service.installed();require(packs.size()>=5,"five legacy packs missing");
  for(var p:packs){configs.put(p.id(),config(p.id(),"behaviors.xml"));require(service.launch(p.id()),"pack launch failed");}
  Method tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);tick.invoke(manager);
  require(manager.getCount()==packs.size(),"launch count");require(service.launch(packs.getFirst().id()),"duplicate launch");tick.invoke(manager);
  app.getProperties().setProperty("MaxCharacters",Integer.toString(manager.getCount()));require(!service.launch(packs.getFirst().id()),"maximum bypassed");
  SwingUtilities.invokeAndWait(()->{
   try{
    com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
    for(String language:java.util.List.of("ar","en-GB")) {
     app.getProperties().setProperty("Language",language);CharacterManagerWindow window=new CharacterManagerWindow();window.setVisible(true);
     require(window.getComponentOrientation().isLeftToRight()==!language.equals("ar"),"cards orientation");snapshot(window,"characters-"+language);window.dispose();
    }
    app.getProperties().setProperty("Language","ar");OnboardingDialog guide=new OnboardingDialog(null,false);guide.setModalityType(Dialog.ModalityType.MODELESS);guide.setVisible(true);
    snapshot(guide,"onboarding-ar");
    JButton next=button(guide,"OnboardingNext");require(next!=null,"onboarding has no next");
    require(guide.pageIndex()==0,"first-run language page missing");
    require(!guide.completed(),"premature completion");next.doClick();require(guide.completed(),"completion not persisted");
    require(app.getProperties().getProperty("OnboardingComplete").equals("true"),"missing completion flag");
    String selected=app.getProperties().getProperty("ActiveShimeji","");app.getProperties().setProperty("Language","en-GB");
    OnboardingDialog english=new OnboardingDialog(null,true);english.setModalityType(Dialog.ModalityType.MODELESS);english.setVisible(true);
    require(english.getComponentOrientation().isLeftToRight(),"English onboarding not LTR");JButton englishNext=button(english,"OnboardingNext");
    for(int i=0;i<5;i++){require(english.pageIndex()==i,"English onboarding skipped page");snapshot(english,"onboarding-en-GB-"+i);englishNext.doClick();}
    require(english.completed()&&selected.equals(app.getProperties().getProperty("ActiveShimeji")),"replayed guide changed active character");
   }catch(Exception ex){throw new RuntimeException(ex);}
  });
  service.remove(packs.getFirst().id());tick.invoke(manager);require(service.count(packs.getFirst().id())==0,"remove missed duplicate");
  manager.disposeAll();tick.invoke(manager);
  app.getProperties().setProperty("MaxCharacters","2");Manager parallel=new Manager();parallel.setExitOnLastRemoved(false);
  try(var pool=java.util.concurrent.Executors.newFixedThreadPool(4)){
   var tasks=new ArrayList<java.util.concurrent.Future<?>>();for(int i=0;i<16;i++)tasks.add(pool.submit(()->parallel.add(new Mascot(packs.getFirst().id()))));
   for(var task:tasks)task.get(20,java.util.concurrent.TimeUnit.SECONDS);
  }
  synchronized(parallel.getMascots()){
   require(parallel.getAdded().size()==2,"concurrent admission exceeded limit");
   for(var m:java.util.List.copyOf(parallel.getAdded())){require(m.getManager()==parallel,"pending mascot has no manager");m.dispose();}
   require(parallel.getAdded().isEmpty(),"pending removal leaked instance");
  }
 }
}
