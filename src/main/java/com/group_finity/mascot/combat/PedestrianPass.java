package com.group_finity.mascot.combat;

import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.CantBeAliveException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;

/** Short, interruptible traversal using the pack's own takeoff, air and landing art. */
public final class PedestrianPass implements Behavior {
 private final Behavior resume;
 private final Rectangle floor;
 private final int landing,height,turnDirection;
 private Mascot pet;
 private Configuration config;
 private Action pose;
 private int age,stage=-1,origin,duration;
 private boolean facing;
 PedestrianPass(Behavior resume,Rectangle floor,int landing,int height,int turnDirection) {
  this.resume=resume;this.floor=new Rectangle(floor);this.landing=landing;this.height=height;this.turnDirection=turnDirection;
 }
 public void init(Mascot pet)throws CantBeAliveException {
  this.pet=pet;config=Main.getInstance().getConfiguration(pet.getImageSet());origin=pet.getAnchor().x;
  double speed=6*com.group_finity.mascot.settings.RuntimeSettings.number("Scaling",1,.5,3)*
    com.group_finity.mascot.settings.RuntimeSettings.number("MovementSpeed",1,.5,2);
  duration=Math.max(36,(int)Math.ceil(Math.abs(landing==Integer.MIN_VALUE?0:landing-origin)/speed));
  facing=landing==Integer.MIN_VALUE?turnDirection>0:landing>origin;
  try{setPose(0);}catch(Exception ex){throw new CantBeAliveException("Cannot prepare pedestrian traversal",ex);}
 }
 private void setPose(int next)throws Exception {
  stage=next;
  String action=next==0?"NinjaHop":next==1?"Jumping":next==2?"Falling":next==3?"Walk":"Stand";
  int target=landing==Integer.MIN_VALUE?(facing?(int)floor.getMaxX()-pet.getImage().size().width/2:floor.x+pet.getImage().size().width/2):landing;
  pose=config.buildAction(action,Map.of("TargetX",Integer.toString(target),"TargetY",Integer.toString((int)floor.getMaxY()),"Duration","200"));
  pose.init(pet);
 }
 public void next()throws CantBeAliveException {
  try {
   if(!floor.equals(pet.getEnvironment().getWorkArea().toRectangle())){pet.setBehavior(config.buildBehavior("Fall"));return;}
   if(landing==Integer.MIN_VALUE) {
    if(age==2)pet.setLookRight(facing);
    if(age==3)setPose(3);
    Point before=new Point(pet.getAnchor());pose.next();
    if(age<3)pet.setAnchor(before);
    int margin=pet.getImage().size().width/2;
    pet.setAnchor(new Point(Math.max(floor.x+margin,Math.min((int)floor.getMaxX()-margin,pet.getAnchor().x)),(int)floor.getMaxY()));
    if(++age>=18)finish();
    return;
   }
   pet.setLookRight(facing);
   if(age<3){Point before=new Point(pet.getAnchor());pose.next();pet.setAnchor(before);}
   else if(age<=duration+3) {
    double t=(age-3.0)/duration;
    int next=t<=.5?1:2;if(stage!=next)setPose(next);
    pose.next();
    pet.setAnchor(new Point((int)Math.round(origin+(landing-origin)*t),(int)Math.round(floor.getMaxY()-4*height*t*(1-t))));
   }else {
    if(stage!=4){setPose(4);pet.playAudio("movement.landing");}
    pose.next();pet.setAnchor(new Point(landing,(int)floor.getMaxY()));
    if(age>=duration+6){finish();return;}
   }
   age++;
  }catch(Exception ex) {
   try{pet.setBehavior(config.buildBehavior("Fall"));}
   catch(Exception fallback){throw new CantBeAliveException("Cannot recover pedestrian traversal",fallback);}
  }
 }
 private void finish()throws CantBeAliveException {
  if(landing==Integer.MIN_VALUE) {
   try {
    int target=facing?(int)floor.getMaxX()-pet.getImage().size().width/2:floor.x+pet.getImage().size().width/2;
    pet.setBehavior(new com.group_finity.mascot.behavior.UserBehavior("WalkAlongWorkAreaFloor",
      config.buildAction("Walk",Map.of("TargetX",Integer.toString(target),"Duration","120")),config));
   }catch(Exception ex){throw new CantBeAliveException("Cannot resume after turning",ex);}
  }else if(resume!=null)pet.setBehavior(resume);
  else try{pet.setBehavior(config.buildBehavior("StandUp"));}catch(Exception ex){throw new CantBeAliveException("Cannot resume floor behavior",ex);}
 }
 public void mousePressed(MouseEvent event)throws CantBeAliveException {if(resume!=null)resume.mousePressed(event);}
 public void mouseReleased(MouseEvent event)throws CantBeAliveException {if(resume!=null)resume.mouseReleased(event);}
 @Override public String toString(){return landing==Integer.MIN_VALUE?"Pedestrian turn":"Pedestrian overtake "+origin+" -> "+landing;}
}
