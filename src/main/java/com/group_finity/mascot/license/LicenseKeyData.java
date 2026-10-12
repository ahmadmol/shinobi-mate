package com.group_finity.mascot.license;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LicenseKeyData {
   private final LicenseLevel level;
   private final int validityDays;
   private final LocalDateTime issueTime;
   private final String signature;
   private final String keyId;
   public static final String KEY_VERSION = "1.0";

   public LicenseKeyData(LicenseLevel level, int validityDays, LocalDateTime issueTime, String signature, String keyId) {
      this.level = level;
      this.validityDays = validityDays;
      this.issueTime = issueTime;
      this.signature = signature;
      this.keyId = keyId;
   }

   public LicenseLevel getLevel() {
      return this.level;
   }

   public int getValidityDays() {
      return this.validityDays;
   }

   public LocalDateTime getIssueTime() {
      return this.issueTime;
   }

   public String getSignature() {
      return this.signature;
   }

   public String getKeyId() {
      return this.keyId;
   }

   public LocalDateTime getExpirationTime() {
      return this.issueTime.plusDays((long)this.validityDays);
   }

   public boolean isExpired() {
      return LocalDateTime.now().isAfter(this.getExpirationTime());
   }

   public long getRemainingDays() {
      return this.isExpired() ? 0L : Duration.between(LocalDateTime.now(), this.getExpirationTime()).toDays();
   }

   public long getRemainingHours() {
      return this.isExpired() ? 0L : Duration.between(LocalDateTime.now(), this.getExpirationTime()).toHours();
   }

   public String getRawDataForSignature() {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      return String.format("%s|%s|%d|%s|%s", "1.0", this.level.getCode(), this.validityDays, this.issueTime.format(formatter), this.keyId);
   }

   public String toString() {
      return String.format("LicenseKey[level=%s, validityDays=%d, remaining=%d days, keyId=%s]", this.level.getDisplayName(), this.validityDays, this.getRemainingDays(), this.keyId);
   }
}
