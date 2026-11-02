import com.group_finity.mascot.*;import com.group_finity.mascot.combat.*;import com.group_finity.mascot.character.*;import com.group_finity.mascot.audio.*;import java.awt.*;import java.nio.file.*;import java.lang.reflect.*;import java.util.*;import java.util.List;import java.util.logging.*;import javax.imageio.ImageIO;
/** Model, complete configuration load, illustrated-pixel integrity and requested matches. */
public class FullCharacterCheck extends CombatCheck{
 static int naturalTransforms;
 static void catalog()throws Exception{var c=new CharacterCatalog().list();require(c.size()==8,"expected 8 catalog characters, got "+c.size());for(String id:List.of("Tom","Jerry","ErenYeager"))require(c.stream().anyMatch(v->v.id().equals(id)),"missing catalog "+id);System.out.println("PASS 8 selectable characters, Titan is not a separate catalog entry");}
 static void assets()throws Exception{
  factory=new Factory();factory.env.resize(new Rectangle(0,0,1400,800));Field nf=NativeFactory.class.getDeclaredField("instance");nf.setAccessible(true);nf.set(null,factory);time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
  for(String name:List.of("Tom","Jerry","ErenYeager")){
   var config=config(name,"behaviors.xml");require(config.getBehaviorNames().stream().anyMatch(n->n.startsWith(name+"_")),"manual actions not registered");
   var p=profile(name);int frameCount=0;
   for(var form:p.forms())for(var anim:form.animations().values())for(var frame:anim.frames()){
    require(frame.image().startsWith("art/")||frame.image().endsWith("hidden.png"),"old placeholder referenced: "+frame.image());
    var im=ImageIO.read(form.root().resolve(frame.image()).toFile());require(im!=null&&im.getWidth()==form.width()&&im.getHeight()==form.height(),"sprite dimensions "+frame.image());
    int visible=0;for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++)if((im.getRGB(x,y)>>>24)>32)visible++;
    require(frame.image().endsWith("hidden.png")||visible>250,"blank/corrupt sprite: "+frame.image());require((im.getRGB(0,0)>>>24)==0,"opaque backdrop "+frame.image());frameCount++;
   }
   require(AudioPack.load(p.root()).size()>=18,"audio manifest missing");
   Mascot m=mascot(name,600,800);m.getEnvironment().refreshWorkArea();
   for(String action:List.of("Walk","Run","Dash","Jumping","Bouncing",name+"_punch",name+"_kick",name+"_dodge",name+"_ko",name+"_victory")){m.setAnchor(new Point(600,800));m.getEnvironment().refreshWorkArea();run(config,m,action,Map.of(),60,false);}
   System.out.println("PASS "+name+": configuration, manual acting, walking/running/jumping, "+frameCount+" frame references and original audio");
  }
  require(profile("Jerry").height()<profile("Tom").height(),"Jerry not smaller");require(profile("Jerry").speed()>profile("Tom").speed(),"Jerry not faster");
 }
 static void match(String one,String two)throws Exception{
  int winsA=0,transforms=0;
  for(int seed=0;seed<24;seed++)for(double scale:List.of(.75,1.0,1.5)){
   var pa=profile(one);var pb=profile(two);Rectangle area=new Rectangle(-1400,-200,1400,800);var ba=new Body(-1150,600,scale,area);var bb=new Body(-200,600,scale,area);var m=new CombatMatch(ba,pa,bb,pb,new Random(seed));boolean transformed=false;
   for(int i=0;i<5000&&!m.isStopped();i++){m.tick();combatTicks++;for(var f:List.of(m.a,m.b)){
    if(i<25)require(f.form==CombatMatch.Form.HUMAN,"transformed immediately");require(f.transformations<=1,"repeat transformation");if(f.form==CombatMatch.Form.TITAN)transformed=true;
    if(f.state==CombatState.TRANSFORMING)require(f.attack==null&&f.hurtbox().isEmpty(),"transform attack/hurtbox active");
   }for(var v:m.visuals())require(v.profile.effects().containsKey(v.effect),"wrong VFX owner");}
   require(m.isFinished()&&m.isStopped(),"stuck "+one+" vs "+two+" seed "+seed);require(m.a.form==CombatMatch.Form.HUMAN&&m.b.form==CombatMatch.Form.HUMAN,"not reset after match");if(m.winner()==ba)winsA++;if(transformed)transforms++;
  }
  if(one.equals("Tom")&&two.equals("Jerry"))require(winsA>0&&winsA<72,"unbalanced Tom/Jerry "+winsA+" / 72");
  if(one.equals("ErenYeager")){require(transforms>0,"Eren never transformed naturally");naturalTransforms+=transforms;}
  System.out.println("PASS "+one+" vs "+two+": 72 seeded fights / 3 scales; wins="+winsA+"/72; transforms="+transforms);
 }
 public static void main(String[]a){try{Logger.getLogger("").setLevel(Level.SEVERE);catalog();assets();for(var pair:List.of(new String[]{"Tom","Jerry"},new String[]{"Tom","NarutoShippuden"},new String[]{"Jerry","SasukeShippuden"},new String[]{"ErenYeager","NarutoShippuden"},new String[]{"ErenYeager","Tom"}))match(pair[0],pair[1]);System.out.println("PASS "+checks+" assertions / "+combatTicks+" combat ticks / "+naturalTransforms+" Eren transformations");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
