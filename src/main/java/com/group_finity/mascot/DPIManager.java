package com.group_finity.mascot;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.util.Properties;
import java.util.logging.Logger;

public class DPIManager {
   private static final Logger log = Logger.getLogger(DPIManager.class.getName());
   private static final int DEFAULT_DPI = 96;
   private static final int[] VALID_DPI_VALUES = new int[]{96, 120, 144, 168, 192, 216, 240, 288, 336, 384};

   public static void autoConfigureDPI(Properties properties) {
      try {
         DisplayInfo displayInfo = getDisplayInfo();
         int recommendedDPI = calculateOptimalDPI(displayInfo);
         String currentDPIStr = properties.getProperty("MenuDPI", "96");
         int currentDPI = Integer.parseInt(currentDPIStr);
         if (shouldUpdateDPI(currentDPI, recommendedDPI)) {
            properties.setProperty("MenuDPI", String.valueOf(recommendedDPI));
            log.info(String.format("DPI 自动配置: %d -> %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", currentDPI, recommendedDPI, displayInfo.width, displayInfo.height, displayInfo.dpi, displayInfo.scaleFactor * (double)100.0F));
         } else {
            log.info(String.format("DPI 保持不变: %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", currentDPI, displayInfo.width, displayInfo.height, displayInfo.dpi, displayInfo.scaleFactor * (double)100.0F));
         }
      } catch (Exception e) {
         log.warning("DPI 自动配置失败: " + e.getMessage());
      }

   }

   private static DisplayInfo getDisplayInfo() {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      DisplayMode dm = gd.getDisplayMode();
      int systemDPI = Toolkit.getDefaultToolkit().getScreenResolution();
      double scaleFactor = getSystemScaleFactor();
      return new DisplayInfo(dm.getWidth(), dm.getHeight(), systemDPI, scaleFactor);
   }

   private static double getSystemScaleFactor() {
      try {
         if (isMacOS()) {
            return getMacOSScaleFactor();
         }

         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
         if (gc.getDefaultTransform() != null) {
            return gc.getDefaultTransform().getScaleX();
         }
      } catch (Exception var1) {
      }

      int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      return (double)dpi / (double)96.0F;
   }

   private static double getMacOSScaleFactor() {
      try {
         GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
         GraphicsConfiguration config = device.getDefaultConfiguration();
         AffineTransform transform = config.getDefaultTransform();
         double scaleFactor = Math.max(transform.getScaleX(), transform.getScaleY());
         log.info("macOS scale factor detected: " + scaleFactor);
         return scaleFactor;
      } catch (Exception e) {
         String retinaFactor = System.getProperty("apple.awt.contentScaleFactor");
         if (retinaFactor != null) {
            try {
               double factor = Double.parseDouble(retinaFactor);
               log.info("macOS scale factor from system property: " + factor);
               return factor;
            } catch (NumberFormatException var5) {
               log.warning("Invalid apple.awt.contentScaleFactor value: " + retinaFactor);
            }
         }

         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
         if (screenSize.width >= 2560 && screenRes >= 144) {
            log.info("Retina display detected by resolution, using 2.0 scale factor");
            return (double)2.0F;
         } else if (screenSize.width >= 1920 && screenRes >= 120) {
            log.info("High DPI display detected, using 1.5 scale factor");
            return (double)1.5F;
         } else {
            log.warning("Failed to detect macOS scale factor, using default: " + e.getMessage());
            return (double)1.0F;
         }
      }
   }

   private static int calculateOptimalDPI(DisplayInfo info) {
      int baseDPI = 96;
      if (info.width >= 3840 && info.height >= 2160) {
         baseDPI = 192;
      } else if (info.width >= 2560 && info.height >= 1440) {
         baseDPI = 144;
      } else if (info.width >= 1920 && info.height >= 1080) {
         if (info.dpi <= 96 && !(info.scaleFactor > (double)1.0F)) {
            baseDPI = 96;
         } else {
            baseDPI = 120;
         }
      }

      int scaledDPI = (int)((double)baseDPI * info.scaleFactor);
      int finalDPI = Math.max(scaledDPI, info.dpi);
      return findNearestValidDPI(finalDPI);
   }

   private static int findNearestValidDPI(int targetDPI) {
      int nearest = VALID_DPI_VALUES[0];
      int minDiff = Math.abs(targetDPI - nearest);

      for(int validDPI : VALID_DPI_VALUES) {
         int diff = Math.abs(targetDPI - validDPI);
         if (diff < minDiff) {
            minDiff = diff;
            nearest = validDPI;
         }
      }

      return nearest;
   }

   private static boolean shouldUpdateDPI(int currentDPI, int recommendedDPI) {
      if (currentDPI == 96 && recommendedDPI != 96) {
         return true;
      } else {
         double difference = (double)Math.abs(currentDPI - recommendedDPI) / (double)currentDPI;
         return difference > 0.3;
      }
   }

   public static String getDPIRecommendationText(Properties properties) {
      try {
         DisplayInfo info = getDisplayInfo();
         int currentDPI = Integer.parseInt(properties.getProperty("MenuDPI", "96"));
         int recommendedDPI = calculateOptimalDPI(info);
         return currentDPI == recommendedDPI ? "当前 DPI 设置最佳" : String.format("建议 DPI: %d (当前: %d)", recommendedDPI, currentDPI);
      } catch (Exception var4) {
         return "DPI 检测失败";
      }
   }

   public static void forceUpdateDPI(Properties properties) {
      try {
         DisplayInfo displayInfo = getDisplayInfo();
         int recommendedDPI = calculateOptimalDPI(displayInfo);
         properties.setProperty("MenuDPI", String.valueOf(recommendedDPI));
         log.info(String.format("DPI 强制更新: %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", recommendedDPI, displayInfo.width, displayInfo.height, displayInfo.dpi, displayInfo.scaleFactor * (double)100.0F));
      } catch (Exception e) {
         log.warning("DPI 强制更新失败: " + e.getMessage());
      }

   }

   private static boolean isMacOS() {
      return System.getProperty("os.name").toLowerCase().contains("mac");
   }

   public static double getUIScaleFactor() {
      try {
         DisplayInfo info = getDisplayInfo();
         return Math.max((double)1.0F, info.scaleFactor);
      } catch (Exception e) {
         log.warning("获取 UI 缩放因子失败: " + e.getMessage());
         return (double)1.0F;
      }
   }

   public static int scaleWidth(int originalWidth) {
      double scaleFactor = getUIScaleFactor();
      return (int)((double)originalWidth * scaleFactor);
   }

   public static int scaleHeight(int originalHeight) {
      double scaleFactor = getUIScaleFactor();
      return (int)((double)originalHeight * scaleFactor);
   }

   public static int scaleFontSize(int originalSize) {
      double scaleFactor = getUIScaleFactor();
      return Math.max(8, (int)((double)originalSize * scaleFactor));
   }

   private static class DisplayInfo {
      final int width;
      final int height;
      final int dpi;
      final double scaleFactor;

      DisplayInfo(int width, int height, int dpi, double scaleFactor) {
         this.width = width;
         this.height = height;
         this.dpi = dpi;
         this.scaleFactor = scaleFactor;
      }
   }
}
