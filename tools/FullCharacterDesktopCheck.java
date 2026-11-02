import com.group_finity.mascot.*;import com.group_finity.mascot.action.Action;import com.group_finity.mascot.character.*;import com.group_finity.mascot.combat.*;import com.group_finity.mascot.audio.*;import com.group_finity.mascot.image.TranslucentWindow;import java.awt.*;import java.awt.event.*;import java.lang.reflect.*;import java.nio.file.*;import java.util.*;import java.util.List;import java.util.concurrent.atomic.*;import javax.swing.*;import javax.imageio.ImageIO;
/** Real Windows native images/windows, public CharacterService, menus and CombatEngine. */
public class FullCharacterDesktopCheck {
 static Main app;static Manager manager;static javax.swing.Timer timer;static int phase,step,subject,actions,pairIndex,checks;static Mascot solo,a,b;static Action action;static Field time;static CombatMatch match;static boolean sawTransform,sawTitan;static AtomicInteger audio=new AtomicInteger();
 static List<String> people=List.of("Tom","Jerry","ErenYeager");static String[] actionNames={"Walk","Run","Jumping","Bouncing","Walk"};
 static String[][] pairs={{"Tom","Jerry"},{"Tom","NarutoShippuden"},{"Jerry","SasukeShippuden"},{"ErenYeager","NarutoShippuden"},{"ErenYeager","Tom"}};
 static Path out=Path.of(System.getProperty("integration.output"));
 static void require(boolean yes,String reason){checks++;if(!yes)throw new AssertionError(reason);}
 static Rectangle area(){var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());Insets i=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=i.left;r.y+=i.top;r.width-=i.left+i.right;r.height-=i.top+i.bottom;return r;}
 static void sync(){manager.getMascots().addAll(manager.getAdded());manager.getAdded().clear();manager.getMascots().removeAll(manager.getRemoved());manager.getRemoved().clear();}
 static Mascot get(String id){return manager.getMascots().stream().filter(m->m.getImageSet().equals(id)).findFirst().orElseThrow();}
 static void place(Mascot m,int x)throws Exception{Rectangle r=area();m.setPaused(false);m.setAnchor(new Point(x,r.y+r.height));m.getEnvironment().refreshWorkArea();m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();}
 static void capture(String file,Mascot...pets)throws Exception{Rectangle r=null;for(Mascot m:pets)r=r==null?m.getBounds():r.union(m.getBounds());r.grow(25,35);r=r.intersection(area());ImageIO.write(new Robot().createScreenCapture(r),"png",out.resolve(file+".png").toFile());}
 static void startSolo()throws Exception{for(Window w:Window.getWindows())if(w instanceof com.group_finity.mascot.ui.CharacterManagerWindow)w.setVisible(false);require(new CharacterService().launch(people.get(subject)),"Character Manager service launch failed");sync();require(manager.getCount()==1,"solo launch spawned extras");solo=get(people.get(subject));place(solo,area().x+area().width/2);capture(solo.getImageSet()+"-solo",solo);actions=0;startAction();}
 static void startAction()throws Exception{place(solo,area().x+area().width/2);solo.setLookRight(actions==4);action=app.getConfiguration(solo.getImageSet()).buildAction(actionNames[actions],Map.of());action.init(solo);step=0;}
 static CombatMatch current()throws Exception{Field f=CombatEngine.class.getDeclaredField("pairs");f.setAccessible(true);Object p=((List<?>)f.get(manager.getCombat())).getFirst();var m=p.getClass().getDeclaredMethod("match");m.setAccessible(true);return (CombatMatch)m.invoke(p);}
 static void startPair()throws Exception{String[] pair=pairs[pairIndex];a=get(pair[0]);b=get(pair[1]);place(a,area().x+area().width/2-180);place(b,area().x+area().width/2+180);
  // The actual right-click Combat menu dispatches its normal action listener.
  JMenu menu=manager.getCombat().menu(a,app.getLanguageBundle());JMenu opponents=(JMenu)menu.getItem(0);JMenuItem selected=null;for(int i=0;i<opponents.getItemCount();i++)if(opponents.getItem(i).getText().startsWith(b.getImageSet()+" #"))selected=opponents.getItem(i);require(selected!=null&&selected.isEnabled(),"missing opponent menu");selected.doClick();require(manager.getCombat().active(a)&&manager.getCombat().active(b),"native combat menu failed");match=current();step=0;sawTransform=false;sawTitan=false;
 }
 static void finish()throws Exception{
  require(audio.get()>0,"no successful native audio playback");require(new CharacterCatalog().list().size()==8,"catalog changed");
  // Actual mouse listeners: drag a new character and return to normal behavior.
  Mascot tom=get("Tom");Method win=Mascot.class.getDeclaredMethod("getWindow");win.setAccessible(true);Component c=((TranslucentWindow)win.invoke(tom)).asComponent();Point p=c.getLocationOnScreen();long now=System.currentTimeMillis();
  c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_PRESSED,now,InputEvent.BUTTON1_DOWN_MASK,40,50,p.x+40,p.y+50,1,false,1));c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_DRAGGED,now+30,InputEvent.BUTTON1_DOWN_MASK,70,50,p.x+70,p.y+50,0,false,0));require(tom.isDragging(),"native drag failed");c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_RELEASED,now+60,0,70,50,p.x+70,p.y+50,1,false,1));require(!tom.isDragging(),"native drag release failed");
  JMenu add=app.characterMenu(()->{});require(add.getItemCount()==8,"tray add-character catalog missing");System.out.println("PASS native drag/release, tray Add Character has 8 entries, original audio playbacks="+audio.get());
  for(Frame frame:Frame.getFrames())if("ShinobiMateCharacters".equals(frame.getName())){var image=new java.awt.image.BufferedImage(frame.getWidth(),frame.getHeight(),2);var graphics=image.createGraphics();frame.paint(graphics);graphics.dispose();ImageIO.write(image,"png",out.resolve("character-manager-eight.png").toFile());}
  System.out.println("PASS native Windows integration: "+checks+" checks, real generated bitmaps, 3 solo launches, 5 requested matches");timer.stop();manager.disposeAll();AudioManager.global().close();System.exit(0);
 }
 static void step(){try{
  NativeFactory.getInstance().getEnvironment().tick();
  if(phase==0){if(action.hasNext())action.next();time.setInt(solo,solo.getTime()+1);solo.apply();require(solo.getImage()!=null,"no native bitmap");if(step==10)capture(solo.getImageSet()+"-"+actionNames[actions]+"-"+(actions==4?"right":"left"),solo);if(++step>=26){if(++actions<actionNames.length)startAction();else{System.out.println("PASS native solo "+solo.getImageSet()+": walking/running/jump/landing/left-right");solo.dispose();sync();if(++subject<people.size())startSolo();else{
    for(String id:List.of("Tom","Jerry","ErenYeager","NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover"))require(new CharacterService().launch(id),"8-character launch "+id);sync();int i=0;for(Mascot m:manager.getMascots()){place(m,area().x+75+(i++)*(area().width-150)/7);require(!manager.getCombat().active(m),"automatic combat");}capture("eight-characters",manager.getMascots().toArray(Mascot[]::new));phase=1;startPair();
   }}}return;}
  manager.getCombat().tick();a.apply();b.apply();step++;
  require(area().contains(a.getBounds())&&area().contains(b.getBounds()),"native combat left monitor");
  if(step==75)capture(a.getImageSet()+"-vs-"+b.getImageSet(),a,b);
  for(var f:List.of(match.a,match.b))if(f.baseProfile.transformation()!=null){if(f.state==CombatState.TRANSFORMING&&!sawTransform){sawTransform=true;capture("eren-transforming-"+pairIndex,a,b);}if(f.form==CombatMatch.Form.TITAN&&!sawTitan){sawTitan=true;require(f.body==(f==match.a?match.a:match.b).body,"Titan replaced instance");capture("eren-titan-"+pairIndex,a,b);}}
  if(match.isStopped()){
   require(match.isFinished(),"native fight stopped early");if(a.getImageSet().equals("ErenYeager")){require(sawTransform&&sawTitan,"natural native Eren transformation not seen");require(match.a.form==CombatMatch.Form.HUMAN&&a.getImage().size().height==128,"native Eren not reset");}
   System.out.println("PASS native "+a.getImageSet()+" vs "+b.getImageSet()+": complete KO/victory, bounds, transformation="+sawTitan);
   require(manager.getCombat().start(a,b),"native rematch failed");var rematch=current();require(rematch.a.form==CombatMatch.Form.HUMAN,"rematch not human");manager.getCombat().stop(a);
   if(++pairIndex<pairs.length)startPair();else finish();
  }else require(step<4500,"native match timeout");
 }catch(Throwable e){fail(e);}}
 static void fail(Throwable e){e.printStackTrace();if(timer!=null)timer.stop();if(manager!=null){manager.stop();manager.disposeAll();}System.exit(1);}
 public static void main(String[]args){try{
  if(System.getProperty("integration.report")!=null){var log=new java.io.PrintStream(Files.newOutputStream(Path.of(System.getProperty("integration.report"))),true,java.nio.charset.StandardCharsets.UTF_8);System.setOut(log);System.setErr(log);}
  new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){fail(new AssertionError("native suite exceeded 120s"));}},120000);
  app=Main.getInstance();app.run();javax.swing.Timer ready=new javax.swing.Timer(1400,e->{try{manager=app.getManager();manager.stop();RuntimeDiagnostics.close();manager.setExitOnLastRemoved(false);require(manager.getCount()==0,"new packs launched automatically");require(new CharacterService().installed().size()==8,"manager catalog not 8");time=Mascot.class.getDeclaredField("time");time.setAccessible(true);AudioManager.global().observe(p->audio.incrementAndGet());startSolo();timer=new javax.swing.Timer(7,v->step());timer.start();}catch(Throwable ex){fail(ex);}});ready.setRepeats(false);ready.start();
 }catch(Throwable e){fail(e);}}
}
