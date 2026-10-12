package com.group_finity.mascot.performance;
/** Render deadlines use monotonic time; simulation ticks never consult this gate. */
public final class RenderGate {
 private double next;
 private int lastFps;
 public boolean ready(long millis,int fps) {
  if(fps!=lastFps){next=millis;lastFps=fps;}
  if(millis<next)return false;
  if(millis-next>1000)next=millis;
  next+=1000.0/Math.max(1,fps);return true;
 }
}
