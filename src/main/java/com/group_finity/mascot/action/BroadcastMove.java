package com.group_finity.mascot.action;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

/** @deprecated */
@Deprecated
public class BroadcastMove extends Move {
   public BroadcastMove(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }
}
