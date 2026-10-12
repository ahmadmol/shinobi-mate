package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.Animate;
import com.group_finity.mascot.action.Move;
import com.group_finity.mascot.action.Select;
import com.group_finity.mascot.action.Sequence;
import com.group_finity.mascot.action.Stay;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionBuilder implements IActionBuilder {
   private static final Logger log = Logger.getLogger(ActionBuilder.class.getName());
   private final String type;
   private final String name;
   private final String className;
   private final Map<String, String> params = new LinkedHashMap<>();
   private final List<AnimationBuilder> animationBuilders = new ArrayList<>();
   private final List<IActionBuilder> actionRefs = new ArrayList<>();
   private final ResourceBundle schema;

   public ActionBuilder(Configuration configuration, Entry actionNode, String imageSet) throws IOException {
      this.schema = configuration.getSchema();
      this.name = actionNode.getAttribute(this.schema.getString("Name"));
      this.type = actionNode.getAttribute(this.schema.getString("Type"));
      this.className = actionNode.getAttribute(this.schema.getString("Class"));
      log.log(Level.INFO, "Read Start Operation({0})", this);
      this.getParams().putAll(actionNode.getAttributes());

      for(Entry node : actionNode.selectChildren(this.schema.getString("Animation"))) {
         this.getAnimationBuilders().add(new AnimationBuilder(this.schema, node, imageSet));
      }

      for(Entry node : actionNode.getChildren()) {
         if (node.getName().equals(this.schema.getString("ActionReference"))) {
            this.getActionRefs().add(new ActionRef(configuration, node));
         } else if (node.getName().equals(this.schema.getString("Action"))) {
            this.getActionRefs().add(new ActionBuilder(configuration, node, imageSet));
         }
      }

      log.log(Level.INFO, "Actions Finished Loading");
   }

   public String toString() {
      String var10000 = this.getName();
      return "Action(" + var10000 + "," + this.getType() + "," + this.getClassName() + ")";
   }

   public Action buildAction(Map<String, String> params) throws ActionInstantiationException {
      try {
         VariableMap variables = this.createVariables(params);
         List<Animation> animations = this.createAnimations();
         List<Action> actions = this.createActions();
         if (this.type.equals(this.schema.getString("Embedded"))) {
            try {
               if(!this.getClassName().matches("com\\.group_finity\\.mascot\\.action\\.[A-Za-z]+"))
                  throw new ClassNotFoundException("Only built-in action classes are supported");
               Class<? extends Action> cls = Class.forName(this.getClassName()).asSubclass(Action.class);

               try {
                  try {
                     return cls.getConstructor(ResourceBundle.class, List.class, VariableMap.class).newInstance(this.schema, animations, variables);
                  } catch (Exception var7) {
                     return cls.getConstructor(ResourceBundle.class, VariableMap.class).newInstance(this.schema, variables);
                  }
               } catch (Exception var8) {
                  return cls.getDeclaredConstructor().newInstance();
               }
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException e) {
               throw new ActionInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedClassActionInitialiseErrorMessage") + "(" + String.valueOf(this) + ")", e);
            } catch (IllegalAccessException e) {
               throw new ActionInstantiationException(Main.getInstance().getLanguageBundle().getString("CannotAccessClassActionErrorMessage") + "(" + String.valueOf(this) + ")", e);
            } catch (ClassNotFoundException e) {
               throw new ActionInstantiationException(Main.getInstance().getLanguageBundle().getString("ClassNotFoundErrorMessage") + "(" + String.valueOf(this) + ")", e);
            }
         } else if (this.type.equals(this.schema.getString("Move"))) {
            return new Move(this.schema, animations, variables);
         } else if (this.type.equals(this.schema.getString("Stay"))) {
            return new Stay(this.schema, animations, variables);
         } else if (this.type.equals(this.schema.getString("Animate"))) {
            return new Animate(this.schema, animations, variables);
         } else if (this.type.equals(this.schema.getString("Sequence"))) {
            return new Sequence(this.schema, variables, actions.toArray(new Action[0]));
         } else if (this.type.equals(this.schema.getString("Select"))) {
            return new Select(this.schema, variables, actions.toArray(new Action[0]));
         } else {
            String var10002 = Main.getInstance().getLanguageBundle().getString("UnknownActionTypeErrorMessage");
            throw new ActionInstantiationException(var10002 + "(" + String.valueOf(this) + ")");
         }
      } catch (AnimationInstantiationException e) {
         throw new ActionInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedCreateAnimationErrorMessage") + "(" + String.valueOf(this) + ")", e);
      } catch (VariableException e) {
         throw new ActionInstantiationException(Main.getInstance().getLanguageBundle().getString("FailedParameterEvaluationErrorMessage") + "(" + String.valueOf(this) + ")", e);
      }
   }

   public void validate() throws ConfigurationException {
      for(IActionBuilder ref : this.getActionRefs()) {
         ref.validate();
      }
   }

   private List<Action> createActions() throws ActionInstantiationException {
      List<Action> actions = new ArrayList<>();

      for(IActionBuilder ref : this.getActionRefs()) {
         actions.add(ref.buildAction(new HashMap<>()));
      }

      return actions;
   }

   private List<Animation> createAnimations() throws AnimationInstantiationException {
      List<Animation> animations = new ArrayList<>();

      for(AnimationBuilder animationFactory : this.getAnimationBuilders()) {
         animations.add(animationFactory.buildAnimation());
      }

      return animations;
   }

   private VariableMap createVariables(Map<String, String> params) throws VariableException {
      VariableMap variables = new VariableMap();

      for(Map.Entry<String, String> param : this.getParams().entrySet()) {
         variables.put(param.getKey(), Variable.parse(param.getValue()));
      }

      if (params != null) {
         for(Map.Entry<String, String> param : params.entrySet()) {
            variables.put(param.getKey(), Variable.parse(param.getValue()));
         }
      }

      return variables;
   }

   public String getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public String getClassName() {
      return this.className;
   }

   public Map<String, String> getParams() {
      return this.params;
   }

   public List<AnimationBuilder> getAnimationBuilders() {
      return this.animationBuilders;
   }

   public List<IActionBuilder> getActionRefs() {
      return this.actionRefs;
   }
}
