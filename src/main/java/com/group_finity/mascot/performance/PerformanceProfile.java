package com.group_finity.mascot.performance;
import com.group_finity.mascot.settings.RuntimeSettings;
public record PerformanceProfile(int fps,int effects,boolean shadows) {
 public static PerformanceProfile current() {
  return switch(RuntimeSettings.text("PerformanceProfile","BALANCED")) {
   case "LOW" -> new PerformanceProfile(10,8,false);
   case "HIGH" -> new PerformanceProfile(25,48,true);
   case "CUSTOM" -> new PerformanceProfile((int)RuntimeSettings.number("RenderFPS",25,5,25),(int)RuntimeSettings.number("VFXLevel",24,0,48),RuntimeSettings.flag("Shadows",true));
   default -> new PerformanceProfile(25,24,true);
  };
 }
 public int effectLimit(){return Math.min(effects,(int)RuntimeSettings.number("VFXLevel",48,0,48));}
}
