package com.group_finity.mascot.generic;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.platform.WindowUtils;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;

class GenericTranslucentWindow extends JWindow implements TranslucentWindow {
   private static final long serialVersionUID = 1L;
   private GenericNativeImage image;
   private float alpha = 1.0F;

   public GenericTranslucentWindow() {
      super(WindowUtils.getAlphaCompatibleGraphicsConfiguration());
      this.init();
      JPanel panel = new JPanel() {
         private static final long serialVersionUID = 1L;

         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (GenericTranslucentWindow.this.getImage() != null) {
               g.drawImage(GenericTranslucentWindow.this.getImage().getManagedImage(), 0, 0, (ImageObserver)null);
            }

         }
      };
      this.setContentPane(panel);
   }

   private void init() {
      System.setProperty("sun.java2d.d3d", "false");
      System.setProperty("sun.java2d.opengl", "true");
   }

   public void setVisible(boolean b) {
      super.setVisible(b);
      if (b) {
         WindowUtils.setWindowTransparent(this, true);
      }

   }

   protected void addImpl(Component comp, Object constraints, int index) {
      super.addImpl(comp, constraints, index);
      if (comp instanceof JComponent jcomp) {
         jcomp.setOpaque(false);
      }

   }

   public float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float alpha) {
      WindowUtils.setWindowAlpha(this, alpha);
      this.alpha = alpha;
   }

   public Component asComponent() {
      return this;
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "GenericTranslucentWindow[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.getBounds()) + "]";
   }

   public GenericNativeImage getImage() {
      return this.image;
   }

   public void setImage(NativeImage image) {
      this.image = (GenericNativeImage)image;
   }

   public void updateImage() {
      WindowUtils.setWindowMask(this, (Icon)this.getImage().getIcon());
      this.validate();
      this.repaint();
   }
}
