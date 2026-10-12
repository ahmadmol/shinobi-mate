package com.group_finity.mascot.combat;

import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;
import java.util.*;

/** Gentle floor-only avoidance; never relocates dragged, paused, wall or combat characters. */
public final class DesktopAvoidance {
 private DesktopAvoidance() { }
 public static void tick(Manager manager) {
  var pets=manager.getMascots();if(pets.size()<2)return;
  double scale=RuntimeSettings.number("Scaling",1,.5,3);
  Map<Mascot,Rectangle> areas=new IdentityHashMap<>();
  Map<Mascot,Integer> shifts=new IdentityHashMap<>();
  Set<Mascot> fighters=Collections.newSetFromMap(new IdentityHashMap<>());
  for(Mascot m:pets) {
   if(m.isDragging()||!m.isAnimationActive())continue;
   if(manager.getCombat().active(m))fighters.add(m);
   Rectangle area=area(m);
   if(Math.abs(m.getAnchor().y-area.getMaxY())<=(fighters.contains(m)?80:8)*scale)areas.put(m,area);
  }
  for(int i=0;i<pets.size();i++)for(int j=i+1;j<pets.size();j++) {
   Mascot a=pets.get(i),b=pets.get(j);Rectangle ra=areas.get(a),rb=areas.get(b);
   if(ra==null||!ra.equals(rb))continue;
   if(fighters.contains(a)&&fighters.contains(b))continue;
   int distance=Math.abs(a.getAnchor().x-b.getAnchor().x);
   if(distance>=52*scale)continue;
   int direction=a.getAnchor().x<b.getAnchor().x||a.getAnchor().x==b.getAnchor().x&&a.getInstanceId()<b.getInstanceId()?-1:1;
   if(!fighters.contains(a))nudge(a,ra,direction,scale,shifts);
   if(!fighters.contains(b))nudge(b,rb,-direction,scale,shifts);
  }
 }
 private static void nudge(Mascot m,Rectangle area,int direction,double scale,Map<Mascot,Integer> shifts) {
  if(shifts.getOrDefault(m,0)>=2)return;
  int x=m.getAnchor().x,margin=(int)Math.ceil(64*scale);
  int nx=Math.max(area.x+margin,Math.min((int)area.getMaxX()-margin,x+direction*2));
  if(nx!=x){m.setAnchor(new Point(nx,m.getAnchor().y));shifts.merge(m,1,Integer::sum);}
 }
 private static Rectangle area(Mascot m) {
  if(GraphicsEnvironment.isHeadless())return m.getEnvironment().getWorkArea().toRectangle();
  Point p=m.getAnchor();Rectangle best=null;double closest=Double.MAX_VALUE;
  for(GraphicsDevice device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
   var gc=device.getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());Insets in=Toolkit.getDefaultToolkit().getScreenInsets(gc);
   r.x+=in.left;r.y+=in.top;r.width-=in.left+in.right;r.height-=in.top+in.bottom;
   double dx=Math.max(r.x-p.x,Math.max(0,p.x-r.getMaxX())),dy=Math.max(r.y-p.y,Math.max(0,p.y-r.getMaxY()));
   if(dx*dx+dy*dy<closest){closest=dx*dx+dy*dy;best=r;}
  }
  return best;
 }
}
