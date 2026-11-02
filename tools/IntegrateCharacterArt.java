import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Production atlas unpacking/alignment using Java2D, never primitive character drawing. */
public class IntegrateCharacterArt {
 static final Path ROOT=Path.of("output/full-character-integration");
 record Part(Rectangle box,int pixels,double footX,int floor,int seed) { }
 static BufferedImage alpha(BufferedImage src) {
  if(src.getColorModel().hasAlpha())return src;
  int w=src.getWidth(),h=src.getHeight();boolean[] bg=new boolean[w*h];int[] queue=new int[w*h];int head=0,tail=0;
  // The supplied Eren sheet has an almost neutral dark backdrop. Flood only the
  // background connected to an edge; enclosed coat shading is retained.
  for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(x==0||y==0||x==w-1||y==h-1){int p=y*w+x;if(dark(src.getRGB(x,y))&&!bg[p]){bg[p]=true;queue[tail++]=p;}}
  while(head<tail){int p=queue[head++],x=p%w,y=p/w;for(int q:new int[]{x>0?p-1:-1,x<w-1?p+1:-1,y>0?p-w:-1,y<h-1?p+w:-1})if(q>=0&&!bg[q]&&dark(src.getRGB(q%w,q/w))){bg[q]=true;queue[tail++]=q;}}
  BufferedImage out=new BufferedImage(w,h,2);for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(!bg[y*w+x])out.setRGB(x,y,src.getRGB(x,y)|0xff000000);return out;
 }
 static boolean dark(int c){int r=c>>16&255,g=c>>8&255,b=c&255;int max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));return max>=12&&max<43&&max-min<7;}
 static List<Part> parts(BufferedImage src,int expected) {
  int w=src.getWidth(),h=src.getHeight();boolean[] seen=new boolean[w*h];int[] queue=new int[w*h];List<Part> parts=new ArrayList<>();
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){
   int p=y*w+x;if(seen[p]||(src.getRGB(x,y)>>>24)<100)continue;int head=0,tail=0,l=x,r=x,t=y,b=y;seen[p]=true;queue[tail++]=p;
   while(head<tail){int v=queue[head++],vx=v%w,vy=v/w;l=Math.min(l,vx);r=Math.max(r,vx);t=Math.min(t,vy);b=Math.max(b,vy);
    for(int q:new int[]{vx>0?v-1:-1,vx<w-1?v+1:-1,vy>0?v-w:-1,vy<h-1?v+w:-1})if(q>=0&&!seen[q]&&(src.getRGB(q%w,q/w)>>>24)>=100){seen[q]=true;queue[tail++]=q;}
   }
   if(tail<300||(b-t)<35||(r-l)<15)continue;
   int fl=w,fr=0;for(int yy=Math.max(t,b-22);yy<=b;yy++)for(int xx=l;xx<=r;xx++)if((src.getRGB(xx,yy)>>>24)>140){fl=Math.min(fl,xx);fr=Math.max(fr,xx);}
   parts.add(new Part(new Rectangle(l,t,r-l+1,b-t+1),tail,(l+r)/2.0,b,p));
  }
  parts.sort(Comparator.comparingInt(Part::pixels).reversed());if(expected>0&&parts.size()>expected)parts=new ArrayList<>(parts.subList(0,expected));
  return parts;
 }
 static List<Part> order(List<Part> parts,int rows,int cols,int height) {
  parts.sort(Comparator.comparingDouble(p->p.box.getCenterY()));List<Part> out=new ArrayList<>();
  for(int row=0;row<rows;row++){int from=row*cols,to=Math.min(parts.size(),from+cols);if(from>=to)break;List<Part> line=new ArrayList<>(parts.subList(from,to));line.sort(Comparator.comparingInt(p->p.box.x));out.addAll(line);}return out;
 }
 static void write(BufferedImage im,Path p)throws Exception{Files.createDirectories(p.getParent());ImageIO.write(im,"png",p.toFile());}
 static void export(String id,String source,int rows,int cols,int size,int bodyHeight,boolean flip)throws Exception{
  BufferedImage src=alpha(ImageIO.read(ROOT.resolve("source/"+source+".png").toFile()));
  List<Part> pp=order(parts(src,rows*cols),rows,cols,src.getHeight());if(pp.size()!=rows*cols)throw new IllegalStateException(source+" expected "+rows*cols+" got "+pp.size());
  double scale=(double)bodyHeight/pp.stream().mapToInt(p->p.box.height).sorted().skip(Math.max(0,pp.size()/2)).findFirst().orElseThrow();
  for(Part p:pp){double extent=Math.max(p.footX-p.box.x,p.box.getMaxX()-p.footX);scale=Math.min(scale,(size/2.0-4)/Math.max(1,extent));scale=Math.min(scale,(size-5.0)/p.box.height);}
  Path folder=Path.of("img",id,"art",source);StringBuilder audit=new StringBuilder("source="+source+", frames="+pp.size()+", scale="+scale+"\n");
  for(int i=0;i<pp.size();i++){
   Part p=pp.get(i);Rectangle b=p.box;BufferedImage out=new BufferedImage(size,size,2);Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
   if(flip||(source.equals("JerryMotion")&&i==22)){g.translate(size,0);g.scale(-1,1);}g.translate(size/2.0-p.footX*scale,size-2-p.floor*scale);g.scale(scale,scale);g.setClip(b.x-1,b.y-1,b.width+2,b.height+2);g.drawImage(src,0,0,null);g.dispose();write(out,folder.resolve(String.format("%02d.png",i)));
   audit.append(i).append(": ").append(b).append("\n");
  }
  Files.createDirectories(ROOT.resolve("audits"));Files.writeString(ROOT.resolve("audits/"+source+".txt"),audit);System.out.println("EXTRACTED "+source+" "+pp.size()+" frames @ "+size);
 }
 static void erenSource()throws Exception {
  erenSource(false);
 }
 static void erenSource(boolean humanOnly)throws Exception {
  BufferedImage raw=ImageIO.read(ROOT.resolve("source/ErenSheet-original.png").toFile());
  for(boolean titan:new boolean[]{false,true}){
   if(titan&&humanOnly)continue;
   String name=titan?"titan-sheet":"human-sheet";int[] ys=titan?new int[]{760,989,1215,1448}:new int[]{50,280,510,713};int index=0,size=titan?256:128;
   double sheetScale=0;
   for(int row=0;row<3;row++){int cols=row==2?7:8;for(int col=0;col<cols;col++){
    int x=col*raw.getWidth()/cols,x2=(col+1)*raw.getWidth()/cols;
    BufferedImage original=raw.getSubimage(x,ys[row],x2-x,ys[row+1]-ys[row]);BufferedImage cell=alpha(original);cell=repairMatte(cell,original);
    var list=parts(cell,1);if(list.isEmpty())throw new IllegalStateException("Empty Eren source cell "+index);
    Part p=list.getFirst();cell=isolate(cell,p.seed);Rectangle b=p.box;
    // Calibrate once from the standing model; sitting must not grow to standing height.
    if(sheetScale==0)sheetScale=(titan?208.0:104.0)/b.height;
    double sc=sheetScale;
    BufferedImage out=new BufferedImage(size,size,2);Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.translate(size/2.0-b.getCenterX()*sc,size-2-p.floor*sc);g.scale(sc,sc);g.drawImage(cell,0,0,null);g.dispose();write(out,Path.of("img/ErenYeager/art",name,String.format("%02d.png",index++)));
   }}
   // Only discard failed, newly generated diagnostic fragments, never user art.
   for(int i=index;i<34;i++)Files.deleteIfExists(Path.of("img/ErenYeager/art",name,String.format("%02d.png",i)));
   System.out.println("EXTRACTED "+name+" "+index+" actual poses (source label says 24)");
  }
 }
 static BufferedImage repairMatte(BufferedImage src,BufferedImage original){
  int w=src.getWidth(),h=src.getHeight();boolean[] mask=new boolean[w*h];for(int y=0;y<h;y++)for(int x=0;x<w;x++)mask[y*w+x]=(src.getRGB(x,y)>>>24)>0;
  for(int k=0;k<3;k++)mask=morph(mask,w,h,true);
  boolean[] outside=new boolean[w*h];int[] q=new int[w*h];int head=0,tail=0;
  for(int y=0;y<h;y++)for(int x=0;x<w;x++)if((x==0||y==0||x==w-1||y==h-1)&&!mask[y*w+x]&&!outside[y*w+x]){outside[y*w+x]=true;q[tail++]=y*w+x;}
  while(head<tail){int p=q[head++],x=p%w,y=p/w;for(int n:new int[]{x>0?p-1:-1,x<w-1?p+1:-1,y>0?p-w:-1,y<h-1?p+w:-1})if(n>=0&&!mask[n]&&!outside[n]){outside[n]=true;q[tail++]=n;}}
  for(int i=0;i<mask.length;i++)mask[i]=!outside[i];for(int k=0;k<3;k++)mask=morph(mask,w,h,false);
  BufferedImage out=new BufferedImage(w,h,2);for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(mask[y*w+x])out.setRGB(x,y,original.getRGB(x,y)|0xff000000);return out;
 }
 static BufferedImage isolate(BufferedImage s,int seed){int w=s.getWidth(),h=s.getHeight();boolean[] seen=new boolean[w*h];int[] q=new int[w*h];int head=0,tail=0;q[tail++]=seed;seen[seed]=true;while(head<tail){int p=q[head++],x=p%w,y=p/w;for(int n:new int[]{x>0?p-1:-1,x<w-1?p+1:-1,y>0?p-w:-1,y<h-1?p+w:-1})if(n>=0&&!seen[n]&&(s.getRGB(n%w,n/w)>>>24)>80){seen[n]=true;q[tail++]=n;}}BufferedImage out=new BufferedImage(w,h,2);for(int i=0;i<seen.length;i++)if(seen[i])out.setRGB(i%w,i/w,s.getRGB(i%w,i/w));return out;}
 static boolean[] morph(boolean[] m,int w,int h,boolean dilate){boolean[] n=new boolean[m.length];for(int y=1;y<h-1;y++)for(int x=1;x<w-1;x++){boolean value=!dilate;for(int yy=y-1;yy<=y+1;yy++)for(int xx=x-1;xx<=x+1;xx++)if(dilate)value|=m[yy*w+xx];else value&=m[yy*w+xx];n[y*w+x]=value;}return n;}
 static void exportParts(String name,BufferedImage src,List<Part> pp,int size,int body)throws Exception {
  pp.sort(Comparator.comparingDouble(p->p.box.getCenterY()));List<List<Part>> rows=new ArrayList<>();for(Part p:pp){List<Part> row=rows.isEmpty()?null:rows.getLast();if(row==null||Math.abs(row.getFirst().box.getCenterY()-p.box.getCenterY())>100){row=new ArrayList<>();rows.add(row);}row.add(p);}
  List<Part> ordered=new ArrayList<>();for(var row:rows){row.sort(Comparator.comparingInt(p->p.box.x));ordered.addAll(row);}double sc=(double)body/ordered.stream().mapToInt(p->p.box.height).sorted().skip(ordered.size()/2).findFirst().orElseThrow();
  for(Part p:ordered)sc=Math.min(sc,(size/2.0-4)/Math.max(p.footX-p.box.x,p.box.getMaxX()-p.footX));
  for(int i=0;i<ordered.size();i++){var p=ordered.get(i);BufferedImage out=new BufferedImage(size,size,2);Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.translate(size/2.0-p.footX*sc,size-2-p.floor*sc);g.scale(sc,sc);g.setClip(p.box);g.drawImage(src,0,0,null);g.dispose();write(out,Path.of("img/ErenYeager/art",name,String.format("%02d.png",i)));}System.out.println("EXTRACTED "+name+" "+ordered.size());
 }
 public static void main(String[] args)throws Exception {
  if(args.length>0 && args[0].equals("eren-sheet")){erenSource(true);return;}
  export("Tom","Tom",4,6,128,107,true);export("Jerry","Jerry",4,6,96,78,true);
  export("Tom","TomMotion",2,4,128,100,false);export("Jerry","JerryMotion",6,4,96,78,false);
  export("ErenYeager","ErenHumanMotion",6,4,128,104,false);erenSource();
  for(String name:List.of("titan-idle","titan-heavy-punch","titan-roar","titan-ground-smash")){
   Files.copy(Path.of("output/eren-animation-preview-v1",name+".png"),ROOT.resolve("source/"+name+".png"),StandardCopyOption.REPLACE_EXISTING);export("ErenYeager",name,2,2,256,208,false);
  }
 }
}
