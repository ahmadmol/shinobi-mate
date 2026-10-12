package com.group_finity.mascot.win;

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
   private static final String SHORTCUT_NAME = "ShinobiMate.lnk";

   private static String getStartupFolderPath() {
      try {
         String userProfile = System.getenv("USERPROFILE");
         if (userProfile != null) {
            String startupPath = userProfile + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            File startupDir = new File(startupPath);
            return startupPath;
         }

         String appData = System.getenv("APPDATA");
         if (appData != null) {
            String startupPath = appData + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            File startupDir = new File(startupPath);
            return startupPath;
         }

         log.warning("Could not determine startup folder path");
         return null;
      } catch (Exception e) {
         log.log(Level.WARNING, "Error getting startup folder path", e);
         return null;
      }
   }

   public static boolean isAutoStartEnabled() {
      try {
         String startupPath = getStartupFolderPath();
         if (startupPath == null) {
            return false;
         } else {
            Path shortcutPath = Paths.get(startupPath, SHORTCUT_NAME);
            Path batPath = Paths.get(startupPath, "Shimeji-ee.bat");
            boolean exists = Files.exists(shortcutPath, new LinkOption[0]) || Files.exists(batPath, new LinkOption[0]) ||
                  Files.exists(Paths.get(startupPath,"Shimeji-ee.lnk")) || Files.exists(Paths.get(startupPath,"ShinobiMate.bat"));
            Logger var10000 = log;
            String var10001 = String.valueOf(shortcutPath);
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
         String startupPath = getStartupFolderPath();
         if (startupPath == null) {
            log.warning("Could not determine startup folder for auto-start");
            return false;
         } else {
            Path shortcutPath = Paths.get(startupPath, SHORTCUT_NAME);
            Path batPath = Paths.get(startupPath, "Shimeji-ee.bat");
            if (enable) {
               Files.createDirectories(shortcutPath.getParent());
               String exePath = getExecutablePath();
               if (exePath == null) {
                  log.warning("Could not determine executable path for auto-start");
                  return false;
               } else {
                  boolean success = createShortcut(exePath, shortcutPath.toString());
                  if (success) {
                     log.info("Auto-start enabled: " + String.valueOf(shortcutPath));
                  } else {
                     log.warning("Failed to create shortcut for auto-start");
                  }

                  return success;
               }
            } else {
               Files.deleteIfExists(Paths.get(startupPath,"Shimeji-ee.lnk"));
               Files.deleteIfExists(Paths.get(startupPath,"ShinobiMate.bat"));
               if (Files.exists(shortcutPath, new LinkOption[0])) {
                  Files.delete(shortcutPath);
                  log.info("Auto-start disabled: " + String.valueOf(shortcutPath));
               }

               if (Files.exists(batPath, new LinkOption[0])) {
                  Files.delete(batPath);
                  log.info("Auto-start disabled: " + String.valueOf(batPath));
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
         String currentDir = System.getProperty("app.dir",System.getProperty("user.dir"));
         File productFile=new File(currentDir,"ShinobiMate.exe");
         if(productFile.isFile())return productFile.getCanonicalPath();
         File exeFile = new File(currentDir, "Shimeji-ee.exe");
         if (exeFile.exists()) {
            return exeFile.getAbsolutePath();
         } else {
            String classPath = System.getProperty("java.class.path");
            if (classPath != null && classPath.endsWith("Shimeji-ee.exe")) {
               return classPath;
            } else {
               log.warning("Could not find ShinobiMate.exe for auto-start");
               return null;
            }
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Error determining executable path", e);
         return null;
      }
   }

   private static boolean createShortcut(String targetPath, String shortcutPath) {
      try {
         String script = String.format("$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%s'); $Shortcut.TargetPath = '%s'; $Shortcut.Arguments = '--background'; $Shortcut.WorkingDirectory = '%s'; $Shortcut.Description = 'ShinobiMate'; $Shortcut.Save()", shortcutPath.replace("'", "''"), targetPath.replace("'", "''"), (new File(targetPath)).getParent().replace("'", "''"));
         String encoded=java.util.Base64.getEncoder().encodeToString(script.getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
         ProcessBuilder pb = new ProcessBuilder(new String[]{"powershell.exe","-NoProfile","-NonInteractive","-WindowStyle","Hidden", "-EncodedCommand", encoded});
         pb.redirectErrorStream(true);
         Process process = pb.start();
         if(!process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();return false;}
         int exitCode = process.exitValue();
         if (exitCode == 0) {
            log.info("Shortcut created successfully: " + shortcutPath);
            return true;
         } else {
            log.warning("PowerShell script failed with exit code: " + exitCode);
            return createShortcutFallback(targetPath, shortcutPath);
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to create shortcut with PowerShell", e);
         return createShortcutFallback(targetPath, shortcutPath);
      }
   }

   private static boolean createShortcutFallback(String targetPath, String shortcutPath) {
      try {
         String batPath = shortcutPath.replace(".lnk", ".bat");
         String batContent = String.format("@echo off\nchcp 65001 >nul\ncd /d \"%s\"\nstart \"\" \"%s\" --background\n", (new File(targetPath)).getParent().replace("%","%%"), targetPath.replace("%","%%"));
         Files.write(Paths.get(batPath), batContent.getBytes(java.nio.charset.StandardCharsets.UTF_8), new OpenOption[0]);
         log.info("Fallback batch file created: " + batPath);
         return true;
      } catch (IOException e) {
         log.log(Level.SEVERE, "Failed to create fallback batch file", e);
         return false;
      }
   }
}
