package com.group_finity.mascot.packagemanager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MascotPackage {
   private String name;
   private String version = "1.0";
   private String author;
   private String description;
   private LocalDateTime createTime = LocalDateTime.now();
   private String previewImage;
   private List<String> imageFiles;
   private List<String> soundFiles;
   private List<String> languageFiles;
   private List<String> supportedLanguages;
   private Map<String, String> metadata;
   private boolean signed = false;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public LocalDateTime getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(LocalDateTime createTime) {
      this.createTime = createTime;
   }

   public String getPreviewImage() {
      return this.previewImage;
   }

   public void setPreviewImage(String previewImage) {
      this.previewImage = previewImage;
   }

   public List<String> getImageFiles() {
      return this.imageFiles;
   }

   public void setImageFiles(List<String> imageFiles) {
      this.imageFiles = imageFiles;
   }

   public List<String> getSoundFiles() {
      return this.soundFiles;
   }

   public void setSoundFiles(List<String> soundFiles) {
      this.soundFiles = soundFiles;
   }

   public List<String> getLanguageFiles() {
      return this.languageFiles;
   }

   public void setLanguageFiles(List<String> languageFiles) {
      this.languageFiles = languageFiles;
   }

   public List<String> getSupportedLanguages() {
      return this.supportedLanguages;
   }

   public void setSupportedLanguages(List<String> supportedLanguages) {
      this.supportedLanguages = supportedLanguages;
   }

   public Map<String, String> getMetadata() {
      return this.metadata;
   }

   public void setMetadata(Map<String, String> metadata) {
      this.metadata = metadata;
   }

   public boolean isSigned() {
      return this.signed;
   }

   public void setSigned(boolean signed) {
      this.signed = signed;
   }

   public boolean validatePackage() {
      boolean basicValidation = this.name != null && !this.name.isEmpty() && this.imageFiles != null && !this.imageFiles.isEmpty();
      boolean hasBasicLanguage = this.languageFiles == null || this.languageFiles.isEmpty() || this.languageFiles.stream().anyMatch((file) -> file.endsWith("language.properties"));
      return basicValidation && hasBasicLanguage;
   }

   public String toString() {
      return String.format("MascotPackage{name='%s', version='%s', author='%s', imageFiles=%d, soundFiles=%d, languageFiles=%d, languages=%s}", this.name, this.version, this.author, this.imageFiles != null ? this.imageFiles.size() : 0, this.soundFiles != null ? this.soundFiles.size() : 0, this.languageFiles != null ? this.languageFiles.size() : 0, this.supportedLanguages);
   }
}
