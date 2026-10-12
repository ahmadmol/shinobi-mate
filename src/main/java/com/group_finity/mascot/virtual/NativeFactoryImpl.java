package com.group_finity.mascot.virtual;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.image.BufferedImage;

public class NativeFactoryImpl extends NativeFactory {
   private VirtualEnvironment environment = new VirtualEnvironment();

   public Environment getEnvironment() {
      return this.environment;
   }

   public NativeImage newNativeImage(BufferedImage src) {
      return new VirtualNativeImage(src);
   }

   public TranslucentWindow newTransparentWindow() {
      VirtualTranslucentPanel panel = new VirtualTranslucentPanel();
      this.environment.addShimeji(panel);
      return panel;
   }
}
