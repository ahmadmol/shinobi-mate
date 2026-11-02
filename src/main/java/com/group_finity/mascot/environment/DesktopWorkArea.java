package com.group_finity.mascot.environment;

import java.awt.*;

/** Swing logical coordinates for the nearest real monitor, with every taskbar inset applied. */
public final class DesktopWorkArea {
 private DesktopWorkArea() { }
 private record Snapshot(Rectangle[] areas,long captured) { }
 private static volatile Snapshot snapshot;
 /** One native monitor/inset query per engine frame, shared by all border checks. */
 public static synchronized void refresh() {
  snapshot=new Snapshot(readAreas(),System.nanoTime());
 }
 public static Rectangle at(Point anchor) {
  Snapshot current=snapshot;
  // UI/startup callers also work while the engine is stopped. Never retain stale
  // topology indefinitely after a monitor/taskbar change.
  if(current==null||System.nanoTime()-current.captured()>100_000_000L) {
   synchronized(DesktopWorkArea.class) {
    current=snapshot;
    if(current==null||System.nanoTime()-current.captured()>100_000_000L) {
     refresh();current=snapshot;
    }
   }
  }
  Rectangle selected=null;double closest=Double.MAX_VALUE;
  for(Rectangle r:current.areas()) {
   double dx=Math.max(r.x-anchor.x,Math.max(0,anchor.x-r.getMaxX()));
   double dy=Math.max(r.y-anchor.y,Math.max(0,anchor.y-r.getMaxY()));
   if(dx*dx+dy*dy<closest){closest=dx*dx+dy*dy;selected=r;}
  }
  if(selected==null)throw new IllegalStateException("No desktop monitor");
  return new Rectangle(selected);
 }
 /** A work-area edge is solid unless another monitor continues beyond it.
  * Use the same logical work areas as collision detection, including secondary taskbars. */
 public static boolean isExterior(Point point,int dx,int dy) {
  at(point); // ensure the shared topology is initialized/current
  Point beyond=new Point(point.x+dx,point.y+dy);
  for(Rectangle area:snapshot.areas())if(area.contains(beyond))return false;
  return true;
 }
 private static Rectangle[] readAreas() {
  java.util.List<Rectangle> areas=new java.util.ArrayList<>();
  for(GraphicsDevice device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
   var gc=device.getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());
   Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
   r.x+=insets.left;r.y+=insets.top;r.width-=insets.left+insets.right;r.height-=insets.top+insets.bottom;
   // With legacy Windows DPI virtualization AWT can report zero insets even
   // while the primary taskbar reserves space. SPI supplies the actual work area.
   if(com.sun.jna.Platform.isWindows()&&insets.top+insets.bottom+insets.left+insets.right==0&&
      device.equals(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice())) {
    var api=com.group_finity.mascot.win.jna.User32.INSTANCE;
    var work=new com.group_finity.mascot.win.jna.RECT();api.SystemParametersInfoW(48,0,work,0);
    int width=api.GetSystemMetrics(0),height=api.GetSystemMetrics(1);
    if(width>0&&height>0&&work.right>work.left&&work.bottom>work.top) {
     double sx=(double)r.width/width,sy=(double)r.height/height;
     Rectangle nativeWork=new Rectangle(r.x+(int)Math.round(work.left*sx),r.y+(int)Math.round(work.top*sy),
       (int)Math.round((work.right-work.left)*sx),(int)Math.round((work.bottom-work.top)*sy));
     r=r.intersection(nativeWork);
    }
   }
   areas.add(r);
  }
  return areas.toArray(Rectangle[]::new);
 }
}
