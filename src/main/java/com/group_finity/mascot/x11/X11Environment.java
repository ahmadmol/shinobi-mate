package com.group_finity.mascot.x11;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.environment.FloorCeiling;
import com.group_finity.mascot.environment.Wall;
import com.group_finity.mascot.x11.jna.X11Extra;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

class X11Environment extends Environment {
   private X.Display display = new X.Display();
   public WindowContainer ieContainer = new WindowContainer();
   private HashMap ieCache = new LinkedHashMap();
   public Area activeIe = new Area();
   private X.Window activeIeObject = null;
   public static final Area workArea = new Area();
   private boolean checkTitles = true;
   private boolean updateOnNext = false;
   private int q = 400;
   private int xOffset;
   private int yOffset;
   private int wMod;
   private int hMod = 0;
   private String[] windowTitles = null;
   private String[] windowTitlesBlacklist = null;
   private static final Random RNG = new Random();
   private Collection curActiveWin = new ArrayList();
   private List curVisibleWin = new ArrayList();
   private Collection badStateList = new ArrayList();
   private Collection badTypeList = new ArrayList();
   private int maximizedVertValue;
   private int maximizedHorzValue;
   private int minimizedValue;
   private int fullscreenValue;
   private int dockValue;

   X11Environment() {
      workArea.set(this.getWorkAreaRect());
      this.maximizedVertValue = this.display.getAtom("_NET_WM_STATE_MAXIMIZED_VERT").intValue();
      this.maximizedHorzValue = this.display.getAtom("_NET_WM_STATE_MAXIMIZED_HORZ").intValue();
      this.minimizedValue = this.display.getAtom("_NET_WM_STATE_HIDDEN").intValue();
      this.fullscreenValue = this.display.getAtom("_NET_WM_STATE_FULLSCREEN").intValue();
      this.badStateList.add(this.minimizedValue);
      this.badStateList.add(this.display.getAtom("_NET_WM_STATE_MODAL").intValue());
      this.badStateList.add(this.display.getAtom("_NET_WM_STATE_ABOVE").intValue());
      this.dockValue = this.display.getAtom("_NET_WM_WINDOW_TYPE_DOCK").intValue();
      this.badTypeList.add(this.dockValue);
      this.badTypeList.add(this.display.getAtom("_NET_WM_WINDOW_TYPE_DESKTOP").intValue());
      this.badTypeList.add(this.display.getAtom("_NET_WM_WINDOW_TYPE_MENU").intValue());
      this.badTypeList.add(this.display.getAtom("_NET_WM_WINDOW_TYPE_SPLASH").intValue());
      this.badTypeList.add(this.display.getAtom("_NET_WM_WINDOW_TYPE_DIALOG").intValue());
   }

   public void tick() {
      super.tick();
      Rectangle ieRect = getWindowBounds(this.findActiveIE());
      this.activeIe.setVisible(ieRect.intersects(this.getScreen().toRectangle()));
      this.activeIe.set(ieRect);
   }

   private void update() {
      if (this.curVisibleWin != null) {
         this.curVisibleWin.clear();
      }

      this.curActiveWin.clear();
      if (this.display != null) {
         this.updateOnNext = false;

         try {
            X.Window[] allWindows = this.display.getRootWindow().getAllSubwindows();

            int curDesktop;
            try {
               curDesktop = this.display.getActiveDesktopNumber();
            } catch (X.X11Exception var20) {
               curDesktop = 0;
            }

            for(X.Window window : allWindows) {
               if (this.isIE(window) || !this.checkTitles) {
                  int id = window.getID();
                  Rectangle bounds = window.getBounds();
                  int x = bounds.x + this.xOffset;
                  int y = bounds.y + this.yOffset;
                  int w = bounds.width + this.wMod;
                  int h = bounds.height + this.hMod;
                  if (this.ieContainer.containsKey(id)) {
                     Area a = (Area)this.ieContainer.get(id);

                     int desktop;
                     try {
                        desktop = window.getDesktop();
                     } catch (X.X11Exception var19) {
                        desktop = 0;
                     }

                     boolean badDesktop = desktop != curDesktop && desktop != -1;
                     boolean badState = this.checkState(Arrays.asList(window.getState()));
                     if (this.checkTitles) {
                        if (!badDesktop && !badState) {
                           ((Area)this.ieContainer.get(id)).setVisible(true);
                           this.curVisibleWin.add(id);
                        } else {
                           ((Area)this.ieContainer.get(id)).setVisible(false);
                        }
                     } else {
                        boolean badType = this.checkType(Arrays.asList(window.getType()));
                        if (!badDesktop && !badType && !badState) {
                           ((Area)this.ieContainer.get(id)).setVisible(true);
                           this.curVisibleWin.add(id);
                        } else {
                           ((Area)this.ieContainer.get(id)).setVisible(false);
                        }
                     }

                     Rectangle r = a.toRectangle();
                     Rectangle newRect = new Rectangle(x, y, w, h);
                     if (!r.equals(newRect)) {
                        this.updateOnNext = true;
                     }

                     a.set(newRect);
                     this.curActiveWin.add(id);
                  } else {
                     Rectangle r = new Rectangle(x, y, w, h);
                     Area a = new Area();
                     a.set(r);
                     a.setVisible(false);
                     this.ieContainer.put(id, a);
                     this.curActiveWin.add(id);
                  }
               }
            }
         } catch (X.X11Exception var21) {
         }

         for(Map.Entry<Number, Area> entry : this.ieContainer.entrySet()) {
            Number i = (Number)entry.getKey();
            if (!this.curActiveWin.contains(i)) {
               ((Area)entry.getValue()).setVisible(false);
               this.ieContainer.remove(i);
               break;
            }
         }

      }
   }

