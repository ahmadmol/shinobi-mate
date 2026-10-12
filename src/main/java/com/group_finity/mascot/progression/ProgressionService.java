package com.group_finity.mascot.progression;
import com.group_finity.mascot.Manager;
import com.group_finity.mascot.settings.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
/** One bounded writer; XP event pacing is per profile, so duplicate instances cannot multiply it. */
public final class ProgressionService {
 private static ProgressionService instance;
 public static synchronized ProgressionService instance(){if(instance==null)instance=new ProgressionService();return instance;}
 private final Path path=SettingsStore.path().getParent().resolve("progress.properties");
 private final ProgressionModel model;
 private final Map<String,Long> interactionAt=new HashMap<>(),useAt=new HashMap<>();
 private LocalDate twoRecorded;private long dirtyGeneration,savedGeneration,lastFlush;
 private final ExecutorService writer=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"ShinobiMateProfileWriter");t.setDaemon(true);return t;});
 private Future<?> saving;
 private ProgressionService(){
  Properties data=new Properties();try{if(Files.isRegularFile(path)&&Files.size(path)<=256000)try(var in=Files.newInputStream(path)){data.load(in);}}catch(Exception ex){log(ex);}
  model=new ProgressionModel(data);
 }
 public ProgressionModel model(){return model;}
 private static long now(){return System.nanoTime()/1_000_000;}
 private static boolean enabled(){return RuntimeSettings.flag("ProgressionEnabled",false)||RuntimeSettings.flag("ChallengesEnabled",false);}
 private synchronized void award(String id,int amount){if(RuntimeSettings.flag("ProgressionEnabled",false)){model.award(id,amount);dirtyGeneration++;}}
 private synchronized void event(ProgressionModel.Event event){if(RuntimeSettings.flag("ChallengesEnabled",false)){model.event(event,LocalDate.now());dirtyGeneration++;}}
 public static void interact(String id){
  if(!enabled())return;var s=instance();long now=now();
  synchronized(s){if(now-s.interactionAt.getOrDefault(id,-60000L)<30000)return;s.interactionAt.put(id,now);s.award(id,1);s.event(ProgressionModel.Event.INTERACTION);}
 }
 public static void battle(String winner,String loser){
  if(!enabled())return;var s=instance();s.award(winner,10);if(!loser.equals(winner))s.award(loser,3);s.event(ProgressionModel.Event.BATTLE_COMPLETED);
 }
 public static void focus(Manager manager){
  if(!enabled())return;var s=instance();Set<String> profiles=new HashSet<>();for(var m:manager.getMascots())profiles.add(m.getImageSet());
  for(String id:profiles)s.award(id,15);s.event(ProgressionModel.Event.FOCUS_COMPLETED);
 }
 public static void tick(Manager manager){
  if(!enabled())return;var s=instance();long now=now();LocalDate date=LocalDate.now();
  synchronized(s){
   if(manager.getCount()>=2&&!date.equals(s.twoRecorded)){s.twoRecorded=date;s.event(ProgressionModel.Event.RUN_TWO);}
   for(var m:manager.getMascots()){
    String id=m.getImageSet();if(now>=s.useAt.getOrDefault(id,now+600000)){s.award(id,1);s.useAt.put(id,now+600000);}else s.useAt.putIfAbsent(id,now+600000);
   }
   if(now-s.lastFlush>=5000){s.lastFlush=now;s.flushAsync();}
  }
 }
 private synchronized void flushAsync(){
  if(dirtyGeneration==savedGeneration||(saving!=null&&!saving.isDone()))return;
  Properties snapshot=model.snapshot();long generation=dirtyGeneration;
  saving=writer.submit(()->{try{SettingsStore.save(snapshot,path);synchronized(this){savedGeneration=generation;}}catch(Exception ex){log(ex);}});
 }
 public static void close(){
  ProgressionService s; synchronized(ProgressionService.class){s=instance;}if(s==null)return;
  s.writer.shutdown();
  try{s.writer.awaitTermination(2,TimeUnit.SECONDS);synchronized(s){if(s.dirtyGeneration!=s.savedGeneration)SettingsStore.save(s.model.snapshot(),s.path);}}
  catch(Exception ex){log(ex);}
 }
 private static void log(Exception ex){Logger.getLogger(ProgressionService.class.getName()).log(Level.WARNING,"Progress profile could not be saved",ex);}
}
