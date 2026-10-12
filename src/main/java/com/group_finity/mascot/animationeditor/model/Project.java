package com.group_finity.mascot.animationeditor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project {
   private String name;
   private File rootDirectory;
   private File configFile;
   private List<AnimationAction> actions;
   private List<File> imageFiles;

   public Project(String name, File rootDirectory) {
      this.name = name;
      this.rootDirectory = rootDirectory;
      this.configFile = new File(rootDirectory, "conf/actions.xml");
      this.actions = new ArrayList<>();
      this.imageFiles = new ArrayList<>();
      this.loadImageFiles();
   }

   private void loadImageFiles() {
      if (this.rootDirectory.exists() && this.rootDirectory.isDirectory()) {
         File[] files = this.rootDirectory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif");
         });
         if (files != null) {
            for(File file : files) {
               this.imageFiles.add(file);
            }
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public File getRootDirectory() {
      return this.rootDirectory;
   }

   public void setRootDirectory(File rootDirectory) {
      this.rootDirectory = rootDirectory;
   }

   public File getConfigFile() {
      return this.configFile;
   }

   public void setConfigFile(File configFile) {
      this.configFile = configFile;
   }

   public List<AnimationAction> getActions() {
      return this.actions;
   }

   public void setActions(List<AnimationAction> actions) {
      this.actions = actions;
   }

   public List<File> getImageFiles() {
      return this.imageFiles;
   }

   public void setImageFiles(List<File> imageFiles) {
      this.imageFiles = imageFiles;
   }

   public String toString() {
      return this.name;
   }
}
