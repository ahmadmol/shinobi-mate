package com.group_finity.mascot.x11;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class X {
   public static final int _NET_WM_STATE_REMOVE = 0;
   public static final int _NET_WM_STATE_ADD = 1;
   public static final int _NET_WM_STATE_TOGGLE = 2;
   public static final int MAX_PROPERTY_VALUE_LEN = 4096;
   private static final X11 x11;

   private static int bytesToInt(byte[] prop) {
      return (prop[3] & 255) << 24 | (prop[2] & 255) << 16 | (prop[1] & 255) << 8 | prop[0] & 255;
   }

   private static int bytesToInt(byte[] prop, int offset) {
      return (prop[3 + offset] & 255) << 24 | (prop[2 + offset] & 255) << 16 | (prop[1 + offset] & 255) << 8 | prop[offset] & 255;
   }

   private static int bytesToInt(byte b1, byte b2, byte b3, byte b4) {
      return (b4 & 255) << 24 | (b3 & 255) << 16 | (b2 & 255) << 8 | b1 & 255;
   }

   private static int bytesToInt(byte b1, byte b2, byte b3, byte b4, int offset) {
      return (b4 + offset & 255) << 24 | (b3 + offset & 255) << 16 | (b2 + offset & 255) << 8 | b1 + offset & 255;
   }

   static {
      x11 = X11.INSTANCE;
   }

   public static class Display {
      private final X11.Display x11Display;
      private final HashMap atomsHash = new HashMap();

      public Display() {
         this.x11Display = X.x11.XOpenDisplay((String)null);
         if (this.x11Display == null) {
            throw new Error("Can't open X Display");
         }
      }

      public Display(X11.Display x11Display) {
         this.x11Display = x11Display;
         if (x11Display == null) {
            throw new Error("X Display is null");
         }
      }

      public void close() {
         X.x11.XCloseDisplay(this.x11Display);
      }

      public void flush() {
         X.x11.XFlush(this.x11Display);
      }

      public X11.Display getX11Display() {
         return this.x11Display;
      }

      public X11.Atom getAtom(String name) {
         X11.Atom atom = (X11.Atom)this.atomsHash.get(name);
         if (atom == null) {
            atom = X.x11.XInternAtom(this.x11Display, name, false);
            this.atomsHash.put(name, atom);
         }

         return atom;
      }

      public Window getWindowManagerInfo() throws X11Exception {
         Window rootWindow = this.getRootWindow();

         try {
            return rootWindow.getWindowProperty(X11.XA_WINDOW, "_NET_SUPPORTING_WM_CHECK");
         } catch (X11Exception var5) {
            try {
               return rootWindow.getWindowProperty(X11.XA_CARDINAL, "_WIN_SUPPORTING_WM_CHECK");
            } catch (X11Exception e1) {
               throw new X11Exception("Cannot get window manager info properties. (_NET_SUPPORTING_WM_CHECK or _WIN_SUPPORTING_WM_CHECK)", e1);
            }
         }
      }

      public Window getRootWindow() {
         return new Window(this, X.x11.XDefaultRootWindow(this.x11Display));
      }

      public Window getActiveWindow() throws X11Exception {
         return this.getRootWindow().getWindowProperty(X11.XA_WINDOW, "_NET_ACTIVE_WINDOW");
      }

      public Window[] getWindows() throws X11Exception {
         Window rootWindow = this.getRootWindow();

         byte[] bytes;
         try {
            bytes = rootWindow.getProperty(X11.XA_WINDOW, "_NET_CLIENT_LIST");
         } catch (X11Exception var6) {
            try {
               bytes = rootWindow.getProperty(X11.XA_CARDINAL, "_WIN_CLIENT_LIST");
            } catch (X11Exception e1) {
               throw new X11Exception("Cannot get client list properties (_NET_CLIENT_LIST or _WIN_CLIENT_LIST)", e1);
            }
         }

         Window[] windowList = new Window[bytes.length / X11.Window.SIZE];

         for(int i = 0; i < windowList.length; ++i) {
            windowList[i] = new Window(this, new X11.Window((long)X.bytesToInt(bytes, X11.XID.SIZE * i)));
         }

         return windowList;
      }

      public int getDesktopCount() throws X11Exception {
         Window root = this.getRootWindow();

         try {
            return root.getIntProperty(X11.XA_CARDINAL, "_NET_NUMBER_OF_DESKTOPS");
         } catch (X11Exception var5) {
            try {
               return root.getIntProperty(X11.XA_CARDINAL, "_WIN_WORKSPACE_COUNT");
            } catch (X11Exception e1) {
               throw new X11Exception("Cannot get number of desktops properties (_NET_NUMBER_OF_DESKTOPS or _WIN_WORKSPACE_COUNT)", e1);
            }
         }
      }

      public int getActiveDesktopNumber() throws X11Exception {
         Window root = this.getRootWindow();

         int curDesktop;
         try {
            curDesktop = root.getIntProperty(X11.XA_CARDINAL, "_NET_CURRENT_DESKTOP");
         } catch (X11Exception var6) {
            try {
               curDesktop = root.getIntProperty(X11.XA_CARDINAL, "_WIN_WORKSPACE");
            } catch (X11Exception e1) {
               throw new X11Exception("Cannot get current desktop properties (_NET_CURRENT_DESKTOP or _WIN_WORKSPACE property)", e1);
            }
         }

         return curDesktop;
      }

      public Desktop[] getDesktops() throws X11Exception {
         Window root = this.getRootWindow();

         String[] desktopNames;
         try {
            desktopNames = root.getUtf8ListProperty(this.getAtom("UTF8_STRING"), "_NET_DESKTOP_NAMES");
         } catch (X11Exception var6) {
            try {
               desktopNames = root.getStringListProperty(X11.XA_STRING, "_WIN_WORKSPACE_NAMES");
            } catch (X11Exception e1) {
               throw new X11Exception("Cannot get desktop names properties (_NET_DESKTOP_NAMES or _WIN_WORKSPACE_NAMES)", e1);
            }
         }

         Desktop[] desktops = new Desktop[this.getDesktopCount()];

         for(int i = 0; i < desktops.length; ++i) {
            desktops[i] = new Desktop(this, i, desktopNames[i]);
         }

         return desktops;
      }

      public void switchDesktop(int nr) throws X11Exception {
         this.getRootWindow().clientMsg("_NET_CURRENT_DESKTOP", nr, 0, 0, 0, 0);
      }

      public void showingDesktop(boolean state) throws X11Exception {
         this.getRootWindow().clientMsg("_NET_SHOWING_DESKTOP", state ? 1 : 0, 0, 0, 0, 0);
      }

      public void setKeyAutoRepeat(boolean on) {
         if (on) {
            X.x11.XAutoRepeatOn(this.x11Display);
         } else {
            X.x11.XAutoRepeatOff(this.x11Display);
         }

      }

      public X11.KeySym getKeySym(String keyName) {
         return X.x11.XStringToKeysym(keyName);
      }

      public X11.KeySym getKeySym(byte keyCode, int index) {
         return X.x11.XKeycodeToKeysym(this.x11Display, keyCode, index);
      }

      public byte getKeyCode(X11.KeySym keySym) {
         return X.x11.XKeysymToKeycode(this.x11Display, keySym);
      }

      public byte getKeyCode(String keyName) {
         return X.x11.XKeysymToKeycode(this.x11Display, this.getKeySym(keyName));
      }

      public String getKeyName(X11.KeySym keySym) {
         return X.x11.XKeysymToString(keySym);
      }

      public String getKeyName(byte keyCode, int index) {
         return this.getKeyName(this.getKeySym(keyCode, index));
      }

      public ModifierKeymap getModifierKeymap() {
         X11.XModifierKeymapRef xModifierKeymapRef = X.x11.XGetModifierMapping(this.x11Display);
         ModifierKeymap modifierKeymap = new ModifierKeymap(xModifierKeymapRef);
         X.x11.XFreeModifiermap(xModifierKeymapRef);
         return modifierKeymap;
      }

      public void setModifierKeymap(ModifierKeymap modifierKeymap) {
         X11.XModifierKeymapRef xModifierKeymapRef = modifierKeymap.toXModifierKeymap();
         X.x11.XSetModifierMapping(this.x11Display, xModifierKeymapRef);
      }
   }

   public static class ModifierKeymap {
      public ArrayList shift = new ArrayList(4);
      public ArrayList lock = new ArrayList(4);
      public ArrayList control = new ArrayList(4);
      public ArrayList mod1 = new ArrayList(4);
      public ArrayList mod2 = new ArrayList(4);
      public ArrayList mod3 = new ArrayList(4);
      public ArrayList mod4 = new ArrayList(4);
      public ArrayList mod5 = new ArrayList(4);

      public ModifierKeymap() {
      }

      public ModifierKeymap(X11.XModifierKeymapRef xModifierKeymapRef) {
         this.fromXModifierKeymap(xModifierKeymapRef);
      }

      public void fromXModifierKeymap(X11.XModifierKeymapRef xModifierKeymapRef) {
         int count = xModifierKeymapRef.max_keypermod;
         byte[] keys = xModifierKeymapRef.modifiermap.getByteArray(0L, 8 * count);
         ArrayList<Byte>[] allModifiers = this.getAllModifiers();

         for(int modNr = 0; modNr < 8; ++modNr) {
            List<Byte> modifier = allModifiers[modNr];
            modifier.clear();

            for(int keyNr = 0; keyNr < count; ++keyNr) {
               byte key = keys[modNr * count + keyNr];
               if (key != 0) {
                  modifier.add(key);
               }
            }
         }

      }

      public X11.XModifierKeymapRef toXModifierKeymap() {
         ArrayList<Byte>[] allModifiers = this.getAllModifiers();
         int count = 0;

         for(List allModifier : allModifiers) {
            count = Math.max(count, allModifier.size());
         }

         byte[] keys = new byte[8 * count];

         for(int modNr = 0; modNr < 8; ++modNr) {
            List<Byte> modifier = allModifiers[modNr];

            for(int keyNr = 0; keyNr < modifier.size(); ++keyNr) {
               keys[modNr * count + keyNr] = (Byte)modifier.get(keyNr);
            }
         }

         X11.XModifierKeymapRef xModifierKeymapRef = new X11.XModifierKeymapRef();
         xModifierKeymapRef.max_keypermod = count;
         xModifierKeymapRef.modifiermap = new Memory((long)keys.length);
         xModifierKeymapRef.modifiermap.write(0L, (byte[])keys, 0, keys.length);
         return xModifierKeymapRef;
      }

      public ArrayList[] getAllModifiers() {
         return new ArrayList[]{this.shift, this.lock, this.control, this.mod1, this.mod2, this.mod3, this.mod4, this.mod5};
      }
   }

   public static class Desktop {
      public Display display;
      public int number;
      public String name;

      public Desktop(Display display, int number, String name) {
         this.display = display;
         this.number = number;
         this.name = name;
      }
   }

   public static class Window {
      private final Display display;
      private final X11.Window x11Window;

      public X11.Window getX11Window() {
         return this.x11Window;
      }

      public int getID() {
         return this.x11Window.intValue();
      }

      public Window(Display display, X11.Window x11Window) {
         this.display = display;
         this.x11Window = x11Window;
      }

      public String getTitle() throws X11Exception {
         try {
            return this.getUtf8Property(this.display.getAtom("UTF8_STRING"), "_NET_WM_NAME");
         } catch (X11Exception var2) {
            return this.getUtf8Property(X11.XA_STRING, X11.XA_WM_NAME);
         }
      }

      public Integer[] getState() throws X11Exception {
         return this.getIntListProperty(this.display.getAtom("ATOM"), "_NET_WM_STATE");
      }

      public Integer[] getType() throws X11Exception {
         return this.getIntListProperty(this.display.getAtom("ATOM"), "_NET_WM_WINDOW_TYPE");
      }

      public String getWindowClass() throws X11Exception {
         return this.getUtf8Property(X11.XA_STRING, X11.XA_WM_CLASS);
      }

      public Integer getPID() throws X11Exception {
         return this.getIntProperty(X11.XA_CARDINAL, "_NET_WM_PID");
      }

      public int getDesktop() throws X11Exception {
         try {
            return this.getIntProperty(X11.XA_CARDINAL, "_NET_WM_DESKTOP");
         } catch (X11Exception var2) {
            return this.getIntProperty(X11.XA_CARDINAL, "_WIN_WORKSPACE");
         }
      }

      public String getMachine() throws X11Exception {
         return this.getStringProperty(X11.XA_STRING, "WM_CLIENT_MACHINE");
      }

      public X11.XWindowAttributes getXWindowAttributes() {
         X11.XWindowAttributes xwa = new X11.XWindowAttributes();
         X.x11.XGetWindowAttributes(this.display.x11Display, this.x11Window, xwa);
         return xwa;
      }

      public Geometry getGeometry() {
         X11.WindowByReference junkRoot = new X11.WindowByReference();
         IntByReference junkX = new IntByReference();
         IntByReference junkY = new IntByReference();
         IntByReference x = new IntByReference();
         IntByReference y = new IntByReference();
         IntByReference width = new IntByReference();
         IntByReference height = new IntByReference();
         IntByReference borderWidth = new IntByReference();
         IntByReference depth = new IntByReference();
         X.x11.XGetGeometry(this.display.x11Display, this.x11Window, junkRoot, junkX, junkY, width, height, borderWidth, depth);
         X.x11.XTranslateCoordinates(this.display.x11Display, this.x11Window, junkRoot.getValue(), 0, 0, x, y, junkRoot);
         return new Geometry(x.getValue(), y.getValue(), width.getValue(), height.getValue(), borderWidth.getValue(), depth.getValue());
      }

      public Rectangle getBounds() {
         X11.WindowByReference junkRoot = new X11.WindowByReference();
         IntByReference junkX = new IntByReference();
         IntByReference junkY = new IntByReference();
         IntByReference x = new IntByReference();
         IntByReference y = new IntByReference();
         IntByReference width = new IntByReference();
         IntByReference height = new IntByReference();
         IntByReference borderWidth = new IntByReference();
         IntByReference depth = new IntByReference();
         X.x11.XGetGeometry(this.display.x11Display, this.x11Window, junkRoot, junkX, junkY, width, height, borderWidth, depth);
         X.x11.XTranslateCoordinates(this.display.x11Display, this.x11Window, junkRoot.getValue(), 0, 0, x, y, junkRoot);
         return new Rectangle(x.getValue(), y.getValue(), width.getValue(), height.getValue());
      }

      public void activate() throws X11Exception {
         this.clientMsg("_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0);
         X.x11.XMapRaised(this.display.x11Display, this.x11Window);
      }

      public int moveToDesktop(int desktopNr) throws X11Exception {
         return this.clientMsg("_NET_WM_DESKTOP", desktopNr, 0, 0, 0, 0);
      }

      public void selectInput(int eventMask) {
         X.x11.XSelectInput(this.display.x11Display, this.x11Window, new NativeLong((long)eventMask));
      }

      public int nextEvent(X11.XEvent event) {
         return X.x11.XNextEvent(this.display.x11Display, event);
      }

      public void sendEvent(int eventMask, X11.XEvent event) {
         X.x11.XSendEvent(this.display.x11Display, this.x11Window, 1, new NativeLong((long)eventMask), event);
      }

      public int close() throws X11Exception {
         return this.clientMsg("_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0);
      }

      public Integer getIntProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         byte[] property = this.getProperty(xaPropType, xaPropName);
         return property == null ? null : X.bytesToInt(property);
      }

      public Integer getIntProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getIntProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public Integer[] getIntListProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         byte[] property = this.getProperty(xaPropType, xaPropName);
         if (property == null) {
            return null;
         } else {
            int listLength = property.length / 4;
            Integer[] list = new Integer[listLength];

            for(int i = 0; i < list.length; ++i) {
               int byteIdx = i * 4;
               int value = X.bytesToInt(property[byteIdx], property[byteIdx + 1], property[byteIdx + 2], property[byteIdx + 3]);
               list[i] = value;
            }

            return list;
         }
      }

      public Integer[] getIntListProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getIntListProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public Window getWindowProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         Integer windowId = this.getIntProperty(xaPropType, xaPropName);
         if (windowId == null) {
            return null;
         } else {
            X11.Window x11Window = new X11.Window((long)windowId);
            return new Window(this.display, x11Window);
         }
      }

      public Window getWindowProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getWindowProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public byte[] getNullTerminatedProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         byte[] bytesOrig = this.getProperty(xaPropType, xaPropName);
         if (bytesOrig == null) {
            return null;
         } else {
            int i;
            for(i = 0; i < bytesOrig.length && bytesOrig[i] != 0; ++i) {
            }

            byte[] bytesDest;
            if (i < bytesOrig.length - 1) {
               bytesDest = new byte[i + 1];
               System.arraycopy(bytesOrig, 0, bytesDest, 0, i + 1);
            } else {
               bytesDest = bytesOrig;
            }

            return bytesDest;
         }
      }

      public byte[] getNullTerminatedProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getNullTerminatedProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public byte[] getNullReplacedStringProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         byte[] bytes = this.getProperty(xaPropType, xaPropName);
         if (bytes == null) {
            return null;
         } else {
            for(int i = 0; i < bytes.length; ++i) {
               if (bytes[i] == 0) {
                  bytes[i] = 46;
               }
            }

            return bytes;
         }
      }

      public byte[] getNullReplacedStringProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getNullReplacedStringProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public String getStringProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         return new String(this.getNullReplacedStringProperty(xaPropType, xaPropName));
      }

      public String getStringProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return new String(this.getNullReplacedStringProperty(xaPropType, xaPropName));
      }

      public String[] getStringListProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         return (new String(this.getProperty(xaPropType, xaPropName))).split("\u0000");
      }

      public String[] getStringListProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         byte[] property = this.getProperty(xaPropType, xaPropName);
         return property == null ? null : (new String(property)).split("\u0000");
      }

      public String getUtf8Property(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         byte[] property = this.getNullReplacedStringProperty(xaPropType, xaPropName);
         return property == null ? null : new String(property, StandardCharsets.UTF_8);
      }

      public String getUtf8Property(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getUtf8Property(xaPropType, this.display.getAtom(xaPropName));
      }

      public String[] getUtf8ListProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         return (new String(this.getProperty(xaPropType, xaPropName), StandardCharsets.UTF_8)).split("\u0000");
      }

      public String[] getUtf8ListProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getUtf8ListProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public byte[] getProperty(X11.Atom xaPropType, X11.Atom xaPropName) throws X11Exception {
         X11.AtomByReference xaRetTypeRef = new X11.AtomByReference();
         IntByReference retFormatRef = new IntByReference();
         NativeLongByReference retNItemsRef = new NativeLongByReference();
         NativeLongByReference retBytesAfterRef = new NativeLongByReference();
         PointerByReference retPropRef = new PointerByReference();
         NativeLong longOffset = new NativeLong(0L);
         NativeLong longLength = new NativeLong(1024L);
         if (X.x11.XGetWindowProperty(this.display.x11Display, this.x11Window, xaPropName, longOffset, longLength, false, xaPropType, xaRetTypeRef, retFormatRef, retNItemsRef, retBytesAfterRef, retPropRef) != 0) {
            String propName = X.x11.XGetAtomName(this.display.x11Display, xaPropName);
            throw new X11Exception("Cannot get " + propName + " property.");
         } else {
            X11.Atom xaRetType = xaRetTypeRef.getValue();
            Pointer retProp = retPropRef.getValue();
            if (xaRetType != null && xaPropType != null && xaRetType.toNative().equals(xaPropType.toNative())) {
               int retFormat = retFormatRef.getValue();
               long retNItems = retNItemsRef.getValue().longValue();
               int nBytes;
               if (retFormat == 32) {
                  nBytes = Native.LONG_SIZE;
               } else if (retFormat == 16) {
                  nBytes = Native.LONG_SIZE / 2;
               } else if (retFormat == 8) {
                  nBytes = 1;
               } else {
                  if (retFormat != 0) {
                     throw new X11Exception("Invalid return format");
                  }

                  nBytes = 0;
               }

               int length = Math.min((int)retNItems * nBytes, 4096);
               byte[] ret = retProp.getByteArray(0L, length);
               X.x11.XFree(retProp);
               return ret;
            } else {
               X.x11.XFree(retProp);
               String propName = X.x11.XGetAtomName(this.display.x11Display, xaPropName);
               throw new X11Exception("Invalid type of " + propName + " property");
            }
         }
      }

      public byte[] getProperty(X11.Atom xaPropType, String xaPropName) throws X11Exception {
         return this.getProperty(xaPropType, this.display.getAtom(xaPropName));
      }

      public int clientMsg(String msg, int data0, int data1, int data2, int data3, int data4) throws X11Exception {
         return this.clientMsg(msg, new NativeLong((long)data0), new NativeLong((long)data1), new NativeLong((long)data2), new NativeLong((long)data3), new NativeLong((long)data4));
      }

      public int clientMsg(String msg, NativeLong data0, NativeLong data1, NativeLong data2, NativeLong data3, NativeLong data4) throws X11Exception {
         NativeLong mask = new NativeLong(1572864L);
         X11.XClientMessageEvent event = new X11.XClientMessageEvent();
         event.type = 33;
         event.serial = new NativeLong(0L);
         event.send_event = 1;
         event.message_type = this.display.getAtom(msg);
         event.window = this.x11Window;
         event.format = 32;
         event.data.setType(NativeLong[].class);
         event.data.l[0] = data0;
         event.data.l[1] = data1;
         event.data.l[2] = data2;
         event.data.l[3] = data3;
         event.data.l[4] = data4;
         X11.XEvent e = new X11.XEvent();
         e.setTypedValue(event);
         if (X.x11.XSendEvent(this.display.x11Display, this.display.getRootWindow().x11Window, 0, mask, e) == 0) {
            throw new X11Exception("Cannot send " + msg + " event.");
         } else {
            return 0;
         }
      }

      public Window[] getSubwindows() throws X11Exception {
         X11.WindowByReference root = new X11.WindowByReference();
         X11.WindowByReference parent = new X11.WindowByReference();
         PointerByReference children = new PointerByReference();
         IntByReference childCount = new IntByReference();
         if (X.x11.XQueryTree(this.display.x11Display, this.x11Window, root, parent, children, childCount) == 0) {
            throw new X11Exception("Can't query subwindows");
         } else if (childCount.getValue() == 0) {
            return null;
         } else {
            Window[] retVal = new Window[childCount.getValue()];
            if (X11.XID.SIZE == 4) {
               int[] windows = children.getValue().getIntArray(0L, childCount.getValue());

               for(int x = 0; x < retVal.length; ++x) {
                  X11.Window win = new X11.Window((long)windows[x]);
                  retVal[x] = new Window(this.display, win);
               }
            } else {
               long[] windows = children.getValue().getLongArray(0L, childCount.getValue());

               for(int x = 0; x < retVal.length; ++x) {
                  X11.Window win = new X11.Window(windows[x]);
                  retVal[x] = new Window(this.display, win);
               }
            }

            X.x11.XFree(children.getValue());
            return retVal;
         }
      }

      public Window[] getAllSubwindows() throws X11Exception {
         List<Window> list = new ArrayList();
         recurse(list, X.x11, this.display, this.x11Window, 0);
         return (Window[])list.toArray(new Window[0]);
      }

      private static void recurse(List list, X11 x11, Display display, X11.Window root, int depth) {
         X11.WindowByReference windowRef = new X11.WindowByReference();
         X11.WindowByReference parentRef = new X11.WindowByReference();
         PointerByReference childrenRef = new PointerByReference();
         IntByReference childCountRef = new IntByReference();
         x11.XQueryTree(display.x11Display, root, windowRef, parentRef, childrenRef, childCountRef);
         if (childrenRef.getValue() != null) {
            long[] ids;
            if (Native.LONG_SIZE == 8) {
               ids = childrenRef.getValue().getLongArray(0L, childCountRef.getValue());
            } else {
               if (Native.LONG_SIZE != 4) {
                  throw new IllegalStateException("Unexpected value for Native.LONG_SIZE: " + Native.LONG_SIZE);
               }

               int[] intIds = childrenRef.getValue().getIntArray(0L, childCountRef.getValue());
               ids = new long[intIds.length];

               for(int i = 0; i < intIds.length; ++i) {
                  ids[i] = (long)intIds[i];
               }
            }

            for(long id : ids) {
               if (id != 0L) {
                  X11.Window window = new X11.Window(id);
                  list.add(new Window(display, window));
                  recurse(list, x11, display, window, depth + 1);
               }
            }

         }
      }

      public String toString() {
         return this.x11Window.toString();
      }

      public static class Geometry {
         public int x;
         public int y;
         public int width;
         public int height;
         public int borderWidth;
         public int depth;

         public Geometry(int x, int y, int width, int height, int borderWidth, int depth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.borderWidth = borderWidth;
            this.depth = depth;
         }
      }
   }

   public static class X11Exception extends Exception {
      private static final long serialVersionUID = 1L;

      public X11Exception() {
      }

      public X11Exception(String message) {
         super(message);
      }

      public X11Exception(String message, Throwable cause) {
         super(message, cause);
      }

      public X11Exception(Throwable cause) {
         super(cause);
      }
   }
}
