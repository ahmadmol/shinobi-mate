import com.group_finity.mascot.*;
import com.group_finity.mascot.progression.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.character.ContentAccess;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
public class ProgressionCheck extends CharacterCheck {
 public static void main(String[] args){
  try{testProgression();System.out.println("PASS cosmetic XP, persistent profile, optional event pacing, daily/ISO-week rollover and entitlement models; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 static void testProgression()throws Exception {
  Properties p=new Properties();ProgressionModel model=new ProgressionModel(p);
  require(model.profile("Test").level()==1,"initial level");model.award("Test",99);require(model.profile("Test").level()==1,"level before threshold");model.award("Test",1);require(model.profile("Test").level()==2,"level threshold");
  require(model.profile("Other").xp()==0,"profile isolation");
  LocalDate dec=LocalDate.of(2026,12,31),jan=dec.plusDays(1);
  model.event(ProgressionModel.Event.INTERACTION,dec);require(model.challenges(dec,false).getFirst().complete(),"daily interaction");
  model.event(ProgressionModel.Event.FOCUS_COMPLETED,jan);
  require(model.challenges(jan,false).getFirst().current()==0,"daily period did not reset");
  require(model.challenges(jan,true).getFirst().current()==1,"ISO week split at new year");
  model.event(ProgressionModel.Event.RUN_TWO,jan);model.event(ProgressionModel.Event.RUN_TWO,jan);
  require(model.challenges(jan,true).getLast().current()==1,"run-two restart farming");
  model.event(ProgressionModel.Event.BATTLE_COMPLETED,jan.plusDays(8));require(model.challenges(jan.plusDays(8),true).getFirst().current()==0,"weekly period did not reset");
  Path temp=Files.createTempFile("shinobi-progress-",".properties");SettingsStore.save(model.snapshot(),temp);Properties restored=new Properties();try(var in=Files.newInputStream(temp)){restored.load(in);}
  require(new ProgressionModel(restored).profile("Test").xp()==100,"profile persistence");Files.delete(temp);
  for(var access:ContentAccess.Access.values()){
   var content=new ContentAccess("Test",ContentAccess.Kind.CHARACTER,access,ContentAccess.InstallStatus.INSTALLED);
   require(content.usable()==(access==ContentAccess.Access.FREE||access==ContentAccess.Access.OWNED),"content access semantics");
  }
  System.setProperty("app.dir",Path.of("output/product-ui").toAbsolutePath().toString());var props=Main.getInstance().getProperties();
  props.setProperty("ProgressionEnabled","false");props.setProperty("ChallengesEnabled","false");ProgressionService.interact("Paced");
  require(ProgressionService.instance().model().profile("Paced").xp()==0,"disabled progression awarded XP");
  props.setProperty("ProgressionEnabled","true");props.setProperty("ChallengesEnabled","true");
  ProgressionService.interact("Paced");ProgressionService.interact("Paced");require(ProgressionService.instance().model().profile("Paced").xp()==1,"click pacing");
  ProgressionService.battle("Paced","Other");require(ProgressionService.instance().model().profile("Paced").xp()==11,"win award");
  SwingUtilities.invokeAndWait(()->{
   try{com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
    for(String language:java.util.List.of("ar","en-GB")){
     props.setProperty("Language",language);ProgressionDialog d=new ProgressionDialog(null);d.setVisible(true);snapshot(d,"progression-"+language);d.dispose();
    }
   }catch(Exception ex){throw new RuntimeException(ex);}
  });
  ProgressionService.close();Path progress=SettingsStore.path().getParent().resolve("progress.properties");
  require(Files.isRegularFile(progress),"exit did not flush profile");Files.delete(progress);
 }
}
