package com.group_finity.mascot.ui;
import com.group_finity.mascot.character.*;
import com.group_finity.mascot.settings.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** First run chooses a language, then hands off to the manager. The optional help replay has five pages. */
public final class OnboardingDialog extends JDialog {
 private final JPanel page=new JPanel(new BorderLayout(16,16));
 private final JLabel progress=new JLabel();
 private final JButton next=Theme.button("");
 private final JButton back=Theme.button("");
 private final List<CharacterDescriptor> packs;
 private final boolean replay;
 private int index;private String selected;private boolean complete;
 public OnboardingDialog(Window owner,boolean replay) {
  super(owner,"ShinobiMate",ModalityType.APPLICATION_MODAL);this.replay=replay;setName("ShinobiMateOnboarding");
  packs=replay?new CharacterCatalog().list():List.of();
  selected=packs.isEmpty()?null:packs.getFirst().id();setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  String active=RuntimeSettings.text("ActiveShimeji","").split("/",2)[0];if(packs.stream().anyMatch(p->p.id().equals(active)))selected=active;
  JPanel root=new JPanel(new BorderLayout(16,16));root.setBorder(new EmptyBorder(24,24,24,24));
  root.add(progress,BorderLayout.NORTH);root.add(page,BorderLayout.CENTER);
  JPanel footer=new JPanel(new FlowLayout(FlowLayout.TRAILING,12,0));back.setName("OnboardingBack");
  back.addActionListener(e->{if(index>0){index--;showPage();}});footer.add(back);
  next.setName("OnboardingNext");next.addActionListener(e->{if(replay&&index<4){index++;showPage();}else finish();});footer.add(next);
  root.add(footer,BorderLayout.SOUTH);setContentPane(root);showPage();Theme.fit(this,650,500);
 }
 public int pageIndex(){return index;}
 public boolean completed(){return complete;}
 private void showPage() {
  page.removeAll();progress.setText((index+1)+" / "+(replay?5:1));Theme.orient(this);
  String[] ar={"مرحبًا بك في ShinobiMate","اختر رفيقك الأول","التفاعل مع شخصيتك","السحابة الروحية","القتال اليدوي"};
  String[] en={"Welcome to ShinobiMate","Choose your first companion","Meet your character","Spiritual Cloud","Manual combat"};
  page.add(Theme.title(RuntimeSettings.arabic()?ar[index]:en[index]),BorderLayout.NORTH);
  JPanel content=Theme.card();
  if(index==0) {
   JComboBox<String> language=new JComboBox<>(new String[]{"العربية","English"});language.setName("OnboardingLanguage");language.setSelectedIndex(RuntimeSettings.arabic()?0:1);
   language.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
   language.setAlignmentX(Component.CENTER_ALIGNMENT);
   language.addActionListener(e->{try{SettingsService.set("Language",language.getSelectedIndex()==0?"ar":"en-GB");showPage();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}});
   content.add(language);
  } else if(index==1) {
   JLabel preview=new JLabel();preview.setName("OnboardingPreview");preview.setAlignmentX(Component.CENTER_ALIGNMENT);
   java.util.function.Consumer<String> showPreview=id->{var pack=packs.stream().filter(p->p.id().equals(id)).findFirst().orElse(null);preview.setIcon(pack==null?null:CharacterPreview.icon(pack,160));preview.getAccessibleContext().setAccessibleName(pack==null?"":pack.displayName());};
   showPreview.accept(selected);content.add(preview);content.add(Box.createVerticalStrut(14));
   JComboBox<String> choice=new JComboBox<>(packs.stream().map(CharacterDescriptor::displayName).toArray(String[]::new));choice.setName("FirstCharacter");
   choice.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
   choice.setAlignmentX(Component.CENTER_ALIGNMENT);
   for(int i=0;i<packs.size();i++)if(packs.get(i).id().equals(selected))choice.setSelectedIndex(i);
   choice.addActionListener(e->{selected=packs.get(choice.getSelectedIndex()).id();showPreview.accept(selected);});content.add(choice);
   JLabel hint=new JLabel(Theme.t("يمكنك إضافة المزيد من مدير الشخصيات لاحقًا","You can add more from the character manager later"));hint.setAlignmentX(Component.CENTER_ALIGNMENT);content.add(hint);
  } else {
   String[] descriptions={
    "","",
    Theme.t("نقرة يسار: تثبيت السحابة أو إخفاؤها. السحب: نقل الشخصية. الزر الأيمن: قائمة الخيارات.","Left click: pin or hide the cloud. Drag: move the character. Right click: open the menu."),
    Theme.t("رسائل لطيفة تظهر عند المرور بالمؤشر. يمكنك تغيير توقيتها أو تعطيلها من الإعدادات.","Gentle messages appear on hover. Change their timing or disable them in settings."),
    Theme.t("زر أيمن ← القتال ← اختيار خصم. تستطيع إيقاف القتال أو طلب إعادة المباراة. لا يبدأ قتال تلقائيًا.","Right click → Combat → Choose opponent. Stop or rematch from the same menu. Battles start only when you choose.")};
   JTextArea text=new JTextArea(descriptions[index]);text.setEditable(false);text.setLineWrap(true);text.setWrapStyleWord(true);text.setOpaque(false);text.setFont(new Font("Segoe UI",Font.PLAIN,19));content.add(text);
  }
  page.add(content,BorderLayout.CENTER);next.setText(!replay?Theme.t("فتح مدير الشخصيات","Open Character Manager"):index==4?Theme.t("ابدأ استخدام ShinobiMate","Start using ShinobiMate"):Theme.t("التالي","Next"));
  next.setEnabled(true);Theme.orient(page);page.revalidate();page.repaint();
  back.setText(Theme.t("السابق","Back"));back.setEnabled(index>0);
 }
 private void finish(){
  try {
   SettingsService.set("OnboardingComplete","true");complete=true;dispose();
  }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
 }
}
