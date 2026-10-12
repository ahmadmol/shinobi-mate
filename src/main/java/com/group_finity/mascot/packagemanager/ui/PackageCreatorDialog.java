package com.group_finity.mascot.packagemanager.ui;

import com.group_finity.mascot.DPIManager;
import com.group_finity.mascot.Main;
import com.group_finity.mascot.packagemanager.MascotPackageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

public class PackageCreatorDialog extends JDialog {
   private JTextField nameField;
   private JTextField authorField;
   private JTextArea descriptionArea;
   private JTextField versionField;
   private JLabel mascotFolderLabel;
   private JLabel languageFilesLabel;
   private JButton createButton;
   private JButton cancelButton;
   private JProgressBar progressBar;
   private JLabel statusLabel;
   private File selectedMascotFolder;
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();

   public PackageCreatorDialog(Frame parent) {
      super(parent, "Package Creator", true);
      this.initComponents();
      this.applyDPIScaling();
      this.setLocationRelativeTo(parent);
   }

   private void initComponents() {
      this.setLayout(new BorderLayout());
      int scaledWidth = DPIManager.scaleWidth(650);
      int scaledHeight = DPIManager.scaleHeight(480);
      this.setSize(scaledWidth, scaledHeight);
      JPanel mainPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = 17;
      gbc.gridx = 0;
      gbc.gridy = 0;
      mainPanel.add(new JLabel(this.languageBundle.getString("MascotFolder")), gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.mascotFolderLabel = new JLabel(this.languageBundle.getString("NotSelected"));
      this.mascotFolderLabel.setBorder(BorderFactory.createEtchedBorder());
      this.mascotFolderLabel.setPreferredSize(new Dimension(300, 25));
      this.mascotFolderLabel.setOpaque(true);
      this.mascotFolderLabel.setBackground(Color.WHITE);
      mainPanel.add(this.mascotFolderLabel, gbc);
      gbc.gridx = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      JButton browseFolderButton = new JButton(this.languageBundle.getString("Browse"));
      browseFolderButton.addActionListener(this::browseMascotFolder);
      mainPanel.add(browseFolderButton, gbc);
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weightx = (double)0.0F;
      mainPanel.add(new JLabel(this.languageBundle.getString("PackageName")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.nameField = new JTextField();
      mainPanel.add(this.nameField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 1;
      gbc.weightx = (double)0.0F;
      mainPanel.add(new JLabel(this.languageBundle.getString("Author")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.weightx = (double)1.0F;
      this.authorField = new JTextField();
      mainPanel.add(this.authorField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.weightx = (double)0.0F;
      mainPanel.add(new JLabel(this.languageBundle.getString("Version")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.weightx = (double)1.0F;
      this.versionField = new JTextField("1.0");
      mainPanel.add(this.versionField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.gridwidth = 1;
      gbc.weightx = (double)0.0F;
      mainPanel.add(new JLabel(this.languageBundle.getString("Description")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.weightx = (double)1.0F;
      gbc.fill = 1;
      gbc.weighty = 0.3;
      this.descriptionArea = new JTextArea(3, 30);
      this.descriptionArea.setLineWrap(true);
      this.descriptionArea.setWrapStyleWord(true);
      JScrollPane descScrollPane = new JScrollPane(this.descriptionArea);
      mainPanel.add(descScrollPane, gbc);
      gbc.gridx = 0;
      gbc.gridy = 5;
      gbc.gridwidth = 1;
      gbc.weightx = (double)0.0F;
      gbc.weighty = (double)0.0F;
      mainPanel.add(new JLabel(this.languageBundle.getString("Localization")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.weightx = (double)1.0F;
      gbc.fill = 2;
      this.languageFilesLabel = new JLabel(this.languageBundle.getString("SelectMascotFolderFirst"));
      this.languageFilesLabel.setBorder(BorderFactory.createEtchedBorder());
      this.languageFilesLabel.setOpaque(true);
      this.languageFilesLabel.setBackground(Color.WHITE);
      mainPanel.add(this.languageFilesLabel, gbc);
      gbc.gridx = 0;
      gbc.gridy = 6;
      gbc.gridwidth = 3;
      gbc.weightx = (double)1.0F;
      gbc.fill = 2;
      this.statusLabel = new JLabel(this.languageBundle.getString("Ready"));
      this.statusLabel.setHorizontalAlignment(0);
      this.statusLabel.setFont(new Font("SansSerif", 0, 12));
      this.statusLabel.setForeground(Color.GRAY);
      mainPanel.add(this.statusLabel, gbc);
      gbc.gridx = 0;
      gbc.gridy = 7;
      gbc.gridwidth = 3;
      gbc.weightx = (double)1.0F;
      gbc.fill = 2;
      this.progressBar = new JProgressBar(0, 100);
      this.progressBar.setStringPainted(true);
      this.progressBar.setString("Ready to create package...");
      this.progressBar.setVisible(false);
      mainPanel.add(this.progressBar, gbc);
      JPanel buttonPanel = new JPanel(new FlowLayout());
      this.createButton = new JButton(this.languageBundle.getString("CreatePackage"));
      this.createButton.setEnabled(false);
      this.createButton.addActionListener(this::createPackage);
      this.cancelButton = new JButton(this.languageBundle.getString("Cancel"));
      this.cancelButton.addActionListener((e) -> this.dispose());
      buttonPanel.add(this.createButton);
      buttonPanel.add(this.cancelButton);
      this.add(mainPanel, "Center");
      this.add(buttonPanel, "South");
   }

   private void browseMascotFolder(ActionEvent e) {
      JFileChooser fileChooser = new JFileChooser("img");
      fileChooser.setFileSelectionMode(1);
      fileChooser.setDialogTitle("选择桌宠文件夹 / Select Mascot Folder");
      if (fileChooser.showOpenDialog(this) == 0) {
         this.selectedMascotFolder = fileChooser.getSelectedFile();
         this.mascotFolderLabel.setText(this.selectedMascotFolder.getName());
         if (this.nameField.getText().isEmpty()) {
            this.nameField.setText(this.selectedMascotFolder.getName());
         }

         this.scanAndDisplayLanguageFiles();
         this.createButton.setEnabled(true);
      }

   }

   private void scanAndDisplayLanguageFiles() {
      if (this.selectedMascotFolder != null) {
         File confFolder = new File(this.selectedMascotFolder, "conf");
         List<String> languageFiles = new ArrayList();
         List<String> supportedLanguages = new ArrayList();
         if (confFolder.exists() && confFolder.isDirectory()) {
            File[] files = confFolder.listFiles((dir, name) -> name.endsWith(".properties"));
            if (files != null) {
               for(File file : files) {
                  languageFiles.add(file.getName());
                  if (file.getName().startsWith("language_")) {
                     String langCode = file.getName().substring(9, file.getName().length() - 11);
                     supportedLanguages.add(langCode);
                  } else if (file.getName().equals("language.properties")) {
                     supportedLanguages.add("default");
                  }
               }
            }
         }

         if (languageFiles.isEmpty()) {
            this.languageFilesLabel.setText("未找到本地化文件 / No localization files found");
            this.languageFilesLabel.setForeground(Color.ORANGE);
         } else {
            this.languageFilesLabel.setText(String.format("找到 %d 个语言文件，支持: %s / Found %d language files, supporting: %s", languageFiles.size(), String.join(", ", supportedLanguages), languageFiles.size(), String.join(", ", supportedLanguages)));
            this.languageFilesLabel.setForeground(Color.BLUE);
         }

      }
   }

   private void createPackage(ActionEvent e) {
      if (this.selectedMascotFolder != null && !this.nameField.getText().trim().isEmpty()) {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
               return f.isDirectory() || f.getName().toLowerCase().endsWith(".smp");
            }

            public String getDescription() {
               return "Shimeji Mascot Package (*.smp)";
            }
         });
         String var10000 = this.nameField.getText().trim();
         String defaultFileName = var10000.replaceAll("[^a-zA-Z0-9_-]", "") + ".smp";
         fileChooser.setSelectedFile(new File(defaultFileName));
         if (fileChooser.showSaveDialog(this) == 0) {
            File selectedFile = fileChooser.getSelectedFile();
            final File outputFile = selectedFile.getName().toLowerCase().endsWith(".smp") ? selectedFile : new File(selectedFile.getParentFile(), selectedFile.getName() + ".smp");
            this.createButton.setEnabled(false);
            this.cancelButton.setEnabled(false);
            this.progressBar.setVisible(true);
            this.statusLabel.setText("正在创建包... / Creating package...");
            this.statusLabel.setForeground(Color.BLUE);
            this.progressBar.setValue(0);
            this.progressBar.setString("Initializing...");
            SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
               protected Boolean doInBackground() throws Exception {
                  this.publish(10);
                  Thread.sleep(100L);
                  this.publish(30);
                  Thread.sleep(100L);
                  boolean result = MascotPackageManager.createPackage(PackageCreatorDialog.this.selectedMascotFolder, outputFile, PackageCreatorDialog.this.nameField.getText().trim(), PackageCreatorDialog.this.authorField.getText().trim(), PackageCreatorDialog.this.descriptionArea.getText().trim());
                  this.publish(90);
                  Thread.sleep(100L);
                  this.publish(100);
                  return result;
               }

               protected void process(List<Integer> chunks) {
                  for(Integer progress : chunks) {
                     PackageCreatorDialog.this.progressBar.setValue(progress);
                     if (progress < 30) {
                        PackageCreatorDialog.this.progressBar.setString("Scanning files...");
                     } else if (progress < 60) {
                        PackageCreatorDialog.this.progressBar.setString("Creating package...");
                     } else if (progress < 90) {
                        PackageCreatorDialog.this.progressBar.setString("Writing data...");
                     } else {
                        PackageCreatorDialog.this.progressBar.setString("Finalizing...");
                     }
                  }

               }

               protected void done() {
                  try {
                     boolean success = this.get();
                     if (success) {
                        PackageCreatorDialog.this.statusLabel.setText("创建成功 / Creation Successful");
                        PackageCreatorDialog.this.statusLabel.setForeground(new Color(0, 150, 0));
                        PackageCreatorDialog.this.progressBar.setString("Package Created!");
                        JOptionPane.showMessageDialog(PackageCreatorDialog.this, "桌宠包创建成功！\nMascot package created successfully!\n\n文件位置 / File location: " + outputFile.getAbsolutePath(), "创建成功 / Creation Success", 1);
                        Timer timer = new Timer(1000, (evt) -> PackageCreatorDialog.this.dispose());
                        timer.setRepeats(false);
                        timer.start();
                     } else {
                        PackageCreatorDialog.this.statusLabel.setText("创建失败 / Creation Failed");
                        PackageCreatorDialog.this.statusLabel.setForeground(Color.RED);
                        PackageCreatorDialog.this.progressBar.setString("Creation Failed");
                        JOptionPane.showMessageDialog(PackageCreatorDialog.this, "创建桌宠包失败 / Failed to create mascot package", "创建错误 / Creation Error", 0);
                     }
                  } catch (Exception ex) {
                     PackageCreatorDialog.this.statusLabel.setText("创建出错 / Creation Error");
                     PackageCreatorDialog.this.statusLabel.setForeground(Color.RED);
                     PackageCreatorDialog.this.progressBar.setString("Error occurred");
                     PackageCreatorDialog var10000 = PackageCreatorDialog.this;
                     String var10001 = ex.getMessage();
                     JOptionPane.showMessageDialog(var10000, "创建过程中发生错误: " + var10001 + "\nError during creation: " + ex.getMessage(), "创建错误 / Creation Error", 0);
                  } finally {
                     PackageCreatorDialog.this.createButton.setEnabled(true);
                     PackageCreatorDialog.this.cancelButton.setEnabled(true);
                  }

               }
            };
            worker.execute();
         }

      } else {
         JOptionPane.showMessageDialog(this, "请填写必需的信息 / Please fill in required information", "信息不完整 / Incomplete Information", 2);
      }
   }

   private void applyDPIScaling() {
   }

   public static void showCreatorDialog(Frame parent) {
      PackageCreatorDialog dialog = new PackageCreatorDialog(parent);
      dialog.setVisible(true);
   }
}
