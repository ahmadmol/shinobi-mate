package com.group_finity.mascot.win.jna;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class SIZE extends Structure {
   public int cx;
   public int cy;

   protected List getFieldOrder() {
      return Arrays.asList("cx", "cy");
   }
}
