import com.group_finity.mascot.*;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.combat.*;
import com.group_finity.mascot.image.TranslucentWindow;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Isolated real Main startup + Windows windows, context menu, combat overlay and pinned clouds. */
public class CombatDesktopCheck {
    static Manager manager;
    static Method mascotWindow,tick,cloudRender;
    static Field cloudField,cloudWindow;
    static int assertions;
    static void require(boolean b,String s){assertions++;if(!b)throw new AssertionError(s);}
    static Component component(Mascot m)throws Exception {return ((TranslucentWindow)mascotWindow.invoke(m)).asComponent();}
    static SpiritualCloud cloud(Mascot m)throws Exception{return (SpiritualCloud)cloudField.get(m);}
    static void pulse(int count)throws Exception {
        for(int i=0;i<count;i++) {tick.invoke(manager);SwingUtilities.invokeAndWait(()->{});Thread.sleep(40);}
    }
    static void cloudFollows(Mascot m)throws Exception {
        SwingUtilities.invokeAndWait(()->{try {
            cloudRender.invoke(cloud(m));JWindow w=(JWindow)cloudWindow.get(cloud(m));
            require(w!=null&&w.isShowing(),"pinned cloud disappeared during combat");
            Rectangle pet=component(m).getBounds(),bubble=w.getBounds();
            require(Math.abs(bubble.getCenterX()-pet.getCenterX())<200,"cloud detached from moving fighter");
            require(bubble.y+bubble.height<=pet.y+10 || bubble.y>=pet.y+pet.height-10,"cloud overlaps fighter");
        }catch(Exception e){throw new RuntimeException(e);}});
    }
    static void screenshot(String name)throws Exception {
        if(Boolean.getBoolean("beta.attach"))return;
        var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        var dm=gc.getDevice().getDisplayMode();Rectangle pixels=new Rectangle(gc.getBounds().x,gc.getBounds().y,dm.getWidth(),dm.getHeight());
        ImageIO.write(new Robot().createScreenCapture(pixels),"png",Path.of("..",name).toFile());
    }
    static Rectangle desktopArea() {
        var gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle r=new Rectangle(gc.getBounds());Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
        r.x+=insets.left;r.y+=insets.top;r.width-=insets.left+insets.right;r.height-=insets.top+insets.bottom;return r;
    }
    public static void main(String[]args) {
        if(!Boolean.getBoolean("beta.attach"))new java.util.Timer(true).schedule(new java.util.TimerTask(){public void run(){System.err.println("FAIL desktop timeout");System.exit(2);}},90000);
        try {
            Main app=Main.getInstance();if(!Boolean.getBoolean("beta.attach"))app.run();
            Method getManager=Main.class.getDeclaredMethod("getManager");getManager.setAccessible(true);manager=(Manager)getManager.invoke(app);
            Thread.sleep(600);manager.stop();
            require(manager.getCount()==5,"startup must load all 5 packs");
            mascotWindow=Mascot.class.getDeclaredMethod("getWindow");mascotWindow.setAccessible(true);
            tick=Manager.class.getDeclaredMethod("tick");tick.setAccessible(true);
            cloudField=Mascot.class.getDeclaredField("spiritualCloud");cloudField.setAccessible(true);
            cloudWindow=SpiritualCloud.class.getDeclaredField("window");cloudWindow.setAccessible(true);
            cloudRender=SpiritualCloud.class.getDeclaredMethod("render");cloudRender.setAccessible(true);
            List<Mascot> pets=new ArrayList<>(manager.getMascots());
            for(int i=0;i<pets.size();i++) {
                Mascot m=pets.get(i);Rectangle area=desktopArea();
                m.setAnchor(new Point(area.x+140+i*180,area.y+area.height));m.setBehavior(app.getConfiguration(m.getImageSet()).buildBehavior("StandUp"));m.getBehavior().next();m.apply();
                require(component(m).isShowing(),"native fighter not visible");require(!manager.getCombat().active(m),"startup auto battle");
            }
            Mascot a=pets.get(0),b=pets.get(1);
            SwingUtilities.invokeAndWait(()->{try{cloud(a).click();cloud(b).click();}catch(Exception e){throw new RuntimeException(e);}});
            pulse(6);
            // Dispatch the real right-click listener and inspect the actual popup in its heavyweight host.
            SwingUtilities.invokeAndWait(()->{try {
                Component c=component(a);Point loc=c.getLocationOnScreen();
                c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),0,64,64,loc.x+64,loc.y+64,1,true,3));
            }catch(Exception e){throw new RuntimeException(e);}});
            SwingUtilities.invokeAndWait(()->{});
            JPopupMenu[] actual={null};
            SwingUtilities.invokeAndWait(()->{
                for(MenuElement e:MenuSelectionManager.defaultManager().getSelectedPath()) if(e instanceof JPopupMenu p) actual[0]=p;
                require(actual[0]!=null,"real right-click popup absent");
                JMenu combat=null;for(Component c:actual[0].getComponents())if(c instanceof JMenu m&&(m.getText().contains("القتال")||m.getText().equals("Combat")))combat=m;
                require(combat!=null,"Arabic combat submenu absent");
                JMenu opponents=(JMenu)combat.getItem(0);require(opponents.getItemCount()==4,"native opponents list incorrect");
                actual[0].setVisible(false);MenuSelectionManager.defaultManager().clearSelectedPath();
                opponents.getItem(0).doClick();
            });
            require(manager.getCombat().active(a)&&manager.getCombat().active(b),"real popup did not start pair");
            boolean overlay=false,captured=false;
            for(int i=0;i<110;i++) {
                pulse(1);
                require(desktopArea().contains(component(a).getBounds())&&desktopArea().contains(component(b).getBounds()),"native fighters left logical work area");
                if(i%10==0) {cloudFollows(a);cloudFollows(b);}
                for(java.awt.Window w:java.awt.Window.getWindows())if("ShinobiMateCombatEffects".equals(w.getName())&&w.isVisible()) {
                    overlay=true;
                    require(!w.isFocusableWindow(),"VFX steals focus");
                    var handle=com.sun.jna.Native.getComponentPointer(w);
                    int style=com.group_finity.mascot.win.jna.User32.INSTANCE.GetWindowLongW(handle,-20);
                    require((style&0x20)!=0,"VFX intercepts mouse");
                    if(!captured&&i>45&&i<90) {screenshot("combat-desktop.png");captured=true;}
                }
                for(Mascot m:pets.subList(2,pets.size()))require(!manager.getCombat().active(m),"native bystander entered fight");
                if(i==65) {
                    for(Mascot m:List.of(a,b)) {
                        com.group_finity.mascot.win.jna.RECT rect=new com.group_finity.mascot.win.jna.RECT();
                        var handle=com.sun.jna.Native.getComponentPointer(component(m));
                        com.group_finity.mascot.win.jna.User32.INSTANCE.GetWindowRect(handle,rect);
                        System.out.println("RENDER "+m.getImageSet()+" Swing="+component(m).getBounds()+" native="+rect.left+","+rect.top+","+rect.right+","+rect.bottom+" image="+m.getImage().size()+" area="+desktopArea());
                    }
                }
            }
            require(overlay,"no transparent native VFX rendered");
            manager.getCombat().stop(b);SwingUtilities.invokeAndWait(()->{});
            require(!manager.getCombat().active(a),"native stop failed");
            for(java.awt.Window w:java.awt.Window.getWindows())if("ShinobiMateCombatEffects".equals(w.getName()))require(!w.isDisplayable(),"VFX window leaked after stop");
            System.out.println("PASS native Windows: 5-pack startup, Arabic right-click popup, explicit pair, transparent click-through VFX, pinned clouds follow both fighters, bystander isolation, stop/disposal; "+assertions+" assertions");
            // Run the other two requested pairs through native rendering as well.
            for(int[] pair:List.of(new int[]{2,3},new int[]{4,0})) {
                require(manager.getCombat().start(pets.get(pair[0]),pets.get(pair[1])),"additional native pair start");pulse(65);
                manager.getCombat().stop(pets.get(pair[0]));SwingUtilities.invokeAndWait(()->{});
                System.out.println("PASS native "+pets.get(pair[0]).getImageSet()+" vs "+pets.get(pair[1]).getImageSet());
            }
            if(Boolean.getBoolean("beta.attach")){
                for(Mascot m:pets){
                    SwingUtilities.invokeAndWait(()->{try{
                        Component c=component(m);Point p=c.getLocationOnScreen();long now=System.currentTimeMillis();
                        c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_PRESSED,now,InputEvent.BUTTON1_DOWN_MASK,64,64,p.x+64,p.y+64,1,false,1));
                        c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_DRAGGED,now+30,InputEvent.BUTTON1_DOWN_MASK,100,80,p.x+100,p.y+80,0,false,0));
                        require(m.isDragging(),"installed native press/movement did not enter drag behavior");
                        c.dispatchEvent(new MouseEvent(c,MouseEvent.MOUSE_RELEASED,now+60,0,100,80,p.x+100,p.y+80,1,false,1));
                    }catch(Exception e){throw new RuntimeException(e);}});
                    require(!m.isDragging(),"installed native release did not end drag behavior");
                    require(!manager.getCombat().active(m),"drag started combat");
                }
                System.out.println("PASS installed native left drag/release for all five characters");
            }
            manager.disposeAll();SwingUtilities.invokeAndWait(()->{});
            if(Boolean.getBoolean("beta.attach"))manager.start();else System.exit(0);
        }catch(Throwable e){e.printStackTrace();if(manager!=null){manager.stop();manager.disposeAll();}if(Boolean.getBoolean("beta.attach"))throw new RuntimeException(e);else System.exit(1);}
    }
}
