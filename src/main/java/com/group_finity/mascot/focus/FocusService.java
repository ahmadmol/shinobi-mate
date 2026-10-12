package com.group_finity.mascot.focus;
import com.group_finity.mascot.Manager;
import com.group_finity.mascot.settings.RuntimeSettings;
import com.group_finity.mascot.ui.Theme;
import java.awt.*;
import javax.swing.SwingUtilities;
public final class FocusService {
 private static final FocusSession session=new FocusSession();
 private FocusService(){ }
 public static FocusSession session(){return session;}
 public static long now(){return System.nanoTime()/1_000_000;}
 public static void start(){session.start(now(),(long)(RuntimeSettings.number("FocusMinutes",25,1,180)*60000),(long)(RuntimeSettings.number("BreakMinutes",5,1,60)*60000));}
 public static void tick(Manager manager){
  var event=session.tick(now());if(event==FocusSession.Event.NONE)return;
  if(event==FocusSession.Event.FOCUS_COMPLETED)com.group_finity.mascot.progression.ProgressionService.focus(manager);
  if(event==FocusSession.Event.FOCUS_COMPLETED)for(var m:manager.getMascots())if(!manager.getCombat().active(m))m.getCompanionFeatures().focusComplete();
  if(!RuntimeSettings.flag("FocusReminders",true))return;
  SwingUtilities.invokeLater(()->{
   if(SystemTray.isSupported()){
    TrayIcon[] icons=SystemTray.getSystemTray().getTrayIcons();if(icons.length>0)icons[0].displayMessage("ShinobiMate",
     event==FocusSession.Event.FOCUS_COMPLETED?Theme.t("أحسنت! حان وقت استراحة قصيرة.","Well done! Time for a short break."):Theme.t("انتهت الاستراحة، لنبدأ جلسة هادئة.","Break is over. Let's start a quiet session."),TrayIcon.MessageType.INFO);
   }
  });
 }
}
