package com.group_finity.mascot.mac;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.mac.jna.AXUIElementRef;
import com.group_finity.mascot.mac.jna.AXValueRef;
import com.group_finity.mascot.mac.jna.CGPoint;
import com.group_finity.mascot.mac.jna.CGSize;
import com.group_finity.mascot.mac.jna.CarbonExtra;
import com.group_finity.mascot.mac.jna.ProcessSerialNumber;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

class MacEnvironment extends Environment {
   private static final Logger log = Logger.getLogger(MacEnvironment.class.getName());
   private static Area activeIE;
   private static Area frontmostWindow;
   private static final int screenWidth;
   private static final int screenHeight;
   private static CarbonExtra carbonEx;
   private static long myPID;
   private static long currentPID;
   private static Set touchedProcesses;
   static final CoreFoundation.CFStringRef kAXPosition;
   static final CoreFoundation.CFStringRef kAXSize;
   static final CoreFoundation.CFStringRef kAXFocusedWindow;
   static final CoreFoundation.CFStringRef kDock;
   static final CoreFoundation.CFStringRef kTileSize;
   static final CoreFoundation.CFStringRef kOrientation;
   static final CoreFoundation.CFStringRef kAXChildren;

   private static Rectangle getFrontmostAppRect() {
      long pid = getCurrentPID();
      AXUIElementRef application = carbonEx.AXUIElementCreateApplication(pid);
      PointerByReference windowp = new PointerByReference();
      Rectangle ret;
      if (carbonEx.AXUIElementCopyAttributeValue(application, kAXFocusedWindow, windowp) == 0L) {
         AXUIElementRef window = new AXUIElementRef();
         window.setPointer(windowp.getValue());
         ret = getRectOfWindow(window);
      } else {
         ret = null;
      }

      application.release();
      return ret;
   }

   private static long getFrontmostAppsPID() {
      ProcessSerialNumber frontProcessPsn = new ProcessSerialNumber();
      LongByReference frontProcessPidp = new LongByReference();
      carbonEx.GetFrontProcess(frontProcessPsn);
      carbonEx.GetProcessPID(frontProcessPsn, frontProcessPidp);
      return frontProcessPidp.getValue();
   }

   private static CGPoint getPositionOfWindow(AXUIElementRef window) {
      CGPoint position = new CGPoint();
      AXValueRef axvalue = new AXValueRef();
      PointerByReference valuep = new PointerByReference();
      carbonEx.AXUIElementCopyAttributeValue(window, kAXPosition, valuep);
      axvalue.setPointer(valuep.getValue());
      carbonEx.AXValueGetValue(axvalue, 1L, position.getPointer());
      position.read();
      return position;
   }

   private static CGSize getSizeOfWindow(AXUIElementRef window) {
      CGSize size = new CGSize();
      AXValueRef axvalue = new AXValueRef();
      PointerByReference valuep = new PointerByReference();
      carbonEx.AXUIElementCopyAttributeValue(window, kAXSize, valuep);
      axvalue.setPointer(valuep.getValue());
      carbonEx.AXValueGetValue(axvalue, 2L, size.getPointer());
      size.read();
      return size;
   }

   private static void moveFrontmostWindow(Point point) {
      AXUIElementRef application = carbonEx.AXUIElementCreateApplication(currentPID);
      PointerByReference windowp = new PointerByReference();
      if (carbonEx.AXUIElementCopyAttributeValue(application, kAXFocusedWindow, windowp) == 0L) {
         AXUIElementRef window = new AXUIElementRef();
         window.setPointer(windowp.getValue());
         moveWindow(window, point.x, point.y);
      }

      application.release();
   }

   private static void restoreWindowsNotIn(Rectangle rect) {
      Rectangle visibleArea = getWindowVisibleArea();

      for(long pid : getTouchedProcesses()) {
         AXUIElementRef application = carbonEx.AXUIElementCreateApplication(pid);

         for(AXUIElementRef window : getWindowsOf(application)) {
            window.retain();
            Rectangle windowRect = getRectOfWindow(window);
            if (!visibleArea.intersects(windowRect)) {
               moveWindow(window, 0, 0);
            }

            window.release();
         }

         application.release();
      }

   }

   private static Iterable<AXUIElementRef> getWindowsOf(AXUIElementRef application) {
      PointerByReference axWindowsp = new PointerByReference();
      Collection<AXUIElementRef> ret = new ArrayList();
      carbonEx.AXUIElementCopyAttributeValue(application, kAXChildren, axWindowsp);
      if (axWindowsp.getValue() == Pointer.NULL) {
         return ret;
      } else {
         CoreFoundation.CFArrayRef cfWindows = new CoreFoundation.CFArrayRef(axWindowsp.getValue());
         int i = 0;

         for(int count = cfWindows.getCount(); i < count; ++i) {
            Pointer p = cfWindows.getValueAtIndex(i);
            AXUIElementRef el = new AXUIElementRef();
            el.setPointer(p);
            ret.add(el);
         }

         return ret;
      }
   }

