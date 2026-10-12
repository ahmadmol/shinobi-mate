package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

/** @deprecated */
@Deprecated
public class MoveWithTurn extends Move {
   public MoveWithTurn(ResourceBundle schema, List animations, VariableMap params) {
      super(schema, animations, params);
      if (animations.size() < 2) {
         throw new IllegalArgumentException("animations.size<2");
      }
   }

   protected Animation getAnimation() throws VariableException {
      if (this.turning) {
         return (Animation)super.getAnimations().getLast();
      } else {
         List<Animation> animations = super.getAnimations();

         for(int index = 0; index < animations.size() - 1; ++index) {
            if (((Animation)animations.get(index)).isEffective(this.getVariables())) {
               return (Animation)animations.get(index);
            }
         }

         return null;
      }
   }

   protected boolean hasTurningAnimation() {
      return true;
   }
}
