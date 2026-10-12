package com.group_finity.mascot.license;

public enum LicenseLevel {
   NO_KEY("NO_KEY", "Free Version", "Basic functionality only"),
   ADVANCED_KEY("ADVANCED", "Advanced Version", "Full functionality with advanced features"),
   SPECIAL_KEY("SPECIAL", "Developer Version", "All features plus key generation capabilities");

   private final String code;
   private final String displayName;
   private final String description;

   private LicenseLevel(String code, String displayName, String description) {
      this.code = code;
      this.displayName = displayName;
      this.description = description;
   }

   public String getCode() {
      return this.code;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public static LicenseLevel fromCode(String code) {
      for(LicenseLevel level : values()) {
         if (level.code.equals(code)) {
            return level;
         }
      }

      return NO_KEY;
   }

   public boolean hasPermission(LicenseLevel requiredLevel) {
      return this.ordinal() >= requiredLevel.ordinal();
   }

   public String toString() {
      return this.displayName;
   }

   // $FF: synthetic method
   private static LicenseLevel[] $values() {
      return new LicenseLevel[]{NO_KEY, ADVANCED_KEY, SPECIAL_KEY};
   }
}
