import com.group_finity.mascot.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.audio.*;
import com.group_finity.mascot.image.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Isolated manual visual/audio probe using the actual native MascotBody and CombatEffects adapter. */
public class AudioVisualQA extends CombatPolishCheck {
 static Main app;static Manager manager;static Mascot naruto,sasuke;static CombatMatch match;static CombatEffects effects;
 static JLabel status=new JLabel();static JPanel preview;static boolean paused;static String demo="rasengan";
 static Map<String,BufferedImage> fx=new HashMap<>();static java.io.PrintWriter timeline;static int frame;static long started;
 static final Path out=Path.of("output/audio-phase1/visual");static Set<String> captured=new HashSet<>();
 static Rectangle arena(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();var r=new Rectangle(gc.getBounds());var in=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=in.left;r.y+=in.top;r.width-=in.left+in.right;r.height-=in.top+in.bottom;return r;}
 static CombatMatch.Body body(Mascot pet,CombatProfile profile)throws Exception {
  var type=Class.forName("com.group_finity.mascot.combat.CombatEngine$MascotBody");var ctor=type.getDeclaredConstructor(Mascot.class,CombatProfile.class);ctor.setAccessible(true);return (CombatMatch.Body)ctor.newInstance(pet,profile);
 }
 static void start(String attack)throws Exception {
  if(match!=null)match.stop();if(effects!=null)effects.close();
  demo=attack;paused=false;var r=arena();naruto.setAnchor(new Point(r.x+300,(int)r.getMaxY()));sasuke.setAnchor(new Point(r.x+395,(int)r.getMaxY()));
  var n=profile("NarutoShippuden");var s=profile("SasukeShippuden");
  var source=attack.equals("rasengan")||attack.equals("kunai")||attack.equals("shuriken")?n:s;
  var selected=source.attacks().stream().filter(a->a.id().equals(attack)).findFirst().orElseThrow();
  var selectedProfile=fixture(source,List.of(new CombatProfile.Attack(selected.id(),selected.kind(),0,selected.range(),selected.hitbox(),selected.damage(),1000,
    selected.startup(),selected.active(),selected.recovery(),selected.speed(),selected.projectileSpeed(),selected.knockback(),selected.jumpHeight(),0,1,
    selected.animation(),selected.chargeAnimation(),selected.effect(),selected.cloneEffect(),selected.recoveryAnimation(),selected.chargeEffect(),selected.polish())));
  boolean nAttack=source==n;var pn=nAttack?selectedProfile:idle(n);var ps=nAttack?idle(s):selectedProfile;
  if(selected.projectile()){naruto.setAnchor(new Point(r.x+250,(int)r.getMaxY()));sasuke.setAnchor(new Point(r.x+430,(int)r.getMaxY()));}
  match=new CombatMatch(body(naruto,pn),pn,body(sasuke,ps),ps,new Random(1));effects=new CombatEffects(pn,ps,r);
  for(var p:List.of(pn,ps))for(var effect:p.effects().values())for(String path:effect.frames())fx.put(p.root().resolve(path).toString(),ImageIO.read(p.root().resolve(path).toFile()));
  frame=0;started=System.nanoTime();timeline.println("DEMO,"+attack);timeline.flush();
 }
 static BufferedImage bitmap(Mascot pet)throws Exception {var image=pet.getImage();if(image==null)return null;var field=image.image().getClass().getDeclaredField("managedImage");field.setAccessible(true);return (BufferedImage)field.get(image.image());}
 static void paintOwned(Graphics2D g,int width,int height) {
  g.setColor(new Color(24,29,44));g.fillRect(0,0,width,height);if(match==null)return;
  double originX=Math.min(match.a.x,match.b.x)-120,originY=match.arena().getMaxY()-160;double scale=1.5;
  g.scale(scale,scale);g.translate(-originX,-originY);
  try {for(Mascot pet:List.of(naruto,sasuke)){var im=bitmap(pet);var b=pet.getBounds();if(im!=null)g.drawImage(im,b.x,b.y,b.width,b.height,null);}
   for(var v:match.visuals()){var effect=v.profile.effects().get(v.effect);var path=effect.frames().get(v.age/effect.frameTicks()%effect.frames().size());var im=fx.get(v.profile.root().resolve(path).toString());if(im!=null)g.drawImage(im,(int)(v.x-im.getWidth()/2.0),(int)(v.y-im.getHeight()/2.0),null);}
  }catch(Exception ex){throw new RuntimeException(ex);}
 }
 static void capture(String stage)throws Exception {
  String name=demo+"-"+stage;if(!captured.add(name))return;
  BufferedImage image=new BufferedImage(820,270,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();paintOwned(g,820,235);g.setTransform(new java.awt.geom.AffineTransform());g.setColor(Color.WHITE);g.setFont(new Font("Segoe UI",Font.PLAIN,14));g.drawString(status.getText(),10,250);g.dispose();ImageIO.write(image,"png",out.resolve(name+".png").toFile());
 }
 static void tick()throws Exception {
  if(paused)return;
  int hp=match.a.hp+match.b.hp;match.tick();frame++;effects.update(match);naruto.apply();sasuke.apply();
  var source=demo.equals("rasengan")||demo.equals("kunai")||demo.equals("shuriken")?match.a:match.b;
  status.setText(demo+" | frame "+frame+" | "+source.state+" age "+source.attackAge()+" | HP "+match.a.hp+" / "+match.b.hp+" | voices "+AudioManager.global().snapshot().voices());
  timeline.println(frame+","+source.state+","+source.attackAge()+","+match.a.hp+","+match.b.hp+","+AudioManager.global().snapshot().voices()+","+AudioManager.global().snapshot().loops());timeline.flush();
  if(source.attack!=null&&source.attackAge()==Math.max(2,source.attack.startup()/2))capture("charge");
  if(source.attack!=null&&source.attackAge()==source.attack.startup()+1)capture("active");
  if(match.a.hp+match.b.hp<hp)capture("impact");preview.repaint();
  if(frame>=120)start(demo);
 }
 public static void main(String[] args){try {
  Files.createDirectories(out);timeline=new java.io.PrintWriter(Files.newBufferedWriter(out.resolve("timeline.csv")));
  app=Main.getInstance();app.run();manager=app.getManager();manager.setExitOnLastRemoved(false);Thread.sleep(800);manager.stop();
  for(String id:List.of("NarutoShippuden","SasukeShippuden"))if(new com.group_finity.mascot.character.CharacterService().count(id)==0){if(!app.prepareCharacter(id))throw new AssertionError("QA pack unavailable "+id);app.createMascot(id);}
  manager.start();Thread.sleep(150);manager.stop();
  for(var pet:manager.getMascots()){if(pet.getImageSet().equals("NarutoShippuden"))naruto=pet;if(pet.getImageSet().equals("SasukeShippuden"))sasuke=pet;}
  if(naruto==null||sasuke==null)throw new AssertionError("Visual QA needs Naruto + Sasuke");
  AudioManager.global().observe(e->{synchronized(timeline){timeline.println("AUDIO,"+e.id()+","+e.nanos()+","+e.thread());timeline.flush();}});
  SwingUtilities.invokeAndWait(()->{try {
   JFrame window=new JFrame("ShinobiMate Audio Phase 1 — Visual QA");window.setName("AudioPhase1VisualQA");window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
   window.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){if(match!=null)match.stop();if(effects!=null)effects.close();AudioManager.global().close();timeline.close();manager.disposeAll();System.exit(0);}});
   JPanel root=new JPanel(new BorderLayout(10,10));JPanel controls=new JPanel(new FlowLayout());
   for(String id:List.of("rasengan","chidori","sword-combo","kunai","shuriken","fire-style")){JButton button=new JButton(id);button.addActionListener(e->{try{start(id);}catch(Exception ex){ex.printStackTrace();}});controls.add(button);}
   JButton pause=new JButton("Pause / resume");pause.addActionListener(e->{paused=!paused;if(paused){match.a.body.stopChargeSounds();match.b.body.stopChargeSounds();}});controls.add(pause);
   JButton settings=new JButton("Audio settings");settings.addActionListener(e->{paused=true;match.a.body.stopChargeSounds();match.b.body.stopChargeSounds();new com.group_finity.mascot.ui.SettingsDialog(window).setVisible(true);});controls.add(settings);
   preview=new JPanel(){protected void paintComponent(Graphics graphics){super.paintComponent(graphics);var g=(Graphics2D)graphics.create();paintOwned(g,getWidth(),getHeight());g.dispose();}};preview.setPreferredSize(new Dimension(820,270));
   root.add(controls,BorderLayout.NORTH);root.add(preview,BorderLayout.CENTER);root.add(status,BorderLayout.SOUTH);window.setContentPane(root);window.pack();window.setLocation(120,80);window.setVisible(true);
   start("rasengan");new javax.swing.Timer(40,e->{try{tick();}catch(Exception ex){ex.printStackTrace();System.exit(1);}}).start();
  }catch(Exception ex){throw new RuntimeException(ex);}});
 }catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
}
