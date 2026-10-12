package com.group_finity.mascot.combat;

import java.awt.Rectangle;
import java.util.*;
import java.util.random.RandomGenerator;
import static com.group_finity.mascot.combat.CombatState.*;

/** One explicit 1v1 pair. No registry lookup, window manipulation, timers or automatic starts. */
public final class CombatMatch {
    public interface Body {
        double x(); double y(); double scale(); Rectangle workArea();
        void pose(double x, double y, boolean right, String image);
        boolean available(); boolean suspended();
        default void sound(String event,String character,double pan) { }
        default void chargeSound(String event,String character,double pan){sound(event,character,pan);}
        default void stopChargeSounds(){ }
        default void stopSounds(){ }
    }
    public static final class Fighter {
        public final Body body;
        public final CombatProfile profile;
        public CombatState state = IDLE;
        public int hp = 100;
        public double energy = 100;
        public double x, y;
        public boolean right;
        public CombatProfile.Attack attack;
        private int age, decisionAt, defenseAt, attackAge, knockTicks;
        private double knockSpeed;
        private boolean hitUsed, counterReady;
        private final Set<Integer> strikesUsed=new HashSet<>();
        private final Deque<String> recentAttackHistory=new ArrayDeque<>();
        public List<String> recentAttacks() {return List.copyOf(recentAttackHistory);}
        public int attackAge(){return attackAge;}
        private final Map<String,Integer> cooldowns = new HashMap<>();
        private final Rectangle arena;
        private String lastPose;
        private String lastAudioAnimation="";
        private int lastAudioAge=-1;
        private boolean airborne;
        Fighter(Body body, CombatProfile profile,Rectangle arena) { this.body=body; this.profile=profile; this.arena=arena; x=body.x(); y=body.y(); }
        public Rectangle hurtbox() { return box(profile.hurtbox(),x,renderY(),right,body.scale()); }
        public double renderY() {
            int jump = state == DODGE ? profile.personality().jumpHeight() :
                attack != null && attackAge >= attack.startup() ? attack.jumpHeight() : 0;
            int duration = state == DODGE ? 10 : attack == null ? 1 : attack.active();
            int phase = state == DODGE ? age : attack == null ? 0 : attackAge - attack.startup();
            return Math.max(arena.y+profile.anchorY()*body.scale(),
                y - Math.max(0, Math.sin(Math.PI * Math.min(duration,Math.max(0,phase)) / duration)) * jump * body.scale());
        }
    }
    public static final class Visual {
        public final CombatProfile profile;
        public final String effect;
        public double x,y;
        public final double scale;
        public final boolean right, clone;
        private Fighter owner;
        private CombatProfile.Attack boundAttack;
        private double offsetX,offsetY;
        public int age;
        public final int lifetime;
        Visual(Fighter f,String effect,double x,double y,int lifetime,boolean clone) {
            profile=f.profile;this.effect=effect;this.x=x;this.y=y;scale=f.body.scale();right=f.right;
            this.lifetime=lifetime;this.clone=clone;
        }
    }
    private static final class Projectile {
        final Fighter owner, target;
        final CombatProfile.Attack attack;
        final Visual visual;
        final WeaponSystem.ProjectileSpec spec;
        final int damage,knockback;
        double x, y, travelled;
        final int direction;
        Projectile(Fighter f,Fighter target,CombatProfile.Attack a,Visual v,int damage,int knockback) {
            owner=f;this.target=target;attack=a;visual=v;x=v.x;y=v.y;direction=f.right?1:-1;
            spec=WeaponSystem.ProjectileSpec.of(a,f.x,f.renderY(),f.right,f.body.scale());
            this.damage=damage;this.knockback=knockback;
        }
    }
    public final Fighter a,b;
    private final RandomGenerator random;
    private final Rectangle arena;
    private final List<Projectile> projectiles=new ArrayList<>();
    private final List<Visual> visuals=new ArrayList<>();
    private int tick, finishAge, impactPause;
    private Fighter loser;
    private boolean stopped, finished;
    private boolean audioSuspended;

