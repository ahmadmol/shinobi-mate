package com.group_finity.mascot.sound;
import com.group_finity.mascot.audio.*;
import java.nio.file.Path;
import java.util.*;
/** Legacy pack sound declarations now share the bounded audio manager. */
public class Sounds {
 private static final Map<String,SoundEvent> EVENTS=Collections.synchronizedMap(new LinkedHashMap<>());
 public static void register(String key,Path file,float decibels){
  if(!Float.isFinite(decibels))return;
  synchronized(EVENTS){EVENTS.put(key,new SoundEvent("legacy",SoundEvent.Category.CHARACTER,List.of(file),Math.min(1,Math.pow(10,decibels/20)),120,2,false,40,5000));
   while(EVENTS.size()>128)EVENTS.remove(EVENTS.keySet().iterator().next());}
 }
 public static boolean contains(String key){return EVENTS.containsKey(key);}
 public static void play(String key,AudioManager.Scope scope){var event=EVENTS.get(key);if(event!=null)AudioManager.global().play(event,scope,0);}
 public static boolean isMuted(){return !com.group_finity.mascot.settings.RuntimeSettings.flag("Mute",false)&&com.group_finity.mascot.settings.RuntimeSettings.flag("Sounds",true);}
 public static void setMuted(boolean muted){if(muted)AudioManager.global().stopAll();else AudioManager.global().refresh();}
 public static void refreshVolumes(){AudioManager.global().refresh();}
 public static void stop(String pack,String relative){AudioManager.global().stopLegacy(pack,relative);}
}
