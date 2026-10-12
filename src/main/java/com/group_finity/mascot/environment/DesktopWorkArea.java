package com.group_finity.mascot.environment;

import java.awt.*;

/** Swing logical coordinates for the nearest real monitor, with every taskbar inset applied. */
public final class DesktopWorkArea {
 private DesktopWorkArea() { }
 public static Rectangle at(Point anchor) {
  Rectangle selected=null;double closest=Double.MAX_VALUE;
  for(GraphicsDevice device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
   var gc=device.getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());
   Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
   r.x+=insets.left;r.y+=insets.top;r.width-=insets.left+insets.right;r.height-=insets.top+insets.bottom;
   double dx=Math.max(r.x-anchor.x,Math.max(0,anchor.x-r.getMaxX()));
   double dy=Math.max(r.y-anchor.y,Math.max(0,anchor.y-r.getMaxY()));
   if(dx*dx+dy*dy<closest){closest=dx*dx+dy*dy;selected=r;}
  }
  if(selected==null)throw new IllegalStateException("No desktop monitor");
  return selected;
 }
}