   private boolean isIE(X.Window window) {
      Boolean cachedValue = (Boolean)this.ieCache.get(window);
      if (cachedValue != null) {
         return cachedValue;
      } else {
         String ieTitle = getWindowTitle(window);
         if (ieTitle.isEmpty()) {
            this.ieCache.put(window, false);
            return false;
         } else {
            boolean blacklistInUse = false;
            if (this.windowTitlesBlacklist == null) {
               this.windowTitlesBlacklist = Main.getInstance().getProperties().getProperty("InteractiveWindowsBlacklist", "").split("/");
            }

            for(String windowTitle : this.windowTitlesBlacklist) {
               if (!windowTitle.trim().isEmpty()) {
                  blacklistInUse = true;
                  if (ieTitle.contains(windowTitle)) {
                     this.ieCache.put(window, false);
                     return false;
                  }
               }
            }

            boolean whitelistInUse = false;
            if (this.windowTitles == null) {
               this.windowTitles = Main.getInstance().getProperties().getProperty("InteractiveWindows", "").split("/");
            }

            for(String windowTitle : this.windowTitles) {
               if (!windowTitle.trim().isEmpty()) {
                  whitelistInUse = true;
                  if (ieTitle.contains(windowTitle)) {
                     this.ieCache.put(window, true);
                     return true;
                  }
               }
            }

            if (!whitelistInUse && blacklistInUse) {
               this.ieCache.put(window, true);
               return true;
            } else {
               this.ieCache.put(window, false);
               return false;
            }
         }
      }
   }

   private IeStatus getIeStatus(X.Window window) {
      int curDesktop;
      int desktop;
      List<Integer> state;
      List<Integer> type;
      try {
         curDesktop = this.display.getActiveDesktopNumber();
         desktop = window.getDesktop();
         state = Arrays.asList(window.getState());
         type = Arrays.asList(window.getType());
      } catch (X.X11Exception var8) {
         return X11Environment.IeStatus.IGNORED;
      }

      boolean badDesktop = desktop != curDesktop && desktop != -1;
      if (!badDesktop && !this.checkState(state) && !this.checkType(type)) {
         if (state.contains(this.maximizedVertValue) && state.contains(this.maximizedHorzValue)) {
            return X11Environment.IeStatus.INVALID;
         }

         if (this.isIE(window) && !state.contains(this.minimizedValue)) {
            Rectangle ieRect = getWindowBounds(window);
            if (ieRect.intersects(getScreenRect())) {
               return X11Environment.IeStatus.VALID;
            }

            return X11Environment.IeStatus.OUT_OF_BOUNDS;
         }
      }

      return X11Environment.IeStatus.IGNORED;
   }

   private X.Window findActiveIE() {
      this.activeIeObject = null;

      X.Window[] allWindows;
      try {
         allWindows = new X.Window[]{this.display.getActiveWindow()};
      } catch (X.X11Exception var6) {
         return null;
      }

      for(X.Window window : allWindows) {
         switch (this.getIeStatus(window).ordinal()) {
            case 0:
               this.activeIeObject = window;
               return this.activeIeObject;
            case 1:
            default:
               this.activeIeObject = null;
               return this.activeIeObject;
            case 2:
            case 3:
         }
      }

      return this.activeIeObject;
   }

