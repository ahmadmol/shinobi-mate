package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Regist extends ActionBase {
   public static final String PARAMETER_OFFSETX = "OffsetX";
   private static final int DEFAULT_OFFSETX = 0;
   public static final String PARAMETER_OFFSETY = "OffsetY";
   private static final int DEFAULT_OFFSETY = 0;
   public static final String PARAMETER_OFFSETTYPE = "OffsetType";
   private static final String DEFAULT_OFFSETTYPE = "ImageAnchor";
   private double scaling;
   private static final Logger log = Logger.getLogger(Regist.class.getName());

   public Regist(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      this.scaling = Double.parseDouble(Main.getInstance().getProperties().getProperty("Scaling", "1.0"));
   }

   public boolean hasNext() throws VariableException {
      int offsetX = (int)Math.round((double)this.getOffsetX() * this.scaling);
      if (this.getOffsetType().equals(this.getSchema().getString("Origin"))) {
         offsetX = 0 - offsetX + this.getMascot().getImage().size().width / 2;
      }

      boolean notMoved = Math.abs(this.getEnvironment().getCursor().getX() - offsetX - this.getMascot().getAnchor().x) < 5;
      return super.hasNext() && notMoved;
   }

   protected void tick() throws LostGroundException, VariableException {
      this.getMascot().setDragging(true);
      Animation animation = this.getAnimation();
      animation.next(this.getMascot(), this.getTime());
      if (this.getTime() + 1 >= this.getAnimation().getDuration()) {
         this.getMascot().setLookRight(Math.random() < (double)0.5F);
         log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{this.getMascot(), this});
         throw new LostGroundException();
      }
   }

   protected void refreshHotspots() {
      this.getMascot().getHotspots().clear();
   }

   private int getOffsetX() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("OffsetX"), Number.class, 0)).intValue();
   }

   private int getOffsetY() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("OffsetY"), Number.class, 0)).intValue();
   }

   private String getOffsetType() throws VariableException {
      return (String)this.eval(this.getSchema().getString("OffsetType"), String.class, "ImageAnchor");
   }
}
