package com.group_finity.mascot.character;
import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.util.List;
/** A narrow product facade; UI never uses reflective access to Main. */
public final class CharacterService {
 private static final java.util.concurrent.ThreadPoolExecutor launches=new java.util.concurrent.ThreadPoolExecutor(1,1,0,java.util.concurrent.TimeUnit.MILLISECONDS,
  new java.util.concurrent.ArrayBlockingQueue<>(20),task->{Thread thread=new Thread(task,"ShinobiMate character loading");thread.setDaemon(true);return thread;});
 public static void launchFromMenu(String id) {
  try{launches.execute(()->{
   com.group_finity.mascot.RuntimeDiagnostics.event("CHARACTER_LAUNCH_STARTED");
   try{if(!new CharacterService().launch(id))Main.showError(com.group_finity.mascot.ui.Theme.t("تعذر تشغيل الشخصية؛ راجع الحزمة أو الحد الأقصى.","Could not launch the character; check the pack or character limit."));}
   catch(RuntimeException ex){java.util.logging.Logger.getLogger(CharacterService.class.getName()).log(java.util.logging.Level.WARNING,"Character launch failed",ex);Main.showError(com.group_finity.mascot.ui.Theme.t("تعذر تشغيل الشخصية؛ راجع سجل الأخطاء.","Could not launch the character; check the error log."));}
  });}catch(java.util.concurrent.RejectedExecutionException ex){/* bounded repeated clicks */}
 }
 public List<CharacterDescriptor> installed(){return new CharacterCatalog().list();}
 public int count(String id){var manager=Main.getInstance().getManager();synchronized(manager.getMascots()){
  var instances=new java.util.LinkedHashSet<>(manager.getMascots());instances.addAll(manager.getAdded());
  return (int)instances.stream().filter(m->m.getManager()==manager&&m.getImageSet().equals(id)).count();
 }}
 public boolean canLaunch(){if(!Main.getInstance().isCharacterLoadingReady())return false;var manager=Main.getInstance().getManager();synchronized(manager.getMascots()){return manager.getCount()+manager.getAdded().size()<RuntimeSettings.number("MaxCharacters",20,1,100);}}
 public boolean launch(String id) {
  Main app=Main.getInstance();Manager manager=app.getManager();
  if(!canLaunch()||installed().stream().noneMatch(p->p.id().equals(id))||!app.prepareCharacter(id))return false;
  if(!canLaunch())return false;
  int before; synchronized(manager.getMascots()){before=manager.getCount()+manager.getAdded().size();}
  manager.setExitOnLastRemoved(false);app.createMascot(id);
  int after; synchronized(manager.getMascots()){after=manager.getCount()+manager.getAdded().size();}
  com.group_finity.mascot.RuntimeDiagnostics.event(after>before?"CHARACTER_LAUNCH_SUCCEEDED":"CHARACTER_LAUNCH_FAILED");return after>before;
 }
 public void remove(String id) {
  var manager=Main.getInstance().getManager();
  synchronized(manager.getMascots()) {
   manager.setExitOnLastRemoved(false);
   for(Mascot mascot:List.copyOf(manager.getMascots()))if(mascot.getImageSet().equals(id))mascot.dispose();
   for(Mascot mascot:List.copyOf(manager.getAdded()))if(mascot.getImageSet().equals(id))mascot.dispose();
  }
 }
 public boolean reload(String id){return Main.getInstance().reloadCharacter(id);}
}
