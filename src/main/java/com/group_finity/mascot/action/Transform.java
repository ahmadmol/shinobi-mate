package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transform extends Animate {
   private static final Logger log = Logger.getLogger(Transform.class.getName());
   public static final String PARAMETER_TRANSFORMBEHAVIOUR = "TransformBehaviour";
   private static final String DEFAULT_TRANSFORMBEHAVIOUR = "";
   public static final String PARAMETER_TRANSFORMMASCOT = "TransformMascot";
   private static final String DEFAULT_TRANSFORMMASCOT = "";

   public Transform(ResourceBundle schema, List animations, VariableMap params) {
      super(schema, animations, params);
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if ((this.getTime() == this.getAnimation().getDuration() - 1 || this.getAnimation().getDuration() == 1) && Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Transformation", "true"))) {
         this.transform();
      }

   }

   private void transform() throws VariableException {
      String childType = Main.getInstance().getConfiguration(this.getTransformMascot()) != null ? this.getTransformMascot() : this.getMascot().getImageSet();

      try {
         this.getMascot().setImageSet(childType);
         this.getMascot().setBehavior(Main.getInstance().getConfiguration(childType).buildBehavior(this.getTransformBehavior(), this.getMascot()));
      } catch (CantBeAliveException | BehaviorInstantiationException e) {
         log.log(Level.SEVERE, "Fatal Exception", e);
         String var10000 = Main.getInstance().getLanguageBundle().getString("FailedCreateNewShimejiErrorMessage");
         Main.showError(var10000 + "\n" + ((Exception)e).getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
      }

   }

   private String getTransformBehavior() throws VariableException {
      return (String)this.eval(this.getSchema().getString("TransformBehaviour"), String.class, "");
   }

   private String getTransformMascot() throws VariableException {
      return (String)this.eval(this.getSchema().getString("TransformMascot"), String.class, "");
   }
}
