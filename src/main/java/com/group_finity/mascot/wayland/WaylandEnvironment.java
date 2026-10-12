package com.group_finity.mascot.wayland;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class WaylandEnvironment extends Environment {
   public WindowContainer ieContainer = new WindowContainer();
   private Timer updateTimer;
   private long lastUpdateTime = 0L;
   private static final long UPDATE_INTERVAL = 1000L;
   private Area workArea = new Area();
   private Rectangle screenRect;
   private boolean initialized = false;
   private Area activeIE = new Area();
   private String activeIETitle = "";
   private long activeWindowId = 0L;

   public WaylandEnvironment() {
      this.initializeWaylandEnvironment();
      this.startUpdateTimer();
   }

   private void initializeWaylandEnvironment() {
      try {
         this.updateScreenInfo();
         this.initialized = true;
      } catch (Exception e) {
         System.err.println("Failed to initialize Wayland environment: " + e.getMessage());
         this.initializeFallback();
      }

   }

   private void initializeFallback() {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      DisplayMode dm = gd.getDisplayMode();
      this.screenRect = new Rectangle(0, 0, dm.getWidth(), dm.getHeight());
      this.workArea.set(this.screenRect);
      this.initialized = true;
   }

   private void updateScreenInfo() {
      try {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice[] screens = ge.getScreenDevices();
         if (screens.length > 0) {
            Rectangle bounds = screens[0].getDefaultConfiguration().getBounds();
            this.screenRect = bounds;
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Insets insets = toolkit.getScreenInsets(screens[0].getDefaultConfiguration());
            Rectangle workRect = new Rectangle(bounds.x + insets.left, bounds.y + insets.top, bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
            this.workArea.set(workRect);
         }
      } catch (Exception e) {
         System.err.println("Failed to update screen info: " + e.getMessage());
         this.initializeFallback();
      }

   }

   private void startUpdateTimer() {
      if (this.updateTimer != null) {
         this.updateTimer.cancel();
      }

      this.updateTimer = new Timer("WaylandEnvironmentUpdater", true);
      this.updateTimer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            WaylandEnvironment.this.updateEnvironment();
         }
      }, 1000L, 1000L);
   }

   private void updateEnvironment() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastUpdateTime >= 1000L) {
         this.lastUpdateTime = currentTime;

         try {
            this.updateWindowList();
            this.updateScreenInfo();
         } catch (Exception e) {
            System.err.println("Error updating Wayland environment: " + e.getMessage());
         }

      }
   }

   private void updateWindowList() {
      try {
         this.ieContainer.clear();
         Frame[] frames = Frame.getFrames();

         for(Frame frame : frames) {
            if (frame.isVisible() && !frame.getName().startsWith("Shimeji")) {
               Rectangle bounds = frame.getBounds();
               Area windowArea = new Area();
               windowArea.set(bounds);
               this.ieContainer.addIE(windowArea);
            }
         }
      } catch (Exception e) {
         System.err.println("Error updating window list: " + e.getMessage());
      }

   }

   protected Area getWorkArea() {
      if (!this.initialized) {
         this.initializeWaylandEnvironment();
      }

      return this.workArea;
   }

   public Area getActiveIE() {
      return this.activeIE;
   }

   public String getActiveIETitle() {
      return this.activeIETitle != null ? this.activeIETitle : "";
   }

   public long getActiveWindowId() {
      return this.activeWindowId;
   }

   public void moveActiveIE(Point point) {
      try {
         Frame[] frames = Frame.getFrames();

         for(Frame frame : frames) {
            if (frame.isVisible() && frame.getName().startsWith("Shimeji")) {
               Point currentLocation = frame.getLocation();
               frame.setLocation(currentLocation.x + point.x, currentLocation.y + point.y);
            }
         }
      } catch (Exception e) {
         System.err.println("Error moving windows: " + e.getMessage());
      }

   }

   public void restoreIE() {
      try {
         Frame[] frames = Frame.getFrames();

         for(Frame frame : frames) {
            if (frame.getExtendedState() == 1) {
               frame.setExtendedState(0);
            }
         }
      } catch (Exception e) {
         System.err.println("Error restoring windows: " + e.getMessage());
      }

   }

   public void refreshCache() {
      this.updateEnvironment();
   }

   public void dispose() {
      if (this.updateTimer != null) {
         this.updateTimer.cancel();
         this.updateTimer = null;
      }

   }

   public Collection getWindows() {
      this.updateWindowList();
      return this.ieContainer.getWindowAreas();
   }

   public static boolean isWayland() {
      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
      String sessionType = System.getenv("XDG_SESSION_TYPE");
      return waylandDisplay != null && !waylandDisplay.isEmpty() || sessionType != null && sessionType.equals("wayland");
   }

   public String getCompositorName() {
      String desktop = System.getenv("XDG_CURRENT_DESKTOP");
      return desktop != null ? desktop.toLowerCase() : "unknown";
   }

   public static class WindowContainer extends Hashtable {
      private static final long serialVersionUID = 1L;

      public void addIE(Area area) {
         this.put(System.currentTimeMillis(), area);
      }

      public Collection getWindowAreas() {
         return this.values();
      }
   }
}
