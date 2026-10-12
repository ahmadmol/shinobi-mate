package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.util.List;
import java.util.ResourceBundle;

public class Fall extends ActionBase {
   public static final String PARAMETER_INITIALVX = "InitialVX";
   private static final int DEFAULT_INITIALVX = 0;
   private static final String PARAMETER_INITIALVY = "InitialVY";
   private static final int DEFAULT_INITIALVY = 0;
   public static final String PARAMETER_RESISTANCEX = "ResistanceX";
   private static final double DEFAULT_RESISTANCEX = 0.05;
   public static final String PARAMETER_RESISTANCEY = "ResistanceY";
   private static final double DEFAULT_RESISTANCEY = 0.1;
   public static final String PARAMETER_GRAVITY = "Gravity";
   private static final double DEFAULT_GRAVITY = (double)2.0F;
   public static final String VARIABLE_VELOCITYX = "VelocityX";
   public static final String VARIABLE_VELOCITYY = "VelocityY";
   private double velocityX;
   private double velocityY;
   private double modX;
   private double modY;
   private boolean landingSoundPlayed;

   public Fall(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      landingSoundPlayed=false;
      this.setVelocityX((double)this.getInitialVx());
      this.setVelocityY((double)this.getInitialVy());
   }

   public boolean hasNext() throws VariableException {
      Point pos = this.getMascot().getAnchor();
      boolean onBorder = this.getEnvironment().getFloor().isOn(pos);
      if (this.getEnvironment().getWall().isOn(pos)) {
         onBorder = true;
      }

      return super.hasNext() && !onBorder;
   }

   protected void tick() throws LostGroundException, VariableException {
      if (this.getVelocityX() != (double)0.0F) {
         this.getMascot().setLookRight(this.getVelocityX() > (double)0.0F);
      }

      this.setVelocityX(this.getVelocityX() - this.getVelocityX() * this.getResistanceX());
      this.setVelocityY(this.getVelocityY() - this.getVelocityY() * this.getResistanceY() + this.getGravity());
      this.putVariable(this.getSchema().getString("VelocityX"), this.getVelocityX());
      this.putVariable(this.getSchema().getString("VelocityY"), this.getVelocityY());
      this.setModX(this.getModX() + this.getVelocityX() % (double)1.0F);
      this.setModY(this.getModY() + this.getVelocityY() % (double)1.0F);
      int dx = (int)this.getVelocityX() + (int)this.getModX();
      int dy = (int)this.getVelocityY() + (int)this.getModY();
      this.setModX(this.getModX() % (double)1.0F);
      this.setModY(this.getModY() % (double)1.0F);
      int dev = Math.max(1, Math.max(Math.abs(dx), Math.abs(dy)));
      Point start = this.getMascot().getAnchor();

      label39:
      for(int i = 0; i <= dev; ++i) {
         int x = start.x + dx * i / dev;
         int y = start.y + dy * i / dev;
         this.getMascot().setAnchor(new Point(x, y));
         if (dy > 0) {
            for(int j = -80; j <= 0; ++j) {
               this.getMascot().setAnchor(new Point(x, y + j));
               if (this.getEnvironment().getFloor(true).isOn(this.getMascot().getAnchor())) {
                  break label39;
               }
            }
         }

         if (this.getEnvironment().getWall(true).isOn(this.getMascot().getAnchor())) {
            break;
         }
      }

      this.getAnimation().next(this.getMascot(), this.getTime());
      if(!landingSoundPlayed&&dy>0&&this.getEnvironment().getFloor(true).isOn(this.getMascot().getAnchor())) {
         landingSoundPlayed=true;this.getMascot().playAudio("movement.landing");
      }
   }

   private int getInitialVx() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("InitialVX"), Number.class, 0)).intValue();
   }

   private int getInitialVy() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("InitialVY"), Number.class, 0)).intValue();
   }

   private double getGravity() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("Gravity"), Number.class, (double)2.0F)).doubleValue();
   }

   private double getResistanceX() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("ResistanceX"), Number.class, 0.05)).doubleValue();
   }

   private double getResistanceY() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("ResistanceY"), Number.class, 0.1)).doubleValue();
   }

   private void setVelocityY(double velocityY) {
      this.velocityY = velocityY;
   }

   private double getVelocityY() {
      return this.velocityY;
   }

   private void setVelocityX(double velocityX) {
      this.velocityX = velocityX;
   }

   private double getVelocityX() {
      return this.velocityX;
   }

   private void setModX(double modX) {
      this.modX = modX;
   }

   private double getModX() {
      return this.modX;
   }

   private void setModY(double modY) {
      this.modY = modY;
   }

   private double getModY() {
      return this.modY;
   }
}
