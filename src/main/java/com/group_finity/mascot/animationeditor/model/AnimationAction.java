package com.group_finity.mascot.animationeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationAction {
   private String name;
   private String type;
   private String className;
   private String borderType;
   private Map<String, String> attributes;
   private List<AnimationPose> poses;

   public AnimationAction() {
      this.attributes = new HashMap<>();
      this.poses = new ArrayList<>();
   }

   public AnimationAction(String name, String type) {
      this();
      this.name = name;
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getClassName() {
      return this.className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getBorderType() {
      return this.borderType;
   }

   public void setBorderType(String borderType) {
      this.borderType = borderType;
   }

   public Map<String, String> getAttributes() {
      return this.attributes;
   }

   public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
   }

   public List<AnimationPose> getPoses() {
      return this.poses;
   }

   public void setPoses(List<AnimationPose> poses) {
      this.poses = poses;
   }

   public void addPose(AnimationPose pose) {
      this.poses.add(pose);
   }

   public void removePose(AnimationPose pose) {
      this.poses.remove(pose);
   }

   public void setAttribute(String key, String value) {
      this.attributes.put(key, value);
   }

   public String getAttribute(String key) {
      return this.attributes.get(key);
   }

   public String toString() {
      return this.name != null ? this.name : "Unnamed Action";
   }
}
