package com.group_finity.mascot.pack;
import com.group_finity.mascot.character.CharacterDescriptor;
import java.nio.file.*;
import java.util.*;
/** Version 1 is additive: existing actions/behaviors and XML combat stay authoritative. */
public record PackManifest(String id,String displayName,String version,String author,String description,String preview,
 List<String> languages,boolean combat,boolean cloud,boolean mood,String minimumVersion,
 Map<String,String> moodPoses,Map<String,String> interactionPoses,Map<String,String> idleActivities,List<String> socialPoses,boolean legacy) {
 public static final String ENGINE_VERSION=com.group_finity.mascot.BuildInfo.NUMERIC_VERSION;
 public static PackManifest read(Path root,String legacyId)throws Exception {
  Path file=root.resolve("manifest.json");String id=PackPaths.id(legacyId);
  if(!Files.isRegularFile(file))return new PackManifest(id,id,"legacy","—","","Preview.png",List.of("ar"),Files.isRegularFile(root.resolve("conf/combat.xml")),
    Files.isRegularFile(root.resolve("conf/cloud_messages_ar.properties")),false,ENGINE_VERSION,Map.of(),Map.of(),Map.of(),List.of(),true);
  if(Files.size(file)>65536)throw new IllegalArgumentException("Manifest too large");
  var m=JsonData.object(Files.readString(file));id=PackPaths.id(JsonData.string(m,"id",""));
  String preview=JsonData.string(m,"preview","Preview.png");if(!Files.isRegularFile(PackPaths.inside(root,preview)))throw new IllegalArgumentException("Preview is missing");
  String version=JsonData.string(m,"version","1.0.0"),minimum=JsonData.string(m,"minimumShinobiMateVersion",ENGINE_VERSION);
  if(compare(minimum,ENGINE_VERSION)>0)throw new IllegalArgumentException("Pack requires ShinobiMate "+minimum);
  var moods=paths(root,m,"moodPoses");var idle=paths(root,m,"idleActivities");var social=JsonData.strings(m,"socialPoses");
  for(String pose:social)if(!Files.isRegularFile(PackPaths.inside(root,pose)))throw new IllegalArgumentException("Social pose missing");
  boolean combat=JsonData.flag(m,"supportsCombat",false),cloud=JsonData.flag(m,"supportsCloud",false),mood=JsonData.flag(m,"supportsMood",false);
  if(combat&&!Files.isRegularFile(root.resolve("conf/combat.xml")))throw new IllegalArgumentException("Combat requires conf/combat.xml v1");
  if(cloud&&!Files.isRegularFile(root.resolve("conf/cloud_messages_ar.properties"))&&!Files.isRegularFile(root.resolve("conf/cloud_messages_en.properties")))throw new IllegalArgumentException("Cloud messages missing");
  if(mood&&moods.isEmpty())throw new IllegalArgumentException("Mood poses missing");
  return new PackManifest(id,JsonData.string(m,"displayName",id),version,JsonData.string(m,"author","—"),JsonData.string(m,"description",""),preview,
   JsonData.strings(m,"supportedLanguages"),combat,cloud,mood,minimum,moods,paths(root,m,"interactionPoses"),idle,social,false);
 }
 @SuppressWarnings("unchecked")
 private static Map<String,String> paths(Path root,Map<String,Object> data,String key) {
  Object value=data.get(key);if(value==null)return Map.of();if(!(value instanceof Map<?,?> map)||map.size()>24)throw new IllegalArgumentException("Invalid "+key);
  Map<String,String> result=new LinkedHashMap<>();
  for(var e:map.entrySet()){if(!(e.getKey() instanceof String name)||!(e.getValue() instanceof String path)||!Files.isRegularFile(PackPaths.inside(root,path)))throw new IllegalArgumentException("Missing "+key+" pose");result.put(name,path);}
  return Map.copyOf(result);
 }
 public static int compare(String a,String b) {
  if(!a.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")||!b.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}"))throw new IllegalArgumentException("Expected major.minor.patch version");
  String[] aa=a.split("\\."),bb=b.split("\\.");for(int i=0;i<3;i++){int c=Integer.compare(Integer.parseInt(aa[i]),Integer.parseInt(bb[i]));if(c!=0)return c;}return 0;
 }
 public CharacterDescriptor descriptor(Path root){return new CharacterDescriptor(id,displayName,author,version,description,root,preview,combat,cloud,mood,legacy);}
}
