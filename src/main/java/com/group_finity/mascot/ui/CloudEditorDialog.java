package com.group_finity.mascot.ui;
import com.group_finity.mascot.cloud.CustomMessages;
import com.group_finity.mascot.Main;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
public final class CloudEditorDialog extends JDialog {
 private final DefaultListModel<CustomMessages.Entry> model=new DefaultListModel<>();
 private final JList<CustomMessages.Entry> list=new JList<>(model);
 public CloudEditorDialog(Window owner) {
  super(owner,Theme.t("رسائلي للسحابة","My cloud messages"),ModalityType.MODELESS);setName("ShinobiMateCloudEditor");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  CustomMessages.load().forEach(model::addElement);
  JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(new EmptyBorder(20,20,20,20));
  root.add(Theme.title(Theme.t("رسائلك، بلغتك","Your words, your language")),BorderLayout.NORTH);
  list.setFixedCellHeight(42);root.add(new JScrollPane(list),BorderLayout.CENTER);
  JPanel actions=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
  button(actions,"إضافة","Add",()->edit(true));button(actions,"تعديل","Edit",()->edit(false));
  button(actions,"حذف","Delete",()->{int i=list.getSelectedIndex();if(i>=0){model.remove(i);persist();}});
  button(actions,"تفعيل / تعطيل","Enable / disable",()->{int i=list.getSelectedIndex();if(i>=0){var e=model.get(i);model.set(i,new CustomMessages.Entry(e.id(),e.text(),!e.enabled()));persist();}});
  root.add(actions,BorderLayout.SOUTH);setContentPane(root);Theme.orient(this);Theme.fit(this,720,460);
 }
 private void button(JPanel p,String ar,String en,Runnable task){JButton b=Theme.button(Theme.t(ar,en));b.addActionListener(e->task.run());p.add(b);}
 private void edit(boolean add) {
  int index=list.getSelectedIndex();if(!add&&index<0)return;
  JTextArea text=new JTextArea(add?"":model.get(index).text(),5,35);text.setLineWrap(true);text.setWrapStyleWord(true);Theme.orient(text);
  if(JOptionPane.showConfirmDialog(this,new JScrollPane(text),Theme.t("رسالة (حتى 500 حرف)","Message (up to 500 characters)"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
  try {
   int id=add?Collections.list(model.elements()).stream().mapToInt(CustomMessages.Entry::id).max().orElse(0)+1:model.get(index).id();
   var entry=new CustomMessages.Entry(id,text.getText(),add||model.get(index).enabled());
   if(add)model.addElement(entry);else model.set(index,entry);persist();
  }catch(IllegalArgumentException ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
 }
 private void persist(){
  try{CustomMessages.save(Collections.list(model.elements()));Main.getInstance().reloadAllCloudMessages();}
  catch(Exception ex){model.clear();CustomMessages.load().forEach(model::addElement);JOptionPane.showMessageDialog(this,Theme.t("تعذر الحفظ: ","Could not save: ")+ex.getMessage());}
 }
}
