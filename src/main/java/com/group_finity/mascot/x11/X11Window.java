package com.group_finity.mascot.x11;

import java.awt.Point;

public class X11Window {
   private final Point window_anchor = new Point();
   private boolean dragged;
   private int top;
   private int bottom;
   private int left;
   private int right;
   private int top_border;
   private int left_border;
   private int bottom_border;
   private int right_border;

   public int getTop_border() {
      return this.top_border;
   }

   public void setTop_border(int top_border) {
      this.top_border = top_border;
   }

   public int getLeft_border() {
      return this.left_border;
   }

   public void setLeft_border(int left_border) {
      this.left_border = left_border;
   }

   public int getBottom_border() {
      return this.bottom_border;
   }

   public void setBottom_border(int bottom_border) {
      this.bottom_border = bottom_border;
   }

   public int getRight_border() {
      return this.right_border;
   }

   public int getHeight_border() {
      return this.top_border + this.bottom_border;
   }

   public int getWidth_border() {
      return this.right_border + this.left_border;
   }

   public void setRight_border(int right_border) {
      this.right_border = right_border;
   }

   X11Window(int x, int y) {
      this.window_anchor.setLocation(x, y);
      this.dragged = false;
   }

   X11Window() {
      this.dragged = false;
   }

   public int getX() {
      return this.window_anchor.x;
   }

   public int getY() {
      return this.window_anchor.y;
   }

   public void setX(int val) {
      if (!this.dragged) {
         this.window_anchor.x = val;
      }

   }

   public void setY(int val) {
      if (!this.dragged) {
         this.window_anchor.y = val;
      }

   }

   public void setDragged(boolean b) {
      this.dragged = b;
   }

   public boolean getDragged() {
      return this.dragged;
   }

   public int getTop() {
      return this.top;
   }

   public void setTop(int top) {
      this.top = top;
   }

   public int getBottom() {
      return this.bottom;
   }

   public void setBottom(int bottom) {
      this.bottom = bottom;
   }

   public int getLeft() {
      return this.left;
   }

   public void setLeft(int left) {
      this.left = left;
   }

   public int getRight() {
      return this.right;
   }

   public void setRight(int right) {
      this.right = right;
   }

   public String toString() {
      return "Frame Border: top:" + this.top_border + " left:" + this.left_border + " bottom:" + this.bottom_border + " right:" + this.right_border;
   }
}
