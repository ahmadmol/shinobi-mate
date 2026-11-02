package com.group_finity.mascot.win;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.win.jna.BLENDFUNCTION;
import com.group_finity.mascot.win.jna.Gdi32;
import com.group_finity.mascot.win.jna.POINT;
import com.group_finity.mascot.win.jna.RECT;
import com.group_finity.mascot.win.jna.SIZE;
import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JWindow;

class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {
   private static final long serialVersionUID = 1L;
   private volatile WindowsNativeImage image;
   private volatile int alpha = 255;
   private record Painted(WindowsNativeImage image,int alpha,int width,int height,java.awt.GraphicsConfiguration configuration) { }
   private volatile Painted painted;

   public Component asComponent() {
      return this;
   }

   private boolean paint(WindowsNativeImage source, int alpha) {
      Pointer hWnd = Native.getComponentPointer(this);
      if (User32.INSTANCE.IsWindow(hWnd) != 0) {
         int exStyle = User32.INSTANCE.GetWindowLongW(hWnd, -20);
         if ((exStyle & 524288) == 0) {
            User32.INSTANCE.SetWindowLongW(hWnd, -20, exStyle | 524288);
         }

         RECT windowRect = new RECT();
         User32.INSTANCE.GetWindowRect(hWnd, windowRect);
         if (windowRect.Width() <= 0 || windowRect.Height() <= 0) return false;
         WindowsNativeImage physicalImage = source.forSize(windowRect.Width(), windowRect.Height());
         Pointer imageHandle = physicalImage.getHandle();
         Pointer clientDC = User32.INSTANCE.GetDC(hWnd);
         Pointer memDC = Gdi32.INSTANCE.CreateCompatibleDC(clientDC);
         Pointer oldBmp = Gdi32.INSTANCE.SelectObject(memDC, imageHandle);
         User32.INSTANCE.ReleaseDC(hWnd, clientDC);
         BLENDFUNCTION bf = new BLENDFUNCTION();
         bf.BlendOp = 0;
         bf.BlendFlags = 0;
         bf.SourceConstantAlpha = (byte)alpha;
         bf.AlphaFormat = 1;
         POINT lt = new POINT();
         lt.x = windowRect.left;
         lt.y = windowRect.top;
         SIZE size = new SIZE();
         size.cx = windowRect.Width();
         size.cy = windowRect.Height();
         POINT zero = new POINT();
         boolean updated=User32.INSTANCE.UpdateLayeredWindow(hWnd, Pointer.NULL, lt, size, memDC, zero, 0, bf, 2)!=0;
         Gdi32.INSTANCE.SelectObject(memDC, oldBmp);
         Gdi32.INSTANCE.DeleteDC(memDC);
         java.lang.ref.Reference.reachabilityFence(physicalImage);
         return updated;
      }
      return false;
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "LayeredWindow[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.getBounds()) + "]";
   }

   public void paint(Graphics g) {
      WindowsNativeImage source=this.getImage();int opacity=this.getAlpha();
      int width=getWidth(),height=getHeight();var configuration=getGraphicsConfiguration();
      if (source != null&&this.paint(source,opacity)) {
         painted=new Painted(source,opacity,width,height,configuration);
      }

   }

   private WindowsNativeImage getImage() {
      return this.image;
   }

   public void setImage(NativeImage image) {
      this.image = (WindowsNativeImage)image;
   }

   public int getAlpha() {
      return this.alpha;
   }

   public void setAlpha(int alpha) {
      this.alpha = alpha;
   }

   public void updateImage() {
      Painted last=painted;
      // Moving a layered HWND preserves its bitmap. AWT still paints on expose
      // and resize; only repeated identical engine repaint requests are omitted.
      if(last==null||last.image()!=image||last.alpha()!=alpha||last.width()!=getWidth()||
            last.height()!=getHeight()||last.configuration()!=getGraphicsConfiguration())this.repaint();
   }
}
