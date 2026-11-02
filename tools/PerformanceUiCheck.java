import com.group_finity.mascot.*;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.ui.*;
import java.awt.*;
import java.nio.file.*;
import javax.swing.*;

/** Real Main/windows with isolated profile: filtering, async previews, remove semantics and settings persistence. */
public class PerformanceUiCheck extends RefinementDesktopCheck {
 static CharacterManagerWindow window;
 static int countNamed(Container root,String prefix){int n=0;for(Component c:root.getComponents()){if(c.getName()!=null&&c.getName().startsWith(prefix))n++;if(c instanceof Container inner)n+=countNamed(inner,prefix);}return n;}
 public static void main(String[]args)throws Exception {
  try {
   Files.createDirectories(out);Main.main(new String[0]);app=Main.getInstance();manager=app.getManager();
   require(manager.getCount()==0,"unexpected startup character");
   for(String lang:java.util.List.of("ar","en-GB"))for(String theme:java.util.List.of("light","dark")) {
    edt(()->{for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow)w.dispose();app.getProperties().setProperty("Language",lang);app.getProperties().setProperty("FlatLafTheme",theme);app.refreshProductLanguage();app.refreshProductTheme();window=new CharacterManagerWindow();window.setVisible(true);});
    waitFor(()->((JLabel)named(window,"Preview.AstaBlackClover")).getIcon()!=null,10,"async preview not delivered");
    edt(()->{try {
     var search=(JTextField)named(window,"CharacterSearch");search.setText("Eren");require(countNamed(window,"Launch.")==1,"search did not filter");search.setText("no match xyz");require(countNamed(window,"Launch.")==0,"empty result wrong");search.setText("");
     require(countNamed(window,"Launch.")==8,"clear search did not restore cards");var filter=(JComboBox<?>)named(window,"CharacterFilter");filter.setSelectedIndex(1);require(countNamed(window,"Launch.")==0,"active-only showed inactive");filter.setSelectedIndex(2);require(countNamed(window,"Launch.")==8,"combat capability filter");filter.setSelectedIndex(0);
     window.setSize(620,720);window.validate();
     require(window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("control F"))!=null,"search shortcut missing");
     SettingsDialog d=new SettingsDialog(window);d.setVisible(true);var nav=(JList<?>)named(d,"SettingsNavigation");for(int i=0;i<10;i++){nav.setSelectedIndex(i);d.validate();}
     nav.setSelectedIndex(4);capture(d,"settings-audio-"+theme+"-"+lang);require(d.getComponentOrientation().isLeftToRight()==!lang.equals("ar"),"orientation wrong");d.dispose();
    }catch(Exception e){throw new RuntimeException(e);}});
    // Resize notifications are delivered after the current EDT event. Capture
    // the settled responsive layout rather than the previous two-column frame.
    Thread.sleep(200);edt(()->{try{window.validate();capture(window,"manager-narrow-"+theme+"-"+lang);
     for(String buttonName:java.util.List.of("Launch.AstaBlackClover","Remove.AstaBlackClover","RemoveAll.AstaBlackClover")){
      JButton button=(JButton)named(window,buttonName);require(button.getWidth()>=button.getFontMetrics(button.getFont()).stringWidth(button.getText())+button.getInsets().left+button.getInsets().right,"button text clipped "+buttonName);
     }
    }catch(Exception e){throw new RuntimeException(e);}});
   }
   for(int i=1;i<=3;i++){edt(()->((JButton)named(window,"Launch.ErenYeager")).doClick());final int expected=i;waitFor(()->manager.getCount()==expected,15,"launch count");}
   edt(()->{require(((JButton)named(window,"Remove.ErenYeager")).isEnabled(),"remove-one stale after launch");((JButton)named(window,"Remove.ErenYeager")).doClick();});waitFor(()->manager.getCount()==2,10,"remove-one removed wrong count");
   edt(()->{require(((JButton)named(window,"RemoveAll.ErenYeager")).isEnabled(),"remove-all stale after launch");((JButton)named(window,"RemoveAll.ErenYeager")).doClick();});waitFor(()->manager.getCount()==0,10,"remove-all retained instances");
   edt(()->{SettingsDialog d=new SettingsDialog(window);d.setVisible(true);((JSlider)named(d,"MasterVolume")).setValue(23);((JCheckBox)named(d,"Mute")).setSelected(false);d.dispose();});
   var stored=new java.util.Properties();try(var input=Files.newInputStream(AppPaths.userData().resolve("settings.properties"))){stored.load(input);}require("23".equals(stored.getProperty("MasterVolume")),"audio preference not persisted");
   pass("real Main manager async previews/filter/search/empty results/remove-one 3->2/remove-all 2->0; ten settings pages, RTL/LTR/dark/light, narrow window, audio persistence");
   edt(()->{window.setSize(1000,780);window.toFront();});
   int hold=Integer.getInteger("ui.interactiveSeconds",0);for(int i=0;i<hold&&!Files.exists(out.resolve("stop-ui"));i++)Thread.sleep(1000);
   app.exit();
  }catch(Throwable error){error.printStackTrace();System.exit(1);}
 }
}
