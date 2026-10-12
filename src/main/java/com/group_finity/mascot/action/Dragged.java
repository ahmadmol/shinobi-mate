package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Location;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.util.List;
import java.util.ResourceBundle;

public class Dragged extends ActionBase {
   private static final String VARIABLE_FOOTX = "FootX";
   private static final String VARIABLE_FOOTDX = "FootDX";
   public static final String PARAMETER_OFFSETX = "OffsetX";
   private static final int DEFAULT_OFFSETX = 0;
   public static final String PARAMETER_OFFSETY = "OffsetY";
   private static final int DEFAULT_OFFSETY = 120;
   public static final String PARAMETER_OFFSETTYPE = "OffsetType";
   private static final String DEFAULT_OFFSETTYPE = "ImageAnchor";
   private double footX;
   private double footDx;
   private int timeToRegist;
   private double scaling;

   public Dragged(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      this.scaling = Double.parseDouble(Main.getInstance().getProperties().getProperty("Scaling", "1.0"));
      this.setFootX((double)(this.getEnvironment().getCursor().getX() + (int)Math.round((double)this.getOffsetX() * this.scaling)));
      this.setTimeToRegist(250);
   }

   public boolean hasNext() throws VariableException {
      return super.hasNext() && this.getTime() < this.getTimeToRegist();
   }

   protected void tick() throws VariableException {
      this.getMascot().setLookRight(false);
      this.getMascot().setDragging(true);
      this.getEnvironment().refreshWorkArea();
      Location cursor = this.getEnvironment().getCursor();
      int offsetX = (int)Math.round((double)this.getOffsetX() * this.scaling);
      int offsetY = (int)Math.round((double)this.getOffsetY() * this.scaling);
      if (this.getOffsetType().equals(this.getSchema().getString("Origin"))) {
         offsetX = 0 - offsetX + this.getMascot().getImage().size().width / 2;
         offsetY = 0 - offsetY + this.getMascot().getImage().size().height / 2;
      }

      if (Math.abs(cursor.getX() - offsetX - this.getMascot().getAnchor().x) >= 5) {
         this.setTime(0);
      }

      int newX = cursor.getX();
      this.setFootDx((this.getFootDx() + ((double)newX - this.getFootX()) * 0.1) * 0.8);
      this.setFootX(this.getFootX() + this.getFootDx());
      this.putVariable(this.getSchema().getString("FootDX"), this.getFootDx());
      this.putVariable(this.getSchema().getString("FootX"), this.getFootX());
      this.getAnimation().next(this.getMascot(), this.getTime());
      this.getMascot().setAnchor(new Point(cursor.getX() + offsetX, cursor.getY() + offsetY));
   }

   protected void refreshHotspots() {
      this.getMascot().getHotspots().clear();
   }

   public void setTimeToRegist(int timeToRegist) {
      this.timeToRegist = timeToRegist;
   }

   private int getTimeToRegist() {
      return this.timeToRegist;
   }

   private void setFootX(double footX) {
      this.footX = footX;
   }

   private double getFootX() {
      return this.footX;
   }

   private void setFootDx(double footDx) {
      this.footDx = footDx;
   }

   private double getFootDx() {
      return this.footDx;
   }

   private int getOffsetX() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("OffsetX"), Number.class, 0)).intValue();
   }

   private int getOffsetY() throws VariableException {
      return ((Number)this.eval(this.getSchema().getString("OffsetY"), Number.class, 120)).intValue();
   }

   private String getOffsetType() throws VariableException {
      return (String)this.eval(this.getSchema().getString("OffsetType"), String.class, "ImageAnchor");
   }
}
