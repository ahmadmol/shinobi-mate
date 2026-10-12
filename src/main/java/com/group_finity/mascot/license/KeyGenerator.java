package com.group_finity.mascot.license;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyGenerator {
   private static final Logger logger = Logger.getLogger(KeyGenerator.class.getName());
   private static final String PRIVATE_KEY_STRING = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=";

   public static void main(String[] args) {
      System.out.println("=== Shimeji-Live License Key Generator ===");
      System.out.println("WARNING: This tool should only be used by authorized personnel!");
      System.out.println();

      try {
         Scanner scanner = new Scanner(System.in);

         try {
            label52:
            while(true) {
               System.out.println("Select an option:");
               System.out.println("1. Generate Advanced Key");
               System.out.println("2. Generate Special Key");
               System.out.println("3. Generate Key Pair (for setup)");
               System.out.println("4. Exit");
               System.out.print("Choice: ");
               switch (scanner.nextLine().trim()) {
                  case "1" -> generateAdvancedKey(scanner);
                  case "2" -> generateSpecialKey(scanner);
                  case "3" -> generateKeyPair();
                  case "4" -> {
                     System.out.println("Goodbye!");
                     break label52;
                  }
                  default -> System.out.println("Invalid choice. Please try again.");
               }

               System.out.println();
            }
         } catch (Throwable var6) {
            try {
               scanner.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         scanner.close();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Error in key generation: {0}", e.getMessage());
      }
   }

   private static void generateAdvancedKey(Scanner scanner) throws Exception {
      System.out.print("Enter validity days for Advanced Key: ");
      int validityDays = Integer.parseInt(scanner.nextLine().trim());
      String key = generateLicenseKey(LicenseLevel.ADVANCED_KEY, validityDays);
      System.out.println("Generated Advanced Key:");
      System.out.println(key);
      System.out.println("Validity: " + validityDays + " days");
   }

   private static void generateSpecialKey(Scanner scanner) throws Exception {
      System.out.print("Enter validity days for Special Key: ");
      int validityDays = Integer.parseInt(scanner.nextLine().trim());
      String key = generateLicenseKey(LicenseLevel.SPECIAL_KEY, validityDays);
      System.out.println("Generated Special Key:");
      System.out.println(key);
      System.out.println("Validity: " + validityDays + " days");
      System.out.println("WARNING: This key enables key generation capabilities!");
   }

   private static void generateKeyPair() throws Exception {
      System.out.println("Generating new RSA key pair...");
      KeyPair keyPair = CryptoUtils.generateKeyPair();
      String publicKeyString = CryptoUtils.publicKeyToString(keyPair.getPublic());
      String privateKeyString = CryptoUtils.privateKeyToString(keyPair.getPrivate());
      System.out.println("Public Key (embed this in LicenseManager):");
      System.out.println(publicKeyString);
      System.out.println();
      System.out.println("Private Key (keep this secure for key generation):");
      System.out.println(privateKeyString);
      System.out.println();
      System.out.println("IMPORTANT: Store the private key securely and update the constants in this class!");
   }

   public static String generateLicenseKey(LicenseLevel level, int validityDays) throws Exception {
      LocalDateTime issueTime = LocalDateTime.now();
      String keyId = CryptoUtils.generateKeyId();
      LicenseKeyData keyData = new LicenseKeyData(level, validityDays, issueTime, "", keyId);
      PrivateKey privateKey = CryptoUtils.stringToPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=");
      String rawData = keyData.getRawDataForSignature();
      String signature = CryptoUtils.signData(rawData, privateKey);
      String keyString = String.format("%s|%s|%d|%s|%s|%s", "1.0", level.getCode(), validityDays, issueTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), keyId, signature);
      return Base64.getEncoder().encodeToString(keyString.getBytes());
   }

   public static boolean validateGeneratedKey(String keyString) {
      LicenseManager manager = LicenseManager.getInstance();
      return manager.validateKey(keyString);
   }
}
