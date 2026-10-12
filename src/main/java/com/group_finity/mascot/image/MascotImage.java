package com.group_finity.mascot.image;

import com.group_finity.mascot.NativeFactory;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

public record MascotImage(NativeImage image, Point center, Dimension size) {
   public MascotImage(BufferedImage image, Point center) {
      this(NativeFactory.getInstance().newNativeImage(image), center, new Dimension(image.getWidth(), image.getHeight()));
   }
}
