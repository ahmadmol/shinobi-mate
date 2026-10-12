package com.group_finity.mascot.ui;
import com.group_finity.mascot.character.CharacterDescriptor;
import com.group_finity.mascot.pack.PackPaths;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Bounded preview decoding shared by cards and onboarding; preserve transparent aspect ratios. */
public final class CharacterPreview {
 private CharacterPreview(){ }
 public static ImageIcon icon(CharacterDescriptor pack,int size){
  if(size<16||size>512)throw new IllegalArgumentException("Invalid preview size");
  try(var input=ImageIO.createImageInputStream(PackPaths.inside(pack.root(),pack.preview()).toFile())){
   if(input==null)return null;var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())return null;var reader=readers.next();
   try{
    reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);
    if(w<1||h<1||w>2048||h>2048)return null;
    BufferedImage source=reader.read(0),canvas=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
    double scale=Math.min(size/(double)w,size/(double)h);int width=Math.max(1,(int)Math.round(w*scale)),height=Math.max(1,(int)Math.round(h*scale));
    Graphics2D g=canvas.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(source,(size-width)/2,(size-height)/2,width,height,null);}finally{g.dispose();}
    return new ImageIcon(canvas,pack.displayName());
   }finally{reader.dispose();}
  }catch(Exception ex){return null;}
 }
}
