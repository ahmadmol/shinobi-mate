package com.group_finity.mascot.settings;
import com.group_finity.mascot.Main;
import java.io.IOException;
/** Write-through settings facade shared by new UI and services. */
public final class SettingsService {
 private SettingsService() { }
 public static void restoreDefaults()throws IOException {
  var current=RuntimeSettings.properties();var defaults=SettingsStore.defaults();
  // Keep the selected language, active characters and onboarding state. Profile data is separate.
  for(String key:java.util.List.of("Language","ActiveShimeji","OnboardingComplete",CharacterSession.HISTORY))if(current.containsKey(key))defaults.setProperty(key,current.getProperty(key));
  SettingsStore.sanitize(defaults);SettingsStore.save(defaults,SettingsStore.path());
  synchronized(current){current.clear();current.putAll(defaults);}
  Main app=Main.getInstance();app.refreshProductLanguage();app.refreshProductTheme();app.reloadAppearance();app.reloadAllCloudMessages();
  com.group_finity.mascot.audio.SoundSystem.refresh();com.group_finity.mascot.sound.Sounds.refreshVolumes();
 }
 public static void set(String key,String value) throws IOException {
  var p=RuntimeSettings.properties();
  synchronized(p) {
   String old=p.getProperty(key);p.setProperty(key,value);
   try{SettingsStore.save(p,SettingsStore.path());}
   catch(IOException ex){if(old==null)p.remove(key);else p.setProperty(key,old);throw ex;}
  }
  Main app=Main.getInstance();
  if(key.equals("Language")) app.refreshProductLanguage();
  if(key.equals("FlatLafTheme")) app.refreshProductTheme();
  if(key.equals("Scaling")||key.equals("Opacity")) app.reloadAppearance();
  if(key.equals("CustomCloudEnabled"))app.reloadAllCloudMessages();
  com.group_finity.mascot.audio.SoundSystem.refresh();
  com.group_finity.mascot.sound.Sounds.refreshVolumes();
 }
}
