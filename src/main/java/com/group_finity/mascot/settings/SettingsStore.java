package com.group_finity.mascot.settings;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Properties;

/** Preserve legacy keys; a failed save never truncates the last good settings. */
public final class SettingsStore {
 private SettingsStore() { }
 public static Path path() {return com.group_finity.mascot.AppPaths.userData().resolve("settings.properties");}
 public static Properties load() throws IOException {
  Properties p=defaults();Path source=path();
  if(Files.isRegularFile(source)) {
   try {
    if(Files.size(source)>256000)throw new IllegalArgumentException("Settings file too large");
    Properties user=new Properties();try(var in=Files.newInputStream(source)){user.load(in);}p.putAll(user);
   }catch(IllegalArgumentException ex){
    // Preserve corrupt data before replacing it with safe defaults.
    Files.copy(source,source.resolveSibling("settings-corrupt-"+System.currentTimeMillis()+".properties"));
    javax.swing.JOptionPane.showMessageDialog(null,"ملف الإعدادات تالف؛ تم الاحتفاظ بنسخة واستعادة القيم الافتراضية.\nSettings were corrupt; a backup was kept and defaults restored.","ShinobiMate",javax.swing.JOptionPane.WARNING_MESSAGE);
   }
  }
  sanitize(p);return p;
 }
 public static Properties defaults() {
  Properties p=new Properties();p.setProperty("Language","ar");p.setProperty("OnboardingComplete","false");
  p.setProperty("AlwaysShowShimejiChooser","false");p.setProperty("PerformanceProfile","BALANCED");
  p.setProperty(CharacterSession.RESTORE,"false");p.setProperty(CharacterSession.HISTORY,"");
  p.setProperty("ProgressionEnabled","false");p.setProperty("ChallengesEnabled","false");
  p.setProperty("MasterVolume","70");p.setProperty("CharacterVolume","70");p.setProperty("MovementVolume","45");
  p.setProperty("CombatVolume","60");p.setProperty("UIVolume","30");p.setProperty("Mute","false");return p;
 }
 public static void sanitize(Properties p) {
  p.setProperty(CharacterSession.RESTORE,Boolean.toString(Boolean.parseBoolean(p.getProperty(CharacterSession.RESTORE,"false"))));
  String[][] numeric={{"MenuDPI","96","72","288"},{"Scaling","1","0.5","3"},{"Opacity","1","0.2","1"}};
  for(String[] n:numeric)p.setProperty(n[0],Double.toString(RuntimeSettings.number(p,n[0],Double.parseDouble(n[1]),Double.parseDouble(n[2]),Double.parseDouble(n[3]))));
  for(String[] audio:new String[][]{{"MasterVolume","70"},{"CharacterVolume","70"},{"MovementVolume","45"},{"CombatVolume","60"},{"UIVolume","30"}})
   p.setProperty(audio[0],Integer.toString((int)RuntimeSettings.number(p,audio[0],Double.parseDouble(audio[1]),0,100)));
  p.setProperty("MenuDPI",Integer.toString((int)RuntimeSettings.number(p,"MenuDPI",96,72,288)));
  if(!p.getProperty("WindowSize","600x500").matches("[1-9][0-9]{1,3}x[1-9][0-9]{1,3}"))p.setProperty("WindowSize","600x500");
  try{java.awt.Color.decode(p.getProperty("Background","#00FF00"));}catch(Exception ex){p.setProperty("Background","#00FF00");}
  if(!java.util.Set.of("ar","en","en-GB","zh","zh-CN").contains(p.getProperty("Language","ar")))p.setProperty("Language","ar");
  if(!java.util.Set.of("generic","win","mac","x11","wayland","virtual").contains(p.getProperty("Environment","generic")))p.setProperty("Environment","generic");
  for(String key:p.stringPropertyNames()) {
   String v=p.getProperty(key);
   if((v.equalsIgnoreCase("true")||v.equalsIgnoreCase("false")))p.setProperty(key,v.toLowerCase(java.util.Locale.ROOT));
  }
 }
 public static void save(Properties properties,Path destination) throws IOException {
  destination=destination.toAbsolutePath();Files.createDirectories(destination.getParent());
  Path temp=Files.createTempFile(destination.getParent(),".settings-",".tmp");
  try {
   synchronized(properties) {try(OutputStream out=Files.newOutputStream(temp)){properties.store(out,"ShinobiMate Configuration Options");}}
   try(FileChannel channel=FileChannel.open(temp,StandardOpenOption.WRITE)){channel.force(true);}
   try {Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException ex){Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING);}
  } finally {Files.deleteIfExists(temp);}
 }
}
