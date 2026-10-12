package com.group_finity.mascot.license;

import com.group_finity.mascot.DPIManager;
import com.group_finity.mascot.Main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class LicenseActivationDialog extends JDialog {
   private static final Logger logger = Logger.getLogger(LicenseActivationDialog.class.getName());
   private JTextArea keyTextArea;
   private JButton activateButton;
   private JButton clearButton;
   private JButton cancelButton;
   private JButton deactivateButton;
   private JLabel statusLabel;
   private JLabel currentLicenseLabel;
   private JProgressBar progressBar;
   private JTextField validityField;
   private JTextArea generatorResultArea;
   private JButton generateKeyButton;
   private JTabbedPane tabbedPane;
   private boolean licenseActivated = false;
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();

   public LicenseActivationDialog(Frame parent) {
      super(parent, "License Activation", true);
      this.initComponents();
      this.applyDPIScaling();
      this.setLocationRelativeTo(parent);
      this.setDefaultCloseOperation(2);
   }

   private void initComponents() {
      this.setLayout(new BorderLayout());
      int scaledWidth = DPIManager.scaleWidth(600);
      int scaledHeight = DPIManager.scaleHeight(500);
      this.setSize(scaledWidth, scaledHeight);
      this.setLocationRelativeTo(this.getParent());
      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("CurrentLicense")));
      this.currentLicenseLabel = new JLabel();
      this.currentLicenseLabel.setFont(new Font("SansSerif", 1, DPIManager.scaleFontSize(12)));
      topPanel.add(this.currentLicenseLabel, "Center");
      this.tabbedPane = new JTabbedPane();
      JPanel activationPanel = this.createActivationPanel();
      this.tabbedPane.addTab(this.languageBundle.getString("ActivateLicense"), activationPanel);
      if (LicenseManager.getInstance().getCurrentLicenseLevel() == LicenseLevel.SPECIAL_KEY) {
         JPanel generatorPanel = this.createKeyGeneratorPanel();
         this.tabbedPane.addTab(this.languageBundle.getString("InternalKeyGenerator"), generatorPanel);
      }

      JPanel bottomPanel = this.createBottomPanel();
      this.add(topPanel, "North");
      this.add(this.tabbedPane, "Center");
      this.add(bottomPanel, "South");
      this.getRootPane().setDefaultButton(this.activateButton);
   }

   private JPanel createActivationPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      JPanel inputPanel = new JPanel(new BorderLayout());
      inputPanel.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("EnterLicenseKey")));
      this.keyTextArea = new JTextArea(6, 40);
      this.keyTextArea.setFont(new Font("Monospaced", 0, DPIManager.scaleFontSize(11)));
      this.keyTextArea.setLineWrap(true);
      this.keyTextArea.setWrapStyleWord(false);
      int scaledPadding = DPIManager.scaleWidth(5);
      this.keyTextArea.setBorder(BorderFactory.createEmptyBorder(scaledPadding, scaledPadding, scaledPadding, scaledPadding));
      JScrollPane scrollPane = new JScrollPane(this.keyTextArea);
      inputPanel.add(scrollPane, "Center");
      JPanel statusPanel = new JPanel(new BorderLayout());
      this.statusLabel = new JLabel(" ");
      this.statusLabel.setFont(new Font("SansSerif", 0, DPIManager.scaleFontSize(11)));
      this.progressBar = new JProgressBar();
      this.progressBar.setIndeterminate(false);
      this.progressBar.setVisible(false);
      statusPanel.add(this.statusLabel, "Center");
      statusPanel.add(this.progressBar, "South");
      inputPanel.add(statusPanel, "South");
      panel.add(inputPanel, "Center");
      return panel;
   }

   private JPanel createKeyGeneratorPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      JPanel controlPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      int scaledMarginLarge = DPIManager.scaleWidth(10);
      int scaledMarginSmall = DPIManager.scaleWidth(5);
      gbc.insets = new Insets(scaledMarginLarge, scaledMarginLarge, scaledMarginSmall, scaledMarginSmall);
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = 17;
      controlPanel.add(new JLabel(this.languageBundle.getString("ValidityDays") + ":"), gbc);
      this.validityField = new JTextField("30", 10);
      gbc.gridx = 1;
      gbc.fill = 2;
      controlPanel.add(this.validityField, gbc);
      this.generateKeyButton = new JButton(this.languageBundle.getString("GenerateAdvancedKey"));
      this.generateKeyButton.addActionListener(new GenerateKeyAction());
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 2;
      int scaledMarginMedium = DPIManager.scaleWidth(20);
      gbc.insets = new Insets(scaledMarginMedium, scaledMarginLarge, scaledMarginLarge, scaledMarginLarge);
      gbc.fill = 0;
      gbc.anchor = 10;
      controlPanel.add(this.generateKeyButton, gbc);
      this.generatorResultArea = new JTextArea(12, 50);
      this.generatorResultArea.setEditable(false);
      this.generatorResultArea.setFont(new Font("Monospaced", 0, DPIManager.scaleFontSize(12)));
      this.generatorResultArea.setBackground(Color.BLACK);
      this.generatorResultArea.setForeground(Color.GREEN);
      JTextArea var10000 = this.generatorResultArea;
      String var10001 = this.languageBundle.getString("InternalKeyGenerator");
      var10000.setText(var10001 + " " + this.languageBundle.getString("Generated") + "\n" + this.languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" + this.languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\nReady to generate Advanced Keys...\n");
      JScrollPane resultScrollPane = new JScrollPane(this.generatorResultArea);
      resultScrollPane.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("Generated") + " Key"));
      panel.add(controlPanel, "North");
      panel.add(resultScrollPane, "Center");
      return panel;
   }

   private JPanel createBottomPanel() {
      JPanel bottomPanel = new JPanel(new FlowLayout(1, 10, 5));
      this.activateButton = new JButton(this.languageBundle.getString("ActivateLicense"));
      this.activateButton.addActionListener(new ActivateAction());
      this.clearButton = new JButton(this.languageBundle.getString("Clear"));
      this.clearButton.addActionListener((e) -> {
         if (this.tabbedPane.getSelectedIndex() == 0) {
            this.keyTextArea.setText("");
            this.statusLabel.setText(" ");
         } else {
            JTextArea var10000 = this.generatorResultArea;
            String var10001 = this.languageBundle.getString("InternalKeyGenerator");
            var10000.setText(var10001 + " " + this.languageBundle.getString("Generated") + "\n" + this.languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" + this.languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\nReady to generate Advanced Keys...\n");
         }

      });
      JButton deactivateButton = new JButton(this.languageBundle.getString("DeactivateLicense"));
      this.deactivateButton = deactivateButton;
      deactivateButton.addActionListener((e) -> {
         int result = JOptionPane.showConfirmDialog(this, this.languageBundle.getString("DeactivateConfirmMessage"), this.languageBundle.getString("ConfirmDeactivation"), 0);
         if (result == 0) {
            LicenseManager.getInstance().clearActivation();
            this.updateCurrentLicenseDisplay();
            this.statusLabel.setText(this.languageBundle.getString("LicenseDeactivatedSuccessfully"));
            this.statusLabel.setForeground(Color.BLUE);
            if (LicenseManager.getInstance().getCurrentLicenseLevel() != LicenseLevel.SPECIAL_KEY && this.tabbedPane.getTabCount() > 1) {
               this.tabbedPane.removeTabAt(1);
            }
         }

      });
      this.cancelButton = new JButton(this.languageBundle.getString("Close"));
      this.cancelButton.addActionListener((e) -> this.dispose());
      Dimension buttonSize = new Dimension(130, 26);
      this.activateButton.setMaximumSize(buttonSize);
      this.activateButton.setMinimumSize(new Dimension(95, 23));
      this.activateButton.setPreferredSize(buttonSize);
      this.clearButton.setMaximumSize(buttonSize);
      this.clearButton.setMinimumSize(new Dimension(95, 23));
      this.clearButton.setPreferredSize(buttonSize);
      deactivateButton.setMaximumSize(buttonSize);
      deactivateButton.setMinimumSize(new Dimension(95, 23));
      deactivateButton.setPreferredSize(buttonSize);
      this.cancelButton.setMaximumSize(buttonSize);
      this.cancelButton.setMinimumSize(new Dimension(95, 23));
      this.cancelButton.setPreferredSize(buttonSize);
      bottomPanel.add(this.activateButton);
      bottomPanel.add(this.clearButton);
      bottomPanel.add(deactivateButton);
      bottomPanel.add(this.cancelButton);
      return bottomPanel;
   }

   private void updateCurrentLicenseDisplay() {
      LicenseManager manager = LicenseManager.getInstance();
      LicenseLevel level = manager.getCurrentLicenseLevel();
      String statusInfo = manager.getLicenseStatusInfo();
      this.currentLicenseLabel.setText(statusInfo);
      switch (level) {
         case NO_KEY -> this.currentLicenseLabel.setForeground(Color.GRAY);
         case ADVANCED_KEY -> this.currentLicenseLabel.setForeground(new Color(0, 150, 0));
         case SPECIAL_KEY -> this.currentLicenseLabel.setForeground(new Color(150, 0, 150));
      }

   }

   public boolean isLicenseActivated() {
      return this.licenseActivated;
   }

   public static void showDialog(Frame parent, ResourceBundle languageBundle) {
      LicenseActivationDialog dialog = new LicenseActivationDialog(parent);
      dialog.setVisible(true);
   }

   private void applyDPIScaling() {
      float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      Dimension preferredSize = this.getContentPane().getPreferredSize();
      this.getContentPane().setPreferredSize(new Dimension((int)((float)preferredSize.width * menuScaling), (int)((float)preferredSize.height * menuScaling)));
      this.activateButton.setPreferredSize(new Dimension((int)((float)this.activateButton.getPreferredSize().width * menuScaling), (int)((float)this.activateButton.getPreferredSize().height * menuScaling)));
      this.clearButton.setPreferredSize(new Dimension((int)((float)this.clearButton.getPreferredSize().width * menuScaling), (int)((float)this.clearButton.getPreferredSize().height * menuScaling)));
      this.deactivateButton.setPreferredSize(new Dimension((int)((float)this.deactivateButton.getPreferredSize().width * menuScaling), (int)((float)this.deactivateButton.getPreferredSize().height * menuScaling)));
      this.cancelButton.setPreferredSize(new Dimension((int)((float)this.cancelButton.getPreferredSize().width * menuScaling), (int)((float)this.cancelButton.getPreferredSize().height * menuScaling)));
      if (this.generateKeyButton != null) {
         this.generateKeyButton.setPreferredSize(new Dimension((int)((float)this.generateKeyButton.getPreferredSize().width * menuScaling), (int)((float)this.generateKeyButton.getPreferredSize().height * menuScaling)));
      }

      if (this.keyTextArea != null) {
         Font currentFont = this.keyTextArea.getFont();
         if (currentFont != null) {
            this.keyTextArea.setFont(currentFont.deriveFont((float)currentFont.getSize() * menuScaling));
         }
      }

      if (this.validityField != null) {
         this.validityField.setPreferredSize(new Dimension((int)((float)this.validityField.getPreferredSize().width * menuScaling), (int)((float)this.validityField.getPreferredSize().height * menuScaling)));
      }

      if (this.generatorResultArea != null) {
         Font currentFont = this.generatorResultArea.getFont();
         if (currentFont != null) {
            this.generatorResultArea.setFont(currentFont.deriveFont((float)currentFont.getSize() * menuScaling));
         }
      }

      this.pack();
   }

   private class ActivateAction implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         final String keyText = LicenseActivationDialog.this.keyTextArea.getText().trim();
         if (keyText.isEmpty()) {
            JOptionPane.showMessageDialog(LicenseActivationDialog.this, LicenseActivationDialog.this.languageBundle.getString("EnterLicenseKey"), LicenseActivationDialog.this.languageBundle.getString("InvalidInput"), 2);
            LicenseActivationDialog.this.keyTextArea.requestFocus();
         } else {
            LicenseActivationDialog.this.activateButton.setEnabled(false);
            LicenseActivationDialog.this.clearButton.setEnabled(false);
            LicenseActivationDialog.this.progressBar.setVisible(true);
            LicenseActivationDialog.this.progressBar.setIndeterminate(true);
            LicenseActivationDialog.this.statusLabel.setText(LicenseActivationDialog.this.languageBundle.getString("ValidationInProgress"));
            LicenseActivationDialog.this.statusLabel.setForeground(Color.BLUE);
            SwingWorker<Boolean, Void> worker = new SwingWorker() {
               protected Boolean doInBackground() throws Exception {
                  Thread.sleep(1000L);
                  return LicenseManager.getInstance().validateKey(keyText);
               }

               protected void done() {
                  try {
                     boolean isValid = (Boolean)this.get();
                     if (isValid) {
                        LicenseActivationDialog.this.statusLabel.setText(LicenseActivationDialog.this.languageBundle.getString("LicenseActivatedSuccessfully"));
                        LicenseActivationDialog.this.statusLabel.setForeground(new Color(0, 150, 0));
                        LicenseActivationDialog.this.licenseActivated = true;
                        LicenseActivationDialog.this.updateCurrentLicenseDisplay();
                        LicenseActivationDialog.this.keyTextArea.setText("");
                        LicenseManager manager = LicenseManager.getInstance();
                        JOptionPane.showMessageDialog(LicenseActivationDialog.this, String.format("%s\n\n%s: %s\n%s: %d", LicenseActivationDialog.this.languageBundle.getString("LicenseActivatedSuccessfully"), LicenseActivationDialog.this.languageBundle.getString("LicenseStatus"), manager.getCurrentLicenseLevel().getDisplayName(), LicenseActivationDialog.this.languageBundle.getString("DaysRemaining"), manager.getDaysRemaining()), LicenseActivationDialog.this.languageBundle.getString("Success"), 1);
                     } else {
                        LicenseActivationDialog.this.statusLabel.setText(LicenseActivationDialog.this.languageBundle.getString("InvalidLicenseKey"));
                        LicenseActivationDialog.this.statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(LicenseActivationDialog.this, LicenseActivationDialog.this.languageBundle.getString("InvalidLicenseKey") + "\n\n" + LicenseActivationDialog.this.languageBundle.getString("EnterLicenseKey"), LicenseActivationDialog.this.languageBundle.getString("InvalidLicenseKey"), 0);
                     }
                  } catch (Exception ex) {
                     LicenseActivationDialog.logger.severe("Error during license activation: " + ex.getMessage());
                     LicenseActivationDialog.this.statusLabel.setText("Error occurred during activation.");
                     LicenseActivationDialog.this.statusLabel.setForeground(Color.RED);
                     JOptionPane.showMessageDialog(LicenseActivationDialog.this, "An error occurred during license activation:\n" + ex.getMessage(), "Activation Error", 0);
                  } finally {
                     LicenseActivationDialog.this.activateButton.setEnabled(true);
                     LicenseActivationDialog.this.clearButton.setEnabled(true);
                     LicenseActivationDialog.this.progressBar.setVisible(false);
                     LicenseActivationDialog.this.progressBar.setIndeterminate(false);
                  }

               }
            };
            worker.execute();
         }
      }
   }

   private class GenerateKeyAction implements ActionListener {
      private static final String INTERNAL_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=";

      public void actionPerformed(ActionEvent e) {
         (new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
               LicenseActivationDialog.this.generateKeyButton.setEnabled(false);
               LicenseActivationDialog.this.generateKeyButton.setText(LicenseActivationDialog.this.languageBundle.getString("Generating") + "...");
            });

            try {
               int validityDays = Integer.parseInt(LicenseActivationDialog.this.validityField.getText().trim());
               if (validityDays > 0 && validityDays <= 3650) {
                  String generatedKey = this.generateAdvancedKey(validityDays);
                  SwingUtilities.invokeLater(() -> {
                     JTextArea var10000 = LicenseActivationDialog.this.generatorResultArea;
                     String var10001 = LicenseActivationDialog.this.languageBundle.getString("InternalKeyGenerator");
                     var10000.setText(var10001 + " " + LicenseActivationDialog.this.languageBundle.getString("Generated") + "\n" + LicenseActivationDialog.this.languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" + LicenseActivationDialog.this.languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\nGenerated Advanced Key:\nLevel: " + LicenseLevel.ADVANCED_KEY.getDisplayName() + "\nValid Days: " + validityDays + "\nIssue Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\nExpiry Time: " + LocalDateTime.now().plusDays((long)validityDays).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\nKey Data:\n" + generatedKey + "\n\nStatus: Key generation completed successfully! Key copied to clipboard.");

                     try {
                        StringSelection selection = new StringSelection(generatedKey);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, (ClipboardOwner)null);
                     } catch (Exception clipboardEx) {
                        LicenseActivationDialog.logger.warning("Failed to copy key to clipboard: " + clipboardEx.getMessage());
                     }

                  });
                  return;
               }

               SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LicenseActivationDialog.this, "有效期必须在1-3650天之间", "输入错误", 0));
            } catch (NumberFormatException var7) {
               SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LicenseActivationDialog.this, "请输入有效的天数", "输入错误", 0));
               return;
            } catch (Exception ex) {
               SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LicenseActivationDialog.this, "密钥生成失败: " + ex.getMessage(), "生成错误", 0));
               return;
            } finally {
               SwingUtilities.invokeLater(() -> {
                  LicenseActivationDialog.this.generateKeyButton.setEnabled(true);
                  LicenseActivationDialog.this.generateKeyButton.setText(LicenseActivationDialog.this.languageBundle.getString("GenerateAdvancedKey"));
               });
            }

         })).start();
      }

      private String generateAdvancedKey(int validityDays) throws Exception {
         LocalDateTime issueTime = LocalDateTime.now();
         String keyId = CryptoUtils.generateKeyId();
         LicenseKeyData keyData = new LicenseKeyData(LicenseLevel.ADVANCED_KEY, validityDays, issueTime, "", keyId);
         PrivateKey privateKey = CryptoUtils.stringToPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=");
         String rawData = keyData.getRawDataForSignature();
         String signature = CryptoUtils.signData(rawData, privateKey);
         String keyString = String.format("%s|%s|%d|%s|%s|%s", "1.0", LicenseLevel.ADVANCED_KEY.getCode(), validityDays, issueTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), keyId, signature);
         return Base64.getEncoder().encodeToString(keyString.getBytes());
      }
   }
}
