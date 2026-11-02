package com.group_finity.mascot.win;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.win.jna.Dwmapi;
import com.group_finity.mascot.win.jna.Gdi32;
import com.group_finity.mascot.win.jna.RECT;
import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedHashMap;

class WindowsEnvironment extends Environment {
   private static final HashMap ieCache = new LinkedHashMap();
   public static final Area workArea = new Area();
   public static final Area activeIE = new Area();
   private static Pointer activeIEobject = null;
   private static String[] windowTitles = null;
   private static String[] windowTitlesBlacklist = null;

   private static boolean isIE(Pointer ie) {
      // Never use our own companion/UI windows as climbable obstacles.
      var process=new com.sun.jna.ptr.IntByReference();
      User32.INSTANCE.GetWindowThreadProcessId(ie,process);
      if(Integer.toUnsignedLong(process.getValue())==ProcessHandle.current().pid())return false;
      Boolean cachedValue = (Boolean)ieCache.get(ie);
      if (cachedValue != null) {
         return cachedValue;
      } else {
         char[] title = new char[1024];
         int titleLength = User32.INSTANCE.GetWindowTextW(ie, title, 1024);
         String ieTitle = new String(title, 0, titleLength);
         if (!ieTitle.isEmpty() && !ieTitle.equals("Program Manager")) {
            boolean blacklistInUse = false;
            if (windowTitlesBlacklist == null) {
               windowTitlesBlacklist = Main.getInstance().getProperties().getProperty("InteractiveWindowsBlacklist", "").split("/");
            }

            for(String windowTitle : windowTitlesBlacklist) {
               if (!windowTitle.trim().isEmpty()) {
                  blacklistInUse = true;
                  if (ieTitle.contains(windowTitle)) {
                     ieCache.put(ie, false);
                     return false;
                  }
               }
            }

            boolean whitelistInUse = false;
            if (windowTitles == null) {
               windowTitles = Main.getInstance().getProperties().getProperty("InteractiveWindows", "").split("/");
            }

            for(String windowTitle : windowTitles) {
               if (!windowTitle.trim().isEmpty()) {
                  whitelistInUse = true;
                  if (ieTitle.contains(windowTitle)) {
                     ieCache.put(ie, true);
                     return true;
                  }
               }
            }

            if (!whitelistInUse) {
               ieCache.put(ie, true);
               return true;
            } else {
               ieCache.put(ie, false);
               return false;
            }
         } else {
            ieCache.put(ie, false);
            return false;
         }
      }
   }

   private static IEResult isViableIE(Pointer ie) {
      if (User32.INSTANCE.IsWindowVisible(ie) != 0) {
         LongByReference flagsRef = new LongByReference();
         NativeLong result = Dwmapi.INSTANCE.DwmGetWindowAttribute(ie, 14, flagsRef, 8);
         if (result.longValue() != -2147024809L && (result.longValue() != 0L || flagsRef.getValue() != 0L)) {
            return WindowsEnvironment.IEResult.NOT_IE;
         }

         if (User32.INSTANCE.IsZoomed(ie) != 0) {
            return WindowsEnvironment.IEResult.INVALID;
         }

         if (isIE(ie) && User32.INSTANCE.IsIconic(ie) == 0) {
            Rectangle ieRect = getIERect(ie);
            if (ieRect.intersects(getScreenRect())) {
               return WindowsEnvironment.IEResult.IE;
            }

            return WindowsEnvironment.IEResult.IE_OUT_OF_BOUNDS;
         }
      }

      return WindowsEnvironment.IEResult.NOT_IE;
   }

   private static Pointer findActiveIE() {
      activeIEobject = null;
      User32.INSTANCE.EnumWindows((ie, data) -> {
         boolean var10000;
         switch (isViableIE(ie).ordinal()) {
            case 1:
            case 2:
               var10000 = true;
               break;
            case 3:
               activeIEobject = ie;
               var10000 = false;
               break;
            default:
               activeIEobject = null;
               var10000 = false;
         }

         return var10000;
      }, (Pointer)null);
      return activeIEobject;
   }

   private static Rectangle getIERect(Pointer ie) {
      if(ie==null)return new Rectangle();
      RECT out = new RECT();
      User32.INSTANCE.GetWindowRect(ie, out);
      RECT in = new RECT();
      if (getWindowRgnBox(ie, in) == 0) {
         in.left = 0;
         in.top = 0;
         in.right = out.right - out.left;
         in.bottom = out.bottom - out.top;
      }

      Rectangle physical=new Rectangle(out.left + in.left, out.top + in.top, in.Width(), in.Height());
      var api=com.sun.jna.platform.win32.User32.INSTANCE;
      var info=new com.sun.jna.platform.win32.WinUser.MONITORINFO();
      if(!api.GetMonitorInfo(api.MonitorFromWindow(new com.sun.jna.platform.win32.WinDef.HWND(ie),2),info).booleanValue())return physical;
      for(var device:java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()){
         var gc=device.getDefaultConfiguration();Rectangle bounds=gc.getBounds();
         // Windows AWT keeps monitor origins in desktop coordinates and scales local offsets.
         if(bounds.x==info.rcMonitor.left&&bounds.y==info.rcMonitor.top){
            double sx=(double)(info.rcMonitor.right-info.rcMonitor.left)/bounds.width,sy=(double)(info.rcMonitor.bottom-info.rcMonitor.top)/bounds.height;
            return new Rectangle(bounds.x+(int)Math.round((physical.x-info.rcMonitor.left)/sx),bounds.y+(int)Math.round((physical.y-info.rcMonitor.top)/sy),
               (int)Math.round(physical.width/sx),(int)Math.round(physical.height/sy));
         }
      }
      return physical;
   }

