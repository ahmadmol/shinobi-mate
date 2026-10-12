import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
/** Direction, behind-target, contact timing and cleanup regressions for the two ninja packs. */
public class NinjaPolishCheck extends CombatCheck {
 static CombatProfile passive(CombatProfile p){return fixture(p,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,1000,10,1,1,0,0,0,0,0,1,"melee","charge","impact",false)));}
 static void art()throws Exception {
  for(String name:List.of("NarutoShippuden","SasukeShippuden")) {
   var p=profile(name);require(p.width()==288&&p.height()==288&&p.anchorX()==144&&p.anchorY()==288,"feet anchor");
   int count=0;try(var files=Files.list(p.root().resolve("combat/ninja093"))) {
    for(Path path:files.filter(f->f.toString().endsWith(".png")).toList()) {
     var im=ImageIO.read(path.toFile());require(im.getWidth()==288&&im.getHeight()==288,"canvas");
     int bottom=0,pixels=0;
     for(int y=0;y<288;y++)for(int x=0;x<288;x++)if((im.getRGB(x,y)>>>24)>12){pixels++;bottom=Math.max(bottom,y);require(x>0&&x<287,"clipped arm/weapon");}
     require(pixels>400&&bottom==287,"empty sprite/baseline");count++;
    }
   }require(count==24,"24 poses per ninja");
   for(var anim:p.animations().values())for(var f:anim.frames())require(f.image().startsWith("combat/ninja093/"),"legacy direction mixed into ninja animation");
   for(String kind:List.of("kunai","shuriken","rasengan-burst","chidori-burst","sword-arc"))for(int i=0;i<8;i++)require(ImageIO.read(p.root().resolve("combat/ninja093/vfx/"+kind+"-"+i+".png").toFile()).getRGB(0,0)==0,"VFX background");
  }
  var ninja=profile("NarutoShippuden");require(ninja.attacks().stream().anyMatch(a->a.id().equals("shuriken")&&a.polish().weapon().equals("shuriken")),"Naruto shuriken missing");
 }
 static void direction()throws Exception {
  for(String name:List.of("NarutoShippuden","SasukeShippuden"))for(var at:profile(name).attacks())for(boolean right:List.of(false,true)) {
   var p=profile(name);double distance=at.projectile()?170:at.speed()>0?145:70;
   var a=new Body(500,600,1,new Rectangle(0,0,1200,600));var b=new Body(500+(right?distance:-distance),600,1,a.area);
   var m=new CombatMatch(a,fixture(p,List.of(at)),b,passive(p),new Random(7));
   if(at.id().contains("counter")){var field=CombatMatch.Fighter.class.getDeclaredField("counterReady");field.setAccessible(true);field.setBoolean(m.a,true);}
   boolean started=false,damaged=false;double previous=m.a.x;int strikes=0,hp=100;
   for(int i=0;i<160;i++) {
    m.tick();
    if(m.a.attack!=null){started=true;require(m.a.right==right,"attack facing "+at.id());require((m.a.x-previous)*(right?1:-1)>=0,"backwards dash "+at.id());require((m.b.x-m.a.x)*(right?1:-1)>0,"dash crossed target "+at.id());}
    if(m.b.hp<hp){strikes++;hp=m.b.hp;damaged=true;}
    previous=m.a.x;
    if(started&&m.a.attack==null&&m.projectileCount()==0)break;
   }
   require(started&&damaged,"attack failed forward "+name+"/"+at.id()+" right="+right);
   require(strikes<=Math.max(1,at.polish().strikes().size()),"duplicate hit per window "+at.id());
   // Put target behind after startup. Locked facing remains forward, with no backwards hitbox.
   if(!at.projectile()) {
    a=new Body(500,600,1,a.area);b=new Body(570,600,1,a.area);m=new CombatMatch(a,fixture(p,List.of(at)),b,passive(p),new Random(7));
    if(at.id().contains("counter")){var f=CombatMatch.Fighter.class.getDeclaredField("counterReady");f.setAccessible(true);f.setBoolean(m.a,true);}
    for(int i=0;i<60&&m.a.attack==null;i++)m.tick();require(m.a.attack!=null,"attack start fixture");
    m.b.x=m.a.x-95;b.x=m.b.x;int life=m.b.hp;
    for(int i=0;i<at.startup()+at.active();i++)m.tick();
    require(m.b.hp==life,"attack hit behind "+at.id());
   }
   System.out.println("PASS forward/behind/timing "+name+"/"+at.id()+" right="+right+" hits="+strikes);
  }
 }
 static void soakNinja()throws Exception {
  int battles=0,ticks=0,vfx=0,shots=0;
  for(int seed=0;seed<40;seed++)for(boolean right:List.of(false,true)) {
   var p=profile("NarutoShippuden");var q=profile("SasukeShippuden");var area=new Rectangle(seed%2==0?-1300:0,-100,1200,750);
   double scale=List.of(.75,1.0,1.25).get(seed%3);
   var a=new Body(area.x+(right?250:850),area.getMaxY(),scale,area);var b=new Body(area.x+(right?850:250),area.getMaxY(),scale,area);
   var m=new CombatMatch(a,p,b,q,new Random(seed));int i=0;boolean victory=false;
   while(!m.isStopped()&&i++<4000){m.tick();ticks++;vfx=Math.max(vfx,m.visuals().size());shots=Math.max(shots,m.projectileCount());victory|=m.a.state==CombatState.VICTORY||m.b.state==CombatState.VICTORY;}
   require(i<4000&&victory,"no complete battle");require(m.projectileCount()==0&&m.visuals().isEmpty(),"stale battle resources");battles++;
  }
  Files.createDirectories(Path.of("output/ninja-polish"));Files.writeString(Path.of("output/ninja-polish/soak.json"),"{\"completeBattles\":"+battles+",\"ticks\":"+ticks+",\"maxVfx\":"+vfx+",\"maxProjectiles\":"+shots+",\"postBattleVfx\":0,\"postBattleProjectiles\":0}");
  System.out.println("PASS "+battles+" complete Naruto/Sasuke battles, both directions, 3 scales, cleanup; "+ticks+" ticks");
 }
 public static void main(String[]args){try{art();direction();soakNinja();System.out.println("PASS "+checks+" ninja polish assertions");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
