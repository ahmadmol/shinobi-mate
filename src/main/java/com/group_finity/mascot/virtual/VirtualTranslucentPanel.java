package com.group_finity.mascot.virtual;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;

class VirtualTranslucentPanel extends JPanel implements TranslucentWindow {
   private VirtualNativeImage image;

   public VirtualTranslucentPanel() {
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "VirtualTranslucentPanel[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.getBounds()) + "]";
   }

   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (this.image != null) {
         g.drawImage(this.image.getManagedImage(), 0, 0, (ImageObserver)null);
      }

   }

   public void paint(Graphics g) {
      if (g instanceof Graphics2D g2d) {
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }

      super.paint(g);
   }

   public Component asComponent() {
      return this;
   }

   protected void addImpl(Component comp, Object constraints, int index) {
      super.addImpl(comp, constraints, index);
   }

   public void setAlwaysOnTop(boolean onTop) {
   }

   public void setImage(NativeImage image) {
      this.image = (VirtualNativeImage)image;
   }

   public void dispose() {
      Container parent = this.getParent();
      if (parent != null) {
         parent.remove(this);
         parent.repaint();
      }

   }

   public void updateImage() {
      this.repaint();
   }

   public boolean contains(int x, int y) {
      if (super.contains(x, y)) {
         try {
            return (this.image.getManagedImage().getRGB(x, y) & -16777216) >>> 24 > 0;
         } catch (RuntimeException var4) {
            return false;
         }
      } else {
         return false;
      }
   }
}
