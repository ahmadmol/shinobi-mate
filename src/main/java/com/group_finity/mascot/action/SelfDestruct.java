package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

public class SelfDestruct extends Animate {
   public SelfDestruct(ResourceBundle schema, List animations, VariableMap params) {
      super(schema, animations, params);
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if (this.getTime() == this.getAnimation().getDuration() - 1) {
         this.getMascot().dispose();
      }

   }
}
