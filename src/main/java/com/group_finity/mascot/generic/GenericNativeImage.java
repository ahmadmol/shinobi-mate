package com.group_finity.mascot.generic;

import com.group_finity.mascot.image.NativeImage;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

class GenericNativeImage implements NativeImage {
   private final BufferedImage managedImage;
   private final Icon icon;

   public GenericNativeImage(BufferedImage image) {
      this.managedImage = image;
      this.icon = new ImageIcon(image);
   }

   BufferedImage getManagedImage() {
      return this.managedImage;
   }

   public Icon getIcon() {
      return this.icon;
   }
}
