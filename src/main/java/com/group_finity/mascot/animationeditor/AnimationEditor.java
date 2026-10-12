package com.group_finity.mascot.animationeditor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.group_finity.mascot.animationeditor.ui.MainEditorWindow;
import com.group_finity.mascot.license.LicenseChecker;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AnimationEditor {
   private static final Logger log = Logger.getLogger(AnimationEditor.class.getName());

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         try {
            FlatDarkLaf.setup();
            UIManager.setLookAndFeel(new FlatDarkLaf());
            (new MainEditorWindow()).setVisible(true);
         } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to start Animation Editor", e);
            System.exit(1);
         }

      });
   }

   public static void launch() {
      if (!LicenseChecker.checkSpecialFeature(true)) {
         log.log(Level.WARNING, "Animation Editor access denied - Special license required");
      } else {
         SwingUtilities.invokeLater(() -> {
            try {
               log.log(Level.INFO, "Launching Animation Editor with Special license authorization");
               MainEditorWindow editorWindow = new MainEditorWindow();
               editorWindow.setTitle("Shimeji Animation Editor - Developer Version");
               editorWindow.setVisible(true);
            } catch (Exception e) {
               log.log(Level.SEVERE, "Failed to launch Animation Editor", e);
               JOptionPane.showMessageDialog((Component)null, "Failed to launch Animation Editor: " + e.getMessage(), "Error", 0);
            }

         });
      }
   }
}
