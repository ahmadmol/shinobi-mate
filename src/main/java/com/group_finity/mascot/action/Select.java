package com.group_finity.mascot.action;

import com.group_finity.mascot.script.VariableMap;
import java.util.ResourceBundle;

public class Select extends ComplexAction {
   public Select(ResourceBundle schema, VariableMap params, Action... actions) {
      super(schema, params, actions);
   }
}
