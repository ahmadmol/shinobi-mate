import com.group_finity.mascot.Main;
import com.group_finity.mascot.audio.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/** Runs through the actual portable launcher and bundled runtime, independent of the build JDK. */
public class AudioRuntimeSmoke {
 static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void drain()throws Exception{AudioManager.global().flush().get(5,TimeUnit.SECONDS);}
 public static void main(String[]args){try {
  Main.getInstance().getProperties().setProperty("Sounds","true");Main.getInstance().getProperties().setProperty("Mute","false");
  var audio=AudioManager.global();audio.preload("NarutoShippuden");drain();audio.preload("SasukeShippuden");drain();
  var electric=new AudioManager.Scope();audio.playCharacter("SasukeShippuden","signature.chidori.loop",electric,0);drain();
  long deviceReady=System.nanoTime();while(audio.snapshot().nativeLines()==0&&audio.snapshot().deviceFailures()==0&&System.nanoTime()-deviceReady<TimeUnit.SECONDS.toNanos(1)){Thread.sleep(20);drain();}
  require(audio.snapshot().nativeLines()==1&&audio.snapshot().loops()==1&&audio.snapshot().deviceFailures()==0,"bundled runtime could not play native electric loop: "+audio.snapshot()+" gain="+AudioManager.gain(SoundEvent.Category.VFX)+" app="+com.group_finity.mascot.AppPaths.application()+" Java="+System.getProperty("java.version"));
  audio.stop(electric);drain();Thread.sleep(550);drain();require(audio.snapshot().voices()==0&&audio.snapshot().nativeLines()==0,"bundled runtime cancellation/idle cleanup");
  var chakra=new AudioManager.Scope();var handle=audio.playCharacter("NarutoShippuden","signature.rasengan.charge",chakra,-.3);drain();Thread.sleep(120);
  audio.stop(handle);audio.playCharacter("NarutoShippuden","signature.rasengan.dash",chakra,0);Thread.sleep(100);audio.playCharacter("NarutoShippuden","signature.rasengan.impact",chakra,.3);drain();Thread.sleep(100);drain();
  require(audio.snapshot().played()>=4&&audio.snapshot().nativeLines()==1,"bundled runtime signature stages");audio.stop(chakra);drain();audio.close();
  require(audio.snapshot().voices()==0&&audio.snapshot().cacheBytes()==0&&audio.snapshot().nativeLines()==0,"bundled runtime shutdown leak");
  Files.writeString(Path.of("output/audio-phase1/runtime-smoke.log"),"PASS portable launcher / bundled Java "+System.getProperty("java.version")+" / real device / Chidori loop cancellation / Rasengan charge-dash-impact / source-line idle cleanup / shutdown.\n"+audio.snapshot()+"\n");System.exit(0);
 }catch(Throwable ex){try{Files.writeString(Path.of("output/audio-phase1/runtime-smoke.log"),"FAIL "+ex);}catch(Exception ignored){ }ex.printStackTrace();System.exit(1);}}
}
