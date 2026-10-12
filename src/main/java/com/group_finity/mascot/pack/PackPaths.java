package com.group_finity.mascot.pack;
import java.nio.file.*;
import java.util.Locale;
public final class PackPaths {
 private PackPaths(){ }
 public static String id(String value) {
  if(value==null||!value.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,63}")||reserved(value))throw new IllegalArgumentException("Invalid pack id");return value;
 }
 private static boolean reserved(String s){return s.toUpperCase(Locale.ROOT).matches("CON|PRN|AUX|NUL|COM[0-9]|LPT[0-9]");}
 public static Path inside(Path root,String relative) {
  if(relative==null||relative.isBlank()||relative.startsWith("/")||relative.startsWith("\\")||relative.contains(":")||relative.contains("\\")||relative.indexOf(0)>=0)
   throw new IllegalArgumentException("Unsafe relative pack path");
  for(String part:relative.split("/",-1))if(part.isBlank()||part.equals(".")||part.equals("..")||part.endsWith(".")||part.endsWith(" ")||reserved(part.split("\\.",2)[0]))throw new IllegalArgumentException("Unsafe pack path component");
  Path normalized=root.toAbsolutePath().normalize().resolve(relative).normalize();
  if(!normalized.startsWith(root.toAbsolutePath().normalize()))throw new IllegalArgumentException("Path escapes pack");
  return normalized;
 }
}
