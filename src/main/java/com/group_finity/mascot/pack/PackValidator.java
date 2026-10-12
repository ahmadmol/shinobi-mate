package com.group_finity.mascot.pack;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
/** Validate data before engine loading: no executable payloads or external XML entities. */
public final class PackValidator {
 public static final long MAX_FILE=16*1024*1024,MAX_TOTAL=128*1024*1024;
 public static final int MAX_FILES=2048;
 private PackValidator(){ }
 public static void extension(String path) {
  String lower=path.toLowerCase(Locale.ROOT);
  if(!lower.matches(".*\\.(png|xml|json|properties|wav|txt|md)$"))throw new IllegalArgumentException("Unsupported/executable pack file: "+path);
 }
 public static PackManifest validate(Path root,String legacyId)throws Exception {
  var manifest=PackManifest.read(root,legacyId);
  if(!Files.isRegularFile(root.resolve("conf/actions.xml"))||!Files.isRegularFile(root.resolve("conf/behaviors.xml")))throw new IllegalArgumentException("Missing actions.xml or behaviors.xml");
  long total=0,pixels=0;int count=0;
  Path realRoot=root.toRealPath();
  try(var files=Files.walk(root)){
   for(Path file:files.toList()) {
    if(Files.isSymbolicLink(file)||!file.toRealPath().startsWith(realRoot))throw new IllegalArgumentException("Pack links are not allowed");
    if(!Files.isRegularFile(file))continue;
    if(++count>MAX_FILES)throw new IllegalArgumentException("Too many files");
    long size=Files.size(file);total+=size;if(size>MAX_FILE||total>MAX_TOTAL)throw new IllegalArgumentException("Pack exceeds size limits");
    String path=root.relativize(file).toString().replace('\\','/');PackPaths.inside(root,path);extension(path);
    String lower=path.toLowerCase(Locale.ROOT);
    if(lower.endsWith(".xml"))validateXml(root,file);
    if(lower.endsWith(".png")) {
     try(var input=ImageIO.createImageInputStream(file.toFile())) {
      var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IllegalArgumentException("Invalid PNG");
      var reader=readers.next();
      try{reader.setInput(input);if(!reader.getFormatName().equalsIgnoreCase("png"))throw new IllegalArgumentException("Invalid PNG format");
       int w=reader.getWidth(0),h=reader.getHeight(0);pixels+=(long)w*h;
       if(w<1||h<1||w>2048||h>2048||pixels>32_000_000)throw new IllegalArgumentException("Decoded sprite dimensions exceed limit");
      }finally{reader.dispose();}
     }
    }
   }
  }
  if(pixels==0)throw new IllegalArgumentException("Pack contains no PNG images");
  if(manifest.combat())com.group_finity.mascot.combat.CombatProfile.load(root);
  return manifest;
 }
 private static void validateXml(Path root,Path file)throws Exception {
  var doc=SecureXml.read(file);var nodes=doc.getElementsByTagName("*");
  for(int i=0;i<nodes.getLength();i++){
   var attrs=nodes.item(i).getAttributes();
   for(int j=0;j<attrs.getLength();j++){
    var attr=attrs.item(j);String name=attr.getNodeName(),value=attr.getNodeValue();
    if(value.startsWith("${")||value.startsWith("#{")) {
     if(!value.endsWith("}"))throw new IllegalArgumentException("Invalid pack expression");
     com.group_finity.mascot.script.PackExpression.validate(value.substring(2,value.length()-1));
    }
    if(name.equals("Class")&&!value.matches("com\\.group_finity\\.mascot\\.action\\.[A-Za-z]+"))throw new IllegalArgumentException("Only built-in action classes are supported");
    if((name.equals("Image")||name.equals("ImageRight")||name.equals("Sound"))&&!value.isBlank()){
     String relative=value.startsWith("/")?value.substring(1):value;
     Path resource=PackPaths.inside(root,name.equals("Sound")?"sound/"+relative:relative);
     if(!Files.isRegularFile(resource))throw new IllegalArgumentException("Missing pack resource");
    }
   }
  }
 }
}
