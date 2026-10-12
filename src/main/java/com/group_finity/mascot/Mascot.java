package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.cloud.ClickGesture;
import com.group_finity.mascot.cloud.SpiritualCloud;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.menu.JLongMenu;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.Sounds;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class Mascot {
   private static final Logger log = Logger.getLogger(Mascot.class.getName());
   public static final boolean DRAW_DEBUG = false;
   private static final AtomicInteger lastId = new AtomicInteger();
   private final int id;
   private String imageSet = "";
   private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();
   private Manager manager = null;
   private Point anchor = new Point(0, 0);
   private MascotImage image = null;
   private boolean lookRight = false;
   private Behavior behavior = null;
   private int time = 0;
   private boolean animating = true;
   private boolean paused = false;
   private boolean dragging = false;
   private final MascotEnvironment environment = new MascotEnvironment(this);
   private String sound = null;
   private com.group_finity.mascot.audio.AudioManager.Scope audioScope=new com.group_finity.mascot.audio.AudioManager.Scope();
   public com.group_finity.mascot.audio.AudioManager.Scope getAudioScope(){return audioScope;}
   public void playAudio(String event) {
      var area=this.environment.getWorkArea().toRectangle();
      double pan=area.width>0?Math.max(-.6,Math.min(.6,(this.anchor.x-area.getCenterX())/area.width)):0;
      com.group_finity.mascot.audio.AudioManager.global().playCharacter(imageSet,event,audioScope,pan);
   }
   protected DebugWindow debugWindow = null;
   private final ArrayList<String> affordances = new ArrayList<>(5);
   private final ArrayList<Hotspot> hotspots = new ArrayList<>(5);
   private VariableMap variables = null;
   private Point cursor = null;
   private final SpiritualCloud spiritualCloud;
   private final ClickGesture cloudGesture = new ClickGesture();
   private MouseEvent pendingCloudPress;
   private final com.group_finity.mascot.character.CompanionFeatures companionFeatures;
   private boolean appliedAlwaysOnTop;
   public com.group_finity.mascot.character.CompanionFeatures getCompanionFeatures(){return this.companionFeatures;}

   public Mascot(String imageSet) {
      this.id = lastId.incrementAndGet();
      this.imageSet = imageSet;
      com.group_finity.mascot.audio.AudioManager.global().preload(imageSet);
      this.spiritualCloud = new SpiritualCloud(this.window.asComponent(), imageSet);
      this.companionFeatures=new com.group_finity.mascot.character.CompanionFeatures(this);
      log.log(Level.INFO, "Created a mascot ({0})", this);
      this.appliedAlwaysOnTop=com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true);
      this.getWindow().setAlwaysOnTop(this.appliedAlwaysOnTop);
      this.getWindow().asComponent().addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            Mascot.this.mousePressed(e);
         }

         public void mouseReleased(MouseEvent e) {
            Mascot.this.mouseReleased(e);
         }

         public void mouseEntered(MouseEvent e) {
            Mascot.this.companionFeatures.hover(true);
            Mascot.this.spiritualCloud.mouseEnter();
         }

         public void mouseExited(MouseEvent e) {
            Mascot.this.companionFeatures.hover(false);
            Mascot.this.spiritualCloud.mouseExit();
         }
      });
      this.getWindow().asComponent().addMouseMotionListener(new MouseMotionListener() {
         public void mouseMoved(MouseEvent e) {
            if (Mascot.this.paused) {
               Mascot.this.refreshCursor(false);
            } else if (Mascot.this.isHotspotClicked()) {
               Mascot.this.setCursorPosition(e.getPoint());
            } else {
               Mascot.this.refreshCursor(e.getPoint());
            }

         }

         public void mouseDragged(MouseEvent e) {
            Mascot.this.updateCloudGesture(e);
            if (Mascot.this.paused) {
               Mascot.this.refreshCursor(false);
            } else if (Mascot.this.isHotspotClicked()) {
               Mascot.this.setCursorPosition(e.getPoint());
            } else {
               Mascot.this.refreshCursor(e.getPoint());
            }

         }
      });
   }

   public String toString() {
      return "mascot" + this.id;
   }

   private static boolean isMacOS() {
      return System.getProperty("os.name").toLowerCase().contains("mac");
   }

   private static boolean shouldShowPopupMenu(MouseEvent event) {
      if (event.isPopupTrigger()) {
         return true;
      } else if (!isMacOS()) {
         return false;
      } else {
         return event.getButton() == 3 || event.getButton() == 1 && event.isControlDown();
      }
   }

   private void mousePressed(MouseEvent event) {
      if ((this.spiritualCloud.isEnabled()||com.group_finity.mascot.settings.RuntimeSettings.flag("MouseInteractions",true)) && SwingUtilities.isLeftMouseButton(event) && !shouldShowPopupMenu(event)) {
         this.cloudGesture.press(event.getLocationOnScreen());
         // Hotspots retain their press behavior. Ordinary presses wait for actual movement.
         boolean hotspot = !this.isPaused() && this.getHotspots().stream().anyMatch(h ->
               h.contains(this, event.getPoint()) && Main.getInstance().getConfiguration(this.imageSet).isBehaviorEnabled(h.behaviour(), this));
         if (!hotspot) {
            this.pendingCloudPress = event;
            return;
         }
      }
      this.forwardMousePressed(event);
   }

   private void updateCloudGesture(MouseEvent event) {
      if (this.cloudGesture.move(event.getLocationOnScreen()) && this.pendingCloudPress != null) {
         MouseEvent pressed = this.pendingCloudPress;
         this.pendingCloudPress = null;
         this.forwardMousePressed(pressed);
      }
   }

   private void forwardMousePressed(MouseEvent event) {
      Manager current = this.manager;
      if (current != null) {
         synchronized (current.getMascots()) { this.forwardMousePressedLocked(event); }
      } else this.forwardMousePressedLocked(event);
   }

   private void forwardMousePressedLocked(MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event) && this.manager != null) this.manager.getCombat().stop(this);
      if (!this.isPaused() && this.getBehavior() != null) {
         try {
            this.getBehavior().mousePressed(event);
         } catch (CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Error", e);
            String var10000 = Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage");
            Main.showError(var10000 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
            this.dispose();
         }
      }

   }

   private void mouseReleased(MouseEvent event) {
      Manager current = this.manager;
      if (current != null) {
         synchronized (current.getMascots()) { this.mouseReleasedLocked(event); }
      } else this.mouseReleasedLocked(event);
   }

   private void mouseReleasedLocked(MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event) && this.cloudGesture.isPressed()) {
         this.updateCloudGesture(event);
         boolean click = this.cloudGesture.release(event.getLocationOnScreen());
         boolean deferred = this.pendingCloudPress != null;
         this.pendingCloudPress = null;
         if (click) {if(this.spiritualCloud.isEnabled())this.spiritualCloud.click();this.companionFeatures.click();}
         if (deferred) return;
      }
      if (shouldShowPopupMenu(event)) {
         SwingUtilities.invokeLater(() -> this.showPopup(event.getX(), event.getY()));
      } else if (!this.isPaused() && this.getBehavior() != null) {
         try {
            this.getBehavior().mouseReleased(event);
         } catch (CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Error", e);
            String var10000 = Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage");
            Main.showError(var10000 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
            this.dispose();
         }
      }

   }

   private void showPopup(int x, int y) {
      JPopupMenu popup = new JPopupMenu();
      final ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();
      popup.addPopupMenuListener(new PopupMenuListener() {
         public void popupMenuCanceled(PopupMenuEvent e) {
         }

         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            Mascot.this.setAnimating(true);
         }

         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            Mascot.this.setAnimating(false);
         }
      });
      JMenuItem increaseMenu = new JMenuItem(languageBundle.getString("CallAnother"));
      increaseMenu.addActionListener((event) -> com.group_finity.mascot.character.CharacterService.launchFromMenu(this.imageSet));
      JMenuItem disposeMenu = new JMenuItem(languageBundle.getString("Dismiss"));
      disposeMenu.addActionListener((e) -> this.dispose());
      JMenuItem gatherMenu = new JMenuItem(languageBundle.getString("FollowCursor"));
      gatherMenu.addActionListener((event) -> this.getManager().setBehaviorAll(Main.getInstance().getConfiguration(this.imageSet), "ChaseMouse", this.imageSet));
      JMenuItem oneMenu = new JMenuItem(languageBundle.getString("DismissOthers"));
      oneMenu.addActionListener((event) -> this.getManager().remainOne(this.imageSet));
      JMenuItem onlyOneMenu = new JMenuItem(languageBundle.getString("DismissAllOthers"));
      onlyOneMenu.addActionListener((event) -> this.getManager().remainOne(this));
      JMenuItem restoreMenu = new JMenuItem(languageBundle.getString("RestoreWindows"));
      restoreMenu.addActionListener((event) -> NativeFactory.getInstance().getEnvironment().restoreIE());
      JMenuItem debugMenu = new JMenuItem(languageBundle.getString("RevealStatistics"));
      debugMenu.addActionListener((event) -> {
         if (this.debugWindow == null) {
            this.debugWindow = new DebugWindow();
         }

         this.debugWindow.setVisible(true);
      });
      JMenuItem closeMenu = new JMenuItem(languageBundle.getString("DismissAll"));
      closeMenu.addActionListener((e) -> Main.getInstance().exit());
      JMenuItem pauseMenu = new JMenuItem(this.isAnimating() ? languageBundle.getString("PauseAnimations") : languageBundle.getString("ResumeAnimations"));
      pauseMenu.addActionListener((event) -> this.setPaused(!this.isPaused()));
      JLongMenu submenu = new JLongMenu(languageBundle.getString("SetBehaviour"), 30);
      JLongMenu allowedSubmenu = new JLongMenu(languageBundle.getString("AllowedBehaviours"), 30);
      submenu.setAutoscrolls(true);
      final Configuration config = Main.getInstance().getConfiguration(this.getImageSet());

      for(String behaviorName : config.getBehaviorNames()) {
         final String command = behaviorName;

         try {
            if (!config.isBehaviorHidden(command)) {
               String caption = behaviorName.replaceAll("([a-z])(IE)?([A-Z])", "$1 $2 $3").replaceAll("  ", " ");
               if (config.isBehaviorEnabled(command, this) && !command.contains("/")) {
                  JMenuItem item = new JMenuItem(languageBundle.containsKey(behaviorName) ? languageBundle.getString(behaviorName) : caption);
                  item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        try {
                           Mascot.this.setBehavior(config.buildBehavior(command));
                        } catch (Exception err) {
                           Mascot.log.log(Level.SEVERE, "Error ({0})", this);
                           String var10000 = languageBundle.getString("CouldNotSetBehaviourErrorMessage");
                           Main.showError(var10000 + "\n" + err.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                        }

                     }
                  });
                  submenu.add(item);
               }

               if (config.isBehaviorToggleable(command) && !command.contains("/")) {
                  JCheckBoxMenuItem toggleItem = new JCheckBoxMenuItem(caption, config.isBehaviorEnabled(command, this));
                  toggleItem.addItemListener((e) -> Main.getInstance().setMascotBehaviorEnabled(command, this, !config.isBehaviorEnabled(command, this)));
                  allowedSubmenu.add(toggleItem);
               }
            }
         } catch (Exception var23) {
         }
      }

      popup.add(increaseMenu);
      popup.add(Main.getInstance().characterMenu(()->{}));
      if (this.manager != null) popup.add(this.manager.getCombat().menu(this, languageBundle));
      popup.add(new JSeparator());
      popup.add(gatherMenu);
      popup.add(restoreMenu);
      if(Boolean.getBoolean("app.development"))popup.add(debugMenu);
      popup.add(new JSeparator());
      if (submenu.getMenuComponentCount() > 0) {
         popup.add(submenu);
      }

      if (allowedSubmenu.getMenuComponentCount() > 0) {
         popup.add(allowedSubmenu);
      }

      popup.add(new JSeparator());
      popup.add(pauseMenu);
      popup.add(new JSeparator());
      popup.add(disposeMenu);
      popup.add(oneMenu);
      popup.add(onlyOneMenu);
      popup.add(closeMenu);
      this.getWindow().asComponent().requestFocus();
      popup.setLightWeightPopupEnabled(false);
      popup.show(this.getWindow().asComponent(), x, y);
   }

   void tick() {
      if (this.isAnimating()) {
         boolean fighting = this.manager != null && this.manager.getCombat().active(this);
         if (this.getBehavior() != null && !fighting && !this.companionFeatures.holdBehavior()) {
            try {
               this.getBehavior().next();
            } catch (CantBeAliveException e) {
               log.log(Level.SEVERE, "Fatal Error.", e);
               String var10000 = Main.getInstance().getLanguageBundle().getString("CouldNotGetNextBehaviourErrorMessage");
               Main.showError(var10000 + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
               this.dispose();
            }

            this.setTime(this.getTime() + 1);
         }

         this.companionFeatures.tick(fighting);

         if (this.debugWindow != null) {
            this.debugWindow.setBehaviour(fighting ? this.manager.getCombat().statistics(this) : String.valueOf(this.behavior));
            this.debugWindow.setShimejiX(this.anchor.x);
            this.debugWindow.setShimejiY(this.anchor.y);
            Area activeWindow = this.environment.getActiveIE();
            this.debugWindow.setWindowTitle(this.environment.getActiveIETitle());
            this.debugWindow.setWindowX(activeWindow.getLeft());
            this.debugWindow.setWindowY(activeWindow.getTop());
            this.debugWindow.setWindowWidth(activeWindow.getWidth());
            this.debugWindow.setWindowHeight(activeWindow.getHeight());
            Area workArea = this.environment.getWorkArea();
            this.debugWindow.setEnvironmentX(workArea.getLeft());
            this.debugWindow.setEnvironmentY(workArea.getTop());
            this.debugWindow.setEnvironmentWidth(workArea.getWidth());
            this.debugWindow.setEnvironmentHeight(workArea.getHeight());
         }
      }

   }

   public void apply() {
      boolean onTop=com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true);
      if(onTop!=this.appliedAlwaysOnTop){this.appliedAlwaysOnTop=onTop;this.getWindow().setAlwaysOnTop(onTop);}
      if (this.isAnimating()) {
         if (this.getImage() != null) {
            this.getWindow().asComponent().setBounds(this.getBounds());
            this.getWindow().setImage(this.getImage().image());
            if (!this.getWindow().asComponent().isVisible()) {
               this.getWindow().asComponent().setVisible(true);
            }

            this.getWindow().updateImage();
         } else if (this.getWindow().asComponent().isVisible()) {
            this.getWindow().asComponent().setVisible(false);
         }

      }

      this.spiritualCloud.update();
   }

   private double poseRemainderX,poseRemainderY;
   public void movePose(int dx,int dy) {
      double speed=com.group_finity.mascot.settings.RuntimeSettings.number("MovementSpeed",1,.5,2);
      poseRemainderX+=dx*speed;poseRemainderY+=dy*speed;
      int x=(int)poseRemainderX,y=(int)poseRemainderY;poseRemainderX-=x;poseRemainderY-=y;
      this.setAnchor(new Point(this.anchor.x+x,this.anchor.y+y));
   }

   public void dispose() {
      com.group_finity.mascot.audio.AudioManager.global().stop(audioScope);
      if (this.manager != null) this.manager.getCombat().forget(this);
      log.log(Level.INFO, "destroy mascot ({0})", this);
      if (this.debugWindow != null) {
         this.debugWindow.setVisible(false);
         this.debugWindow = null;
      }

      this.animating = false;
      this.pendingCloudPress = null;
      this.cloudGesture.cancel();
      this.spiritualCloud.dispose();
      var disposalWindow=this.getWindow();
      if(disposalWindow.asComponent() instanceof java.awt.Window&&!SwingUtilities.isEventDispatchThread())
         SwingUtilities.invokeLater(disposalWindow::dispose);
      else disposalWindow.dispose();
      this.affordances.clear();
      if (this.getManager() != null) {
         this.getManager().remove(this);
      }

   }

   private void refreshCursor(Point position) {
      boolean useHand = false;

      for(Hotspot hotspot : this.hotspots) {
         if (hotspot.contains(this, position) && Main.getInstance().getConfiguration(this.imageSet).isBehaviorEnabled(hotspot.behaviour(), this)) {
            useHand = true;
            break;
         }
      }

      this.refreshCursor(useHand);
   }

   private void refreshCursor(Boolean useHand) {
      this.getWindow().asComponent().setCursor(Cursor.getPredefinedCursor(useHand ? 12 : 0));
   }

   public Manager getManager() {
      return this.manager;
   }

   public void setManager(Manager manager) {
      this.manager = manager;
   }

   public Point getAnchor() {
      return this.anchor;
   }

   public void setAnchor(Point anchor) {
      this.anchor = anchor;
   }

   public MascotImage getImage() {
      return this.image;
   }

   public void setImage(MascotImage image) {
      this.image = image;
   }

   public boolean isLookRight() {
      return this.lookRight;
   }

   public void setLookRight(boolean lookRight) {
      this.lookRight = lookRight;
   }

   public Rectangle getBounds() {
      if (this.getImage() != null) {
         int top = this.getAnchor().y - this.getImage().center().y;
         int left = this.getAnchor().x - this.getImage().center().x;
         return new Rectangle(left, top, this.getImage().size().width, this.getImage().size().height);
      } else {
         return this.getWindow().asComponent().getBounds();
      }
   }

   public int getTime() {
      return this.time;
   }

   private void setTime(int time) {
      this.time = time;
   }

   public Behavior getBehavior() {
      return this.behavior;
   }

   public void setBehavior(Behavior behavior) throws CantBeAliveException {
      if (this.manager != null) this.manager.getCombat().stop(this);
      this.behavior = behavior;
      this.behavior.init(this);
   }

   public int getCount() {
      return this.manager != null ? this.getManager().getCount(this.imageSet) : 0;
   }

   public int getTotalCount() {
      return this.manager != null ? this.getManager().getCount() : 0;
   }

   private boolean isAnimating() {
      return this.animating && !this.paused;
   }

   private void setAnimating(boolean animating) {
      this.animating = animating;
   }

   private TranslucentWindow getWindow() {
      return this.window;
   }

   public MascotEnvironment getEnvironment() {
      return this.environment;
   }

   public ArrayList<String> getAffordances() {
      return this.affordances;
   }

   public void setAffordance(String affordance) {
      if (affordance != null && !affordance.isEmpty() && !this.affordances.contains(affordance)) {
         this.affordances.add(affordance);
      }

   }

   public void removeAffordance(String affordance) {
      this.affordances.remove(affordance);
   }

   public void clearAffordances() {
      this.affordances.clear();
   }

   public ArrayList<Hotspot> getHotspots() {
      return this.hotspots;
   }

   public void setImageSet(String set) {
      com.group_finity.mascot.audio.AudioManager.global().stop(audioScope);
      audioScope=new com.group_finity.mascot.audio.AudioManager.Scope();
      if (this.manager != null) this.manager.getCombat().stop(this);
      this.imageSet = set;
      com.group_finity.mascot.audio.AudioManager.global().preload(set);
      this.reloadCloudMessages();
   }

   public boolean isAnimationActive() { return this.isAnimating(); }

   public int getInstanceId() { return this.id; }

   public void reloadCloudMessages() {
      this.companionFeatures.reload();
      SwingUtilities.invokeLater(() -> {
         this.pendingCloudPress = null;
         this.cloudGesture.cancel();
      });
      this.spiritualCloud.reload(this.imageSet);
   }

   public String getImageSet() {
      return this.imageSet;
   }

   public String getSound() {
      return this.sound;
   }

   public void setSound(String name) {
      this.sound = name;
   }

   public boolean isPaused() {
      return this.paused;
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public void setDragging(boolean isDragging) {
      if(isDragging!=this.dragging)playAudio(isDragging?"movement.drag.start":"movement.drag.release");
      if(isDragging&&!this.dragging)this.companionFeatures.drag();
      this.dragging = isDragging;
   }

   public boolean isHotspotClicked() {
      return this.cursor != null;
   }

   public Point getCursorPosition() {
      return this.cursor;
   }

   public void setCursorPosition(Point point) {
      this.cursor = point;
      if (point == null) {
         this.refreshCursor(false);
      } else {
         this.refreshCursor(point);
      }

   }

   public VariableMap getVariables() {
      if (this.variables == null) {
         this.variables = new VariableMap();
      }

      return this.variables;
   }
}
