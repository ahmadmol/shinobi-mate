package com.group_finity.mascot.animationeditor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectScanner {
   public static List<File> scanProjects(File imgDirectory) {
      List<File> projects = new ArrayList<>();
      if (imgDirectory.exists() && imgDirectory.isDirectory()) {
         File[] subdirs = imgDirectory.listFiles(File::isDirectory);
         if (subdirs != null) {
            projects.addAll(Arrays.asList(subdirs));
         }
         return projects;
      } else {
         return projects;
      }
   }

   public static List<File> scanImageFiles(File projectDirectory) {
      List<File> imageFiles = new ArrayList<>();
      if (projectDirectory.exists() && projectDirectory.isDirectory()) {
         scanImageFilesRecursive(projectDirectory, imageFiles);
         return imageFiles;
      } else {
         return imageFiles;
      }
   }

   private static void scanImageFilesRecursive(File directory, List<File> imageFiles) {
      File[] files = directory.listFiles();
      if (files != null) {
         for(File file : files) {
            if (file.isDirectory()) {
               scanImageFilesRecursive(file, imageFiles);
            } else if (isImageFile(file)) {
               imageFiles.add(file);
            }
         }
      }
   }

   public static boolean isImageFile(File file) {
      if (!file.isFile()) {
         return false;
      } else {
         String name = file.getName().toLowerCase();
         return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".tiff");
      }
   }

   public static File getConfigFile(File projectDirectory) {
      return new File(projectDirectory, "conf/actions.xml");
   }

   public static boolean isValidProject(File projectDirectory) {
      return getConfigFile(projectDirectory).exists();
   }

   public static File getFirstValidProject(File imgDirectory) {
      for(File project : scanProjects(imgDirectory)) {
         if (isValidProject(project)) {
            return project;
         }
      }

      return null;
   }

   public static File getNextProject(File imgDirectory, File currentProject) {
      List<File> projects = scanProjects(imgDirectory);
      if (projects.isEmpty()) {
         return null;
      } else {
         int currentIndex = -1;

         for(int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).equals(currentProject)) {
               currentIndex = i;
               break;
            }
         }

         int nextIndex = (currentIndex + 1) % projects.size();
         return projects.get(nextIndex);
      }
   }

   public static File getPreviousProject(File imgDirectory, File currentProject) {
      List<File> projects = scanProjects(imgDirectory);
      if (projects.isEmpty()) {
         return null;
      } else {
         int currentIndex = -1;

         for(int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).equals(currentProject)) {
               currentIndex = i;
               break;
            }
         }

         int prevIndex = currentIndex <= 0 ? projects.size() - 1 : currentIndex - 1;
         return projects.get(prevIndex);
      }
   }
}
