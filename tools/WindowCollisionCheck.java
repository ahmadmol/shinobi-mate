import com.group_finity.mascot.*;import java.awt.*;import javax.swing.*;import java.nio.file.*;
/** A separate native window fixture; no user windows are moved or changed. */
public class WindowCollisionCheck {
 public static void main(String[]args)throws Exception{
  if(args.length>0){
   SwingUtilities.invokeAndWait(()->{var f=new JFrame("ShinobiMate collision fixture");f.setBounds(Integer.parseInt(args[1]),180,500,300);f.setAlwaysOnTop(true);f.setVisible(true);try{var b=f.getBounds();Files.writeString(Path.of(args[0]),b.x+","+b.y+","+b.width+","+b.height);}catch(Exception e){throw new RuntimeException(e);}new javax.swing.Timer(15000,e->System.exit(0)).start();});return;
  }
  try{
   Main.getInstance().run();var manager=Main.getInstance().getManager();manager.stop();RuntimeDiagnostics.close();var env=NativeFactory.getInstance().getEnvironment();
   for(var device:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()){
    Rectangle screen=device.getDefaultConfiguration().getBounds();Path file=Path.of(System.getProperty("app.userDataDir"),"window-"+screen.x+".txt");Files.deleteIfExists(file);
    var process=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin/java.exe").toString(),"-cp",System.getProperty("java.class.path"),"WindowCollisionCheck",file.toString(),""+(screen.x+160)).start();
    try{
     for(int i=0;i<150&&!Files.exists(file);i++)Thread.sleep(50);if(!Files.exists(file))throw new AssertionError("fixture did not open");Thread.sleep(400);env.refreshCache();env.tick();
     int[] v=java.util.Arrays.stream(Files.readString(file).split(",")).mapToInt(Integer::parseInt).toArray();Rectangle expected=new Rectangle(v[0],v[1],v[2],v[3]);Rectangle actual=env.getActiveIE().toRectangle();
     if(!env.getActiveIE().isVisible()||!expected.equals(actual))throw new AssertionError("window collision coordinates expected="+expected+" actual="+actual+" title="+env.getActiveIETitle());
     System.out.println("PASS native external window default discovery + logical collision rectangle "+actual);
     manager.getMascots().addAll(manager.getAdded());manager.getAdded().clear();
     var time=Mascot.class.getDeclaredField("time");time.setAccessible(true);
     for(var pet:java.util.List.copyOf(manager.getMascots())){
      pet.setAnchor(new Point(actual.x+actual.width/2,actual.y-70));pet.getEnvironment().refreshWorkArea();
      var fall=Main.getInstance().getConfiguration(pet.getImageSet()).buildAction("Fall",java.util.Map.of());fall.init(pet);
      for(int n=0;n<70&&fall.hasNext();n++){env.tick();fall.next();time.setInt(pet,pet.getTime()+1);pet.apply();}
      if(pet.getAnchor().y!=actual.y)throw new AssertionError("missed window landing "+pet.getImageSet()+pet.getAnchor());
      pet.setLookRight(true);pet.setAnchor(new Point(actual.x,actual.y+actual.height-50));
      var climb=Main.getInstance().getConfiguration(pet.getImageSet()).buildAction("ClimbWall",java.util.Map.of("TargetY",""+(actual.y+50)));climb.init(pet);int start=pet.getAnchor().y;
      for(int n=0;n<24&&climb.hasNext();n++){env.tick();climb.next();time.setInt(pet,pet.getTime()+1);pet.apply();}
      if(pet.getAnchor().y>=start)throw new AssertionError("window climb failed "+pet.getImageSet());
     }
     System.out.println("PASS eight packs landed on external window and climbed its side");
    }finally{process.destroy();process.waitFor();}
   }
   manager.disposeAll();System.out.println("PASS external window collision on both monitors");System.exit(0);
  }catch(Throwable e){e.printStackTrace();System.exit(1);}
 }
}
