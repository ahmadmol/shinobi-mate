package com.group_finity.mascot.combat;

import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.Native;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.*;

/** One reusable, click-through overlay per pair; pack PNGs are decoded once per match. */
public final class CombatEffects implements AutoCloseable {
    private record Draw(BufferedImage image,double x,double y,double scale,boolean right,float alpha) { }
    private record Hud(int x,int y,int hp,String name) {
        Rectangle bounds(){return new Rectangle(x-48,y-2,96,28);}
    }
    private volatile List<Hud> latestHud=List.of();
    private List<Hud> displayedHud=List.of();
    private final Map<String,BufferedImage> images=new HashMap<>();
    private final AtomicBoolean queued=new AtomicBoolean();
    private volatile List<Draw> latest=List.of();
    private volatile boolean closed;
    private JWindow window;
    private JPanel panel;
    private List<Draw> displayed=List.of();
    private final Rectangle arena;
    public CombatEffects(CombatProfile a,CombatProfile b,Rectangle arena) throws Exception {
        this.arena=arena;
        for(var p:List.of(a,b)) for(var effect:p.effects().values()) {
            for(String frame:effect.frames()) load(p,frame);
            if(effect.cloneImage()!=null) load(p,effect.cloneImage());
        }
        for(var p:List.of(a,b))if(p.animations().containsKey("shadow-clone-active"))for(var frame:p.animations().get("shadow-clone-active").frames())load(p,frame.image());
    }
    private void load(CombatProfile p,String name) throws Exception {
        String key=p.root().resolve(name).toString();
        if(!images.containsKey(key)) {
            BufferedImage im=ImageIO.read(p.root().resolve(name).toFile());
            if(im==null) throw new IllegalArgumentException("Invalid PNG: "+name);
            images.put(key,im);
        }
    }
    public void update(CombatMatch match) {
        if(closed) return;
        List<Draw> draws=new ArrayList<>();
        int limit=com.group_finity.mascot.performance.PerformanceProfile.current().effectLimit();
        for(var v:match.visuals()) {
            if(draws.size()>=limit) break;
            var effect=v.profile.effects().get(v.effect);
            String name=effect.frames().get((v.age/effect.frameTicks())%effect.frames().size());
            draws.add(new Draw(images.get(v.profile.root().resolve(name).toString()),v.x,v.y,v.scale,v.right,1));
            String cloneImage=v.profile.animations().containsKey("shadow-clone-active")?v.profile.animations().get("shadow-clone-active").poseAt(v.age):effect.cloneImage();
            if(v.clone && cloneImage!=null) {
                var clone=images.get(v.profile.root().resolve(cloneImage).toString());
                double launchY=v.profile.attacks().stream().filter(a->a.cloneEffect()).findFirst().map(a->(double)a.polish().projectileY()).orElse(-60.0);
                double centerY=v.y+(-launchY-v.profile.anchorY()+clone.getHeight()/2.0)*v.scale;
                draws.add(new Draw(clone,v.x+(v.right?-28:28)*v.scale,centerY,v.scale,v.right,0.45f));
            }
        }
        latest=List.copyOf(draws);
        latestHud=!match.isFinished()&&com.group_finity.mascot.settings.RuntimeSettings.flag("ShowCombatHP",true)?List.of(hud(match.a),hud(match.b)):List.of();
        if(!queued.compareAndSet(false,true)) return;
        SwingUtilities.invokeLater(() -> {
            queued.set(false);if(closed) return;
            displayed=latest;displayedHud=latestHud;render();
        });
    }
    private void render() {
        if(displayed.isEmpty()&&displayedHud.isEmpty()) {if(window!=null) window.setVisible(false);return;}
        if(GraphicsEnvironment.isHeadless()) return;
        if(window==null) {
            window=new JWindow();window.setName("ShinobiMateCombatEffects");
            window.setFocusableWindowState(false);window.setAutoRequestFocus(false);window.setAlwaysOnTop(com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true));
            window.setBackground(new Color(0,0,0,0));
            panel=new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    for(Draw d:displayed) {
                        int width=(int)Math.round(d.image.getWidth()*d.scale),height=(int)Math.round(d.image.getHeight()*d.scale);
                        int x=(int)Math.round(d.x)-window.getX()-width/2,y=(int)Math.round(d.y)-window.getY()-height/2;
                        g2.setComposite(AlphaComposite.SrcOver.derive(d.alpha));
                        // Effects and pack art are authored facing left, matching Shimeji convention.
                        g2.drawImage(d.image,d.right?x+width:x,y,d.right?-width:width,height,null);
                    }
                    g2.setComposite(AlphaComposite.SrcOver);
                    for(Hud hud:displayedHud) {
                        int x=hud.x-window.getX()-46,y=hud.y-window.getY();
                        g2.setColor(new Color(24,27,38,210));g2.fillRoundRect(x-2,y-2,96,28,8,8);
                        g2.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,10));g2.setColor(new Color(235,238,249));
                        g2.drawString(hud.name,x+2,y+10);
                        g2.setColor(new Color(57,64,83));g2.fillRoundRect(x,y+16,92,6,4,4);
                        g2.setColor(hud.hp<30?new Color(242,113,105):new Color(85,208,158));
                        g2.fillRoundRect(x,y+16,92*Math.max(0,hud.hp)/100,6,4,4);
                    }
                    g2.dispose();
                }
            };
            panel.setOpaque(false);window.setContentPane(panel);window.setSize(1,1);window.addNotify();
            if(com.sun.jna.Platform.isWindows()) {
                var handle=Native.getComponentPointer(window);
                int style=User32.INSTANCE.GetWindowLongW(handle,-20);
                User32.INSTANCE.SetWindowLongW(handle,-20,style|0x20|0x08000000);
            }
        }
        boolean onTop=com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true);
        if(window.isAlwaysOnTop()!=onTop)window.setAlwaysOnTop(onTop);
        Rectangle bounds=null;
        for(Draw d:displayed) {
            int w=(int)Math.ceil(d.image.getWidth()*d.scale),h=(int)Math.ceil(d.image.getHeight()*d.scale);
            Rectangle r=new Rectangle((int)d.x-w/2,(int)d.y-h/2,w,h);
            bounds=bounds==null?r:bounds.union(r);
        }
        for(Hud h:displayedHud) bounds=bounds==null?h.bounds():bounds.union(h.bounds());
        bounds=bounds.intersection(arena);
        if(bounds.isEmpty()) {window.setVisible(false);return;}
        window.setBounds(bounds);panel.repaint();if(!window.isVisible()) window.setVisible(true);
    }
    private Hud hud(CombatMatch.Fighter f) {
        String name=f.profile.root().getFileName().toString().replace("Shippuden","").replace("BlackClover","").replace("Satoru","").replace("Ryomen","");
        int x=(int)Math.max(arena.x+48,Math.min(arena.getMaxX()-48,f.x));
        int visibleHeight=f.profile.hurtbox().height+41;
        int y=(int)Math.max(arena.y+2,f.renderY()-visibleHeight*f.body.scale()-30);
        return new Hud(x,y,f.hp,name);
    }
    public void close() {
        closed=true;latest=List.of();latestHud=List.of();
        SwingUtilities.invokeLater(() -> {
            displayed=List.of();displayedHud=List.of();if(window!=null) {window.dispose();window=null;panel=null;}
            images.clear();
        });
    }
}
