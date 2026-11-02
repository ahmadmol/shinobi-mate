package com.group_finity.mascot.combat;

import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Predict a blocked floor path and choose a pass or a turn, never cancel motion with nudges. */
public final class DesktopAvoidance {
 private static final Map<Mascot,Point> previous=new WeakHashMap<>();
 private DesktopAvoidance() { }
 public static void tick(Manager manager) {
  List<Mascot> pets=manager.getMascots();
  Map<Mascot,Double> velocity=new IdentityHashMap<>();
  Map<Mascot,Rectangle> floors=new IdentityHashMap<>();
  double scale=RuntimeSettings.number("Scaling",1,.5,3);
  for(Mascot m:pets) {
   Point p=m.getAnchor(),old=previous.put(m,new Point(p));
   double delta=old==null?0.0:(double)p.x-old.x;
   velocity.put(m,Math.abs(delta)>24*scale?0.0:delta);
   Rectangle r=m.getEnvironment().getWorkArea().toRectangle();
   if(m.getImage()!=null&&Math.abs(p.y-r.getMaxY())<=8*scale)floors.put(m,r);
  }
  for(Mascot mover:pets) {
   Rectangle area=floors.get(mover);double dx=velocity.get(mover);
   if(area==null||Math.abs(dx)<.5||mover.isDragging()||!mover.isAnimationActive()||
       manager.getCombat().active(mover)||mover.getBehavior() instanceof PedestrianPass)continue;
   int dir=dx>0?1:-1;Mascot obstacle=null;double nearest=Double.MAX_VALUE;
   for(Mascot other:pets) {
    if(other==mover||!area.equals(floors.get(other))||other.getBehavior() instanceof PedestrianPass)continue;
    double ahead=(other.getAnchor().x-mover.getAnchor().x)*dir,gap=clearance(mover,other);
    double closing=(dx-velocity.get(other))*dir;
    if(ahead<0||ahead>=gap+Math.max(Math.abs(dx),closing)*10+64*scale)continue;
    if((dx-velocity.get(other))*dir<=.25&&ahead>=gap*.7)continue;
    if(ahead<nearest){nearest=ahead;obstacle=other;}
   }
   if(obstacle==null)continue;
   int landing=Integer.MIN_VALUE;
   int height=(int)Math.ceil(Math.max(mover.getImage().size().height,obstacle.getImage().size().height)+56*scale);
   // A moving character may enter the descent path. Pass stationary companions;
   // reroute around moving traffic instead of assuming a fixed landing geometry.
   if(!manager.getCombat().active(obstacle)&&Math.abs(velocity.get(obstacle))<.5&&area.height>height+mover.getImage().size().height) {
    for(int travel=(int)Math.ceil(Math.max(240*scale,2*nearest+32*scale));travel<=420*scale;travel+=(int)Math.max(1,32*scale)) {
     int x=mover.getAnchor().x+dir*travel,margin=mover.getImage().size().width/2;
     if(x<area.x+margin||x>area.getMaxX()-margin)break;
     final int candidate=x;
     boolean free=pets.stream().filter(p->p!=mover&&area.equals(floors.get(p)))
       .noneMatch(p->Math.abs(candidate-p.getAnchor().x)<clearance(mover,p)+20*scale);
     if(free){landing=x;break;}
    }
   }
   var resume=mover.getBehavior();
   try {mover.setBehavior(new PedestrianPass(resume,area,landing,height,-dir));}
   catch(com.group_finity.mascot.exception.CantBeAliveException ex) {
    try{if(resume!=null)mover.setBehavior(resume);}catch(com.group_finity.mascot.exception.CantBeAliveException ignored){ }
    java.util.logging.Logger.getLogger(DesktopAvoidance.class.getName()).fine(ex.toString());
   }
  }
 }
 private static double clearance(Mascot a,Mascot b) {
  return (a.getImage().size().width+b.getImage().size().width)*.28;
 }
}
