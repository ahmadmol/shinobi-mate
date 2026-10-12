package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScanJump extends ActionBase {
   private static final Logger log = Logger.getLogger(ScanJump.class.getName());
   public static final String PARAMETER_BEHAVIOUR = "Behaviour";
   private static final String DEFAULT_BEHAVIOUR = "";
   public static final String PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour";
   private static final String DEFAULT_TARGETBEHAVIOUR = "";
   public static final String PARAMETER_TARGETLOOK = "TargetLook";
   private static final boolean DEFAULT_TARGETLOOK = false;
   public static final String PARAMETER_VELOCITY = "VelocityParam";
   private static final double DEFAULT_VELOCITY = (double)20.0F;
   public static final String VARIABLE_VELOCITYX = "VelocityX";
   public static final String VARIABLE_VELOCITYY = "VelocityY";
   private WeakReference target;

   public ScanJump(ResourceBundle schema, List animations, VariableMap params) {
      super(schema, animations, params);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      this.getMascot().getAffordances().clear();
      if (this.getMascot().getManager() != null) {
         this.target = this.getMascot().getManager().getMascotWithAffordance(this.getAffordance());
      }

      this.putVariable(this.getSchema().getString("TargetX"), this.target != null && this.target.get() != null ? ((Mascot)this.target.get()).getAnchor().x : null);
      this.putVariable(this.getSchema().getString("TargetY"), this.target != null && this.target.get() != null ? ((Mascot)this.target.get()).getAnchor().y : null);
   }

   public boolean hasNext() throws VariableException {
      if (this.getMascot().getManager() == null) {
         return super.hasNext();
      } else {
         return super.hasNext() && this.target != null && this.target.get() != null && ((Mascot)this.target.get()).getAffordances().contains(this.getAffordance());
      }
   }

   protected void tick() throws VariableException {
      this.getMascot().getAffordances().clear();
      int targetX = ((Mascot)this.target.get()).getAnchor().x;
      int targetY = ((Mascot)this.target.get()).getAnchor().y;
      this.putVariable(this.getSchema().getString("TargetX"), targetX);
      this.putVariable(this.getSchema().getString("TargetY"), targetY);
      if (this.getMascot().getAnchor().x != targetX) {
         this.getMascot().setLookRight(this.getMascot().getAnchor().x < targetX);
      }

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

         try {
            this.getMascot().setBehavior(Main.getInstance().getConfiguration(this.getMascot().getImageSet()).buildBehavior(this.getBehavior(), this.getMascot()));
            ((Mascot)this.target.get()).setBehavior(Main.getInstance().getConfiguration(((Mascot)this.target.get()).getImageSet()).buildBehavior(this.getTargetBehavior(), (Mascot)this.target.get()));
            if (this.getTargetLook() && ((Mascot)this.target.get()).isLookRight() == this.getMascot().isLookRight()) {
               ((Mascot)this.target.get()).setLookRight(!this.getMascot().isLookRight());
            }
         } catch (CantBeAliveException | BehaviorInstantiationException | NullPointerException e) {
            log.log(Level.SEVERE, "Fatal Exception", e);
            String var10000 = Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage");
            Main.showError(var10000 + "\n" + ((Exception)e).getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
         }
      }

   }

   private String getBehavior() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Behaviour"), String.class, "");
   }

   private String getTargetBehavior() throws VariableException {
      return (String)this.eval(this.getSchema().getString("TargetBehaviour"), String.class, "");
   }

   private boolean getTargetLook() throws VariableException {
      return (Boolean)this.eval(this.getSchema().getString("TargetLook"), Boolean.class, false);
   }

   private double getVelocity() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("VelocityParam"), Number.class, (double)20.0F)).doubleValue();
   }
}
