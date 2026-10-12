package com.group_finity.mascot.settings;

import com.group_finity.mascot.Main;
import java.util.Properties;

/** Validated reads also tolerate edited or old property files. */
public final class RuntimeSettings {
 private RuntimeSettings() { }
 public static Properties properties(){return Main.getInstance().getProperties();}
 public static boolean flag(String key,boolean fallback){String value=properties().getProperty(key,"");return value.equalsIgnoreCase("true")?true:value.equalsIgnoreCase("false")?false:fallback;}
 public static String text(String key,String fallback){return properties().getProperty(key,fallback);}
 public static double number(String key,double fallback,double min,double max) {
  return number(properties(),key,fallback,min,max);
 }
 public static double number(Properties p,String key,double fallback,double min,double max) {
  try{double v=Double.parseDouble(p.getProperty(key,Double.toString(fallback)));return Double.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
  catch(NumberFormatException ex){return fallback;}
 }
 public static boolean arabic(){return text("Language","ar").startsWith("ar");}
 public static String label(String ar,String en){return arabic()?ar:en;}
}
