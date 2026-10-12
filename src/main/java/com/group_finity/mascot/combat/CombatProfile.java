package com.group_finity.mascot.combat;

import java.awt.Rectangle;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/** Immutable, pack-owned configuration. Units are pixels at scale 1 and 40ms ticks. */
public record CombatProfile(Path root, int width, int height, int anchorX, int anchorY,
        Rectangle hurtbox, double speed, Personality personality, Map<String, Animation> animations,
        List<Attack> attacks, Map<String, Effect> effects) {
    public record Personality(double aggression, double technique, double dodge, double block,
            double counter, double taunt, double dodgeSpeed, int jumpHeight, int defenseCooldown,
            int defenseCost, double blockReduction, double projectileParry, String blockEffect, String dodgeEffect) { }
    public record Frame(String image, int ticks,String soundEvent) {
        public Frame(String image,int ticks){this(image,ticks,"");}
    }
    public record Animation(List<Frame> frames) {
        public String eventAt(int age) {
            int t=Math.floorMod(age,frames.stream().mapToInt(Frame::ticks).sum());
            for(Frame f:frames){if(t==0)return f.soundEvent();if(t<f.ticks())return "";t-=f.ticks();}return "";
        }
        public String poseAt(int age) {
            int t=Math.max(0,age);
            for(Frame f:frames) {if(t<f.ticks())return f.image();t-=f.ticks();}
            return frames.getLast().image();
        }
        public String imageAt(int age) {
            int total = frames.stream().mapToInt(Frame::ticks).sum();
            int t = Math.floorMod(age, total);
            for (Frame f : frames) { if (t < f.ticks()) return f.image(); t -= f.ticks(); }
            return frames.getLast().image();
        }
    }
    public record Effect(List<String> frames, int frameTicks, String cloneImage) { }
    /** Each strike is a distinct activation; intervening windup frames cannot deal damage. */
    public record Strike(int at,int duration,int damage,int knockback,String animation,String windup,String attackSound,String impactSound) {
        public Strike(int at,int duration,int damage,int knockback,String animation,String windup) {this(at,duration,damage,knockback,animation,windup,"","");}
    }
    public record Polish(List<Strike> strikes,String weapon,String startupSound,String attackSound,String impactSound,
            int effectX,int effectY,int chargeX,int chargeY,int projectileX,int projectileY,String impactEffect,
            String loopSound,String readySound,String afterSound) {
        public Polish(List<Strike> strikes,String weapon,String startupSound,String attackSound,String impactSound,
                int effectX,int effectY,int chargeX,int chargeY,int projectileX,int projectileY,String impactEffect) {
            this(strikes,weapon,startupSound,attackSound,impactSound,effectX,effectY,chargeX,chargeY,projectileX,projectileY,impactEffect,"","","");
        }
        public Polish(List<Strike> strikes,String weapon,String startupSound,String attackSound,String impactSound) {
            this(strikes,weapon,startupSound,attackSound,impactSound,36,-60,24,-60,32,-60,"impact");
        }
        static final Polish DEFAULT=new Polish(List.of(),"none","charge","whoosh","impact");
    }
    public record Attack(String id, String kind, double minRange, double range, Rectangle hitbox,
            int damage, int cooldown, int startup, int active, int recovery, double speed,
            double projectileSpeed, int knockback, int jumpHeight, int cost, double weight,
            String animation, String chargeAnimation, String effect, boolean cloneEffect,
            String recoveryAnimation, String chargeEffect, Polish polish) {
        public Attack(String id,String kind,double minRange,double range,Rectangle hitbox,
                int damage,int cooldown,int startup,int active,int recovery,double speed,
                double projectileSpeed,int knockback,int jumpHeight,int cost,double weight,
                String animation,String chargeAnimation,String effect,boolean cloneEffect,
                String recoveryAnimation,String chargeEffect) {
            this(id,kind,minRange,range,hitbox,damage,cooldown,startup,active,recovery,speed,
                projectileSpeed,knockback,jumpHeight,cost,weight,animation,chargeAnimation,effect,
                cloneEffect,recoveryAnimation,chargeEffect,Polish.DEFAULT);
        }
        public Attack(String id, String kind, double minRange, double range, Rectangle hitbox,
                int damage, int cooldown, int startup, int active, int recovery, double speed,
                double projectileSpeed, int knockback, int jumpHeight, int cost, double weight,
                String animation, String chargeAnimation, String effect, boolean cloneEffect) {
            this(id,kind,minRange,range,hitbox,damage,cooldown,startup,active,recovery,speed,projectileSpeed,
                knockback,jumpHeight,cost,weight,animation,chargeAnimation,effect,cloneEffect,"recover",effect);
        }
        public boolean special() { return kind.equals("special"); }
        public boolean projectile() { return projectileSpeed > 0; }
    }

    private static int optional(Element e,String name,int fallback){return e.hasAttribute(name)?i(e,name,-150,150):fallback;}
    public static CombatProfile load(Path root) throws Exception {
        root = root.toAbsolutePath().normalize();
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Element e = factory.newDocumentBuilder().parse(root.resolve("conf/combat.xml").toFile()).getDocumentElement();
        if (!e.getTagName().equals("combat") || !e.getAttribute("version").equals("1"))
            throw new IllegalArgumentException("Expected <combat version=\"1\">");
        Element p = child(e, "personality");
        var personality = new Personality(n(p,"aggression",0,1), n(p,"technique",0,1),
            n(p,"dodge",0,1), n(p,"block",0,1), n(p,"counter",0,1), n(p,"taunt",0,1),
            n(p,"dodgeSpeed",1,40), i(p,"jumpHeight",0,80), i(p,"defenseCooldown",1,500),
            i(p,"defenseCost",1,100), n(p,"blockReduction",0,0.95),
            p.hasAttribute("projectileParry")?n(p,"projectileParry",0,0.8):0,
            p.hasAttribute("blockEffect")?p.getAttribute("blockEffect"):"spark",
            p.hasAttribute("dodgeEffect")?p.getAttribute("dodgeEffect"):"dust");
        if (personality.dodge() + personality.block() > 0.9)
            throw new IllegalArgumentException("Combined defense probability must be <= 0.9");
        Map<String, Animation> animations = new LinkedHashMap<>();
        for (Element a : children(child(e,"animations"), "animation")) {
            List<Frame> frames = new ArrayList<>();
            for (Element f : children(a,"frame")) {
                String image = asset(root, f.getAttribute("image"));
                frames.add(new Frame(image, i(f,"ticks",1,500),attribute(f,"soundEvent","")));
            }
            if (frames.isEmpty() || animations.put(a.getAttribute("id"), new Animation(List.copyOf(frames))) != null)
                throw new IllegalArgumentException("Empty/duplicate animation");
        }
        for (String required : List.of("idle","approach","dodge","block","hit","knockback","recover","ko","victory"))
            if (!animations.containsKey(required)) throw new IllegalArgumentException("Missing animation: " + required);
        Map<String, Effect> effects = new LinkedHashMap<>();
        for (Element f : children(child(e,"effects"),"effect")) {
            List<String> frames = new ArrayList<>();
            for (Element frame : children(f,"frame")) frames.add(asset(root,frame.getAttribute("image")));
            if (frames.isEmpty()) throw new IllegalArgumentException("Empty effect");
            String clone = f.hasAttribute("cloneImage") ? asset(root,f.getAttribute("cloneImage")) : null;
            effects.put(f.getAttribute("id"),new Effect(List.copyOf(frames),i(f,"frameTicks",1,50),clone));
        }
        for (String required : List.of("impact","dust","spark"))
            if (!effects.containsKey(required)) throw new IllegalArgumentException("Missing effect: " + required);
        if(!effects.containsKey(personality.blockEffect())||!effects.containsKey(personality.dodgeEffect()))
            throw new IllegalArgumentException("Unknown defense effect");
        List<Attack> attacks = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Element a : children(child(e,"attacks"),"attack")) {
            String kind = a.getAttribute("kind"), anim = a.getAttribute("animation"), charge = a.getAttribute("chargeAnimation");
            String recovery=a.hasAttribute("recoveryAnimation")?a.getAttribute("recoveryAnimation"):"recover";
            String chargeEffect=a.hasAttribute("chargeEffect")?a.getAttribute("chargeEffect"):a.getAttribute("effect");
            if (!Set.of("melee","ranged","dash","special","aerial").contains(kind)
                    || !animations.containsKey(anim) || !animations.containsKey(charge) || !animations.containsKey(recovery)
                    || !effects.containsKey(chargeEffect)
                    || !effects.containsKey(a.getAttribute("effect")) || !ids.add(a.getAttribute("id")))
                throw new IllegalArgumentException("Invalid attack references/kind/id: " + a.getAttribute("id"));
            double min = n(a,"minRange",0,1000), range = n(a,"range",1,1000);
            if (min > range) throw new IllegalArgumentException("minRange > range");
            List<Strike> strikes=new ArrayList<>();int end=0;
            for(Element s:children(a,"strike")) {
                int at=i(s,"at",0,99),duration=i(s,"duration",1,20);
                String contact=s.getAttribute("animation"),windup=s.getAttribute("windup");
                if(at<end || at+duration>i(a,"active",1,100) || !animations.containsKey(contact)||!animations.containsKey(windup))
                    throw new IllegalArgumentException("Invalid combo window: "+a.getAttribute("id"));
                strikes.add(new Strike(at,duration,i(s,"damage",1,50),i(s,"knockback",-100,100),contact,windup,
                    attribute(s,"attackSound",""),attribute(s,"impactSound","")));end=at+duration;
            }
            if(strikes.size()>4)throw new IllegalArgumentException("At most four strikes per combo");
            String weapon=attribute(a,"weapon","none");
            if(!Set.of("none","kunai","shuriken","sword","energy","slash").contains(weapon))throw new IllegalArgumentException("Unknown weapon");
            Polish polish=new Polish(List.copyOf(strikes),weapon,attribute(a,"startupSound","charge"),
                attribute(a,"attackSound",weapon.equals("sword")?"sword":"whoosh"),attribute(a,"impactSound","impact"),
                optional(a,"effectX",36),optional(a,"effectY",-60),optional(a,"chargeX",24),optional(a,"chargeY",-60),
                optional(a,"projectileX",32),optional(a,"projectileY",-60),attribute(a,"impactEffect","impact"),
                attribute(a,"loopSound",""),attribute(a,"readySound",""),attribute(a,"afterSound",""));
            if(!effects.containsKey(polish.impactEffect()))throw new IllegalArgumentException("Unknown impact effect");
            // Sounds are optional pack event IDs; an unknown/missing event cannot invalidate combat.
            attacks.add(new Attack(a.getAttribute("id"),kind,min,range,box(child(a,"hitbox")),
                i(a,"damage",1,50),i(a,"cooldown",1,1000),i(a,"startup",1,100),i(a,"active",1,100),
                i(a,"recovery",1,100),n(a,"speed",0,40),n(a,"projectileSpeed",0,40),i(a,"knockback",-100,100),
                i(a,"jumpHeight",0,80),i(a,"cost",0,100),n(a,"weight",0.01,10),anim,charge,
                a.getAttribute("effect"),Boolean.parseBoolean(a.getAttribute("clone")),recovery,chargeEffect,polish));
        }
        if (attacks.isEmpty() || attacks.size() > 32) throw new IllegalArgumentException("Expected 1..32 attacks");
        return new CombatProfile(root,i(e,"width",16,512),i(e,"height",16,512),i(e,"anchorX",0,512),
            i(e,"anchorY",0,512),box(child(e,"hurtbox")),n(e,"movementSpeed",1,20),personality,
            Map.copyOf(animations),List.copyOf(attacks),Map.copyOf(effects));
    }
    private static String attribute(Element e,String key,String fallback) {return e.hasAttribute(key)?e.getAttribute(key):fallback;}
    private static String asset(Path root, String name) {
        Path path = root.resolve(name).normalize();
        if (name.isBlank() || !path.startsWith(root) || !Files.isRegularFile(path))
            throw new IllegalArgumentException("Missing/unsafe combat asset: " + name);
        return name;
    }
    private static Rectangle box(Element e) {
        return new Rectangle(i(e,"x",-1000,1000),i(e,"y",-1000,1000),i(e,"width",1,1000),i(e,"height",1,1000));
    }
    private static double n(Element e,String key,double min,double max) {
        double value = Double.parseDouble(e.getAttribute(key));
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Invalid " + key);
        return value;
    }
    private static int i(Element e,String key,int min,int max) {
        double value = n(e,key,min,max);
        if (value != Math.rint(value)) throw new IllegalArgumentException("Expected integer: " + key);
        return (int)value;
    }
    private static Element child(Element e,String name) {
        return children(e,name).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Missing " + name));
    }
    private static List<Element> children(Element e,String name) {
        List<Element> result = new ArrayList<>();
        for (var node=e.getFirstChild();node!=null;node=node.getNextSibling())
            if (node instanceof Element item && item.getTagName().equals(name)) result.add(item);
        return result;
    }
}
