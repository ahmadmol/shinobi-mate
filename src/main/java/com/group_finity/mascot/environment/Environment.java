package com.group_finity.mascot.environment;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Environment {
   protected static Rectangle screenRect = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
   protected static Map screenRects = new HashMap();
   private static final Thread thread = new Thread(() -> {
      try {
         while(true) {
            updateScreenRect();
            Thread.sleep(5000L);
         }
      } catch (InterruptedException var1) {
      }
   });
   public final ComplexArea complexScreen = new ComplexArea();
   public final Area screen = new Area();
   public final Location cursor = new Location();

   protected abstract Area getWorkArea();

   public abstract Area getActiveIE();

   public abstract String getActiveIETitle();

   public abstract long getActiveWindowId();

   public abstract void moveActiveIE(Point var1);

   public abstract void restoreIE();

   public abstract void refreshCache();

   public abstract void dispose();

   private static void updateScreenRect() {
      Rectangle virtualBounds = new Rectangle();
      Map<String, Rectangle> screenRects = new HashMap();
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gs = ge.getScreenDevices();

      for(GraphicsDevice gd : gs) {
         screenRects.put(gd.getIDstring(), gd.getDefaultConfiguration().getBounds());
         virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
      }

      Environment.screenRects = screenRects;
      screenRect = virtualBounds;
   }

   protected static Rectangle getScreenRect() {
      return screenRect;
   }

   private static Point getCursorPos() {
      PointerInfo info = MouseInfo.getPointerInfo();
      return info != null ? info.getLocation() : new Point(0, 0);
   }

   public void init() {
      if (!thread.isAlive()) {
         thread.setDaemon(true);
         thread.setPriority(1);
         thread.start();
      }

      this.tick();
   }

   public void tick() {
      this.screen.set(getScreenRect());
      this.complexScreen.set(screenRects);
      this.cursor.set(getCursorPos());
   }

   public Area getScreen() {
      return this.screen;
   }

   public Collection<Area> getScreens() {
      return this.complexScreen.getAreas();
   }

   public ComplexArea getComplexScreen() {
      return this.complexScreen;
   }

   public Location getCursor() {
      return this.cursor;
   }

   public boolean isScreenTopBottom(Point location) {
      int count = 0;

      for(Area area : this.getScreens()) {
         if (area.getTopBorder().isOn(location)) {
            ++count;
         }

         if (area.getBottomBorder().isOn(location)) {
            ++count;
         }
      }

      if (count == 0) {
         if (this.getWorkArea().getTopBorder().isOn(location)) {
            return true;
         }

         if (this.getWorkArea().getBottomBorder().isOn(location)) {
            return true;
         }
      }

      return count == 1;
   }

   public boolean isScreenLeftRight(Point location) {
      int count = 0;

      for(Area area : this.getScreens()) {
         if (area.getLeftBorder().isOn(location)) {
            ++count;
         }

         if (area.getRightBorder().isOn(location)) {
            ++count;
         }
      }

      if (count == 0) {
         if (this.getWorkArea().getLeftBorder().isOn(location)) {
            return true;
         }

         if (this.getWorkArea().getRightBorder().isOn(location)) {
            return true;
         }
      }

      return count == 1;
   }
}
