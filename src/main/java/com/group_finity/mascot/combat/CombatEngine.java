package com.group_finity.mascot.combat;

import com.group_finity.mascot.*;
import com.group_finity.mascot.image.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.random.RandomGenerator;
import javax.swing.*;

/** Manager-owned combat lifecycle. All state changes use the manager's mascot-list lock. */
public final class CombatEngine {
    private static final Logger log=Logger.getLogger(CombatEngine.class.getName());
    private record Pair(Mascot a,Mascot b,CombatMatch match,CombatEffects effects) { }
    private final Manager manager;
    private final List<Pair> pairs=new ArrayList<>();
    private final Map<Mascot,WeakReference<Mascot>> lastOpponent=new WeakHashMap<>();
    private double accumulatedTicks;
    private final Set<CombatMatch> reported=new HashSet<>();
    public CombatEngine(Manager manager) {this.manager=manager;}
    private Path pack(Mascot m) {
        return AppPaths.pack(m.getImageSet());
    }
    public boolean active(Mascot m) {
        synchronized(manager.getMascots()) {return find(m)!=null;}
    }
    public String statistics(Mascot m) {
        synchronized(manager.getMascots()) {
            Pair p=find(m);if(p==null) return "NON_COMBAT";
            var f=p.a==m?p.match.a:p.match.b;
            return f.state+" | HP "+f.hp+" | Energy "+(int)f.energy;
        }
    }
    private Pair find(Mascot m) {return pairs.stream().filter(p->p.a==m||p.b==m).findFirst().orElse(null);}
    public boolean start(Mascot a,Mascot b) {
        synchronized(manager.getMascots()) {
            if(!com.group_finity.mascot.settings.RuntimeSettings.flag("CombatEnabled",true)) return false;
            if(a==b || !manager.getMascots().contains(a) || !manager.getMascots().contains(b) ||
                    a.isDragging() || b.isDragging() || a.isPaused() || b.isPaused() || find(a)!=null || find(b)!=null) return false;
            CombatEffects effects=null;
            try {
                // Read on every explicit start: add/reload/rematch always observes current pack data.
                CombatProfile pa=CombatProfile.load(pack(a)),pb=CombatProfile.load(pack(b));
                var ba=new MascotBody(a,pa);var bb=new MascotBody(b,pb);
                CombatMatch match=new CombatMatch(ba,pa,bb,pb,RandomGenerator.getDefault());
                effects=new CombatEffects(pa,pb,match.arena());
                a.getHotspots().clear();b.getHotspots().clear();a.clearAffordances();b.clearAffordances();
                a.setSound(null);b.setSound(null);
                pairs.add(new Pair(a,b,match,effects));
                com.group_finity.mascot.audio.AudioManager.play("ui.combat.start");
                lastOpponent.put(a,new WeakReference<>(b));lastOpponent.put(b,new WeakReference<>(a));
                return true;
            } catch(Exception ex) {
                if(effects!=null) effects.close();
                log.log(Level.WARNING,"Cannot start combat for "+a.getImageSet()+" / "+b.getImageSet(),ex);
                return false;
            }
        }
    }
    public void tick() {
        tick(true);
    }
    public void tick(boolean render) {
        synchronized(manager.getMascots()) {
            if(!com.group_finity.mascot.settings.RuntimeSettings.flag("CombatEnabled",true)) {
                for(Pair p:new ArrayList<>(pairs))release(p);return;
            }
            accumulatedTicks+=com.group_finity.mascot.settings.RuntimeSettings.number("CombatSpeed",1,.5,2);
            int steps=(int)accumulatedTicks;accumulatedTicks-=steps;
            for(Pair p:new ArrayList<>(pairs)) {
                try {for(int i=0;i<steps&&!p.match.isStopped();i++){
                    p.match.tick();
                    if(p.match.isFinished()&&reported.add(p.match)){
                        p.a.getCompanionFeatures().combatResult(p.match.winner()==p.match.a.body);
                        p.b.getCompanionFeatures().combatResult(p.match.winner()==p.match.b.body);
                        com.group_finity.mascot.progression.ProgressionService.battle(
                            p.match.winner()==p.match.a.body?p.a.getImageSet():p.b.getImageSet(),
                            p.match.winner()==p.match.a.body?p.b.getImageSet():p.a.getImageSet());
                    }
                    }
                    if(render)p.effects.update(p.match);if(p.match.isStopped()) release(p);}
                catch(RuntimeException ex) {log.log(Level.WARNING,"Combat stopped after update failure",ex);release(p);}
            }
        }
    }
    public void stop(Mascot m) {
        synchronized(manager.getMascots()) {Pair p=find(m);if(p!=null) release(p);}
    }
    public void reload(String imageSet) {
        com.group_finity.mascot.audio.AudioManager.global().reload(imageSet);
        synchronized(manager.getMascots()) {
            for(Pair p:new ArrayList<>(pairs)) if(p.a.getImageSet().equals(imageSet)||p.b.getImageSet().equals(imageSet)) release(p);
        }
    }
    public void forget(Mascot m) {
        synchronized(manager.getMascots()) {
            stop(m);lastOpponent.remove(m);
            lastOpponent.values().removeIf(ref->ref.get()==null||ref.get()==m);
        }
    }
    private void release(Pair p) {
        pairs.remove(p);reported.remove(p.match);p.match.stop();p.effects.close();
        com.group_finity.mascot.audio.AudioManager.play("ui.combat.end");
        for(Mascot m:List.of(p.a,p.b)) if(m.getManager()==manager && manager.getMascots().contains(m)) {
            try {
                var config=Main.getInstance().getConfiguration(m.getImageSet());
                // A fresh Fall action drops wall/ceiling pets safely back into normal behavior.
                m.setBehavior(config.buildBehavior(config.getSchema().getString("Fall")));
            } catch(Exception ex) {log.log(Level.WARNING,"Cannot restore mascot behavior",ex);}
        }
    }
    private String label(Mascot m) {return m.getImageSet()+" #"+m.getInstanceId();}
    public JMenu menu(Mascot owner,ResourceBundle language) {
        synchronized(manager.getMascots()) {
            JMenu combat=new JMenu(word(language,"Combat","Combat").replace("⚔","").strip());
            combat.setIcon(new CombatIcon());
            JMenu opponents=new JMenu(word(language,"CombatChooseOpponent","Choose opponent"));
            boolean locked=find(owner)!=null;
            for(Mascot other:manager.getMascots()) if(other!=owner) {
                JMenuItem item=new JMenuItem(label(other));
                boolean supported=Files.isRegularFile(pack(owner).resolve("conf/combat.xml"))&&
                    Files.isRegularFile(pack(other).resolve("conf/combat.xml"));
                item.setEnabled(!locked && find(other)==null && supported);
                item.addActionListener(event -> {
                    com.group_finity.mascot.audio.AudioManager.play("ui.combat.select");
                    if(!start(owner,other)) Main.showError(word(language,"CombatUnavailable","Cannot start combat. Check the profiles and fighter availability."));
                });
                opponents.add(item);
            }
            if(opponents.getItemCount()==0) {JMenuItem empty=new JMenuItem(word(language,"CombatNoOpponent","No opponent on desktop"));empty.setEnabled(false);opponents.add(empty);}
            opponents.setEnabled(!locked);
            JMenuItem stop=new JMenuItem(word(language,"CombatStop","Stop combat"));stop.setEnabled(locked);stop.addActionListener(e->stop(owner));
            JMenuItem rematch=new JMenuItem(word(language,"CombatRematch","Rematch"));
            WeakReference<Mascot> ref=lastOpponent.get(owner);Mascot previous=ref==null?null:ref.get();
            rematch.setEnabled(!locked&&previous!=null&&manager.getMascots().contains(previous)&&find(previous)==null);
            rematch.addActionListener(e->{if(previous!=null&&!start(owner,previous)) Main.showError(word(language,"CombatUnavailable","Cannot start combat."));});
            combat.add(opponents);combat.add(stop);combat.add(rematch);
            combat.applyComponentOrientation(ComponentOrientation.getOrientation(language.getLocale()));
            return combat;
        }
    }
    private static String word(ResourceBundle language,String key,String fallback) {return language.containsKey(key)?language.getString(key):fallback;}
    private static final class MascotBody implements CombatMatch.Body {
        private final Mascot mascot;
        private final double scale;
        private final Map<String,ImagePair> frames=new HashMap<>();
        private final com.group_finity.mascot.audio.AudioManager.Scope audioScope=new com.group_finity.mascot.audio.AudioManager.Scope();
        private final List<com.group_finity.mascot.audio.AudioManager.Handle> chargeHandles=new ArrayList<>();
        MascotBody(Mascot mascot,CombatProfile profile) throws Exception {
            this.mascot=mascot;
            var settings=Main.getInstance().getProperties();
            scale=Double.parseDouble(settings.getProperty("Scaling","1.0"));
            if(!Double.isFinite(scale)||scale<=0) throw new IllegalArgumentException("Invalid Scaling");
            double opacity=Double.parseDouble(settings.getProperty("Opacity","1.0"));
            for(var anim:profile.animations().values()) for(var frame:anim.frames()) if(!frames.containsKey(frame.image())) {
                String key="/"+mascot.getImageSet()+"/"+frame.image();
                ImagePairLoader.load(key,null,new Point(profile.anchorX(),profile.anchorY()),scale,ImagePairLoader.Filter.BICUBIC,opacity);
                ImagePair source=ImagePairs.getImagePair(key);
                var left=source.getImage(false);var right=source.getImage(true);
                int cx=(int)Math.round(profile.anchorX()*scale),cy=(int)Math.round(profile.anchorY()*scale);
                // Ordinary actions can cache this PNG with wall/hanging/throw anchors.
                // Share the native bitmap, but give combat its own stable feet anchor.
                frames.put(frame.image(),new ImagePair(
                    new MascotImage(left.image(),new Point(cx,cy),left.size()),
                    new MascotImage(right.image(),new Point(right.size().width-cx,cy),right.size())));
            }
        }
        public double x() {return mascot.getAnchor().x;}
        public double y() {return mascot.getAnchor().y;}
        public double scale() {return scale;}
        public Rectangle workArea() {
            // Win32 work-area coordinates can be physical pixels while Swing uses logical pixels.
            // Select one real monitor, including its taskbar insets, in the same space as our windows.
            if (NativeFactory.getInstance() instanceof com.group_finity.mascot.win.NativeFactoryImpl) {
                Rectangle selected=null;double closest=Double.MAX_VALUE;
                Point anchor=mascot.getAnchor();
                for(GraphicsDevice device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    var gc=device.getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());
                    Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
                    r.x+=insets.left;r.y+=insets.top;r.width-=insets.left+insets.right;r.height-=insets.top+insets.bottom;
                    double dx=Math.max(r.x-anchor.x,Math.max(0,anchor.x-r.getMaxX()));
                    double dy=Math.max(r.y-anchor.y,Math.max(0,anchor.y-r.getMaxY()));
                    double distance=dx*dx+dy*dy;
                    if(distance<closest) {closest=distance;selected=r;}
                }
                if(selected!=null) return selected;
            }
            return mascot.getEnvironment().getWorkArea().toRectangle();
        }
        public void pose(double x,double y,boolean right,String image) {
            mascot.setAnchor(new Point((int)Math.round(x),(int)Math.round(y)));mascot.setLookRight(right);
            mascot.setImage(frames.get(image).getImage(right));
        }
        public boolean available() {return mascot.getManager()!=null;}
        public void sound(String event,String character,double pan) {
            com.group_finity.mascot.audio.AudioManager.global().playCharacter(character,event,audioScope,pan);
        }
        public void chargeSound(String event,String character,double pan){chargeHandles.add(com.group_finity.mascot.audio.AudioManager.global().playCharacter(character,event,audioScope,pan));}
        public void stopChargeSounds(){for(var h:chargeHandles)com.group_finity.mascot.audio.AudioManager.global().stop(h);chargeHandles.clear();}
        public void stopSounds(){stopChargeSounds();com.group_finity.mascot.audio.AudioManager.global().stop(audioScope);}
        public boolean suspended() {return !mascot.isAnimationActive() || mascot.isDragging();}
    }
    private static final class CombatIcon implements Icon {
        public int getIconWidth(){return 16;}public int getIconHeight(){return 16;}
        public void paintIcon(Component c,Graphics g,int x,int y){
            Graphics2D p=(Graphics2D)g.create();p.setColor(new Color(155,173,210));p.setStroke(new BasicStroke(2));
            p.drawLine(x+3,y+2,x+13,y+13);p.drawLine(x+13,y+2,x+3,y+13);
            p.drawLine(x+2,y+9,x+7,y+14);p.drawLine(x+9,y+14,x+14,y+9);p.dispose();
        }
    }
}
