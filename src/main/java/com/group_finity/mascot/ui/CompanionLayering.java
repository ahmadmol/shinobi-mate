package com.group_finity.mascot.ui;

import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;

/** Product controls must stay reachable even when companions normally float above desktop apps. */
public final class CompanionLayering {
 private CompanionLayering(){ }
 private static boolean control(Window window){return window.isShowing()&&(window instanceof Dialog||window instanceof Frame&&window.getClass().getName().startsWith("com.group_finity.mascot."));}
 public static boolean coversControls(Rectangle bounds){for(Window window:Window.getWindows())if(control(window)&&window.getBounds().intersects(bounds))return true;return false;}
 public static boolean alwaysOnTop() {
  if(!RuntimeSettings.flag("AlwaysOnTop",true))return false;
  for(Window window:Window.getWindows())if(control(window))return false;
  return true;
 }
}
