import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Reproducible original abstract chibi VFX. Character poses reuse the existing pack art. */
public class BuildCombatPacks {
    record Move(String id,String kind,int min,int range,int damage,int cooldown,int startup,int active,
                int recovery,int speed,int projectile,int knock,int jump,int cost,double weight,String effect,boolean cloneEffect) { }
    record Pack(String name,String personality,Color color,List<Move> moves) { }
    static Move m(String id,String kind,int min,int range,int damage,int cd,int start,int active,int recovery,
            int speed,int projectile,int knock,int jump,int cost,double weight,String effect) {
        return new Move(id,kind,min,range,damage,cd,start,active,recovery,speed,projectile,knock,jump,cost,weight,effect,id.equals("shadow-clone"));
    }
    static String personality(double aggression,double technique,double dodge,double block,double counter,double taunt,
            int speed,int jump,int cooldown,int cost,double reduction) {
        return "<personality aggression=\""+aggression+"\" technique=\""+technique+"\" dodge=\""+dodge+"\" block=\""+block+
            "\" counter=\""+counter+"\" taunt=\""+taunt+"\" dodgeSpeed=\""+speed+"\" jumpHeight=\""+jump+
            "\" defenseCooldown=\""+cooldown+"\" defenseCost=\""+cost+"\" blockReduction=\""+reduction+"\"/>\n";
    }
    static List<Pack> packs=List.of(
        new Pack("NarutoShippuden",personality(.85,.35,.38,.06,.15,0,9,35,22,18,.55),new Color(55,174,255),List.of(
            m("light-combo","melee",0,100,10,22,4,7,7,2,0,22,0,0,1.7,"impact"),
            m("kunai","ranged",100,420,7,35,5,2,8,0,20,10,0,5,1,"projectile"),
            m("shadow-clone","ranged",85,280,12,80,10,3,12,0,14,20,0,20,.6,"energy"),
            m("rasengan","special",65,235,26,120,13,14,17,12,0,65,0,35,1.3,"energy"),
            m("aerial-ninja","aerial",0,160,13,45,6,12,12,6,0,26,35,10,1,"slash"))),
        new Pack("SasukeShippuden",personality(.5,.65,.30,.13,.65,0,12,0,20,20,.65),new Color(149,122,255),List.of(
            m("sword-combo","melee",0,105,12,30,5,7,10,1,0,28,0,0,1.6,"slash"),
            m("shuriken","ranged",95,440,9,38,5,2,10,0,24,16,0,7,1,"projectile"),
            m("fire-style","ranged",110,340,16,80,12,3,14,0,13,34,0,22,.8,"fire"),
            m("chidori","special",65,260,25,125,14,12,17,17,0,58,0,35,1.3,"lightning"),
            m("aerial-sword","aerial",0,155,14,50,7,12,12,5,0,30,24,10,.8,"slash"))),
        new Pack("GojoSatoru",personality(.4,.9,.24,.29,.25,.10,13,0,32,27,.8),new Color(100,196,255),List.of(
            m("fast-melee","melee",0,100,9,19,3,5,6,2,0,18,0,0,1.5,"impact"),
            m("blue-pull","ranged",90,350,11,55,9,2,12,0,16,-48,0,18,1.1,"blue"),
            m("red-blast","ranged",90,360,14,65,11,2,14,0,18,70,0,22,1.1,"red"),
            m("blue-red-special","special",70,390,27,145,18,2,21,0,16,75,0,40,.85,"energy"))),
        new Pack("SukunaRyomen",personality(.95,.55,.18,.08,.7,0,8,0,22,18,.55),new Color(245,83,118),List.of(
            m("melee-strike","melee",0,100,14,27,4,6,10,2,0,34,0,0,1.8,"impact"),
            m("cursed-slash","ranged",90,430,11,38,6,2,10,0,22,24,0,8,1.1,"slash"),
            m("multi-slash","ranged",65,290,17,70,10,5,14,0,16,38,0,20,.9,"slash"),
            m("heavy-cursed","special",55,240,28,130,15,12,20,9,0,70,0,36,1.2,"energy"),
            m("counter-strike","melee",0,105,12,32,3,5,8,3,0,25,0,5,.65,"slash"))),
        new Pack("AstaBlackClover",personality(.8,.25,.24,.30,.4,0,8,28,24,22,.78),new Color(91,68,120),List.of(
            m("sword-slash","melee",0,115,13,30,6,7,11,1,0,32,0,0,1.6,"slash"),
            m("heavy-sword","melee",0,120,20,65,15,8,18,0,0,55,0,17,.8,"impact"),
            m("dash-slash","dash",90,245,15,55,8,14,14,11,0,40,0,15,1.1,"slash"),
            m("jumping-sword","aerial",0,170,16,55,8,14,14,6,0,40,32,12,.9,"slash"),
            m("anti-magic","special",0,180,29,140,18,11,23,5,0,75,0,38,1.1,"energy")))
    );
    static void animation(StringBuilder xml,String id,String... frames) {
        xml.append("    <animation id=\"").append(id).append("\">\n");
        for(String frame:frames) xml.append("      <frame image=\"").append(frame).append(".png\" ticks=\"3\"/>\n");
        xml.append("    </animation>\n");
    }
    public static void main(String[] args) throws Exception {
        for(Pack p:packs) {
            Path root=Path.of("img",p.name);Files.createDirectories(root.resolve("combat/vfx"));
            StringBuilder xml=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<!-- Tick = 40ms. Original VFX; MVP choreography reuses existing pack poses. -->\n"+
                "<combat version=\"1\" width=\"128\" height=\"128\" anchorX=\"64\" anchorY=\"128\" movementSpeed=\""+
                (p.name.startsWith("Naruto")?7:p.name.startsWith("Asta")?5:6)+"\">\n"+
                "  <hurtbox x=\"-23\" y=\"-95\" width=\"46\" height=\"87\"/>\n  "+
                p.personality.replace("/>"," projectileParry=\""+(p.name.startsWith("Asta")?"0.35":"0")+
                    "\" blockEffect=\""+(p.name.startsWith("Gojo")?"blue":"spark")+"\" dodgeEffect=\"dust\"/>")+"  <animations>\n");
            animation(xml,"idle","Stand","Blink","Stand");
            animation(xml,"approach","WalkLeft","WalkPass1","WalkRight","WalkPass2");
            animation(xml,"dodge","JumpStart","NinjaJump","Falling");
            animation(xml,"block","Resist1","Resist2");
            animation(xml,"hit","DragWince","StruggleHard2");
            animation(xml,"knockback","Falling","BounceCompress");
            animation(xml,"recover","JumpStart","Stand");
            animation(xml,"ko","BounceCompress","SitLegsDown","Sit");
            animation(xml,"victory","HandSign","Smile","Smile");
            animation(xml,"charge","HandSign","HandSign","JumpStart");
            animation(xml,"heavy-charge","PullUpPrepare","PullUpEffort","StandWithWindow");
            animation(xml,"melee","StruggleHard1","StruggleHard2","StruggleHard3","StruggleHard4");
            animation(xml,"sword","WalkWithWindow1","ThrowWindow","WalkWithWindow2");
            animation(xml,"ranged","HandSign","ThrowWindow","Stand");
            animation(xml,"dash","JumpStart","NinjaJump","StruggleHard3");
            animation(xml,"aerial","JumpStart","NinjaJump","ThrowWindow","BounceCompress");
            xml.append("  </animations>\n  <effects>\n");
            for(String effect:List.of("slash","impact","dust","energy","projectile","spark","blue","red","fire","lightning")) {
                xml.append("    <effect id=\"").append(effect).append("\" frameTicks=\"2\" cloneImage=\"Stand.png\">\n");
                for(int f=0;f<4;f++) {
                    String image="combat/vfx/"+effect+f+".png";
                    draw(root.resolve(image),effect,f,p);
                    xml.append("      <frame image=\"").append(image).append("\"/>\n");
                }
                xml.append("    </effect>\n");
            }
            xml.append("  </effects>\n  <attacks>\n");
            for(Move m:p.moves) {
                String anim=m.kind.equals("melee")?(p.name.startsWith("Asta")||p.name.startsWith("Sasuke")?"sword":"melee"):
                    m.kind.equals("aerial")?"aerial":m.projectile>0?"ranged":"dash";
                String charge=m.id.equals("anti-magic")||m.id.equals("heavy-sword")?"heavy-charge":"charge";
                xml.append("    <attack id=\"").append(m.id).append("\" kind=\"").append(m.kind)
                    .append("\" minRange=\"").append(m.min).append("\" range=\"").append(m.range)
                    .append("\" damage=\"").append(m.damage).append("\" cooldown=\"").append(m.cooldown)
                    .append("\" startup=\"").append(m.startup).append("\" active=\"").append(m.active)
                    .append("\" recovery=\"").append(m.recovery).append("\" speed=\"").append(m.speed)
                    .append("\" projectileSpeed=\"").append(m.projectile).append("\" knockback=\"").append(m.knock)
                    .append("\" jumpHeight=\"").append(m.jump).append("\" cost=\"").append(m.cost)
                    .append("\" weight=\"").append(m.weight).append("\" animation=\"").append(anim)
                    .append("\" chargeAnimation=\"").append(charge).append("\" effect=\"").append(m.effect)
                    .append("\" clone=\"").append(m.cloneEffect).append("\">\n");
                xml.append(m.projectile>0?"      <hitbox x=\"-12\" y=\"-76\" width=\"24\" height=\"32\"/>\n":
                    "      <hitbox x=\"14\" y=\"-102\" width=\"86\" height=\"100\"/>\n");
                xml.append("    </attack>\n");
            }
            xml.append("  </attacks>\n</combat>\n");
            Files.writeString(root.resolve("conf/combat.xml"),xml.toString());
            Path portable=Path.of("Shimeji-ee","img",p.name);
            Files.copy(root.resolve("conf/combat.xml"),portable.resolve("conf/combat.xml"),StandardCopyOption.REPLACE_EXISTING);
            try(var files=Files.walk(root.resolve("combat"))) {
                for(Path source:files.filter(Files::isRegularFile).toList()) {
                    Path target=portable.resolve(root.relativize(source));Files.createDirectories(target.getParent());
                    Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("Generated "+p.name+": "+p.moves.size()+" moves, 40 transparent VFX frames");
        }
    }
    static void draw(Path path,String effect,int frame,Pack p) throws Exception {
        BufferedImage image=new BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color color=switch(effect) {case "blue"->new Color(44,160,255);case "red"->new Color(255,77,96);
            case "fire"->new Color(255,151,32);case "dust"->new Color(218,194,145);
            case "spark","lightning"->new Color(166,218,255);default->p.color;};
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),Math.max(70,230-frame*40)));
        g.setStroke(new BasicStroke(3+frame,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        int r=16+frame*7;
        switch(effect) {
            case "slash" -> {
                for(int i=0;i<3;i++) g.draw(new Arc2D.Double(12+i*6,14+i*5,65-i*8,68-i*4,110+frame*15,130,Arc2D.OPEN));
            }
            case "impact","spark" -> {
                for(int i=0;i<8;i++) {double a=i*Math.PI/4+frame*.15;g.drawLine(48+(int)(8*Math.cos(a)),48+(int)(8*Math.sin(a)),
                    48+(int)(r*Math.cos(a)),48+(int)(r*Math.sin(a)));}
            }
            case "dust" -> {for(int i=0;i<5;i++)g.fillOval(15+i*12-frame*2,45+(i%2)*8-frame*4,10+frame*3,8+frame*2);}
            case "projectile" -> {
                if(p.name.startsWith("Sasuke")) {
                    Path2D star=new Path2D.Double();for(int i=0;i<8;i++) {double a=i*Math.PI/4+frame*.6;double radius=i%2==0?24:7;
                        double x=48+radius*Math.cos(a),y=48+radius*Math.sin(a);if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}
                    star.closePath();g.fill(star);
                } else {g.fillPolygon(new int[]{18,48,70,48},new int[]{48,40,48,56},4);g.drawOval(70,42,12,12);}
                g.drawLine(60,30+frame*2,90,30+frame*2);
            }
            case "lightning" -> {for(int i=0;i<3;i++)g.drawPolyline(new int[]{80,60,68,36,44,14},
                new int[]{24+i*15,32+i*12,44+i*8,40+i*8,56+i*6,70+i*5},6);}
            case "fire" -> {
                for(int i=0;i<4;i++) {Path2D flame=new Path2D.Double();flame.moveTo(15+i*14,74);flame.curveTo(5+i*14,48,35+i*10,45,25+i*10,15+frame*5);
                    flame.curveTo(65+i*5,47,60+i*7,66,15+i*14,74);g.fill(flame);}
            }
            default -> {
                g.drawOval(48-r,48-r,r*2,r*2);
                for(int i=0;i<3;i++)g.draw(new Arc2D.Double(48-r+i*4,48-r+i*4,r*2-i*8,r*2-i*8,frame*50+i*100,180,Arc2D.OPEN));
                g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),60));g.fillOval(48-r,48-r,r*2,r*2);
            }
        }
        g.dispose();ImageIO.write(image,"png",path.toFile());
    }
}
