package com.group_finity.mascot.packagemanager;

import com.group_finity.mascot.packagemanager.ui.PackageCreatorDialog;
import com.group_finity.mascot.packagemanager.ui.PackageInstallDialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class PackageManagerTest {
   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
            e.printStackTrace();
         }

         createTestUI();
      });
   }

   private static void createTestUI() {
      JFrame frame = new JFrame("Mascot Package Manager Test");
      frame.setDefaultCloseOperation(3);
      frame.setSize(400, 200);
      frame.setLocationRelativeTo((Component)null);
      JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      JButton installButton = new JButton("Test Install Package Dialog");
      installButton.addActionListener((e) -> PackageInstallDialog.showInstallDialog(frame));
      JButton createButton = new JButton("Test Create Package Dialog");
      createButton.addActionListener((e) -> PackageCreatorDialog.showCreatorDialog(frame));
      JButton testMergeButton = new JButton("Test Localization Merge");
      testMergeButton.addActionListener((e) -> testLocalizationMerge());
      panel.add(installButton);
      panel.add(createButton);
      panel.add(testMergeButton);
      frame.add(panel);
      frame.setVisible(true);
   }

   private static void testLocalizationMerge() {
      Map<String, Properties> testLocalization = new HashMap();
      Properties testProps = new Properties();
      testProps.setProperty("TestAction1", "Test Action 1");
      testProps.setProperty("TestAction2", "Test Action 2");
      testProps.setProperty("CommonAction", "Common Action from Test Package");
      testLocalization.put("language.properties", testProps);
      LocalizationConflictReport conflictReport = LocalizationMergeManager.checkConflicts("TestMascot", testLocalization);
      if (conflictReport.hasConflicts()) {
         JTextArea textArea = new JTextArea(conflictReport.getFormattedReport());
         textArea.setEditable(false);
         textArea.setFont(new Font("Monospaced", 0, 12));
         JScrollPane scrollPane = new JScrollPane(textArea);
         scrollPane.setPreferredSize(new Dimension(600, 400));
         JOptionPane.showMessageDialog((Component)null, scrollPane, "Localization Conflicts Test", 1);
      } else {
         boolean mergeResult = LocalizationMergeManager.mergeLocalizationFiles("TestMascot", testLocalization);
         JOptionPane.showMessageDialog((Component)null, "Localization merge test completed.\nResult: " + (mergeResult ? "Success" : "Failed") + "\nCheck conf/language.properties for merged entries.", "Localization Merge Test", 1);
      }

   }
}
