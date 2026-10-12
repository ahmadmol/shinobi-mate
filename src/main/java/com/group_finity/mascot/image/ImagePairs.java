package com.group_finity.mascot.image;

import java.util.*;
import java.util.function.Supplier;

/** Reloadable image recipes survive eviction; decoded bitmap retention is bounded. */
public final class ImagePairs {
   private static final long LIMIT=64L*1024*1024;
   private static final Map<String,Supplier<ImagePair>> recipes=new HashMap<>();
   private static final Map<String,ImagePair> fixed=new HashMap<>();
   private static final LinkedHashMap<String,ImagePair> resident=new LinkedHashMap<>(32,.75f,true);
   private static long bytes;
   public static synchronized void load(String filename,ImagePair pair) {
      if(!contains(filename))fixed.put(filename,pair);
   }
   public static synchronized void loadReloadable(String filename,ImagePair pair,Supplier<ImagePair> recipe) {
      if(contains(filename))return;recipes.put(filename,recipe);retain(filename,pair);
   }
   private static long size(ImagePair pair) {
      var left=pair.getImage(false).size();var right=pair.getImage(true).size();
      return 4L*((long)left.width*left.height+(long)right.width*right.height);
   }
   private static void retain(String filename,ImagePair pair) {
      long cost=size(pair);
      // Oversized single frames are usable by the caller without global retention.
      if(cost>LIMIT)return;
      while(bytes+cost>LIMIT&&!resident.isEmpty()) {
         var oldest=resident.entrySet().iterator();var entry=oldest.next();
         bytes-=size(entry.getValue());oldest.remove();
      }
      resident.put(filename,pair);bytes+=cost;
   }
   public static synchronized ImagePair getImagePair(String filename) {
      ImagePair pair=fixed.get(filename);if(pair!=null)return pair;
      pair=resident.get(filename);
      if(pair==null&&recipes.containsKey(filename)){pair=recipes.get(filename).get();retain(filename,pair);}
      return pair;
   }
   public static synchronized boolean contains(String filename){return recipes.containsKey(filename)||fixed.containsKey(filename);}
   public static synchronized void clear(){recipes.clear();fixed.clear();resident.clear();bytes=0;}
   public static synchronized void removeAll(String pack) {
      java.util.function.Predicate<String> belongs=key->{String[] parts=key.split("/");return parts.length>1&&pack.equals(parts[1]);};
      recipes.keySet().removeIf(belongs);fixed.keySet().removeIf(belongs);
      var entries=resident.entrySet().iterator();while(entries.hasNext()) {
         var entry=entries.next();if(belongs.test(entry.getKey())){bytes-=size(entry.getValue());entries.remove();}
      }
   }
   public static MascotImage getImage(String filename,boolean right){ImagePair pair=getImagePair(filename);return pair==null?null:pair.getImage(right);}
   public static synchronized long retainedBytes(){return bytes;}
}
