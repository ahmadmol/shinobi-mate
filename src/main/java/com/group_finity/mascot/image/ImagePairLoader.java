package com.group_finity.mascot.image;

import com.group_finity.mascot.license.LicenseLevel;
import com.group_finity.mascot.license.LicenseManager;
import hqx.Hqx_2x;
import hqx.Hqx_3x;
import hqx.Hqx_4x;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ImagePairLoader {
   private static BufferedImage loadImage(String imagePath) throws IOException {
      String normalizedPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
      normalizedPath = normalizedPath.replace('/', File.separatorChar);
      String externalPath = com.group_finity.mascot.AppPaths.image(normalizedPath.replace(File.separatorChar,'/')).toString();
      File externalFile = new File(externalPath);
      if (externalFile.exists()) {
         InputStream is = new FileInputStream(externalFile);

         BufferedImage var14;
         try {
            var14 = ImageIO.read(is);
         } catch (Throwable var10) {
            try {
               is.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         is.close();
         return var14;
      } else {
         InputStream resourceStream = ImagePairLoader.class.getResourceAsStream(imagePath);
         if (resourceStream != null) {
            InputStream is = resourceStream;

            BufferedImage var6;
            try {
               var6 = ImageIO.read(is);
            } catch (Throwable var11) {
               if (resourceStream != null) {
                  try {
                     is.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (resourceStream != null) {
               resourceStream.close();
            }

            return var6;
         } else {
            throw new IOException("Cannot find image: " + imagePath + " (tried external path: " + externalFile.getAbsolutePath() + " and classpath resource)");
         }
      }
   }

   public static void load(String name, String rightName, Point center, double scaling, Filter filter, double opacity) throws IOException {
      if (!ImagePairs.contains(name + (rightName == null ? "" : rightName))) {
         Point anchor=new Point(center);
         ImagePair pair=createPair(name,rightName,anchor,scaling,filter,opacity);
         ImagePairs.loadReloadable(name+(rightName==null?"":rightName),pair,()->{
            try{return createPair(name,rightName,anchor,scaling,filter,opacity);}
            catch(IOException error){throw new java.io.UncheckedIOException(error);}
         });
      }
   }

   private static ImagePair createPair(String name,String rightName,Point center,double scaling,Filter filter,double opacity)throws IOException {
         BufferedImage leftImage = scale(premultiply(loadImage(name), opacity), scaling, filter);
         BufferedImage rightImage;
         if (rightName == null) {
            rightImage = flip(leftImage);
         } else {
            rightImage = scale(premultiply(loadImage(rightName), opacity), scaling, filter);
         }

         ImagePair ip = new ImagePair(new MascotImage(leftImage, new Point((int)Math.round((double)center.x * scaling), (int)Math.round((double)center.y * scaling))), new MascotImage(rightImage, new Point(rightImage.getWidth() - (int)Math.round((double)center.x * scaling), (int)Math.round((double)center.y * scaling))));
         return ip;
   }

   private static BufferedImage flip(BufferedImage src) {
      BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType() == 0 ? 2 : src.getType());

      for(int y = 0; y < src.getHeight(); ++y) {
         for(int x = 0; x < src.getWidth(); ++x) {
            copy.setRGB(copy.getWidth() - x - 1, y, src.getRGB(x, y));
         }
      }

      return copy;
   }

   private static BufferedImage premultiply(BufferedImage source, double opacity) {
      BufferedImage returnImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType() == 0 ? 3 : source.getType());

      for(int y = 0; y < returnImage.getHeight(); ++y) {
         for(int x = 0; x < returnImage.getWidth(); ++x) {
            Color colour = new Color(source.getRGB(x, y), true);
            float[] components = colour.getComponents((float[])null);
            components[3] *= (float)opacity;
            components[0] = components[3] * components[0];
            components[1] = components[3] * components[1];
            components[2] = components[3] * components[2];
            colour = new Color(components[0], components[1], components[2], components[3]);
            returnImage.setRGB(x, y, colour.getRGB());
         }
      }

      return returnImage;
   }

   private static BufferedImage scale(BufferedImage source, double scaling, Filter filter) {
      int width = source.getWidth();
      int height = source.getHeight();
      BufferedImage workingImage = null;
      if (filter == ImagePairLoader.Filter.HQX) {
         LicenseManager licenseManager = LicenseManager.getInstance();
         LicenseLevel currentLevel = licenseManager.getCurrentLicenseLevel();
         if (currentLevel == LicenseLevel.NO_KEY) {
            filter = ImagePairLoader.Filter.NEAREST_NEIGHBOUR;
            Logger.getLogger(ImagePairLoader.class.getName()).info("HQX filter disabled - Advanced license required. Using NEAREST_NEIGHBOUR instead.");
         }
      }

      double effectiveScaling = scaling;
      if (filter == ImagePairLoader.Filter.HQX && scaling > (double)1.0F) {
         int[] rbgValues = source.getRGB(0, 0, width, height, (int[])null, 0, width);
         if (scaling != (double)4.0F && scaling != (double)8.0F) {
            if (scaling != (double)3.0F && scaling != (double)6.0F) {
               if (scaling == (double)2.0F) {
                  width *= 2;
                  height *= 2;
                  int[] buffer = new int[width * height];
                  Hqx_2x.hq2x_32_rb(rbgValues, buffer, width / 2, height / 2);
                  rbgValues = buffer;
                  effectiveScaling = (double)1.0F;
               } else {
                  filter = ImagePairLoader.Filter.NEAREST_NEIGHBOUR;
               }
            } else {
               width *= 3;
               height *= 3;
               int[] buffer = new int[width * height];
               Hqx_3x.hq3x_32_rb(rbgValues, buffer, width / 3, height / 3);
               rbgValues = buffer;
               effectiveScaling = scaling > (double)4.0F ? (double)2.0F : (double)1.0F;
            }
         } else {
            width *= 4;
            height *= 4;
            int[] buffer = new int[width * height];
            Hqx_4x.hq4x_32_rb(rbgValues, buffer, width / 4, height / 4);
            rbgValues = buffer;
            effectiveScaling = scaling > (double)4.0F ? (double)2.0F : (double)1.0F;
         }

         if (filter == ImagePairLoader.Filter.HQX) {
            workingImage = new BufferedImage((int)Math.round((double)width * effectiveScaling), (int)Math.round((double)height * effectiveScaling), 3);
            int srcColIndex = 0;
            int srcRowIndex = 0;

            for(int y = 0; y < workingImage.getHeight(); ++y) {
               for(int x = 0; x < workingImage.getWidth(); ++x) {
                  workingImage.setRGB(x, y, rbgValues[srcColIndex / (int)effectiveScaling]);
                  ++srcColIndex;
               }

               ++srcRowIndex;
               if ((double)srcRowIndex != effectiveScaling) {
                  srcColIndex -= workingImage.getWidth();
               } else {
                  srcRowIndex = 0;
               }
            }
         }
      }

      width = (int)Math.round((double)width * effectiveScaling);
      height = (int)Math.round((double)height * effectiveScaling);
      BufferedImage copy = new BufferedImage(width, height, 3);
      Graphics2D g2d = copy.createGraphics();
      Object renderHint = filter == ImagePairLoader.Filter.BICUBIC ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
      g2d.drawImage(workingImage != null ? workingImage : source, 0, 0, width, height, (ImageObserver)null);
      g2d.dispose();
      return copy;
   }

   public static enum Filter {
      NEAREST_NEIGHBOUR,
      HQX,
      BICUBIC;

      // $FF: synthetic method
      private static Filter[] $values() {
         return new Filter[]{NEAREST_NEIGHBOUR, HQX, BICUBIC};
      }
   }
}
