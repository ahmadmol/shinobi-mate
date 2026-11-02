package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairs;
import java.awt.Point;

public class Pose {
   private final String image;
   private final String rightImage;
   private final int dx;
   private final int dy;
   private final int duration;
   private final String sound;
   private final String soundEvent;
   private final Point imageAnchor;
   private java.lang.ref.WeakReference<com.group_finity.mascot.image.MascotImage> anchoredLeft=new java.lang.ref.WeakReference<>(null),anchoredRight=new java.lang.ref.WeakReference<>(null);

   public Pose(String image) {
      this(image, "", 0, 0, 1);
   }

   public Pose(String image, int duration) {
      this(image, "", 0, 0, duration);
   }

   public Pose(String image, int dx, int dy, int duration) {
      this(image, "", dx, dy, duration);
   }

   public Pose(String image, String rightImage) {
      this(image, rightImage, 0, 0, 1);
   }

   public Pose(String image, String rightImage, int duration) {
      this(image, rightImage, 0, 0, duration);
   }

   public Pose(String image, String rightImage, int dx, int dy, int duration) {
      this(image, rightImage, dx, dy, duration, (String)null);
   }

   public Pose(String image, String rightImage, int dx, int dy, int duration, String sound) {
      this(image,rightImage,dx,dy,duration,sound,null);
   }
   public Pose(String image, String rightImage, int dx, int dy, int duration, String sound,String soundEvent) {
      this(image,rightImage,dx,dy,duration,sound,soundEvent,null);
   }
   public Pose(String image, String rightImage, int dx, int dy, int duration, String sound,String soundEvent,Point imageAnchor) {
      this.image = image;
      this.rightImage = rightImage;
      this.dx = dx;
      this.dy = dy;
      this.duration = duration;
      this.sound = sound;
      this.soundEvent = soundEvent;
      this.imageAnchor=imageAnchor==null?null:new Point(imageAnchor);
   }

   public String toString() {
      String var10000 = String.valueOf(this.getImage() == null ? "" : this.getImage());
      return "Pose (" + var10000 + "," + this.getDx() + "," + this.getDy() + "," + this.getDuration() + ", " + this.sound + ")";
   }

   public void next(Mascot mascot) {
      mascot.movePose(mascot.isLookRight() ? -this.getDx() : this.getDx(),this.getDy());
      mascot.setImage(poseImage(mascot.isLookRight()));
      mascot.setSound(this.getSoundName());
   }
   private com.group_finity.mascot.image.MascotImage poseImage(boolean right) {
      var source=ImagePairs.getImage(getImageName(),right);
      if(source==null||imageAnchor==null)return source;
      int x=right?source.size().width-imageAnchor.x:imageAnchor.x;
      if(source.center().x==x&&source.center().y==imageAnchor.y)return source;
      var cached=(right?anchoredRight:anchoredLeft).get();
      if(cached!=null&&cached.image()==source.image())return cached;
      // Anchor is pose geometry, not part of the shared PNG's identity. Keep the bounded
      // native bitmap cache; weak wrappers must not retain every pose after eviction.
      var result=new com.group_finity.mascot.image.MascotImage(source.image(),new Point(x,imageAnchor.y),source.size());
      if(right)anchoredRight=new java.lang.ref.WeakReference<>(result);else anchoredLeft=new java.lang.ref.WeakReference<>(result);
      return result;
   }
   public void emitSound(Mascot mascot) {
      if(soundEvent!=null&&!soundEvent.isBlank())mascot.playAudio(soundEvent);
      if(sound!=null)com.group_finity.mascot.sound.Sounds.play(sound,mascot.getAudioScope());
   }

   public int getDuration() {
      return this.duration;
   }

   public String getImageName() {
      String var10000 = this.image == null ? "" : this.image;
      return var10000 + (this.rightImage == null ? "" : this.rightImage);
   }

   public ImagePair getImage() {
      return ImagePairs.getImagePair(this.getImageName());
   }

   public int getDx() {
      return this.dx;
   }

   public int getDy() {
      return this.dy;
   }

   public String getSoundName() {
      return this.sound;
   }
}
