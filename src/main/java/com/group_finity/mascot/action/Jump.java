package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.util.List;
import java.util.ResourceBundle;

public class Jump extends ActionBase {
   public static final String PARAMETER_TARGETX = "TargetX";
   private static final int DEFAULT_TARGETX = 0;
   public static final String PARAMETER_TARGETY = "TargetY";
   private static final int DEFAULT_TARGETY = 0;
   public static final String PARAMETER_VELOCITY = "VelocityParam";
   private static final double DEFAULT_VELOCITY = (double)20.0F;
   public static final String VARIABLE_VELOCITYX = "VelocityX";
   public static final String VARIABLE_VELOCITYY = "VelocityY";

   public Jump(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }
   @Override public void init(com.group_finity.mascot.Mascot mascot) throws VariableException {
      super.init(mascot);mascot.playAudio("movement.jump");
   }

   public boolean hasNext() throws VariableException {
      int targetX = this.getTargetX();
      int targetY = this.getTargetY();
      double distanceX = (double)(targetX - this.getMascot().getAnchor().x);
      double distanceY = (double)(targetY - this.getMascot().getAnchor().y) - Math.abs(distanceX) / (double)2.0F;
      double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
      return super.hasNext() && distance != (double)0.0F;
   }

   protected void tick() throws LostGroundException, VariableException {
      int targetX = this.getTargetX();
      int targetY = this.getTargetY();
      this.getMascot().setLookRight(this.getMascot().getAnchor().x < targetX);
      double distanceX = (double)(targetX - this.getMascot().getAnchor().x);
      double distanceY = (double)(targetY - this.getMascot().getAnchor().y) - Math.abs(distanceX) / (double)2.0F;
      double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
      double velocity = this.getVelocity();
      if (distance != (double)0.0F) {
         int velocityX = (int)(velocity * distanceX / distance);
         int velocityY = (int)(velocity * distanceY / distance);
         this.putVariable(this.getSchema().getString("VelocityX"), velocity * distanceX / distance);
         this.putVariable(this.getSchema().getString("VelocityY"), velocity * distanceY / distance);
         this.getMascot().setAnchor(new Point(this.getMascot().getAnchor().x + velocityX, this.getMascot().getAnchor().y + velocityY));
         this.getAnimation().next(this.getMascot(), this.getTime());
      }

      if (distance <= velocity) {
         this.getMascot().setAnchor(new Point(targetX, targetY));
      }

   }

   private double getVelocity() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("VelocityParam"), Number.class, (double)20.0F)).doubleValue();
   }

   private int getTargetX() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("TargetX"), Number.class, 0)).intValue();
   }

   private int getTargetY() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("TargetY"), Number.class, 0)).intValue();
   }
}