   private static Rectangle getRectOfWindow(AXUIElementRef window) {
      CGPoint pos = getPositionOfWindow(window);
      CGSize size = getSizeOfWindow(window);
      return new Rectangle(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
   }

   private static void moveWindow(AXUIElementRef window, int x, int y) {
      CGPoint position = new CGPoint((double)x, (double)y);
      position.write();
      AXValueRef axvalue = carbonEx.AXValueCreate(1L, position.getPointer());
      carbonEx.AXUIElementSetAttributeValue(window, kAXPosition, axvalue);
   }

   private static CoreFoundation.CFStringRef createCFString(String s) {
      return CoreFoundation.CFStringRef.createCFString(s);
   }

   private static int getScreenWidth() {
      return screenWidth;
   }

   private static int getScreenHeight() {
      return screenHeight;
   }

   private static double betweenOrLimit(double a, double min, double max) {
      return Math.min(Math.max(a, min), max);
   }

   private static Rectangle getWindowVisibleArea() {
      int menuBarHeight = 22;
      int x = 1;
      int y = 22;
      int width = getScreenWidth() - 2;
      int height = getScreenHeight() - 22;
      refreshDockState();
      String orientation = getDockOrientation();
      int tilesize = getDockTileSize();
      if ("bottom".equals(orientation)) {
         height -= tilesize;
      } else if ("right".equals(orientation)) {
         width -= tilesize;
      } else if ("left".equals(orientation)) {
         x += tilesize;
         width -= tilesize;
      } else {
         x += tilesize;
         width -= 2 * tilesize;
      }

      return new Rectangle(x, y, width, height);
   }

   private static String getDockOrientation() {
      CoreFoundation.CFTypeRef orientationRef = carbonEx.CFPreferencesCopyValue(kOrientation, kDock, CarbonExtra.kCurrentUser, CarbonExtra.kAnyHost);
      if (orientationRef == null) {
         return "null";
      } else {
         CoreFoundation.CFStringRef orientationStringRef = new CoreFoundation.CFStringRef(orientationRef.getPointer());
         int bufsize = 64;
         Memory buf = new Memory(64L);
         CoreFoundation.INSTANCE.CFStringGetCString(orientationStringRef, buf, new CoreFoundation.CFIndex(64L), carbonEx.CFStringGetSystemEncoding());
         orientationStringRef.release();
         String ret = buf.getString(0L);
         buf.clear();
         return ret;
      }
   }

   private static int getDockTileSize() {
      return 100;
   }

   private static void refreshDockState() {
      carbonEx.CFPreferencesAppSynchronize(kDock);
   }

   private static long getCurrentPID() {
      return currentPID;
   }

   private static void setCurrentPID(long newPID) {
      if (newPID != myPID) {
         currentPID = newPID;
         getTouchedProcesses().add(newPID);
      }

   }

   private static Set<Long> getTouchedProcesses() {
      return touchedProcesses;
   }

   private void updateFrontmostWindow() {
      Rectangle frontmostWindowRect = getFrontmostAppRect();
      Rectangle windowVisibleArea = getWindowVisibleArea();
      frontmostWindow.setVisible(frontmostWindowRect != null && frontmostWindowRect.intersects(windowVisibleArea) && !frontmostWindowRect.contains(windowVisibleArea));
      frontmostWindow.set(frontmostWindowRect == null ? new Rectangle(-1, -1, 0, 0) : frontmostWindowRect);
   }

   private static void updateFrontmostApp() {
      long newPID = getFrontmostAppsPID();
      setCurrentPID(newPID);
   }

   public void tick() {
      super.tick();
      updateFrontmostApp();
      this.updateFrontmostWindow();
   }

   public void moveActiveIE(Point point) {
      Rectangle visibleRect = getWindowVisibleArea();
      Rectangle windowRect = getFrontmostAppRect();
      double minX = visibleRect.getMinX() - windowRect.getWidth();
      double maxX = visibleRect.getMaxX();
      double minY = visibleRect.getMinY();
      double maxY = visibleRect.getMaxY();
      double pX = point.getX();
      double pY = point.getY();
      pX = betweenOrLimit(pX, minX, maxX);
      pY = betweenOrLimit(pY, minY, maxY);
      point.setLocation(pX, pY);
      moveFrontmostWindow(point);
   }

   public void restoreIE() {
      Rectangle visibleRect = getWindowVisibleArea();
      restoreWindowsNotIn(visibleRect);
      getTouchedProcesses().clear();
   }

   public Area getWorkArea() {
      return this.getScreen();
   }

   public Area getActiveIE() {
      return activeIE;
   }

   public String getActiveIETitle() {
      return null;
   }

   public long getActiveWindowId() {
      return currentPID;
   }

   public void refreshCache() {
   }

   public void dispose() {
   }

   private static void optimizeForMacOSVersion() {
      try {
         String osVersion = System.getProperty("os.version");
         String[] versionParts = osVersion.split("\\.");
         int majorVersion = Integer.parseInt(versionParts[0]);
         log.info("Detected macOS version: " + osVersion + " (major: " + majorVersion + ")");
         if (majorVersion >= 20) {
            System.setProperty("apple.awt.application.useCustomCursor", "true");
            log.info("Enabled Big Sur+ optimizations");
         }

         if (majorVersion >= 21) {
            System.setProperty("sun.java2d.metal", "true");
            log.info("Enabled Monterey+ Metal rendering");
         }

         if (majorVersion >= 22) {
            System.setProperty("apple.awt.enableTemplateImages", "true");
            log.info("Enabled Ventura+ template images");
         }
      } catch (Exception e) {
         log.warning("Failed to detect macOS version: " + e.getMessage());
      }

   }

   static {
      optimizeForMacOSVersion();
      activeIE = new Area();
      frontmostWindow = activeIE;
      screenWidth = (int)Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
      screenHeight = (int)Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
      carbonEx = CarbonExtra.INSTANCE;
      myPID = Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
      currentPID = myPID;
      touchedProcesses = new HashSet<>();
      kAXPosition = createCFString("AXPosition");
      kAXSize = createCFString("AXSize");
      kAXFocusedWindow = createCFString("AXFocusedWindow");
      kDock = createCFString("com.apple.Dock");
      kTileSize = createCFString("tilesize");
      kOrientation = createCFString("orientation");
      kAXChildren = createCFString("AXChildren");
   }
}
