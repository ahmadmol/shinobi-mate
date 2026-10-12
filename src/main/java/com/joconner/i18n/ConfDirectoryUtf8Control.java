package com.joconner.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Loads language bundles from {@code conf/} on disk before the classpath copy
 * packaged inside the JAR, so a stale JAR cannot hide newer translations.
 */
public class ConfDirectoryUtf8Control extends Utf8ResourceBundleControl {
   private final File confDirectory;

   public ConfDirectoryUtf8Control(File confDirectory) {
      super(false);
      this.confDirectory = confDirectory;
   }

   @Override
   public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
      if ("java.properties".equals(format) && this.confDirectory != null) {
         String bundleName = this.toBundleName(baseName, locale);
         String resourceName = this.toResourceName(bundleName, "properties");
         if (resourceName != null && !resourceName.contains("://")) {
            File file = new File(this.confDirectory, resourceName);
            if (file.isFile()) {
               try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                  return new PropertyResourceBundle(reader);
               }
            }
         }
      }

      return super.newBundle(baseName, locale, format, loader, reload);
   }
}