   private static Rectangle getWindowBounds(X.Window window) {
      return window == null ? new Rectangle() : window.getBounds();
   }

   private static String getWindowTitle(X.Window window) {
      if (window == null) {
         return "";
      } else {
         String title;
         try {
            title = window.getID() == 0 ? "" : window.getTitle();
         } catch (X.X11Exception var3) {
            title = "";
         }

         return title;
      }
   }

   private boolean checkState(Collection state) {
      if (state != null && !state.isEmpty()) {
         return this.checkTitles ? state.contains(this.minimizedValue) : state.stream().anyMatch((value) -> this.badStateList.contains(value));
      } else {
         return true;
      }
   }

   private boolean checkType(Collection type) {
      if (type != null && !type.isEmpty()) {
         return this.checkTitles ? false : type.stream().anyMatch((value) -> this.badTypeList.contains(value));
      } else {
         return true;
      }
   }

   private Rectangle getWorkAreaRect() {
      return this.getScreen().toRectangle();
   }

   private void getRandomIE() {
      if (this.curVisibleWin != null) {
         ArrayList<Area> visibleWin = (ArrayList)this.curVisibleWin.stream().filter(Objects::nonNull).map((n) -> (Area)this.ieContainer.get(n)).collect(Collectors.toCollection(ArrayList::new));
         if (!visibleWin.isEmpty()) {
            this.activeIe = (Area)visibleWin.get(RNG.nextInt(visibleWin.size()));
         }
      }
   }

   public int getDockValue() {
      return this.dockValue;
   }

   public List getVisible() {
      return this.curVisibleWin;
   }

   public Area getActiveIE() {
      return this.activeIe;
   }

   public String getActiveIETitle() {
      return getWindowTitle(this.activeIeObject);
   }

   public long getActiveWindowId() {
      return this.activeIeObject == null ? 0L : (long)this.activeIeObject.getID();
   }

   public void moveActiveIE(Point point) {
      if (this.activeIeObject != null) {
         X11Extra.INSTANCE.XMoveWindow(this.display.getX11Display(), this.activeIeObject.getX11Window(), point.x, point.y);
      }

   }

   public void restoreIE() {
   }

   public void refreshCache() {
      this.ieContainer.clear();
      this.ieCache.clear();
      this.windowTitles = null;
      this.windowTitlesBlacklist = null;
      this.curActiveWin.clear();
      this.curVisibleWin.clear();
   }

   public void dispose() {
   }

   public Area getWorkArea() {
      return workArea;
   }

   private static enum IeStatus {
      VALID,
      INVALID,
      IGNORED,
      OUT_OF_BOUNDS;

      // $FF: synthetic method
      private static IeStatus[] $values() {
         return new IeStatus[]{VALID, INVALID, IGNORED, OUT_OF_BOUNDS};
      }
   }

   public static class WindowContainer extends Hashtable<Number, Area> {
      public boolean onLeft(Point p) {
         return this.values().stream().map((area) -> new Wall(area, false)).anyMatch((w) -> w.isOn(p));
      }

      public boolean onRight(Point p) {
         return this.values().stream().map((area) -> new Wall(area, true)).anyMatch((w) -> w.isOn(p));
      }

      public boolean onTop(Point p) {
         return this.values().stream().map((area) -> new FloorCeiling(area, false)).anyMatch((w) -> w.isOn(p));
      }

      public boolean onBottom(Point p) {
         return this.values().stream().map((area) -> new FloorCeiling(area, true)).anyMatch((w) -> w.isOn(p));
      }

      public Wall getLeft(Point p) {
         return this.values().stream().map((area) -> new Wall(area, false)).filter((w) -> w.isOn(p)).findFirst().orElse(null);
      }

      public Wall getRight(Point p) {
         return this.values().stream().map((area) -> new Wall(area, true)).filter((w) -> w.isOn(p)).findFirst().orElse(null);
      }

      public FloorCeiling getTop(Point p) {
         return this.values().stream().map((area) -> new FloorCeiling(area, false)).filter((w) -> w.isOn(p)).findFirst().orElse(null);
      }

      public FloorCeiling getBottom(Point p) {
         return this.values().stream().map((area) -> new FloorCeiling(area, true)).filter((w) -> w.isOn(p)).findFirst().orElse(null);
      }
   }
}
