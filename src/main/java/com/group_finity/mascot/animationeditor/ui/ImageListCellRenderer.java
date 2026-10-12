package com.group_finity.mascot.animationeditor.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

public class ImageListCellRenderer extends DefaultListCellRenderer {
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof File imageFile) {
         this.setText(imageFile.getName());

         try {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            if (originalIcon.getIconWidth() > 0) {
               Image scaledImage = originalIcon.getImage().getScaledInstance(32, 32, 4);
               this.setIcon(new ImageIcon(scaledImage));
            } else {
               this.setIcon(this.createFileIcon());
            }
         } catch (Exception var9) {
            this.setIcon(this.createFileIcon());
         }
      }

      return this;
   }

   private Icon createFileIcon() {
      return new FileIcon(16, 16);
   }

   private static class FileIcon implements Icon {
      private int width;
      private int height;

      public FileIcon(int width, int height) {
         this.width = width;
         this.height = height;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         Graphics2D g2d = (Graphics2D)g.create();
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setColor(Color.LIGHT_GRAY);
         g2d.fillRect(x + 2, y + 2, this.width - 6, this.height - 4);
         g2d.setColor(Color.DARK_GRAY);
         g2d.drawRect(x + 2, y + 2, this.width - 6, this.height - 4);
         g2d.setColor(Color.WHITE);
         g2d.fillPolygon(new int[]{x + this.width - 6, x + this.width - 2, x + this.width - 2}, new int[]{y + 2, y + 2, y + 6}, 3);
         g2d.setColor(Color.DARK_GRAY);
         g2d.drawLine(x + this.width - 6, y + 2, x + this.width - 2, y + 6);
         g2d.drawLine(x + this.width - 6, y + 2, x + this.width - 6, y + 6);
         g2d.drawLine(x + this.width - 6, y + 6, x + this.width - 2, y + 6);
         g2d.dispose();
      }

      public int getIconWidth() {
         return this.width;
      }

      public int getIconHeight() {
         return this.height;
      }
   }
}
