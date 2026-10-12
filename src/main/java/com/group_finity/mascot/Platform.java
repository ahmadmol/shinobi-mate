package com.group_finity.mascot;

public enum Platform {
   x86(20),
   x86_64(24);

   private final int bitmapSize;

   private Platform(int bitmapSize) {
      this.bitmapSize = bitmapSize;
   }

   public int getBitmapSize() {
      return this.bitmapSize;
   }

   // $FF: synthetic method
   private static Platform[] $values() {
      return new Platform[]{x86, x86_64};
   }
}
