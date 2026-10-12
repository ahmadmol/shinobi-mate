package com.group_finity.mascot.x11.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;

public interface X11Extra extends Library {
   X11Extra INSTANCE = (X11Extra)Native.load("X11", X11Extra.class);

   void XMoveWindow(X11.Display var1, X11.Window var2, int var3, int var4);
}
