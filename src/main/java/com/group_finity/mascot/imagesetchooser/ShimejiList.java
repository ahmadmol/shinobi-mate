package com.group_finity.mascot.imagesetchooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ShimejiList extends JList {
   public ShimejiList() {
      this.setCellRenderer(new CustomCellRenderer());
   }

   static class CustomCellRenderer implements ListCellRenderer<Object> {
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         if (value instanceof ImageSetChooserPanel panel) {
            panel.setForeground(Color.white);
            panel.setBackground((Color)(isSelected ? SystemColor.controlHighlight : Color.white));
            panel.setCheckbox(isSelected);
            return panel;
         } else {
            return new JLabel("???");
         }
      }
   }
}
