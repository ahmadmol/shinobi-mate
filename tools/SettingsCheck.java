import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.performance.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
public class SettingsCheck extends CombatCheck {
 public static void main(String[] args) {
  try {checkSettings();System.out.println("PASS settings, profiles, persistence, RTL/LTR, audio gain; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 static void checkSettings() throws Exception {
  Path temp=Files.createTempDirectory("shinobi-settings-check-"),destination=temp.resolve("settings.properties");
  Properties p=new Properties();p.setProperty("CustomUnicode","مرحبا 🌙 Hello");p.setProperty("LegacyKey","preserved");
  SettingsStore.save(p,destination);Properties loaded=new Properties();try(var in=Files.newInputStream(destination)){loaded.load(in);}
  require(loaded.equals(p),"Unicode/legacy settings lost");
  p.setProperty("bad","NaN");require(RuntimeSettings.number(p,"bad",1,.5,2)==1,"NaN accepted");
  p.setProperty("bad","999");require(RuntimeSettings.number(p,"bad",1,.5,2)==2,"unbounded setting");
  String base=System.getProperty("app.userDataDir");Path blocked=temp.resolve("blocked");Files.createDirectory(blocked);Files.writeString(blocked.resolve("settings.properties"),"blocks settings directory");
  var props=Main.getInstance().getProperties();props.setProperty("RollbackProbe","before");System.setProperty("app.userDataDir",blocked.resolve("settings.properties").toString());
  boolean failed=false;try{SettingsService.set("RollbackProbe","after");}catch(java.io.IOException expected){failed=true;}
  require(failed&&props.getProperty("RollbackProbe").equals("before"),"save failure did not roll back memory");System.setProperty("app.userDataDir",base);Files.delete(blocked.resolve("settings.properties"));Files.delete(blocked);
  SettingsService.set("UnicodeProbe","عربية 🌙 English");Properties persisted=new Properties();try(var in=Files.newInputStream(SettingsStore.path())){persisted.load(in);}require(persisted.getProperty("UnicodeProbe").equals("عربية 🌙 English"),"write-through Unicode persistence");
  var global=Main.getInstance().getProperties();global.setProperty("MasterVolume","50");global.setProperty("CombatVolume","40");
  require(Math.abs(com.group_finity.mascot.audio.SoundSystem.gain(com.group_finity.mascot.audio.SoundSystem.Category.COMBAT)-.2)<.001,"volume not multiplicative");
  global.setProperty("Mute","true");require(com.group_finity.mascot.audio.SoundSystem.gain(com.group_finity.mascot.audio.SoundSystem.Category.COMBAT)==0,"mute not respected");global.setProperty("Mute","false");
  var pa=profile("NarutoShippuden");var pb=profile("SasukeShippuden");int[] result=null;
  for(String name:java.util.List.of("LOW","BALANCED","HIGH","CUSTOM")) {
   global.setProperty("PerformanceProfile",name);var profile=PerformanceProfile.current();RenderGate gate=new RenderGate();int renders=0;
   Body a=new Body(200,500,1,new Rectangle(0,0,1000,500)),b=new Body(800,500,1,a.area);
   var match=new CombatMatch(a,pa,b,pb,new Random(7));
   for(int tick=0;tick<500;tick++){match.tick();if(gate.ready(tick*40L,profile.fps()))renders++;}
   int[] now={match.a.hp,match.b.hp,(int)a.x,(int)b.x};
   if(result==null)result=now;else require(Arrays.equals(result,now),"FPS changed combat simulation");
   require(renders>=190&&renders<=500,"render schedule invalid");
   System.out.println("PASS "+name+": 500 logic ticks, "+renders+" render updates, identical simulation");
  }
  SwingUtilities.invokeAndWait(()->{
   try{
    com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
    for(String language:java.util.List.of("ar","en-GB")) {
     global.setProperty("Language",language);SettingsDialog dialog=new SettingsDialog(null);dialog.setVisible(true);
     require(dialog.getComponentOrientation().isLeftToRight()==!language.equals("ar"),"settings orientation");
     var image=new BufferedImage(dialog.getWidth(),dialog.getHeight(),BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();dialog.paint(g);g.dispose();
     Files.createDirectories(Path.of("art/product-roadmap/screenshots"));
     ImageIO.write(image,"png",Path.of("art/product-roadmap/screenshots/settings-"+language+".png").toFile());dialog.dispose();
     SettingsDialog expanded=new SettingsDialog(null);expanded.setVisible(true);JTabbedPane tabs=findTabs(expanded);require(tabs.getTabCount()==9,"settings/audio section missing");
     for(int tab=0;tab<tabs.getTabCount();tab++){tabs.setSelectedIndex(tab);expanded.validate();var shot=new BufferedImage(expanded.getWidth(),expanded.getHeight(),BufferedImage.TYPE_INT_RGB);var graphics=shot.createGraphics();expanded.paint(graphics);graphics.dispose();ImageIO.write(shot,"png",Path.of("art/product-roadmap/screenshots/settings-"+language+"-"+tab+".png").toFile());}
     var restore=findCheck(expanded,CharacterSession.RESTORE);require(restore!=null&&!restore.isSelected(),"restore missing or enabled by default");restore.doClick();require(RuntimeSettings.flag(CharacterSession.RESTORE,false),"restore setting not connected");restore.doClick();require(!RuntimeSettings.flag(CharacterSession.RESTORE,true),"restore setting failed to disable");
     var input=findCheck(expanded,"AlwaysOnTop");boolean selected=input.isSelected();input.doClick();require(RuntimeSettings.flag("AlwaysOnTop",selected)!=selected,"settings UI toggle not connected");input.doClick();expanded.dispose();
    }
    com.formdev.flatlaf.FlatLightLaf.setup();Theme.defaults();global.setProperty("Language","ar");SettingsDialog light=new SettingsDialog(null);light.setVisible(true);var shot=new BufferedImage(light.getWidth(),light.getHeight(),BufferedImage.TYPE_INT_RGB);var graphics=shot.createGraphics();light.paint(graphics);graphics.dispose();ImageIO.write(shot,"png",Path.of("art/product-roadmap/screenshots/settings-light-ar.png").toFile());light.dispose();
   }catch(Exception ex){throw new RuntimeException(ex);}
  });
  Files.delete(destination);Files.delete(temp);
 }
 static JTabbedPane findTabs(Container parent){for(Component c:parent.getComponents()){if(c instanceof JTabbedPane tabs)return tabs;if(c instanceof Container child){var found=findTabs(child);if(found!=null)return found;}}return null;}
 static JCheckBox findCheck(Container parent,String name){for(Component c:parent.getComponents()){if(c instanceof JCheckBox input&&name.equals(c.getName()))return input;if(c instanceof Container child){var found=findCheck(child,name);if(found!=null)return found;}}return null;}
}
