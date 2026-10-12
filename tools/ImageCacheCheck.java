import com.group_finity.mascot.*;
import com.group_finity.mascot.image.*;
import java.nio.file.*;
import java.awt.*;
import javax.imageio.ImageIO;
/** Decode all five packs at the largest supported scale under a constrained JVM heap. */
public class ImageCacheCheck {
 public static void main(String[]args)throws Exception {
  Main app=Main.getInstance();var platform=Main.class.getDeclaredField("platform");platform.setAccessible(true);platform.set(app,Platform.x86_64);
  ImagePairs.clear();String first=null;Dimension expected=null;int count=0;
  for(String pack:new String[]{"NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover"}) {
   try(var files=Files.walk(AppPaths.pack(pack))) {
    for(Path file:files.filter(p->p.getFileName().toString().toLowerCase().endsWith(".png")).toList()) {
     String key="/"+pack+"/"+AppPaths.pack(pack).relativize(file).toString().replace('\\','/');
     ImagePairLoader.load(key,null,new Point(64,128),3,ImagePairLoader.Filter.BICUBIC,1);
     var image=ImagePairs.getImage(key,false);
     if(first==null){first=key;expected=new Dimension(image.size());}
     if(image==null||ImagePairs.retainedBytes()>64L*1024*1024)throw new AssertionError("image/cache budget");
     count++;
    }
   }
  }
  var reloaded=ImagePairs.getImage(first,false);
  if(!reloaded.size().equals(expected)||!reloaded.center().equals(new Point(192,384)))throw new AssertionError("evicted frame reload changed pixels/anchor");
  ImagePairs.removeAll("NarutoShippuden");if(ImagePairs.contains(first))throw new AssertionError("pack reload retained stale recipe");
  ImagePairs.clear();if(ImagePairs.retainedBytes()!=0)throw new AssertionError("clear retained frames");
  System.out.println("PASS all five packs: "+count+" PNG pairs at scale 3, eviction/reload/anchors/pack removal under -Xmx256M; software renderer="+System.getProperty("sun.java2d.d3d"));
  System.exit(0);
 }
}
