package com.group_finity.mascot.win.jna;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class POINT extends Structure {
   public int x;
   public int y;

   protected List getFieldOrder() {
      return Arrays.asList("x", "y");
   }
}
