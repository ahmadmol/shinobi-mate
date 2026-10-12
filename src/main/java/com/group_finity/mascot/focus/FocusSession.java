package com.group_finity.mascot.focus;
/** Offline Pomodoro; missed ticks emit one transition, never farm overnight catch-up sessions. */
public final class FocusSession {
 public enum Phase { IDLE,FOCUS,BREAK,PAUSED }
 public enum Event { NONE,FOCUS_COMPLETED,BREAK_COMPLETED }
 private Phase phase=Phase.IDLE,resumePhase=Phase.IDLE;
 private long deadline,remaining,focusMillis,breakMillis;
 public synchronized Phase phase(){return phase;}
 public synchronized boolean quiet(){return phase==Phase.FOCUS||(phase==Phase.PAUSED&&resumePhase==Phase.FOCUS);}
 public synchronized long remaining(long now){return phase==Phase.IDLE?0:phase==Phase.PAUSED?remaining:Math.max(0,deadline-now);}
 public synchronized void start(long now,long focusMillis,long breakMillis){
  if(focusMillis<1000||breakMillis<1000||focusMillis>10800000||breakMillis>10800000)throw new IllegalArgumentException("Invalid focus duration");
  this.focusMillis=focusMillis;this.breakMillis=breakMillis;phase=Phase.FOCUS;deadline=now+focusMillis;
 }
 public synchronized void pause(long now){if(phase==Phase.FOCUS||phase==Phase.BREAK){resumePhase=phase;remaining=Math.max(0,deadline-now);phase=Phase.PAUSED;}}
 public synchronized void resume(long now){if(phase==Phase.PAUSED){phase=resumePhase;deadline=now+remaining;}}
 public synchronized void stop(){phase=resumePhase=Phase.IDLE;remaining=0;}
 public synchronized Event tick(long now){
  if((phase!=Phase.FOCUS&&phase!=Phase.BREAK)||now<deadline)return Event.NONE;
  if(phase==Phase.FOCUS){phase=Phase.BREAK;deadline=now+breakMillis;return Event.FOCUS_COMPLETED;}
  phase=Phase.FOCUS;deadline=now+focusMillis;return Event.BREAK_COMPLETED;
 }
}
