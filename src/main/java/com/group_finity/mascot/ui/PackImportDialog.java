package com.group_finity.mascot.ui;
import com.group_finity.mascot.pack.*;
import com.group_finity.mascot.character.CharacterCatalog;
import java.awt.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
/** Staging runs off the EDT; confirmation uses the exact validated preview. */
public final class PackImportDialog {
 private PackImportDialog(){ }
 public static void choose(Window owner) {
  JFileChooser chooser=new JFileChooser();chooser.setFileFilter(new FileNameExtensionFilter(Theme.t("حزمة شخصية","Character pack"),"characterpack","zip","smp"));
  if(chooser.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION)return;
  Path archive=chooser.getSelectedFile().toPath();
  new SwingWorker<Boolean,Void>() {
   protected Boolean doInBackground(){return installWithPreview(owner,archive);}
   protected void done(){
    try{if(get()){JOptionPane.showMessageDialog(owner,Theme.t("تم تثبيت الحزمة. يمكنك تشغيلها من مدير الشخصيات.","Pack installed. Launch it from the character manager."));CharacterManagerWindow.open();}}
    catch(Exception ex){JOptionPane.showMessageDialog(owner,ex.getMessage());}
   }
  }.execute();
 }
 public static boolean installWithPreview(Component owner,Path archive) {
  try(var preview=new SafePackImporter().preview(archive,CharacterCatalog.root())) {
   AtomicBoolean approved=new AtomicBoolean();
   Runnable confirm=()->{
    var m=preview.manifest();JPanel panel=Theme.card();
    try{var image=javax.imageio.ImageIO.read(preview.content().resolve(m.preview()).toFile());if(image!=null)panel.add(new JLabel(new ImageIcon(image.getScaledInstance(128,128,Image.SCALE_SMOOTH))));}catch(Exception ignored){ }
    panel.add(Theme.title(m.displayName()));panel.add(new JLabel(m.author()+" • "+m.version()));
    JTextArea description=new JTextArea(m.description(),3,32);description.setLineWrap(true);description.setWrapStyleWord(true);description.setEditable(false);description.setOpaque(false);panel.add(description);
    panel.add(new JLabel(Theme.t("معرّف الحزمة: ","Pack id: ")+m.id()));
    Theme.orient(panel);
    String[] options={Theme.t("تثبيت","Install"),Theme.t("إلغاء","Cancel")};
    JOptionPane question=new JOptionPane(panel,JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,options,options[1]);
    JDialog dialog=question.createDialog(owner,Theme.t("معاينة الحزمة — تثبيت؟","Pack preview — install?"));
    dialog.setName("PackPreview");Theme.orient(dialog);dialog.setVisible(true);
    approved.set(options[0].equals(question.getValue()));dialog.dispose();
   };
   if(SwingUtilities.isEventDispatchThread())confirm.run();else SwingUtilities.invokeAndWait(confirm);
   if(!approved.get())return false;preview.install();return true;
  }catch(Exception ex){
   SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(owner,Theme.t("تعذر الاستيراد: ","Import failed: ")+ex.getMessage()));return false;
  }
 }
}
