package com.group_finity.mascot.packagemanager.ui;

import com.group_finity.mascot.DPIManager;
import com.group_finity.mascot.Main;
import com.group_finity.mascot.packagemanager.MascotPackage;
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
import java.awt.event.ActionListener;
import java.io.File;
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
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

public class PackageInstallDialog extends JDialog {
   private JProgressBar progressBar;
   private JButton installButton;
   private JButton cancelButton;
   private JLabel statusLabel;
   private JLabel packageFileLabel;
   private JTextArea packageInfoArea;
   private File selectedPackage;
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();

   public PackageInstallDialog(Frame parent) {
      super(parent, "Install Mascot Package", true);
      this.initComponents();
      this.applyDPIScaling();
      this.setLocationRelativeTo(parent);
   }

   private void initComponents() {
      this.setLayout(new BorderLayout());
      JPanel mainPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(8, 8, 8, 8);
      gbc.anchor = 17;
      gbc.gridx = 0;
      gbc.gridy = 0;
      mainPanel.add(new JLabel(this.languageBundle.getString("PackageFile")), gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.packageFileLabel = new JLabel(this.languageBundle.getString("NotSelected"));
      this.packageFileLabel.setBorder(BorderFactory.createEtchedBorder());
      this.packageFileLabel.setPreferredSize(new Dimension(400, 25));
      this.packageFileLabel.setOpaque(true);
      this.packageFileLabel.setBackground(Color.WHITE);
      mainPanel.add(this.packageFileLabel, gbc);
      gbc.gridx = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      JButton browseButton = new JButton(this.languageBundle.getString("Browse"));
      browseButton.addActionListener((e) -> {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
               return f.isDirectory() || f.getName().toLowerCase().endsWith(".smp");
            }

            public String getDescription() {
               return "Shimeji Mascot Package (*.smp)";
            }
         });
         if (fileChooser.showOpenDialog(this) == 0) {
            this.selectedPackage = fileChooser.getSelectedFile();
            this.packageFileLabel.setText(this.selectedPackage.getName());
            this.packageFileLabel.setToolTipText(this.selectedPackage.getAbsolutePath());
            this.installButton.setEnabled(true);

            try {
               MascotPackage packageInfo = MascotPackageManager.readPackageInfo(this.selectedPackage);
               StringBuilder info = new StringBuilder();
               info.append(this.languageBundle.getString("Name")).append(": ").append(packageInfo.getName()).append("\n");
               info.append(this.languageBundle.getString("Version")).append(": ").append(packageInfo.getVersion()).append("\n");
               info.append(this.languageBundle.getString("Author")).append(": ").append(packageInfo.getAuthor()).append("\n");
               info.append(this.languageBundle.getString("Description")).append(": ").append(packageInfo.getDescription()).append("\n");
               info.append(this.languageBundle.getString("Created")).append(": ").append(packageInfo.getCreateTime()).append("\n");
               this.packageInfoArea.setText(info.toString());
               this.statusLabel.setText(this.languageBundle.getString("PackageSelectedClickInstall"));
               this.statusLabel.setForeground(new Color(0, 120, 0));
            } catch (Exception ex) {
               JTextArea var10000 = this.packageInfoArea;
               String var10001 = this.languageBundle.getString("ErrorReadingPackage");
               var10000.setText(var10001 + "\n\n" + ex.getMessage());
               this.statusLabel.setText(this.languageBundle.getString("PackageFileCorrupted"));
               this.statusLabel.setForeground(Color.RED);
            }
         }

      });
      mainPanel.add(browseButton, gbc);
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.weightx = (double)0.0F;
      gbc.fill = 0;
      gbc.weighty = (double)0.0F;
      gbc.insets = new Insets(15, 5, 5, 5);
      mainPanel.add(new JLabel(this.languageBundle.getString("PackageInfo")), gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.weightx = (double)1.0F;
      gbc.fill = 1;
      gbc.weighty = 0.3;
      this.packageInfoArea = new JTextArea(4, 40);
      this.packageInfoArea.setEditable(false);
      this.packageInfoArea.setBackground(new Color(245, 245, 245));
      this.packageInfoArea.setText(this.languageBundle.getString("SelectPackageForDetails"));
      this.packageInfoArea.setBorder(BorderFactory.createLoweredBevelBorder());
      JScrollPane packageInfoScrollPane = new JScrollPane(this.packageInfoArea);
      mainPanel.add(packageInfoScrollPane, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 3;
      gbc.weightx = (double)1.0F;
      gbc.weighty = (double)0.0F;
      gbc.fill = 2;
      gbc.insets = new Insets(15, 5, 5, 5);
      this.statusLabel = new JLabel(this.languageBundle.getString("SelectPackageToInstall"));
      this.statusLabel.setHorizontalAlignment(0);
      this.statusLabel.setFont(new Font("SansSerif", 0, 12));
      this.statusLabel.setForeground(Color.GRAY);
      mainPanel.add(this.statusLabel, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 3;
      gbc.fill = 2;
      gbc.insets = new Insets(5, 5, 15, 5);
      this.progressBar = new JProgressBar(0, 100);
      this.progressBar.setStringPainted(true);
      this.progressBar.setString("Ready to install...");
      this.progressBar.setVisible(false);
      mainPanel.add(this.progressBar, gbc);
      JPanel buttonPanel = new JPanel(new FlowLayout());
      this.installButton = new JButton(this.languageBundle.getString("Install"));
      this.installButton.setEnabled(false);
      this.installButton.addActionListener(new InstallAction());
      this.cancelButton = new JButton(this.languageBundle.getString("Cancel"));
      this.cancelButton.addActionListener((e) -> this.dispose());
      buttonPanel.add(this.installButton);
      buttonPanel.add(this.cancelButton);
      this.add(mainPanel, "Center");
      this.add(buttonPanel, "South");
   }

   private void applyDPIScaling() {
      int scaledWidth = DPIManager.scaleWidth(650);
      int scaledHeight = DPIManager.scaleHeight(480);
      this.setSize(scaledWidth, scaledHeight);
   }

   public static void showInstallDialog(Frame parent) {
      PackageInstallDialog dialog = new PackageInstallDialog(parent);
      dialog.setVisible(true);
   }

   private class InstallAction implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         if (PackageInstallDialog.this.selectedPackage != null && PackageInstallDialog.this.selectedPackage.exists()) {
            PackageInstallDialog.this.installButton.setEnabled(false);
            PackageInstallDialog.this.cancelButton.setEnabled(false);
            PackageInstallDialog.this.progressBar.setVisible(true);
            PackageInstallDialog.this.statusLabel.setText("正在准备安装... / Preparing installation...");
            PackageInstallDialog.this.statusLabel.setForeground(Color.BLUE);
            SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
               protected Boolean doInBackground() throws Exception {
                  Thread.sleep(200L);
                  return MascotPackageManager.installPackage(PackageInstallDialog.this.selectedPackage, PackageInstallDialog.this.progressBar);
               }

               protected void process(List<String> chunks) {
                  for(String message : chunks) {
                     PackageInstallDialog.this.statusLabel.setText(message);
                  }

               }

               protected void done() {
                  try {
                     boolean success = this.get();
                     if (success) {
                        PackageInstallDialog.this.statusLabel.setText(PackageInstallDialog.this.languageBundle.getString("InstallationSuccessful"));
                        PackageInstallDialog.this.statusLabel.setForeground(new Color(0, 150, 0));
                        PackageInstallDialog.this.progressBar.setString("Installation Complete!");
                        Timer timer = new Timer(1500, (evt) -> PackageInstallDialog.this.dispose());
                        timer.setRepeats(false);
                        timer.start();
                     } else {
                        PackageInstallDialog.this.statusLabel.setText(PackageInstallDialog.this.languageBundle.getString("InstallationFailed"));
                        PackageInstallDialog.this.statusLabel.setForeground(Color.RED);
                        PackageInstallDialog.this.progressBar.setString("Installation Failed");
                     }
                  } catch (Exception ex) {
                     PackageInstallDialog.this.statusLabel.setText(PackageInstallDialog.this.languageBundle.getString("InstallationError"));
                     PackageInstallDialog.this.statusLabel.setForeground(Color.RED);
                     PackageInstallDialog.this.progressBar.setString("Error occurred");
                     JOptionPane.showMessageDialog(PackageInstallDialog.this, PackageInstallDialog.this.languageBundle.getString("ErrorDuringInstallation") + ": " + ex.getMessage(), PackageInstallDialog.this.languageBundle.getString("InstallationError"), 0);
                  } finally {
                     PackageInstallDialog.this.installButton.setEnabled(true);
                     PackageInstallDialog.this.cancelButton.setEnabled(true);
                  }

               }
            };
            worker.execute();
         } else {
            JOptionPane.showMessageDialog(PackageInstallDialog.this, PackageInstallDialog.this.languageBundle.getString("SelectValidPackageFile"), PackageInstallDialog.this.languageBundle.getString("Error"), 0);
         }
      }
   }
}
