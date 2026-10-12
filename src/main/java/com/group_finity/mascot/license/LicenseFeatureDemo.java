package com.group_finity.mascot.license;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LicenseFeatureDemo {
   public static void showAdvancedFeatureDemo(Frame parent) {
      if (LicenseChecker.checkAdvancedFeature(true)) {
         JOptionPane.showMessageDialog(parent, "\ud83c\udf89 Advanced Feature Unlocked!\n\nThis is an example of an advanced feature that requires\nan Advanced or Special license to access.\n\nAdvanced features might include:\n• Custom mascot behaviors\n• Enhanced animations\n• Advanced settings\n• Export/Import configurations\n• And much more!", "Advanced Feature Demo", 1);
         LicenseChecker.logFeatureUsage("AdvancedFeatureDemo", LicenseLevel.ADVANCED_KEY);
      }
   }

   public static void showSpecialFeatureDemo(Frame parent) {
      if (LicenseChecker.checkSpecialFeature(true)) {
         JDialog dialog = new JDialog(parent, "Special Feature Demo", true);
         dialog.setLayout(new BorderLayout());
         JPanel contentPanel = new JPanel(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         JLabel titleLabel = new JLabel("\ud83d\udd25 Special Feature Activated!");
         titleLabel.setFont(new Font("SansSerif", 1, 16));
         titleLabel.setForeground(new Color(150, 0, 150));
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.insets = new Insets(20, 20, 10, 20);
         contentPanel.add(titleLabel, gbc);
         JTextArea textArea = new JTextArea("Congratulations! You have access to special features!\n\nSpecial license features include:\n• All Advanced features\n• Developer tools and debugging\n• Internal key generation capabilities\n• Beta feature access\n• Priority support\n\nYou can now generate new Advanced keys for distribution!");
         textArea.setEditable(false);
         textArea.setOpaque(false);
         textArea.setWrapStyleWord(true);
         textArea.setLineWrap(true);
         gbc.gridy = 1;
         gbc.fill = 1;
         gbc.weightx = (double)1.0F;
         gbc.weighty = (double)1.0F;
         contentPanel.add(textArea, gbc);
         JPanel buttonPanel = new JPanel(new FlowLayout());
         JButton keyGenButton = new JButton("Open Key Generator");
         keyGenButton.addActionListener((e) -> {
            dialog.dispose();
            InternalKeyGenerator.showKeyGenerator();
         });
         buttonPanel.add(keyGenButton);
         JButton closeButton = new JButton("Close");
         closeButton.addActionListener((e) -> dialog.dispose());
         buttonPanel.add(closeButton);
         dialog.add(contentPanel, "Center");
         dialog.add(buttonPanel, "South");
         dialog.setSize(400, 300);
         dialog.setLocationRelativeTo(parent);
         dialog.setVisible(true);
         LicenseChecker.logFeatureUsage("SpecialFeatureDemo", LicenseLevel.SPECIAL_KEY);
      }
   }

   public static void showFeatureStatusInfo(Frame parent) {
      LicenseManager manager = LicenseManager.getInstance();
      StringBuilder sb = new StringBuilder();
      sb.append("=== Shimeji-Live License Status ===\n\n");
      sb.append("Current License: ").append(manager.getLicenseStatusInfo()).append("\n");
      sb.append("License Level: ").append(manager.getCurrentLicenseLevel().getDisplayName()).append("\n");
      if (manager.getCurrentLicenseLevel() != LicenseLevel.NO_KEY) {
         sb.append("Days Remaining: ").append(manager.getDaysRemaining()).append("\n");
         sb.append("Hours Remaining: ").append(manager.getHoursRemaining()).append("\n");
      }

      sb.append("\n=== Feature Availability ===\n\n");
      sb.append("Basic Features: Available\n");
      sb.append("Advanced Features: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.ADVANCED_KEY)).append("\n");
      sb.append("Special Features: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.SPECIAL_KEY)).append("\n");
      sb.append("Key Generation: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.SPECIAL_KEY)).append("\n");
      if (LicenseChecker.isLicenseExpiringSoon()) {
         sb.append("\n⚠️ WARNING: Your license is expiring soon!\n");
         sb.append("Please renew to continue enjoying premium features.\n");
      }

      JTextArea textArea = new JTextArea(sb.toString());
      textArea.setEditable(false);
      textArea.setFont(new Font("Monospaced", 0, 12));
      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setPreferredSize(new Dimension(500, 400));
      JOptionPane.showMessageDialog(parent, scrollPane, "License Status Information", 1);
   }
}