    public CombatMatch(Body a,CombatProfile pa,Body b,CombatProfile pb,RandomGenerator random) {
        if (a==b) throw new IllegalArgumentException("Two distinct instances required");
        arena=new Rectangle(a.workArea());
        if(!arena.equals(b.workArea()))throw new IllegalArgumentException("Fighters must share one monitor work area");
        this.a=new Fighter(a,pa,arena);this.b=new Fighter(b,pb,arena);this.random=random;
        if (arena.width < Math.max(pa.width()*a.scale(),pb.width()*b.scale()) * 2 ||
                arena.height < Math.max(pa.height()*a.scale(),pb.height()*b.scale()))
            throw new IllegalArgumentException("Work area too small for two fighters");
        clamp(this.a);clamp(this.b);face();
    }
    public boolean isStopped() { return stopped; }
    public boolean isFinished() { return finished; }
    public Body winner(){return loser==null?null:loser==a?b.body:a.body;}
    public List<Visual> visuals() { return Collections.unmodifiableList(visuals); }
    public int projectileCount() { return projectiles.size(); }
    public Rectangle arena() { return new Rectangle(arena); }
    public void stop() {
        stopped=true;projectiles.clear();visuals.clear();
        for(Fighter f:List.of(a,b)) {f.body.stopSounds();f.hp=100;f.energy=100;f.attack=null;f.state=IDLE; }
    }

