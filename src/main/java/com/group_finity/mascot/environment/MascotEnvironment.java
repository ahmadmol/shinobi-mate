package com.group_finity.mascot.environment;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.NativeFactory;
import java.awt.Point;

public class MascotEnvironment {
   private final Environment impl;
   private final Mascot mascot;
   private Area currentWorkArea;
   private final Area logicalWorkArea=new Area();

   public MascotEnvironment(Mascot mascot) {
      this.mascot = mascot;
      this.impl = NativeFactory.getInstance().getEnvironment();
      this.impl.init();
   }

   public Area getWorkArea() {
      return this.getWorkArea(false);
   }

   public Area getWorkArea(Boolean ignoreSettings) {
      if(NativeFactory.getInstance() instanceof com.group_finity.mascot.win.NativeFactoryImpl) {
         if(ignoreSettings || currentWorkArea!=logicalWorkArea || Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen","true")))
            logicalWorkArea.set(DesktopWorkArea.at(mascot.getAnchor()));
         currentWorkArea=logicalWorkArea;
         return logicalWorkArea;
      }
      if (this.currentWorkArea != null) {
         if (!ignoreSettings && !Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen", "true"))) {
            return this.currentWorkArea;
         }

         if (this.currentWorkArea != this.impl.getWorkArea() && this.currentWorkArea.toRectangle().contains(this.impl.getWorkArea().toRectangle()) && this.impl.getWorkArea().contains(this.mascot.getAnchor().x, this.mascot.getAnchor().y)) {
            this.currentWorkArea = this.impl.getWorkArea();
            return this.currentWorkArea;
         }

         if (this.currentWorkArea.contains(this.mascot.getAnchor().x, this.mascot.getAnchor().y)) {
            return this.currentWorkArea;
         }
      }

      if (this.impl.getWorkArea().contains(this.mascot.getAnchor().x, this.mascot.getAnchor().y)) {
         this.currentWorkArea = this.impl.getWorkArea();
         return this.currentWorkArea;
      } else {
         for(Area area : this.impl.getScreens()) {
            if (area.contains(this.mascot.getAnchor().x, this.mascot.getAnchor().y)) {
               this.currentWorkArea = area;
               return this.currentWorkArea;
            }
         }

         this.currentWorkArea = this.impl.getWorkArea();
         return this.currentWorkArea;
      }
   }

   public Area getActiveIE() {
      Area activeIE = this.impl.getActiveIE();
      return this.currentWorkArea != null && !Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen", "true")) && !this.currentWorkArea.toRectangle().intersects(activeIE.toRectangle()) ? new Area() : activeIE;
   }

   public String getActiveIETitle() {
      return this.impl.getActiveIETitle();
   }

   public Border getCeiling() {
      return this.getCeiling(false);
   }

   public Border getCeiling(boolean ignoreSeparator) {
      if (this.getActiveIE().getBottomBorder().isOn(this.mascot.getAnchor())) {
         return this.getActiveIE().getBottomBorder();
      } else {
         return (Border)(!this.getWorkArea().getTopBorder().isOn(this.mascot.getAnchor()) || ignoreSeparator && !this.isScreenTopBottom() ? NotOnBorder.INSTANCE : this.getWorkArea().getTopBorder());
      }
   }

   public ComplexArea getComplexScreen() {
      return this.impl.getComplexScreen();
   }

   public Location getCursor() {
      return this.impl.getCursor();
   }

   public Border getFloor() {
      return this.getFloor(false);
   }

   public Border getFloor(boolean ignoreSeparator) {
      if (this.getActiveIE().getTopBorder().isOn(this.mascot.getAnchor())) {
         return this.getActiveIE().getTopBorder();
      } else {
         return (Border)(!this.getWorkArea().getBottomBorder().isOn(this.mascot.getAnchor()) || ignoreSeparator && !this.isScreenTopBottom() ? NotOnBorder.INSTANCE : this.getWorkArea().getBottomBorder());
      }
   }

   public Area getScreen() {
      return this.impl.getScreen();
   }

   public Border getWall() {
      return this.getWall(false);
   }

   public Border getWall(boolean ignoreSeparator) {
      if (this.mascot.isLookRight()) {
         if (this.getActiveIE().getLeftBorder().isOn(this.mascot.getAnchor())) {
            return this.getActiveIE().getLeftBorder();
         }

         if (this.getWorkArea().getRightBorder().isOn(this.mascot.getAnchor()) && (!ignoreSeparator || this.isScreenLeftRight())) {
            return this.getWorkArea().getRightBorder();
         }
      } else {
         if (this.getActiveIE().getRightBorder().isOn(this.mascot.getAnchor())) {
            return this.getActiveIE().getRightBorder();
         }

         if (this.getWorkArea().getLeftBorder().isOn(this.mascot.getAnchor()) && (!ignoreSeparator || this.isScreenLeftRight())) {
            return this.getWorkArea().getLeftBorder();
         }
      }

      return NotOnBorder.INSTANCE;
   }

   public void moveActiveIE(Point point) {
      this.impl.moveActiveIE(point);
   }

   public void restoreIE() {
      this.impl.restoreIE();
   }

   public void refreshWorkArea() {
      this.getWorkArea(true);
   }

   private boolean isScreenTopBottom() {
      return this.impl.isScreenTopBottom(this.mascot.getAnchor());
   }

   private boolean isScreenLeftRight() {
      return this.impl.isScreenLeftRight(this.mascot.getAnchor());
   }
}
