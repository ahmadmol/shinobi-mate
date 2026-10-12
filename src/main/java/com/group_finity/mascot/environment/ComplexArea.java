package com.group_finity.mascot.environment;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ComplexArea {
   private final Map<String, Area> areas = new HashMap<>();

   public void set(Map<String, Rectangle> rectangles) {
      this.retain(rectangles.keySet());

      for(Map.Entry<String, Rectangle> e : rectangles.entrySet()) {
         this.set(e.getKey(), e.getValue());
      }
   }

   public void set(String name, Rectangle value) {
      for(Area area : this.areas.values()) {
         if (area.getLeft() == value.x && area.getTop() == value.y && area.getWidth() == value.width && area.getHeight() == value.height) {
            return;
         }
      }

      Area area = this.areas.get(name);
      if (area == null) {
         area = new Area();
         this.areas.put(name, area);
      }

      area.set(value);
   }

   public void retain(Collection<String> deviceNames) {
      this.areas.keySet().removeIf((key) -> !deviceNames.contains(key));
   }

   public FloorCeiling getBottomBorder(Point location) {
      FloorCeiling ret = null;

      for(Area area : this.areas.values()) {
         if (area.getBottomBorder().isOn(location)) {
            ret = area.getBottomBorder();
         }
      }

      for(Area area : this.areas.values()) {
         if (area.getTopBorder().isOn(location)) {
            ret = null;
         }
      }

      return ret;
   }

   public FloorCeiling getTopBorder(Point location) {
      FloorCeiling ret = null;

      for(Area area : this.areas.values()) {
         if (area.getTopBorder().isOn(location)) {
            ret = area.getTopBorder();
         }
      }

      for(Area area : this.areas.values()) {
         if (area.getBottomBorder().isOn(location)) {
            ret = null;
         }
      }

      return ret;
   }

   public Wall getLeftBorder(Point location) {
      Wall ret = null;

      for(Area area : this.areas.values()) {
         if (area.getLeftBorder().isOn(location)) {
            ret = area.getRightBorder();
         }
      }

      for(Area area : this.areas.values()) {
         if (area.getRightBorder().isOn(location)) {
            ret = null;
         }
      }

      return ret;
   }

   public Wall getRightBorder(Point location) {
      Wall ret = null;

      for(Area area : this.areas.values()) {
         if (area.getRightBorder().isOn(location)) {
            ret = area.getRightBorder();
         }
      }

      for(Area area : this.areas.values()) {
         if (area.getLeftBorder().isOn(location)) {
            ret = null;
         }
      }

      return ret;
   }

   public Collection<Area> getAreas() {
      return this.areas.values();
   }
}
