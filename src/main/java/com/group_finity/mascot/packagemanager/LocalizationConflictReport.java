package com.group_finity.mascot.packagemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalizationConflictReport {
   private final Map<String, List<ConflictEntry>> conflicts = new HashMap<>();

   public void addConflict(String languageFile, String key, String existingValue, String newValue) {
      this.conflicts.computeIfAbsent(languageFile, (k) -> new ArrayList<>()).add(new ConflictEntry(key, existingValue, newValue));
   }

   public boolean hasConflicts() {
      return !this.conflicts.isEmpty();
   }

   public Map<String, List<ConflictEntry>> getConflicts() {
      return this.conflicts;
   }

   public int getTotalConflicts() {
      return this.conflicts.values().stream().mapToInt(List::size).sum();
   }

   public String getFormattedReport() {
      if (!this.hasConflicts()) {
         return "无冲突 / No conflicts found";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append("本地化文件冲突报告 / Localization Conflicts Report:\n");
         sb.append("总计 / Total: ").append(this.getTotalConflicts()).append(" conflicts\n\n");

         for(Map.Entry<String, List<ConflictEntry>> entry : this.conflicts.entrySet()) {
            sb.append("文件 / File: ").append(entry.getKey()).append(" (").append(entry.getValue().size()).append(" conflicts)\n");

            for(ConflictEntry conflict : entry.getValue()) {
               sb.append("  ").append(conflict.toString()).append("\n");
            }

            sb.append("\n");
         }

         return sb.toString();
      }
   }

   public String getBriefReport() {
      return !this.hasConflicts() ? "无冲突 / No conflicts" : String.format("发现 %d 个冲突，涉及 %d 个语言文件 / Found %d conflicts in %d language files", this.getTotalConflicts(), this.conflicts.size(), this.getTotalConflicts(), this.conflicts.size());
   }

   public static class ConflictEntry {
      public final String key;
      public final String existingValue;
      public final String newValue;

      public ConflictEntry(String key, String existingValue, String newValue) {
         this.key = key;
         this.existingValue = existingValue;
         this.newValue = newValue;
      }

      public String toString() {
         return String.format("Key: %s\n  Existing: %s\n  New: %s", this.key, this.existingValue, this.newValue);
      }
   }
}
