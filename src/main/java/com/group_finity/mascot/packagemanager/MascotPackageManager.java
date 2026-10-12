package com.group_finity.mascot.packagemanager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MascotPackageManager {
   private static final Logger log = Logger.getLogger(MascotPackageManager.class.getName());
   private static final String MANIFEST_FILE = "manifest.json";
   private static final String PREVIEW_FILE = "preview.png";
   private static final String DATA_FOLDER = "mascot_data";
   private static final String CONF_FOLDER = "conf";

   public static boolean createPackage(File mascotFolder, File outputFile, String name, String author, String description) {
      try {
         MascotPackage packageData = new MascotPackage();
         packageData.setName(name);
         packageData.setAuthor(author);
         packageData.setDescription(description);
         packageData.setVersion("1.0");
         packageData.setCreateTime(LocalDateTime.now());
         scanMascotFolder(mascotFolder, packageData);
         ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

         boolean var7;
         try {
            writeManifest(zos, packageData);
            writePreviewImage(zos, mascotFolder, packageData);
            writeMascotData(zos, mascotFolder);
            log.info("Successfully created package: " + outputFile.getName());
            var7 = true;
         } catch (Throwable var10) {
            try {
               zos.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         zos.close();
         return var7;
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to create package", e);
         return false;
      }
   }

   public static boolean installPackage(File packageFile, JProgressBar progressBar) {
      boolean installed=com.group_finity.mascot.ui.PackImportDialog.installWithPreview(null,packageFile.toPath());
      javax.swing.SwingUtilities.invokeLater(()->{progressBar.setValue(installed?100:0);progressBar.setString(installed?"Installed":"Not installed");});
      return installed;
   }
   public static boolean uninstallMascot(String mascotName) {
      try {
         File mascotFolder = com.group_finity.mascot.pack.PackPaths.inside(com.group_finity.mascot.AppPaths.packs(),mascotName).toFile();
         if (!mascotFolder.exists()) {
            JOptionPane.showMessageDialog((Component)null, "桌宠不存在 / Mascot does not exist: " + mascotName, "错误 / Error", 0);
            return false;
         } else {
            int confirm = JOptionPane.showConfirmDialog((Component)null, String.format("确定要卸载桌宠 '%s' 吗？\n这将删除所有相关文件并移除对应的本地化条目。\n\nAre you sure you want to uninstall mascot '%s'?\nThis will delete all related files and remove corresponding localization entries.", mascotName, mascotName), "确认卸载 / Confirm Uninstall", 0, 2);
            if (confirm != 0) {
               return false;
            } else {
               boolean deleted = deleteDirectory(mascotFolder);
               if (deleted) {
                  JOptionPane.showMessageDialog((Component)null, "桌宠卸载成功 / Mascot uninstalled successfully: " + mascotName, "卸载成功 / Uninstall Successful", 1);
                  log.info("Successfully uninstalled mascot: " + mascotName);
                  return true;
               } else {
                  JOptionPane.showMessageDialog((Component)null, "卸载失败，某些文件可能正在使用中 / Uninstall failed, some files may be in use", "卸载错误 / Uninstall Error", 0);
                  return false;
               }
            }
         }
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to uninstall mascot: " + mascotName, e);
         return false;
      }
   }

   private static void scanMascotFolder(File folder, MascotPackage packageData) {
      List<String> imageFiles = new ArrayList();
      List<String> soundFiles = new ArrayList();
      List<String> languageFiles = new ArrayList();
      Set<String> supportedLanguages = new HashSet();
      scanFolderRecursively(folder, "", imageFiles, soundFiles, languageFiles, supportedLanguages);
      packageData.setImageFiles(imageFiles);
      packageData.setSoundFiles(soundFiles);
      packageData.setLanguageFiles(languageFiles);
      packageData.setSupportedLanguages(new ArrayList(supportedLanguages));
      Logger var10000 = log;
      int var10001 = imageFiles.size();
      var10000.info("Scanned mascot folder: " + var10001 + " images, " + soundFiles.size() + " sounds, " + languageFiles.size() + " language files");
   }

   private static void scanFolderRecursively(File folder, String relativePath, List imageFiles, List soundFiles, List languageFiles, Set supportedLanguages) {
      File[] files = folder.listFiles();
      if (files != null) {
         for(File file : files) {
            String currentPath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
            if (file.isDirectory()) {
               scanFolderRecursively(file, currentPath, imageFiles, soundFiles, languageFiles, supportedLanguages);
            } else {
               String fileName = file.getName().toLowerCase();
               if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".gif")) {
                  if (!fileName.endsWith(".wav") && !fileName.endsWith(".mp3") && !fileName.endsWith(".ogg")) {
                     if (fileName.endsWith(".properties") && currentPath.startsWith("conf/")) {
                        languageFiles.add(currentPath);
                        if (fileName.startsWith("language_") && fileName.length() > 19) {
                           String langCode = fileName.substring(9, fileName.length() - 11);
                           supportedLanguages.add(langCode);
                        } else if (fileName.equals("language.properties")) {
                           supportedLanguages.add("default");
                        }
                     }
                  } else {
                     soundFiles.add(currentPath);
                  }
               } else {
                  imageFiles.add(currentPath);
               }
            }
         }

      }
   }

   private static void writeManifest(ZipOutputStream zos, MascotPackage packageData) throws IOException {
      String manifestJson = packageToJson(packageData);
      ZipEntry entry = new ZipEntry("manifest.json");
      zos.putNextEntry(entry);
      zos.write(manifestJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
   }

   private static MascotPackage readManifest(ZipFile zipFile) throws IOException {
      ZipEntry manifestEntry = zipFile.getEntry("manifest.json");
      if (manifestEntry == null) {
         return null;
      } else {
         InputStream is = zipFile.getInputStream(manifestEntry);

         MascotPackage var7;
         try {
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            try {
               StringBuilder jsonBuilder = new StringBuilder();
               char[] buffer = new char[1024];

               int length;
               while((length = reader.read(buffer)) > 0) {
                  jsonBuilder.append(buffer, 0, length);
               }

               var7 = jsonToPackage(jsonBuilder.toString());
            } catch (Throwable var10) {
               try {
                  reader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            reader.close();
         } catch (Throwable var11) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (is != null) {
            is.close();
         }

         return var7;
      }
   }

   private static void writePreviewImage(ZipOutputStream zos, File mascotFolder, MascotPackage packageData) throws IOException {
      File previewFile = findPreviewImage(mascotFolder);
      if (previewFile != null && previewFile.exists()) {
         ZipEntry entry = new ZipEntry("preview.png");
         zos.putNextEntry(entry);
         FileInputStream fis = new FileInputStream(previewFile);

         try {
            byte[] buffer = new byte[1024];

            int length;
            while((length = fis.read(buffer)) > 0) {
               zos.write(buffer, 0, length);
            }
         } catch (Throwable var9) {
            try {
               fis.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         fis.close();
         zos.closeEntry();
         packageData.setPreviewImage("preview.png");
      }

   }

   private static File findPreviewImage(File mascotFolder) {
      String[] previewNames = new String[]{"Preview.png", "preview.png", "Stand.png", "stand.png"};

      for(String name : previewNames) {
         File file = new File(mascotFolder, name);
         if (file.exists()) {
            return file;
         }
      }

      File[] pngFiles = mascotFolder.listFiles((dir, namex) -> namex.toLowerCase().endsWith(".png"));
      if (pngFiles != null && pngFiles.length > 0) {
         return pngFiles[0];
      } else {
         return null;
      }
   }

   private static void writeMascotData(ZipOutputStream zos, File mascotFolder) throws IOException {
      writeFolderToZip(zos, mascotFolder, "mascot_data/");
   }

   private static void writeFolderToZip(ZipOutputStream zos, File folder, String basePath) throws IOException {
      File[] files = folder.listFiles();
      if (files != null) {
         for(File file : files) {
            String entryName = basePath + file.getName();
            if (file.isDirectory()) {
               ZipEntry dirEntry = new ZipEntry(entryName + "/");
               zos.putNextEntry(dirEntry);
               zos.closeEntry();
               writeFolderToZip(zos, file, entryName + "/");
            } else {
               ZipEntry fileEntry = new ZipEntry(entryName);
               zos.putNextEntry(fileEntry);
               FileInputStream fis = new FileInputStream(file);

               try {
                  byte[] buffer = new byte[1024];

                  int length;
                  while((length = fis.read(buffer)) > 0) {
                     zos.write(buffer, 0, length);
                  }
               } catch (Throwable var14) {
                  try {
                     fis.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }

                  throw var14;
               }

               fis.close();
               zos.closeEntry();
            }
         }

      }
   }

   private static Map readLocalizationData(ZipFile zipFile) throws IOException {
      Map<String, Properties> localizationData = new HashMap();
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while(entries.hasMoreElements()) {
         ZipEntry entry = (ZipEntry)entries.nextElement();
         if (entry.getName().startsWith("mascot_data/conf/") && entry.getName().endsWith(".properties")) {
            String fullPath = entry.getName();
            String fileName = fullPath.substring("mascot_data/conf/".length());
            Properties props = new Properties();
            InputStream is = zipFile.getInputStream(entry);

            try {
               InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

               try {
                  props.load(reader);
               } catch (Throwable var13) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }

                  throw var13;
               }

               reader.close();
            } catch (Throwable var14) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var11) {
                     var14.addSuppressed(var11);
                  }
               }

               throw var14;
            }

            if (is != null) {
               is.close();
            }

            localizationData.put(fileName, props);
         }
      }

      return localizationData;
   }

   private static void extractMascotData(ZipFile zipFile, File targetFolder, JProgressBar progressBar) throws IOException {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      List<ZipEntry> dataEntries = new ArrayList();

      while(entries.hasMoreElements()) {
         ZipEntry entry = (ZipEntry)entries.nextElement();
         if (entry.getName().startsWith("mascot_data/") && !entry.isDirectory()) {
            dataEntries.add(entry);
         }
      }

      int totalEntries = dataEntries.size();
      int processedEntries = 0;

      for(ZipEntry entry : dataEntries) {
         String relativePath = entry.getName().substring("mascot_data/".length());
         File outputFile = new File(targetFolder, relativePath);
         outputFile.getParentFile().mkdirs();
         InputStream is = zipFile.getInputStream(entry);

         try {
            FileOutputStream fos = new FileOutputStream(outputFile);

            try {
               byte[] buffer = new byte[1024];

               int length;
               while((length = is.read(buffer)) > 0) {
                  fos.write(buffer, 0, length);
               }
            } catch (Throwable var17) {
               try {
                  fos.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }

               throw var17;
            }

            fos.close();
         } catch (Throwable var18) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var15) {
                  var18.addSuppressed(var15);
               }
            }

            throw var18;
         }

         if (is != null) {
            is.close();
         }

         ++processedEntries;
         int progress = 50 + processedEntries * 30 / totalEntries;
         progressBar.setValue(progress);
         progressBar.setString("Extracting: " + relativePath);
      }

   }

   private static int showConflictDialog(LocalizationConflictReport conflictReport) {
      JTextArea textArea = new JTextArea(conflictReport.getFormattedReport());
      textArea.setEditable(false);
      textArea.setFont(new Font("Monospaced", 0, 12));
      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setPreferredSize(new Dimension(600, 400));
      String message = "检测到本地化文件冲突。系统将使用带前缀的键名解决冲突，\n原有的本地化不会丢失。\n\nLocalization conflicts detected. System will resolve conflicts\nusing prefixed keys, existing localizations won't be lost.\n\n是否继续安装？ / Continue installation?";
      Object[] components = new Object[]{message, scrollPane};
      return JOptionPane.showConfirmDialog((Component)null, components, "本地化冲突 / Localization Conflicts", 0, 2);
   }

   private static void showInstallationSummary(MascotPackage packageData, Map<String, Properties> localizationData) {
      StringBuilder summary = new StringBuilder();
      summary.append("安装摘要 / Installation Summary:\n\n");
      summary.append("桌宠名称 / Mascot: ").append(packageData.getName()).append("\n");
      summary.append("版本 / Version: ").append(packageData.getVersion()).append("\n");
      summary.append("作者 / Author: ").append(packageData.getAuthor()).append("\n\n");
      if (!localizationData.isEmpty()) {
         summary.append("本地化支持 / Localization Support:\n");

         for(String langFile : localizationData.keySet()) {
            Properties props = localizationData.get(langFile);
            summary.append("  ").append(langFile).append(": ").append(props.size()).append(" entries\n");
         }
      } else {
         summary.append("本地化支持 / Localization Support: 无 / None\n");
      }

      JOptionPane.showMessageDialog((Component)null, summary.toString(), "安装成功 / Installation Successful", 1);
   }

   private static boolean deleteDirectory(File directory) {
      if (!directory.exists()) {
         return false;
      } else {
         File[] files = directory.listFiles();
         if (files != null) {
            for(File file : files) {
               if (file.isDirectory()) {
                  deleteDirectory(file);
               } else {
                  file.delete();
               }
            }
         }

         return directory.delete();
      }
   }

   private static String packageToJson(MascotPackage packageData) {
      String var10000 = escapeJson(packageData.getName());
      return "{\n  \"name\": \"" + var10000 + "\",\n  \"version\": \"" + escapeJson(packageData.getVersion()) + "\",\n  \"author\": \"" + escapeJson(packageData.getAuthor()) + "\",\n  \"description\": \"" + escapeJson(packageData.getDescription()) + "\",\n  \"createTime\": \"" + packageData.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n  \"previewImage\": \"" + escapeJson(packageData.getPreviewImage()) + "\",\n  \"imageFiles\": " + listToJson(packageData.getImageFiles()) + ",\n  \"soundFiles\": " + listToJson(packageData.getSoundFiles()) + ",\n  \"languageFiles\": " + listToJson(packageData.getLanguageFiles()) + ",\n  \"supportedLanguages\": " + listToJson(packageData.getSupportedLanguages()) + ",\n  \"signed\": " + packageData.isSigned() + "\n}";
   }

   private static MascotPackage jsonToPackage(String json) {
      MascotPackage packageData = new MascotPackage();
      packageData.setName(extractJsonString(json, "name"));
      packageData.setVersion(extractJsonString(json, "version"));
      packageData.setAuthor(extractJsonString(json, "author"));
      packageData.setDescription(extractJsonString(json, "description"));
      String createTimeStr = extractJsonString(json, "createTime");
      if (createTimeStr != null && !createTimeStr.isEmpty()) {
         try {
            packageData.setCreateTime(LocalDateTime.parse(createTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
         } catch (Exception e) {
            log.warning("Failed to parse createTime: " + e.getMessage());
         }
      }

      packageData.setPreviewImage(extractJsonString(json, "previewImage"));
      packageData.setImageFiles(extractJsonStringList(json, "imageFiles"));
      packageData.setSoundFiles(extractJsonStringList(json, "soundFiles"));
      packageData.setLanguageFiles(extractJsonStringList(json, "languageFiles"));
      packageData.setSupportedLanguages(extractJsonStringList(json, "supportedLanguages"));
      packageData.setSigned(extractJsonBoolean(json));
      return packageData;
   }

   private static String escapeJson(String str) {
      return str == null ? "" : str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
   }

   private static String listToJson(List list) {
      if (list != null && !list.isEmpty()) {
         StringBuilder json = new StringBuilder();
         json.append("[");

         for(int i = 0; i < list.size(); ++i) {
            if (i > 0) {
               json.append(", ");
            }

            json.append("\"").append(escapeJson((String)list.get(i))).append("\"");
         }

         json.append("]");
         return json.toString();
      } else {
         return "[]";
      }
   }

   private static String extractJsonString(String json, String key) {
      String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"";
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(json);
      return m.find() ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t") : null;
   }

   private static List extractJsonStringList(String json, String key) {
      String pattern = "\"" + key + "\"\\s*:\\s*\\[(.*?)]";
      Pattern p = Pattern.compile(pattern, 32);
      Matcher m = p.matcher(json);
      List<String> result = new ArrayList();
      if (m.find()) {
         String arrayContent = m.group(1).trim();
         if (!arrayContent.isEmpty()) {
            String[] items = arrayContent.split(",");

            for(String item : items) {
               String trimmed = item.trim();
               if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                  trimmed = trimmed.substring(1, trimmed.length() - 1);
                  trimmed = trimmed.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                  result.add(trimmed);
               }
            }
         }
      }

      return result;
   }

   private static boolean extractJsonBoolean(String json) {
      String pattern = "\"signed\"\\s*:\\s*(true|false)";
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(json);
      return m.find() ? Boolean.parseBoolean(m.group(1)) : false;
   }

   public static MascotPackage readPackageInfo(File packageFile) throws IOException {
      if (packageFile.exists() && packageFile.getName().endsWith(".smp")) {
         ZipFile zipFile = new ZipFile(packageFile);

         MascotPackage var3;
         try {
            MascotPackage packageData = readManifest(zipFile);
            if (packageData == null || !packageData.validatePackage()) {
               throw new IllegalArgumentException("Invalid package manifest");
            }

            var3 = packageData;
         } catch (Throwable var5) {
            try {
               zipFile.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         zipFile.close();
         return var3;
      } else {
         throw new IllegalArgumentException("Invalid package file");
      }
   }
}
