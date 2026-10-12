package com.group_finity.mascot.imagesetchooser;

import com.group_finity.mascot.DPIManager;
import com.group_finity.mascot.Main;
import com.group_finity.mascot.Manager;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class ImageSetChooser extends JDialog {
   private final ArrayList<String> imageSets = new ArrayList<>();
   private boolean closeProgram = true;
   private boolean selectAllSets = false;
   private JButton cancelButton;
   private JButton clearAllButton;
   private JButton dismissCurrentButton;
   private JButton addCharacterButton;
   private JButton reloadButton;
   private JLabel jLabel1;
   private JList<ImageSetChooserPanel> jList1;
   private JList<ImageSetChooserPanel> jList2;
   private JButton selectAllButton;
   private JButton useSelectedButton;

   public ImageSetChooser(JFrame owner, boolean modal) {
      super(owner, modal);
      DPIManager.autoConfigureDPI(Main.getInstance().getProperties());
      this.initComponents();
      this.applyArabicFont();

      com.group_finity.mascot.ui.AppIcon.apply(this);
      this.setAlwaysOnTop(true);
      com.group_finity.mascot.ui.Theme.fit(this,1000,640);
      com.group_finity.mascot.ui.Theme.orient(this);

      this.loadCharacterPacks();
   }

   public static boolean isValidCharacterPack(File dir) {
      if (dir == null || !dir.exists() || !dir.isDirectory()) {
         return false;
      }
      String name = dir.getName();
      if (name.equalsIgnoreCase("unused") || name.startsWith(".") || name.startsWith("_")) {
         return false;
      }

      File confDir = new File(dir, "conf");
      File actionsFile = new File(confDir.exists() ? confDir : dir, "actions.xml");
      File behaviorsFile = new File(confDir.exists() ? confDir : dir, "behaviors.xml");

      if (!actionsFile.exists() || !behaviorsFile.exists()) {
         System.err.println("Ignoring invalid character pack '" + name + "': missing actions.xml or behaviors.xml");
         return false;
      }

      if (!hasPngImages(dir)) {
         System.err.println("Ignoring invalid character pack '" + name + "': no PNG images found");
         return false;
      }

      return true;
   }

   private static boolean hasPngImages(File dir) {
      File[] files = dir.listFiles();
      if (files == null) return false;
      for (File f : files) {
         if (f.isFile() && f.getName().toLowerCase().endsWith(".png")) {
            return true;
         }
         if (f.isDirectory() && !f.getName().equalsIgnoreCase("conf")) {
            if (hasPngImages(f)) {
               return true;
            }
         }
      }
      return false;
   }

   public void loadCharacterPacks() {
      this.imageSets.clear();
      ArrayList<String> selectedConfigs = this.readConfigFile();
      ArrayList<ImageSetChooserPanel> list1Data = new ArrayList<>();
      ArrayList<ImageSetChooserPanel> list2Data = new ArrayList<>();
      ArrayList<Integer> selectedList1 = new ArrayList<>();
      ArrayList<Integer> selectedList2 = new ArrayList<>();

      File[] subDirs = new com.group_finity.mascot.character.CharacterCatalog().list().stream().map(p->p.root().toFile()).toArray(File[]::new);

      boolean toggle = true;

      for (File dir : subDirs) {
         if (!isValidCharacterPack(dir)) {
            continue;
         }

         String setName = dir.getName();
         File confDir = new File(dir, "conf");
         File targetConf = confDir.exists() ? confDir : dir;
         String actionsFile = new File(targetConf, "actions.xml").getAbsolutePath();
         String behaviorsFile = new File(targetConf, "behaviors.xml").getAbsolutePath();
         String infoFile = new File(targetConf, "info.xml").getAbsolutePath();
         String previewFile = new File(dir, "Preview.png").getAbsolutePath();
         String displayName = setName;

         try {
            Configuration config = new Configuration();
            if (new File(infoFile).exists()) {
               Document doc = com.group_finity.mascot.pack.SecureXml.read(java.nio.file.Path.of(infoFile));
               config.load(new Entry(doc.getDocumentElement()), setName);
            }

            if (config.containsInformationKey(config.getSchema().getString("Name"))) {
               displayName = config.getInformation(config.getSchema().getString("Name"));
            }

            if (config.containsInformationKey(config.getSchema().getString("PreviewImage"))) {
               previewFile = new File(dir, config.getInformation(config.getSchema().getString("PreviewImage"))).getAbsolutePath();
            }
         } catch (Exception ignored) {
            previewFile = new File(dir, "Preview.png").getAbsolutePath();
            displayName = setName;
         }

         ImageSetChooserPanel panel = new ImageSetChooserPanel(setName, actionsFile, behaviorsFile, previewFile, displayName);

         if (toggle) {
            toggle = false;
            list1Data.add(panel);
            if (selectedConfigs.contains(setName) || this.selectAllSets) {
               selectedList1.add(list1Data.size() - 1);
            }
         } else {
            toggle = true;
            list2Data.add(panel);
            if (selectedConfigs.contains(setName) || this.selectAllSets) {
               selectedList2.add(list2Data.size() - 1);
            }
         }

         this.imageSets.add(setName);
      }

      this.setUpList1();
      this.jList1.setListData(list1Data.toArray(new ImageSetChooserPanel[0]));
      this.jList1.setSelectedIndices(this.convertIntegers(selectedList1));

      this.setUpList2();
      this.jList2.setListData(list2Data.toArray(new ImageSetChooserPanel[0]));
      this.jList2.setSelectedIndices(this.convertIntegers(selectedList2));
   }

   public ArrayList<String> display() {
      this.setTitle(this.text("ShimejiImageSetChooser", "ShinobiMate - اختيار الشخصيات"));
      this.jLabel1.setText(this.text("SelectImageSetsToUse", "الشخصيات المتوفرة"));
      this.useSelectedButton.setText(this.text("UseSelected", "تشغيل الشخصيات المحددة"));
      this.addCharacterButton.setText(this.text("AddCharacter", "إضافة شخصية"));
      this.reloadButton.setText(this.text("ReloadCharacters", "إعادة تحميل الشخصيات"));
      this.selectAllButton.setText(this.text("SelectAll", "تحديد الكل"));
      this.clearAllButton.setText(this.text("ClearAll", "إلغاء تحديد الكل"));
      this.dismissCurrentButton.setText(this.text("DismissCurrent", "إزالة الشخصيات الحالية"));
      this.cancelButton.setText(this.text("Close", "إغلاق"));
      Main.getInstance().saveConfigFile();
      this.setVisible(true);
      return this.closeProgram ? null : this.imageSets;
   }

   private String text(String key, String fallback) {
      try {
         return Main.getInstance().getLanguageBundle().getString(key);
      } catch (Exception e) {
         return fallback;
      }
   }

   private String resolveBaseDir() {
      String dir = System.getProperty("app.dir");
      if (dir == null) dir = System.getProperty("user.dir");
      if (dir == null) dir = ".";
      return dir;
   }

   private ArrayList<String> readConfigFile() {
      ArrayList<String> list = new ArrayList<>(com.group_finity.mascot.settings.CharacterSession.snapshot(Main.getInstance().getManager()));
      this.selectAllSets = false;
      return list;
   }

   private void updateConfigFile() {
      // Selection is an explicit launch request; actual live instances update ActiveShimeji.
   }

   private void initComponents() {
      this.jList1 = new ShimejiList();
      this.jList2 = new ShimejiList();
      this.jLabel1 = new JLabel("الشخصيات المتوفرة", JLabel.RIGHT);
      this.useSelectedButton = new JButton("تشغيل الشخصيات المحددة");
      this.addCharacterButton = new JButton("إضافة شخصية");
      this.reloadButton = new JButton("إعادة تحميل الشخصيات");
      this.selectAllButton = new JButton("تحديد الكل");
      this.clearAllButton = new JButton("إلغاء تحديد الكل");
      this.dismissCurrentButton = new JButton("إزالة الشخصيات الحالية");
      this.cancelButton = new JButton("إغلاق");

      this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      this.setTitle("ShinobiMate - اختيار الشخصيات");
      this.setMinimumSize(new Dimension(980, 580));

      JPanel listPanel = new JPanel(new GridLayout(1, 2, 8, 0));
      listPanel.add(this.jList1);
      listPanel.add(this.jList2);

      JScrollPane scrollPane = new JScrollPane(listPanel);
      scrollPane.setPreferredSize(new Dimension(740, 440));

      JPanel headerPanel = new JPanel(new BorderLayout());
      headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
      this.jLabel1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      headerPanel.add(this.jLabel1, BorderLayout.CENTER);

      JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 0, 8));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 12));
      buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      buttonPanel.setPreferredSize(new Dimension(230, 440));

      this.useSelectedButton.addActionListener(this::useSelectedButtonActionPerformed);
      this.addCharacterButton.addActionListener(this::addCharacterButtonActionPerformed);
      this.reloadButton.addActionListener(this::reloadButtonActionPerformed);
      this.selectAllButton.addActionListener(this::selectAllButtonActionPerformed);
      this.clearAllButton.addActionListener(this::clearAllButtonActionPerformed);
      this.dismissCurrentButton.addActionListener(this::dismissCurrentButtonActionPerformed);
      this.cancelButton.addActionListener(this::cancelButtonActionPerformed);

      buttonPanel.add(this.useSelectedButton);
      buttonPanel.add(this.addCharacterButton);
      buttonPanel.add(this.reloadButton);
      buttonPanel.add(this.selectAllButton);
      buttonPanel.add(this.clearAllButton);
      buttonPanel.add(this.dismissCurrentButton);
      buttonPanel.add(this.cancelButton);

      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(headerPanel, BorderLayout.NORTH);
      this.getContentPane().add(scrollPane, BorderLayout.CENTER);
      this.getContentPane().add(buttonPanel, BorderLayout.EAST);
      this.pack();
      this.setSize(new Dimension(1000, 640));
   }

   private void applyArabicFont() {
      float menuScaling = 1.0F;
      try {
         menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      } catch (Exception ignored) {
      }

      float fontSize = Math.max(15.0F, Math.min(18.0F, 14.0F * menuScaling));
      Font font = new Font("Segoe UI", Font.PLAIN, Math.round(fontSize));
      this.applyFont(this.getContentPane(), font);
      this.jLabel1.setFont(font.deriveFont(Font.BOLD, fontSize + 1.0F));
   }

   private void applyFont(Component comp, Font font) {
      comp.setFont(font);
      if (comp instanceof JPanel panel) {
         for (Component child : panel.getComponents()) {
            this.applyFont(child, font);
         }
      }
   }

   private void clearAllButtonActionPerformed(ActionEvent event) {
      this.jList1.clearSelection();
      this.jList2.clearSelection();
   }

   private void selectAllButtonActionPerformed(ActionEvent event) {
      int size1 = this.jList1.getModel().getSize();
      int size2 = this.jList2.getModel().getSize();
      if (size1 > 0) this.jList1.addSelectionInterval(0, size1 - 1);
      if (size2 > 0) this.jList2.addSelectionInterval(0, size2 - 1);
   }

   private void reloadButtonActionPerformed(ActionEvent event) {
      this.loadCharacterPacks();
      JOptionPane.showMessageDialog(this,
         text("CharactersReloaded", "تم إعادة تحميل الشخصيات بنجاح"),
         text("ShimejiEE", "ShinobiMate"),
         JOptionPane.INFORMATION_MESSAGE);
   }

   private void addCharacterButtonActionPerformed(ActionEvent event) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle(text("SelectCharacterFolder", "اختر مجلد الشخصية الجديد"));
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int choice = fileChooser.showOpenDialog(this);
      if (choice == JFileChooser.APPROVE_OPTION) {
         File selectedDir = fileChooser.getSelectedFile();
         if (isValidCharacterPack(selectedDir)) {
            try {
               try(var preview=new com.group_finity.mascot.pack.SafePackImporter().previewDirectory(selectedDir.toPath(),com.group_finity.mascot.AppPaths.packs())) {preview.install();}
               loadCharacterPacks();
               JOptionPane.showMessageDialog(this,
                  text("CharactersReloaded", "تم إضافة الشخصية وإعادة تحميل القائمة بنجاح"),
                  text("ShimejiEE", "ShinobiMate"),
                  JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
               JOptionPane.showMessageDialog(this,
                  text("InvalidPack", "الحزمة غير صالحة: ") + e.getMessage(),
                  text("Error", "خطأ"),
                  JOptionPane.ERROR_MESSAGE);
            }
         } else {
            JOptionPane.showMessageDialog(this,
               text("InvalidPack", "الحزمة غير صالحة. يرجى التأكد من وجود ملفات conf والصور المطلوبة."),
               text("Error", "خطأ"),
               JOptionPane.ERROR_MESSAGE);
         }
      }
   }

   private static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
      if (sourceLocation.isDirectory()) {
         if (!targetLocation.exists()) {
            targetLocation.mkdirs();
         }
         String[] children = sourceLocation.list();
         if (children != null) {
            for (String child : children) {
               copyDirectory(new File(sourceLocation, child), new File(targetLocation, child));
            }
         }
      } else {
         Files.copy(sourceLocation.toPath(), targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private void useSelectedButtonActionPerformed(ActionEvent event) {
      this.imageSets.clear();

      for (ImageSetChooserPanel panel : this.jList1.getSelectedValuesList()) {
         this.imageSets.add(panel.getImageSetName());
      }

      for (ImageSetChooserPanel panel : this.jList2.getSelectedValuesList()) {
         this.imageSets.add(panel.getImageSetName());
      }

      if (!this.imageSets.isEmpty()) {
         this.updateConfigFile();
         this.closeProgram = false;
         this.dispose();
      }
   }

   private void dismissCurrentButtonActionPerformed(ActionEvent event) {
      this.dismissCurrentCharacters();
   }

   private void dismissCurrentCharacters() {
      Main main=Main.getInstance();main.getManager().setExitOnLastRemoved(false);
      main.getManager().disposeAll();main.activeCharactersChanged(main.getManager());
   }

   private void cancelButtonActionPerformed(ActionEvent event) {
      this.dispose();
   }

   private int[] convertIntegers(List<Integer> list) {
      int[] arr = new int[list.size()];
      for (int i = 0; i < arr.length; ++i) {
         arr[i] = list.get(i);
      }
      return arr;
   }

   private void setUpList1() {
      this.jList1.setSelectionModel(new DefaultListSelectionModel() {
         @Override
         public void setSelectionInterval(int index0, int index1) {
            if (this.isSelectedIndex(index0)) {
               super.removeSelectionInterval(index0, index1);
            } else {
               super.addSelectionInterval(index0, index1);
            }
         }
      });
   }

   private void setUpList2() {
      this.jList2.setSelectionModel(new DefaultListSelectionModel() {
         @Override
         public void setSelectionInterval(int index0, int index1) {
            if (this.isSelectedIndex(index0)) {
               super.removeSelectionInterval(index0, index1);
            } else {
               super.addSelectionInterval(index0, index1);
            }
         }
      });
   }

   public static void main(String[] args) {
      EventQueue.invokeLater(() -> {
         (new ImageSetChooser(new JFrame(), true)).display();
         System.exit(0);
      });
   }
}
