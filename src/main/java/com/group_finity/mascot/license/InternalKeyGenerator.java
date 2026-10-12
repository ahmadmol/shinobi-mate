package com.group_finity.mascot.license;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class InternalKeyGenerator {
   private static final Logger logger = Logger.getLogger(InternalKeyGenerator.class.getName());
   private static final String INTERNAL_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=";
   private JFrame frame;
   private JTextField validityField;
   private JTextArea resultArea;
   private JButton generateButton;

   public static boolean canAccess() {
      LicenseManager manager = LicenseManager.getInstance();
      return manager.getCurrentLicenseLevel() == LicenseLevel.SPECIAL_KEY;
   }

   public static void showKeyGenerator() {
      if (!canAccess()) {
         JOptionPane.showMessageDialog((Component)null, "Access Denied: Special Key required to access the internal key generator.", "Permission Required", 0);
      } else {
         SwingUtilities.invokeLater(() -> (new InternalKeyGenerator()).createAndShowGUI());
      }
   }

   private void createAndShowGUI() {
      this.frame = new JFrame("Shimeji-Live Internal Key Generator");
      this.frame.setDefaultCloseOperation(2);
      this.frame.setLayout(new BorderLayout());
      JPanel mainPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      JLabel titleLabel = new JLabel("Internal Advanced Key Generator");
      titleLabel.setFont(new Font("SansSerif", 1, 16));
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(10, 10, 20, 10);
      mainPanel.add(titleLabel, gbc);
      gbc.gridwidth = 1;
      gbc.insets = new Insets(5, 10, 5, 5);
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.anchor = 17;
      mainPanel.add(new JLabel("Validity (days):"), gbc);
      this.validityField = new JTextField("30", 10);
      gbc.gridx = 1;
      gbc.fill = 2;
      mainPanel.add(this.validityField, gbc);
      this.generateButton = new JButton("Generate Advanced Key");
      this.generateButton.addActionListener(new GenerateKeyAction());
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(20, 10, 10, 10);
      gbc.fill = 0;
      gbc.anchor = 10;
      mainPanel.add(this.generateButton, gbc);
      this.resultArea = new JTextArea(10, 50);
      this.resultArea.setEditable(false);
      this.resultArea.setFont(new Font("Monospaced", 0, 12));
      this.resultArea.setBackground(Color.BLACK);
      this.resultArea.setForeground(Color.GREEN);
      JScrollPane scrollPane = new JScrollPane(this.resultArea);
      scrollPane.setBorder(BorderFactory.createTitledBorder("Generated Key"));
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 2;
      gbc.fill = 1;
      gbc.weightx = (double)1.0F;
      gbc.weighty = (double)1.0F;
      gbc.insets = new Insets(10, 10, 10, 10);
      mainPanel.add(scrollPane, gbc);
      JPanel buttonPanel = new JPanel(new FlowLayout());
      JButton copyButton = new JButton("Copy to Clipboard");
      copyButton.addActionListener((e) -> this.copyToClipboard());
      buttonPanel.add(copyButton);
      JButton clearButton = new JButton("Clear");
      clearButton.addActionListener((e) -> this.resultArea.setText(""));
      buttonPanel.add(clearButton);
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener((e) -> this.frame.dispose());
      buttonPanel.add(closeButton);
      this.frame.add(mainPanel, "Center");
      this.frame.add(buttonPanel, "South");
      this.frame.setSize(600, 500);
      this.frame.setLocationRelativeTo((Component)null);
      this.frame.setVisible(true);
      LicenseManager manager = LicenseManager.getInstance();
      this.resultArea.setText("Internal Key Generator Activated\n");
      this.resultArea.append("Current License: " + manager.getLicenseStatusInfo() + "\n");
      this.resultArea.append("Days Remaining: " + manager.getDaysRemaining() + "\n\n");
      this.resultArea.append("Ready to generate Advanced Keys...\n");
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

   private void copyToClipboard() {
      String text = this.resultArea.getText();
      String[] lines = text.split("\n");

      for(String line : lines) {
         if (line.startsWith("MS4w")) {
            StringSelection selection = new StringSelection(line.trim());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, (ClipboardOwner)null);
            JOptionPane.showMessageDialog(this.frame, "Key copied to clipboard!", "Success", 1);
            return;
         }
      }

      JOptionPane.showMessageDialog(this.frame, "No key found to copy.", "Nothing to Copy", 2);
   }

   private class GenerateKeyAction implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         try {
            final int validityDays = Integer.parseInt(InternalKeyGenerator.this.validityField.getText().trim());
            if (validityDays <= 0 || validityDays > 3650) {
               JOptionPane.showMessageDialog(InternalKeyGenerator.this.frame, "Validity days must be between 1 and 3650.", "Invalid Input", 0);
               return;
            }

            InternalKeyGenerator.this.generateButton.setEnabled(false);
            InternalKeyGenerator.this.resultArea.append("\nGenerating Advanced Key...\n");
            SwingWorker<String, Void> worker = new SwingWorker() {
               protected String doInBackground() throws Exception {
                  return InternalKeyGenerator.this.generateAdvancedKey(validityDays);
               }

               protected void done() {
                  try {
                     String key = (String)this.get();
                     InternalKeyGenerator.this.resultArea.append("Generated Successfully!\n\n");
                     InternalKeyGenerator.this.resultArea.append("Advanced Key (" + validityDays + " days):\n");
                     InternalKeyGenerator.this.resultArea.append(key + "\n\n");
                     InternalKeyGenerator.this.resultArea.append("Instructions:\n");
                     InternalKeyGenerator.this.resultArea.append("1. Copy the key above\n");
                     InternalKeyGenerator.this.resultArea.append("2. Share with authorized users\n");
                     InternalKeyGenerator.this.resultArea.append("3. Users can activate via License menu\n\n");
                  } catch (Exception ex) {
                     InternalKeyGenerator.logger.log(Level.SEVERE, "Failed to generate key", ex);
                     InternalKeyGenerator.this.resultArea.append("ERROR: Failed to generate key\n");
                     InternalKeyGenerator.this.resultArea.append("Reason: " + ex.getMessage() + "\n");
                  } finally {
                     InternalKeyGenerator.this.generateButton.setEnabled(true);
                  }

               }
            };
            worker.execute();
         } catch (NumberFormatException var4) {
            JOptionPane.showMessageDialog(InternalKeyGenerator.this.frame, "Please enter a valid number for validity days.", "Invalid Input", 0);
         }

      }
   }
}
