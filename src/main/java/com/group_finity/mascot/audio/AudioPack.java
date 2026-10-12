package com.group_finity.mascot.audio;

import com.group_finity.mascot.pack.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

/** Optional sounds.json; malformed audio never makes a character or attack unavailable. */
public final class AudioPack {
    private static final Logger LOG=Logger.getLogger(AudioPack.class.getName());
    public static Map<String,SoundEvent> load(Path root) {
        Map<String,SoundEvent> result=new LinkedHashMap<>();
        Path manifest=root.resolve("conf/sounds.json");
        if (!Files.isRegularFile(manifest)) return Map.of();
        try {
            if(Files.size(manifest)>65536)throw new IllegalArgumentException("Sound manifest exceeds 64 KiB");
            var json=JsonData.object(Files.readString(manifest));
            if(number(json,"version",1)!=1)throw new IllegalArgumentException("Unsupported sounds version");
            if(!(json.get("events") instanceof Map<?,?> events)||events.size()>128)throw new IllegalArgumentException("Expected up to 128 events");
            for(var entry:events.entrySet())try {
                String id=(String)entry.getKey();
                @SuppressWarnings("unchecked") var data=(Map<String,Object>)entry.getValue();
                var paths=JsonData.strings(data,"variations").stream().map(s->PackPaths.inside(root,s)).toList();
                result.put(id,new SoundEvent(id,SoundEvent.Category.valueOf(JsonData.string(data,"category","COMBAT")),paths,
                    number(data,"volume",.6),integer(data,"cooldown",100),integer(data,"maxConcurrent",2),
                    JsonData.flag(data,"loop",false),integer(data,"priority",50),integer(data,"maxDurationMs",3000)));
            }catch(Exception ex){LOG.log(Level.WARNING,"Ignoring invalid sound event "+entry.getKey()+" in "+manifest,ex);}
        }catch(Exception ex){LOG.log(Level.WARNING,"Optional audio manifest ignored: "+manifest,ex);}
        return Map.copyOf(result);
    }
    private static double number(Map<String,Object> data,String key,double fallback) {
        Object value=data.get(key);if(value==null)return fallback;
        if(!(value instanceof Number n)||!Double.isFinite(n.doubleValue()))throw new IllegalArgumentException(key);
        return n.doubleValue();
    }
    private static int integer(Map<String,Object> data,String key,int fallback) {
        double n=number(data,key,fallback);if(n!=Math.rint(n))throw new IllegalArgumentException(key);return (int)n;
    }
    private AudioPack(){ }
}
