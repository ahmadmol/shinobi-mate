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

public class ComplexMove extends BorderedAction {
   private static final Logger log = Logger.getLogger(ComplexMove.class.getName());
   private final Breed.Delegate delegate = new Breed.Delegate(this);
   public static final String PARAMETER_CHARACTERISTICS = "Characteristics";
   private static final String DEFAULT_CHARACTERISTICS = "";
   public static final String PARAMETER_BEHAVIOUR = "Behaviour";
   private static final String DEFAULT_BEHAVIOUR = "";
   public static final String PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour";
   private static final String DEFAULT_TARGETBEHAVIOUR = "";
   public static final String PARAMETER_TARGETLOOK = "TargetLook";
   private static final boolean DEFAULT_TARGETLOOK = false;
   public static final String PARAMETER_TARGETX = "TargetX";
   private static final int DEFAULT_TARGETX = Integer.MAX_VALUE;
   public static final String PARAMETER_TARGETY = "TargetY";
   private static final int DEFAULT_TARGETY = Integer.MAX_VALUE;
   private WeakReference target;
   private boolean turning = false;
   private Boolean hasTurning = null;
   private boolean breedEnabled = false;
   private boolean scanEnabled = false;

   public ComplexMove(ResourceBundle schema, List<Animation> animations, VariableMap params) {
      super(schema, animations, params);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);

      for(String characteristic : this.getCharacteristics().split(",")) {
         if (characteristic.equals(this.getSchema().getString("Breed"))) {
            this.breedEnabled = true;
         }

         if (characteristic.equals(this.getSchema().getString("Scan"))) {
            this.scanEnabled = true;
         }
      }

      if (this.breedEnabled) {
         this.delegate.validateBornCount();
         this.delegate.validateBornInterval();
      }

      if (this.scanEnabled) {
         this.getMascot().getAffordances().clear();
         if (this.getMascot().getManager() != null) {
            this.target = this.getMascot().getManager().getMascotWithAffordance(this.getAffordance());
         }

         this.putVariable(this.getSchema().getString("TargetX"), this.target != null && this.target.get() != null ? ((Mascot)this.target.get()).getAnchor().x : null);
         this.putVariable(this.getSchema().getString("TargetY"), this.target != null && this.target.get() != null ? ((Mascot)this.target.get()).getAnchor().y : null);
      }

   }

   public boolean hasNext() throws VariableException {
      if (this.scanEnabled) {
         if (this.getMascot().getManager() == null) {
            return super.hasNext();
         } else {
            return super.hasNext() && (this.turning || this.target != null && this.target.get() != null && ((Mascot)this.target.get()).getAffordances().contains(this.getAffordance()));
         }
      } else {
         int targetX = this.getTargetX();
         int targetY = this.getTargetY();
         boolean hasNotReached = targetX != Integer.MIN_VALUE && this.getMascot().getAnchor().x == targetX || targetY != Integer.MIN_VALUE && this.getMascot().getAnchor().y == targetY;
         return super.hasNext() && (!hasNotReached || this.turning);
      }
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if (this.scanEnabled) {
         this.getMascot().getAffordances().clear();
      }

      if (this.getBorder() != null && !this.getBorder().isOn(this.getMascot().getAnchor())) {
         log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{this.getMascot(), this});
         throw new LostGroundException();
      } else {
         int targetX = this.scanEnabled ? ((Mascot)this.target.get()).getAnchor().x : this.getTargetX();
         int targetY = this.scanEnabled ? ((Mascot)this.target.get()).getAnchor().y : this.getTargetY();
         if (this.scanEnabled) {
            this.putVariable(this.getSchema().getString("TargetX"), targetX);
            this.putVariable(this.getSchema().getString("TargetY"), targetY);
         }

         if (this.getMascot().getAnchor().x != targetX) {
            this.turning = this.hasTurningAnimation() && (this.turning || this.getMascot().getAnchor().x < targetX != this.getMascot().isLookRight());
            this.getMascot().setLookRight(this.getMascot().getAnchor().x < targetX);
         }

         boolean down = this.getMascot().getAnchor().y < targetY;
         if (this.turning && this.getTime() >= this.getAnimation().getDuration()) {
            this.turning = false;
         }

         this.getAnimation().next(this.getMascot(), this.getTime());
         if ((targetX != Integer.MAX_VALUE || this.scanEnabled) && (this.getMascot().isLookRight() && this.getMascot().getAnchor().x >= targetX || !this.getMascot().isLookRight() && this.getMascot().getAnchor().x <= targetX)) {
            this.getMascot().setAnchor(new Point(targetX, this.getMascot().getAnchor().y));
         }

         if ((targetY != Integer.MAX_VALUE || this.scanEnabled) && (down && this.getMascot().getAnchor().y >= targetY || !down && this.getMascot().getAnchor().y <= targetY)) {
            this.getMascot().setAnchor(new Point(this.getMascot().getAnchor().x, targetY));
         }

         if (this.breedEnabled && this.delegate.isIntervalFrame() && !this.isTurning() && this.delegate.isEnabled()) {
            this.delegate.breed();
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

   private String getCharacteristics() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Characteristics"), String.class, "");
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

   private int getTargetX() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("TargetX"), Number.class, Integer.MAX_VALUE)).intValue();
   }

   private int getTargetY() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("TargetY"), Number.class, Integer.MAX_VALUE)).intValue();
   }
}
