package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.animationeditor.util.ProjectScanner;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

public class OptimizedImageListRenderer extends DefaultListCellRenderer {
   private static final ConcurrentHashMap iconCache = new ConcurrentHashMap();
   private static final int ICON_SIZE = 32;
   private static final Icon DEFAULT_FILE_ICON = createDefaultFileIcon();

   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof File imageFile) {
         this.setText(imageFile.getName());
         String var10000 = imageFile.getAbsolutePath();
         String cacheKey = var10000 + "_" + imageFile.lastModified();
         ImageIcon icon = (ImageIcon)iconCache.get(cacheKey);
         if (icon == null) {
            icon = this.createThumbnailIcon(imageFile);
            if (icon == null) {
               this.setIcon(DEFAULT_FILE_ICON);
               return this;
            }

            iconCache.put(cacheKey, icon);
         }

         this.setIcon(icon);
      }

      return this;
   }

   private ImageIcon createThumbnailIcon(File imageFile) {
      try {
         if (!ProjectScanner.isImageFile(imageFile)) {
            return null;
         }

         ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
         if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
            Image scaledImage = originalIcon.getImage().getScaledInstance(32, 32, 4);
            return new ImageIcon(scaledImage);
         }
      } catch (Exception var4) {
      }

      return null;
   }

   private static Icon createDefaultFileIcon() {
      return new Icon() {
         public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x + 2, y + 2, this.getIconWidth() - 6, this.getIconHeight() - 4);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(x + 2, y + 2, this.getIconWidth() - 6, this.getIconHeight() - 4);
            int cornerSize = 6;
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(new int[]{x + this.getIconWidth() - cornerSize - 2, x + this.getIconWidth() - 2, x + this.getIconWidth() - 2}, new int[]{y + 2, y + 2, y + cornerSize + 2}, 3);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(x + this.getIconWidth() - cornerSize - 2, y + 2, x + this.getIconWidth() - 2, y + cornerSize + 2);
            g2d.drawLine(x + this.getIconWidth() - cornerSize - 2, y + 2, x + this.getIconWidth() - cornerSize - 2, y + cornerSize + 2);
            g2d.drawLine(x + this.getIconWidth() - cornerSize - 2, y + cornerSize + 2, x + this.getIconWidth() - 2, y + cornerSize + 2);
            g2d.dispose();
         }

         public int getIconWidth() {
            return 32;
         }

         public int getIconHeight() {
            return 32;
         }
      };
   }

   public static void clearCache() {
      iconCache.clear();
   }
}
