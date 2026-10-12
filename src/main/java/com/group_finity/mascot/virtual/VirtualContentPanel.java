package com.group_finity.mascot.virtual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;

public class VirtualContentPanel extends JPanel {
   private Image resizedImage;
   private String mode;
   private static final String CENTRE = "centre";
   private static final String FIT = "fit";
   private static final String STRETCH = "stretch";
   private static final String FILL = "fill";

   public VirtualContentPanel(Dimension preferredSize, Color background, final Image image, final String mode) {
      this.setLayout((LayoutManager)null);
      this.setPreferredSize(preferredSize);
      this.setBackground(background);
      this.resizedImage = image;
      this.mode = mode;
      if (!mode.equals("centre") && !mode.equals("fit") && !mode.equals("stretch") && !mode.equals("fill")) {
         this.mode = "centre";
      }

      this.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e) {
            if (image != null) {
               if (mode.equals("stretch")) {
                  VirtualContentPanel.this.resizedImage = image.getScaledInstance(VirtualContentPanel.this.getWidth(), VirtualContentPanel.this.getHeight(), 4);
               } else if (!mode.equals("centre")) {
                  double factor = mode.equals("fit") ? Math.min((double)VirtualContentPanel.this.getWidth() / (double)image.getWidth((ImageObserver)null), (double)VirtualContentPanel.this.getHeight() / (double)image.getHeight((ImageObserver)null)) : Math.max((double)VirtualContentPanel.this.getWidth() / (double)image.getWidth((ImageObserver)null), (double)VirtualContentPanel.this.getHeight() / (double)image.getHeight((ImageObserver)null));
                  VirtualContentPanel.this.resizedImage = image.getScaledInstance((int)(factor * (double)image.getWidth((ImageObserver)null)), (int)(factor * (double)image.getHeight((ImageObserver)null)), 4);
               }
            }

         }

         public void componentMoved(ComponentEvent e) {
         }

         public void componentShown(ComponentEvent e) {
         }

         public void componentHidden(ComponentEvent e) {
         }
      });
   }

   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (this.resizedImage != null) {
         if (this.mode.equals("stretch")) {
            g.drawImage(this.resizedImage, 0, 0, (ImageObserver)null);
         } else if (this.mode.equals("centre")) {
            g.drawImage(this.resizedImage, this.resizedImage.getWidth((ImageObserver)null) > this.getWidth() ? (this.resizedImage.getWidth((ImageObserver)null) - this.getWidth()) / -2 : (this.getWidth() - this.resizedImage.getWidth((ImageObserver)null)) / 2, this.resizedImage.getHeight((ImageObserver)null) > this.getHeight() ? (this.resizedImage.getHeight((ImageObserver)null) - this.getHeight()) / -2 : (this.getHeight() - this.resizedImage.getHeight((ImageObserver)null)) / 2, (ImageObserver)null);
         } else {
            g.drawImage(this.resizedImage, (this.getWidth() - this.resizedImage.getWidth((ImageObserver)null)) / 2, (this.getHeight() - this.resizedImage.getHeight((ImageObserver)null)) / 2, (ImageObserver)null);
         }
      }

   }
}
