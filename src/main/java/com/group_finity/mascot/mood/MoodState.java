package com.group_finity.mascot.mood;
/** Per-instance monotonic-clock model, with a minimum dwell time and bounded event bursts. */
public final class MoodState {
 public enum Mood { HAPPY,NORMAL,BORED,SLEEPY,EXCITED,ANNOYED }
 public enum Event { CLICK,HOVER,DRAG,WIN,LOSS,WAKE }
 private Mood mood=Mood.NORMAL;
 private long lastInteraction,lastChange,holdUntil,lastDrag=-60000;
 private int dragCount;
 public MoodState(long now){lastInteraction=lastChange=now;}
 public synchronized Mood mood(){return mood;}
 public synchronized long idleMillis(long now){return Math.max(0,now-lastInteraction);}
 public synchronized void interact(Event event,long now) {
  long idle=idleMillis(now);lastInteraction=now;
  if(event==Event.DRAG){dragCount=now-lastDrag<=30000?dragCount+1:1;lastDrag=now;}
  Mood desired=switch(event){
   case WIN,WAKE -> Mood.EXCITED;
   case LOSS -> Mood.BORED;
   case DRAG -> dragCount>=3?Mood.ANNOYED:Mood.NORMAL;
   case CLICK -> idle>=600000?Mood.EXCITED:Mood.HAPPY;
   case HOVER -> idle>=600000?Mood.EXCITED:Mood.HAPPY;
  };
  if(event==Event.WIN||event==Event.LOSS||event==Event.WAKE||idle>=600000||desired==Mood.ANNOYED||now-lastChange>=20000||mood==Mood.NORMAL){
   if(desired!=mood){mood=desired;lastChange=now;}
   holdUntil=now+(event==Event.WIN?90000:event==Event.LOSS?45000:30000);
  }
 }
 public synchronized void update(long now){
  if(now<holdUntil||now-lastChange<20000)return;
  long idle=idleMillis(now);Mood desired=idle>=600000?Mood.SLEEPY:idle>=180000?Mood.BORED:Mood.NORMAL;
  if(desired!=mood){mood=desired;lastChange=now;}
 }
}
