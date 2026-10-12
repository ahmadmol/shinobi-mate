package com.group_finity.mascot.virtual;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

class VirtualEnvironment extends Environment {
   private final JFrame display = new JFrame();
   private final Area activeIE = new Area();

   public Area getWorkArea() {
      return this.getScreen();
   }

   public Area getActiveIE() {
      return this.activeIE;
   }

   public String getActiveIETitle() {
      return null;
   }

   public long getActiveWindowId() {
      return 0L;
   }

   public void moveActiveIE(Point point) {
   }

   public void restoreIE() {
   }

   public void refreshCache() {
   }

   public void init() {
      if (!this.display.isVisible()) {
         this.display.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
               Main.getInstance().exit();
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
         });
         this.display.setAutoRequestFocus(false);
         String title = Main.getInstance().getProperties().getProperty("ShimejiEENameOverride", "").trim();
         if (title.isEmpty()) {
            title = Main.getInstance().getLanguageBundle().getString("ShimejiEE");
         }

         this.display.setTitle(title);
         String[] windowArray = Main.getInstance().getProperties().getProperty("WindowSize", "600x500").split("x");
         BufferedImage image = null;

         try {
            if (!Main.getInstance().getProperties().getProperty("BackgroundImage", "").isEmpty()) {
               image = ImageIO.read(new File(Main.getInstance().getProperties().getProperty("BackgroundImage", "")));
            }
         } catch (IOException var11) {
         }

         this.display.setContentPane(new VirtualContentPanel(new Dimension(Integer.parseInt(windowArray[0]), Integer.parseInt(windowArray[1])), Color.decode(Main.getInstance().getProperties().getProperty("Background", "#00FF00")), image, Main.getInstance().getProperties().getProperty("BackgroundMode", "centre")));
         this.display.setBackground(this.display.getContentPane().getBackground());
         com.group_finity.mascot.ui.AppIcon.apply(this.display);
         SwingUtilities.invokeLater(() -> {
            this.display.pack();
            this.display.setVisible(true);
            this.display.toFront();
         });
         this.activeIE.set(new Rectangle(-500, -500, 0, 0));
         screenRect.setBounds(this.display.getContentPane().getBounds());
      }

      this.tick();
   }

   public void tick() {
      if (this.display.isVisible()) {
         getScreenRect().setBounds(this.display.getContentPane().getBounds());
         this.getScreen().set(getScreenRect());
      }

      PointerInfo info = MouseInfo.getPointerInfo();
      Point point = new Point(0, 0);
      if (info != null && this.display.isVisible()) {
         point = info.getLocation();
         SwingUtilities.convertPointFromScreen(point, this.display.getContentPane());
      }

      this.getCursor().set(point);
   }

   public void dispose() {
      if (this.display != null) {
         this.display.dispose();
      }

   }

   public void addShimeji(VirtualTranslucentPanel shimeji) {
      SwingUtilities.invokeLater(() -> {
         if (this.display.getContentPane().getSize().width > 0 && this.display.getContentPane().getSize().height > 0) {
            this.display.setPreferredSize(this.display.getSize());
            this.display.getRootPane().setPreferredSize(this.display.getRootPane().getSize());
            this.display.getContentPane().setPreferredSize(this.display.getContentPane().getSize());
         }

         shimeji.setOpaque(false);
         this.display.getContentPane().add(shimeji);
      });
   }
}
