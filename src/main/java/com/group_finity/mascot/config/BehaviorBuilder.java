package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BehaviorBuilder {
   private static final Logger log = Logger.getLogger(BehaviorBuilder.class.getName());
   private final Configuration configuration;
   private final String name;
   private final String actionName;
   private final int frequency;
   private final List<String> conditions;
   private final boolean hidden;
   private final boolean toggleable;
   private final boolean nextAdditive;
   private final List<BehaviorBuilder> nextBehaviorBuilders = new ArrayList<>();
   private final Map<String, String> params = new LinkedHashMap<>();

   public BehaviorBuilder(Configuration configuration, Entry behaviorNode, List<String> conditions) {
      this.configuration = configuration;
      this.name = behaviorNode.getAttribute(configuration.getSchema().getString("Name"));
      this.actionName = behaviorNode.getAttribute(configuration.getSchema().getString("Action")) == null ? this.getName() : behaviorNode.getAttribute(configuration.getSchema().getString("Action"));
      this.frequency = Integer.parseInt(behaviorNode.getAttribute(configuration.getSchema().getString("Frequency")));
      this.hidden = Boolean.parseBoolean(behaviorNode.getAttribute(configuration.getSchema().getString("Hidden")));
      this.conditions = new ArrayList<>(conditions);
      this.getConditions().add(behaviorNode.getAttribute(configuration.getSchema().getString("Condition")));
      if (!this.name.equals("Fall") && !this.name.equals("Thrown") && !this.name.equals("Dragged")) {
         this.toggleable = Boolean.parseBoolean(behaviorNode.getAttribute(configuration.getSchema().getString("Toggleable")));
      } else {
         this.toggleable = false;
      }

      log.log(Level.INFO, "Start Reading({0})", this);
      this.getParams().putAll(behaviorNode.getAttributes());
      this.getParams().remove(configuration.getSchema().getString("Name"));
      this.getParams().remove(configuration.getSchema().getString("Action"));
      this.getParams().remove(configuration.getSchema().getString("Frequency"));
      this.getParams().remove(configuration.getSchema().getString("Hidden"));
      this.getParams().remove(configuration.getSchema().getString("Condition"));
      this.getParams().remove(configuration.getSchema().getString("Toggleable"));
      boolean nextAdditive = true;

      for(Entry nextList : behaviorNode.selectChildren(configuration.getSchema().getString("NextBehaviourList"))) {
         log.log(Level.INFO, "Lists the Following Behaviors...");
         nextAdditive = Boolean.parseBoolean(nextList.getAttribute(configuration.getSchema().getString("Add")));
         this.loadBehaviors(nextList, new ArrayList<>());
      }

      this.nextAdditive = nextAdditive;
      log.log(Level.INFO, "Behaviors have finished loading({0})", this);
   }

   public String toString() {
      String var10000 = this.getName();
      return "Behavior(" + var10000 + "," + this.getFrequency() + "," + this.getActionName() + ")";
   }

   private void loadBehaviors(Entry list, List<String> conditions) {
      for(Entry node : list.getChildren()) {
         if (node.getName().equals(this.configuration.getSchema().getString("Condition"))) {
            List<String> newConditions = new ArrayList<>(conditions);
            newConditions.add(node.getAttribute(this.configuration.getSchema().getString("Condition")));
            this.loadBehaviors(node, newConditions);
         } else if (node.getName().equals(this.configuration.getSchema().getString("BehaviourReference"))) {
            BehaviorBuilder behavior = new BehaviorBuilder(this.getConfiguration(), node, conditions);
            this.getNextBehaviorBuilders().add(behavior);
         }
      }
   }

   public void validate() throws ConfigurationException {
      if (!this.getConfiguration().getActionBuilders().containsKey(this.getActionName())) {
         log.log(Level.SEVERE, "There is no corresponding action(" + String.valueOf(this) + ")");
         String var10002 = Main.getInstance().getLanguageBundle().getString("NoActionFoundErrorMessage");
         throw new ConfigurationException(var10002 + "(" + String.valueOf(this) + ")");
      }
   }

   public Behavior buildBehavior() throws BehaviorInstantiationException {
      try {
         return new UserBehavior(this.getName(), this.getConfiguration().buildAction(this.getActionName(), this.getParams()), this.getConfiguration());
      } catch (ActionInstantiationException e) {
         log.log(Level.SEVERE, "Failed to initialize the corresponding action(" + String.valueOf(this) + ")");
         throw new BehaviorInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseCorrespondingActionErrorMessage") + "(" + String.valueOf(this) + ")", e);
      }
   }

   public boolean isEffective(VariableMap context) throws VariableException {
      if (this.frequency == 0) {
         return false;
      } else {
         for(String condition : this.getConditions()) {
            if (condition != null && !(Boolean)Variable.parse(condition).get(context)) {
               return false;
            }
         }

         return true;
      }
   }

   public String getName() {
      return this.name;
   }

   public int getFrequency() {
      return this.frequency;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public boolean isToggleable() {
      return this.toggleable;
   }

   public String getActionName() {
      return this.actionName;
   }

   public Map<String, String> getParams() {
      return this.params;
   }

   public List<String> getConditions() {
      return this.conditions;
   }

   public Configuration getConfiguration() {
      return this.configuration;
   }

   public boolean isNextAdditive() {
      return this.nextAdditive;
   }

   public List<BehaviorBuilder> getNextBehaviorBuilders() {
      return this.nextBehaviorBuilders;
   }
}
