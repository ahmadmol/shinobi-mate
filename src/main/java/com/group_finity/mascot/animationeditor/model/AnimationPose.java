package com.group_finity.mascot.animationeditor.model;

public class AnimationPose {
   private String image;
   private String imageAnchor;
   private String velocity;
   private int duration;
   private String sound;
   private double volume = (double)1.0F;

   public AnimationPose() {
      this.duration = 1;
   }

   public AnimationPose(String image, String imageAnchor, String velocity, int duration) {
      this.image = image;
      this.imageAnchor = imageAnchor;
      this.velocity = velocity;
      this.duration = duration;
   }

   public String getImage() {
      return this.image;
   }

   public void setImage(String image) {
      this.image = image;
   }

   public String getImageAnchor() {
      return this.imageAnchor;
   }

   public void setImageAnchor(String imageAnchor) {
      this.imageAnchor = imageAnchor;
   }

   public String getVelocity() {
      return this.velocity;
   }

   public void setVelocity(String velocity) {
      this.velocity = velocity;
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public String getSound() {
      return this.sound;
   }

   public void setSound(String sound) {
      this.sound = sound;
   }

   public double getVolume() {
      return this.volume;
   }

   public void setVolume(double volume) {
      this.volume = volume;
   }

   public String toString() {
      return this.image != null ? this.image : "No Image";
   }
}
