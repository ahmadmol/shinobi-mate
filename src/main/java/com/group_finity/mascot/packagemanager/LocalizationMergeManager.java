package com.group_finity.mascot.packagemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalizationMergeManager {
   private static final Logger log = Logger.getLogger(LocalizationMergeManager.class.getName());
   private static final String CONF_FOLDER = "conf";
   private static final String BACKUP_SUFFIX = ".backup";
   private static final String MASCOT_PREFIX_FORMAT = "mascot.%s.%s";

   public static boolean mergeLocalizationFiles(String mascotName, Map<String, Properties> localizationData) {
      try {
         File confDir = new File("conf");
         if (!confDir.exists()) {
            confDir.mkdirs();
         }

         for(Map.Entry<String, Properties> entry : localizationData.entrySet()) {
            String languageFile = entry.getKey();
            Properties newProperties = entry.getValue();
            if (!mergeLanguageFile(mascotName, languageFile, newProperties)) {
               log.warning("Failed to merge language file: " + languageFile);
               return false;
            }
         }

         log.info("Successfully merged localization for mascot: " + mascotName);
         return true;
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to merge localization files", e);
         return false;
      }
   }

   private static boolean mergeLanguageFile(String mascotName, String languageFile, Properties newProperties) throws IOException {
      File targetFile = new File("conf", languageFile);
      File backupFile = new File("conf", languageFile + ".backup");
      Properties existingProperties = new Properties();
      if (targetFile.exists()) {
         Files.copy(targetFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         FileInputStream fis = new FileInputStream(targetFile);

         try {
            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);

            try {
               existingProperties.load(reader);
            } catch (Throwable var20) {
               try {
                  reader.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }

               throw var20;
            }

            reader.close();
         } catch (Throwable var21) {
            try {
               fis.close();
            } catch (Throwable var16) {
               var21.addSuppressed(var16);
            }

            throw var21;
         }

         fis.close();
      }

      Set<String> addedKeys = new HashSet();
      Set<String> conflictKeys = new HashSet();

      for(Object keyObj : newProperties.keySet()) {
         String originalKey = keyObj.toString();
         String value = newProperties.getProperty(originalKey);
         String prefixedKey = String.format("mascot.%s.%s", mascotName, originalKey);
         if (existingProperties.containsKey(originalKey)) {
            String existingValue = existingProperties.getProperty(originalKey);
            if (!existingValue.equals(value)) {
               conflictKeys.add(originalKey);
               existingProperties.setProperty(prefixedKey, value);
            }
         } else {
            existingProperties.setProperty(originalKey, value);
            existingProperties.setProperty(prefixedKey, value);
            addedKeys.add(originalKey);
         }
      }

      addMergeRecord(existingProperties, mascotName, addedKeys, conflictKeys);
      FileOutputStream fos = new FileOutputStream(targetFile);

      try {
         OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

         try {
            existingProperties.store(writer, String.format("Merged localization for mascot: %s at %s", mascotName, new Date()));
         } catch (Throwable var18) {
            try {
               writer.close();
            } catch (Throwable var15) {
               var18.addSuppressed(var15);
            }

            throw var18;
         }

         writer.close();
      } catch (Throwable var19) {
         try {
            fos.close();
         } catch (Throwable var14) {
            var19.addSuppressed(var14);
         }

         throw var19;
      }

      fos.close();
      log.info(String.format("Merged %s: added %d keys, %d conflicts resolved", languageFile, addedKeys.size(), conflictKeys.size()));
      return true;
   }

   private static void addMergeRecord(Properties props, String mascotName, Set addedKeys, Set conflictKeys) {
      String timestamp = String.valueOf(System.currentTimeMillis());
      props.setProperty("_merge.mascot." + mascotName + ".timestamp", timestamp);
      props.setProperty("_merge.mascot." + mascotName + ".added", String.join(",", addedKeys));
      props.setProperty("_merge.mascot." + mascotName + ".conflicts", String.join(",", conflictKeys));
   }

   public static boolean removeLocalizationEntries(String mascotName) {
      try {
         File confDir = new File("conf");
         if (!confDir.exists()) {
            return true;
         } else {
            File[] languageFiles = confDir.listFiles((dir, name) -> name.startsWith("language") && name.endsWith(".properties"));
            if (languageFiles == null) {
               return true;
            } else {
               for(File langFile : languageFiles) {
                  removeMascotEntries(langFile, mascotName);
               }

               log.info("Removed localization entries for mascot: " + mascotName);
               return true;
            }
         }
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to remove localization entries", e);
         return false;
      }
   }

   private static void removeMascotEntries(File langFile, String mascotName) throws IOException {
      Properties props = new Properties();
      FileInputStream fis = new FileInputStream(langFile);

      try {
         InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);

         try {
            props.load(reader);
         } catch (Throwable var18) {
            try {
               reader.close();
            } catch (Throwable var15) {
               var18.addSuppressed(var15);
            }

            throw var18;
         }

         reader.close();
      } catch (Throwable var19) {
         try {
            fis.close();
         } catch (Throwable var14) {
            var19.addSuppressed(var14);
         }

         throw var19;
      }

      fis.close();
      String addedKeysStr = props.getProperty("_merge.mascot." + mascotName + ".added", "");
      String conflictKeysStr = props.getProperty("_merge.mascot." + mascotName + ".conflicts", "");
      Set<String> addedKeys = addedKeysStr.isEmpty() ? new HashSet() : new HashSet(Arrays.asList(addedKeysStr.split(",")));
      Set<String> conflictKeys = conflictKeysStr.isEmpty() ? new HashSet() : new HashSet(Arrays.asList(conflictKeysStr.split(",")));

      for(String key : addedKeys) {
         if (!conflictKeys.contains(key)) {
            props.remove(key);
         }
      }

      String prefix = "mascot." + mascotName + ".";
      props.entrySet().removeIf((entry) -> entry.getKey().toString().startsWith(prefix));
      props.remove("_merge.mascot." + mascotName + ".timestamp");
      props.remove("_merge.mascot." + mascotName + ".added");
      props.remove("_merge.mascot." + mascotName + ".conflicts");
      FileOutputStream fos = new FileOutputStream(langFile);

      try {
         OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

         try {
            props.store(writer, "Removed entries for mascot: " + mascotName);
         } catch (Throwable var16) {
            try {
               writer.close();
            } catch (Throwable var13) {
               var16.addSuppressed(var13);
            }

            throw var16;
         }

         writer.close();
      } catch (Throwable var17) {
         try {
            fos.close();
         } catch (Throwable var12) {
            var17.addSuppressed(var12);
         }

         throw var17;
      }

      fos.close();
   }

   public static String getLocalizedValue(String mascotName, String key, Properties langProps) {
      String prefixedKey = String.format("mascot.%s.%s", mascotName, key);
      String value = langProps.getProperty(prefixedKey);
      return value != null ? value : langProps.getProperty(key);
   }

   public static LocalizationConflictReport checkConflicts(String mascotName, Map<String, Properties> newLocalization) {
      LocalizationConflictReport report = new LocalizationConflictReport();
      File confDir = new File("conf");
      if (!confDir.exists()) {
         return report;
      } else {
         for(Map.Entry<String, Properties> entry : newLocalization.entrySet()) {
            String languageFile = entry.getKey();
            Properties newProps = entry.getValue();
            File existingFile = new File(confDir, languageFile);
            if (existingFile.exists()) {
               try {
                  Properties existingProps = new Properties();
                  FileInputStream fis = new FileInputStream(existingFile);

                  try {
                     InputStreamReader reader = new InputStreamReader(fis, "UTF-8");

                     try {
                        existingProps.load(reader);
                     } catch (Throwable var17) {
                        try {
                           reader.close();
                        } catch (Throwable var16) {
                           var17.addSuppressed(var16);
                        }

                        throw var17;
                     }

                     reader.close();
                  } catch (Throwable var18) {
                     try {
                        fis.close();
                     } catch (Throwable var15) {
                        var18.addSuppressed(var15);
                     }

                     throw var18;
                  }

                  fis.close();

                  for(Object keyObj : newProps.keySet()) {
                     String key = keyObj.toString();
                     if (existingProps.containsKey(key)) {
                        String existingValue = existingProps.getProperty(key);
                        String newValue = newProps.getProperty(key);
                        if (!existingValue.equals(newValue)) {
                           report.addConflict(languageFile, key, existingValue, newValue);
                        }
                     }
                  }
               } catch (IOException e) {
                  log.warning("Failed to check conflicts for " + languageFile + ": " + e.getMessage());
               }
            }
         }

         return report;
      }
   }
}
