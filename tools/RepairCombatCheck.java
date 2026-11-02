import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Post-transformation damage, move contact, both orders/facings and no HP refill. */
public class RepairCombatCheck extends CombatCheck {
 static void natural(String opponent, int count)throws Exception {
  var p=profile("ErenYeager");var q=profile(opponent);
  int wins=0,transforms=0,damaging=0;Map<String,Integer> moves=new TreeMap<>();
  for(int seed=0;seed<count;seed++)for(double scale:List.of(1.,1.5))for(boolean reverse:List.of(false,true)){
   Rectangle area=new Rectangle(-1600,-200,1600,900);
   Body eb=new Body(-1250,700,scale,area),ob=new Body(-350,700,scale,area);
   var m=reverse?new CombatMatch(ob,q,eb,p,new Random(seed)):new CombatMatch(eb,p,ob,q,new Random(seed));
   var f=reverse?m.b:m.a;var enemy=reverse?m.a:m.b;
   boolean titan=false,hit=false;int oldHp=100;CombatProfile.Attack old=null;
   for(int tick=0;tick<5000&&!m.isStopped();tick++){
    int hp=f.hp,enemyHp=enemy.hp;boolean finished=m.isFinished();m.tick();
    if(!m.isStopped())require(f.hp<=hp,"HP refilled during battle");
    if(f.form==CombatMatch.Form.TITAN){titan=true;if(enemy.hp<enemyHp)hit=true;
     if(f.attack!=null&&f.attack!=old)moves.merge(f.attack.id(),1,Integer::sum);
    }
    old=f.attack;
    if(Boolean.getBoolean("repair.trace")&&seed==0&&scale==1&&!reverse&&!finished&&(f.hp!=hp||enemy.hp!=enemyHp))
     System.out.println(tick+" HP "+f.hp+"/"+enemy.hp+" "+f.form+" "+f.state+" "+(f.attack==null?"-":f.attack.id()+":"+f.attackAge())+" enemy "+enemy.state+" dx="+Math.round(Math.abs(f.x-enemy.x)));
   }
   require(m.isFinished()&&m.isStopped(),"stuck "+opponent);if(titan)transforms++;if(hit)damaging++;if(m.winner()==eb)wins++;
  }
  System.out.println("RESULT Eren vs "+opponent+" fights="+(count*4)+" wins="+wins+" transforms="+transforms+" damagingTitans="+damaging+" moves="+moves);
  if(!Boolean.getBoolean("repair.baseline")){require(wins>0&&wins<count*4,"balance "+opponent);require(damaging>=transforms*.7,"Titan rarely lands damage");}
 }
 static void contacts()throws Exception {
  var titan=profile("ErenYeager").transformation().target();var enemy=profile("Tom");
  for(var at:titan.attacks())for(boolean left:List.of(false,true))for(double scale:List.of(1.,1.5)) {
   Rectangle area=new Rectangle(0,0,1600,900);
   Body a=new Body(left?900:500,900,scale,area),b=new Body(left?900-85*scale:500+85*scale,900,scale,area);
   var idle=fixture(enemy,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,1000,10,1,1,0,0,0,0,0,1,"punch","charge","impact",false)));
   var m=new CombatMatch(a,fixture(titan,List.of(at)),b,idle,new Random(2));boolean hit=false;
   for(int i=0;i<130;i++){m.tick();if(m.b.hp<100)hit=true;}
   require(hit,"Titan move no contact "+at.id()+" left="+left+" scale="+scale);m.stop();
  }
  System.out.println("PASS all five Titan attacks land actual damage, both facings, 100/150% scale");
 }
 static void cartoons()throws Exception {
  int wins=0,counters=0,retreats=0;
  for(int seed=0;seed<40;seed++)for(double scale:List.of(1.,1.5))for(boolean reverse:List.of(false,true)){
   Rectangle area=new Rectangle(0,0,1500,900);Body tom=new Body(350,900,scale,area),jerry=new Body(1100,900,scale,area);
   var m=reverse?new CombatMatch(jerry,profile("Jerry"),tom,profile("Tom"),new Random(seed)):new CombatMatch(tom,profile("Tom"),jerry,profile("Jerry"),new Random(seed));
   var f=reverse?m.a:m.b;CombatProfile.Attack old=null;
   for(int t=0;t<5000&&!m.isStopped();t++){
    double oldX=f.x;var at=f.attack;int age=f.attackAge();m.tick();
    if(f.attack!=null&&f.attack!=old&&f.attack.id().equals("counterattack"))counters++;
    if(at!=null&&at.kind().equals("retreat")&&age>=at.startup()&&age<at.startup()+at.active()/2&&Math.abs(f.x-oldX)>1)retreats++;
    if(!m.isFinished())require(!m.a.hurtbox().intersects(m.b.hurtbox()),"cartoon bodies overlap");
    old=f.attack;
   }
   require(m.isFinished()&&m.isStopped(),"cartoon match stuck");if(m.winner()==tom)wins++;
  }
  require(wins>=32&&wins<=128,"Tom/Jerry balance outside 20-80%: "+wins);require(counters>0&&retreats>0,"no real counter/retreat");
  System.out.println("RESULT Tom vs Jerry fights=160 TomWins="+wins+" JerryWins="+(160-wins)+" counterStarts="+counters+" retreatMovementTicks="+retreats);
  var tom=profile("Tom");var at=tom.attacks().getFirst();Rectangle area=new Rectangle(0,0,1600,900);
  var idle=fixture(tom,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,1000,10,1,1,0,0,0,0,0,1,"punch","charge","impact",false)));
  var m=new CombatMatch(new Body(500,900,1,area),fixture(tom,List.of(at)),new Body(580,900,1,area),idle,new Random(2));
  for(int i=0;i<80;i++){m.tick();if(m.a.attack!=null)m.b.x=1300;require(m.visuals().stream().noneMatch(v->v.effect.equals("impact")),"false impact on missed swing");}
  require(m.b.hp==100,"miss dealt damage");m.stop();System.out.println("PASS missed swing emits no impact VFX or damage");
 }
 public static void main(String[]args) {try{
  java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);
  if(!Boolean.getBoolean("repair.baseline"))contacts();
  for(String enemy:List.of("NarutoShippuden","SasukeShippuden","Tom"))natural(enemy,Integer.getInteger("repair.seeds",24));
  if(!Boolean.getBoolean("repair.baseline"))cartoons();
  System.out.println("PASS repair assertions="+checks);System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
