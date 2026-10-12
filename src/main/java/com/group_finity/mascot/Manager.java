package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {
   private static final Logger log = Logger.getLogger(Manager.class.getName());
   public static final int TICK_INTERVAL = 40;
   private final List<Mascot> mascots = new ArrayList<>();
   private final com.group_finity.mascot.combat.CombatEngine combat = new com.group_finity.mascot.combat.CombatEngine(this);

   public com.group_finity.mascot.combat.CombatEngine getCombat() { return this.combat; }
   private final Set<Mascot> added = new LinkedHashSet<>();
   private final Set<Mascot> removed = new LinkedHashSet<>();
   private boolean exitOnLastRemoved = true;
   private Thread thread;
   private volatile boolean running;
   private final com.group_finity.mascot.performance.RenderGate renderGate = new com.group_finity.mascot.performance.RenderGate();
   private final com.group_finity.mascot.interaction.SocialService social=new com.group_finity.mascot.interaction.SocialService();
   private volatile long logicTicks,renderTicks;
   public long getLogicTicks(){return logicTicks;}
   public long getRenderTicks(){return renderTicks;}

   public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
      this.exitOnLastRemoved = exitOnLastRemoved;
   }

   public boolean isExitOnLastRemoved() {
      return this.exitOnLastRemoved;
   }

   public Manager() {
      // The engine thread is created by start(); constructing a manager must not leak a sleeper.
   }

   public void start() {
      if (this.thread == null || !this.thread.isAlive()) {
         this.running=true;
         this.thread = new Thread(() -> {
            long prev = System.nanoTime() / 1000000L;

            try {
               while(this.running) {
                  long cur = System.nanoTime() / 1000000L;
                  if (cur - prev >= 40L) {
                     if (cur > prev + 80L) {
                        prev = cur;
                     } else {
                        prev += 40L;
                     }

                     try{this.tick();}catch(RuntimeException ex){log.log(Level.SEVERE,"Engine tick failed; continuing with the next tick",ex);Thread.sleep(TICK_INTERVAL);}
                  } else {
                     Thread.sleep(1L, 0);
                  }
               }
            } catch (InterruptedException ignored) {
            }
         });
         this.thread.setDaemon(false);
         this.thread.setName("ShinobiMate engine");
         this.thread.start();
      }
   }

   public void stop() {
      this.running=false;
      if (this.thread != null && this.thread.isAlive()) {
         this.thread.interrupt();

         try {
            if(Thread.currentThread()!=this.thread)this.thread.join();
         } catch (InterruptedException ignored) {
         }
      }
   }

   private void tick() {
      this.logicTicks++;
      NativeFactory.getInstance().getEnvironment().tick();
      List<Mascot> rendering=List.of();
      synchronized(this.getMascots()) {
         for(Mascot mascot : this.getAdded()) {
            this.getMascots().add(mascot);
         }

         this.getAdded().clear();

         for(Mascot mascot : this.getRemoved()) {
            this.getMascots().remove(mascot);
         }

         this.getRemoved().clear();

         com.group_finity.mascot.focus.FocusService.tick(this);
         boolean render = this.renderGate.ready(System.nanoTime()/1_000_000L,
               com.group_finity.mascot.performance.PerformanceProfile.current().fps());
         if(render)this.renderTicks++;
         this.combat.tick(render);

         for(Mascot mascot : this.getMascots()) {
            try{mascot.tick();}catch(RuntimeException ex){log.log(Level.WARNING,"Character update failed",ex);mascot.dispose();}
         }
         this.social.tick(this);
         com.group_finity.mascot.combat.DesktopAvoidance.tick(this);
         com.group_finity.mascot.progression.ProgressionService.tick(this);

         if(render)rendering=List.copyOf(this.getMascots());
      }
      // AWT's tree lock must never be taken while the EDT can wait for the mascot-list lock.
      for(Mascot mascot:rendering)if(mascot.getManager()==this) {
         try{mascot.apply();}catch(RuntimeException ex){log.log(Level.WARNING,"Character rendering failed",ex);mascot.dispose();}
      }

      if (this.isExitOnLastRemoved() && this.getMascots().isEmpty()) {
         Main.getInstance().exit();
      }
   }

   public void add(Mascot mascot) {
      synchronized(this.getMascots()) {
         int max=(int)com.group_finity.mascot.settings.RuntimeSettings.number("MaxCharacters",20,1,100);
         if(this.mascots.size()+this.added.size()>=max){mascot.dispose();return;}
         this.getAdded().add(mascot);
         this.getRemoved().remove(mascot);
         mascot.setManager(this);
         Main.getInstance().activeCharactersChanged(this);
      }
   }

   public void remove(Mascot mascot) {
      synchronized(this.getMascots()) {
         this.combat.forget(mascot);
         this.getAdded().remove(mascot);
         this.getRemoved().add(mascot);
         mascot.setManager(null);
         Main.getInstance().activeCharactersChanged(this);
      }
   }

   public void setBehaviorAll(String name) {
      synchronized(this.getMascots()) {
         for(Mascot mascot : this.getMascots()) {
            try {
               Configuration configuration = Main.getInstance().getConfiguration(mascot.getImageSet());
               mascot.setBehavior(configuration.buildBehavior(configuration.getSchema().getString(name), mascot));
            } catch (BehaviorInstantiationException e) {
               log.log(Level.SEVERE, "Failed to initialize the following actions", e);
               String var10 = Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage");
               Main.showError(var10 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               mascot.dispose();
            } catch (CantBeAliveException e) {
               log.log(Level.SEVERE, "Fatal Error", e);
               String var10000 = Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage");
               Main.showError(var10000 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               mascot.dispose();
            }
         }
      }
   }

   public void setBehaviorAll(Configuration configuration, String name, String imageSet) {
      synchronized(this.getMascots()) {
         for(Mascot mascot : this.getMascots()) {
            try {
               if (mascot.getImageSet().equals(imageSet)) {
                  mascot.setBehavior(configuration.buildBehavior(configuration.getSchema().getString(name), mascot));
               }
            } catch (BehaviorInstantiationException e) {
               log.log(Level.SEVERE, "Failed to initialize the following actions", e);
               String var12 = Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage");
               Main.showError(var12 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               mascot.dispose();
            } catch (CantBeAliveException e) {
               log.log(Level.SEVERE, "Fatal Error", e);
               String var10000 = Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage");
               Main.showError(var10000 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               mascot.dispose();
            }
         }
      }
   }

   public void remainOne() {
      synchronized(this.getMascots()) {
         this.getMascots().stream().skip(1L).forEach(Mascot::dispose);
      }
   }

   public void remainOne(Mascot mascot) {
      synchronized(this.getMascots()) {
         this.getMascots().stream().filter((m) -> !m.equals(mascot)).forEach(Mascot::dispose);
      }
   }

   public void remainOne(String imageSet) {
      synchronized(this.getMascots()) {
         List<Mascot> matching = this.getMascots().stream().filter((m) -> m.getImageSet().equals(imageSet)).toList();
         matching.stream().skip(1L).forEach(Mascot::dispose);
      }
   }

   public void remainNone(String imageSet) {
      synchronized(this.getMascots()) {
         this.getMascots().removeIf((mascot) -> {
            if (mascot.getImageSet().equals(imageSet)) {
               mascot.dispose();
               return true;
            } else {
               return false;
            }
         });
      }
   }

   public void togglePauseAll() {
      boolean isPaused = true;
      synchronized(this.getMascots()) {
         for(Mascot mascot : this.getMascots()) {
            if (!mascot.isPaused()) {
               isPaused = false;
               break;
            }
         }

         for(Mascot mascot : this.getMascots()) {
            mascot.setPaused(!isPaused);
         }
      }
   }

   public boolean isPaused() {
      boolean isPaused = true;
      synchronized(this.getMascots()) {
         for(Mascot mascot : this.getMascots()) {
            if (!mascot.isPaused()) {
               isPaused = false;
               break;
            }
         }

         return isPaused;
      }
   }

   public int getCount() {
      return this.getCount((String)null);
   }

   public int getCount(String imageSet) {
      synchronized(this.getMascots()) {
         return imageSet == null ? this.getMascots().size() : (int)this.getMascots().stream().filter((mascot) -> mascot.getImageSet().equals(imageSet)).count();
      }
   }

   public List<Mascot> getMascots() {
      return this.mascots;
   }

   public Set<Mascot> getAdded() {
      return this.added;
   }

   public Set<Mascot> getRemoved() {
      return this.removed;
   }

   public WeakReference<Mascot> getMascotWithAffordance(String affordance) {
      synchronized(this.getMascots()) {
         Optional<Mascot> found = this.getMascots().stream().filter((mascot) -> mascot.getAffordances().contains(affordance)).findFirst();
         return found.map(WeakReference::new).orElse(null);
      }
   }

   public boolean hasOverlappingMascotsAtPoint(Point anchor) {
      synchronized(this.getMascots()) {
         long count = this.getMascots().stream().filter((mascot) -> mascot.getAnchor().equals(anchor)).count();
         return count > 1L;
      }
   }

   public void disposeAll() {
      java.util.List<Mascot> disposal;
      synchronized(this.getMascots()) {
         java.util.Set<Mascot> snapshot=new java.util.LinkedHashSet<>(this.getMascots());snapshot.addAll(this.getAdded());
         disposal=java.util.List.copyOf(snapshot);this.getAdded().clear();this.getRemoved().clear();
         this.getMascots().clear();
      }
      // Window disposal must never wait for the EDT while holding the mascot-list lock.
      disposal.forEach(Mascot::dispose);
      Main.getInstance().activeCharactersChanged(this);
   }
}
