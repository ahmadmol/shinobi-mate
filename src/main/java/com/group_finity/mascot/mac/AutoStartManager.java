package com.group_finity.mascot.mac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoStartManager {
   private static final Logger log = Logger.getLogger(AutoStartManager.class.getName());
   private static final String PLIST_NAME = "com.group_finity.shimeji.plist";
   private static final String APP_NAME = "Shimeji-ee";

   private static String getLaunchAgentsPath() {
      try {
         String userHome = System.getProperty("user.home");
         if (userHome != null) {
            String launchAgentsPath = userHome + "/Library/LaunchAgents";
            File launchAgentsDir = new File(launchAgentsPath);
            if (launchAgentsDir.exists() || launchAgentsDir.mkdirs()) {
               return launchAgentsPath;
            }
         }

         log.warning("Could not determine Launch Agents folder path");
         return null;
      } catch (Exception e) {
         log.log(Level.WARNING, "Error getting Launch Agents folder path", e);
         return null;
      }
   }

   public static boolean isAutoStartEnabled() {
      try {
         String launchAgentsPath = getLaunchAgentsPath();
         if (launchAgentsPath == null) {
            return false;
         } else {
            Path plistPath = Paths.get(launchAgentsPath, "com.group_finity.shimeji.plist");
            boolean exists = Files.exists(plistPath, new LinkOption[0]);
            Logger var10000 = log;
            String var10001 = String.valueOf(plistPath);
            var10000.info("Checking auto-start: " + var10001 + " exists: " + exists);
            return exists;
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to check auto-start status", e);
         return false;
      }
   }

   public static boolean setAutoStart(boolean enable) {
      try {
         String launchAgentsPath = getLaunchAgentsPath();
         if (launchAgentsPath == null) {
            log.warning("Could not determine Launch Agents folder for auto-start");
            return false;
         } else {
            Path plistPath = Paths.get(launchAgentsPath, "com.group_finity.shimeji.plist");
            if (enable) {
               String executablePath = getExecutablePath();
               if (executablePath == null) {
                  log.warning("Could not determine executable path for auto-start");
                  return false;
               } else {
                  boolean success = createLaunchAgent(executablePath, plistPath.toString());
                  if (success) {
                     boolean loaded = loadLaunchAgent(plistPath.toString());
                     if (loaded) {
                        log.info("Auto-start enabled: " + String.valueOf(plistPath));
                        return true;
                     } else {
                        log.warning("Failed to load Launch Agent");
                        return false;
                     }
                  } else {
                     log.warning("Failed to create Launch Agent for auto-start");
                     return false;
                  }
               }
            } else {
               if (Files.exists(plistPath, new LinkOption[0])) {
                  unloadLaunchAgent(plistPath.toString());
                  Files.delete(plistPath);
                  log.info("Auto-start disabled: " + String.valueOf(plistPath));
               }

               return true;
            }
         }
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to set auto-start: " + enable, e);
         return false;
      }
   }

   private static String getExecutablePath() {
      try {
         String currentDir = System.getProperty("user.dir");
         File appFile = new File(currentDir, "Shimeji-ee.app");
         if (appFile.exists() && appFile.isDirectory()) {
            return appFile.getAbsolutePath();
         } else {
            File parentDir = (new File(currentDir)).getParentFile();
            if (parentDir != null) {
               File parentAppFile = new File(parentDir, "Shimeji-ee.app");
               if (parentAppFile.exists() && parentAppFile.isDirectory()) {
                  return parentAppFile.getAbsolutePath();
               }
            }

            if (currentDir.contains(".app/Contents")) {
               String appPath = currentDir.substring(0, currentDir.indexOf(".app/Contents") + 4);
               File appBundle = new File(appPath);
               if (appBundle.exists()) {
                  return appBundle.getAbsolutePath();
               }
            }

            String classPath = System.getProperty("java.class.path");
            if (classPath != null && classPath.endsWith(".jar")) {
               File jarFile = new File(classPath);
               if (jarFile.exists()) {
                  return jarFile.getAbsolutePath();
               }
            }

            log.warning("Could not find executable for auto-start");
            return null;
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Error determining executable path", e);
         return null;
      }
   }

   private static boolean createLaunchAgent(String executablePath, String plistPath) {
      try {
         String plistContent;
         if (executablePath.endsWith(".app")) {
            plistContent = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n<plist version=\"1.0\">\n<dict>\n    <key>Label</key>\n    <string>com.group_finity.shimeji</string>\n    <key>ProgramArguments</key>\n    <array>\n        <string>/usr/bin/open</string>\n        <string>%s</string>\n    </array>\n    <key>RunAtLoad</key>\n    <true/>\n    <key>KeepAlive</key>\n    <false/>\n    <key>LaunchOnlyOnce</key>\n    <true/>\n</dict>\n</plist>", executablePath);
         } else {
            plistContent = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n<plist version=\"1.0\">\n<dict>\n    <key>Label</key>\n    <string>com.group_finity.shimeji</string>\n    <key>ProgramArguments</key>\n    <array>\n        <string>/usr/bin/java</string>\n        <string>-jar</string>\n        <string>%s</string>\n    </array>\n    <key>WorkingDirectory</key>\n    <string>%s</string>\n    <key>RunAtLoad</key>\n    <true/>\n    <key>KeepAlive</key>\n    <false/>\n    <key>LaunchOnlyOnce</key>\n    <true/>\n</dict>\n</plist>", executablePath, (new File(executablePath)).getParent());
         }

         Files.write(Paths.get(plistPath), plistContent.getBytes(), new OpenOption[0]);
         log.info("Launch Agent plist created: " + plistPath);
         return true;
      } catch (IOException e) {
         log.log(Level.SEVERE, "Failed to create Launch Agent plist", e);
         return false;
      }
   }

   private static boolean loadLaunchAgent(String plistPath) {
      try {
         ProcessBuilder pb = new ProcessBuilder(new String[]{"launchctl", "load", plistPath});
         pb.redirectErrorStream(true);
         Process process = pb.start();
         int exitCode = process.waitFor();
         if (exitCode == 0) {
            log.info("Launch Agent loaded successfully: " + plistPath);
            return true;
         } else {
            log.warning("Failed to load Launch Agent, exit code: " + exitCode);
            return false;
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to load Launch Agent", e);
         return false;
      }
   }

   private static boolean unloadLaunchAgent(String plistPath) {
      try {
         ProcessBuilder pb = new ProcessBuilder(new String[]{"launchctl", "unload", plistPath});
         pb.redirectErrorStream(true);
         Process process = pb.start();
         int exitCode = process.waitFor();
         if (exitCode == 0) {
            log.info("Launch Agent unloaded successfully: " + plistPath);
            return true;
         } else {
            log.warning("Failed to unload Launch Agent, exit code: " + exitCode);
            return false;
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to unload Launch Agent", e);
         return false;
      }
   }
}
