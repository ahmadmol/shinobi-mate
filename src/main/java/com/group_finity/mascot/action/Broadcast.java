package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

public class Broadcast extends Animate {
   private boolean affordanceSet = false;

   public Broadcast(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      String affordance = this.getAffordance();
      if (affordance != null && !affordance.trim().isEmpty() && !this.affordanceSet) {
         this.getMascot().setAffordance(affordance);
         this.affordanceSet = true;
      }

   }

   public boolean hasNext() throws VariableException {
      boolean hasNext = super.hasNext();
      if (!hasNext && this.affordanceSet) {
         String affordance = this.getAffordance();
         this.getMascot().removeAffordance(affordance);
         this.affordanceSet = false;
      }

      return hasNext;
   }
}
