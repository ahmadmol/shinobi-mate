package com.group_finity.mascot.ui;
import com.group_finity.mascot.update.UpdateChecker;
import com.group_finity.mascot.settings.*;
import java.awt.*;
import javax.swing.*;
public final class UpdateDialog extends JDialog {
 private final JLabel status=new JLabel(" ");
 private final JButton check=Theme.button(Theme.t("تحقق الآن","Check now"));
 private final JButton download=Theme.button(Theme.t("صفحة التحميل","Download page"));
 private UpdateChecker.Result result;
 public UpdateDialog(Window owner){
  super(owner,Theme.t("تحديث ShinobiMate","ShinobiMate updates"),ModalityType.MODELESS);setName("ShinobiMateUpdates");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  JPanel root=Theme.card();root.add(Theme.title(Theme.t("التحديثات، بقرارك","Updates, when you choose")));
  root.add(new JLabel(Theme.t("رابط بيانات التحديث الرسمي (HTTPS)","Official update metadata URL (HTTPS)")));
  JTextField endpoint=new JTextField(RuntimeSettings.text("UpdateMetadataURL",""));endpoint.setName("UpdateMetadataURL");endpoint.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));root.add(endpoint);
  root.add(Box.createVerticalStrut(16));root.add(status);root.add(Box.createVerticalStrut(16));
  JPanel actions=new JPanel(new FlowLayout(FlowLayout.CENTER,12,0));actions.setOpaque(false);download.setEnabled(false);
  check.addActionListener(e->{
   try{
    var uri=UpdateChecker.https(endpoint.getText().trim());SettingsService.set("UpdateMetadataURL",uri.toString());check.setEnabled(false);download.setEnabled(false);status.setText(Theme.t("جارٍ التحقق…","Checking…"));
    new SwingWorker<UpdateChecker.Result,Void>(){
     protected UpdateChecker.Result doInBackground()throws Exception{return new UpdateChecker().check(uri);}
     protected void done(){check.setEnabled(true);try{result=get();status.setText(result.newer()?Theme.t("إصدار جديد: ","New version: ")+result.version():Theme.t("لا يوجد إصدار أحدث","No newer version"));download.setEnabled(result.newer());}catch(Exception ex){status.setText(Theme.t("تعذر التحقق؛ راجع الرابط والاتصال","Could not check; review the URL and connection"));}}
    }.execute();
   }catch(Exception ex){status.setText(ex.getMessage());}
  });
  download.addActionListener(e->{try{if(result!=null&&Desktop.isDesktopSupported())Desktop.getDesktop().browse(result.downloadPage());}catch(Exception ex){status.setText(ex.getMessage());}});
  actions.add(check);actions.add(download);root.add(actions);setContentPane(root);Theme.orient(this);Theme.fit(this,720,310);
 }
}
