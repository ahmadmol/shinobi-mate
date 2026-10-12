package com.group_finity.mascot.ui;
import com.group_finity.mascot.focus.*;
import com.group_finity.mascot.settings.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
public final class FocusDialog extends JDialog {
 private final JLabel clock=Theme.title("");
 private final JButton pause=Theme.button("");
 private final JButton start=Theme.button(Theme.t("ابدأ","Start")),stop=Theme.button(Theme.t("إيقاف","Stop"));
 private final java.util.List<JSpinner> durations=new java.util.ArrayList<>();
 private final Timer timer=new Timer(1000,e->refresh());
 public FocusDialog(Window owner) {
  super(owner,Theme.t("وقت التركيز","Focus time"),ModalityType.MODELESS);setName("ShinobiMateFocus");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  JPanel root=Theme.card();root.setBorder(new EmptyBorder(24,24,24,24));
  root.add(Theme.title(Theme.t("جلسة هادئة، خطوة صغيرة","A quiet session, one small step")));root.add(Box.createVerticalStrut(20));root.add(clock);
  spinner(root,"دقائق التركيز","Focus minutes","FocusMinutes",25,180);spinner(root,"دقائق الاستراحة","Break minutes","BreakMinutes",5,60);
  JPanel controls=new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));controls.setOpaque(false);
  start.setName("FocusStart");start.addActionListener(e->{if(FocusService.session().phase()==FocusSession.Phase.IDLE)FocusService.start();refresh();});controls.add(start);
  pause.setName("FocusPause");pause.addActionListener(e->{if(FocusService.session().phase()==FocusSession.Phase.PAUSED)FocusService.session().resume(FocusService.now());else FocusService.session().pause(FocusService.now());refresh();});controls.add(pause);
  stop.setName("FocusStop");stop.addActionListener(e->{FocusService.session().stop();refresh();});controls.add(stop);root.add(controls);
  JCheckBox reminders=new JCheckBox(Theme.t("تذكير لطيف عند الانتقال","Gentle transition reminder"),RuntimeSettings.flag("FocusReminders",true));
  reminders.addActionListener(e->{try{SettingsService.set("FocusReminders",Boolean.toString(reminders.isSelected()));}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}});root.add(reminders);
  setContentPane(root);Theme.orient(this);Theme.fit(this,620,390);refresh();
  addComponentListener(new ComponentAdapter(){public void componentShown(ComponentEvent e){timer.start();}public void componentHidden(ComponentEvent e){timer.stop();}});
  addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){timer.stop();}});
 }
 private void spinner(JPanel p,String ar,String en,String key,int fallback,int max) {
  JPanel row=new JPanel(new FlowLayout(FlowLayout.LEADING));row.setOpaque(false);row.add(new JLabel(Theme.t(ar,en)));
  JSpinner s=new JSpinner(new SpinnerNumberModel((int)RuntimeSettings.number(key,fallback,1,max),1,max,1));s.setName(key);durations.add(s);s.addChangeListener(e->{try{SettingsService.set(key,s.getValue().toString());}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}});row.add(s);p.add(row);
 }
 private void refresh(){
  var state=FocusService.session();long seconds=(state.remaining(FocusService.now())+999)/1000;
  String phase=switch(state.phase()){case IDLE->Theme.t("جاهز","Ready");case FOCUS->Theme.t("تركيز","Focus");case BREAK->Theme.t("استراحة","Break");case PAUSED->Theme.t("متوقف مؤقتًا","Paused");};
  clock.setText(phase+"  "+String.format("%02d:%02d",seconds/60,seconds%60));pause.setText(state.phase()==FocusSession.Phase.PAUSED?Theme.t("متابعة","Resume"):Theme.t("إيقاف مؤقت","Pause"));pause.setEnabled(state.phase()!=FocusSession.Phase.IDLE);
  boolean idle=state.phase()==FocusSession.Phase.IDLE;start.setEnabled(idle);stop.setEnabled(!idle);durations.forEach(s->s.setEnabled(idle));
 }
}
