package com.group_finity.mascot.win;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.win.jna.BITMAP;
import com.group_finity.mascot.win.jna.BITMAPINFOHEADER;
import com.group_finity.mascot.win.jna.Gdi32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.awt.Graphics;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.lang.ref.Cleaner;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

class WindowsNativeImage implements NativeImage {
   private static final Cleaner CLEANER = Cleaner.create();
   private record BitmapCleanup(Pointer handle) implements Runnable {
      public void run() { if (handle != null) Gdi32.INSTANCE.DeleteObject(handle); }
   }
   private final BufferedImage managedImage;
   private final Pointer nativeHandle;
   private final Cleaner.Cleanable cleanup;
   private final Map<Long, WindowsNativeImage> scaledImages = new LinkedHashMap<>(4, 0.75f, true);

   private static Pointer createNative(int width, int height) {
      BITMAPINFOHEADER bmi = new BITMAPINFOHEADER();
      bmi.biSize = 40;
      bmi.biWidth = width;
      bmi.biHeight = height;
      bmi.biPlanes = 1;
      bmi.biBitCount = 32;
      return Gdi32.INSTANCE.CreateDIBSection(Pointer.NULL, bmi, 0, Pointer.NULL, Pointer.NULL, 0);
   }

   private static void flushNative(Pointer nativeHandle, int[] rgb) {
      BITMAP bmp = new BITMAP();
      Gdi32.INSTANCE.GetObjectW(nativeHandle, Main.getInstance().getPlatform().getBitmapSize() + Native.POINTER_SIZE, bmp);
      int width = bmp.bmWidth;
      int height = bmp.bmHeight;
      int destPitch = (bmp.bmWidth * bmp.bmBitsPixel + 31) / 32 * 4;
      long destIndex = (long)destPitch * ((long)height - 1L);

      for(int i = 0; i < rgb.length; ++i) {
         if ((rgb[i] & -16777216) == 0) {
            rgb[i] = 0;
         }
      }

      for(int y = 0; y < height; ++y) {
         int srcRowIndex = y * width;
         bmp.bmBits.write(destIndex, rgb, srcRowIndex, width);
         destIndex -= (long)destPitch;
      }

   }

   public WindowsNativeImage(BufferedImage image) {
      this.managedImage = image;
      this.nativeHandle = createNative(image.getWidth(), image.getHeight());
      this.cleanup = CLEANER.register(this, new BitmapCleanup(this.nativeHandle));
      int[] rbgValues = image.getRGB(0, 0, image.getWidth(), image.getHeight(), (int[])null, 0, image.getWidth());
      flushNative(this.getNativeHandle(), rbgValues);
   }

   /** LayeredWindow dimensions are physical pixels, even when Swing bounds are logical. */
   synchronized WindowsNativeImage forSize(int width, int height) {
      if (width == getWidth() && height == getHeight()) return this;
      long key = ((long)width << 32) | (height & 0xffffffffL);
      WindowsNativeImage cached = this.scaledImages.get(key);
      if (cached != null) return cached;
      BufferedImage pixels = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
      var graphics = pixels.createGraphics();
      graphics.setComposite(AlphaComposite.Src);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.drawImage(this.managedImage, 0, 0, width, height, null);
      graphics.dispose();
      cached = new WindowsNativeImage(pixels);
      if (this.scaledImages.size() >= 4) {
         var entry = this.scaledImages.entrySet().iterator();
         WindowsNativeImage old = entry.next().getValue();entry.remove();old.cleanup.clean();
      }
      this.scaledImages.put(key, cached);
      return cached;
   }

   public void update() {
   }

   public void flush() {
      this.managedImage.flush();
   }

   public Graphics getGraphics() {
      return this.managedImage.createGraphics();
   }

   public Pointer getHandle() {
      return this.nativeHandle;
   }

   public int getHeight() {
      return this.managedImage.getHeight();
   }

   public int getWidth() {
      return this.managedImage.getWidth();
   }

   public int getHeight(ImageObserver observer) {
      return this.managedImage.getHeight(observer);
   }

   public Object getProperty(String name, ImageObserver observer) {
      return this.managedImage.getProperty(name, observer);
   }

   public ImageProducer getSource() {
      return this.managedImage.getSource();
   }

   public int getWidth(ImageObserver observer) {
      return this.managedImage.getWidth(observer);
   }

   private Pointer getNativeHandle() {
      return this.nativeHandle;
   }
}
