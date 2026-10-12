package com.group_finity.mascot.wayland;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Window.Type;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class WaylandTranslucentWindow extends JWindow implements TranslucentWindow {
   private static final long serialVersionUID = 1L;
   private WaylandNativeImage image;
   private float alpha = 1.0F;
   private boolean windowVisible = false;
   private long lastRepaintTime = 0L;
   private static final long MIN_REPAINT_INTERVAL = 16L;
   private final AtomicBoolean repaintScheduled = new AtomicBoolean(false);

   public WaylandTranslucentWindow() {
      this.init();
      this.setupWindow();
   }

   private void init() {
      System.setProperty("sun.java2d.d3d", "false");
      System.setProperty("sun.java2d.opengl", "true");
      System.setProperty("sun.java2d.noddraw", "true");
      System.setProperty("awt.useSystemAAFontSettings", "on");
      System.setProperty("swing.aatext", "true");
      System.setProperty("sun.java2d.transaccel", "true");
      System.setProperty("sun.java2d.ddforcevram", "true");
   }

   private void setupWindow() {
      this.setType(Type.UTILITY);
      this.setAlwaysOnTop(true);
      this.setFocusable(false);
      this.setFocusableWindowState(false);
      this.setAutoRequestFocus(false);
      this.setBackground(new Color(0, 0, 0, 0));
      JPanel panel = new JPanel((LayoutManager)null) {
         private static final long serialVersionUID = 1L;

         protected void paintComponent(Graphics g) {
            if (WaylandTranslucentWindow.this.image != null) {
               WaylandTranslucentWindow.this.paintOptimized(g);
            }

         }

         public void paint(Graphics g) {
            this.paintComponent(g);
         }

         public boolean isOpaque() {
            return false;
         }
      };
      panel.setOpaque(false);
      panel.setBackground(new Color(0, 0, 0, 0));
      this.setContentPane(panel);
      this.setLayout(new BorderLayout());
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            WaylandTranslucentWindow.this.scheduleRepaint();
         }

         public void componentMoved(ComponentEvent e) {
            WaylandTranslucentWindow.this.scheduleRepaint();
         }
      });
      AtomicBoolean updating = new AtomicBoolean();
      this.addPropertyChangeListener("graphicsConfiguration", (evt) -> {
         if (updating.compareAndSet(false, true)) {
            try {
               this.setBackground(new Color(0, 0, 0, 0));
            } finally {
               updating.set(false);
            }
         }

      });
   }

   private void paintOptimized(Graphics g) {
      if (this.image != null) {
         Graphics2D g2d = (Graphics2D)g.create();

         try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            BufferedImage sourceImage = this.image.getManagedImage();
            if (this.alpha < 1.0F) {
               g2d.setComposite(AlphaComposite.getInstance(3, this.alpha));
            }

            g2d.drawImage(sourceImage, 0, 0, (ImageObserver)null);
         } finally {
            g2d.dispose();
         }

      }
   }

   public void setVisible(boolean b) {
      super.setVisible(b);
      this.windowVisible = b;
      if (b) {
         this.toFront();
      }

   }

   protected void addImpl(Component comp, Object constraints, int index) {
      super.addImpl(comp, constraints, index);
      if (comp instanceof JComponent jcomp) {
         jcomp.setOpaque(false);
      }

   }

   public float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float alpha) {
      this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
      if (this.windowVisible) {
         this.scheduleRepaint();
      }

   }

   private void scheduleRepaint() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastRepaintTime >= 16L && this.repaintScheduled.compareAndSet(false, true)) {
         SwingUtilities.invokeLater(() -> {
            try {
               this.repaint();
               this.lastRepaintTime = System.currentTimeMillis();
            } finally {
               this.repaintScheduled.set(false);
            }

         });
      }

   }

   public Component asComponent() {
      return this;
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "WaylandTranslucentWindow[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.getBounds()) + "]";
   }

   public void paint(Graphics g) {
      if (g instanceof Graphics2D g2d) {
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }

      super.paint(g);
   }

   public WaylandNativeImage getImage() {
      return this.image;
   }

   public void setImage(NativeImage image) {
      this.image = (WaylandNativeImage)image;
      this.scheduleRepaint();
   }

   public void updateImage() {
      this.validate();
      this.scheduleRepaint();
   }

   public void dispose() {
      if (this.image != null) {
         this.image.dispose();
      }

      super.dispose();
   }
}
