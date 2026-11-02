import com.group_finity.mascot.*;
import com.group_finity.mascot.ui.*;
import javax.swing.*;

/** Real product manager/dialog show-hide controls mascot and combat overlay layering policy. */
public class LayeringCheck {
 public static void main(String[]args)throws Exception {
  Main.getInstance().getProperties().setProperty("AlwaysOnTop","true");
  SwingUtilities.invokeAndWait(()->{
   if(!CompanionLayering.alwaysOnTop())throw new AssertionError("default topmost lost");
   CharacterManagerWindow manager=new CharacterManagerWindow();manager.setVisible(true);
   if(CompanionLayering.alwaysOnTop())throw new AssertionError("companions cover manager");
   if(!CompanionLayering.coversControls(manager.getBounds()))throw new AssertionError("overlapping window not masked");
   if(CompanionLayering.coversControls(new java.awt.Rectangle(-9999,-9999,1,1)))throw new AssertionError("unrelated desktop area masked");
   manager.setVisible(false);if(!CompanionLayering.alwaysOnTop())throw new AssertionError("hiding manager did not restore setting");
   JDialog dialog=new JDialog();dialog.setSize(200,120);dialog.setVisible(true);
   if(CompanionLayering.alwaysOnTop())throw new AssertionError("companions cover dialog");dialog.dispose();manager.dispose();
   Main.getInstance().getProperties().setProperty("AlwaysOnTop","false");
   if(CompanionLayering.alwaysOnTop())throw new AssertionError("user topmost preference ignored");
  });
  System.out.println("PASS visible manager/dialog lower companions and VFX; hide restores user preference");System.exit(0);
 }
}
