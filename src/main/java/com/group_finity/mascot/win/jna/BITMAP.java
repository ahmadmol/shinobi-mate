package com.group_finity.mascot.win.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class BITMAP extends Structure {
   public int bmType;
   public int bmWidth;
   public int bmHeight;
   public int bmWidthBytes;
   public short bmPlanes;
   public short bmBitsPixel;
   public Pointer bmBits;

   protected List getFieldOrder() {
      return Arrays.asList("bmType", "bmWidth", "bmHeight", "bmWidthBytes", "bmPlanes", "bmBitsPixel", "bmBits");
   }
}
