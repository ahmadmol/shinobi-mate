package com.group_finity.mascot.sound;
import java.nio.file.Path;
/** Registration only; the audio worker decodes lazily and owns all native resources. */
public class SoundLoader {
 public static void load(String name,float volume){Sounds.register(name+volume,Path.of(name),volume);}
}
