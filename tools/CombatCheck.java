import com.group_finity.mascot.*;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.config.Configuration;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;

/** Deterministic combat model + actual Manager/Mascot/menu integration, no external window moves. */
public class CombatCheck extends PolishEngineCheck {
    static int combatTicks;
    static class Body implements CombatMatch.Body {
        double x,y,scale;final Rectangle area;boolean available=true,suspended;String image;boolean right;
        Body(double x,double y,double scale,Rectangle area) {this.x=x;this.y=y;this.scale=scale;this.area=area;}
        public double x(){return x;}public double y(){return y;}public double scale(){return scale;}
        public Rectangle workArea(){return area;}
        public boolean available(){return available;}public boolean suspended(){return suspended;}
        public void pose(double x,double y,boolean right,String image){this.x=x;this.y=y;this.right=right;this.image=image;}
    }
    static CombatProfile profile(String name)throws Exception {return CombatProfile.load(Path.of("img",name));}
    static void pair(String an,String bn)throws Exception {
        var pa=profile(an);var pb=profile(bn);
        EnumSet<CombatState> observed=EnumSet.noneOf(CombatState.class);
        boolean damage=false,approached=false;
        for(int seed=0;seed<16;seed++)for(double scale:List.of(.75,1.0,1.5)) {
            Rectangle area=new Rectangle(seed%2==0?-1200:80,seed%2==0?-200:60,1000,600);
            Body a=new Body(area.x+130,area.getMaxY()-100,scale,area),b=new Body(area.getMaxX()-130,area.getMaxY(),scale,area);
            CombatMatch match=new CombatMatch(a,pa,b,pb,new Random(seed));
            int t=0;double initial=Math.abs(a.x-b.x);
            while(!match.isStopped()&&t++<4000) {
                match.tick();combatTicks++;
                observed.add(match.a.state);observed.add(match.b.state);
                damage|=match.a.hp<100||match.b.hp<100;approached|=Math.abs(a.x-b.x)<initial-50;
                require(area.contains(new Rectangle((int)Math.round(a.x-pa.anchorX()*scale),(int)Math.round(a.y-pa.anchorY()*scale),
                    (int)Math.round(pa.width()*scale),(int)Math.round(pa.height()*scale))),"A left screen");
                require(area.contains(new Rectangle((int)Math.round(b.x-pb.anchorX()*scale),(int)Math.round(b.y-pb.anchorY()*scale),
                    (int)Math.round(pb.width()*scale),(int)Math.round(pb.height()*scale))),"B left screen");
                if(!match.isFinished()) require(!match.a.hurtbox().intersects(match.b.hurtbox()),"bodies overlap");
                require(match.projectileCount()<10 && match.visuals().size()<=48,"unbounded effects/projectiles");
                if(match.a.attack==null && match.b.attack==null && a.x!=b.x && !match.isFinished())
                    require(a.right==(a.x<b.x)&&b.right==(b.x<a.x),"wrong facing");
            }
            require(match.isFinished()&&match.isStopped(),"match did not reach KO/end: "+an+" vs "+bn+" seed "+seed+" states "+match.a.state+" / "+match.b.state);
            require(match.a.hp==100&&match.b.hp==100,"HP not reset");
            require(match.visuals().isEmpty()&&match.projectileCount()==0,"effects survive end");
        }
        for(var state:List.of(CombatState.APPROACH,CombatState.ATTACK,CombatState.SPECIAL_ATTACK,CombatState.DODGE,
                CombatState.BLOCK,CombatState.HIT,CombatState.KNOCKBACK,CombatState.KO,CombatState.VICTORY))
            require(observed.contains(state),"state not exercised "+state+" / "+an+" vs "+bn);
        require(damage&&approached,"no damage/approach");
        System.out.println("PASS "+an+" vs "+bn+": 48 seeded matches, 3 scales, negative screen origin, approach/facing/defense/KO/victory/bounds");
    }
    static CombatProfile fixture(CombatProfile p,List<CombatProfile.Attack> attacks) {
        var personality=new CombatProfile.Personality(1,0,0,0,0,0,8,0,20,20,.5,0,"spark","dust");
        return new CombatProfile(p.root(),p.width(),p.height(),p.anchorX(),p.anchorY(),p.hurtbox(),p.speed(),personality,p.animations(),attacks,p.effects());
    }
    static CombatProfile.Attack attack(boolean projectile) {
        return new CombatProfile.Attack("test","melee",0,400,projectile?new Rectangle(-12,-76,24,32):new Rectangle(14,-102,86,100),
            10,120,8,12,5,0,projectile?40:0,0,0,0,1,"melee","charge","impact",false);
    }
    static void precise()throws Exception {
        var p=profile("NarutoShippuden");var idle=fixture(p,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),
            1,100,10,1,1,0,0,0,0,0,1,"melee","charge","impact",false)));
        for(boolean projectile:List.of(false,true)) {
            Body a=new Body(200,500,1,new Rectangle(0,0,900,500)),b=new Body(projectile?380:270,500,1,a.area);
            CombatMatch match=new CombatMatch(a,fixture(p,List.of(attack(projectile))),b,idle,new Random(1));
            for(int t=0;t<33;t++)match.tick();
            require(match.b.hp==100,"damage during telegraph");
            for(int t=0;t<50;t++)match.tick();
            require(match.b.hp==90,"must hit exactly once and respect cooldown, projectile="+projectile+" HP="+match.b.hp);
            a.suspended=true;double x=match.a.x;int hp=match.b.hp;
            for(int t=0;t<30;t++)match.tick();
            require(match.a.x==x&&match.b.hp==hp,"pause must suspend both");
            a.suspended=false;a.available=false;match.tick();
            require(match.isStopped()&&match.visuals().isEmpty(),"removal did not clear match");
        }
        Rectangle left=CombatMatch.box(new Rectangle(10,-50,30,20),100,100,false,1.5);
        Rectangle right=CombatMatch.box(new Rectangle(10,-50,30,20),100,100,true,1.5);
        require(left.x==40&&right.x==115&&left.width==45,"mirrored scaled hitbox");
        Body resizedA=new Body(200,500,1,new Rectangle(0,0,900,500)),resizedB=new Body(380,500,1,resizedA.area);
        CombatMatch resized=new CombatMatch(resizedA,p,resizedB,p,new Random(1));
        for(int t=0;t<60;t++)resized.tick();resizedA.area.setBounds(-1920,-200,1920,1080);resized.tick();
        require(resized.isStopped()&&resized.visuals().isEmpty()&&resized.projectileCount()==0,"monitor resize retained combat/effects");
        System.out.println("PASS telegraph, once-per-activation damage, cooldown, swept projectile, pause, removal, scaled/mirrored hitboxes");
    }
    static void invalidProfiles()throws Exception {
        Path root=Files.createTempDirectory("shinobi-combat-invalid-");Files.createDirectories(root.resolve("conf"));
        Path xml=root.resolve("conf/combat.xml");
        Files.writeString(xml,"<!DOCTYPE combat [<!ENTITY x SYSTEM 'file:///missing'>]><combat version='1'>&x;</combat>");
        boolean rejected=false;try{CombatProfile.load(root);}catch(Exception expected){rejected=true;}
        require(rejected,"external XML entities accepted");
        Files.writeString(xml,"<combat version='99'/>");rejected=false;try{CombatProfile.load(root);}catch(Exception expected){rejected=true;}
        require(rejected,"unsupported profile version accepted");
        Files.delete(xml);Files.delete(root.resolve("conf"));Files.delete(root);
        System.out.println("PASS malformed/unsupported profiles rejected, external XML entities disabled");
    }
    static class Counter implements Behavior {
        int ticks;public void init(Mascot m){}public void next(){ticks++;}
        public void mousePressed(MouseEvent e){}public void mouseReleased(MouseEvent e){}
    }
    @SuppressWarnings("unchecked")
    static void integration()throws Exception {
        factory=new Factory();Field f=NativeFactory.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,factory);
        Main app=Main.getInstance();Field lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);
        ResourceBundle ar=ResourceBundle.getBundle("language",Locale.forLanguageTag("ar"),new com.joconner.i18n.ConfDirectoryUtf8Control(new java.io.File("conf")));
        lang.set(app,ar);
        Field conf=Main.class.getDeclaredField("configurations");conf.setAccessible(true);
        Map<String,Configuration> configs=(Map<String,Configuration>)conf.get(app);
        for(String name:List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover")) configs.put(name,config(name,"behaviors.xml"));
        Manager manager=new Manager();manager.setExitOnLastRemoved(false);
        List<Mascot> pets=new ArrayList<>();
        for(String name:List.of("NarutoShippuden","NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover")) {
            Mascot m=new Mascot(name);m.setAnchor(new Point(120+pets.size()*70,500));m.setManager(manager);manager.getMascots().add(m);
            m.setBehavior(configs.get(name).buildBehavior("StandUp"));pets.add(m);
        }
        Mascot a=pets.get(0),b=pets.get(2),spectator=pets.get(3);Counter counter=new Counter();spectator.setBehavior(counter);
        Method tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
        for(int i=0;i<60;i++)tick.invoke(manager);
        for(Mascot m:pets) require(!manager.getCombat().active(m),"combat auto-started");
        SwingUtilities.invokeAndWait(()->{
            JMenu menu=manager.getCombat().menu(a,ar);require(menu.getText().contains("القتال"),"Arabic combat label");
            JMenu opponents=(JMenu)menu.getItem(0);require(opponents.getItemCount()==5,"opponents must be instance based");
            require(opponents.getItem(0).getText().contains("NarutoShippuden #"),"duplicate instance label");
            // Start via the real menu action, selecting Sasuke specifically.
            opponents.getItem(1).doClick();
        });
        require(manager.getCombat().active(a)&&manager.getCombat().active(b),"menu failed to start selected pair");
        require(!manager.getCombat().active(pets.get(1))&&!manager.getCombat().active(spectator),"bystander entered pair");
        require(!manager.getCombat().start(a,spectator),"opponent swap allowed");
        int before=counter.ticks;
        for(int i=0;i<80;i++)tick.invoke(manager);
        require(counter.ticks==before+80,"bystander normal behavior stopped");
        a.setPaused(true);String stats=manager.getCombat().statistics(b);
        for(int i=0;i<20;i++)tick.invoke(manager);
        require(stats.equals(manager.getCombat().statistics(b)),"one fighter pause failed");a.setPaused(false);
        SwingUtilities.invokeAndWait(()->{
            JMenu menu=manager.getCombat().menu(b,ar);require(!menu.getItem(0).isEnabled(),"opponent menu not locked");
            require(menu.getItem(1).isEnabled(),"stop unavailable on B");menu.getItem(1).doClick();
        });
        require(!manager.getCombat().active(a)&&!manager.getCombat().active(b),"stop failed");
        require(a.getBehavior()!=null&&b.getBehavior()!=null,"normal behavior not restored");
        SwingUtilities.invokeAndWait(()->manager.getCombat().menu(a,ar).getItem(2).doClick());
        require(manager.getCombat().active(a)&&manager.getCombat().active(b),"rematch failed");
        // Actual drag threshold forwarding (cloud press deferred, movement initiates drag).
        Method press=Mascot.class.getDeclaredMethod("mousePressed",MouseEvent.class);press.setAccessible(true);
        Method move=Mascot.class.getDeclaredMethod("updateCloudGesture",MouseEvent.class);move.setAccessible(true);
        Component component=new Canvas();
        SwingUtilities.invokeAndWait(()->{try {
            press.invoke(a,new MouseEvent(component,MouseEvent.MOUSE_PRESSED,1,MouseEvent.BUTTON1_DOWN_MASK,64,64,200,300,1,false,1));
            move.invoke(a,new MouseEvent(component,MouseEvent.MOUSE_DRAGGED,2,MouseEvent.BUTTON1_DOWN_MASK,80,64,216,300,1,false,0));
        }catch(Exception e){throw new RuntimeException(e);}});
        require(!manager.getCombat().active(a)&&!manager.getCombat().active(b)&&a.isDragging(),"drag did not stop pair / start normal drag");
        Method release=Mascot.class.getDeclaredMethod("mouseReleased",MouseEvent.class);release.setAccessible(true);
        release.invoke(a,new MouseEvent(component,MouseEvent.MOUSE_RELEASED,3,0,80,64,216,300,1,false,1));
        require(!a.isDragging(),"drag release failed");
        require(manager.getCombat().start(a,b),"new start after drag failed");manager.getCombat().reload(a.getImageSet());
        require(!manager.getCombat().active(b),"pack reload failed to stop pair");
        app.getProperties().setProperty("CombatEnabled","false");
        require(!manager.getCombat().start(a,b),"disabled combat started");app.getProperties().setProperty("CombatEnabled","true");
        require(manager.getCombat().start(a,b),"enabled combat failed");app.getProperties().setProperty("CombatEnabled","false");tick.invoke(manager);
        require(!manager.getCombat().active(a)&&!manager.getCombat().active(b),"disable did not release pair");app.getProperties().setProperty("CombatEnabled","true");
        require(manager.getCombat().start(a,b),"new start after reload failed");
        manager.setBehaviorAll(configs.get(a.getImageSet()),"ChaseMouse",a.getImageSet());
        require(!manager.getCombat().active(b),"Follow Cursor did not restore normal pair");
        require(manager.getCombat().start(a,b),"start after Follow Cursor failed");b.dispose();
        require(!manager.getCombat().active(a),"disposal retained pair");
        manager.disposeAll();SwingUtilities.invokeAndWait(()->{});
        System.out.println("PASS actual Manager/Mascot: no auto battle, Arabic menu, duplicate instances, pair lock, bystander ticks, stop/rematch, drag/release, reload, Follow Cursor, disposal");
    }
    public static void main(String[]args) {
        try {
            Logger.getLogger("").setLevel(Level.SEVERE);
            invalidProfiles();precise();pair("NarutoShippuden","SasukeShippuden");pair("GojoSatoru","SukunaRyomen");pair("AstaBlackClover","NarutoShippuden");
            integration();System.out.println("PASS "+checks+" assertions, "+combatTicks+" deterministic combat ticks");System.exit(0);
        }catch(Throwable e){e.printStackTrace();System.exit(1);}
    }
}
