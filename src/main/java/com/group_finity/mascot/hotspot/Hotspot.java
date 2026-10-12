package com.group_finity.mascot.hotspot;

import com.group_finity.mascot.Mascot;
import java.awt.Point;
import java.awt.Shape;

public record Hotspot(String behaviour, Shape shape) {
   public boolean contains(Mascot mascot, Point point) {
      if (mascot.isLookRight()) {
         point = new Point(mascot.getBounds().width - point.x, point.y);
      }

      return this.shape.contains(point);
   }
}
