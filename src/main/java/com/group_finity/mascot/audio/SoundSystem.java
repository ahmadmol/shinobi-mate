package com.group_finity.mascot.audio;
import javax.sound.sampled.*;
import java.util.Random;
/** Compatibility facade for existing optional companion cues. All playback is centralized. */
public final class SoundSystem {
 public enum Category {UI,CHARACTER,COMBAT,VFX,ATTACK,IMPACT,MOVEMENT}
 public enum Cue {UI,GRUNT,KO,VICTORY,CHARGE,CHAKRA,ELECTRIC,FIRE,DARK,WHOOSH,SWORD,IMPACT,HEAVY,PARRY,BLOCK,DODGE}
 public static final int MAX_CLIPS=AudioManager.MAX_VOICES;
 private SoundSystem(){ }
 public static int clipCount(){return AudioManager.global().snapshot().voices();}
 public static int cacheCount(){return AudioManager.global().snapshot().cacheFiles();}
 private static SoundEvent.Category category(Category c){return switch(c){case ATTACK,IMPACT->SoundEvent.Category.COMBAT;default->SoundEvent.Category.valueOf(c.name());};}
 public static float gain(Category c){return AudioManager.gain(category(c));}
 public static void play(Category c){play(c==Category.CHARACTER?Cue.GRUNT:c==Category.UI?Cue.UI:c==Category.MOVEMENT?Cue.DODGE:Cue.IMPACT,"",0);}
 public static void play(Cue cue,String character,double pan){AudioManager.global().playCharacter(character==null||character.isBlank()||character.equals("generic")?null:character,cue.name().toLowerCase(java.util.Locale.ROOT),null,pan);}
 public static void volume(Clip clip,Category c){/* Legacy callers enqueue refresh; no native audio on EDT. */ AudioManager.global().refresh();}
 public static void refresh(){AudioManager.global().refresh();}
 public static void close(){AudioManager.global().close();}
 public static byte[] synthesize(Cue cue,int identity,int variant) {
  int rate=22050,n=(int)(rate*(switch(cue){case CHARGE,CHAKRA,ELECTRIC,DARK->.24;case KO,VICTORY->.30;default->.13;}));
  byte[] pcm=new byte[n*2];Random noise=new Random(101L+cue.ordinal()*71L+identity*13L+variant);
  double base=180+identity*34+variant*23;
  for(int i=0;i<n;i++) {
   double t=i/(double)n,envelope=Math.sin(Math.PI*t)*Math.exp(-3*t),white=noise.nextDouble()*2-1;
   double tone=Math.sin(2*Math.PI*(base+150*t)*i/rate),sample=switch(cue) {
    case WHOOSH,DODGE,SWORD->white*(cue==Cue.SWORD?.8:.5)+.15*tone;
    case IMPACT,HEAVY,BLOCK->.65*white+.35*Math.sin(2*Math.PI*(cue==Cue.HEAVY?70:115)*i/rate);
    case ELECTRIC,PARRY->.4*white+.5*Math.sin(2*Math.PI*(base*3+1200*t)*i/rate);
    case FIRE,DARK->.6*white+.25*Math.sin(2*Math.PI*base*.5*i/rate);
    case GRUNT,KO->tone*.65+.25*Math.sin(2*Math.PI*base*1.8*i/rate);
    case VICTORY,UI->Math.sin(2*Math.PI*(base*2+(t>.5?180:0))*i/rate)*.7;
    default->tone*.5+.25*Math.sin(2*Math.PI*base*2*i/rate);
   };
   short v=(short)(Math.max(-1,Math.min(1,sample))*5500*envelope);pcm[2*i]=(byte)v;pcm[2*i+1]=(byte)(v>>8);
  }
  return pcm;
 }
}
