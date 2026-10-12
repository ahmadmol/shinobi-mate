import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.focus.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
public class UiRefinementCheck extends CharacterCheck {
 static void awaitLaunch(java.util.function.BooleanSupplier ready){
  var loop=java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
  var timer=new javax.swing.Timer(25,e->{if(ready.getAsBoolean()||System.nanoTime()>deadline)loop.exit();});timer.start();loop.enter();timer.stop();require(ready.getAsBoolean(),"asynchronous launch did not complete while EDT remained responsive");
 }
 public static void main(String[] args){try{refinements();System.out.println("PASS search/filter, pending counts/limits, responsive cards, aspect-safe preview, bilingual guide, settings UI rollback and focus protection; "+checks+" assertions");System.exit(0);}catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 static Component named(Container parent,String name){for(Component c:parent.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container child){var found=named(child,name);if(found!=null)return found;}}return null;}
 @SuppressWarnings("unchecked") static void refinements()throws Exception{
  factory=new Factory();var nativeField=NativeFactory.class.getDeclaredField("instance");nativeField.setAccessible(true);nativeField.set(null,factory);
  var app=Main.getInstance();var manager=app.getManager();manager.setExitOnLastRemoved(false);var props=app.getProperties();var configurations=Main.class.getDeclaredField("configurations");configurations.setAccessible(true);var configs=(Map<String,com.group_finity.mascot.config.Configuration>)configurations.get(app);
  var packs=new CharacterService().installed();for(var p:packs)configs.put(p.id(),config(p.id(),"behaviors.xml"));
  var arabic=new CharacterDescriptor("Test","نَـارُوتُو","شينوبي","1","",Path.of("."),"Preview.png",true,false,false,false);
  require(CharacterFilter.matches(arabic,"ناروتو",CharacterFilter.Scope.ALL,0),"Arabic diacritics/tatweel search");require(!CharacterFilter.matches(arabic,"",CharacterFilter.Scope.ACTIVE,0),"inactive filter");require(!CharacterFilter.matches(arabic,"",CharacterFilter.Scope.CLOUD,1),"cloud capability filter");
  Path temp=Files.createTempDirectory("shinobi-preview-"),image=temp.resolve("Preview.png");var source=new java.awt.image.BufferedImage(200,100,java.awt.image.BufferedImage.TYPE_INT_ARGB);var g=source.createGraphics();g.setColor(Color.RED);g.fillRect(0,0,200,100);g.dispose();javax.imageio.ImageIO.write(source,"png",image.toFile());
  var previewPack=new CharacterDescriptor("Test","Preview","","1","",temp,"Preview.png",false,false,false,false);var icon=CharacterPreview.icon(previewPack,100);var rendered=(java.awt.image.BufferedImage)icon.getImage();require((rendered.getRGB(50,0)>>>24)==0&&(rendered.getRGB(50,26)>>>24)>0,"preview stretched or not centered");Files.delete(image);Files.delete(temp);
  SwingUtilities.invokeAndWait(()->{try{
   com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
   for(String language:java.util.List.of("ar","en-GB")){
    props.setProperty("Language",language);CharacterManagerWindow window=new CharacterManagerWindow();window.setVisible(true);
    var search=(JTextField)named(window,"CharacterSearch");var scope=(JComboBox<?>)named(window,"CharacterFilter");search.setText("gOjO");require(button(window,"Launch.GojoSatoru")!=null&&button(window,"Launch.NarutoShippuden")==null,"case-insensitive name search");snapshot(window,"manager-search-"+language);
    search.setText("no-such-pack");require(button(window,"Launch.GojoSatoru")==null,"empty search kept cards");search.setText("");scope.setSelectedIndex(1);require(button(window,"Launch.GojoSatoru")==null,"active filter kept inactive");scope.setSelectedIndex(0);
    props.setProperty("MaxCharacters","1");button(window,"Launch.GojoSatoru").doClick();awaitLaunch(()->new CharacterService().count("GojoSatoru")==1&&!button(window,"Launch.NarutoShippuden").isEnabled());require(new CharacterService().count("GojoSatoru")==1,"pending instance not counted");require(!button(window,"Launch.NarutoShippuden").isEnabled(),"quota button not disabled");scope.setSelectedIndex(1);require(button(window,"Remove.GojoSatoru")!=null,"active filter omitted pending instance");button(window,"Remove.GojoSatoru").doClick();require(new CharacterService().count("GojoSatoru")==0,"disposed instance stayed active");manager.getRemoved().clear();
    scope.setSelectedIndex(0);window.setSize(520,730);window.validate();var field=CharacterManagerWindow.class.getDeclaredField("cards");field.setAccessible(true);JPanel cards=(JPanel)field.get(window);cards.dispatchEvent(new ComponentEvent(cards,ComponentEvent.COMPONENT_RESIZED));require(((GridLayout)cards.getLayout()).getColumns()==1,"narrow manager retained two columns");snapshot(window,"manager-narrow-"+language);window.dispose();
   }
   props.setProperty("Language","ar");OnboardingDialog guide=new OnboardingDialog(null,true);guide.setModalityType(Dialog.ModalityType.MODELESS);guide.setVisible(true);require(!button(guide,"OnboardingBack").isEnabled(),"back enabled on page one");var language=(JComboBox<?>)named(guide,"OnboardingLanguage");language.setSelectedIndex(1);require(button(guide,"OnboardingBack").getText().equals("Back"),"guide back label failed language change");button(guide,"OnboardingNext").doClick();require(((JLabel)named(guide,"OnboardingPreview")).getIcon()!=null,"first companion preview missing");snapshot(guide,"onboarding-preview-en-GB");guide.dispose();
   props.setProperty("Language","ar");SettingsDialog settings=new SettingsDialog(null);settings.setVisible(true);((JComboBox<?>)named(settings,"Language")).setSelectedIndex(1);require(((JLabel)named(settings,"SettingsHeading")).getText().equals("Your companion, your way"),"settings heading stale after language switch");
   String root=System.getProperty("app.userDataDir");Path blocked=Files.createTempDirectory("shinobi-ui-save-");Files.writeString(blocked.resolve("settings.properties"),"blocked");System.setProperty("app.userDataDir",blocked.resolve("settings.properties").toString());
   var top=(JCheckBox)named(settings,"AlwaysOnTop");boolean before=top.isSelected();top.doClick();require(top.isSelected()==before,"failed checkbox save remained changed");var speed=(JSpinner)named(settings,"MovementSpeed");Object old=speed.getValue();speed.setValue(1.5);require(speed.getValue().equals(old),"failed spinner save remained changed");var profile=(JComboBox<?>)named(settings,"PerformanceProfile");int selected=profile.getSelectedIndex();profile.setSelectedIndex((selected+1)%4);require(profile.getSelectedIndex()==selected,"failed choice save remained changed");System.setProperty("app.userDataDir",root);Files.delete(blocked.resolve("settings.properties"));Files.delete(blocked);settings.dispose();
   FocusService.session().stop();FocusDialog focus=new FocusDialog(null);focus.setVisible(true);button(focus,"FocusStart").doClick();long remaining=FocusService.session().remaining(FocusService.now());require(!button(focus,"FocusStart").isEnabled()&&!named(focus,"FocusMinutes").isEnabled(),"active session restart/duration editing available");button(focus,"FocusStart").doClick();require(FocusService.session().remaining(FocusService.now())<=remaining,"start reset running focus");button(focus,"FocusPause").doClick();require(!button(focus,"FocusStart").isEnabled(),"paused focus allowed restart");button(focus,"FocusStop").doClick();require(button(focus,"FocusStart").isEnabled()&&named(focus,"FocusMinutes").isEnabled()&&!button(focus,"FocusStop").isEnabled(),"idle controls did not restore");focus.dispose();
  }catch(Exception ex){throw new RuntimeException(ex);}});
  manager.disposeAll();
 }
}
