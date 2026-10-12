package com.group_finity.mascot.mac;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.Component;
import javax.swing.JRootPane;
import javax.swing.JWindow;

class MacTranslucentWindow implements TranslucentWindow {
   private TranslucentWindow delegate;
   private boolean imageChanged = false;
   private NativeImage oldImage = null;

   MacTranslucentWindow(NativeFactory factory) {
      this.delegate = factory.newTransparentWindow();
      JRootPane rootPane = ((JWindow)this.delegate.asComponent()).getRootPane();
      rootPane.putClientProperty("Window.shadow", Boolean.FALSE);
      rootPane.putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);
   }

   public Component asComponent() {
      return this.delegate.asComponent();
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "MacTranslucentWindow[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.asComponent().getBounds()) + "]";
   }

   public void setImage(NativeImage image) {
      this.imageChanged = this.oldImage != null && image != this.oldImage;
      this.oldImage = image;
      this.delegate.setImage(image);
   }

   public void updateImage() {
      if (this.imageChanged) {
         this.delegate.updateImage();
         this.imageChanged = false;
      }

   }

   public void dispose() {
      this.delegate.dispose();
   }

   public void setAlwaysOnTop(boolean onTop) {
      ((JWindow)this.delegate.asComponent()).setAlwaysOnTop(onTop);
   }
}
