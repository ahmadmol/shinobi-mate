package com.group_finity.mascot.virtual;

import com.group_finity.mascot.image.NativeImage;
import java.awt.image.BufferedImage;

class VirtualNativeImage implements NativeImage {
   private final BufferedImage managedImage;

   public VirtualNativeImage(BufferedImage image) {
      this.managedImage = image;
   }

   BufferedImage getManagedImage() {
      return this.managedImage;
   }
}
