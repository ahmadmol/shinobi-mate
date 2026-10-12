package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScanMove extends BorderedAction {
   private static final Logger log = Logger.getLogger(ScanMove.class.getName());
   public static final String PARAMETER_BEHAVIOUR = "Behaviour";
   private static final String DEFAULT_BEHAVIOUR = "";
   public static final String PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour";
   private static final String DEFAULT_TARGETBEHAVIOUR = "";
   public static final String PARAMETER_TARGETLOOK = "TargetLook";
   private static final boolean DEFAULT_TARGETLOOK = false;
   private WeakReference<Mascot> target;
   private boolean turning = false;
   private Boolean hasTurning = null;

   public ScanMove(ResourceBundle schema, List<Animation> animations, VariableMap params) {
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
         return super.hasNext() && (this.turning || this.target != null && this.target.get() != null && ((Mascot)this.target.get()).getAffordances().contains(this.getAffordance()));
      }
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      this.getMascot().getAffordances().clear();
      if (this.getBorder() != null && !this.getBorder().isOn(this.getMascot().getAnchor())) {
         log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{this.getMascot(), this});
         throw new LostGroundException();
      } else {
         int targetX = ((Mascot)this.target.get()).getAnchor().x;
         int targetY = ((Mascot)this.target.get()).getAnchor().y;
         this.putVariable(this.getSchema().getString("TargetX"), targetX);
         this.putVariable(this.getSchema().getString("TargetY"), targetY);
         if (this.getMascot().getAnchor().x != targetX) {
            this.turning = this.hasTurningAnimation() && (this.turning || this.getMascot().getAnchor().x < targetX != this.getMascot().isLookRight());
            this.getMascot().setLookRight(this.getMascot().getAnchor().x < targetX);
         }

         boolean down = this.getMascot().getAnchor().y < targetY;
         if (this.turning && this.getTime() >= this.getAnimation().getDuration()) {
            this.turning = false;
         }

         this.getAnimation().next(this.getMascot(), this.getTime());
         if (this.getMascot().isLookRight() && this.getMascot().getAnchor().x >= targetX || !this.getMascot().isLookRight() && this.getMascot().getAnchor().x <= targetX) {
            this.getMascot().setAnchor(new Point(targetX, this.getMascot().getAnchor().y));
         }

         if (down && this.getMascot().getAnchor().y >= targetY || !down && this.getMascot().getAnchor().y <= targetY) {
            this.getMascot().setAnchor(new Point(this.getMascot().getAnchor().x, targetY));
         }

         if (!this.turning && this.getMascot().getAnchor().x == targetX && this.getMascot().getAnchor().y == targetY) {
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
   }

   protected Animation getAnimation() throws VariableException {
      for(Animation animation : super.getAnimations()) {
         if (animation.isEffective(this.getVariables()) && this.turning == animation.isTurn()) {
            return animation;
         }
      }

      return null;
   }

   protected boolean hasTurningAnimation() {
      if (this.hasTurning == null) {
         this.hasTurning = false;
         List<Animation> animations = super.getAnimations();

         for(int index = 0; index < animations.size(); ++index) {
            if (((Animation)animations.get(index)).isTurn()) {
               this.hasTurning = true;
               index = animations.size();
            }
         }
      }

      return this.hasTurning;
   }

   protected boolean isTurning() {
      return this.turning;
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
}
