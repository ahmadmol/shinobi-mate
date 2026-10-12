package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

public class BreedMove extends Move {
   private final Breed.Delegate delegate = new Breed.Delegate(this);

   public BreedMove(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      this.delegate.validateBornCount();
      this.delegate.validateBornInterval();
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if (this.delegate.isIntervalFrame() && !this.isTurning() && this.delegate.isEnabled()) {
         this.delegate.breed();
      }

   }
}
