package com.group_finity.mascot.interaction;
import com.group_finity.mascot.Manager;
import com.group_finity.mascot.settings.RuntimeSettings;
/** Friendly gestures only; no code path to CombatEngine.start. */
public final class SocialService {
 private long next=System.nanoTime()/1_000_000+60000;
 public void tick(Manager manager){
  long now=System.nanoTime()/1_000_000;
  if(now<next||!RuntimeSettings.flag("SocialInteractions",true)||com.group_finity.mascot.focus.FocusService.session().quiet())return;
  next=now+90000;var pets=manager.getMascots();
  for(int i=0;i<pets.size();i++)for(int j=i+1;j<pets.size();j++){
   var a=pets.get(i);var b=pets.get(j);
   if(manager.getCombat().active(a)||manager.getCombat().active(b)||!a.getCompanionFeatures().resting()||!b.getCompanionFeatures().resting()||a.getAnchor().distance(b.getAnchor())>300)continue;
   if(a.getCompanionFeatures().greet()&&b.getCompanionFeatures().greet()){
    a.setLookRight(a.getAnchor().x<b.getAnchor().x);b.setLookRight(b.getAnchor().x<a.getAnchor().x);return;
   }
  }
 }
}
