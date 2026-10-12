package com.group_finity.mascot.animationeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageGridPanelRenderer {
   private static final ConcurrentHashMap iconCache = new ConcurrentHashMap();
   private static final int PREVIEW_WIDTH = 100;
   private static final int PREVIEW_HEIGHT = 80;
   private static final int PANEL_WIDTH = 200;
   private static final int PANEL_HEIGHT = 100;

   public static JPanel createImagePanel(File imageFile, Runnable onClickAction, Runnable onDoubleClickAction) {
      JPanel imagePanel = new JPanel(new BorderLayout());
      imagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      imagePanel.setPreferredSize(new Dimension(200, 100));
      imagePanel.setBackground(Color.WHITE);
      imagePanel.setCursor(Cursor.getPredefinedCursor(12));
      JLabel imageLabel = new JLabel();
      imageLabel.setHorizontalAlignment(0);
      imageLabel.setVerticalAlignment(0);
      imageLabel.setPreferredSize(new Dimension(115, 90));
      ImageIcon icon = getCachedIcon(imageFile);
      if (icon != null) {
         imageLabel.setIcon(icon);
      } else {
         imageLabel.setText("<html><center>无法<br/>预览</center></html>");
         imageLabel.setFont(new Font("SansSerif", 0, 9));
         imageLabel.setForeground(Color.GRAY);
      }

      String displayName = imageFile.getName();
      if (displayName.length() > 20) {
         int dotIndex = displayName.lastIndexOf(46);
         if (dotIndex > 15) {
            String var10000 = displayName.substring(0, 15);
            displayName = var10000 + "...<br/>" + displayName.substring(dotIndex - 3);
         }
      }

      JLabel nameLabel = new JLabel("<html><div style='word-wrap: break-word; width: 100px;'>" + displayName + "</div></html>");
      nameLabel.setFont(new Font("SansSerif", 1, 10));
      nameLabel.setVerticalAlignment(0);
      nameLabel.setToolTipText(imageFile.getName());
      nameLabel.setForeground(new Color(64, 64, 64));
      nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
      imagePanel.add(imageLabel, "West");
      imagePanel.add(nameLabel, "Center");
      MouseHandler mouseHandler = new MouseHandler(onClickAction, onDoubleClickAction);
      imagePanel.addMouseListener(mouseHandler);
      imageLabel.addMouseListener(mouseHandler);
      nameLabel.addMouseListener(mouseHandler);
      return imagePanel;
   }

   private static ImageIcon getCachedIcon(File imageFile) {
      String var10000 = imageFile.getAbsolutePath();
      String cacheKey = var10000 + "_" + imageFile.lastModified();
      ImageIcon icon = (ImageIcon)iconCache.get(cacheKey);
      if (icon == null) {
         icon = createThumbnailIcon(imageFile);
         if (icon != null) {
            iconCache.put(cacheKey, icon);
         }
      }

      return icon;
   }

   private static ImageIcon createThumbnailIcon(File imageFile) {
      try {
         BufferedImage image = ImageIO.read(imageFile);
         if (image != null) {
            Image scaledImage = scaleImageToFit(image, 100, 80);
            return new ImageIcon(scaledImage);
         }
      } catch (Exception var3) {
      }

      return null;
   }

   private static Image scaleImageToFit(BufferedImage originalImage, int maxWidth, int maxHeight) {
      int originalWidth = originalImage.getWidth();
      int originalHeight = originalImage.getHeight();
      double scaleX = (double)maxWidth / (double)originalWidth;
      double scaleY = (double)maxHeight / (double)originalHeight;
      double scale = Math.min(scaleX, scaleY);
      int newWidth = (int)((double)originalWidth * scale);
      int newHeight = (int)((double)originalHeight * scale);
      return originalImage.getScaledInstance(newWidth, newHeight, 4);
   }

   public static void highlightPanel(JPanel selectedPanel, Container parentContainer) {
      for(Component comp : parentContainer.getComponents()) {
         if (comp instanceof JPanel) {
            comp.setBackground(Color.WHITE);
            ((JPanel)comp).setBorder(BorderFactory.createRaisedBevelBorder());
         }
      }

      selectedPanel.setBackground(new Color(173, 216, 230));
      selectedPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLineBorder(new Color(0, 123, 255), 2)));
      selectedPanel.repaint();
   }

   public static void clearCache() {
      iconCache.clear();
   }

   private static class MouseHandler extends MouseAdapter {
      private final Runnable onClickAction;
      private final Runnable onDoubleClickAction;

      public MouseHandler(Runnable onClickAction, Runnable onDoubleClickAction) {
         this.onClickAction = onClickAction;
         this.onDoubleClickAction = onDoubleClickAction;
      }

      public void mouseClicked(MouseEvent evt) {
         if (evt.getClickCount() == 1 && this.onClickAction != null) {
            this.onClickAction.run();
         } else if (evt.getClickCount() == 2 && this.onDoubleClickAction != null) {
            this.onDoubleClickAction.run();
         }

      }

      public void mouseEntered(MouseEvent evt) {
         Component source = evt.getComponent();
         if (source instanceof JPanel panel) {
            if (panel.getBackground().equals(Color.WHITE)) {
               panel.setBackground(new Color(245, 245, 245));
            }
         }

      }

      public void mouseExited(MouseEvent evt) {
         Component source = evt.getComponent();
         if (source instanceof JPanel panel) {
            if (panel.getBackground().equals(new Color(245, 245, 245))) {
               panel.setBackground(Color.WHITE);
            }
         }

      }
   }
}
