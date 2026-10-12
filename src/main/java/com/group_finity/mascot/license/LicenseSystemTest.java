package com.group_finity.mascot.license;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LicenseSystemTest {
   private JFrame frame;
   private JLabel statusLabel;
   private JButton generateKeyButton;
   private JButton activateButton;
   private JButton advancedFeatureButton;
   private JButton specialFeatureButton;
   private JButton statusButton;
   private JButton clearButton;
   private ResourceBundle languageBundle;
   private JTextArea currentResultArea;

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception var1) {
         }

         (new LicenseSystemTest()).createAndShowGUI();
      });
   }

   private void createAndShowGUI() {
      try {
         this.languageBundle = ResourceBundle.getBundle("language", Locale.getDefault());
      } catch (Exception var6) {
         try {
            this.languageBundle = ResourceBundle.getBundle("language", Locale.forLanguageTag("en-GB"));
         } catch (Exception var5) {
            this.languageBundle = new ResourceBundle() {
               protected Object handleGetObject(String key) {
                  String var10000;
                  switch (key) {
                     case "LicenseActivation" -> var10000 = "License Activation";
                     case "CurrentLicense" -> var10000 = "Current License Status";
                     case "EnterLicenseKey" -> var10000 = "Enter License Key";
                     case "ActivateLicense" -> var10000 = "Activate License";
                     case "DeactivateLicense" -> var10000 = "Deactivate";
                     case "Clear" -> var10000 = "Clear";
                     case "Close" -> var10000 = "Close";
                     default -> var10000 = key;
                  }

                  return var10000;
               }

               public Enumeration getKeys() {
                  return Collections.emptyEnumeration();
               }
            };
         }
      }

      this.frame = new JFrame("Shimeji-Live License System Test");
      this.frame.setDefaultCloseOperation(3);
      this.frame.setLayout(new BorderLayout());
      JPanel statusPanel = new JPanel(new BorderLayout());
      statusPanel.setBorder(BorderFactory.createTitledBorder("License Status"));
      this.statusLabel = new JLabel();
      this.updateStatusLabel();
      statusPanel.add(this.statusLabel, "Center");
      JPanel controlPanel = new JPanel(new GridLayout(3, 2, 10, 10));
      controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      this.generateKeyButton = new JButton("Generate Test Keys");
      this.generateKeyButton.addActionListener((exx) -> this.showKeyGenerationDialog());
      controlPanel.add(this.generateKeyButton);
      this.activateButton = new JButton("Activate License");
      this.activateButton.addActionListener((exx) -> {
         LicenseActivationDialog.showDialog(this.frame, this.languageBundle);
         this.updateStatusLabel();
      });
      controlPanel.add(this.activateButton);
      this.advancedFeatureButton = new JButton("Test Advanced Feature");
      this.advancedFeatureButton.addActionListener((exx) -> LicenseFeatureDemo.showAdvancedFeatureDemo(this.frame));
      controlPanel.add(this.advancedFeatureButton);
      this.specialFeatureButton = new JButton("Test Special Feature");
      this.specialFeatureButton.addActionListener((exx) -> LicenseFeatureDemo.showSpecialFeatureDemo(this.frame));
      controlPanel.add(this.specialFeatureButton);
      this.statusButton = new JButton("Show Status Info");
      this.statusButton.addActionListener((exx) -> LicenseFeatureDemo.showFeatureStatusInfo(this.frame));
      controlPanel.add(this.statusButton);
      this.clearButton = new JButton("Clear License");
      this.clearButton.addActionListener((exx) -> {
         int result = JOptionPane.showConfirmDialog(this.frame, "Are you sure you want to clear the current license?", "Confirm Clear", 0);
         if (result == 0) {
            LicenseManager.getInstance().clearActivation();
            this.updateStatusLabel();
         }

      });
      controlPanel.add(this.clearButton);
      JPanel infoPanel = new JPanel(new BorderLayout());
      infoPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
      JTextArea infoText = new JTextArea("1. Click 'Generate Test Keys' to create sample license keys\n2. Copy a key and use 'Activate License' to test activation\n3. Try 'Test Advanced Feature' and 'Test Special Feature' to see access control\n4. Use 'Show Status Info' to view detailed license information\n5. Special keys unlock the internal key generator\n\nNote: This is a demonstration of the license system functionality.");
      infoText.setEditable(false);
      infoText.setOpaque(false);
      infoText.setWrapStyleWord(true);
      infoText.setLineWrap(true);
      infoText.setFont(new Font("SansSerif", 0, 11));
      infoPanel.add(infoText, "Center");
      this.frame.add(statusPanel, "North");
      this.frame.add(controlPanel, "Center");
      this.frame.add(infoPanel, "South");
      this.frame.setSize(600, 450);
      this.frame.setLocationRelativeTo((Component)null);
      this.frame.setVisible(true);
   }

   private void updateStatusLabel() {
      LicenseManager manager = LicenseManager.getInstance();
      String statusInfo = manager.getLicenseStatusInfo();
      this.statusLabel.setText("<html><font size='4'>" + statusInfo + "</font></html>");
      LicenseLevel level = manager.getCurrentLicenseLevel();
      switch (level) {
         case NO_KEY -> this.statusLabel.setForeground(Color.GRAY);
         case ADVANCED_KEY -> this.statusLabel.setForeground(new Color(0, 150, 0));
         case SPECIAL_KEY -> this.statusLabel.setForeground(new Color(150, 0, 150));
      }

   }

   private void showKeyGenerationDialog() {
      JDialog dialog = new JDialog(this.frame, "Generate Test Keys", true);
      dialog.setLayout(new BorderLayout());
      JPanel contentPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      JLabel titleLabel = new JLabel("Test License Key Generator");
      titleLabel.setFont(new Font("SansSerif", 1, 14));
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(10, 10, 20, 10);
      contentPanel.add(titleLabel, gbc);
      gbc.gridwidth = 1;
      gbc.insets = new Insets(5, 10, 5, 5);
      gbc.gridx = 0;
      gbc.gridy = 1;
      JButton advancedKeyBtn = new JButton("Generate Advanced Key (30 days)");
      advancedKeyBtn.addActionListener((e) -> this.generateAndShowKey(LicenseLevel.ADVANCED_KEY, 30));
      contentPanel.add(advancedKeyBtn, gbc);
      gbc.gridx = 1;
      JButton advanced90Btn = new JButton("Generate Advanced Key (90 days)");
      advanced90Btn.addActionListener((e) -> this.generateAndShowKey(LicenseLevel.ADVANCED_KEY, 90));
      contentPanel.add(advanced90Btn, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      JButton specialKeyBtn = new JButton("Generate Special Key (30 days)");
      specialKeyBtn.addActionListener((e) -> this.generateAndShowKey(LicenseLevel.SPECIAL_KEY, 30));
      contentPanel.add(specialKeyBtn, gbc);
      gbc.gridx = 1;
      JButton special365Btn = new JButton("Generate Special Key (365 days)");
      special365Btn.addActionListener((e) -> this.generateAndShowKey(LicenseLevel.SPECIAL_KEY, 365));
      contentPanel.add(special365Btn, gbc);
      this.currentResultArea = new JTextArea(8, 50);
      this.currentResultArea.setEditable(false);
      this.currentResultArea.setFont(new Font("Monospaced", 0, 10));
      this.currentResultArea.setBackground(Color.BLACK);
      this.currentResultArea.setForeground(Color.GREEN);
      JScrollPane scrollPane = new JScrollPane(this.currentResultArea);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 2;
      gbc.fill = 1;
      gbc.weightx = (double)1.0F;
      gbc.weighty = (double)1.0F;
      gbc.insets = new Insets(20, 10, 10, 10);
      contentPanel.add(scrollPane, gbc);
      JPanel buttonPanel = new JPanel(new FlowLayout());
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener((e) -> {
         this.currentResultArea = null;
         dialog.dispose();
      });
      buttonPanel.add(closeButton);
      dialog.add(contentPanel, "Center");
      dialog.add(buttonPanel, "South");
      this.currentResultArea.setText("Click a button above to generate a test license key...\n\n");
      dialog.setSize(700, 500);
      dialog.setLocationRelativeTo(this.frame);
      dialog.setVisible(true);
   }

   private void generateAndShowKey(LicenseLevel level, int days) {
      try {
         String key = KeyGenerator.generateLicenseKey(level, days);
         if (this.currentResultArea != null) {
            JTextArea var10000 = this.currentResultArea;
            String var10001 = level.getDisplayName();
            var10000.append("Generated " + var10001 + " (" + days + " days):\n");
            this.currentResultArea.append(key + "\n\n");
            this.currentResultArea.append("Copy this key to test license activation.\n\n");
            this.currentResultArea.setCaretPosition(this.currentResultArea.getDocument().getLength());
         }
      } catch (Exception ex) {
         JOptionPane.showMessageDialog(this.frame, "Failed to generate key: " + ex.getMessage(), "Generation Error", 0);
      }

   }
}
