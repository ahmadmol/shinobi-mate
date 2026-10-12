package com.group_finity.mascot.behavior;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.ActionBase;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class UserBehavior implements Behavior {
   private static final Logger log = Logger.getLogger(UserBehavior.class.getName());
   public static final String BEHAVIOURNAME_FALL = "Fall";
   public static final String BEHAVIOURNAME_DRAGGED = "Dragged";
   public static final String BEHAVIOURNAME_THROWN = "Thrown";
   private final String name;
   private final Configuration configuration;
   private final Action action;
   private Mascot mascot;

   public UserBehavior(String name, Action action, Configuration configuration) {
      this.name = name;
      this.configuration = configuration;
      this.action = action;
   }

   public String toString() {
      return "Behavior(" + this.getName() + ")";
   }

   public synchronized void init(Mascot mascot) throws CantBeAliveException {
      this.setMascot(mascot);
      log.log(Level.INFO, "Default Behavior({0},{1})", new Object[]{this.getMascot(), this});

      try {
         this.getAction().init(mascot);
         if (!this.getAction().hasNext()) {
            try {
               mascot.setBehavior(this.getConfiguration().buildNextBehavior(this.getName(), mascot));
            } catch (BehaviorInstantiationException e) {
               throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingBehaviourErrorMessage"), e);
            }
         }

      } catch (VariableException e) {
         throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("VariableEvaluationErrorMessage"), e);
      }
   }

   private Configuration getConfiguration() {
      return this.configuration;
   }

   private Action getAction() {
      return this.action;
   }

   private String getName() {
      return this.name;
   }

   public synchronized void mousePressed(MouseEvent event) throws CantBeAliveException {
      if (SwingUtilities.isLeftMouseButton(event)) {
         boolean handled = false;
         if (!this.mascot.getHotspots().isEmpty()) {
            for(Hotspot hotspot : this.mascot.getHotspots()) {
               if (hotspot.contains(this.mascot, event.getPoint()) && Main.getInstance().getConfiguration(this.mascot.getImageSet()).isBehaviorEnabled(hotspot.behaviour(), this.mascot)) {
                  handled = true;

                  try {
                     this.getMascot().setCursorPosition(event.getPoint());
                     if (hotspot.behaviour() != null) {
                        this.getMascot().setBehavior(this.configuration.buildBehavior(hotspot.behaviour(), this.mascot));
                     }
                     break;
                  } catch (BehaviorInstantiationException e) {
                     throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingBehaviourErrorMessage") + " " + hotspot.behaviour(), e);
                  }
               }
            }
         }

         if (!handled && this.action != null && this.action instanceof ActionBase) {
            try {
               handled = !((ActionBase)this.action).isDraggable();
            } catch (VariableException ex) {
               throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDragActionInitialiseErrorMessage"), ex);
            }
         }

         if (!handled) {
            try {
               this.getMascot().setBehavior(this.configuration.buildBehavior(this.configuration.getSchema().getString("Dragged")));
               // A release may arrive before the next engine tick (especially a fast drag).
               this.getMascot().setDragging(true);
            } catch (BehaviorInstantiationException e) {
               throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDragActionInitialiseErrorMessage"), e);
            }
         }
      }

   }

   public synchronized void mouseReleased(MouseEvent event) throws CantBeAliveException {
      if (SwingUtilities.isLeftMouseButton(event)) {
         if (this.getMascot().isHotspotClicked()) {
            this.getMascot().setCursorPosition((Point)null);
         }

         if (this.getMascot().isDragging()) {
            try {
               this.getMascot().setDragging(false);
               this.getMascot().setBehavior(this.configuration.buildBehavior(this.configuration.getSchema().getString("Thrown")));
            } catch (BehaviorInstantiationException e) {
               throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedDropActionInitialiseErrorMessage"), e);
            }
         }
      }

   }

   public synchronized void next() throws CantBeAliveException {
      try {
         if (this.getAction().hasNext()) {
            this.getAction().next();
         }

         HotspotResult hotspotIsActive = UserBehavior.HotspotResult.INACTIVE;
         if (this.getMascot().isHotspotClicked()) {
            if (!this.mascot.getHotspots().isEmpty()) {
               for(Hotspot hotspot : this.mascot.getHotspots()) {
                  if (hotspot.contains(this.mascot, this.mascot.getCursorPosition())) {
                     hotspotIsActive = UserBehavior.HotspotResult.ACTIVE_NULL;

                     try {
                        if (hotspot.behaviour() != null) {
                           hotspotIsActive = UserBehavior.HotspotResult.ACTIVE;
                           this.getMascot().setBehavior(this.configuration.buildBehavior(hotspot.behaviour(), this.mascot));
                        }
                        break;
                     } catch (BehaviorInstantiationException e) {
                        throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingBehaviourErrorMessage") + " " + hotspot.behaviour(), e);
                     }
                  }
               }
            }

            if (hotspotIsActive == UserBehavior.HotspotResult.INACTIVE) {
               this.getMascot().setCursorPosition((Point)null);
            }
         }

         if (hotspotIsActive != UserBehavior.HotspotResult.ACTIVE) {
            if (this.getAction().hasNext()) {
               if (this.getMascot().getBounds().getX() + this.getMascot().getBounds().getWidth() <= (double)this.getEnvironment().getScreen().getLeft() || (double)this.getEnvironment().getScreen().getRight() <= this.getMascot().getBounds().getX() || (double)this.getEnvironment().getScreen().getBottom() <= this.getMascot().getBounds().getY()) {
                  log.log(Level.INFO, "Out of the screen bounds({0},{1})", new Object[]{this.getMascot(), this});
                  if (Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Multiscreen", "true"))) {
                     this.getMascot().setAnchor(new Point((int)(Math.random() * (double)(this.getEnvironment().getScreen().getRight() - this.getEnvironment().getScreen().getLeft())) + this.getEnvironment().getScreen().getLeft(), this.getEnvironment().getScreen().getTop() - 256));
                  } else {
                     this.getMascot().setAnchor(new Point((int)(Math.random() * (double)(this.getEnvironment().getWorkArea().getRight() - this.getEnvironment().getWorkArea().getLeft())) + this.getEnvironment().getWorkArea().getLeft(), this.getEnvironment().getWorkArea().getTop() - 256));
                  }

                  try {
                     this.getMascot().setBehavior(this.getConfiguration().buildBehavior(this.configuration.getSchema().getString("Fall")));
                  } catch (BehaviorInstantiationException e) {
                     throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedFallingActionInitialiseErrorMessage"), e);
                  }
               }
            } else {
               log.log(Level.INFO, "Completed Behavior ({0},{1})", new Object[]{this.getMascot(), this});

               try {
                  this.getMascot().setBehavior(this.getConfiguration().buildNextBehavior(this.getName(), this.getMascot()));
               } catch (BehaviorInstantiationException e) {
                  throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedInitialiseFollowingActionsErrorMessage"), e);
               }
            }
         }
      } catch (LostGroundException e) {
         log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[]{this.getMascot(), this});

         try {
            this.getMascot().setCursorPosition((Point)null);
            this.getMascot().setDragging(false);
            this.getMascot().setBehavior(this.configuration.buildBehavior(this.configuration.getSchema().getString("Fall")));
         } catch (BehaviorInstantiationException var5) {
            throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("FailedFallingActionInitialiseErrorMessage"), e);
         }
      } catch (VariableException e) {
         throw new CantBeAliveException(Main.getInstance().getLanguageBundle().getString("VariableEvaluationErrorMessage"), e);
      }

   }

   private void setMascot(Mascot mascot) {
      this.mascot = mascot;
   }

   private Mascot getMascot() {
      return this.mascot;
   }

   protected MascotEnvironment getEnvironment() {
      return this.getMascot().getEnvironment();
   }

   private static enum HotspotResult {
      INACTIVE,
      ACTIVE_NULL,
      ACTIVE;

      // $FF: synthetic method
      private static HotspotResult[] $values() {
         return new HotspotResult[]{INACTIVE, ACTIVE_NULL, ACTIVE};
      }
   }
}
