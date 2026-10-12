package com.group_finity.mascot.image;

public class ImagePair {
   private final MascotImage leftImage;
   private final MascotImage rightImage;

   public ImagePair(MascotImage leftImage, MascotImage rightImage) {
      this.leftImage = leftImage;
      this.rightImage = rightImage;
   }

   public MascotImage getImage(boolean lookRight) {
      return lookRight ? this.getRightImage() : this.getLeftImage();
   }

   private MascotImage getLeftImage() {
      return this.leftImage;
   }

   private MascotImage getRightImage() {
      return this.rightImage;
   }
}
