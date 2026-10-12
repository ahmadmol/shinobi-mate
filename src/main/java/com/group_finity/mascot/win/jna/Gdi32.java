package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

public interface Gdi32 extends StdCallLibrary {
   Gdi32 INSTANCE = (Gdi32)Native.load("Gdi32", Gdi32.class);
   int DIB_RGB_COLORS = 0;

   Pointer CreateCompatibleDC(Pointer var1);

   Pointer SelectObject(Pointer var1, Pointer var2);

   void DeleteDC(Pointer var1);

   Pointer CreateDIBSection(Pointer var1, BITMAPINFOHEADER var2, int var3, Pointer var4, Pointer var5, int var6);

   void GetObjectW(Pointer var1, int var2, BITMAP var3);

   void DeleteObject(Pointer var1);

   Pointer CreateRectRgn(int var1, int var2, int var3, int var4);

   void GetRgnBox(Pointer var1, RECT var2);
}
