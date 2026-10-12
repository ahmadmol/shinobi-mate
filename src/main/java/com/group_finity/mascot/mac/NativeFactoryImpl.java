package com.group_finity.mascot.mac;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.image.BufferedImage;

public class NativeFactoryImpl extends NativeFactory {
   private NativeFactory delegate = new com.group_finity.mascot.generic.NativeFactoryImpl();
   private Environment environment = new MacEnvironment();

   public Environment getEnvironment() {
      return this.environment;
   }

   public NativeImage newNativeImage(BufferedImage src) {
      return this.delegate.newNativeImage(src);
   }

   public TranslucentWindow newTransparentWindow() {
      return new MacTranslucentWindow(this.delegate);
   }
}
