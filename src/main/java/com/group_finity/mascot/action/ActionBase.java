package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;

public abstract class ActionBase implements Action {
   public static final String PARAMETER_DURATION = "Duration";
   private static final boolean DEFAULT_CONDITION = true;
   public static final String PARAMETER_CONDITION = "Condition";
   private static final int DEFAULT_DURATION = Integer.MAX_VALUE;
   public static final String PARAMETER_DRAGGABLE = "Draggable";
   private static final boolean DEFAULT_DRAGGABLE = true;
   public static final String PARAMETER_AFFORDANCE = "Affordance";
   private static final String DEFAULT_AFFORDANCE = "";
   private Mascot mascot;
   private int startTime;
   private final List<Animation> animations;
   private final VariableMap variables;
   private final ResourceBundle schema;

   public ActionBase(ResourceBundle schema, List<Animation> animations, VariableMap context) {
      this.schema = schema;
      this.animations = animations;
      this.variables = context;
   }

   public String toString() {
      try {
         String var10000 = this.getClass().getSimpleName();
         return "Action (" + var10000 + "," + this.getName() + ")";
      } catch (VariableException var2) {
         return "Action (" + this.getClass().getSimpleName() + ",null)";
      }
   }

   public void init(Mascot mascot) throws VariableException {
      this.setMascot(mascot);
      this.setTime(0);
      this.getVariables().put("mascot", mascot);
      this.getVariables().put("action", this);
      this.getVariables().init();

      for(Animation animation : this.animations) {
         animation.init();
      }
   }

   public void next() throws LostGroundException, VariableException {
      this.initFrame();
      if (!this.getMascot().getAffordances().isEmpty()) {
         this.getMascot().getAffordances().clear();
      }

      if (!this.getAffordance().trim().isEmpty()) {
         this.getMascot().getAffordances().add(this.getAffordance());
      }

      this.refreshHotspots();
      this.tick();
   }

   private void initFrame() {
      this.getVariables().initFrame();

      for(Animation animation : this.getAnimations()) {
         animation.initFrame();
      }
   }

   protected List<Animation> getAnimations() {
      return this.animations;
   }

   protected Animation getAnimation() throws VariableException {
      for (Animation animation : this.getAnimations()) {
         if (animation.isEffective(this.getVariables())) {
            return animation;
         }
      }
      return null;
   }

   protected abstract void tick() throws LostGroundException, VariableException;

   public boolean hasNext() throws VariableException {
      boolean effective = this.isEffective();
      boolean intime = this.getTime() < this.getDuration();
      return effective && intime;
   }

   public Boolean isEffective() throws VariableException {
      return (Boolean)this.eval(this.getSchema().getString("Condition"), Boolean.class, true);
   }

   protected Object eval(String name, Class<?> type, Object defaultValue) throws VariableException {
      Object obj = this.getVariables().get(name);
      if (obj instanceof Variable variable) {
         return variable.get(this.getVariables());
      } else if (obj != null) {
         if (type.isInstance(obj)) {
            return obj;
         } else if (obj instanceof String str) {
            return Variable.parse(str).get(this.getVariables());
         }
      }
      return defaultValue;
   }

   public Mascot getMascot() {
      return this.mascot;
   }

   private void setMascot(Mascot mascot) {
      this.mascot = mascot;
   }

   protected MascotEnvironment getEnvironment() {
      return this.getMascot().getEnvironment();
   }

   protected int getTime() {
      return this.getMascot().getTime() - this.startTime;
   }

   protected void setTime(int time) {
      this.startTime = this.getMascot().getTime() - time;
   }

   public VariableMap getVariables() {
      return this.variables;
   }

   protected void putVariable(String key, Object value) {
      if (value != null) {
         this.getVariables().put(key, value);
      } else {
         this.getVariables().remove(key);
      }
   }

   protected int getDuration() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("Duration"), Number.class, Integer.MAX_VALUE)).intValue();
   }

   public boolean isDraggable() throws VariableException {
      return (Boolean)this.eval(this.getSchema().getString("Draggable"), Boolean.class, true);
   }

   protected String getAffordance() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Affordance"), String.class, "");
   }

   public String getName() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Name"), String.class, this.getClass().getSimpleName());
   }

   protected ResourceBundle getSchema() {
      return this.schema;
   }

   protected void refreshHotspots() {
      this.getMascot().getHotspots().clear();

      for(Animation animation : this.getAnimations()) {
         for(Hotspot hotspot : animation.getHotspots()) {
            this.getMascot().getHotspots().add(hotspot);
         }
      }
   }
}
