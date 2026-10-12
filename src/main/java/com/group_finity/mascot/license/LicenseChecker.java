package com.group_finity.mascot.license;

import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class LicenseChecker {
   private static final Logger logger = Logger.getLogger(LicenseChecker.class.getName());

   public static boolean checkPermission(LicenseLevel requiredLevel) {
      return checkPermission(requiredLevel, false);
   }

   public static boolean checkPermission(LicenseLevel requiredLevel, boolean showDialog) {
      LicenseManager manager = LicenseManager.getInstance();
      LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
      boolean hasPermission = currentLevel.hasPermission(requiredLevel);
      if (!hasPermission && showDialog) {
         showPermissionDeniedDialog(requiredLevel);
      }

      return hasPermission;
   }

   public static boolean checkAdvancedFeature() {
      return checkAdvancedFeature(false);
   }

   public static boolean checkAdvancedFeature(boolean showDialog) {
      return checkPermission(LicenseLevel.ADVANCED_KEY, showDialog);
   }

   public static boolean checkSpecialFeature() {
      return checkSpecialFeature(false);
   }

   public static boolean checkSpecialFeature(boolean showDialog) {
      return checkPermission(LicenseLevel.SPECIAL_KEY, showDialog);
   }

   private static void showPermissionDeniedDialog(LicenseLevel requiredLevel) {
      LicenseManager manager = LicenseManager.getInstance();
      String currentStatus = manager.getLicenseStatusInfo();
      String message;
      String var10000;
      switch (requiredLevel) {
         case ADVANCED_KEY:
            message = "This feature requires an Advanced or Special license.\n\nCurrent Status: " + currentStatus + "\n\nPlease activate a valid license to access this feature.\nYou can purchase a license from our website or contact support.";
            var10000 = "Advanced License Required";
            break;
         case SPECIAL_KEY:
            message = "This feature requires a Special license.\n\nCurrent Status: " + currentStatus + "\n\nPlease activate a Special license to access this feature.\nSpecial licenses are available for developers and premium users.";
            var10000 = "Special License Required";
            break;
         default:
            message = "This feature requires a higher license level.\n\nCurrent Status: " + currentStatus + "\n\nPlease upgrade your license to access this feature.";
            var10000 = "License Upgrade Required";
      }

      String title = var10000;
      JOptionPane.showMessageDialog((Component)null, message, title, 2);
   }

   public static String getFeatureStatus(LicenseLevel requiredLevel) {
      LicenseManager manager = LicenseManager.getInstance();
      boolean hasPermission = manager.getCurrentLicenseLevel().hasPermission(requiredLevel);
      return hasPermission ? "Available" : "Requires " + requiredLevel.getDisplayName();
   }

   public static boolean isLicenseExpiringSoon() {
      LicenseManager manager = LicenseManager.getInstance();
      long daysRemaining = manager.getDaysRemaining();
      return daysRemaining > 0L && daysRemaining <= 7L;
   }

   public static void showExpirationWarningIfNeeded() {
      LicenseManager manager = LicenseManager.getInstance();
      LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
      if (currentLevel != LicenseLevel.NO_KEY) {
         long daysRemaining = manager.getDaysRemaining();
         if (daysRemaining <= 0L) {
            JOptionPane.showMessageDialog((Component)null, "Your license has expired!\nThe software will continue to work with limited functionality.\nPlease renew your license to restore full features.", "License Expired", 2);
         } else if (daysRemaining <= 3L) {
            JOptionPane.showMessageDialog((Component)null, "Your license will expire in " + daysRemaining + " day(s)!\n\nPlease renew your license to avoid interruption of service.\nContact support or visit our website to renew.", "License Expiring Soon", 2);
         } else if (daysRemaining <= 7L) {
            logger.info("License expiring in " + daysRemaining + " days");
         }
      }

   }

   public static void logFeatureUsage(String featureName, LicenseLevel requiredLevel) {
      LicenseManager manager = LicenseManager.getInstance();
      LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
      boolean hasPermission = currentLevel.hasPermission(requiredLevel);
      logger.info(String.format("Feature access: %s, Required: %s, Current: %s, Allowed: %s", featureName, requiredLevel.getDisplayName(), currentLevel.getDisplayName(), hasPermission));
   }
}
