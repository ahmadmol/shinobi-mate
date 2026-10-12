package com.group_finity.mascot.x11;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;

class X11TranslucentWindow extends JWindow implements TranslucentWindow {
   private static final long serialVersionUID = 1L;
   private X11NativeImage image;
   private static final X11 x11;
   private final X11.Display dpy;
   private X11.Window win;
   private float alpha;

   public X11TranslucentWindow() {
      super(WindowUtils.getAlphaCompatibleGraphicsConfiguration());
      this.dpy = x11.XOpenDisplay((String)null);
      this.win = null;
      this.alpha = 1.0F;
      this.init();
      this.setBackground(new Color(0, 0, 0, 0));
      JPanel panel = new JPanel() {
         private static final long serialVersionUID = 1L;

         protected void paintComponent(Graphics g) {
            g.clearRect(0, 0, this.getWidth(), this.getHeight());
            super.paintComponent(g);
            if (X11TranslucentWindow.this.getImage() != null) {
               g.drawImage(X11TranslucentWindow.this.getImage().getManagedImage(), 0, 0, (ImageObserver)null);
            }

         }
      };
      panel.setOpaque(false);
      this.setContentPane(panel);
      this.setLayout(new BorderLayout());
      AtomicBoolean updating = new AtomicBoolean();
      this.addPropertyChangeListener("graphicsConfiguration", (evt) -> {
         if (updating.compareAndSet(false, true)) {
            try {
               this.setBackground(new Color(0, 0, 0, 255));
               this.setBackground(new Color(0, 0, 0, 0));
            } finally {
               updating.set(false);
            }
         }

      });
   }

   private void init() {
      System.setProperty("sun.java2d.d3d", "false");
      System.setProperty("sun.java2d.opengl", "true");
   }

   public void setVisible(boolean b) {
      super.setVisible(b);
      if (b) {
         try {
            WindowUtils.setWindowTransparent(this, true);
         } catch (IllegalArgumentException var3) {
         }
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
      WindowUtils.setWindowAlpha(this, alpha);
      this.alpha = alpha;
   }

   public void setToDock(int value) {
      IntByReference dockAtom = new IntByReference(value);
      X11 var10000 = x11;
      X11.Display var10001 = this.dpy;
      X11.Window var10002 = this.win;
      X11.Atom var10003 = x11.XInternAtom(this.dpy, "_NET_WM_WINDOW_TYPE", false);
      X11 var10004 = x11;
      X11 var10006 = x11;
      var10000.XChangeProperty(var10001, var10002, var10003, X11.XA_ATOM, 32, 0, dockAtom.getPointer(), 1);
   }

   public Component asComponent() {
      return this;
   }

   public String toString() {
      int var10000 = this.hashCode();
      return "X11TranslucentWindow[hashCode=" + var10000 + ",bounds=" + String.valueOf(this.getBounds()) + "]";
   }

   public void paint(Graphics g) {
      if (g instanceof Graphics2D g2d) {
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }

      super.paint(g);
   }

   public X11NativeImage getImage() {
      return this.image;
   }

   public void setImage(NativeImage image) {
      this.image = (X11NativeImage)image;
   }

   public void updateImage() {
      this.validate();
      this.repaint();
   }

   static {
      x11 = X11.INSTANCE;
   }
}
