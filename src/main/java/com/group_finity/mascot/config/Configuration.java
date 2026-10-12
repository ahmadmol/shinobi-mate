package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.joconner.i18n.Utf8ResourceBundleControl;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {
   private static final Logger log = Logger.getLogger(Configuration.class.getName());
   private final Map<String, String> constants = new LinkedHashMap<>(2);
   private final Map<String, ActionBuilder> actionBuilders = new LinkedHashMap<>();
   private final Map<String, BehaviorBuilder> behaviorBuilders = new LinkedHashMap<>();
   private final Map<String, String> information = new LinkedHashMap<>(8);
   private ResourceBundle schema;

   public void load(Entry configurationNode, String imageSet) throws IOException, ConfigurationException {
      log.log(Level.INFO, "Start Reading Configuration File...");
      ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl(false);
      Locale locale;
      if (!configurationNode.hasChild("動作リスト") && !configurationNode.hasChild("行動リスト")) {
         log.log(Level.INFO, "Using en-US schema");
         locale = Locale.forLanguageTag("en-US");
      } else {
         log.log(Level.INFO, "Using ja-JP schema");
         locale = Locale.forLanguageTag("ja-JP");
      }

      this.schema = ResourceBundle.getBundle("schema", locale, utf8Control);

      for(Entry constant : configurationNode.selectChildren(this.schema.getString("Constant"))) {
         this.getConstants().put(constant.getAttribute(this.schema.getString("Name")), constant.getAttribute(this.schema.getString("Value")));
      }

      for(Entry list : configurationNode.selectChildren(this.schema.getString("ActionList"))) {
         log.log(Level.INFO, "Action List...");

         for(Entry node : list.selectChildren(this.schema.getString("Action"))) {
            ActionBuilder action = new ActionBuilder(this, node, imageSet);
            if (this.getActionBuilders().containsKey(action.getName())) {
               String var10002 = Main.getInstance().getLanguageBundle().getString("DuplicateActionErrorMessage");
               throw new ConfigurationException(var10002 + ": " + action.getName());
            }

            this.getActionBuilders().put(action.getName(), action);
         }
      }

      for(Entry list : configurationNode.selectChildren(this.schema.getString("BehaviourList"))) {
         log.log(Level.INFO, "Behavior List...");
         this.loadBehaviors(list, new ArrayList<>());
      }

      for(Entry list : configurationNode.selectChildren(this.schema.getString("Information"))) {
         log.log(Level.INFO, "Information List...");
         this.loadInformation(list);
      }

      log.log(Level.INFO, "Configuration loaded successfully");
   }

   private void loadBehaviors(Entry list, List<String> conditions) {
      for(Entry node : list.getChildren()) {
         if (node.getName().equals(this.schema.getString("Condition"))) {
            List<String> newConditions = new ArrayList<>(conditions);
            newConditions.add(node.getAttribute(this.schema.getString("Condition")));
            this.loadBehaviors(node, newConditions);
         } else if (node.getName().equals(this.schema.getString("Behaviour"))) {
            BehaviorBuilder behavior = new BehaviorBuilder(this, node, conditions);
            this.getBehaviorBuilders().put(behavior.getName(), behavior);
         }
      }
   }

   public Action buildAction(String name, Map<String, String> params) throws ActionInstantiationException {
      ActionBuilder factory = this.actionBuilders.get(name);
      if (factory == null) {
         String var10002 = Main.getInstance().getLanguageBundle().getString("NoCorrespondingActionFoundErrorMessage");
         throw new ActionInstantiationException(var10002 + ": " + name);
      } else {
         return factory.buildAction(params);
      }
   }

   private void loadInformation(Entry list) {
      for(Entry node : list.getChildren()) {
         if (!node.getName().equals(this.schema.getString("Name")) && !node.getName().equals(this.schema.getString("PreviewImage")) && !node.getName().equals(this.schema.getString("SplashImage"))) {
            if (node.getName().equals(this.schema.getString("Artist")) || node.getName().equals(this.schema.getString("Scripter")) || node.getName().equals(this.schema.getString("Commissioner")) || node.getName().equals(this.schema.getString("Support"))) {
               String nameText = node.getAttribute(this.schema.getString("Name")) != null ? node.getAttribute(this.schema.getString("Name")) : null;
               String linkText = node.getAttribute(this.schema.getString("URL")) != null ? node.getAttribute(this.schema.getString("URL")) : null;
               if (nameText != null) {
                  this.information.put(node.getName() + this.schema.getString("Name"), nameText);
                  if (linkText != null) {
                     this.information.put(node.getName() + this.schema.getString("URL"), linkText);
                  }
               }
            }
         } else {
            this.information.put(node.getName(), node.getText());
         }
      }
   }

   public void validate() throws ConfigurationException {
      for(ActionBuilder builder : this.getActionBuilders().values()) {
         builder.validate();
      }

      for(BehaviorBuilder builder : this.getBehaviorBuilders().values()) {
         builder.validate();
      }
   }

   public Behavior buildNextBehavior(String previousName, Mascot mascot) throws BehaviorInstantiationException {
      VariableMap context = new VariableMap();
      context.putAll(this.getConstants());
      context.put("mascot", mascot);
      List<BehaviorBuilder> candidates = new ArrayList<>();
      long totalFrequency = 0L;

      for(BehaviorBuilder behaviorFactory : this.getBehaviorBuilders().values()) {
         try {
            if (behaviorFactory.isEffective(context) && this.isBehaviorEnabled(behaviorFactory, mascot)) {
               candidates.add(behaviorFactory);
               totalFrequency += (long)behaviorFactory.getFrequency();
            }
         } catch (VariableException e) {
            log.log(Level.WARNING, "An error occurred calculating the frequency of the action", e);
         }
      }

      if (previousName != null) {
         BehaviorBuilder previousBehaviorFactory = this.getBehaviorBuilders().get(previousName);
         if (!previousBehaviorFactory.isNextAdditive()) {
            totalFrequency = 0L;
            candidates.clear();
         }

         for(BehaviorBuilder behaviorFactory : previousBehaviorFactory.getNextBehaviorBuilders()) {
            try {
               if (behaviorFactory.isEffective(context) && this.isBehaviorEnabled(behaviorFactory, mascot)) {
                  candidates.add(behaviorFactory);
                  totalFrequency += (long)behaviorFactory.getFrequency();
               }
            } catch (VariableException e) {
               log.log(Level.WARNING, "An error occurred calculating the frequency of the behavior", e);
            }
         }
      }

      if (totalFrequency == 0L) {
         if (Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen", "true"))) {
            mascot.setAnchor(new Point((int)(Math.random() * (double)(mascot.getEnvironment().getScreen().getRight() - mascot.getEnvironment().getScreen().getLeft())) + mascot.getEnvironment().getScreen().getLeft(), mascot.getEnvironment().getScreen().getTop() - 256));
         } else {
            mascot.setAnchor(new Point((int)(Math.random() * (double)(mascot.getEnvironment().getWorkArea().getRight() - mascot.getEnvironment().getWorkArea().getLeft())) + mascot.getEnvironment().getWorkArea().getLeft(), mascot.getEnvironment().getWorkArea().getTop() - 256));
         }

         return this.buildBehavior(this.schema.getString("Fall"));
      } else {
         double random = Math.random() * (double)totalFrequency;

         for(BehaviorBuilder behaviorFactory : candidates) {
            random -= (double)behaviorFactory.getFrequency();
            if (random < (double)0.0F) {
               return behaviorFactory.buildBehavior();
            }
         }

         return null;
      }
   }

   public Behavior buildBehavior(String name, Mascot mascot) throws BehaviorInstantiationException {
      if (this.behaviorBuilders.containsKey(name)) {
         if (this.isBehaviorEnabled(name, mascot)) {
            return this.getBehaviorBuilders().get(name).buildBehavior();
         } else {
            if (Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen", "true"))) {
               mascot.setAnchor(new Point((int)(Math.random() * (double)(mascot.getEnvironment().getScreen().getRight() - mascot.getEnvironment().getScreen().getLeft())) + mascot.getEnvironment().getScreen().getLeft(), mascot.getEnvironment().getScreen().getTop() - 256));
            } else {
               mascot.setAnchor(new Point((int)(Math.random() * (double)(mascot.getEnvironment().getWorkArea().getRight() - mascot.getEnvironment().getWorkArea().getLeft())) + mascot.getEnvironment().getWorkArea().getLeft(), mascot.getEnvironment().getWorkArea().getTop() - 256));
            }

            return this.buildBehavior(this.schema.getString("Fall"));
         }
      } else {
         String var10002 = Main.getInstance().getLanguageBundle().getString("NoBehaviourFoundErrorMessage");
         throw new BehaviorInstantiationException(var10002 + " (" + name + ")");
      }
   }

   public Behavior buildBehavior(String name) throws BehaviorInstantiationException {
      if (this.behaviorBuilders.containsKey(name)) {
         return this.getBehaviorBuilders().get(name).buildBehavior();
      } else {
         String var10002 = Main.getInstance().getLanguageBundle().getString("NoBehaviourFoundErrorMessage");
         throw new BehaviorInstantiationException(var10002 + " (" + name + ")");
      }
   }

   public boolean isBehaviorEnabled(BehaviorBuilder builder, Mascot mascot) {
      if (builder.isToggleable()) {
         for(String behaviour : Main.getInstance().getProperties().getProperty("DisabledBehaviours." + mascot.getImageSet(), "").split("/")) {
            if (behaviour.equals(builder.getName())) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean isBehaviorEnabled(String name, Mascot mascot) {
      return this.behaviorBuilders.containsKey(name) ? this.isBehaviorEnabled(this.getBehaviorBuilders().get(name), mascot) : false;
   }

   public boolean isBehaviorHidden(String name) {
      return this.behaviorBuilders.containsKey(name) ? this.getBehaviorBuilders().get(name).isHidden() : false;
   }

   public boolean isBehaviorToggleable(String name) {
      return this.behaviorBuilders.containsKey(name) ? this.getBehaviorBuilders().get(name).isToggleable() : false;
   }

   Map<String, String> getConstants() {
      return this.constants;
   }

   Map<String, ActionBuilder> getActionBuilders() {
      return this.actionBuilders;
   }

   Map<String, BehaviorBuilder> getBehaviorBuilders() {
      return this.behaviorBuilders;
   }

   public Set<String> getBehaviorNames() {
      return this.behaviorBuilders.keySet();
   }

   public boolean containsInformationKey(String key) {
      return this.information.containsKey(key);
   }

   public String getInformation(String key) {
      return this.information.get(key);
   }

   public ResourceBundle getSchema() {
      return this.schema;
   }
}
