import java.awt.*;import java.awt.image.*;import java.nio.file.*;import java.util.*;import javax.imageio.ImageIO;
public class CharacterArtContact {
 public static void main(String[] a)throws Exception{
  var files=new ArrayList<Path>();for(String folder:a)try(var s=Files.list(Path.of(folder))){s.filter(p->p.toString().endsWith(".png")).sorted().forEach(files::add);}
  int w=8*144,h=((files.size()+7)/8)*162;BufferedImage im=new BufferedImage(w,h,2);Graphics2D g=im.createGraphics();g.setColor(new Color(230,235,240));g.fillRect(0,0,w,h);g.setFont(new Font("SansSerif",0,11));int i=0;for(Path p:files){var art=ImageIO.read(p.toFile());int x=i%8*144,y=i/8*162;double sc=Math.min(128.0/art.getWidth(),128.0/art.getHeight());g.drawImage(art,x+(144-(int)(art.getWidth()*sc))/2,y, (int)(art.getWidth()*sc),(int)(art.getHeight()*sc),null);g.setColor(Color.DARK_GRAY);g.drawString(p.getParent().getFileName()+"/"+p.getFileName(),x+2,y+146);i++;}g.dispose();ImageIO.write(im,"png",Path.of("output/full-character-integration/contact.png").toFile());
 }
}
