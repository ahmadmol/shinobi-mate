package com.group_finity.mascot.progression;
import com.group_finity.mascot.pack.PackPaths;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;

/** Local cosmetic progression and ISO-week challenges; never used by combat stats. */
public final class ProgressionModel {
 public enum Event { INTERACTION,FOCUS_COMPLETED,BATTLE_COMPLETED,RUN_TWO }
 public record Profile(long xp,int level,long nextLevelXP){ }
 public record Challenge(Event event,int current,int target){public boolean complete(){return current>=target;}}
 private final Properties data;
 public ProgressionModel(Properties data){this.data=data;}
 public synchronized Profile profile(String id){
  PackPaths.id(id);long xp=value("xp."+id);int level=1+(int)Math.sqrt(xp/100.0);return new Profile(xp,level,(long)level*level*100);
 }
 public synchronized void award(String id,int amount){
  PackPaths.id(id);if(amount<0||amount>100)throw new IllegalArgumentException("Invalid XP award");
  data.setProperty("xp."+id,Long.toString(Math.min(10_000_000,profile(id).xp+amount)));
 }
 private long value(String key){
  try{return Math.max(0,Math.min(10_000_000,Long.parseLong(data.getProperty(key,"0"))));}catch(NumberFormatException ex){return 0;}
 }
 public static String week(LocalDate date){return date.get(WeekFields.ISO.weekBasedYear())+"W"+date.get(WeekFields.ISO.weekOfWeekBasedYear());}
 public synchronized void event(Event event,LocalDate date){
  rollover(date);String daily="daily."+date+"."+event,weekly="weekly."+week(date)+"."+event;
  if(event==Event.RUN_TWO&&value(daily)>=1)return;
  data.setProperty(daily,Long.toString(Math.min(1,value(daily)+1)));
  data.setProperty(weekly,Long.toString(Math.min(weeklyTarget(event),value(weekly)+1)));
 }
 private void rollover(LocalDate date){
  String daily="daily."+date+".",weekly="weekly."+week(date)+".";
  for(String k:new ArrayList<>(data.stringPropertyNames()))
   if((k.startsWith("daily.")&&!k.startsWith(daily))||(k.startsWith("weekly.")&&!k.startsWith(weekly)))data.remove(k);
 }
 private int weeklyTarget(Event event){return event==Event.INTERACTION?10:3;}
 public synchronized List<Challenge> challenges(LocalDate date,boolean weekly){
  String prefix=weekly?"weekly."+week(date)+".":"daily."+date+".";
  return Arrays.stream(Event.values()).map(e->new Challenge(e,(int)value(prefix+e),weekly?weeklyTarget(e):1)).toList();
 }
 public synchronized Properties snapshot(){Properties copy=new Properties();copy.putAll(data);return copy;}
}
