package com.group_finity.mascot.x11;

import com.group_finity.mascot.image.NativeImage;
import java.awt.image.BufferedImage;

class X11NativeImage implements NativeImage {
   private final BufferedImage managedImage;

   public X11NativeImage(BufferedImage image) {
      this.managedImage = image;
   }

   public int getWidth() {
      return this.getManagedImage().getWidth();
   }

   public int getHeight() {
      return this.getManagedImage().getHeight();
   }

   BufferedImage getManagedImage() {
      return this.managedImage;
   }
}
