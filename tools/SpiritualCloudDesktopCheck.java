import com.group_finity.mascot.*;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Real Main startup, native mascot windows and Swing clouds. Input events use production listeners. */
public class SpiritualCloudDesktopCheck {
    static Main app;
    static Manager manager;
    static Field cloudField, stateField, windowField, timeField;
    static Method mascotWindow;
    static int assertions;
    static final Path LOG=Path.of("..","spiritual-cloud-desktop-check.log");
    static void log(String value) throws Exception {
        System.out.println(value);
        Files.writeString(LOG,value+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }
    static void require(boolean value,String reason) {assertions++;if(!value)throw new AssertionError(reason);}
    static SpiritualCloud cloud(Mascot mascot) throws Exception {return (SpiritualCloud)cloudField.get(mascot);}
    static CloudState state(Mascot mascot) throws Exception {return (CloudState)stateField.get(cloud(mascot));}
    static JWindow window(Mascot mascot) throws Exception {return (JWindow)windowField.get(cloud(mascot));}
    static Component component(Mascot mascot) throws Exception {return ((TranslucentWindow)mascotWindow.invoke(mascot)).asComponent();}
    static void event(Mascot mascot,int id,int button,int dx,int dy,boolean popup) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                Component c=component(mascot);Point p=c.getLocationOnScreen();
                int mask=button==1||id==MouseEvent.MOUSE_DRAGGED?MouseEvent.BUTTON1_DOWN_MASK:0;
                c.dispatchEvent(new MouseEvent(c,id,System.currentTimeMillis(),mask,64+dx,60+dy,
                        p.x+64+dx,p.y+60+dy,1,popup,button));
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
    static void click(Mascot mascot) throws Exception {
        event(mascot,MouseEvent.MOUSE_PRESSED,1,0,0,false);
        event(mascot,MouseEvent.MOUSE_RELEASED,1,0,0,false);
    }
    static void pulse(long millis) throws Exception {
        long until=System.nanoTime()/1_000_000+millis;
        do {
            synchronized(manager.getMascots()) {for(Mascot mascot:manager.getMascots())mascot.apply();}
            SwingUtilities.invokeAndWait(()->{});
            Thread.sleep(40);
        }while(System.nanoTime()/1_000_000<until);
    }
    static void follows(Mascot mascot) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                JWindow w=window(mascot);require(w!=null&&w.isVisible(),"cloud native visibility");
                Component c=component(mascot);
                Rectangle actual=w.getBounds(), m=new Rectangle(c.getLocationOnScreen(),c.getSize());
                List<Rectangle> monitors=new ArrayList<>();
                for(GraphicsDevice d:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    GraphicsConfiguration gc=d.getDefaultConfiguration();Rectangle r=new Rectangle(gc.getBounds());
                    Insets i=Toolkit.getDefaultToolkit().getScreenInsets(gc);r.x+=i.left;r.y+=i.top;r.width-=i.left+i.right;r.height-=i.top+i.bottom;monitors.add(r);
                }
                Rectangle monitor=CloudPlacement.monitorFor(m,monitors);
                require(monitor.contains(actual),"native cloud outside monitor");
                require(actual.equals(CloudPlacement.place(m,w.getContentPane().getPreferredSize(),monitor)),"native cloud did not follow mascot");
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
    static void runAction(Mascot mascot,String name,Point anchor,Map<String,String> params,int frames) throws Exception {
        mascot.setAnchor(anchor);mascot.getEnvironment().refreshWorkArea();
        if(name.contains("Wall"))mascot.setLookRight(false);
        Action action=app.getConfiguration(mascot.getImageSet()).buildAction(name,params);action.init(mascot);
        for(int i=0;i<frames&&action.hasNext();i++) {
            action.next();timeField.setInt(mascot,mascot.getTime()+1);mascot.apply();
            SwingUtilities.invokeAndWait(()->{});follows(mascot);
        }
    }
    public static void main(String[] args) {
        new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.exit(3);}},180_000);
        try {
            Files.deleteIfExists(LOG);log("START native desktop check; Java "+Runtime.version());
            app=Main.getInstance();app.run();
            Method getManager=Main.class.getDeclaredMethod("getManager");getManager.setAccessible(true);manager=(Manager)getManager.invoke(app);
            Thread.sleep(1800);manager.stop();manager.setExitOnLastRemoved(false);
            cloudField=Mascot.class.getDeclaredField("spiritualCloud");cloudField.setAccessible(true);
            stateField=SpiritualCloud.class.getDeclaredField("state");stateField.setAccessible(true);
            windowField=SpiritualCloud.class.getDeclaredField("window");windowField.setAccessible(true);
            timeField=Mascot.class.getDeclaredField("time");timeField.setAccessible(true);
            mascotWindow=Mascot.class.getDeclaredMethod("getWindow");mascotWindow.setAccessible(true);
            List<Mascot> mascots=new ArrayList<>(manager.getMascots());require(mascots.size()==5,"five startup characters");
            int n=0;Rectangle screen=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            for(Mascot m:mascots) {
                m.setAnchor(new Point(screen.x+200+n*300,screen.y+500));
                m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();n++;
                require(component(m).getClass().getName().contains("WindowsTranslucentWindow"),"real native mascot");
                event(m,MouseEvent.MOUSE_ENTERED,0,0,0,false);pulse(250);
                require(state(m).visibility()==CloudState.Visibility.HOVER_VISIBLE&&window(m).isVisible(),"hover show");
                event(m,MouseEvent.MOUSE_EXITED,0,0,0,false);pulse(250);
                require(!window(m).isVisible(),"exit hide");
                event(m,MouseEvent.MOUSE_ENTERED,0,0,0,false);click(m);pulse(250);
                event(m,MouseEvent.MOUSE_EXITED,0,0,0,false);pulse(250);
                require(state(m).visibility()==CloudState.Visibility.PINNED&&window(m).isVisible(),"pinned survives exit");
                JWindow reused=window(m);click(m);pulse(250);require(!window(m).isVisible(),"second click hide");
                click(m);pulse(250);require(window(m)==reused,"reused cloud window");
                event(m,MouseEvent.MOUSE_PRESSED,1,0,0,false);
                event(m,MouseEvent.MOUSE_DRAGGED,0,24,0,false);
                require(m.isDragging(),"drag begins after threshold");
                event(m,MouseEvent.MOUSE_RELEASED,1,24,0,false);
                require(!m.isDragging()&&state(m).visibility()==CloudState.Visibility.PINNED,"drag does not toggle pinned state");
                event(m,MouseEvent.MOUSE_RELEASED,3,0,0,true);SwingUtilities.invokeAndWait(()->{});
                SwingUtilities.invokeAndWait(()->{
                    MenuElement[] path=MenuSelectionManager.defaultManager().getSelectedPath();require(path.length>0,"right click menu");
                    JPopupMenu popup=(JPopupMenu)path[0];String label=((JMenuItem)popup.getComponent(0)).getText();
                    require(label.codePoints().anyMatch(c->c>=0x600&&c<=0x6ff),"Arabic menu preserved");
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                });
                follows(m);log("PASS "+m.getImageSet()+": native hover/exit, pin/unpin, drag threshold, reused window, Arabic right-click menu");
            }
            List<String> old=new ArrayList<>();for(Mascot m:mascots)old.add(state(m).message());pulse(6500);
            for(int i=0;i<mascots.size();i++)require(!state(mascots.get(i)).message().equals(old.get(i)),"native 6s text change");
            Mascot first=mascots.getFirst(),second=mascots.get(1);String independent=state(second).message();
            click(first);pulse(250);require(state(second).visibility()==CloudState.Visibility.PINNED&&window(second).isVisible()&&state(second).message().equals(independent),"independent packs");click(first);pulse(250);
            Mascot clone=new Mascot(first.getImageSet());clone.setManager(manager);clone.setAnchor(new Point(600,650));clone.setImage(first.getImage());clone.setBehavior(app.getConfiguration(clone.getImageSet()).buildBehavior("StandUp"));clone.getBehavior().next();manager.getMascots().add(clone);clone.apply();pulse(100);
            require(cloud(clone)!=cloud(first),"independent duplicate instance");event(clone,MouseEvent.MOUSE_ENTERED,0,0,0,false);pulse(250);click(clone);pulse(250);
            click(first);pulse(250);require(state(clone).visibility()==CloudState.Visibility.PINNED&&window(clone).isVisible(),"same-pack pin independence");click(first);pulse(250);clone.dispose();manager.getMascots().remove(clone);
            int floor=first.getEnvironment().getWorkArea().getBottom(),top=first.getEnvironment().getWorkArea().getTop(),left=first.getEnvironment().getWorkArea().getLeft();
            for(Mascot m:mascots) {
                for(String action:List.of("Walk","Run","NinjaJump","Fall","Sit","ClimbAlongWall","GrabCeiling","ClimbCeiling")) {
                    Point anchor=action.contains("Ceiling")?new Point(700,top):action.contains("Wall")?new Point(left,floor-150):action.equals("Fall")?new Point(700,floor-200):new Point(700,floor);
                    runAction(m,action,anchor,Map.of("TargetX","740","Duration","12"),8);
                }
                m.setAnchor(new Point(0,top+20));m.apply();pulse(100);follows(m);
                m.setAnchor(new Point(screen.x+screen.width-60,floor));m.apply();pulse(100);follows(m);
                m.setAnchor(new Point(-40,floor));m.apply();pulse(100);follows(m);
                m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("ChaseMouse"));m.getBehavior().next();m.apply();pulse(100);follows(m);
                log("PASS "+m.getImageSet()+": cloud follows walk/run/jump/fall/sit/wall/ceiling; edge clamping; Follow Cursor");
            }
            Method reload=Main.class.getDeclaredMethod("loadConfiguration",String.class);reload.setAccessible(true);
            for(GraphicsDevice device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                Rectangle bounds=device.getDefaultConfiguration().getBounds();
                for(Point point:List.of(new Point(bounds.x+20,bounds.y+20),new Point(bounds.x+bounds.width-20,bounds.y+20),
                        new Point(bounds.x+20,bounds.y+bounds.height-80),new Point(bounds.x+bounds.width-20,bounds.y+bounds.height-80))) {
                    first.setAnchor(point);first.apply();pulse(100);follows(first);
                }
            }
            log("PASS actual cloud placement at four edges of each connected monitor");
            require((boolean)reload.invoke(app,first.getImageSet()),"reload configuration");SwingUtilities.invokeAndWait(()->{});
            require(cloud(first).isEnabled()&&state(first).visibility()==CloudState.Visibility.HIDDEN,"reload resets cloud safely");
            for(int i=0;i<mascots.size();i++) {
                Mascot m=mascots.get(i);m.setAnchor(new Point(screen.x+200+i*300,screen.y+520));m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();
                if(state(m).visibility()!=CloudState.Visibility.PINNED)click(m);
            }
            pulse(250);log("PASS "+assertions+" native desktop assertions; monitor count="+GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length);
            if(args.length>0&&args[0].equals("--preview")) {
                SwingUtilities.invokeAndWait(()->{
                    JFrame status=new JFrame("ShinobiMate Spiritual Cloud — native desktop check passed");
                    JLabel text=new JLabel("<html><div style='text-align:center'>Native desktop checks passed<br>Five independent mascot clouds are visible.<br>Hover and click the characters to inspect them.</div></html>",SwingConstants.CENTER);
                    status.add(text);status.setSize(650,150);status.setLocation(450,140);status.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);status.setVisible(true);
                });
                while(true)pulse(40);
            }
            List<JWindow> windows=new ArrayList<>();for(Mascot m:mascots)windows.add(window(m));
            manager.disposeAll();SwingUtilities.invokeAndWait(()->{});
            for(JWindow w:windows)require(!w.isDisplayable(),"cloud disposed with mascot");
            log("PASS native disposal; "+assertions+" total assertions");System.exit(0);
        }catch(Throwable error){error.printStackTrace();try{log("FAIL "+error);}catch(Exception ignored){}System.exit(1);}
    }
}
