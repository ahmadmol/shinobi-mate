import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Atlas slicing with ONE calibration scale per character, never fitting poses independently. */
public class IntegrateLocomotionArt {
 static final Path OUT=Path.of("output/locomotion-20261016");
 static Rectangle bounds(BufferedImage image) {
  int l=image.getWidth(),r=-1,t=image.getHeight(),b=-1;
  for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>100){l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);}
  if(r<0)throw new IllegalStateException("empty atlas cell");
  return new Rectangle(l,t,r-l+1,b-t+1);
 }
 public static void main(String[] args)throws Exception {
  for(String id:java.util.List.of("ErenYeager","Tom","Jerry")) {
   int size=id.equals("Jerry")?96:128;
   String motion=id.equals("ErenYeager")?"ErenHumanMotion":id+"Motion";
   BufferedImage ref=ImageIO.read(Path.of("img",id,"art",motion,"00.png").toFile());
   BufferedImage atlas=ImageIO.read(OUT.resolve("source/"+id+".png").toFile());
   BufferedImage[] cells=new BufferedImage[8];Rectangle[] boxes=new Rectangle[8];
   // Components may extend a few pixels beyond the nominal grid. Extract the
   // whole connected drawing so a neighbor's tail/hand never leaks into a frame.
   var parts=IntegrateCharacterArt.order(IntegrateCharacterArt.parts(atlas,8),2,4,atlas.getHeight());
   if(parts.size()!=8)throw new IllegalStateException("Expected eight separate full-body poses: "+id);
   for(int i=0;i<8;i++){cells[i]=IntegrateCharacterArt.isolate(atlas,parts.get(i).seed());boxes[i]=bounds(cells[i]);}
   double scale=(double)bounds(ref).height/boxes[0].height;
   Path folder=Path.of("img",id,"art","locomotion-v2");Files.createDirectories(folder);
   StringBuilder audit=new StringBuilder("character="+id+" referenceBodyHeight="+bounds(ref).height+" sharedScale="+scale+"\n");
   for(int i=0;i<8;i++) {
    Rectangle b=boxes[i];
    if(b.width*scale>size-10||b.height*scale>size-4)throw new IllegalStateException("Atlas pose exceeds shared canvas: "+id+"/"+i);
    BufferedImage packed=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);Graphics2D g=packed.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    double x=i>=2&&i<=4?8-b.x*scale:size/2.0-b.getCenterX()*scale;
    double y=i>=5?2-b.y*scale:size-2-b.getMaxY()*scale;
    g.translate(x,y);g.scale(scale,scale);g.drawImage(cells[i],0,0,null);g.dispose();
    ImageIO.write(packed,"png",folder.resolve(String.format("%02d.png",i)).toFile());
    audit.append(i+": "+b+" -> "+bounds(packed)+"\n");
   }
   Files.writeString(OUT.resolve(id+"-atlas-audit.txt"),audit);
   System.out.print(audit);
  }
 }
}
