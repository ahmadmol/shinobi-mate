package com.group_finity.mascot;

/** Installer holds this per-session mutex until file replacement has finished. */
public final class WindowsMaintenance {
 private WindowsMaintenance(){}
 public static boolean active(){
  if(!com.sun.jna.Platform.isWindows())return false;
  var kernel=com.sun.jna.platform.win32.Kernel32.INSTANCE;
  var handle=kernel.OpenMutex(0x00100000,false,"Local\\ShinobiMate.Maintenance");
  if(handle==null)return false;
  kernel.CloseHandle(handle);return true;
 }
}
