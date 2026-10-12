package com.group_finity.mascot.ui;
import com.group_finity.mascot.progression.*;
import com.group_finity.mascot.character.CharacterCatalog;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
public final class ProgressionDialog extends JDialog {
 private final JTabbedPane tabs=new JTabbedPane();
 private final Timer timer=new Timer(2000,e->refresh());
 public ProgressionDialog(Window owner){
  super(owner,Theme.t("المستويات والتحديات","Progression and challenges"),ModalityType.MODELESS);setName("ShinobiMateProgression");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(new EmptyBorder(20,20,20,20));
  root.add(Theme.title(Theme.t("إنجازات صغيرة، بلا ضغط","Small achievements, no pressure")),BorderLayout.NORTH);root.add(tabs,BorderLayout.CENTER);
  root.add(new JLabel(Theme.t("المستويات تجميلية ولا تؤثر على قوة القتال","Levels are cosmetic and do not affect combat")),BorderLayout.SOUTH);setContentPane(root);refresh();Theme.orient(this);Theme.fit(this,700,520);
  addComponentListener(new ComponentAdapter(){public void componentShown(ComponentEvent e){timer.start();}public void componentHidden(ComponentEvent e){timer.stop();}});
  addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){timer.stop();}});
 }
 private void refresh(){
  int selected=tabs.getSelectedIndex();tabs.removeAll();var model=ProgressionService.instance().model();JPanel profiles=Theme.card();
  if(!RuntimeSettings.flag("ProgressionEnabled",false))profiles.add(Theme.text(Theme.t("فعّل المستويات الاختيارية من الإعدادات","Enable optional progression in settings")));
  for(var p:new CharacterCatalog().list()){
   var level=model.profile(p.id());profiles.add(Theme.text(p.displayName()+" • "+Theme.t("المستوى ","Level ")+level.level()+" • XP "+level.xp()));
  }
  tabs.addTab(Theme.t("الشخصيات","Profiles"),new JScrollPane(profiles));
  for(boolean weekly:new boolean[]{false,true}){
   JPanel panel=Theme.card();if(!RuntimeSettings.flag("ChallengesEnabled",false))panel.add(Theme.text(Theme.t("فعّل التحديات الاختيارية من الإعدادات","Enable optional challenges in settings")));
   for(var challenge:model.challenges(LocalDate.now(),weekly)){
    String label=switch(challenge.event()){case INTERACTION->Theme.t("تفاعل مع شخصية","Interact with a character");case FOCUS_COMPLETED->Theme.t("أكمل جلسة تركيز","Complete focus sessions");case BATTLE_COMPLETED->Theme.t("أكمل قتالًا يدويًا","Complete manual battles");case RUN_TWO->Theme.t("شغّل شخصيتين","Run two characters");};
    panel.add(Theme.text(label));JProgressBar bar=new JProgressBar(0,challenge.target());bar.setValue(challenge.current());bar.setStringPainted(true);bar.setString(challenge.current()+" / "+challenge.target());bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));panel.add(bar);panel.add(Box.createVerticalStrut(16));
   }
   tabs.addTab(weekly?Theme.t("الأسبوع","Weekly"):Theme.t("اليوم","Daily"),new JScrollPane(panel));
  }
  if(selected>=0&&selected<tabs.getTabCount())tabs.setSelectedIndex(selected);Theme.orient(tabs);
 }
}
