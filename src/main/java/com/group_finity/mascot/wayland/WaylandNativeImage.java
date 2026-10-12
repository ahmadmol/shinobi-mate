package com.group_finity.mascot.wayland;

import com.group_finity.mascot.image.NativeImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class WaylandNativeImage implements NativeImage {
   private BufferedImage managedImage;
   private Icon icon;
   private int[] imageData;
   private int width;
   private int height;

   public WaylandNativeImage(BufferedImage image) {
      this.managedImage = this.createOptimizedImage(image);
      this.width = image.getWidth();
      this.height = image.getHeight();
      this.extractImageData();
      this.icon = new ImageIcon(this.managedImage);
   }

   private BufferedImage createOptimizedImage(BufferedImage source) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      BufferedImage optimized = gc.createCompatibleImage(source.getWidth(), source.getHeight(), 3);
      Graphics2D g2d = optimized.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2d.setComposite(AlphaComposite.Src);
      g2d.drawImage(source, 0, 0, (ImageObserver)null);
      g2d.dispose();
      return optimized;
   }

   private void extractImageData() {
      if (this.managedImage.getType() == 2) {
         DataBufferInt buffer = (DataBufferInt)this.managedImage.getRaster().getDataBuffer();
         this.imageData = buffer.getData();
      } else {
         BufferedImage argbImage = new BufferedImage(this.width, this.height, 2);
         Graphics2D g2d = argbImage.createGraphics();
         g2d.drawImage(this.managedImage, 0, 0, (ImageObserver)null);
         g2d.dispose();
         DataBufferInt buffer = (DataBufferInt)argbImage.getRaster().getDataBuffer();
         this.imageData = buffer.getData();
         this.managedImage = argbImage;
      }

   }

   public BufferedImage getManagedImage() {
      return this.managedImage;
   }

   public Icon getIcon() {
      return this.icon;
   }

   public void dispose() {
      if (this.managedImage != null) {
         this.managedImage.flush();
         this.managedImage = null;
      }

      this.icon = null;
      this.imageData = null;
   }

   public int[] getImageData() {
      return this.imageData;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean hasTransparency() {
      return this.managedImage.getColorModel().hasAlpha();
   }
}