   private static int getWindowRgnBox(Pointer window, RECT rect) {
      Pointer hRgn = Gdi32.INSTANCE.CreateRectRgn(0, 0, 0, 0);

      byte var3;
      try {
         if (User32.INSTANCE.GetWindowRgn(window, hRgn) != 0) {
            Gdi32.INSTANCE.GetRgnBox(hRgn, rect);
            var3 = 1;
            return var3;
         }

         var3 = 0;
      } finally {
         Gdi32.INSTANCE.DeleteObject(hRgn);
      }

      return var3;
   }

   private static void moveIE(Pointer ie, Rectangle rect) {
      if (ie != null) {
         rect=new Rectangle(rect);
         for(var device:java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()){
            var gc=device.getDefaultConfiguration();Rectangle bounds=gc.getBounds();
            if(bounds.contains(rect.x+rect.width/2,rect.y+rect.height/2)){
               var api=com.sun.jna.platform.win32.User32.INSTANCE;var info=new com.sun.jna.platform.win32.WinUser.MONITORINFO();
               var p=new com.sun.jna.platform.win32.WinDef.POINT.ByValue();p.x=bounds.x+1;p.y=bounds.y+1;
               if(!api.GetMonitorInfo(api.MonitorFromPoint(p,2),info).booleanValue())break;
               double sx=(double)(info.rcMonitor.right-info.rcMonitor.left)/bounds.width,sy=(double)(info.rcMonitor.bottom-info.rcMonitor.top)/bounds.height;
               rect=new Rectangle(bounds.x+(int)Math.round((rect.x-bounds.x)*sx),bounds.y+(int)Math.round((rect.y-bounds.y)*sy),(int)Math.round(rect.width*sx),(int)Math.round(rect.height*sy));break;
            }
         }
         RECT out = new RECT();
         User32.INSTANCE.GetWindowRect(ie, out);
         RECT in = new RECT();
         if (getWindowRgnBox(ie, in) == 0) {
            in.left = 0;
            in.top = 0;
            in.right = out.right - out.left;
            in.bottom = out.bottom - out.top;
         }

         User32.INSTANCE.MoveWindow(ie, rect.x - in.left, rect.y - in.top, rect.width + out.Width() - in.Width(), rect.height + out.Height() - in.Height(), 1);
      }
   }

   private static void restoreAllIEs() {
      User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
         int offset = 25;

         public boolean callback(Pointer ie, Pointer data) {
            IEResult result = WindowsEnvironment.isViableIE(ie);
            if (result == WindowsEnvironment.IEResult.IE_OUT_OF_BOUNDS) {
               RECT workArea = new RECT();
               User32.INSTANCE.SystemParametersInfoW(48, 0, workArea, 0);
               RECT rect = new RECT();
               User32.INSTANCE.GetWindowRect(ie, rect);
               rect.OffsetRect(workArea.left + this.offset - rect.left, workArea.top + this.offset - rect.top);
               User32.INSTANCE.MoveWindow(ie, rect.left, rect.top, rect.Width(), rect.Height(), 1);
               User32.INSTANCE.BringWindowToTop(ie);
               this.offset += 25;
            }

            return true;
         }
      }, (Pointer)null);
   }

   public void tick() {
      com.group_finity.mascot.environment.DesktopWorkArea.refresh();
      super.tick();
      workArea.set(getWorkAreaRect());
      Rectangle ieRect = getIERect(findActiveIE());
      activeIE.setVisible(ieRect.intersects(this.getScreen().toRectangle()));
      activeIE.set(ieRect);
   }

   public void dispose() {
   }

   public void moveActiveIE(Point point) {
      moveIE(findActiveIE(), new Rectangle(point.x, point.y, activeIE.getWidth(), activeIE.getHeight()));
   }

   public void restoreIE() {
      restoreAllIEs();
   }

   public Area getWorkArea() {
      return workArea;
   }

   public Area getActiveIE() {
      return activeIE;
   }

   public String getActiveIETitle() {
      Pointer ie = findActiveIE();
      char[] title = new char[1024];
      int titleLength = User32.INSTANCE.GetWindowTextW(ie, title, 1024);
      return new String(title, 0, titleLength);
   }

   private static Rectangle getWorkAreaRect() {
      RECT rect = new RECT();
      User32.INSTANCE.SystemParametersInfoW(48, 0, rect, 0);
      return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
   }

   public void refreshCache() {
      ieCache.clear();
      windowTitles = null;
      windowTitlesBlacklist = null;
   }

   public long getActiveWindowId() {
      return activeIEobject != null ? Pointer.nativeValue(activeIEobject) : 0L;
   }

   private static enum IEResult {
      INVALID,
      NOT_IE,
      IE_OUT_OF_BOUNDS,
      IE;

      // $FF: synthetic method
      private static IEResult[] $values() {
         return new IEResult[]{INVALID, NOT_IE, IE_OUT_OF_BOUNDS, IE};
      }
   }
}
