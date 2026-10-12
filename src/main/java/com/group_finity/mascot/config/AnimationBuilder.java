package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.sound.SoundLoader;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnimationBuilder {
   private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName());
   private final String condition;
   private String imageSet = "";
   private final List<Pose> poses = new ArrayList<>();
   private final List<Hotspot> hotspots = new ArrayList<>();
   private final ResourceBundle schema;
   private final String turn;

   public AnimationBuilder(ResourceBundle schema, Entry animationNode, String imageSet) throws IOException {
      if (!imageSet.isEmpty()) {
         this.imageSet = "/" + imageSet;
      }

      this.schema = schema;
      this.condition = animationNode.getAttribute(schema.getString("Condition")) == null ? "true" : animationNode.getAttribute(schema.getString("Condition"));
      this.turn = animationNode.getAttribute(schema.getString("IsTurn")) == null ? "false" : animationNode.getAttribute(schema.getString("IsTurn"));
      log.log(Level.INFO, "Start Reading Animations");

      for(Entry frameNode : animationNode.selectChildren(schema.getString("Pose"))) {
         this.poses.add(this.loadPose(frameNode));
      }

      for(Entry frameNode : animationNode.selectChildren(schema.getString("Hotspot"))) {
         this.hotspots.add(this.loadHotspot(frameNode));
      }

      log.log(Level.INFO, "Animations Finished Loading");
   }

   private Pose loadPose(Entry frameNode) throws IOException {
      String imageText = frameNode.getAttribute(this.schema.getString("Image")) != null ? this.imageSet + frameNode.getAttribute(this.schema.getString("Image")) : null;
      String imageRightText = frameNode.getAttribute(this.schema.getString("ImageRight")) != null ? this.imageSet + frameNode.getAttribute(this.schema.getString("ImageRight")) : null;
      String anchorText = frameNode.getAttribute(this.schema.getString("ImageAnchor")) != null ? frameNode.getAttribute(this.schema.getString("ImageAnchor")) : null;
      String moveText = frameNode.getAttribute(this.schema.getString("Velocity"));
      String durationText = frameNode.getAttribute(this.schema.getString("Duration"));
      String soundText = frameNode.getAttribute(this.schema.getString("Sound")) != null ? frameNode.getAttribute(this.schema.getString("Sound")) : null;
      String volumeText = frameNode.getAttribute(this.schema.getString("Volume")) != null ? frameNode.getAttribute(this.schema.getString("Volume")) : "0";
      double opacity = com.group_finity.mascot.settings.RuntimeSettings.number("Opacity",1,.2,1);
      double scaling = com.group_finity.mascot.settings.RuntimeSettings.number("Scaling",1,.5,3);
      String filterText = Main.getInstance().getProperties().getProperty("Filter", "false");
      ImagePairLoader.Filter filter = ImagePairLoader.Filter.NEAREST_NEIGHBOUR;
      if (!filterText.equalsIgnoreCase("true") && !filterText.equalsIgnoreCase("hqx")) {
         if (filterText.equalsIgnoreCase("bicubic")) {
            filter = ImagePairLoader.Filter.BICUBIC;
         }
      } else {
         filter = ImagePairLoader.Filter.HQX;
      }

      if (imageText != null) {
         try {
            String[] anchorCoordinates = anchorText.split(",");
            Point anchor = new Point(Integer.parseInt(anchorCoordinates[0]), Integer.parseInt(anchorCoordinates[1]));
            ImagePairLoader.load(imageText, imageRightText, anchor, scaling, filter, opacity);
         } catch (Exception var22) {
            String error = imageText;
            if (imageRightText != null) {
               error = imageText + ", " + imageRightText;
            }

            log.log(Level.SEVERE, "Failed to load image: {0}", error);
            String var10002 = Main.getInstance().getLanguageBundle().getString("FailedLoadImageErrorMessage");
            throw new IOException(var10002 + " " + error);
         }
      }

      String[] moveCoordinates = moveText.split(",");
      int moveX = Integer.parseInt(moveCoordinates[0]);
      int moveY = Integer.parseInt(moveCoordinates[1]);
      moveX = Math.abs(moveX) > 0 && Math.abs((double)moveX * scaling) < (double)1.0F ? (moveX > 0 ? 1 : -1) : (int)Math.round((double)moveX * scaling);
      moveY = Math.abs(moveY) > 0 && Math.abs((double)moveY * scaling) < (double)1.0F ? (moveY > 0 ? 1 : -1) : (int)Math.round((double)moveY * scaling);
      Point move = new Point(moveX, moveY);
      int duration = Integer.parseInt(durationText);
      if (soundText != null) {
         try {
            String relative=soundText.startsWith("/")?soundText.substring(1):soundText;
            soundText=com.group_finity.mascot.pack.PackPaths.inside(com.group_finity.mascot.AppPaths.pack(this.imageSet.substring(1)).resolve("sound"),relative).toString();

            SoundLoader.load(soundText, Float.parseFloat(volumeText));
            soundText = soundText + Float.parseFloat(volumeText);
         } catch (Exception var21) {
            log.log(Level.WARNING, "Optional sound ignored: {0}", soundText);
            soundText=null;
         }
      }

      Pose pose = new Pose(imageText, imageRightText, move.x, move.y, duration, soundText,frameNode.getAttribute("SoundEvent"));
      log.log(Level.INFO, "ReadPosition({0})", pose);
      return pose;
   }

   private Hotspot loadHotspot(Entry frameNode) throws IOException {
      String shapeText = frameNode.getAttribute(this.schema.getString("Shape"));
      String originText = frameNode.getAttribute(this.schema.getString("Origin"));
      String sizeText = frameNode.getAttribute(this.schema.getString("Size"));
      String behaviourText = frameNode.getAttribute(this.schema.getString("Behaviour"));
      double scaling = com.group_finity.mascot.settings.RuntimeSettings.number("Scaling",1,.5,3);
      String[] originCoordinates = originText.split(",");
      String[] sizeCoordinates = sizeText.split(",");
      Point origin = new Point((int)Math.round((double)Integer.parseInt(originCoordinates[0]) * scaling), (int)Math.round((double)Integer.parseInt(originCoordinates[1]) * scaling));
      Dimension size = new Dimension((int)Math.round((double)Integer.parseInt(sizeCoordinates[0]) * scaling), (int)Math.round((double)Integer.parseInt(sizeCoordinates[1]) * scaling));
      Shape shape;
      if (shapeText.equalsIgnoreCase("Rectangle")) {
         shape = new Rectangle(origin, size);
      } else {
         if (!shapeText.equalsIgnoreCase("Ellipse")) {
            log.log(Level.SEVERE, "Failed to load hotspot shape: {0}", shapeText);
            String var10002 = Main.getInstance().getLanguageBundle().getString("HotspotShapeNotSupportedErrorMessage");
            throw new IOException(var10002 + " " + shapeText);
         }

         shape = new Ellipse2D.Float((float)origin.x, (float)origin.y, (float)size.width, (float)size.height);
      }

      Hotspot hotspot = new Hotspot(behaviourText, shape);
      log.log(Level.INFO, "ReadHotSpot({0})", hotspot);
      return hotspot;
   }

   public Animation buildAnimation() throws AnimationInstantiationException {
      try {
         return new Animation(Variable.parse(this.condition), this.poses.toArray(new Pose[0]), this.hotspots.toArray(new Hotspot[0]), Boolean.parseBoolean(this.turn));
      } catch (VariableException e) {
         throw new AnimationInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedConditionEvaluationErrorMessage"), e);
      }
   }
}
