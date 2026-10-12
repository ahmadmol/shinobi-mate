import com.group_finity.mascot.Main;
import com.group_finity.mascot.audio.*;
import com.group_finity.mascot.settings.SettingsStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Compatibility entry point for the product/combat check scripts. */
public class AudioCheck {
 static void require(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 static void drain()throws Exception{AudioManager.global().flush().get(10,TimeUnit.SECONDS);}
 public static void main(String[] args){try {
  var props=Main.getInstance().getProperties();props.putAll(SettingsStore.defaults());props.setProperty("Sounds","true");props.setProperty("CombatSound","true");
  props.setProperty("MasterVolume","25");props.setProperty("CharacterVolume","40");props.setProperty("CombatVolume","20");props.setProperty("MovementVolume","30");props.setProperty("UIVolume","10");
  require(Math.abs(AudioManager.gain(SoundEvent.Category.CHARACTER)-.1)<.001,"character gain");
  require(Math.abs(AudioManager.gain(SoundEvent.Category.COMBAT)-.05)<.001,"combat gain");
  require(Math.abs(AudioManager.gain(SoundEvent.Category.UI)-.025)<.001,"UI gain");
  var scope=new AudioManager.Scope();AudioManager.global().playCharacter("SasukeShippuden","signature.chidori.loop",scope,0);drain();
  for(int i=0;i<1000;i++)SoundSystem.play(SoundSystem.Category.UI);drain();require(SoundSystem.clipCount()<=12,"unbounded voices");
  props.setProperty("Mute","true");SoundSystem.refresh();drain();require(SoundSystem.clipCount()==0,"mute retained voice");
  for(var c:SoundEvent.Category.values())require(AudioManager.gain(c)==0,"category mute");
  SoundSystem.close();require(SoundSystem.clipCount()==0&&SoundSystem.cacheCount()==0,"close retained native audio or PCM");
  require(AudioManager.global().snapshot().nativeLines()==0,"native line survived close");
  System.out.println("PASS pack audio, gain/mute, bounded voices, native mixer line cleanup and PCM cleanup");System.exit(0);
 }catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
}
