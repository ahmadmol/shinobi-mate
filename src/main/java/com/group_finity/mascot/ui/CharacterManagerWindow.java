package com.group_finity.mascot.ui;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.settings.RuntimeSettings;
import com.group_finity.mascot.Main;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class CharacterManagerWindow extends JFrame {
 private static CharacterManagerWindow instance;
 private static final java.util.concurrent.atomic.AtomicBoolean statusQueued=new java.util.concurrent.atomic.AtomicBoolean();
 /** Coalesce instance changes without acquiring AWT locks on the engine/loading thread. */
 public static void activeCharactersChanged(){if(statusQueued.compareAndSet(false,true))SwingUtilities.invokeLater(()->{statusQueued.set(false);for(Window w:Window.getWindows())if(w instanceof CharacterManagerWindow manager&&manager.isShowing())manager.refreshStatus();});}
 private final CharacterService service=new CharacterService();
 private final JPanel cards=new CardGrid();
 private final JTextField search=new JTextField();
 private final JComboBox<String> filter=new JComboBox<>(new String[]{Theme.t("الكل","All"),Theme.t("النشطة","Active"),Theme.t("تدعم القتال","Combat"),Theme.t("تدعم السحابة","Cloud"),Theme.t("تدعم المزاج","Mood")});
 private final JLabel summary=new JLabel();
 private final JPanel footer=new JPanel(new GridLayout(0,3,10,8));
 private java.util.List<CharacterDescriptor> installed=java.util.List.of();
 private java.util.List<String> visible=java.util.List.of();
 private final Map<String,ImageIcon> previews=new HashMap<>();
 private final Map<String,SwingWorker<ImageIcon,Void>> previewJobs=new HashMap<>();
 private long previewGeneration;
 private final Map<String,JButton> launches=new HashMap<>(),removes=new HashMap<>(),removeAll=new HashMap<>();
 private static final class CardGrid extends JPanel implements Scrollable {
  CardGrid(){super(new GridLayout(0,2,16,16));setOpaque(false);}
  public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
  public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return 24;}
  public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return Math.max(24,r.height-24);}
  public boolean getScrollableTracksViewportWidth(){return true;}
  public boolean getScrollableTracksViewportHeight(){return false;}
 }
 private final Map<String,JLabel> status=new HashMap<>();
 private final javax.swing.Timer timer=new javax.swing.Timer(750,e->refreshStatus());
 @Override public void dispose(){timer.stop();cancelPreviews();super.dispose();}
 private void cancelPreviews(){previewGeneration++;previewJobs.values().forEach(job->job.cancel(true));previewJobs.clear();}
 public static void open() {
  if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(CharacterManagerWindow::open);return;}
  com.group_finity.mascot.audio.AudioManager.play("ui.manager.open");
  if(instance==null||!instance.isDisplayable())instance=new CharacterManagerWindow();
  instance.setExtendedState(JFrame.NORMAL);instance.setVisible(true);instance.toFront();instance.requestFocus();
  instance.reload();
 }
 public CharacterManagerWindow() {
  super(Theme.t("شخصيات ShinobiMate","ShinobiMate characters"));setName("ShinobiMateCharacters");setDefaultCloseOperation(HIDE_ON_CLOSE);
  JPanel root=new JPanel(new BorderLayout(14,14));root.setBorder(new EmptyBorder(24,24,20,24));
  JPanel header=new JPanel(new BorderLayout(8,12));JPanel brand=new JPanel(new BorderLayout(8,4));brand.setOpaque(false);
  brand.add(Theme.title(Theme.t("رفاق سطح المكتب","Your desktop companions")),BorderLayout.NORTH);
  brand.add(Theme.muted(Theme.t("اختر من ترافقك. القتال يبدأ باختيارك فقط.","Choose your company. Battles begin only when you choose.")),BorderLayout.SOUTH);header.add(brand,BorderLayout.NORTH);
  JPanel toolbar=new JPanel(new BorderLayout(10,0));search.setName("CharacterSearch");search.putClientProperty("JTextField.placeholderText",Theme.t("ابحث بالاسم أو المؤلف…","Search name or author…"));search.getAccessibleContext().setAccessibleName(Theme.t("البحث عن شخصية","Search characters"));
  filter.setName("CharacterFilter");filter.getAccessibleContext().setAccessibleName(Theme.t("تصفية الشخصيات","Filter characters"));toolbar.add(search,BorderLayout.CENTER);toolbar.add(filter,BorderLayout.LINE_END);header.add(toolbar,BorderLayout.CENTER);summary.setName("CharacterSummary");header.add(summary,BorderLayout.SOUTH);root.add(header,BorderLayout.NORTH);
  search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){refreshStatus();}public void removeUpdate(javax.swing.event.DocumentEvent e){refreshStatus();}public void changedUpdate(javax.swing.event.DocumentEvent e){refreshStatus();}});
  filter.addActionListener(e->refreshStatus());
  JScrollPane scroll=new JScrollPane(cards);scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(24);root.add(scroll,BorderLayout.CENTER);
  JButton reload=Theme.button(Theme.t("تحديث القائمة","Refresh"));reload.addActionListener(e->reload());footer.add(reload);
  JButton legacy=Theme.button(Theme.t("الاختيار المتقدم القديم","Legacy chooser"));legacy.addActionListener(e->Main.getInstance().openLegacyChooser());footer.add(legacy);
  JButton settings=Theme.button(Theme.t("الإعدادات","Settings"));settings.setName("OpenSettings");settings.addActionListener(e->new SettingsDialog(this).setVisible(true));footer.add(settings);
  JButton importPack=Theme.button(Theme.t("استيراد حزمة","Import pack"));importPack.addActionListener(e->PackImportDialog.choose(this));footer.add(importPack);
  JButton focus=Theme.button(Theme.t("التركيز","Focus"));focus.addActionListener(e->new FocusDialog(this).setVisible(true));footer.add(focus);
  JButton progress=Theme.button(Theme.t("المستويات والتحديات","Progression"));progress.addActionListener(e->new ProgressionDialog(this).setVisible(true));footer.add(progress);
  root.add(footer,BorderLayout.SOUTH);setContentPane(root);reload();Theme.orient(this);Theme.fit(this,1020,780);
  cards.addComponentListener(new ComponentAdapter(){public void componentResized(ComponentEvent e){int minimum=Math.max(280,2*(getFontMetrics(UIManager.getFont("Button.font")).stringWidth(Theme.t("إزالة نسخة واحدة","Remove one"))+32)+40);int columns=Math.max(1,Math.min(3,(cards.getWidth()+16)/(minimum+16)));GridLayout layout=(GridLayout)cards.getLayout();if(layout.getColumns()!=columns){layout.setColumns(columns);((GridLayout)footer.getLayout()).setColumns(columns==1?2:3);cards.revalidate();footer.revalidate();}}});
  addWindowStateListener(e->{if((e.getNewState()&ICONIFIED)!=0&&RuntimeSettings.flag("MinimizeToTray",true)){setVisible(false);setState(NORMAL);}});
  addComponentListener(new ComponentAdapter(){public void componentShown(ComponentEvent e){timer.start();}public void componentHidden(ComponentEvent e){timer.stop();}});
  addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){timer.stop();}});
  getRootPane().registerKeyboardAction(e->search.requestFocusInWindow(),KeyStroke.getKeyStroke("control F"),JComponent.WHEN_IN_FOCUSED_WINDOW);
  getRootPane().registerKeyboardAction(e->settings.doClick(),KeyStroke.getKeyStroke("control COMMA"),JComponent.WHEN_IN_FOCUSED_WINDOW);
  getRootPane().registerKeyboardAction(e->setVisible(false),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
 }
 public void reload() {
  cancelPreviews();installed=service.installed();previews.clear();visible=null;refreshStatus();
 }
 private void showCards(java.util.List<CharacterDescriptor> packs){
  cards.removeAll();status.clear();launches.clear();removes.clear();removeAll.clear();
  if(packs.isEmpty()){JPanel empty=Theme.card();empty.add(Theme.text(Theme.t("لا توجد شخصيات مطابقة. جرّب مسح البحث أو تغيير التصفية.","No matching characters. Clear search or change the filter.")));cards.add(empty);}
  for(CharacterDescriptor pack:packs) {
   JPanel card=Theme.card();JLabel preview=new JLabel();preview.setHorizontalAlignment(SwingConstants.CENTER);preview.setAlignmentX(Component.CENTER_ALIGNMENT);
   preview.setName("Preview."+pack.id());preview.setPreferredSize(new Dimension(136,136));preview.setMinimumSize(new Dimension(136,136));
   preview.setIcon(previews.get(pack.id()));if(preview.getIcon()==null)preview.setText(Theme.t("جارٍ التحميل…","Loading…"));preview.getAccessibleContext().setAccessibleName(pack.displayName());
   if(!previews.containsKey(pack.id())&&!previewJobs.containsKey(pack.id())){
    long generation=previewGeneration;
    SwingWorker<ImageIcon,Void> job=new SwingWorker<>(){
     protected ImageIcon doInBackground(){return CharacterPreview.icon(pack,136);}
     protected void done(){if(generation!=previewGeneration)return;previewJobs.remove(pack.id());if(isCancelled())return;
      try{previews.put(pack.id(),get());}catch(Exception ex){previews.put(pack.id(),null);}
      // A filter may have replaced the original label while decoding was in flight.
      for(Component c:cards.getComponents())if(c instanceof Container card)for(Component child:card.getComponents())if(child instanceof JLabel label&&("Preview."+pack.id()).equals(label.getName())){
       label.setIcon(previews.get(pack.id()));label.setText(label.getIcon()==null?Theme.t("لا توجد معاينة","No preview"):null);
      }cards.revalidate();cards.repaint();
     }
    };previewJobs.put(pack.id(),job);job.execute();
   }else if(previews.containsKey(pack.id())&&preview.getIcon()==null)preview.setText(Theme.t("لا توجد معاينة","No preview"));
   card.add(preview);card.add(Box.createVerticalStrut(8));JLabel title=Theme.title(pack.displayName());title.setAlignmentX(Component.CENTER_ALIGNMENT);card.add(title);
   JLabel state=new JLabel();state.setName("Status."+pack.id());state.setAlignmentX(Component.CENTER_ALIGNMENT);status.put(pack.id(),state);card.add(state);card.add(Box.createVerticalStrut(10));
   JPanel controls=new JPanel(new GridLayout(0,1,8,6));controls.setOpaque(false);
   JButton launch=Theme.primary(Theme.t("تشغيل","Launch"));launch.setName("Launch."+pack.id());
   launches.put(pack.id(),launch);
   launch.addActionListener(e->{com.group_finity.mascot.audio.AudioManager.play("ui.character.select");CharacterService.launchFromMenu(pack.id());});controls.add(launch);
   JButton remove=Theme.button(Theme.t("إزالة نسخة واحدة","Remove one"));remove.setName("Remove."+pack.id());removes.put(pack.id(),remove);remove.addActionListener(e->{service.removeOne(pack.id());refreshStatus();});
   JButton all=Theme.button(Theme.t("إزالة الجميع","Remove all"));all.setName("RemoveAll."+pack.id());removeAll.put(pack.id(),all);all.addActionListener(e->{service.remove(pack.id());refreshStatus();});
   JPanel removal=new JPanel(new GridLayout(1,2,6,0));removal.setOpaque(false);removal.add(remove);removal.add(all);controls.add(removal);
   JButton details=Theme.button(Theme.t("التفاصيل","Details"));details.addActionListener(e->details(pack));controls.add(details);card.add(controls);cards.add(card);
  }
  Theme.orient(cards);cards.revalidate();cards.repaint();
 }
 private void refreshStatus(){
  var scope=CharacterFilter.Scope.values()[Math.max(0,filter.getSelectedIndex())];var matches=installed.stream().filter(p->CharacterFilter.matches(p,search.getText(),scope,service.count(p.id()))).toList();var ids=matches.stream().map(CharacterDescriptor::id).toList();
  if(!ids.equals(visible)){visible=ids;showCards(matches);}
  boolean room=service.canLaunch();status.forEach((id,label)->{int count=service.count(id);label.setText(Theme.t("مثبّت • نشط: ","Installed • Active: ")+count);launches.get(id).setText(count==0?Theme.t("تشغيل","Launch"):Theme.t("+ إضافة نسخة","+ Add instance"));launches.get(id).setEnabled(room);removes.get(id).setEnabled(count>0);removeAll.get(id).setEnabled(count>1);});
  int active=installed.stream().mapToInt(p->service.count(p.id())).sum();
  var rejected=CharacterCatalog.rejected();
  summary.setText(Theme.t("المعروضة: ","Showing: ")+matches.size()+" / "+installed.size()+Theme.t(" • نشطة: "," • Active: ")+active+(rejected.isEmpty()?"":Theme.t(" • تعذر تحميل حزم: "," • Could not load packs: ")+rejected.size()));
  summary.setToolTipText(rejected.isEmpty()?null:String.join(", ",rejected));
 }
 private void details(CharacterDescriptor p) {
  com.group_finity.mascot.audio.AudioManager.play("ui.character.select");
  JDialog d=new JDialog(this,p.displayName(),false);JPanel body=Theme.card();
  for(String text:java.util.List.of(Theme.t("الاسم: ","Name: ")+p.displayName(),Theme.t("المؤلف: ","Author: ")+p.author(),
   Theme.t("الإصدار: ","Version: ")+p.version(),Theme.t("القتال: ","Combat: ")+yes(p.combat()),Theme.t("السحابة: ","Cloud: ")+yes(p.cloud()),
   Theme.t("المزاج: ","Mood poses: ")+yes(p.mood()),Theme.t("النسخ النشطة: ","Active instances: ")+service.count(p.id()),
   p.legacy()?Theme.t("حزمة قديمة متوافقة","Compatible legacy pack"):Theme.t("حزمة شخصية مثبّتة","Installed character pack")))body.add(Theme.text(text));
  JButton reload=Theme.button(Theme.t("إعادة تحميل الحزمة","Reload pack"));reload.addActionListener(e->{reload.setEnabled(false);reload.setText(Theme.t("جارٍ التحميل…","Reloading…"));new SwingWorker<Boolean,Void>(){
   protected Boolean doInBackground(){return service.reload(p.id());}
   protected void done(){reload.setEnabled(true);try{reload.setText(get()?Theme.t("تمت إعادة التحميل","Reloaded"):Theme.t("تعذر التحميل","Reload failed"));}catch(Exception ex){reload.setText(Theme.t("تعذر التحميل","Reload failed"));}}
  }.execute();});body.add(Box.createVerticalStrut(16));body.add(reload);
  d.setContentPane(body);Theme.orient(d);Theme.fit(d,540,400);d.setVisible(true);
 }
 private static String yes(boolean b){return b?Theme.t("نعم","Yes"):Theme.t("لا","No");}
}