    public void tick() {
        if(stopped) return;
        if(!a.body.available() || !b.body.available()) { stop();return; }
        if(!a.body.workArea().equals(arena)||!b.body.workArea().equals(arena)){stop();return;}
        // Opening either context menu or pausing either fighter suspends the whole pair.
        if(a.body.suspended() || b.body.suspended()) {
            if(!audioSuspended){a.body.stopChargeSounds();b.body.stopChargeSounds();audioSuspended=true;}return;
        }
        if(audioSuspended){audioSuspended=false;for(Fighter f:List.of(a,b))if(f.attack!=null&&f.attackAge<f.attack.startup())chargeSound(f,f.attack.polish().loopSound());}
        if(impactPause>0){impactPause--;render(a);render(b);return;}
        tick++;
        visuals.removeIf(v -> {boolean expired=++v.age >= v.lifetime || v.boundAttack!=null&&v.owner.attack!=v.boundAttack;
            if(expired&&v.clone)sound(v.owner,"vfx.clone.out");return expired;});
        for(Visual v:visuals)if(v.boundAttack!=null){v.x=v.owner.x+v.offsetX;v.y=v.owner.renderY()+v.offsetY;}
        if(finished) {
            finishAge++;
            a.age++;b.age++;
            if(finishAge<14){move(loser,loser.knockSpeed,0);loser.knockSpeed*=.78;}
            render(a);render(b);
            if(finishAge>=90) stop();
            return;
        }
        face();
        for(Fighter f:List.of(a,b)) {
            f.age++;f.energy=Math.min(100,f.energy+0.45);
            // Walk down to the desktop floor without moving any external window.
            move(f,0,Math.min(8*f.body.scale(),arena.getMaxY()-f.y));
        }
        if(tick<=25 || Math.abs(a.y+(a.profile.height()-a.profile.anchorY())*a.body.scale()
                - b.y-(b.profile.height()-b.profile.anchorY())*b.body.scale())>18*Math.max(a.body.scale(),b.body.scale())) {
            state(a,SEEK_TARGET);state(b,SEEK_TARGET);separate();render(a);render(b);return;
        }
        decide(a,b);decide(b,a);
        incoming(a,b);incoming(b,a);
        update(a,b);
        if(!finished) update(b,a);
        if(!finished) updateProjectiles();
        separate();face();render(a);render(b);
    }
    private void decide(Fighter f,Fighter target) {
        if(f.attack!=null || !(f.state==IDLE || f.state==APPROACH || f.state==RECOVER || f.state==SEEK_TARGET)) return;
        if(tick<f.decisionAt) return;
        double distance=Math.abs(f.x-target.x)/f.body.scale();
        List<CombatProfile.Attack> candidates=f.profile.attacks().stream().filter(at ->
            tick>=f.cooldowns.getOrDefault(at.id(),0) && f.energy>=at.cost() &&
            (!at.id().contains("counter")||f.counterReady) &&
            distance>=at.minRange() && distance<=at.range()).toList();
        if(candidates.size()>1 && f.recentAttackHistory.size()>=2) {
            var history=f.recentAttacks();String last=history.getLast();
            if(last.equals(history.get(history.size()-2)))candidates=candidates.stream().filter(at->!at.id().equals(last)).toList();
        }
        var p=f.profile.personality();
        if(!candidates.isEmpty() && (f.counterReady || random.nextDouble()<0.55+0.4*p.aggression())) {
            double total=0;
            for(var at:candidates) total+=weight(f,at,distance);
            double choice=random.nextDouble()*total;
            var selected=candidates.getLast();
            for(var at:candidates) { choice-=weight(f,at,distance);if(choice<=0) {selected=at;break;} }
            f.attack=selected;f.attackAge=0;f.hitUsed=false;f.counterReady=false;
            f.strikesUsed.clear();f.recentAttackHistory.addLast(selected.id());
            if(f.recentAttackHistory.size()>4)f.recentAttackHistory.removeFirst();
            f.cooldowns.put(selected.id(),tick+selected.startup()+selected.active()+selected.recovery()+selected.cooldown());f.energy-=selected.cost();
            state(f,selected.special()?SPECIAL_ATTACK:ATTACK);
            chargeSound(f,selected.polish().startupSound());
            chargeSound(f,selected.polish().loopSound());
            if(selected.special()) attached(f,selected.chargeEffect(),selected.polish().chargeX(),selected.polish().chargeY(),selected.startup());
        } else if(random.nextDouble()<p.taunt() && distance<220) {
            state(f,f.profile.animations().containsKey("taunt")?TAUNT:RECOVER);f.decisionAt=tick+14;
        } else {
            state(f,APPROACH);
            f.decisionAt=tick+2;
        }
    }
    private double weight(Fighter f,CombatProfile.Attack at,double distance) {
        var p=f.profile.personality();
        long repeats=f.recentAttackHistory.stream().filter(at.id()::equals).count();
        double history=Math.pow(.35,repeats)*(at.id().equals(f.recentAttackHistory.peekLast())?.25:1);
        double opportunity=targetRecovering(f)?1.2:1;
        return at.weight() * history * opportunity * (at.special()||at.projectile()?0.3+2*p.technique():0.4+2*p.aggression())
            * (distance<85 && at.kind().equals("melee")?2:1) * (f.counterReady&&at.kind().equals("melee")?(at.id().contains("counter")?12:4):1)
            * (f.hp<30&&at.startup()>12?.7:1);
    }
    private boolean targetRecovering(Fighter f) {Fighter t=f==a?b:a;return t.state==RECOVER||t.state==KNOCKBACK;}
    private void incoming(Fighter source,Fighter target) {
        if(source.attack==null || source.attackAge!=Math.max(1,source.attack.startup()-3) || tick<target.defenseAt || target.hp<=0 ||
            !(target.state==IDLE || target.state==APPROACH || target.state==RECOVER || target.state==SEEK_TARGET || target.state==TAUNT)) return;
        if(Math.abs(source.x-target.x)>source.attack.range()*source.body.scale()+30) return;
        defend(target,source);
    }
    private boolean defend(Fighter f,Fighter source) {
        var p=f.profile.personality();
        if(tick<f.defenseAt || f.energy<p.defenseCost()) return false;
        double r=random.nextDouble(), low=f.hp<30?1.2:1;
        double threat=source.attack!=null&&(source.attack.special()||source.attack.damage()>=20)?1.3:.7;
        double dodge=Math.min(.65,p.dodge()*low*threat);
        CombatState defense=r<dodge?DODGE:r<Math.min(.85,dodge+p.block()*low)?BLOCK:null;
        if(defense==null) return false;
        f.energy-=p.defenseCost();f.defenseAt=tick+p.defenseCooldown();f.attack=null;
        state(f,defense);
        f.counterReady=false;
        f.body.stopChargeSounds();
        if(defense==DODGE)sound(f,"dodge");
        if(defense==DODGE) effect(f,p.dodgeEffect(),f.x,f.y-10*f.body.scale(),10,false);
        else effect(f,p.blockEffect(),f.x+(f.right?25:-25)*f.body.scale(),f.y-65*f.body.scale(),12,false);
        return true;
    }
    private void update(Fighter f,Fighter target) {
        switch(f.state) {
            case DODGE -> {
                if(f.age<7) move(f,(f.x<target.x?-1:1)*f.profile.personality().dodgeSpeed()*f.body.scale(),0);
                if(f.age>=10) recover(f,f.counterReady?1:5);
            }
            case BLOCK -> { if(f.age>=14) recover(f,f.counterReady?1:5); }
            case TAUNT -> { if(f.age>=14)recover(f,3); }
            case HIT -> { if(f.age>=5) {state(f,KNOCKBACK);if(Math.abs(f.knockSpeed)>0.1)sound(f,"combat.knockback");} }
            case KNOCKBACK -> {
                move(f,f.knockSpeed,0);f.knockSpeed*=0.78;
                if(++f.knockTicks>=7) {if(Math.abs(f.knockSpeed)>.1)sound(f,"movement.landingHeavy");recover(f,6);}
            }
            case APPROACH -> {
                double dist=Math.abs(f.x-target.x), min=personalDistance();
                if(dist>min) move(f,(f.x<target.x?1:-1)*Math.min(dist-min,f.profile.speed()*f.body.scale()),0);
            }
            case ATTACK, SPECIAL_ATTACK -> updateAttack(f,target);
            default -> { }
        }
    }
    private void updateAttack(Fighter f,Fighter target) {
        var at=f.attack;if(at==null) {recover(f,3);return;}
        int age=f.attackAge++;
        if(age==Math.max(1,at.startup()/2))chargeSound(f,at.polish().readySound());
        if(age==at.startup()+at.active()){f.body.stopChargeSounds();sound(f,at.polish().afterSound());}
        if(age>=at.startup() && age<at.startup()+at.active()) {
            if(at.speed()>0) {
                // A locked facing must not carry a dash through its opponent.
                double forward=(target.x-f.x)*(f.right?1:-1);
                double step=Math.min(at.speed()*f.body.scale(),Math.max(0,forward-personalDistance()));
                move(f,(f.right?1:-1)*step,0);
            }
            separate();
            if(age==at.startup()) {
                f.body.stopChargeSounds();
                if(at.jumpHeight()>0)sound(f,"movement.jump");
                if(at.polish().strikes().isEmpty())sound(f,at.polish().attackSound());
                if(at.projectile()&&at.polish().strikes().isEmpty()) {
                    f.hitUsed=true;
                    launch(f,target,at,at.damage(),at.knockback());
                } else if(at.polish().strikes().isEmpty())attached(f,at.effect(),at.polish().effectX(),at.polish().effectY(),at.active());
            }
            if(!at.polish().strikes().isEmpty()) {
                int phase=age-at.startup();
                for(int i=0;i<at.polish().strikes().size();i++) {
                    var strike=at.polish().strikes().get(i);
                    if(at.projectile()) {
                        if(phase==strike.at()&&f.strikesUsed.add(i)){sound(f,strike.attackSound().isBlank()?at.polish().attackSound():strike.attackSound());launch(f,target,at,strike.damage(),strike.knockback());}
                        continue;
                    }
                    if(phase==strike.at()) {sound(f,strike.attackSound().isBlank()?at.polish().attackSound():strike.attackSound());attached(f,at.effect(),at.polish().effectX(),at.polish().effectY(),strike.duration()+2);}
                    if(phase>=strike.at()&&phase<strike.at()+strike.duration()&&!f.strikesUsed.contains(i)&&
                            box(at.hitbox(),f.x,f.renderY(),f.right,f.body.scale()).intersects(target.hurtbox())) {
                        f.strikesUsed.add(i);damage(f,target,at,strike.damage(),strike.knockback(),strike.impactSound());
                        if(finished)return;
                    }
                }
            } else if(!f.hitUsed && box(at.hitbox(),f.x,f.renderY(),f.right,f.body.scale()).intersects(target.hurtbox())) {
                f.hitUsed=true;damage(f,target,at,at.damage(),at.knockback());
                if(!at.projectile()&&at.jumpHeight()==0)f.attackAge=at.startup()+at.active();
                if(finished)return;
            }
        }
        if(age>=at.startup()+at.active()+at.recovery()) {f.body.stopChargeSounds();f.attack=null;recover(f,3); }
    }
    private void launch(Fighter f,Fighter target,CombatProfile.Attack at,int damage,int knockback) {
        if(projectiles.size()>=8)return;
        var spec=WeaponSystem.ProjectileSpec.of(at,f.x,f.renderY(),f.right,f.body.scale());
        Visual v=effect(f,at.effect(),spec.spawnX(),spec.spawnY(),spec.lifetime(),at.cloneEffect());
        projectiles.add(new Projectile(f,target,at,v,damage,knockback));
    }
    private void updateProjectiles() {
        for(Iterator<Projectile> it=projectiles.iterator();it.hasNext();) {
            Projectile p=it.next();double step=p.spec.speed();
            if(p.visual.age>=p.visual.lifetime) {it.remove();continue;}
            double old=p.x;p.x+=step*p.direction;p.travelled+=step;p.visual.x=p.x;
            // Swept AABB prevents fast projectiles tunnelling through a hurtbox.
            Rectangle hit=p.spec.sweep(old,p.x,p.y,p.owner.body.scale());
            if(hit.intersects(p.target.hurtbox())) {
                damage(p.owner,p.target,p.attack,p.damage,p.knockback);
                if(finished) return; // finish clears the iterator's backing list immediately
                p.visual.age=p.visual.lifetime;it.remove();
            } else if(p.travelled>=p.attack.range()*p.owner.body.scale() || !arena.contains(p.x,p.y)) {
                p.visual.age=p.visual.lifetime;it.remove();
            }
        }
    }
    private void damage(Fighter source,Fighter target,CombatProfile.Attack at,int damage,int knockback) {
        damage(source,target,at,damage,knockback,"");
    }
    private void damage(Fighter source,Fighter target,CombatProfile.Attack at,int damage,int knockback,String strikeSound) {
        if(finished || target.hp<=0 || (target.state==DODGE&&target.age>=1&&target.age<=7)) return;
        boolean blocked=target.state==BLOCK;
        double chance=at.projectile()?target.profile.personality().projectileParry():target.profile.personality().counter();
        if(blocked && target.age<=3 && random.nextDouble()<chance) {
            target.counterReady=true;
            effect(target,"spark",target.x,target.y-60*target.body.scale(),10,false);sound(target,"parry");
            if(!at.projectile()){source.body.stopChargeSounds();source.attack=null;recover(source,8);}
            return;
        }
        int amount=blocked?Math.max(1,(int)Math.round(damage*(1-target.profile.personality().blockReduction()))):damage;
        target.hp=Math.max(0,target.hp-amount);
        effect(source,blocked?"spark":at.polish().impactEffect(),target.x,target.renderY()+at.polish().effectY()*target.body.scale(),10,false);
        if(blocked)sound(target,"block");
        else {
            // The attacking pack owns signature/weapon impact identity, at the victim's position.
            source.body.sound(strikeSound.isBlank()?at.polish().impactSound():strikeSound,source.profile.root().getFileName().toString(),pan(target));
        }
        if(!blocked)sound(target,"grunt");
        if(!blocked && (damage>=18||at.special())) {impactPause=2;effect(target,"dust",target.x,target.y-8*target.body.scale(),8,false);}
        if(!blocked && at.special())effect(target,at.effect(),target.x,target.y-60*target.body.scale(),8,false);
        if(!blocked && at.special()&&target.profile.effects().containsKey("shockwave"))effect(target,"shockwave",target.x,target.y-60*target.body.scale(),8,false);
        if(!blocked && target.profile.effects().containsKey("hitflash"))
            effect(target,"hitflash",target.x,target.y-60*target.body.scale(),4,false);
        if(blocked && target.hp>0) return;
        target.body.stopChargeSounds();target.attack=null;target.knockTicks=0;
        target.knockSpeed=(source.x<target.x?1:-1)*knockback*target.body.scale()*.22/(1-Math.pow(.78,7));
        state(target,HIT);
        if(target.hp==0) {
            finished=true;loser=target;finishAge=0;
            source.body.stopChargeSounds();target.body.stopChargeSounds();
            projectiles.clear();visuals.clear();source.attack=null;state(source,VICTORY);
            state(target,KO);sound(target,"ko");sound(source,"victory");
        }
    }
    private void recover(Fighter f,int delay) { state(f,RECOVER);f.decisionAt=tick+delay; }
    private void sound(Fighter f,String event) {
        if(event!=null&&!event.isBlank())f.body.sound(event,f.profile.root().getFileName().toString(),pan(f));
    }
    private double pan(Fighter f){return Math.max(-.6,Math.min(.6,(f.x-arena.getCenterX())/arena.width));}
    private void chargeSound(Fighter f,String event){if(event!=null&&!event.isBlank())f.body.chargeSound(event,f.profile.root().getFileName().toString(),pan(f));}
    private static void state(Fighter f,CombatState state) { if(f.state!=state) {f.state=state;f.age=0;} }
    private Visual effect(Fighter f,String effect,double x,double y,int lifetime,boolean clone) {
        Visual v=new Visual(f,effect,x,y,lifetime,clone);
        v.owner=f;
        if(clone)sound(f,"vfx.clone.in");
        if(visuals.size()<48) visuals.add(v);
        return v;
    }
    private void attached(Fighter f,String effect,double dx,double dy,int lifetime) {
        Visual v=effect(f,effect,f.x+(f.right?dx:-dx)*f.body.scale(),f.renderY()+dy*f.body.scale(),lifetime,false);
        v.owner=f;v.boundAttack=f.attack;v.offsetX=(f.right?dx:-dx)*f.body.scale();v.offsetY=dy*f.body.scale();
    }
    private void face() {
        if(a.attack==null) a.right=a.x<=b.x;
        if(b.attack==null) b.right=b.x<=a.x;
    }
    private void move(Fighter f,double dx,double dy) {f.x+=dx;f.y+=dy;clamp(f);}
    private void clamp(Fighter f) {
        var p=f.profile;double s=f.body.scale();
        f.x=Math.max(arena.x+p.anchorX()*s,Math.min(arena.getMaxX()-(p.width()-p.anchorX())*s,f.x));
        f.y=Math.max(arena.y+p.anchorY()*s,Math.min(arena.getMaxY()-(p.height()-p.anchorY())*s,f.y));
    }
    private void separate() {
        if(Math.abs(a.y-b.y)>Math.max(a.profile.hurtbox().height*a.body.scale(),b.profile.hurtbox().height*b.body.scale()))return;
        double delta=personalDistance()-Math.abs(a.x-b.x);
        if(delta<=0)return;
        int direction=a.x<=b.x?-1:1;
        double step=Math.min(delta/2,4*Math.max(a.body.scale(),b.body.scale()));
        double oldA=a.x,oldB=b.x;move(a,direction*step,0);move(b,-direction*step,0);
        double missing=step*2-Math.abs(a.x-oldA)-Math.abs(b.x-oldB);
        if(missing>0){if(a.x==oldA)move(b,-direction*missing,0);else if(b.x==oldB)move(a,direction*missing,0);}
    }
    private double personalDistance(){return a.profile.hurtbox().width*a.body.scale()/2+b.profile.hurtbox().width*b.body.scale()/2+8*Math.max(a.body.scale(),b.body.scale());}
    private void render(Fighter f) {
        String animation=switch(f.state) {
            case IDLE, SEEK_TARGET -> "idle";
            case APPROACH -> "approach";
            case ATTACK, SPECIAL_ATTACK -> f.attack==null?"idle":
                f.attackAge<=f.attack.startup()?f.attack.chargeAnimation():
                f.attackAge>f.attack.startup()+f.attack.active()?f.attack.recoveryAnimation():f.attack.animation();
            default -> f.state.name().toLowerCase(Locale.ROOT);
        };
        int age=f.age;
        if(f.attack!=null) {
            int t=Math.max(0,f.attackAge-1);var at=f.attack;
            if(t<at.startup()){animation=at.chargeAnimation();age=t;}
            else if(t>=at.startup()+at.active()){animation=at.recoveryAnimation();age=t-at.startup()-at.active();}
            else {age=t-at.startup();animation=at.animation();
                for(var strike:at.polish().strikes()) {
                    if(age<strike.at()){animation=strike.windup();break;}
                    if(age<strike.at()+strike.duration()){animation=strike.animation();age-=strike.at();break;}
                    animation=at.recoveryAnimation();
                }
            }
        }
        double y=Math.max(arena.y+f.profile.anchorY()*f.body.scale(),f.renderY());
        var sequence=f.profile.animations().get(animation);
        String image=f.attack!=null||f.state==KO||f.state==VICTORY?sequence.poseAt(age):sequence.imageAt(age);
        if(f.state==APPROACH&&(!animation.equals(f.lastAudioAnimation)||age!=f.lastAudioAge))sound(f,sequence.eventAt(age));
        f.lastAudioAnimation=animation;f.lastAudioAge=age;
        boolean airborne=y<f.y-.5;
        if(f.airborne&&!airborne)sound(f,"movement.landing");
        f.airborne=airborne;f.lastPose=image;
        f.body.pose(f.x,y,f.right,image);
    }
    public static Rectangle box(Rectangle local,double x,double y,boolean right,double scale) {
        double left=right?local.x:-local.x-local.width;
        return new Rectangle((int)Math.floor(x+left*scale),(int)Math.floor(y+local.y*scale),
            (int)Math.ceil(local.width*scale),(int)Math.ceil(local.height*scale));
    }
}
