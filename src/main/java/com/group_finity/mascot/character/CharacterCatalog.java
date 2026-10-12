package com.group_finity.mascot.character;
import com.group_finity.mascot.AppPaths;
import java.nio.file.*;
import java.util.*;
/** Bundled IDs take precedence; imports cannot replace application resources. */
public final class CharacterCatalog {
 private static volatile List<String> rejected=List.of();
 public static List<String> rejected(){return rejected;}
 public static Path root(){return AppPaths.packs();}
 public List<CharacterDescriptor> list() {
  Map<String,CharacterDescriptor> result=new TreeMap<>();
  List<String> problems=new ArrayList<>();
  for(Path source:List.of(AppPaths.application().resolve("img"),root())) {
   if(!Files.isDirectory(source))continue;
   try(var folders=Files.list(source)) {
    for(Path p:folders.filter(Files::isDirectory).sorted().toList()) {
     String id=p.getFileName().toString();if(id.startsWith(".")||id.equals("unused"))continue;
     try {
      var manifest=com.group_finity.mascot.pack.PackManifest.read(p,id);
      if(!manifest.id().equals(id))throw new IllegalArgumentException("Manifest id must match directory");
      if(!com.group_finity.mascot.imagesetchooser.ImageSetChooser.isValidCharacterPack(p.toFile()))continue;
      result.putIfAbsent(id,manifest.descriptor(p));
     }catch(Exception ex){problems.add(id);java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,"Invalid character pack",ex);}
    }
   }catch(java.io.IOException ex){java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,"Could not list character packs",ex);}
  }
  rejected=List.copyOf(problems);return List.copyOf(result.values());
 }
}
