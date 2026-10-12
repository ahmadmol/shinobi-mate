package com.group_finity.mascot;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.win.NativeFactoryImpl;
import java.awt.image.BufferedImage;

public abstract class NativeFactory {
   private static NativeFactory instance;

   public static NativeFactory getInstance() {
      if (instance == null) {
         resetInstance();
      }

      return instance;
   }

   public static void resetInstance() {
      String environment = "generic";

      try {
         if (Main.getInstance() != null && Main.getInstance().getProperties() != null) {
            environment = Main.getInstance().getProperties().getProperty("Environment", "generic");
         }
      } catch (Exception var2) {
         environment = "generic";
      }

      if (environment.equals("generic")) {
         if (com.sun.jna.Platform.isWindows()) {
            instance = new NativeFactoryImpl();
         } else if (com.sun.jna.Platform.isMac()) {
            instance = new com.group_finity.mascot.mac.NativeFactoryImpl();
         } else if (com.sun.jna.Platform.isX11()) {
            if (isWayland()) {
               instance = new com.group_finity.mascot.wayland.NativeFactoryImpl();
            } else {
               instance = new com.group_finity.mascot.x11.NativeFactoryImpl();
            }
         } else {
            instance = new com.group_finity.mascot.generic.NativeFactoryImpl();
         }
      } else if (environment.equals("virtual")) {
         instance = new com.group_finity.mascot.virtual.NativeFactoryImpl();
      } else if (environment.equals("wayland")) {
         instance = new com.group_finity.mascot.wayland.NativeFactoryImpl();
      } else {
         instance = new com.group_finity.mascot.generic.NativeFactoryImpl();
      }

   }

   private static boolean isWayland() {
      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
      String sessionType = System.getenv("XDG_SESSION_TYPE");
      return waylandDisplay != null && !waylandDisplay.isEmpty() || sessionType != null && sessionType.equals("wayland");
   }

   public abstract Environment getEnvironment();

   public abstract NativeImage newNativeImage(BufferedImage var1);

   public abstract TranslucentWindow newTransparentWindow();
}
