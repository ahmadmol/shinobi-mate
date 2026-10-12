package com.group_finity.mascot.image;

import java.awt.Component;

public interface TranslucentWindow {
   Component asComponent();

   void setImage(NativeImage var1);

   void updateImage();

   void dispose();

   void setAlwaysOnTop(boolean var1);
}
