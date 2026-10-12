package com.group_finity.mascot.win.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class MONITORINFO extends Structure {
   public NativeLong cbSize;
   public RECT rcMonitor;
   public RECT rcWork;
   public NativeLong dwFlags;

   protected List getFieldOrder() {
      return Arrays.asList("cbSize", "rcMonitor", "rcWork", "dwFlags");
   }
}
