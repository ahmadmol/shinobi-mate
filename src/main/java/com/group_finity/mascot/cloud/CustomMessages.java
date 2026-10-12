package com.group_finity.mascot.cloud;
import com.group_finity.mascot.settings.SettingsStore;
import java.nio.file.*;
import java.util.*;
import java.io.*;
/** A separate user layer; editing it never writes a character pack. */
public final class CustomMessages {
 public record Entry(int id,String text,boolean enabled) {
  public Entry{if(id<1||id>100000||text==null||text.isBlank()||text.length()>500)throw new IllegalArgumentException("Message must contain 1–500 characters");}
  public String toString(){return (enabled?"✓ ":"○ ")+text.replace('\n',' ');}
 }
 public static Path path(){return SettingsStore.path().getParent().resolve("custom-cloud.properties");}
 public static List<Entry> load(){return load(path());}
 public static List<Entry> load(Path file) {
  if(!Files.isRegularFile(file))return List.of();
  Properties data=new Properties();
  try {
   if(Files.size(file)>256000)throw new IOException("Custom message layer too large");
   try(InputStream in=Files.newInputStream(file)){data.load(in);}
   List<Entry> entries=new ArrayList<>();
   for(String key:data.stringPropertyNames())if(key.matches("entry\\.[0-9]+\\.text")){
    String id=key.substring(6,key.length()-5);entries.add(new Entry(Integer.parseInt(id),data.getProperty(key),Boolean.parseBoolean(data.getProperty("entry."+id+".enabled","true"))));
   }
   entries.sort(Comparator.comparingInt(Entry::id));return List.copyOf(entries);
  }catch(IOException|IllegalArgumentException ex){java.util.logging.Logger.getLogger(CustomMessages.class.getName()).warning(ex.getMessage());return List.of();}
 }
 public static void save(List<Entry> entries)throws IOException{save(entries,path());}
 public static void save(List<Entry> entries,Path path)throws IOException{
  if(entries.size()>200)throw new IllegalArgumentException("Maximum 200 custom messages");
  Properties data=new Properties();Set<Integer> ids=new HashSet<>();
  for(Entry e:entries){if(!ids.add(e.id))throw new IllegalArgumentException("Duplicate message id");data.setProperty("entry."+e.id+".text",e.text);data.setProperty("entry."+e.id+".enabled",Boolean.toString(e.enabled));}
  SettingsStore.save(data,path);
 }
}
