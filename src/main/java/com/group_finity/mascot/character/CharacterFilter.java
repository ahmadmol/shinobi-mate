package com.group_finity.mascot.character;
import java.text.Normalizer;
import java.util.Locale;
/** Data-only search; ignore case, Arabic vowel marks and tatweel, without character-name rules. */
public final class CharacterFilter {
 public enum Scope { ALL,ACTIVE,COMBAT,CLOUD,MOOD }
 private CharacterFilter(){ }
 private static String normalize(String text){return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}","").replace("ـ","").toLowerCase(Locale.ROOT).strip();}
 public static boolean matches(CharacterDescriptor pack,String query,Scope scope,int active){
  boolean allowed=switch(scope){case ALL->true;case ACTIVE->active>0;case COMBAT->pack.combat();case CLOUD->pack.cloud();case MOOD->pack.mood();};
  return allowed&&normalize(pack.id()+" "+pack.displayName()+" "+pack.author()+" "+pack.description()).contains(normalize(query));
 }
}
