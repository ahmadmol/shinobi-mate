import java.awt.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/** Applies phase choreography to existing packs; keeps original art and immutable baseline atlases. */
public class BuildCombatPolishPacks {
 static Document doc;static Element root,animations;
 static String pose(String name){return "combat/polish/"+name+".png";}
 static Element animation(String id,String...names){
  Element old=find(animations,"animation",id);if(old!=null)animations.removeChild(old);
  Element a=doc.createElement("animation");a.setAttribute("id",id);
  for(String name:names){Element f=doc.createElement("frame");f.setAttribute("image",name.contains("/")?name:pose(name));f.setAttribute("ticks","3");a.appendChild(f);}
  animations.appendChild(a);return a;
 }
 static Element find(Element parent,String tag,String id){for(var n=parent.getFirstChild();n!=null;n=n.getNextSibling())if(n instanceof Element e&&e.getTagName().equals(tag)&&e.getAttribute("id").equals(id))return e;return null;}
 static Element child(Element e,String tag){return (Element)e.getElementsByTagName(tag).item(0);}
 static void set(Element e,String...pairs){for(int i=0;i<pairs.length;i+=2)e.setAttribute(pairs[i],pairs[i+1]);}
 static Element attack(String id){return find(child(root,"attacks"),"attack",id);}
 static void phases(String id,String[]start,String[]active,String[]recovery){
  animation(id+"-start",start);animation(id+"-active",active);animation(id+"-recover",recovery);
  set(attack(id),"chargeAnimation",id+"-start","animation",id+"-active","recoveryAnimation",id+"-recover");
 }
 static void combo(String id,String[]contacts,String[]windups,int[]damage,int knock){
  Element a=attack(id);set(a,"active",Integer.toString((contacts.length-1)*6+3));
  for(int i=0;i<contacts.length;i++){
   animation(id+"-contact-"+i,contacts[i]);animation(id+"-windup-"+i,windups[i]);
   Element s=doc.createElement("strike");set(s,"at",Integer.toString(i*6),"duration","3","damage",Integer.toString(damage[i]),
    "knockback",Integer.toString(i==contacts.length-1?knock:0),"animation",id+"-contact-"+i,"windup",id+"-windup-"+i);a.appendChild(s);
  }
 }
 static Element copy(String source,String id){Element a=(Element)attack(source).cloneNode(true);a.setAttribute("id",id);
  for(Node n=a.getFirstChild();n!=null;){Node next=n.getNextSibling();if(n instanceof Element e&&e.getTagName().equals("strike"))a.removeChild(n);n=next;}
  child(root,"attacks").appendChild(a);return a;}
 static void sound(String id,String start,String active,String impact){set(attack(id),"startupSound",start,"attackSound",active,"impactSound",impact);}
 public static void main(String[]args)throws Exception {
  for(String name:List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover")){
   Path path=Path.of("img",name),xml=path.resolve("conf/combat.xml");
   Path baseline=Path.of("art/combat-polish",name,"0.9.2/baseline-combat.xml");
   if(!Files.exists(baseline))Files.copy(xml,baseline);
   doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(baseline.toFile());root=doc.getDocumentElement();animations=child(root,"animations");
   // Hitboxes fit the hand/blade, not the entire 128px sprite.
   NodeList attacks=child(root,"attacks").getElementsByTagName("attack");
   for(int i=0;i<attacks.getLength();i++){
    Element a=(Element)attacks.item(i);boolean projectile=Double.parseDouble(a.getAttribute("projectileSpeed"))>0;
    boolean sword=name.startsWith("Asta")||name.startsWith("Sasuke")&&!projectile&&!a.getAttribute("kind").equals("special");
    set(a,"weapon",projectile?"energy":sword?"sword":"none","startupSound",sword?"sword":"grunt","attackSound",sword?"sword":"whoosh","impactSound","impact");
    set(child(a,"hitbox"),"x",projectile?"-10":"12","y",projectile?"-71":"-80","width",projectile?"20":sword?"72":"56","height",projectile?"22":"50");
    set(a,"cooldown",Integer.toString(Math.max(Integer.parseInt(a.getAttribute("cooldown")),a.getAttribute("kind").equals("special")?140:20)));
   }
   switch(name){
    case "NarutoShippuden"->{
     phases("light-combo",new String[]{"punch-start"},new String[]{"punch-contact"},new String[]{"punch-recover"});
     combo("light-combo",new String[]{"punch-contact","kick-contact"},new String[]{"punch-start","kick-start"},new int[]{4,7},24);
     Element kick=copy("light-combo","kick-combo");set(kick,"cooldown","32","weight","1.1","startup","5");
     phases("kick-combo",new String[]{"kick-start"},new String[]{"kick-contact"},new String[]{"kick-recover"});
     combo("kick-combo",new String[]{"kick-contact","jump-contact"},new String[]{"kick-start","jump-start"},new int[]{5,8},28);
     phases("kunai",new String[]{"kunai-start"},new String[]{"kunai-contact"},new String[]{"punch-recover"});set(attack("kunai"),"weapon","kunai");
     phases("shadow-clone",new String[]{"clone-sign","punch-start"},new String[]{"punch-contact","kick-contact"},new String[]{"punch-recover"});set(attack("shadow-clone"),"effect","chakra","chargeEffect","chakra");
     phases("rasengan",new String[]{"rasengan-low","rasengan-charge"},new String[]{"rasengan-contact"},new String[]{"rasengan-recover","punch-recover"});
     set(attack("rasengan"),"minRange","0","effect","chakra","chargeEffect","chakra");sound("rasengan","chakra","whoosh","heavy");
     phases("aerial-ninja",new String[]{"jump-start"},new String[]{"jump-contact","kick-contact"},new String[]{"kick-recover"});animation("victory","new-victory");
    }
    case "SasukeShippuden"->{
     phases("sword-combo",new String[]{"sword-start"},new String[]{"sword-contact"},new String[]{"sword-recover"});
     combo("sword-combo",new String[]{"sword-contact","backswing-contact","kick-contact"},new String[]{"sword-start","backswing-start","kick-start"},new int[]{4,4,7},26);
     phases("shuriken",new String[]{"shuriken-start"},new String[]{"shuriken-contact"},new String[]{"kick-recover"});set(attack("shuriken"),"weapon","shuriken");
     phases("fire-style",new String[]{"fire-start"},new String[]{"fire-contact"},new String[]{"kick-recover"});sound("fire-style","fire","fire","heavy");
     phases("chidori",new String[]{"chidori-low","chidori-charge"},new String[]{"chidori-contact"},new String[]{"chidori-recover","sword-recover"});
     set(attack("chidori"),"minRange","0","chargeEffect","lightning");sound("chidori","electric","whoosh","electric");
     phases("aerial-sword",new String[]{"sword-start"},new String[]{"backswing-start","backswing-contact"},new String[]{"sword-recover"});
     Element counter=copy("sword-combo","counter");set(counter,"active","4","startup","2","damage","12","cooldown","50","weight","0.6");
     phases("counter",new String[]{"sword-start"},new String[]{"backswing-contact"},new String[]{"sword-recover"});
    }
    case "GojoSatoru"->{
     phases("fast-melee",new String[]{"punch-start"},new String[]{"punch-contact"},new String[]{"kick-recover"});
     combo("fast-melee",new String[]{"punch-contact","elbow-contact","kick-contact"},new String[]{"punch-start","elbow-start","kick-start"},new int[]{3,3,6},25);
     phases("blue-pull",new String[]{"blue-start"},new String[]{"blue-contact"},new String[]{"purple-recover"});set(attack("blue-pull"),"chargeEffect","blue");sound("blue-pull","chakra","chakra","impact");
     phases("red-blast",new String[]{"red-start"},new String[]{"red-contact"},new String[]{"purple-recover"});set(attack("red-blast"),"chargeEffect","red");sound("red-blast","chakra","whoosh","heavy");
     phases("blue-red-special",new String[]{"purple-low","purple-charge"},new String[]{"purple-contact"},new String[]{"purple-recover"});
     set(attack("blue-red-special"),"effect","purple","chargeEffect","purple","cooldown","250","weight","0.3","minRange","0","damage","35");sound("blue-red-special","chakra","chakra","heavy");
     animation("taunt","taunt");set(child(root,"personality"),"dodgeEffect","blue","defenseCooldown","45");
    }
    case "SukunaRyomen"->{
     phases("melee-strike",new String[]{"punch-start"},new String[]{"punch-contact"},new String[]{"heavy-recover"});
     combo("melee-strike",new String[]{"punch-contact","slash-contact","heavy-contact"},new String[]{"punch-start","slash-start","heavy-start"},new int[]{4,4,8},36);
     phases("cursed-slash",new String[]{"curse-start"},new String[]{"curse-contact"},new String[]{"heavy-recover"});set(attack("cursed-slash"),"weapon","slash");
     phases("multi-slash",new String[]{"multi-start"},new String[]{"multi-contact","curse-contact"},new String[]{"heavy-recover"});set(attack("multi-slash"),"weapon","slash");
     combo("multi-slash",new String[]{"multi-contact","curse-contact"},new String[]{"multi-start","curse-start"},new int[]{7,10},38);
     phases("heavy-cursed",new String[]{"heavy-start","curse-start"},new String[]{"dash-contact","heavy-contact"},new String[]{"heavy-recover"});set(attack("heavy-cursed"),"effect","cursed","chargeEffect","cursed","minRange","0");sound("heavy-cursed","dark","whoosh","heavy");
     phases("counter-strike",new String[]{"slash-start"},new String[]{"slash-contact"},new String[]{"heavy-recover"});
     Element fire=copy("cursed-slash","fire-arrow");set(fire,"kind","special","effect","fire","chargeEffect","fire","startup","16","active","5","damage","24","cost","32","cooldown","180","weight","0.6","projectileSpeed","18");
     phases("fire-arrow",new String[]{"fire-start","fire-charge"},new String[]{"fire-contact"},new String[]{"heavy-recover"});sound("fire-arrow","fire","fire","heavy");
     animation("victory","new-victory");
    }
    case "AstaBlackClover"->{
     phases("sword-slash",new String[]{"sword-start"},new String[]{"sword-contact"},new String[]{"heavy-recover"});
     combo("sword-slash",new String[]{"sword-contact","backswing-contact","heavy-contact"},new String[]{"sword-start","backswing-start","heavy-start"},new int[]{4,5,9},40);
     phases("heavy-sword",new String[]{"heavy-start","heavy-start","heavy-swing"},new String[]{"heavy-swing","heavy-contact"},new String[]{"heavy-recover"});sound("heavy-sword","sword","sword","heavy");
     phases("dash-slash",new String[]{"dash-start"},new String[]{"dash-contact"},new String[]{"heavy-recover"});
     phases("jumping-sword",new String[]{"jump-start"},new String[]{"jump-contact"},new String[]{"heavy-recover"});
     phases("anti-magic",new String[]{"anti-start","heavy-start"},new String[]{"heavy-swing","heavy-contact"},new String[]{"heavy-recover"});set(attack("anti-magic"),"effect","anti-magic","chargeEffect","anti-magic");sound("anti-magic","dark","sword","heavy");
     Element dash=copy("dash-slash","anti-magic-dash");set(dash,"effect","anti-magic","chargeEffect","anti-magic","cost","24","cooldown","100","damage","20","speed","8","minRange","0","startup","12");
     phases("anti-magic-dash",new String[]{"anti-start"},new String[]{"anti-contact"},new String[]{"heavy-recover"});sound("anti-magic-dash","dark","sword","heavy");
     animation("block","parry");animation("victory","new-victory");
    }
   }
   Color color=name.startsWith("Naruto")?new Color(55,174,255):name.startsWith("Sukuna")?new Color(245,83,118):new Color(135,110,255);
   for(String effect:List.of("chakra","cursed","anti-magic","purple","shockwave")){
    Color c=effect.equals("chakra")?new Color(55,174,255):effect.equals("anti-magic")?new Color(45,30,65):effect.equals("cursed")?new Color(230,65,110):color;
    Element e=doc.createElement("effect");set(e,"id",effect,"frameTicks","2","cloneImage",pose(name.startsWith("Naruto")?"punch-contact":name.startsWith("Asta")?"sword-contact":name.startsWith("Sasuke")?"sword-contact":"punch-contact"));
    for(int f=0;f<4;f++){String image="combat/vfx/"+effect+f+".png";
     BuildCombatPacks.draw(path.resolve(image),effect,f,new BuildCombatPacks.Pack(name,"",c,List.of()));
     Element frame=doc.createElement("frame");frame.setAttribute("image",image);e.appendChild(frame);}
    child(root,"effects").appendChild(e);
   }
   var tf=TransformerFactory.newInstance().newTransformer();tf.setOutputProperty(OutputKeys.INDENT,"yes");tf.transform(new DOMSource(doc),new StreamResult(xml.toFile()));
   Path portable=Path.of("Shimeji-ee/img",name);Files.copy(xml,portable.resolve("conf/combat.xml"),StandardCopyOption.REPLACE_EXISTING);
   try(var files=Files.walk(path.resolve("combat"))){for(Path source:files.filter(Files::isRegularFile).toList()) {Path dest=portable.resolve(path.relativize(source));Files.createDirectories(dest.getParent());Files.copy(source,dest,StandardCopyOption.REPLACE_EXISTING);}}
   System.out.println("PASS choreography, weapon, hitbox and sound profile: "+name);
  }
 }
}
