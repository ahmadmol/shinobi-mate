package com.group_finity.mascot.audio;

import com.group_finity.mascot.AppPaths;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import javax.sound.sampled.*;
import static com.group_finity.mascot.audio.SoundEvent.Category.*;

/** A fixed cache worker and PCM mixer own all audio I/O. Callers only enqueue commands. */
public final class AudioManager implements AutoCloseable {
    public static final int MAX_VOICES=12, MAX_CACHE_FILES=128, MAX_CACHE_BYTES=8*1024*1024, MAX_PENDING=64;
    private static final Logger LOG=Logger.getLogger(AudioManager.class.getName());
    private static final AudioFormat FORMAT=new AudioFormat(22050,16,2,true,false);
    private static final AudioManager GLOBAL=new AudioManager(new NativeOutput());
    public static AudioManager global(){return GLOBAL;}
    public static void play(String event){GLOBAL.playCharacter(null,event,null,0);}
    public static final class Scope {private final AtomicBoolean alive=new AtomicBoolean(true);}
    public static final class Handle {
        private final AtomicBoolean cancelled=new AtomicBoolean();
        public boolean isCancelled(){return cancelled.get();}
    }
    /** Injectable output device for deterministic arbitration tests; production uses Java Sound. */
    public interface Voice extends AutoCloseable {
        void start(byte[] pcm,boolean loop,float volume,float pan) throws Exception;
        boolean running(); void volume(float volume); void close();
    }
    public interface Backend {
        Voice open() throws Exception;
        default void close(){ }
        default void warm(){ }
        default int lineCount(){return 0;}
        default long deviceFailures(){return 0;}
    }
    public record Snapshot(int voices,int loops,int cacheFiles,int cacheBytes,int pending,long played,long dropped,
            long evicted,long failures,int peakVoices,int peakPending,String workerThread,int nativeLines,long deviceFailures) { }
    private record Playing(Voice voice,SoundEvent event,String key,Path file,Scope scope,Handle handle,long started,long expires) { }
    private record Sample(byte[] pcm) { }
    private final Backend backend;
    private final ScheduledThreadPoolExecutor worker;
    private final Semaphore pending=new Semaphore(MAX_PENDING);
    private final List<Playing> voices=new ArrayList<>();
    private final LinkedHashMap<Path,Sample> cache=new LinkedHashMap<>(16,.75f,true);
    private final LinkedHashMap<Path,Map<String,SoundEvent>> packs=new LinkedHashMap<>(16,.75f,true);
    private final LinkedHashMap<String,Long> cooldowns=new LinkedHashMap<>();
    private final LinkedHashMap<String,Integer> lastVariants=new LinkedHashMap<>();
    private final LinkedHashMap<Path,Long> failedFiles=new LinkedHashMap<>();
    public record Playback(String id,Path file,long nanos,String thread,boolean loop){ }
    private volatile java.util.function.Consumer<Playback> observer;
    public void observe(java.util.function.Consumer<Playback> listener){observer=listener;}
    private final Set<String> warned=new LinkedHashSet<>();
    private final Random random=new Random();
    private final AtomicBoolean closed=new AtomicBoolean(), refreshPending=new AtomicBoolean(),reapPending=new AtomicBoolean();
    private int cacheBytes,peakVoices,peakPending;
    private long played,dropped,evicted,failures,retryAt;
    private volatile Snapshot snapshot=new Snapshot(0,0,0,0,0,0,0,0,0,0,0,"",0,0);
    public AudioManager(Backend backend) {
        this.backend=Objects.requireNonNull(backend);
        worker=new ScheduledThreadPoolExecutor(1,r->{Thread t=new Thread(r,"ShinobiMate audio");t.setDaemon(true);return t;});
        worker.setRemoveOnCancelPolicy(true);
        worker.scheduleWithFixedDelay(()->{try{reap();publish();}catch(RuntimeException ex){LOG.log(Level.WARNING,"Audio maintenance",ex);}},50,50,TimeUnit.MILLISECONDS);
    }
    public Snapshot snapshot(){return snapshot;}
    public Handle playCharacter(String character,String event,Scope scope,double pan) {
        // Reserve eight queue positions for combat/signatures during a movement flood.
        if(event!=null&&event.startsWith("movement.")&&pending.availablePermits()<=8){Handle h=new Handle();h.cancelled.set(true);return h;}
        return enqueue(scope,h->{Path root=character==null?AppPaths.application().resolve("audio/common"):AppPaths.pack(character);
            playNow(resolve(root,event),scope,h,pan);});
    }
    public Handle play(Path root,String event,Scope scope,double pan) {return enqueue(scope,h->playNow(resolve(root,event),scope,h,pan));}
    public Handle play(SoundEvent event,Scope scope,double pan) {return enqueue(scope,h->playNow(event,scope,h,pan));}
    private Handle enqueue(Scope scope,java.util.function.Consumer<Handle> action) {
        Handle h=new Handle();long requested=System.nanoTime();
        if(closed.get() || scope!=null&&!scope.alive.get() || !pending.tryAcquire()){h.cancelled.set(true);return h;}
        try{worker.execute(()->{
            try {
                peakPending=Math.max(peakPending,MAX_PENDING-pending.availablePermits());
                if(closed.get()||h.cancelled.get() || scope!=null&&!scope.alive.get() || System.nanoTime()-requested>TimeUnit.MILLISECONDS.toNanos(180)){dropped++;return;}
                action.accept(h);
            }catch(Exception ex){failures++;warn("play:"+ex.getClass().getName(),"Optional audio failed: "+ex.getMessage());}
            finally{pending.release();publish();}
        });}catch(RejectedExecutionException ex){pending.release();h.cancelled.set(true);}
        return h;
    }
    private Map<String,SoundEvent> catalog(Path root) {
        root=root.toAbsolutePath().normalize();
        var found=packs.get(root);if(found!=null)return found;
        found=AudioPack.load(root);packs.put(root,found);
        if(packs.size()>32)packs.remove(packs.keySet().iterator().next());return found;
    }
    private SoundEvent resolve(Path root,String id) {
        if(id==null||id.isBlank())return null;
        SoundEvent e=catalog(root).get(id);
        Path common=AppPaths.application().resolve("audio/common");
        if(e==null)e=catalog(common).get(id);
        if(e==null)warn(root+":"+id,"Missing optional audio event "+id+" in "+root);
        return e;
    }
    public void preload(String character) {
        enqueue(null,h->{if(!RuntimeSettings.flag("Mute",false)&&RuntimeSettings.flag("Sounds",true)&&RuntimeSettings.number("MasterVolume",70,0,100)>0)backend.warm();
            Path root=AppPaths.pack(character);for(var e:catalog(root).values())for(var p:e.variations())
            if(cacheBytes<MAX_CACHE_BYTES/2)sample(p);});
    }
    private void playNow(SoundEvent e,Scope scope,Handle handle,double pan) {
        reap();if(e==null||gain(e.category())==0)return;
        long now=System.nanoTime();if(now<retryAt){dropped++;return;}
        String key=e.variations().getFirst().getParent()+":"+e.id();
        if(now-cooldowns.getOrDefault(key,Long.MIN_VALUE/2)<TimeUnit.MILLISECONDS.toNanos(e.cooldown())){dropped++;return;}
        long same=voices.stream().filter(v->v.key.equals(key)).count();
        if(same>=e.maxConcurrent()){dropped++;return;}
        int categoryLimit=switch(e.category()){case MOVEMENT->3;case UI->2;case CHARACTER->2;default->8;};
        boolean categoryFull=voices.stream().filter(v->v.event.category()==e.category()).count()>=categoryLimit;
        if(categoryFull||voices.size()>=MAX_VOICES) {
            Playing victim=voices.stream().filter(v->!categoryFull||v.event.category()==e.category())
                .filter(v->v.event.priority()<e.priority()).min(Comparator.comparingInt(v->v.event.priority())).orElse(null);
            if(victim==null){dropped++;return;}remove(victim);evicted++;
        }
        int count=e.variations().size(),last=lastVariants.getOrDefault(key,-1),variant;
        if(count<2)variant=0;else if(last<0)variant=random.nextInt(count);else{variant=random.nextInt(count-1);if(variant>=last)variant++;}
        Path file=e.variations().get(variant);Sample decoded=sample(file);if(decoded==null)return;
        // A stop can arrive during decoding. It must cancel a pending loop before line acquisition.
        if(handle.cancelled.get()||scope!=null&&!scope.alive.get())return;
        Voice voice=null;
        try {
            voice=backend.open();voice.start(decoded.pcm,e.loop(),(float)(gain(e.category())*e.volume()),(float)Math.max(-.6,Math.min(.6,Double.isFinite(pan)?pan:0)));
            long started=System.nanoTime();
            voices.add(new Playing(voice,e,key,file,scope,handle,started,started+TimeUnit.MILLISECONDS.toNanos(e.maxDurationMs())));
            cooldowns.put(key,now);lastVariants.put(key,variant);trim(cooldowns,512);trim(lastVariants,512);
            played++;peakVoices=Math.max(peakVoices,voices.size());
            var listener=observer;if(listener!=null)try{listener.accept(new Playback(e.id(),file,System.nanoTime(),Thread.currentThread().getName(),e.loop()));}catch(RuntimeException ex){warn("observer","Audio diagnostic listener failed");}
        }catch(Exception ex){if(voice!=null)voice.close();failures++;retryAt=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);warn("device","Audio device unavailable; playback will retry in 30 seconds: "+ex.getMessage());}
    }
    private Sample sample(Path file) {
        Sample found=cache.get(file);if(found!=null)return found;
        if(System.nanoTime()<failedFiles.getOrDefault(file,0L))return null;
        try {
            if(!Files.isRegularFile(file)||Files.size(file)>2*1024*1024)throw new IllegalArgumentException("Missing/oversized WAV");
            // Follow symlinks only inside the declared asset's pack root.
            Path real=file.toRealPath();Path assetRoot=file;
            while(assetRoot.getParent()!=null&&!assetRoot.getFileName().toString().equals("sounds"))assetRoot=assetRoot.getParent();
            if(assetRoot.getParent()!=null&&!real.startsWith(assetRoot.getParent().toRealPath()))throw new IllegalArgumentException("Audio escapes pack");
            try(AudioInputStream source=AudioSystem.getAudioInputStream(file.toFile());AudioInputStream decoded=AudioSystem.getAudioInputStream(FORMAT,source)) {
                byte[] pcm=decoded.readNBytes(1024*1024+1);
                if(pcm.length==0||pcm.length>1024*1024||pcm.length%FORMAT.getFrameSize()!=0)throw new IllegalArgumentException("Invalid/oversized PCM");
                while(!cache.isEmpty()&&(cache.size()>=MAX_CACHE_FILES||cacheBytes+pcm.length>MAX_CACHE_BYTES)) {
                    var it=cache.entrySet().iterator();var old=it.next();cacheBytes-=old.getValue().pcm.length;it.remove();
                }
                found=new Sample(pcm);cache.put(file,found);cacheBytes+=pcm.length;return found;
            }
        }catch(Exception ex){failures++;failedFiles.put(file,System.nanoTime()+TimeUnit.SECONDS.toNanos(30));trim(failedFiles,128);warn(file.toString(),"Missing/invalid optional audio "+file+": "+ex.getMessage());return null;}
    }
    private void reap() {
        long now=System.nanoTime();
        for(var it=voices.iterator();it.hasNext();) {
            Playing p=it.next();
            // Give newly admitted voices one maintenance interval before testing natural completion.
            if(p.handle.cancelled.get()||p.scope!=null&&!p.scope.alive.get()||now>=p.expires||
                    now-p.started>TimeUnit.MILLISECONDS.toNanos(60)&&!p.voice.running()||gain(p.event.category())==0) {
                p.voice.close();it.remove();
            }
        }
    }
    private void remove(Playing p){p.handle.cancelled.set(true);p.voice.close();voices.remove(p);}
    private void requestReap(){if(reapPending.compareAndSet(false,true))control(()->{try{reap();publish();}finally{reapPending.set(false);}});}
    public void stop(Handle handle) {if(handle!=null){handle.cancelled.set(true);requestReap();}}
    public void stop(Scope scope) {if(scope!=null){scope.alive.set(false);requestReap();}}
    public void stopAll(){control(()->{for(var v:voices){v.handle.cancelled.set(true);v.voice.close();}voices.clear();publish();});}
    public void stopLegacy(String pack,String relative){control(()->{String name=relative.replace('\\','/');
        for(var it=voices.iterator();it.hasNext();){var v=it.next();if(v.event.id().equals("legacy")&&v.file.toString().replace('\\','/').endsWith(name)){v.handle.cancelled.set(true);v.voice.close();it.remove();}}publish();});}
    public void reload(String character) {
        control(()->{Path root=AppPaths.pack(character).toAbsolutePath().normalize();packs.remove(root);
            for(var it=voices.iterator();it.hasNext();){var p=it.next();if(p.file.startsWith(root)){p.handle.cancelled.set(true);p.voice.close();it.remove();}}
            cache.entrySet().removeIf(entry->{if(entry.getKey().startsWith(root)){cacheBytes-=entry.getValue().pcm.length;return true;}return false;});
            cooldowns.clear();lastVariants.clear();failedFiles.clear();warned.clear();publish();});
    }
    public void refresh() {
        if(refreshPending.compareAndSet(false,true))control(()->{try{reap();for(var v:voices)v.voice.volume((float)(gain(v.event.category())*v.event.volume()));publish();}finally{refreshPending.set(false);}});
    }
    private void control(Runnable task){if(!closed.get())try{worker.execute(task);}catch(RejectedExecutionException ignored){ }}
    public CompletableFuture<Snapshot> flush() {
        var done=new CompletableFuture<Snapshot>();if(closed.get()){done.complete(snapshot);return done;}
        control(()->{reap();publish();done.complete(snapshot);});return done;
    }
    private void publish(){snapshot=new Snapshot(voices.size(),(int)voices.stream().filter(v->v.event.loop()).count(),cache.size(),cacheBytes,MAX_PENDING-pending.availablePermits(),played,dropped,evicted,failures,peakVoices,peakPending,Thread.currentThread().getName(),backend.lineCount(),backend.deviceFailures());}
    private void warn(String key,String message){if(warned.add(key)){LOG.warning(message);if(warned.size()>256)warned.remove(warned.iterator().next());}}
    private static <K,V> void trim(LinkedHashMap<K,V> map,int size){while(map.size()>size)map.remove(map.keySet().iterator().next());}
    public static float gain(SoundEvent.Category c) {
        if(RuntimeSettings.flag("Mute",false)||!RuntimeSettings.flag("Sounds",true))return 0;
        if((c==COMBAT||c==WEAPON||c==VFX)&&!RuntimeSettings.flag("CombatSound",true))return 0;
        double master=RuntimeSettings.number("MasterVolume",70,0,100)/100;
        String key=switch(c){case MOVEMENT->"MovementVolume";case COMBAT,WEAPON,VFX->"CombatVolume";case CHARACTER->"CharacterVolume";case UI->"UIVolume";};
        double fallback=switch(c){case MOVEMENT->45;case UI->30;case CHARACTER->70;default->60;};
        return (float)(master*RuntimeSettings.number(key,fallback,0,100)/100);
    }
    @Override public void close() {
        if(!closed.compareAndSet(false,true))return;
        CompletableFuture<Void> done=new CompletableFuture<>();
        worker.execute(()->{for(var v:voices){v.handle.cancelled.set(true);v.voice.close();}voices.clear();cache.clear();packs.clear();cooldowns.clear();lastVariants.clear();failedFiles.clear();cacheBytes=0;backend.close();publish();done.complete(null);});
        worker.shutdown();
        if(!javax.swing.SwingUtilities.isEventDispatchThread())try{done.get(3,TimeUnit.SECONDS);}catch(Exception ex){LOG.warning("Audio shutdown still pending");}
    }
    /** Twelve software voices share one output line. No per-event Clip, driver thread or open/close. */
    private static final class NativeOutput implements Backend {
        private final Set<MixedVoice> active=ConcurrentHashMap.newKeySet();
        private final Object wake=new Object();
        private final AtomicBoolean stopped=new AtomicBoolean();
        private volatile Thread renderer;
        private volatile int lines;
        private volatile long failures;
        private final AtomicBoolean warmed=new AtomicBoolean();
        private volatile boolean warmRequested;
        private long retryAfter;
        public synchronized Voice open() {
            if(stopped.get())throw new IllegalStateException("Audio output closed");
            if(renderer==null||!renderer.isAlive()){renderer=new Thread(this::render,"ShinobiMate audio mixer");renderer.setDaemon(true);renderer.start();}
            return new MixedVoice(this);
        }
        public int lineCount(){return lines;}
        public long deviceFailures(){return failures;}
        public void warm(){if(warmed.compareAndSet(false,true)){warmRequested=true;open();synchronized(wake){wake.notifyAll();}}}
        private void render() {
            SourceDataLine line=null;byte[] output=new byte[256*4];int[] mixed=new int[256*2];long idleSince=0;
            try {
                while(!stopped.get()) {
                    if(active.isEmpty()) {
                        if(idleSince==0)idleSince=System.nanoTime();
                        if(line!=null&&System.nanoTime()-idleSince>TimeUnit.MILLISECONDS.toNanos(400)) {line.stop();line.flush();line.close();line=null;lines=0;}
                        if(line==null&&!warmRequested){synchronized(wake){if(active.isEmpty()&&!stopped.get())wake.wait(50);}continue;}
                    }else idleSince=0;
                    if(line==null) {
                        if(System.nanoTime()<retryAfter){for(var v:active)v.close();continue;}
                        SourceDataLine candidate=null;
                        try {
                            candidate=AudioSystem.getSourceDataLine(FORMAT);candidate.open(FORMAT,4096);candidate.start();line=candidate;lines=1;warmRequested=false;
                        }catch(Exception ex){warmRequested=false;if(candidate!=null)candidate.close();failures++;retryAfter=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);for(var v:active)v.close();LOG.log(Level.WARNING,"Audio output unavailable; retry in 30 seconds",ex);continue;}
                    }
                    Arrays.fill(mixed,0);
                    for(MixedVoice voice:active) {
                        if(!voice.running)continue;
                        byte[] pcm=voice.pcm;int cursor=voice.cursor;
                        float volume=voice.volume,pan=voice.pan;
                        float left=volume*(pan>0?1-pan:1),right=volume*(pan<0?1+pan:1);
                        for(int frame=0;frame<256&&voice.running;frame++) {
                            if(cursor>=pcm.length){if(voice.loop)cursor=0;else{voice.close();break;}}
                            int l=(short)((pcm[cursor]&255)|(pcm[cursor+1]<<8)),r=(short)((pcm[cursor+2]&255)|(pcm[cursor+3]<<8));
                            mixed[frame*2]+=(int)(l*left);mixed[frame*2+1]+=(int)(r*right);cursor+=4;
                        }
                        voice.cursor=cursor;
                    }
                    for(int i=0;i<mixed.length;i++){int v=Math.max(-32000,Math.min(32000,mixed[i]));output[i*2]=(byte)v;output[i*2+1]=(byte)(v>>8);}
                    // The only blocking audio operation is isolated on this dedicated fixed renderer.
                    line.write(output,0,output.length);
                }
            }catch(InterruptedException ex){Thread.currentThread().interrupt();}
            catch(RuntimeException ex){failures++;retryAfter=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);LOG.log(Level.WARNING,"Audio mixer stopped; next playback will retry",ex);}
            finally {for(var v:active)v.close();if(line!=null){try{line.stop();line.flush();}finally{line.close();}}lines=0;}
        }
        public void close() {
            stopped.set(true);for(var v:active)v.close();synchronized(wake){wake.notifyAll();}
            Thread t=renderer;if(t!=null&&t!=Thread.currentThread())try{t.join(1500);}catch(InterruptedException ex){Thread.currentThread().interrupt();}
        }
    }
    private static final class MixedVoice implements Voice {
        private final NativeOutput output;
        private byte[] pcm;
        private boolean loop;
        private volatile boolean running;
        private volatile float volume,pan;
        private int cursor;
        MixedVoice(NativeOutput output){this.output=output;}
        public void start(byte[] pcm,boolean loop,float volume,float pan){this.pcm=pcm;this.loop=loop;this.volume=volume;this.pan=pan;running=true;output.active.add(this);synchronized(output.wake){output.wake.notifyAll();}}
        public boolean running(){return running;}
        public void volume(float value){volume=value;if(value<=0)close();}
        public void close(){running=false;output.active.remove(this);}
    }
}
