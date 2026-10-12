package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import java.awt.Point;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Breed extends Animate {
   private static final Logger log = Logger.getLogger(Breed.class.getName());
   private final Delegate delegate = new Delegate(this);

   public Breed(ResourceBundle schema, List animations, VariableMap context) {
      super(schema, animations, context);
   }

   public void init(Mascot mascot) throws VariableException {
      super.init(mascot);
      this.delegate.validateBornCount();
   }

   protected void tick() throws LostGroundException, VariableException {
      super.tick();
      if (this.delegate.isPenultimateFrame() && this.delegate.isEnabled()) {
         this.delegate.breed();
      }

   }

   static class Delegate {
      public static final String PARAMETER_BORNX = "BornX";
      private static final int DEFAULT_BORNX = 0;
      public static final String PARAMETER_BORNY = "BornY";
      private static final int DEFAULT_BORNY = 0;
      public static final String PARAMETER_BORNBEHAVIOUR = "BornBehaviour";
      private static final String DEFAULT_BORNBEHAVIOUR = "";
      public static final String PARAMETER_BORNMASCOT = "BornMascot";
      private static final String DEFAULT_BORNMASCOT = "";
      public static final String PARAMETER_BORNINTERVAL = "BornInterval";
      private static final int DEFAULT_BORNINTERVAL = 1;
      public static final String PARAMETER_BORNTRANSIENT = "BornTransient";
      private static final boolean DEFAULT_BORNTRANSIENT = false;
      public static final String PARAMETER_BORNCOUNT = "BornCount";
      private static final int DEFAULT_BORNCOUNT = 1;
      private final ActionBase action;

      Delegate(ActionBase action) {
         this.action = action;
      }

      boolean isEnabled() throws VariableException {
         return this.getBornTransient() ? Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Transients", "true")) : Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Breeding", "true"));
      }

      boolean isIntervalFrame() throws VariableException {
         return this.action.getTime() % this.getBornInterval() == 0;
      }

      boolean isPenultimateFrame() throws VariableException {
         return this.action.getTime() == this.action.getAnimation().getDuration() - 1;
      }

      void breed() throws VariableException {
         double scaling = Double.parseDouble(Main.getInstance().getProperties().getProperty("Scaling", "1.0"));
         String childType = Main.getInstance().getConfiguration(this.getBornMascot()) != null ? this.getBornMascot() : this.action.getMascot().getImageSet();

         for(int index = 0; index < this.getBornCount(); ++index) {
            Mascot mascot = new Mascot(childType);
            Breed.log.log(Level.INFO, "Breed Mascot ({0},{1},{2})", new Object[]{this.action.getMascot(), this.action, mascot});
            if (this.action.getMascot().isLookRight()) {
               mascot.setAnchor(new Point(this.action.getMascot().getAnchor().x - (int)Math.round((double)this.getBornX() * scaling), this.action.getMascot().getAnchor().y + (int)Math.round((double)this.getBornY() * scaling)));
            } else {
               mascot.setAnchor(new Point(this.action.getMascot().getAnchor().x + (int)Math.round((double)this.getBornX() * scaling), this.action.getMascot().getAnchor().y + (int)Math.round((double)this.getBornY() * scaling)));
            }

            mascot.setLookRight(this.action.getMascot().isLookRight());

            try {
               mascot.setBehavior(Main.getInstance().getConfiguration(childType).buildBehavior(this.getBornBehaviour(), this.action.getMascot()));
               this.action.getMascot().getManager().add(mascot);
            } catch (CantBeAliveException | BehaviorInstantiationException e) {
               Breed.log.log(Level.SEVERE, "Fatal Exception", e);
               String var10000 = Main.getInstance().getLanguageBundle().getString("FailedCreateNewShimejiErrorMessage");
               Main.showError(var10000 + "\n" + ((Exception)e).getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               mascot.dispose();
            }
         }

      }

      void validateBornCount() throws VariableException {
         if (this.getBornCount() < 1) {
            throw new VariableException("BornCount must be positive");
         }
      }

      void validateBornInterval() throws VariableException {
         if (this.getBornInterval() < 1) {
            throw new VariableException("BornInterval must be positive");
         }
      }

      private int getBornX() throws VariableException {
         return ((Number)this.action.eval(this.action.getSchema().getString("BornX"), Number.class, 0)).intValue();
      }

      private int getBornY() throws VariableException {
         return ((Number)this.action.eval(this.action.getSchema().getString("BornY"), Number.class, 0)).intValue();
      }

      private String getBornBehaviour() throws VariableException {
         return (String)this.action.eval(this.action.getSchema().getString("BornBehaviour"), String.class, "");
      }

      private String getBornMascot() throws VariableException {
         return (String)this.action.eval(this.action.getSchema().getString("BornMascot"), String.class, "");
      }

      private boolean getBornTransient() throws VariableException {
         return (Boolean)this.action.eval(this.action.getSchema().getString("BornTransient"), Boolean.class, false);
      }

      private int getBornInterval() throws VariableException {
         return ((Number)this.action.eval(this.action.getSchema().getString("BornInterval"), Number.class, 1)).intValue();
      }

      private int getBornCount() throws VariableException {
         return ((Number)this.action.eval(this.action.getSchema().getString("BornCount"), Number.class, 1)).intValue();
      }
   }
}
