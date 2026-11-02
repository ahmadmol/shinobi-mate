package com.group_finity.mascot.ui;
import com.group_finity.mascot.Main;
import com.group_finity.mascot.settings.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Product settings; legacy environment/editor options remain reachable. */
public final class SettingsDialog extends JDialog {
 private final DefaultListModel<String> sections=new DefaultListModel<>();
 private final JList<String> navigation=new JList<>(sections);
 private final JScrollPane sidebar=new JScrollPane(navigation);
 private final JPanel pages=new JPanel(new CardLayout());
 private final JLabel saved=new JLabel(" ");
 private final JLabel heading=Theme.title(Theme.t("رفيقك، على طريقتك","Your companion, your way"));
 private boolean restoring;
 public SettingsDialog(Window owner) {
  super(owner,Theme.t("إعدادات ShinobiMate","ShinobiMate settings"),ModalityType.MODELESS);
  setName("ShinobiMateSettings");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(new EmptyBorder(18,18,18,18));
  heading.setName("SettingsHeading");root.add(heading,BorderLayout.NORTH);
  navigation.setName("SettingsNavigation");navigation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);navigation.setFixedCellHeight(46);
  navigation.setCellRenderer(new DefaultListCellRenderer(){@Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus){
   JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus);label.setBorder(new EmptyBorder(8,12,8,12));label.setFont(label.getFont().deriveFont(selected?Font.BOLD:Font.PLAIN));return label;
  }});
  navigation.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&navigation.getSelectedIndex()>=0)((CardLayout)pages.getLayout()).show(pages,Integer.toString(navigation.getSelectedIndex()));});
  sidebar.setBorder(null);navigation.addPropertyChangeListener("font",e->sizeSidebar());
  JPanel body=new JPanel(new BorderLayout(18,0));body.add(sidebar,BorderLayout.LINE_START);body.add(pages,BorderLayout.CENTER);
  root.add(body,BorderLayout.CENTER);saved.setBorder(new EmptyBorder(8,0,0,0));root.add(saved,BorderLayout.SOUTH);setContentPane(root);
  build();Theme.orient(this);Theme.fit(this,1000,740);
  getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
  addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowOpened(java.awt.event.WindowEvent e){navigation.requestFocusInWindow();}});
  com.group_finity.mascot.audio.AudioManager.play("ui.settings.open");
 }
 private void sizeSidebar(){
  Font font=navigation.getFont();if(font==null)return;
  FontMetrics metrics=navigation.getFontMetrics(font.deriveFont(Font.BOLD));int width=178;
  for(int i=0;i<sections.size();i++)width=Math.max(width,metrics.stringWidth(sections.get(i))+40);
  navigation.setFixedCellHeight(Math.max(46,metrics.getHeight()+16));sidebar.setPreferredSize(new Dimension(width,100));sidebar.revalidate();
 }
 private JPanel section(String ar,String en) {
  JPanel p=Theme.card();JScrollPane scroll=new JScrollPane(p);
  scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(20);
  int index=sections.size();sections.addElement(Theme.t(ar,en));pages.add(scroll,Integer.toString(index));
  JLabel title=Theme.title(Theme.t(ar,en));title.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(title);p.add(Box.createVerticalStrut(12));return p;
 }
 private void row(JPanel p,String ar,String en,JComponent input) {
  JPanel row=new JPanel(new BorderLayout(14,0));row.setOpaque(false);row.setBorder(new EmptyBorder(8,0,8,0));
  JTextArea label=Theme.description(Theme.t(ar,en));label.setRows(2);label.setBorder(null);label.setMinimumSize(new Dimension(0,30));
  input.getAccessibleContext().setAccessibleName(Theme.t(ar,en));Theme.revealOnFocus(input);row.add(label,BorderLayout.CENTER);row.add(input,BorderLayout.LINE_END);
  Runnable fit=()->{row.setMaximumSize(new Dimension(Integer.MAX_VALUE,Math.max(68,label.getFontMetrics(label.getFont()).getHeight()*2+16)));row.revalidate();};
  label.addPropertyChangeListener("font",e->fit.run());fit.run();row.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(row);
 }
 private boolean save(String key,String value) {
  if(restoring)return true;
  try{SettingsService.set(key,value);saved.setText(Theme.t("تم الحفظ","Saved"));
   if(key.equals("Language")){int selected=navigation.getSelectedIndex();build();navigation.setSelectedIndex(Math.max(0,selected));Theme.orient(this);heading.setText(Theme.t("رفيقك، على طريقتك","Your companion, your way"));setTitle(Theme.t("إعدادات ShinobiMate","ShinobiMate settings"));}
   com.group_finity.mascot.audio.SoundSystem.play(com.group_finity.mascot.audio.SoundSystem.Category.UI);return true;}
  catch(Exception ex){saved.setText(Theme.t("تعذر الحفظ: ","Could not save: ")+ex.getMessage());return false;}
 }
 private void restore(Runnable action){restoring=true;try{action.run();}finally{restoring=false;}}
 private JCheckBox toggle(JPanel p,String ar,String en,String key,boolean fallback) {
  JCheckBox input=new JCheckBox();input.setName(key);input.setSelected(RuntimeSettings.flag(key,fallback));
  input.addActionListener(e->{if(!save(key,Boolean.toString(input.isSelected())))restore(()->input.setSelected(RuntimeSettings.flag(key,fallback)));});row(p,ar,en,input);return input;
 }
 private void number(JPanel p,String ar,String en,String key,double fallback,double min,double max,double step) {
  JSpinner input=new JSpinner(new SpinnerNumberModel(RuntimeSettings.number(key,fallback,min,max),min,max,step));
  input.setName(key);input.setPreferredSize(new Dimension(130,34));input.addChangeListener(e->{if(!save(key,input.getValue().toString()))restore(()->input.setValue(RuntimeSettings.number(key,fallback,min,max)));});row(p,ar,en,input);
 }
 private void volume(JPanel p,String ar,String en,String key,int fallback) {
  JPanel control=new JPanel(new BorderLayout(8,0));control.setOpaque(false);
  JSlider slider=new JSlider(0,100,(int)RuntimeSettings.number(key,fallback,0,100));slider.setName(key);
  slider.setPreferredSize(new Dimension(220,38));slider.getAccessibleContext().setAccessibleName(Theme.t(ar,en));
  JLabel percentage=new JLabel(slider.getValue()+"%");
  slider.addChangeListener(e->{percentage.setText(slider.getValue()+"%");
   if(!slider.getValueIsAdjusting()&&!save(key,Integer.toString(slider.getValue())))restore(()->slider.setValue((int)RuntimeSettings.number(key,fallback,0,100)));});
  control.add(slider,BorderLayout.CENTER);control.add(percentage,BorderLayout.LINE_END);row(p,ar,en,control);
 }
 private void appearanceSlider(JPanel p,String ar,String en,String key,int fallback,int min,int max) {
  JPanel control=new JPanel(new BorderLayout(8,0));control.setOpaque(false);
  JSlider slider=new JSlider(min,max,(int)Math.round(RuntimeSettings.number(key,fallback/100.0,min/100.0,max/100.0)*100));slider.setName(key);slider.setPreferredSize(new Dimension(180,38));
  JLabel value=new JLabel(slider.getValue()+"%");
  slider.addChangeListener(e->{value.setText(slider.getValue()+"%");if(restoring||slider.getValueIsAdjusting())return;
   double requested=slider.getValue()/100.0;slider.setEnabled(false);saved.setText(Theme.t("جارٍ تطبيق المظهر…","Applying appearance…"));
   new SwingWorker<Void,Void>() {
    protected Void doInBackground()throws Exception{SettingsService.set(key,Double.toString(requested));return null;}
    protected void done(){try{get();saved.setText(Theme.t("تم الحفظ","Saved"));}catch(Exception ex){restore(()->slider.setValue((int)Math.round(RuntimeSettings.number(key,fallback/100.0,min/100.0,max/100.0)*100)));saved.setText(Theme.t("تعذر تطبيق المظهر؛ راجع السجل","Could not apply appearance; check logs"));}finally{slider.setEnabled(true);}}
   }.execute();
  });control.add(slider,BorderLayout.CENTER);control.add(value,BorderLayout.LINE_END);row(p,ar,en,control);
 }
 private void choice(JPanel p,String ar,String en,String key,String fallback,String[] ids,String[] arLabels,String[] enLabels) {
  JComboBox<String> input=new JComboBox<>(RuntimeSettings.arabic()?arLabels:enLabels);input.setName(key);
  input.setSelectedIndex(Math.max(0,Arrays.asList(ids).indexOf(RuntimeSettings.text(key,fallback))));
  input.addActionListener(e->{if(!save(key,ids[input.getSelectedIndex()]))restore(()->input.setSelectedIndex(Math.max(0,Arrays.asList(ids).indexOf(RuntimeSettings.text(key,fallback)))));});row(p,ar,en,input);
 }
 private void action(JPanel p,String ar,String en,Runnable task) {
  JButton button=Theme.button(Theme.t(ar,en));button.addActionListener(e->task.run());button.setAlignmentX(Component.CENTER_ALIGNMENT);
  p.add(Box.createVerticalStrut(12));p.add(button);
 }
 private void build() {
  sections.clear();pages.removeAll();
  JPanel general=section("عام","General");
  choice(general,"اللغة","Language","Language","ar",new String[]{"ar","en-GB"},new String[]{"العربية","English"},new String[]{"العربية","English"});
  toggle(general,"التصغير إلى علبة النظام","Minimize to tray","MinimizeToTray",true);
  toggle(general,"إبقاء الشخصيات في المقدمة","Always on top","AlwaysOnTop",true);
  action(general,"استعادة الإعدادات الافتراضية","Restore defaults",()->{
   if(JOptionPane.showConfirmDialog(this,Theme.t("استعادة التفضيلات الافتراضية؟ ستبقى شخصياتك ورسائلك وتقدمك وخيار بدء التشغيل محفوظة.","Restore default preferences? Your characters, messages, progress and startup choice are kept."),"ShinobiMate",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
   try{SettingsService.restoreDefaults();build();Theme.orient(this);saved.setText(Theme.t("تمت استعادة الإعدادات","Defaults restored"));}
   catch(Exception ex){saved.setText(Theme.t("تعذر استعادة الإعدادات","Could not restore defaults"));}
  });
  JPanel characters=section("الشخصيات","Characters");
  appearanceSlider(characters,"حجم الشخصية","Character size","Scaling",100,50,300);
  number(characters,"سرعة الحركة","Movement speed","MovementSpeed",1,.5,2,.1);
  number(characters,"أقصى عدد للشخصيات","Maximum active characters","MaxCharacters",20,1,100,1);
  choice(characters,"أنشطة الخمول","Idle activity","IdleActivityLevel","1",new String[]{"0","1","2"},new String[]{"هادئة","متوازنة","مرحة"},new String[]{"Calm","Balanced","Playful"});
  toggle(characters,"تعبيرات المزاج","Mood expressions","MoodEnabled",true);
  toggle(characters,"التفاعل مع الماوس","Mouse reactions","MouseInteractions",true);
  toggle(characters,"النظر نحو المؤشر","Look toward cursor","CursorTracking",true);
  toggle(characters,"اللعب مع المؤشر","Playful cursor","PlayfulCursor",false);
  toggle(characters,"التفاعلات الودية","Friendly interactions","SocialInteractions",true);
  toggle(characters,"مستويات تجميلية اختيارية","Optional cosmetic progression","ProgressionEnabled",false);
  toggle(characters,"تحديات محلية اختيارية","Optional local challenges","ChallengesEnabled",false);

  JPanel appearance=section("المظهر","Appearance");
  choice(appearance,"السمة","Theme","FlatLafTheme","light",new String[]{"light","dark"},new String[]{"فاتحة","داكنة"},new String[]{"Light","Dark"});
  appearanceSlider(appearance,"وضوح الشخصية","Character opacity","Opacity",100,20,100);
  toggle(appearance,"ظلال خفيفة","Soft shadows","Shadows",true);
  action(appearance,"الإعدادات المتقدمة القديمة","Legacy advanced settings",()->Main.getInstance().openAdvancedSettings());
  JPanel combat=section("القتال","Combat");
  toggle(combat,"تفعيل القتال اليدوي","Enable manual combat","CombatEnabled",true);
  toggle(combat,"صوت القتال","Combat sound","CombatSound",true);
  number(combat,"عدد المؤثرات المرئية","Visible effect limit","VFXLevel",48,0,48,1);
  toggle(combat,"إظهار شريط الصحة","Show health bars","ShowCombatHP",true);
  number(combat,"سرعة القتال","Combat speed","CombatSpeed",1,.5,2,.1);
  JPanel audio=section("الصوت","Audio");
  volume(audio,"الصوت الرئيسي","Master volume","MasterVolume",70);
  volume(audio,"صوت الشخصيات","Character volume","CharacterVolume",70);
  volume(audio,"صوت الحركة","Movement volume","MovementVolume",45);
  volume(audio,"صوت القتال","Combat volume","CombatVolume",60);
  volume(audio,"صوت الواجهة","UI volume","UIVolume",30);
  toggle(audio,"كتم جميع الأصوات","Mute all","Mute",false);
  JPanel cloud=section("السحابة الروحية","Spiritual Cloud");
  toggle(cloud,"تفعيل السحابة","Enable cloud","CloudEnabled",true);
  number(cloud,"فاصل الرسائل (ثانية)","Message interval (seconds)","CloudIntervalSeconds",6,3,120,1);
  number(cloud,"حجم النص","Text size","CloudTextSize",16,12,26,1);
  number(cloud,"شفافية السحابة","Cloud opacity","CloudOpacity",1,.2,1,.05);
  toggle(cloud,"تفعيل رسائلي الخاصة","Enable my custom messages","CustomCloudEnabled",true);
  action(cloud,"إضافة وتعديل رسائلي","Edit my messages",()->new CloudEditorDialog(this).setVisible(true));
  JPanel performance=section("الأداء","Performance");
  choice(performance,"نمط الأداء","Performance profile","PerformanceProfile","BALANCED",
   new String[]{"LOW","BALANCED","HIGH","CUSTOM"},new String[]{"منخفض","متوازن","مرتفع","مخصص"},new String[]{"Low","Balanced","High","Custom"});
  number(performance,"FPS في النمط المخصص","Custom render FPS","RenderFPS",25,5,25,1);
  performance.add(Theme.description(Theme.t("النمط المتوازن مناسب للاستخدام اليومي. خفض معدل الرسم يقلل استهلاك الجهاز دون تغيير قواعد القتال.","Balanced suits everyday use. A lower render rate reduces load without changing combat rules.")));
  JPanel focus=section("التركيز","Focus");
  focus.add(Theme.description(Theme.t("قلّل حركة رفاقك وأصواتهم أثناء الدراسة، مع جلسات عمل واستراحة اختيارية.","Keep companions quiet while you study, with optional work and break sessions.")));
  toggle(focus,"تذكير عند انتهاء الجلسة","Session transition reminders","FocusReminders",true);
  action(focus,"فتح جلسة التركيز","Open focus session",()->new FocusDialog(this).setVisible(true));
  JPanel start=section("بدء التشغيل","Startup");
  JCheckBox startup=new JCheckBox();startup.setName("StartWithWindows");
  startup.setSelected(com.group_finity.mascot.win.AutoStartManager.isAutoStartEnabled());
  startup.addActionListener(e->{if(!com.group_finity.mascot.win.AutoStartManager.setAutoStart(startup.isSelected())){
   startup.setSelected(!startup.isSelected());saved.setText(Theme.t("تعذر إنشاء اختصار بدء التشغيل","Could not change startup shortcut"));}});
  row(start,"بدء التشغيل مع Windows","Start with Windows",startup);

  action(start,"عرض دليل البداية","Show welcome guide",()->new OnboardingDialog(this,true).setVisible(true));
  toggle(start,"استعادة الشخصيات السابقة عند بدء التشغيل","Restore previous characters on startup",CharacterSession.RESTORE,false);
  toggle(start,"عرض معلومات الحزم","Show pack information","AlwaysShowInformationScreen",false);
  JPanel about=section("حول","About");
  about.add(Theme.title(com.group_finity.mascot.BuildInfo.NAME+" "+com.group_finity.mascot.BuildInfo.VERSION));about.add(new JLabel(Theme.t("رفيق سطح مكتب محلي • بدون حساب","Local desktop companion • no account")));
  action(about,"التحقق من التحديثات","Check for updates",()->new UpdateDialog(this).setVisible(true));

  navigation.setSelectedIndex(0);sizeSidebar();pages.revalidate();pages.repaint();
 }
}
