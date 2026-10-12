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

public class Interact extends Animate {
   private static final Logger log = Logger.getLogger(Interact.class.getName());
   public static final String PARAMETER_BEHAVIOUR = "Behaviour";
   private static final String DEFAULT_BEHAVIOUR = "";

   public Interact(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public boolean hasNext() throws VariableException {
      return super.hasNext() && this.getMascot().getManager().hasOverlappingMascotsAtPoint(this.getMascot().getAnchor());
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if ((this.getTime() == this.getAnimation().getDuration() - 1 || this.getAnimation().getDuration() == 1) && !this.getBehavior().trim().isEmpty()) {
         try {
            this.getMascot().setBehavior(Main.getInstance().getConfiguration(this.getMascot().getImageSet()).buildBehavior(this.getBehavior(), this.getMascot()));
         } catch (CantBeAliveException | BehaviorInstantiationException e) {
            log.log(Level.SEVERE, "Fatal Exception", e);
            String var10000 = Main.getInstance().getLanguageBundle().getString("FailedCreateNewShimejiErrorMessage");
            Main.showError(var10000 + "\n" + ((Exception)e).getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
         }
      }

   }

   private String getBehavior() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Behaviour"), String.class, "");
   }
}
