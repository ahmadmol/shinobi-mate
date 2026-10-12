package com.group_finity.mascot.license;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LicenseManager {
   private static final Logger logger = Logger.getLogger(LicenseManager.class.getName());
   private static final String EMBEDDED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlKP0EIsGHPLcrMOVZoBoc8gDruZ8Yn1crQCsji8c2x6ItSG3zmMT4RulObh1oTapnt8sk19QLl4Mbs7q/MlLOZqV1aI5xddJyZ0zHJRpH8PjoUlpTNJEz9pFclZIOWSfkT8DYEF6eX3uJL5105KYdoKLf5/Tdvjqe0nD89OWRLHvGH9UHF9CAAG+tmWuqaiyIRPSzRp9rlWpxYQVvi90cbPXzrEGBnhTKvLGGN3jczhXlafQZ3QQqp2OoqairhPsVKK+v6zu41m+QqrhoyAqp0Qlk+5teLMiGi5PvpSU1vUYWEHA6wI2INcUcR9UKMdBMJ0AQ9Hgyb72CdZeu19VJQIDAQAB";
   private static final String LICENSE_FILE = "license.properties";
   private static final String ACTIVATION_TIME_KEY = "activation.time";
   private static final String LICENSE_KEY_KEY = "license.key";
   private static final String LICENSE_LEVEL_KEY = "license.level";
   private static LicenseManager instance;
   private LicenseKeyData currentLicenseKey;
   private LocalDateTime activationTime;
   private final Path licenseFilePath = com.group_finity.mascot.AppPaths.userData().resolve("license.properties");

   private LicenseManager() {
      this.loadLicenseFromFile();
   }

   public static synchronized LicenseManager getInstance() {
      if (instance == null) {
         instance = new LicenseManager();
      }

      return instance;
   }

   public boolean validateKey(String keyString) {
      try {
         LicenseKeyData keyData = this.parseKey(keyString);
         if (keyData == null) {
            logger.warning("Failed to parse license key");
            return false;
         } else {
            PublicKey publicKey = CryptoUtils.stringToPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlKP0EIsGHPLcrMOVZoBoc8gDruZ8Yn1crQCsji8c2x6ItSG3zmMT4RulObh1oTapnt8sk19QLl4Mbs7q/MlLOZqV1aI5xddJyZ0zHJRpH8PjoUlpTNJEz9pFclZIOWSfkT8DYEF6eX3uJL5105KYdoKLf5/Tdvjqe0nD89OWRLHvGH9UHF9CAAG+tmWuqaiyIRPSzRp9rlWpxYQVvi90cbPXzrEGBnhTKvLGGN3jczhXlafQZ3QQqp2OoqairhPsVKK+v6zu41m+QqrhoyAqp0Qlk+5teLMiGi5PvpSU1vUYWEHA6wI2INcUcR9UKMdBMJ0AQ9Hgyb72CdZeu19VJQIDAQAB");
            String rawData = keyData.getRawDataForSignature();
            if (!CryptoUtils.verifySignature(rawData, keyData.getSignature(), publicKey)) {
               logger.warning("License key signature verification failed");
               return false;
            } else if (keyData.isExpired()) {
               logger.warning("License key has expired");
               return false;
            } else {
               this.currentLicenseKey = keyData;
               this.activationTime = LocalDateTime.now();
               this.saveLicenseToFile(keyString, keyData.getLevel());
               logger.info("License key validated successfully: " + String.valueOf(keyData));
               return true;
            }
         }
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Error validating license key", e);
         return false;
      }
   }

   public LicenseLevel getCurrentLicenseLevel() {
      if (this.currentLicenseKey == null) {
         return LicenseLevel.NO_KEY;
      } else if (this.currentLicenseKey.isExpired()) {
         this.clearActivation();
         return LicenseLevel.NO_KEY;
      } else {
         return this.currentLicenseKey.getLevel();
      }
   }

   public long getDaysRemaining() {
      return this.currentLicenseKey != null && !this.currentLicenseKey.isExpired() ? this.currentLicenseKey.getRemainingDays() : 0L;
   }

   public long getHoursRemaining() {
      return this.currentLicenseKey != null && !this.currentLicenseKey.isExpired() ? this.currentLicenseKey.getRemainingHours() : 0L;
   }

   public String getLicenseStatusInfo() {
      LicenseLevel level = this.getCurrentLicenseLevel();
      if (level == LicenseLevel.NO_KEY) {
         return "Free Version - No active license";
      } else {
         long daysRemaining = this.getDaysRemaining();
         long hoursRemaining = this.getHoursRemaining();
         if (daysRemaining > 0L) {
            return String.format("%s - %d days remaining", level.getDisplayName(), daysRemaining);
         } else {
            return hoursRemaining > 0L ? String.format("%s - %d hours remaining", level.getDisplayName(), hoursRemaining) : String.format("%s - Expires soon", level.getDisplayName());
         }
      }
   }

   public boolean hasPermission(LicenseLevel requiredLevel) {
      return this.getCurrentLicenseLevel().hasPermission(requiredLevel);
   }

   public void clearActivation() {
      this.currentLicenseKey = null;
      this.activationTime = null;

      try {
         Files.deleteIfExists(this.licenseFilePath);
         logger.info("License activation cleared");
      } catch (IOException e) {
         logger.log(Level.WARNING, "Failed to delete license file", e);
      }

   }

   private LicenseKeyData parseKey(String keyString) {
      try {
         String decoded = new String(Base64.getDecoder().decode(keyString));
         String[] parts = decoded.split("\\|");
         if (parts.length != 6) {
            logger.warning("Invalid key format: incorrect number of parts");
            return null;
         } else {
            String version = parts[0];
            if (!"1.0".equals(version)) {
               logger.warning("Unsupported key version: " + version);
               return null;
            } else {
               LicenseLevel level = LicenseLevel.fromCode(parts[1]);
               int validityDays = Integer.parseInt(parts[2]);
               LocalDateTime issueTime = LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
               String keyId = parts[4];
               String signature = parts[5];
               return new LicenseKeyData(level, validityDays, issueTime, signature, keyId);
            }
         }
      } catch (Exception e) {
         logger.log(Level.WARNING, "Failed to parse key string", e);
         return null;
      }
   }

   private void loadLicenseFromFile() {
      try {
         if (!Files.exists(this.licenseFilePath, new LinkOption[0])) {
            return;
         }

         Properties props = new Properties();
         InputStream input = Files.newInputStream(this.licenseFilePath);

         try {
            props.load(input);
         } catch (Throwable var6) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (input != null) {
            input.close();
         }

         String keyString = props.getProperty("license.key");
         String activationTimeStr = props.getProperty("activation.time");
         if (keyString != null && activationTimeStr != null) {
            LicenseKeyData keyData = this.parseKey(keyString);
            if (keyData != null && !keyData.isExpired()) {
               this.currentLicenseKey = keyData;
               this.activationTime = LocalDateTime.parse(activationTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
               logger.info("License loaded from file: " + String.valueOf(keyData));
            } else {
               Files.deleteIfExists(this.licenseFilePath);
            }
         }
      } catch (Exception e) {
         logger.log(Level.WARNING, "Failed to load license from file", e);
      }

   }

   private void saveLicenseToFile(String keyString, LicenseLevel level) {
      try {
         Files.createDirectories(this.licenseFilePath.getParent());
         Properties props = new Properties();
         props.setProperty("license.key", keyString);
         props.setProperty("license.level", level.getCode());
         props.setProperty("activation.time", this.activationTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
         OutputStream output = Files.newOutputStream(this.licenseFilePath);

         try {
            props.store(output, "Shimeji-Live License Information");
         } catch (Throwable var8) {
            if (output != null) {
               try {
                  output.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (output != null) {
            output.close();
         }

         logger.info("License information saved to file");
      } catch (Exception e) {
         logger.log(Level.WARNING, "Failed to save license to file", e);
      }

   }
}
