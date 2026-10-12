package com.group_finity.mascot.mac.jna;

import com.sun.jna.Structure;

@Structure.FieldOrder({"x", "y"})
public class CGPoint extends Structure {
   public double x;
   public double y;

   public CGPoint() {
      this((double)0.0F, (double)0.0F);
   }

   public CGPoint(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public int getX() {
      return (int)Math.round(this.x);
   }

   public int getY() {
      return (int)Math.round(this.y);
   }
}
