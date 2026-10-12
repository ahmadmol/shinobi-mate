import com.group_finity.mascot.combat.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
/** Asset integrity and real model startup/active/recovery choreography. */
public class CombatVisualCheck extends CombatCheck {
 public static void main(String[] args) {
  try { checkVisuals(); } catch(Exception e) {throw new RuntimeException(e);}
 }
 static void checkVisuals() throws Exception {
  var names=List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover");
  var poses=List.of("stance","melee-start","melee-active","melee-recovery","ranged-start","ranged-active","special-start","special-active","special-recovery","dodge-start","dodge-active","block","hit","knockback","ko","victory");
  BufferedImage sheet=new BufferedImage(16*128,5*160,BufferedImage.TYPE_INT_RGB);
  Graphics2D g=sheet.createGraphics();g.setColor(new Color(29,32,45));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
  for(int row=0;row<names.size();row++) {
   String name=names.get(row);var p=profile(name);
   Set<Integer> hashes=new HashSet<>();
   for(int i=0;i<poses.size();i++) {
    BufferedImage im=ImageIO.read(p.root().resolve("combat/poses/"+poses.get(i)+".png").toFile());
    require(im!=null&&im.getWidth()==128&&im.getHeight()==128,"invalid frame dimensions");
    int pixels=0,hash=1;
    for(int y=0;y<128;y++)for(int x=0;x<128;x++) {
     int rgb=im.getRGB(x,y);hash=hash*31+rgb;
     if((rgb>>>24)>12){pixels++;require(x>0&&x<127,"weapon/limb clipped at horizontal edge");}
    }
    require(pixels>250&&pixels<12000,"invalid transparency/empty sprite");require(hashes.add(hash),"duplicate pose");
    g.drawImage(im,i*128,row*160+24,null);
    g.setColor(Color.WHITE);g.drawString(poses.get(i),i*128+4,row*160+155);
   }
   g.setColor(Color.WHITE);g.drawString(name,4,row*160+18);
   for(var attack:p.attacks()) {
    var idle=fixture(p,List.of(new CombatProfile.Attack("idle","melee",900,1000,new Rectangle(0,0,1,1),1,100,10,1,1,0,0,0,0,0,1,"melee","charge","impact",false)));
    Body a=new Body(300,500,1,new Rectangle(0,0,1200,500)),b=new Body(300+Math.max(70,attack.minRange()+5),500,1,a.area);
    var match=new CombatMatch(a,fixture(p,List.of(attack)),b,idle,new Random(2));
    if(attack.id().contains("counter")){var ready=CombatMatch.Fighter.class.getDeclaredField("counterReady");ready.setAccessible(true);ready.setBoolean(match.a,true);}
    Set<String> rendered=new HashSet<>();
    for(int t=0;t<450;t++){match.tick();if(a.image!=null)rendered.add(a.image);}
    for(String id:List.of(attack.chargeAnimation(),attack.animation(),attack.recoveryAnimation()))
     require(p.animations().get(id).frames().stream().anyMatch(f->rendered.contains(f.image())),"missing attack phase "+name+"/"+attack.id()+"/"+id);
   }
  }
  g.dispose();Files.createDirectories(Path.of("art/combat-polish"));
  ImageIO.write(sheet,"png",Path.of("art/combat-polish/contact-sheet.png").toFile());
  System.out.println("PASS 80 original transparent unique combat poses, bounds, all configured attacks startup/active/recovery; "+checks+" assertions");
 }
}
