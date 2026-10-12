package com.group_finity.mascot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import com.group_finity.mascot.animationeditor.AnimationEditor;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.imagesetchooser.ImageSetChooser;
import com.group_finity.mascot.license.LicenseActivationDialog;
import com.group_finity.mascot.license.LicenseChecker;
import com.group_finity.mascot.license.LicenseLevel;
import com.group_finity.mascot.license.LicenseManager;
import com.group_finity.mascot.packagemanager.ui.PackageCreatorDialog;
import com.group_finity.mascot.packagemanager.ui.PackageInstallDialog;
import com.group_finity.mascot.sound.Sounds;
import com.joconner.i18n.ConfDirectoryUtf8Control;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {
   private static final Logger log = Logger.getLogger(Main.class.getName());
   public static final Path CONFIG_DIRECTORY = AppPaths.application().resolve("conf");
   public static final Path IMAGE_DIRECTORY = AppPaths.application().resolve("img");
   public static final Path SOUND_DIRECTORY = AppPaths.application().resolve("sound");
   public static final Path SETTINGS_FILE;
   public static final Path LOGGING_FILE;
   public static final Path ICON_FILE;
   static final String BEHAVIOR_GATHER = "ChaseMouse";
   private final Manager manager = new Manager();
   private ArrayList<String> imageSets = new ArrayList<>();
   private final ConcurrentHashMap<String, Configuration> configurations = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, ArrayList<String>> childImageSets = new ConcurrentHashMap<>();
   private static final Main instance;
   private Properties properties = new Properties();
   private Platform platform;
   private ResourceBundle languageBundle;
   private JDialog form;
   private TrayIcon trayIcon;
   private final java.util.concurrent.atomic.AtomicBoolean exiting=new java.util.concurrent.atomic.AtomicBoolean();
   private volatile boolean interfaceReady, activationRequested, backgroundLaunch;
   private volatile boolean characterLoadingReady=true;
   public boolean isCharacterLoadingReady(){return characterLoadingReady;}
   private static final JFrame frame;

   private void setupFlatLafTheme() {
      String themeName = this.properties.getProperty("FlatLafTheme", "light");
      System.setProperty("flatlaf.useWindowDecorations", "true");
      System.setProperty("flatlaf.menuBarEmbedded", "false");
      switch (themeName.toLowerCase()) {
         case "dark":
            FlatDarkLaf.setup();
            break;
         case "intellij":
            FlatLightFlatIJTheme.setup();
            break;
         case "cyan":
            FlatCyanLightIJTheme.setup();
            break;
         case "solarized":
            FlatSolarizedLightIJTheme.setup();
            break;
         case "atom":
            FlatCyanLightIJTheme.setup();
            break;
         case "owl":
            FlatSolarizedLightIJTheme.setup();
            break;
         case "light":
         default:
            FlatLightFlatIJTheme.setup();
      }

      float menuScaling = Float.parseFloat(this.properties.getProperty("MenuDPI", "96")) / 96.0F;
      applyCustomThemeColors();
      com.group_finity.mascot.ui.Theme.defaults();
      if (menuScaling != 1.0F) {
         UIManager.put("defaultFont", UIManager.getFont("Label.font").deriveFont((float)UIManager.getFont("Label.font").getSize() * menuScaling));
      }

      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
      FlatLaf.updateUI();
   }

   private static void applyCustomThemeColors() {
      String baseDir = System.getProperty("app.dir");
      if (baseDir == null) {
         baseDir = System.getProperty("user.dir");
      }

      if (baseDir == null) {
         baseDir = ".";
      }

      File themeFile = AppPaths.theme().toFile();
      if (themeFile.exists()) {
         try {
            Properties themeProps = new Properties();
            themeProps.load(new FileInputStream(themeFile));
            if (themeProps.containsKey("PrimaryColour1")) {
               UIManager.put("@accentColor", Color.decode(themeProps.getProperty("PrimaryColour1", "#1EA6EB")));
            }

            if (themeProps.containsKey("PrimaryColour2")) {
               UIManager.put("Button.background", Color.decode(themeProps.getProperty("PrimaryColour2", "#28B0F5")));
            }

            if (themeProps.containsKey("PrimaryColour3")) {
               UIManager.put("Button.hoverBackground", Color.decode(themeProps.getProperty("PrimaryColour3", "#32BAFF")));
            }
         } catch (Exception e) {
            log.log(Level.WARNING, "Could not load custom theme colors, using defaults", e);
            applyDefaultThemeColors();
         }
      } else {
         applyDefaultThemeColors();
      }

   }

   private static void applyDefaultThemeColors() {
      UIManager.put("@accentColor", Color.decode("#1EA6EB"));
      UIManager.put("Button.background", Color.decode("#28B0F5"));
      UIManager.put("Button.hoverBackground", Color.decode("#32BAFF"));
   }

   public static Main getInstance() {
      return instance;
   }

   public static void showError(String message) {
      if(Thread.holdsLock(instance.manager.getMascots())){
         javax.swing.SwingUtilities.invokeLater(()->showError(message));return;
      }
      JOptionPane.showMessageDialog(frame, message, BuildInfo.NAME, 0);
   }

   private static void configureHighDPIGlobally() {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("mac")) {
         System.setProperty("apple.awt.application-appearance", "system");
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("apple.awt.application-name", "ShinobiMate");
         System.setProperty("sun.java2d.metal", "true");
         System.setProperty("sun.java2d.opengl", "false");
         System.setProperty("sun.java2d.dpiaware", "true");
         System.setProperty("sun.java2d.uiScale", "1.0");
         System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
         System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
         log.info("已配置 macOS 高DPI 支持");
      }

      System.setProperty("awt.useSystemAAFontSettings", "on");
      System.setProperty("swing.aatext", "true");
      System.setProperty("sun.java2d.renderer", "sun.java2d.marlin.MarlinRenderingEngine");
      System.setProperty("sun.java2d.renderer.useThreadLocal", "true");
      System.setProperty("sun.java2d.renderer.useRef", "true");
      System.setProperty("sun.java2d.renderer.pixelsize", "2048");
      System.setProperty("sun.awt.noerasebackground", "true");
      System.setProperty("sun.java2d.noddraw", "true");
   }

   public static void main(String[] args) {
      try {
         AppPaths.initialize();
         SingleInstance guard=SingleInstance.acquire(AppPaths.userData());
         if(guard==null){
            if(!SingleInstance.activate(AppPaths.userData()))JOptionPane.showMessageDialog(null,"تعذر إظهار نافذة ShinobiMate الحالية. راجع أيقونة علبة النظام أو سجل الأخطاء.\nCould not show the running ShinobiMate window. Check its tray icon or error log.",BuildInfo.NAME,JOptionPane.WARNING_MESSAGE);
            System.exit(0);return;
         }
         Runtime.getRuntime().addShutdownHook(new Thread(()->{try{guard.close();}catch(IOException ignored){}}));
         ProductionLogging.initialize();
         getInstance().backgroundLaunch=Arrays.asList(args).contains("--background");
         guard.listen(getInstance()::requestManager);
         configureHighDPIGlobally();
         getInstance().run();
      } catch(OutOfMemoryError error) {
         log.log(Level.SEVERE,"Insufficient memory",error);showError("ShinobiMate: insufficient memory / الذاكرة غير كافية");System.exit(1);
      } catch(Exception error) {
         log.log(Level.SEVERE,"Application startup failed",error);
         showError("تعذر بدء ShinobiMate. تحقق من صلاحية مجلد بيانات المستخدم وسجل الأخطاء.\nCould not start ShinobiMate. Check access to the user data folder and logs.");
         System.exit(1);
      }

   }

   public void run() {
      this.characterLoadingReady=false;
      if (!System.getProperty("sun.arch.data.model").equals("64")) {
         this.platform = Platform.x86;
      } else {
         this.platform = Platform.x86_64;
      }

      try {this.properties=com.group_finity.mascot.settings.SettingsStore.load();}
      catch(IOException ex){log.log(Level.SEVERE,"Settings could not be read",ex);throw new IllegalStateException("Could not read user settings",ex);}
      boolean firstRun=!Boolean.parseBoolean(this.properties.getProperty("OnboardingComplete","false"));
      this.imageSets.clear();
      this.properties.setProperty("ActiveShimeji","");

      DPIManager.autoConfigureDPI(this.properties);
      this.updateConfigFile();

      try {
         this.languageBundle = this.loadLanguageBundle();
      } catch (Exception var9) {
         showError("The default language file could not be loaded. Ensure that you have the latest ShinobiMate language.properties in your conf directory.");
         this.exit();
      }

      try {
         this.setupFlatLafTheme();
      } catch (Exception var8) {
         try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         } catch (Exception ex1) {
            log.log(Level.SEVERE, "Look & Feel unsupported.", ex1);
            this.exit();
         }
      }

      if(firstRun&&!this.showInitialOnboarding()){this.exit();return;}
      this.getManager().setExitOnLastRemoved(false);
      this.createTrayIcon();
      LicenseManager licenseManager = LicenseManager.getInstance();
      log.info("License System Initialized - Current Level: " + licenseManager.getCurrentLicenseLevel().getDisplayName());
      LicenseChecker.showExpirationWarningIfNeeded();

      java.util.List<String> restore=com.group_finity.mascot.settings.CharacterSession.restore(this.properties,firstRun);
      if(!restore.isEmpty()) {
         var installed=new com.group_finity.mascot.character.CharacterCatalog().list().stream()
               .map(com.group_finity.mascot.character.CharacterDescriptor::id).collect(java.util.stream.Collectors.toSet());
         for(String id:restore) {
            if(installed.contains(id)&&this.prepareCharacter(id))this.createMascot(id);
            else log.log(Level.WARNING,"Skipping unavailable saved character: {0}",id);
         }
      }

      this.getManager().start();
      this.characterLoadingReady=true;
      this.interfaceReady=true;
      this.requestManager();
      RuntimeDiagnostics.start(this.manager);
   }

   private boolean showInitialOnboarding() {
      var completed=new java.util.concurrent.atomic.AtomicBoolean();
      Runnable show=()->{var guide=new com.group_finity.mascot.ui.OnboardingDialog(frame,false);guide.setVisible(true);completed.set(guide.completed());};
      try{if(SwingUtilities.isEventDispatchThread())show.run();else SwingUtilities.invokeAndWait(show);}
      catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Onboarding interrupted",ex);}
      catch(java.lang.reflect.InvocationTargetException ex){throw new IllegalStateException("Onboarding failed",ex.getCause());}
      return completed.get();
   }

   public void requestManager() {
      this.activationRequested=true;
      if(this.interfaceReady&&!this.exiting.get())javax.swing.SwingUtilities.invokeLater(com.group_finity.mascot.ui.CharacterManagerWindow::open);
   }

   private synchronized boolean loadConfiguration(String imageSet) {
      try {
         String baseDir = System.getProperty("app.dir");
         if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
         }

         if (baseDir == null) {
            baseDir = ".";
         }

         Path pack=AppPaths.pack(imageSet);
         com.group_finity.mascot.pack.PackValidator.validate(pack,imageSet);
         String filePath = pack.resolve("conf").toString()+File.separator;
         String actionsFile = filePath + "actions.xml";
         log.log(Level.INFO, imageSet + " Read Action File ({0})", actionsFile);
         Document actions = com.group_finity.mascot.pack.SecureXml.read(java.nio.file.Path.of(actionsFile));
         Configuration configuration = new Configuration();
         configuration.load(new Entry(actions.getDocumentElement()), imageSet);
         String behaviorsFile = filePath + "behaviors.xml";
         log.log(Level.INFO, imageSet + " Read Behavior File ({0})", behaviorsFile);
         Document behaviors = com.group_finity.mascot.pack.SecureXml.read(java.nio.file.Path.of(behaviorsFile));
         configuration.load(new Entry(behaviors.getDocumentElement()), imageSet);
         String infoFile = filePath + "info.xml";
         if ((new File(infoFile)).exists()) {
            log.log(Level.INFO, imageSet + " Read Information File ({0})", infoFile);
            Document information = com.group_finity.mascot.pack.SecureXml.read(java.nio.file.Path.of(infoFile));
            configuration.load(new Entry(information.getDocumentElement()), imageSet);
         }

         configuration.validate();
         this.configurations.put(imageSet, configuration);
         this.getManager().getCombat().reload(imageSet);
         synchronized (this.getManager().getMascots()) {
            for (Mascot mascot : this.getManager().getMascots()) {
               if (mascot.getImageSet().equals(imageSet)) mascot.reloadCloudMessages();
            }
         }
         ArrayList<String> childMascots = new ArrayList();

         for(Entry list : (new Entry(actions.getDocumentElement())).selectChildren("ActionList")) {
            for(Entry node : list.selectChildren("Action")) {
               if (node.getAttributes().containsKey("BornMascot")) {
                  String set = node.getAttribute("BornMascot");
                  if (!childMascots.contains(set)) {
                     childMascots.add(set);
                  }

                  if (!this.configurations.containsKey(set)) {
                     this.loadConfiguration(set);
                  }
               }

               if (node.getAttributes().containsKey("TransformMascot")) {
                  String set = node.getAttribute("TransformMascot");
                  if (!childMascots.contains(set)) {
                     childMascots.add(set);
                  }

                  if (!this.configurations.containsKey(set)) {
                     this.loadConfiguration(set);
                  }
               }
            }
         }

         this.childImageSets.put(imageSet, childMascots);
         return true;
      } catch (ConfigurationException | ParserConfigurationException | SAXException e) {
         log.log(Level.SEVERE, "Failed to load configuration files", e);
         String var20 = this.languageBundle.getString("FailedLoadConfigErrorMessage");
         showError(var20 + "\n" + ((Exception)e).getMessage() + "\n" + this.languageBundle.getString("SeeLogForDetails"));
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to load configuration files", e);
         String var10000 = this.languageBundle.getString("FailedLoadConfigErrorMessage");
         showError(var10000 + "\n" + e.getMessage() + "\n" + this.languageBundle.getString("SeeLogForDetails"));
      }

      return false;
   }

   private static BufferedImage loadIconImage(String iconPath) throws IOException {
      String normalizedPath = iconPath.startsWith("/") ? iconPath.substring(1) : iconPath;
      normalizedPath = normalizedPath.replace('/', File.separatorChar);
      BufferedImage rawImage = null;
      String externalPath = AppPaths.image(normalizedPath.replace(File.separatorChar,'/')).toString();
      File externalFile = new File(externalPath);
      if (externalFile.exists()) {
         try {
            InputStream is = new FileInputStream(externalFile);

            try {
               rawImage = ImageIO.read(is);
               log.log(Level.INFO, "Successfully loaded external icon from: " + externalPath);
            } catch (Throwable var11) {
               try {
                  is.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            is.close();
         } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load external icon from: " + externalPath, e);
         }
      }

      if (rawImage == null) {
         InputStream resourceStream = Main.class.getResourceAsStream(iconPath);
         if (resourceStream != null) {
            try {
               InputStream is = resourceStream;

               try {
                  rawImage = ImageIO.read(is);
                  log.log(Level.INFO, "Successfully loaded resource icon from: " + iconPath);
               } catch (Throwable var13) {
                  if (resourceStream != null) {
                     try {
                        is.close();
                     } catch (Throwable var9) {
                        var13.addSuppressed(var9);
                     }
                  }

                  throw var13;
               }

               if (resourceStream != null) {
                  resourceStream.close();
               }
            } catch (IOException e) {
               log.log(Level.WARNING, "Failed to load resource icon from: " + iconPath, e);
            }
         } else {
            log.log(Level.WARNING, "Resource not found: " + iconPath);
         }
      }

      return rawImage != null ? optimizeIconForTray(rawImage) : null;
   }

   private static BufferedImage optimizeIconForTray(BufferedImage original) {
      if (original == null) {
         return null;
      } else {
         Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
         int targetSize = Math.max(traySize.width, traySize.height);
         if (original.getWidth() == targetSize && original.getHeight() == targetSize) {
            return original;
         } else {
            BufferedImage optimized = new BufferedImage(targetSize, targetSize, 2);
            Graphics2D g2d = optimized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(original, 0, 0, targetSize, targetSize, (ImageObserver)null);
            g2d.dispose();
            return optimized;
         }
      }
   }

   private static boolean isMacOS() {
      return System.getProperty("os.name").toLowerCase().contains("mac");
   }

   private static boolean isWaylandEnvironment() {
      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
      String sessionType = System.getenv("XDG_SESSION_TYPE");
      return waylandDisplay != null && !waylandDisplay.isEmpty() || sessionType != null && sessionType.equals("wayland");
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

   private void createTrayIcon() {
      log.log(Level.INFO, "create a tray icon");
      Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
      BufferedImage image = optimizeIconForTray(com.group_finity.mascot.ui.AppIcon.trayImage(
            Math.max(traySize.width, traySize.height)));

      if (image == null) {
         try {
            image = loadIconImage("/icon.png");
            if (image != null) {
               log.log(Level.INFO, "Successfully loaded PNG tray icon");
            }
         } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load PNG fallback", e);
         }
      }

      if (image == null) {
         log.log(Level.WARNING, "Creating default tray icon");
         image = createDefaultTrayIcon();
      }

      try {
         String caption = this.properties.getProperty("ShimejiEENameOverride", "").trim();
         if (caption.isEmpty()) {
            caption = this.languageBundle.getString("ShimejiEE");
         }

         final TrayIcon icon = new TrayIcon(image, caption);
         icon.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent event) {
               if(javax.swing.SwingUtilities.isLeftMouseButton(event))Main.this.requestManager();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent event) {
               if (Main.shouldShowPopupMenu(event)) {
                  if (Main.this.form != null) {
                     Main.this.form.dispose();
                  }

                  Main.this.form = new JDialog(Main.frame, false);
                  JPanel panel = new JPanel();
                  panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                  Main.this.form.add(panel);
                  JButton btnCallShimeji = new JButton(com.group_finity.mascot.ui.Theme.t("إضافة شخصية","Add character"));
                  btnCallShimeji.addActionListener((e) -> {
                     Main.this.characterMenu(()->Main.this.form.dispose()).getPopupMenu().show(btnCallShimeji,0,btnCallShimeji.getHeight());
                  });
                  JButton btnFollowCursor = new JButton(Main.this.languageBundle.getString("FollowCursor"));
                  btnFollowCursor.addActionListener((e) -> {
                     Main.this.getManager().setBehaviorAll("ChaseMouse");
                     Main.this.form.dispose();
                  });
                  JButton btnReduceToOne = new JButton(Main.this.languageBundle.getString("ReduceToOne"));
                  btnReduceToOne.addActionListener((e) -> {
                     Main.this.getManager().remainOne();
                     Main.this.form.dispose();
                  });
                  JButton btnRestoreWindows = new JButton(Main.this.languageBundle.getString("RestoreWindows"));
                  btnRestoreWindows.addActionListener((e) -> {
                     NativeFactory.getInstance().getEnvironment().restoreIE();
                     Main.this.form.dispose();
                  });
                  final JButton btnAllowedBehaviours = new JButton(Main.this.languageBundle.getString("AllowedBehaviours"));
                  btnAllowedBehaviours.addMouseListener(new MouseListener() {
                     public void mouseClicked(MouseEvent e) {
                     }

                     public void mousePressed(MouseEvent e) {
                     }

                     public void mouseReleased(MouseEvent e) {
                        btnAllowedBehaviours.setEnabled(true);
                     }

                     public void mouseEntered(MouseEvent e) {
                     }

                     public void mouseExited(MouseEvent e) {
                     }
                  });
                  btnAllowedBehaviours.addActionListener((event3) -> {
                     JCheckBoxMenuItem breedingMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("BreedingCloning"), Boolean.parseBoolean(Main.this.properties.getProperty("Breeding", "true")));
                     breedingMenu.addItemListener((e) -> {
                        breedingMenu.setState(Main.this.toggleBooleanSetting("Breeding"));
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JCheckBoxMenuItem transientMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("BreedingTransient"), Boolean.parseBoolean(Main.this.properties.getProperty("Transients", "true")));
                     transientMenu.addItemListener((e) -> {
                        transientMenu.setState(Main.this.toggleBooleanSetting("Transients"));
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JCheckBoxMenuItem transformationMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("Transformation"), Boolean.parseBoolean(Main.this.properties.getProperty("Transformation", "true")));
                     transformationMenu.addItemListener((e) -> {
                        transformationMenu.setState(Main.this.toggleBooleanSetting("Transformation"));
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JCheckBoxMenuItem throwingMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("ThrowingWindows"), Boolean.parseBoolean(Main.this.properties.getProperty("Throwing", "true")));
                     throwingMenu.addItemListener((e) -> {
                        throwingMenu.setState(Main.this.toggleBooleanSetting("Throwing"));
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JCheckBoxMenuItem soundsMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("SoundEffects"), Boolean.parseBoolean(Main.this.properties.getProperty("Sounds", "true")));
                     soundsMenu.addItemListener((e) -> {
                        boolean result = Main.this.toggleBooleanSetting("Sounds");
                        soundsMenu.setState(result);
                        Sounds.setMuted(!result);
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JCheckBoxMenuItem multiscreenMenu = new JCheckBoxMenuItem(Main.this.languageBundle.getString("Multiscreen"), Boolean.parseBoolean(Main.this.properties.getProperty("Multiscreen", "true")));
                     multiscreenMenu.addItemListener((e) -> {
                        multiscreenMenu.setState(Main.this.toggleBooleanSetting("Multiscreen"));
                        Main.this.updateConfigFile();
                        btnAllowedBehaviours.setEnabled(true);
                     });
                     JPopupMenu behaviourPopup = new JPopupMenu();
                     behaviourPopup.add(breedingMenu);
                     behaviourPopup.add(transientMenu);
                     behaviourPopup.add(transformationMenu);
                     behaviourPopup.add(throwingMenu);
                     behaviourPopup.add(soundsMenu);
                     behaviourPopup.add(multiscreenMenu);
                     behaviourPopup.addPopupMenuListener(new PopupMenuListener() {
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        }

                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                           if (panel.getMousePosition() != null) {
                              btnAllowedBehaviours.setEnabled(panel.getMousePosition().x <= btnAllowedBehaviours.getX() || panel.getMousePosition().x >= btnAllowedBehaviours.getX() + btnAllowedBehaviours.getWidth() || panel.getMousePosition().y <= btnAllowedBehaviours.getY() || panel.getMousePosition().y >= btnAllowedBehaviours.getY() + btnAllowedBehaviours.getHeight());
                           } else {
                              btnAllowedBehaviours.setEnabled(true);
                           }

                        }

                        public void popupMenuCanceled(PopupMenuEvent e) {
                        }
                     });
                     behaviourPopup.show(btnAllowedBehaviours, 0, btnAllowedBehaviours.getHeight());
                     btnAllowedBehaviours.requestFocusInWindow();
                  });
                  JButton btnChooseShimeji = new JButton(Main.this.languageBundle.getString("ChooseShimeji"));
                  btnChooseShimeji.addActionListener((event2) -> {
                     Main.this.form.dispose();
                     com.group_finity.mascot.ui.CharacterManagerWindow.open();
                  });
                  JButton btnSettings = new JButton(Main.this.languageBundle.getString("Settings"));
                  btnSettings.addActionListener((event1) -> {
                     Main.this.form.dispose();
                     new com.group_finity.mascot.ui.SettingsDialog(Main.frame).setVisible(true);
                  });
                  JButton btnAnimationEditor = new JButton(Main.this.languageBundle.getString("AnimationEditor"));
                  LicenseLevel currentLevel = LicenseManager.getInstance().getCurrentLicenseLevel();
                  if (currentLevel != LicenseLevel.SPECIAL_KEY) {
                     String var10001 = Main.this.languageBundle.getString("AnimationEditor");
                     btnAnimationEditor.setText(var10001 + " (" + Main.this.languageBundle.getString("SpecialLicenseRequired") + ")");
                     btnAnimationEditor.setEnabled(false);
                  }

                  btnAnimationEditor.addActionListener((event1) -> {
                     if (LicenseChecker.checkSpecialFeature(true)) {
                        Main.this.form.dispose();
                        AnimationEditor.launch();
                     }
                  });
                  JButton btnInstallPackage = new JButton(Main.this.languageBundle.getString("InstallPackage"));
                  btnInstallPackage.addActionListener((e) -> {
                     Main.this.form.dispose();
                     com.group_finity.mascot.ui.PackImportDialog.choose(Main.frame);
                  });
                  JButton btnCreatePackage = new JButton(Main.this.languageBundle.getString("CreatePackage"));
                  btnCreatePackage.addActionListener((e) -> {
                     Main.this.form.dispose();
                     PackageCreatorDialog.showCreatorDialog(Main.frame);
                  });
                  final JButton btnLanguage = new JButton(Main.this.languageBundle.getString("Language"));
                  btnLanguage.addMouseListener(new MouseListener() {
                     public void mouseClicked(MouseEvent e) {
                     }

                     public void mousePressed(MouseEvent e) {
                     }

                     public void mouseReleased(MouseEvent e) {
                        btnLanguage.setEnabled(true);
                     }

                     public void mouseEntered(MouseEvent e) {
                     }

                     public void mouseExited(MouseEvent e) {
                     }
                  });
                  btnLanguage.addActionListener((e) -> {
                     JMenuItem englishMenu = new JMenuItem("English");
                     englishMenu.addActionListener((e1) -> {
                        Main.this.form.dispose();
                        Main.this.updateLanguage("en-GB");
                        Main.this.updateConfigFile();
                     });
                     JMenuItem chineseMenu = new JMenuItem("简体中文");
                     chineseMenu.addActionListener((e2) -> {
                        Main.this.form.dispose();
                        Main.this.updateLanguage("zh-CN");
                        Main.this.updateConfigFile();
                     });
                     JPopupMenu languagePopup = new JPopupMenu();
                     languagePopup.add(englishMenu);
                     languagePopup.addSeparator();
                     languagePopup.add(chineseMenu);
                     languagePopup.addPopupMenuListener(new PopupMenuListener() {
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        }

                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                           if (panel.getMousePosition() != null) {
                              btnLanguage.setEnabled(panel.getMousePosition().x <= btnLanguage.getX() || panel.getMousePosition().x >= btnLanguage.getX() + btnLanguage.getWidth() || panel.getMousePosition().y <= btnLanguage.getY() || panel.getMousePosition().y >= btnLanguage.getY() + btnLanguage.getHeight());
                           } else {
                              btnLanguage.setEnabled(true);
                           }

                        }

                        public void popupMenuCanceled(PopupMenuEvent e) {
                        }
                     });
                     languagePopup.show(btnLanguage, 0, btnLanguage.getHeight());
                     btnLanguage.requestFocusInWindow();
                  });
                  JButton btnAutoStart = new JButton();
                  Main.this.updateAutoStartButtonText(btnAutoStart);
                  btnAutoStart.addActionListener((e) -> {
                     boolean currentStatus = Main.AutoStartManager.isAutoStartEnabled();
                     boolean success = Main.AutoStartManager.setAutoStart(!currentStatus);
                     if (success) {
                        Main.this.updateAutoStartButtonText(btnAutoStart);
                        String message = currentStatus ? Main.this.languageBundle.getString("AutoStartDisabled") : Main.this.languageBundle.getString("AutoStartEnabled");
                        Main.this.showInfo(message);
                     } else {
                        Main.showError(Main.this.languageBundle.getString("AutoStartError"));
                     }

                  });
                  JButton btnLicense = new JButton();
                  Main.this.updateLicenseButtonText(btnLicense);
                  btnLicense.addActionListener((e) -> {
                     Main.this.form.dispose();
                     LicenseActivationDialog.showDialog(Main.frame, Main.this.languageBundle);
                     Main.this.updateLicenseButtonText(btnLicense);
                  });
                  JButton btnPauseAll = new JButton(Main.this.getManager().isPaused() ? Main.this.languageBundle.getString("ResumeAnimations") : Main.this.languageBundle.getString("PauseAnimations"));
                  btnPauseAll.addActionListener((e) -> {
                     Main.this.form.dispose();
                     Main.this.getManager().togglePauseAll();
                  });
                  JButton btnDismissAll = new JButton(Main.this.languageBundle.getString("DismissAll"));
                  btnDismissAll.addActionListener((e) -> Main.this.exit());
                  float scaling = Float.parseFloat(Main.this.properties.getProperty("MenuDPI", "96")) / 96.0F;
                  panel.setLayout(new GridBagLayout());
                  GridBagConstraints gridBag = new GridBagConstraints();
                  gridBag.fill = 2;
                  gridBag.gridx = 0;
                  gridBag.gridy = 0;
                  panel.add(btnCallShimeji, gridBag);
                  gridBag.insets = new Insets((int)(5.0F * scaling), 0, 0, 0);
                  ++gridBag.gridy;
                  panel.add(btnFollowCursor, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnReduceToOne, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnRestoreWindows, gridBag);
                  ++gridBag.gridy;
                  panel.add(new JSeparator(), gridBag);
                  ++gridBag.gridy;
                  panel.add(btnAllowedBehaviours, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnChooseShimeji, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnSettings, gridBag);
                  ++gridBag.gridy;
                  if(Boolean.getBoolean("app.development"))panel.add(btnAnimationEditor, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnInstallPackage, gridBag);
                  ++gridBag.gridy;
                  if(Boolean.getBoolean("app.development"))panel.add(btnCreatePackage, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnLanguage, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnAutoStart, gridBag);
                  ++gridBag.gridy;
                  if(Boolean.getBoolean("app.development"))panel.add(btnLicense, gridBag);
                  ++gridBag.gridy;
                  panel.add(new JSeparator(), gridBag);
                  ++gridBag.gridy;
                  panel.add(btnPauseAll, gridBag);
                  ++gridBag.gridy;
                  panel.add(btnDismissAll, gridBag);
                  com.group_finity.mascot.ui.AppIcon.apply(Main.this.form);
                  Main.this.form.setTitle(Main.this.languageBundle.getString("ShimejiEE"));
                  Main.this.form.setDefaultCloseOperation(2);
                  Main.this.form.setAlwaysOnTop(true);
                  Main.this.setupTrayMenuAutoSizing(Main.this.form, panel, scaling, icon, event, btnCallShimeji, btnFollowCursor, btnReduceToOne, btnRestoreWindows, btnAllowedBehaviours, btnChooseShimeji, btnSettings, btnAnimationEditor, btnInstallPackage, btnCreatePackage, btnLanguage, btnAutoStart, btnLicense, btnPauseAll, btnDismissAll);
                  Main.this.form.setMinimumSize(Main.this.form.getSize());
               } else if (event.getButton() == 1) {
                  Main.this.requestManager();
               } else if (event.getButton() == 2 && event.getClickCount() == 2) {
                  Main.this.requestManager();
               }

            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
         });
         if (SystemTray.isSupported()) {
            SystemTray.getSystemTray().add(icon);
            RuntimeDiagnostics.event("TRAY_CREATED");
            this.trayIcon = icon;
            log.log(Level.INFO, "System tray icon created successfully");
         } else {
            log.log(Level.WARNING, "System tray is not supported on this platform (possibly Wayland)");
            this.trayIcon = icon;
            if (isWaylandEnvironment()) {
               log.info("注意：在Wayland环境下，系统托盘功能不可用。");
               log.info("您仍然可以通过右键点击桌宠来访问菜单功能。");
            } else {
               log.info("注意：系统托盘功能在当前环境下不可用。");
            }
         }
      } catch (UnsupportedOperationException e) {
         log.log(Level.WARNING, "System tray not supported in Wayland environment", e);
         this.trayIcon = null;
         log.info("注意：在Wayland环境下，系统托盘功能不可用。");
         log.info("您仍然可以通过右键点击桌宠来访问菜单功能。");
      } catch (AWTException e) {
         log.log(Level.SEVERE, "Failed to create tray icon", e);
         String var10000 = this.languageBundle.getString("FailedDisplaySystemTrayErrorMessage");
         showError(var10000 + "\n" + this.languageBundle.getString("SeeLogForDetails"));
         this.exit();
      }

   }

   private static BufferedImage createDefaultTrayIcon() {
      int size = 16;

      try {
         Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
         size = Math.max(traySize.width, traySize.height);
      } catch (Exception var3) {
      }

      BufferedImage image = new BufferedImage(size, size, 2);
      Graphics2D g2d = image.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setColor(Color.BLUE);
      g2d.fillOval(2, 2, size - 4, size - 4);
      g2d.setColor(Color.WHITE);
      g2d.drawOval(2, 2, size - 4, size - 4);
      g2d.dispose();
      return image;
   }

   public void createMascot() {
      this.requestManager();
   }

   public javax.swing.JMenu characterMenu(Runnable selected) {
      var menu=new javax.swing.JMenu(com.group_finity.mascot.ui.Theme.t("إضافة شخصية","Add character"));menu.setName("AddCharacterMenu");
      for(var pack:new com.group_finity.mascot.character.CharacterService().installed()) {
         var item=new JMenuItem(pack.displayName());item.setName("Summon."+pack.id());
         item.addActionListener(e->{com.group_finity.mascot.audio.AudioManager.play("ui.character.select");selected.run();com.group_finity.mascot.character.CharacterService.launchFromMenu(pack.id());});menu.add(item);
      }
      menu.setEnabled(menu.getItemCount()>0);return menu;
   }

   public void createMascot(String imageSet) {
      log.log(Level.INFO, "create a mascot");
      Mascot mascot = new Mascot(imageSet);
      mascot.setAnchor(new Point(-4000, -4000));
      mascot.setLookRight(Math.random() < (double)0.5F);

      try {
         mascot.setBehavior(this.getConfiguration(imageSet).buildNextBehavior((String)null, mascot));
         this.getManager().add(mascot);
         com.group_finity.mascot.audio.AudioManager.play("ui.character.spawn");
      } catch (BehaviorInstantiationException e) {
         log.log(Level.SEVERE, "Failed to initialize the first action", e);
         String var8 = this.languageBundle.getString("FailedInitialiseFirstActionErrorMessage");
         showError(var8 + "\n" + e.getMessage() + "\n" + this.languageBundle.getString("SeeLogForDetails"));
         mascot.dispose();
      } catch (CantBeAliveException e) {
         log.log(Level.SEVERE, "Fatal Error", e);
         String var7 = this.languageBundle.getString("FailedInitialiseFirstActionErrorMessage");
         showError(var7 + "\n" + e.getMessage() + "\n" + this.languageBundle.getString("SeeLogForDetails"));
         mascot.dispose();
      } catch (Exception e) {
         log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
         String var10000 = this.languageBundle.getString("CouldNotCreateShimejiErrorMessage");
         showError(var10000 + " " + imageSet + ".\n" + e.getMessage() + "\n" + this.languageBundle.getString("SeeLogForDetails"));
         mascot.dispose();
      }

   }

   private ResourceBundle loadLanguageBundle() {
      String baseDir = System.getProperty("app.dir");
      if (baseDir == null) {
         baseDir = System.getProperty("user.dir");
      }

      if (baseDir == null) {
         baseDir = ".";
      }

      String language = this.properties.getProperty("Language", "ar").trim();
      if (language.isEmpty()) {
         language = "ar";
      }

      ResourceBundle.clearCache(Thread.currentThread().getContextClassLoader());
      return ResourceBundle.getBundle("language", Locale.forLanguageTag(language), new ConfDirectoryUtf8Control(new File(baseDir, "conf")));
   }

   private void refreshLanguage() {
      this.languageBundle = this.loadLanguageBundle();
      var active=com.group_finity.mascot.settings.CharacterSession.snapshot(this.manager);
      boolean isExit = this.getManager().isExitOnLastRemoved();
      this.getManager().setExitOnLastRemoved(false);
      this.getManager().disposeAll();

      for(String imageSet : this.imageSets) {
         this.loadConfiguration(imageSet);
      }

      for(String imageSet : active) {
         this.createMascot(imageSet);
      }

      this.getManager().setExitOnLastRemoved(isExit);
   }

   private void updateLanguage(String language) {
      if (!this.properties.getProperty("Language", "ar").equals(language)) {
         this.properties.setProperty("Language", language);
         this.refreshLanguage();
      }

   }

   private boolean toggleBooleanSetting(String propertyName) {
      if (Boolean.parseBoolean(this.properties.getProperty(propertyName, "true"))) {
         this.properties.setProperty(propertyName, "false");
         return false;
      } else {
         this.properties.setProperty(propertyName, "true");
         return true;
      }
   }

   private void setMascotInformationDismissed(String imageSet) {
      ArrayList<String> list = new ArrayList();
      String[] data = this.properties.getProperty("InformationDismissed", "").split("/");
      if (data.length > 0 && !data[0].isEmpty()) {
         list.addAll(Arrays.asList(data));
      }

      if (!list.contains(imageSet)) {
         list.add(imageSet);
      }

      this.properties.setProperty("InformationDismissed", list.toString().replace("[", "").replace("]", "").replace(", ", "/"));
   }

   public void setMascotBehaviorEnabled(String name, Mascot mascot, boolean enabled) {
      ArrayList<String> list = new ArrayList();
      String[] data = this.properties.getProperty("DisabledBehaviours." + mascot.getImageSet(), "").split("/");
      if (data.length > 0 && !data[0].isEmpty()) {
         list.addAll(Arrays.asList(data));
      }

      if (list.contains(name) && enabled) {
         list.remove(name);
      } else if (!list.contains(name) && !enabled) {
         list.add(name);
      }

      if (!list.isEmpty()) {
         this.properties.setProperty("DisabledBehaviours." + mascot.getImageSet(), list.toString().replace("[", "").replace("]", "").replace(", ", "/"));
      } else {
         this.properties.remove("DisabledBehaviours." + mascot.getImageSet());
      }

      this.updateConfigFile();
   }

   private void updateConfigFile() {
      try {
         com.group_finity.mascot.settings.SettingsStore.save(this.properties,
               com.group_finity.mascot.settings.SettingsStore.path());
      } catch (Exception var7) {
         log.log(Level.WARNING,"Could not save settings",var7);
      }

   }

   public void saveConfigFile() {
      this.updateConfigFile();
   }

   public void refreshProductLanguage() {
      this.languageBundle=this.loadLanguageBundle();
      this.reloadAllCloudMessages();
   }

   public void reloadAllCloudMessages(){
      synchronized(this.manager.getMascots()){for(Mascot mascot:this.manager.getMascots())mascot.reloadCloudMessages();}
   }

   public void refreshProductTheme() {
      this.setupFlatLafTheme();
      for(java.awt.Window window:java.awt.Window.getWindows()) javax.swing.SwingUtilities.updateComponentTreeUI(window);
   }

   public void reloadAppearance() {
      synchronized(this.manager.getMascots()) {
         ImagePairs.clear();
         for(String set:new ArrayList<String>(this.imageSets))this.loadConfiguration(set);
         for(Mascot mascot:this.manager.getMascots()) {
            try {mascot.setBehavior(this.getConfiguration(mascot.getImageSet()).buildBehavior(
                  this.getConfiguration(mascot.getImageSet()).getSchema().getString("Fall"),mascot));}
            catch(Exception ex){log.log(Level.WARNING,"Appearance reload failed",ex);}
         }
      }
   }

   public void openAdvancedSettings() {
      SettingsWindow dialog=new SettingsWindow(frame,true);dialog.init();dialog.display();
      if(dialog.getEnvironmentReloadRequired()) NativeFactory.getInstance().getEnvironment().dispose();
      if(dialog.getImageReloadRequired()||dialog.getEnvironmentReloadRequired())this.reloadAppearance();
      if(dialog.getInteractiveWindowReloadRequired()) NativeFactory.getInstance().getEnvironment().refreshCache();
   }

   public void openLegacyChooser() {
      this.setActiveImageSets(new ImageSetChooser(frame,true).display());
   }

   public synchronized boolean prepareCharacter(String id) {
      if(!this.configurations.containsKey(id)&&!this.loadConfiguration(id))return false;
      if(!this.imageSets.contains(id))this.imageSets.add(id);
      return true;
   }

   /** Legacy chooser sees only the live session; resource loading cannot make a pack active. */
   public void activeCharactersChanged(Manager owner) {
      if(owner==this.manager&&!this.exiting.get())this.properties.setProperty("ActiveShimeji",
            String.join("/",new java.util.LinkedHashSet<>(com.group_finity.mascot.settings.CharacterSession.snapshot(owner))));
   }

   public void saveCharacterSession() {
      this.properties.setProperty(com.group_finity.mascot.settings.CharacterSession.HISTORY,
            String.join("/",com.group_finity.mascot.settings.CharacterSession.snapshot(this.manager)));
      this.saveConfigFile();
   }

   public boolean reloadCharacter(String id) {
      synchronized(this.manager.getMascots()) {
         this.manager.getCombat().reload(id);ImagePairs.removeAll(id);
         if(!this.loadConfiguration(id))return false;
         for(Mascot mascot:this.manager.getMascots())if(mascot.getImageSet().equals(id)) {
            try {mascot.setBehavior(this.getConfiguration(id).buildBehavior(this.getConfiguration(id).getSchema().getString("Fall"),mascot));}
            catch(Exception ex){log.log(Level.WARNING,"Pack reload failed",ex);return false;}
         }
         return true;
      }
   }

   private void setActiveImageSets(ArrayList<String> newImageSets) {
      if (newImageSets != null) {
         var active=new java.util.LinkedHashSet<>(com.group_finity.mascot.settings.CharacterSession.snapshot(this.manager));
         ArrayList<String> toRemove = new ArrayList<>(active);
         toRemove.removeAll(newImageSets);
         ArrayList<String> toAdd = new ArrayList<>();
         ArrayList<String> toRetain = new ArrayList<>();

         for(String set : newImageSets) {
            if (!active.contains(set)) {
               toAdd.add(set);
            }

            if (!toRetain.contains(set)) {
               toRetain.add(set);
            }

            this.populateArrayListWithChildSets(set, toRetain);
         }

         boolean isExit = this.getManager().isExitOnLastRemoved();
         this.getManager().setExitOnLastRemoved(false);

         for(String r : toRemove) {
            this.removeLoadedImageSet(r, toRetain);
         }

         for(String a : toAdd) {
            this.addImageSet(a);
         }

         this.getManager().setExitOnLastRemoved(isExit);
      }
   }

   private void populateArrayListWithChildSets(String imageSet, ArrayList<String> childList) {
      if (this.childImageSets.containsKey(imageSet)) {
         for(String set : this.childImageSets.get(imageSet)) {
            if (!childList.contains(set)) {
               this.populateArrayListWithChildSets(set, childList);
               childList.add(set);
            }
         }
      }

   }

   private void removeLoadedImageSet(String imageSet, ArrayList<String> setsToIgnore) {
      if (this.childImageSets.containsKey(imageSet)) {
         for(String set : this.childImageSets.get(imageSet)) {
            if (!setsToIgnore.contains(set)) {
               setsToIgnore.add(set);
               this.imageSets.remove(imageSet);
               this.getManager().remainNone(imageSet);
               this.configurations.remove(imageSet);
               ImagePairs.removeAll(imageSet);
               this.removeLoadedImageSet(set, setsToIgnore);
            }
         }
      }

      if (!setsToIgnore.contains(imageSet)) {
         this.imageSets.remove(imageSet);
         this.getManager().remainNone(imageSet);
         this.configurations.remove(imageSet);
         ImagePairs.removeAll(imageSet);
      }

   }

   private void addImageSet(String imageSet) {
      if (this.configurations.containsKey(imageSet)) {
         if(!this.imageSets.contains(imageSet))this.imageSets.add(imageSet);
         this.createMascot(imageSet);
      } else if (this.loadConfiguration(imageSet)) {
         if(!this.imageSets.contains(imageSet))this.imageSets.add(imageSet);
         String informationAlreadySeen = this.properties.getProperty("InformationDismissed", "");
         if (((Configuration)this.configurations.get(imageSet)).containsInformationKey("SplashImage") && (Boolean.parseBoolean(this.properties.getProperty("AlwaysShowInformationScreen", "false")) || !informationAlreadySeen.contains(imageSet))) {
            InformationWindow info = new InformationWindow();
            info.init(imageSet, (Configuration)this.configurations.get(imageSet));
            info.display();
            this.setMascotInformationDismissed(imageSet);
            this.updateConfigFile();
         }

         this.createMascot(imageSet);
      } else {
         this.configurations.remove(imageSet);
      }

   }

   public Configuration getConfiguration(String imageSet) {
      return (Configuration)this.configurations.get(imageSet);
   }

   public Manager getManager() {
      return this.manager;
   }

   public Platform getPlatform() {
      return this.platform;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public ResourceBundle getLanguageBundle() {
      return this.languageBundle;
   }

   public void exit() {
      if(!this.exiting.compareAndSet(false,true))return;
      Runnable shutdown=()->{
         this.getManager().stop();
         if(this.characterLoadingReady)this.saveCharacterSession();
         this.getManager().disposeAll();
         com.group_finity.mascot.audio.SoundSystem.close();
         com.group_finity.mascot.progression.ProgressionService.close();
         System.exit(0);
      };
      if(javax.swing.SwingUtilities.isEventDispatchThread())new Thread(shutdown,"ShinobiMateShutdown").start();
      else shutdown.run();
   }

   private void setupTrayMenuAutoSizing(JDialog form, JPanel panel, float scaling, TrayIcon icon, MouseEvent event, JButton... buttons) {
      try {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gd = ge.getDefaultScreenDevice();
         DisplayMode dm = gd.getDisplayMode();
         Font baseFont = buttons[0].getFont();
         FontMetrics metrics = buttons[0].getFontMetrics(baseFont);
         int maxWidth = 0;

         for(JButton button : buttons) {
            int textWidth = metrics.stringWidth(button.getText());
            maxWidth = Math.max(maxWidth, textWidth);
         }

         int basePadding = (int)(32.0F * scaling);
         int buttonPadding = (int)(16.0F * scaling);
         int separatorHeight = (int)(2.0F * scaling);
         int insetHeight = (int)(5.0F * scaling);
         int windowWidth = maxWidth + basePadding + buttonPadding;
         int minWidth = (int)(200.0F * scaling);
         windowWidth = Math.max(windowWidth, minWidth);
         int buttonCount = buttons.length;
         int separatorCount = 2;
         int buttonHeight = metrics.getHeight() + (int)(8.0F * scaling);
         int windowHeight = buttonCount * buttonHeight + separatorCount * separatorHeight + (buttonCount - 1) * insetHeight + basePadding;
         java.util.List<Rectangle> monitors=new java.util.ArrayList<>();
         for(GraphicsDevice device:ge.getScreenDevices()) {
            var gc=device.getDefaultConfiguration();Rectangle bounds=new Rectangle(gc.getBounds());
            Insets insets=java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
            bounds.x+=insets.left;bounds.y+=insets.top;bounds.width-=insets.left+insets.right;bounds.height-=insets.top+insets.bottom;monitors.add(bounds);
         }
         Point click=event.getPoint();
         Rectangle screenBounds=com.group_finity.mascot.cloud.CloudPlacement.monitorFor(new Rectangle(click.x,click.y,1,1),monitors);
         int maxWindowWidth = (int)((double)screenBounds.width * 0.3);
         int maxWindowHeight = (int)((double)screenBounds.height * 0.8);
         windowWidth = Math.min(windowWidth, maxWindowWidth);
         windowHeight = Math.min(windowHeight, maxWindowHeight);
         panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
         form.pack();
         Point clickPoint = event.getPoint();
         int formX = clickPoint.x - form.getWidth();
         int formY = clickPoint.y - form.getHeight();
         if (formX < screenBounds.x) {
            formX = clickPoint.x;
         }

         if (formY < screenBounds.y) {
            formY = clickPoint.y;
         }

         if (formX + form.getWidth() > screenBounds.x + screenBounds.width) {
            formX = screenBounds.x + screenBounds.width - form.getWidth();
         }

         if (formY + form.getHeight() > screenBounds.y + screenBounds.height) {
            formY = screenBounds.y + screenBounds.height - form.getHeight();
         }

         form.setLocation(formX, formY);
         form.setVisible(true);
         log.info(String.format("Tray menu auto adjustment: window size=%dx%d, position=(%d,%d), DPI scaling=%.2f, screen=%dx%d", windowWidth, windowHeight, formX, formY, scaling, dm.getWidth(), dm.getHeight()));
      } catch (Exception e) {
         log.warning("Tray menu auto adjustment failed, using default layout: " + e.getMessage());
         panel.setPreferredSize(new Dimension((int)(250.0F * scaling), (int)(400.0F * scaling)));
         form.pack();
         form.setLocation(event.getPoint().x - form.getWidth(), event.getPoint().y - form.getHeight());
         form.setVisible(true);
      }

   }

   private void updateAutoStartButtonText(JButton btnAutoStart) {
      boolean isEnabled = Main.AutoStartManager.isAutoStartEnabled();
      String text = isEnabled ? this.languageBundle.getString("DisableAutoStart") : this.languageBundle.getString("EnableAutoStart");
      btnAutoStart.setText(text);
   }

   private void updateLicenseButtonText(JButton btnLicense) {
      LicenseManager manager = LicenseManager.getInstance();
      LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
      String text;
      switch (currentLevel) {
         case NO_KEY:
            String var10000 = this.languageBundle.getString("License");
            text = var10000 + " - " + this.languageBundle.getString("FreeVersion");
            break;
         case ADVANCED_KEY:
            long daysRemaining = manager.getDaysRemaining();
            text = String.format("%s - %s (%d %s)", this.languageBundle.getString("License"), this.languageBundle.getString("AdvancedVersion"), daysRemaining, this.languageBundle.getString("DaysRemaining").toLowerCase());
            break;
         case SPECIAL_KEY:
            long specialDaysRemaining = manager.getDaysRemaining();
            text = String.format("%s - %s (%d %s)", this.languageBundle.getString("License"), this.languageBundle.getString("DeveloperVersion"), specialDaysRemaining, this.languageBundle.getString("DaysRemaining").toLowerCase());
            break;
         default:
            text = this.languageBundle.getString("License");
      }

      btnLicense.setText(text);
   }

   private void showInfo(String message) {
      if (this.trayIcon != null) {
         this.trayIcon.displayMessage("ShinobiMate", message, MessageType.INFO);
      }

   }

   static {
      SETTINGS_FILE = com.group_finity.mascot.settings.SettingsStore.path();
      LOGGING_FILE = CONFIG_DIRECTORY.resolve("logging.properties");
      ICON_FILE = IMAGE_DIRECTORY.resolve("icon.ico");

      try {
         String osName = System.getProperty("os.name");
         String osVersion = System.getProperty("os.version");
         String osArch = System.getProperty("os.arch");
         log.log(Level.FINE, "当前操作系统: {0} {1} {2}", new Object[]{osName, osVersion, osArch});
         String javaVersion = System.getProperty("java.version");
         log.log(Level.FINE, "当前 Java 版本: {0}", javaVersion);
         // A failed D3D update worker can leave the EDT waiting indefinitely.
         System.setProperty("sun.java2d.d3d", "false");
         System.setProperty("sun.java2d.dpiaware", "false");
         System.setProperty("sun.java2d.uiScale", "1.0");
         System.setProperty("sun.java2d.uiScale.enabled", "false");
         System.setProperty("sun.java2d.win.uiScaleX", "1.0");
         System.setProperty("sun.java2d.win.uiScaleY", "1.0");
         System.setProperty("sun.java2d.win.dpiAwareness", "false");
         System.setProperty("sun.java2d.uiScale.enabled", "false");
         System.setProperty("sun.java2d.renderer", "sun.java2d.marlin.MarlinRenderingEngine");
         log.fine("Java 9+ DPI 兼容模式已启用");
         log.fine("  - DPI 感知: 已禁用");
         log.fine("  - UI 缩放: 已固定为 1.0");
         log.fine("  - 兼容模式: Java 8 行为");
      } catch (Exception e) {
         log.log(Level.WARNING, "启用 Java 9+ DPI 兼容模式失败: {0}", e.getMessage());
         log.warning("  - 请确保您使用的是 Oracle 或 OpenJDK 的 Java 9 或更高版本");
      }

      instance = new Main();
      frame = new JFrame();
      com.group_finity.mascot.ui.AppIcon.apply(frame);
   }

   private static class AutoStartManager {
      public static boolean isAutoStartEnabled() {
         try {
            if (Main.isMacOS()) {
               return com.group_finity.mascot.mac.AutoStartManager.isAutoStartEnabled();
            } else {
               return isWindows() ? com.group_finity.mascot.win.AutoStartManager.isAutoStartEnabled() : false;
            }
         } catch (Exception e) {
            Main.log.log(Level.WARNING, "Failed to check auto-start status", e);
            return false;
         }
      }

      public static boolean setAutoStart(boolean enable) {
         try {
            if (Main.isMacOS()) {
               return com.group_finity.mascot.mac.AutoStartManager.setAutoStart(enable);
            } else if (isWindows()) {
               return com.group_finity.mascot.win.AutoStartManager.setAutoStart(enable);
            } else {
               Main.log.warning("Auto-start is not supported on this platform");
               return false;
            }
         } catch (Exception e) {
            Main.log.log(Level.WARNING, "Failed to set auto-start: " + enable, e);
            return false;
         }
      }

      private static boolean isWindows() {
         return System.getProperty("os.name").toLowerCase().contains("win");
      }
   }
}
