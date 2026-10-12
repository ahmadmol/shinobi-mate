import com.group_finity.mascot.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.audio.SoundSystem;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
/** Forced move showcase through the actual native combat renderer, in both directions. */
public class NinjaNativeMoveCheck extends CombatPolishDesktopCheck {
 static void set(Object obj,String name,Object value)throws Exception{Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,value);}
 static CombatMatch match()throws Exception {
  Field pairs=CombatEngine.class.getDeclaredField("pairs");pairs.setAccessible(true);
  Object pair=((java.util.List<?>)pairs.get(manager.getCombat())).getFirst();Method m=pair.getClass().getDeclaredMethod("match");m.setAccessible(true);return (CombatMatch)m.invoke(pair);
 }
 public static void main(String[]args) {
  try {
   var stream=new java.io.PrintStream(Files.newOutputStream(Path.of("native-moves.log")),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(stream);System.setErr(stream);
   Files.createDirectories(Path.of("captures"));Main app=Main.getInstance();app.run();Method get=Main.class.getDeclaredMethod("getManager");get.setAccessible(true);
   manager=(Manager)get.invoke(app);manager.setExitOnLastRemoved(false);Thread.sleep(500);manager.stop();
   mascotWindow=Mascot.class.getDeclaredMethod("getWindow");mascotWindow.setAccessible(true);tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
   Map<String,Mascot> pets=new HashMap<>();for(Mascot m:manager.getMascots())pets.put(m.getImageSet(),m);Rectangle area=desktopArea();int cases=0;
   for(String name:java.util.List.of("NarutoShippuden","SasukeShippuden"))for(boolean right:java.util.List.of(false,true)) {
    var p=CombatProfile.load(AppPaths.pack(name));
    String[] moves=name.startsWith("Naruto")?new String[]{"kunai","shuriken","rasengan","light-combo"}:new String[]{"sword-combo","shuriken","chidori","fire-style"};
    for(String id:moves) {
     Mascot a=pets.get(name),b=pets.get(name.startsWith("Naruto")?"SasukeShippuden":"NarutoShippuden");
     var at=p.attacks().stream().filter(x->x.id().equals(id)).findFirst().orElseThrow();
     double distance=at.projectile()?170:at.speed()>0?130:70;
     a.setAnchor(new Point(area.x+area.width/2,(int)area.getMaxY()));b.setAnchor(new Point((int)(a.getAnchor().x+(right?distance:-distance)),(int)area.getMaxY()));
     require(manager.getCombat().start(a,b),"native move start");pulse(25);CombatMatch m=match();
     m.a.attack=at;m.a.state=at.special()?CombatState.SPECIAL_ATTACK:CombatState.ATTACK;m.a.right=right;m.a.energy=100;
     set(m.a,"attackAge",0);set(m.a,"age",0);set(m.a,"hitUsed",false);m.b.attack=null;m.b.state=CombatState.RECOVER;m.b.energy=0;set(m.b,"decisionAt",100000);
     String label=name+"-"+id+"-"+(right?"right":"left");int before=m.b.hp;
     for(int i=0;i<at.startup()+at.active()+25;i++) {
      pulse(1);
      if(i==at.startup()-2)capture(label+"-charge",area);
      if(i==at.startup()+1)capture(label+"-active",area);
      if(i==at.startup()+at.active()+1)capture(label+"-recovery",area);
     }
     require(m.b.hp<before,"native move did not contact "+label);require(m.a.right==right,"native facing changed "+label);
     manager.getCombat().stop(a);SwingUtilities.invokeAndWait(()->{});require(!manager.getCombat().active(b),"forced move stop retained pair");cases++;
     System.out.println("PASS native charge/active/recovery/contact "+label);
    }
   }
   manager.disposeAll();SoundSystem.close();SwingUtilities.invokeAndWait(()->{});require(SoundSystem.clipCount()==0,"clips survived");System.out.println("PASS "+cases+" actual native move cases, both directions, dedicated capture buffers; "+assertions+" assertions");System.exit(0);
  }catch(Throwable e){e.printStackTrace();if(manager!=null){manager.stop();manager.disposeAll();}SoundSystem.close();System.exit(1);}
 }
}
