package com.group_finity.mascot.pack;
import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.zip.*;
/** Preview owns a private staging directory. Nothing is installed until install() is invoked. */
public final class SafePackImporter {
 public static final class Preview implements AutoCloseable {
  private final Path stage,content,approvedRoot;
  private final PackManifest manifest;private boolean installed;
  private Preview(Path stage,Path content,Path root,PackManifest manifest){this.stage=stage;this.content=content;this.approvedRoot=root;this.manifest=manifest;}
  public PackManifest manifest(){return manifest;}
  public Path content(){return content;}
  public Path install()throws IOException {
   if(installed)throw new IllegalStateException("Already installed");
   Path target=PackPaths.inside(approvedRoot,manifest.id());
   if(Files.exists(com.group_finity.mascot.AppPaths.application().resolve("img").resolve(manifest.id())))throw new FileAlreadyExistsException("Bundled pack cannot be replaced");
   if(Files.exists(target,LinkOption.NOFOLLOW_LINKS))throw new FileAlreadyExistsException("Pack already installed: "+manifest.id());
   // Stage and target share a filesystem. Rename is the only installation mutation.
   Files.move(content,target);installed=true;return target;
  }
  public void close()throws IOException{removeStage(stage,approvedRoot);}
 }
 public Preview preview(Path archive,Path packRoot)throws Exception {
  String filename=archive.getFileName().toString().toLowerCase(Locale.ROOT);
  if(!(filename.endsWith(".zip")||filename.endsWith(".characterpack")||filename.endsWith(".smp")))throw new IllegalArgumentException("Expected .characterpack, .zip or legacy .smp");
  if(!Files.isRegularFile(archive)||Files.size(archive)>PackValidator.MAX_TOTAL)throw new IllegalArgumentException("Archive is missing or too large");
  Files.createDirectories(packRoot);Path approvedRoot=packRoot.toRealPath();Path stage=Files.createTempDirectory(approvedRoot,".import-");
  Path extraction=stage.resolve("content");Files.createDirectory(extraction);boolean ok=false;
  try(ZipFile zip=new ZipFile(archive.toFile())){
   Set<String> seen=new HashSet<>();long total=0;int count=0;
   String oldId=null;
   if(filename.endsWith(".smp")) {
    ZipEntry metadata=zip.getEntry("manifest.json");
    if(metadata==null||metadata.getSize()>65536)throw new IllegalArgumentException("Missing legacy metadata");
    try(InputStream in=zip.getInputStream(metadata)){byte[] bytes=in.readNBytes(65537);if(bytes.length>65536)throw new IllegalArgumentException("Legacy manifest too large");oldId=PackPaths.id(JsonData.string(JsonData.object(new String(bytes,java.nio.charset.StandardCharsets.UTF_8)),"name",""));}
   }
   var entries=zip.entries();
   while(entries.hasMoreElements()) {
    ZipEntry entry=entries.nextElement();if(++count>PackValidator.MAX_FILES)throw new IllegalArgumentException("Too many archive entries");
    String name=entry.getName();
    // Legacy .smp metadata/localization is not installed into engine configuration.
    if(filename.endsWith(".smp")){if(!name.startsWith("mascot_data/"))continue;name=name.substring("mascot_data/".length());}
    if(entry.isDirectory()){if(!name.isEmpty())PackPaths.inside(extraction,name.endsWith("/")?name.substring(0,name.length()-1):name);continue;}
    Path destination=PackPaths.inside(extraction,name);PackValidator.extension(name);
    if(!seen.add(Normalizer.normalize(name,Normalizer.Form.NFC).toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("Duplicate archive path");
    if(entry.getSize()>PackValidator.MAX_FILE)throw new IllegalArgumentException("Archive member too large");
    Files.createDirectories(destination.getParent());
    try(InputStream in=zip.getInputStream(entry);OutputStream out=Files.newOutputStream(destination,StandardOpenOption.CREATE_NEW)) {
     byte[] buffer=new byte[8192];long member=0;int length;
     while((length=in.read(buffer))!=-1){member+=length;total+=length;if(member>PackValidator.MAX_FILE||total>PackValidator.MAX_TOTAL)throw new IllegalArgumentException("Expanded archive exceeds limits");out.write(buffer,0,length);}
    }
   }
   List<Path> candidates;
   try(var paths=Files.walk(extraction)){candidates=paths.filter(p->p.endsWith(Path.of("conf","actions.xml"))).map(p->p.getParent().getParent()).toList();}
   if(candidates.size()!=1)throw new IllegalArgumentException("Archive must contain exactly one character pack");
   Path content=candidates.getFirst();String legacyId=oldId!=null?oldId:content.equals(extraction)?archive.getFileName().toString().replaceFirst("\\.[^.]+$",""):content.getFileName().toString();
   var manifest=PackValidator.validate(content,legacyId);
   // Reject unrelated files outside the selected pack (except legacy archive metadata).
   try(var paths=Files.walk(extraction)){if(paths.anyMatch(p->Files.isRegularFile(p)&&!p.startsWith(content)))throw new IllegalArgumentException("Files outside character pack");}
   ok=true;return new Preview(stage,content,approvedRoot,manifest);
  } finally {if(!ok)removeStage(stage,approvedRoot);}
 }
 public Preview previewDirectory(Path folder,Path packRoot)throws Exception {
  String id=PackPaths.id(folder.getFileName().toString());
  Files.createDirectories(packRoot);Path approvedRoot=packRoot.toRealPath();
  Path stage=Files.createTempDirectory(approvedRoot,".import-");Path content=stage.resolve("content");boolean ok=false;
  try {
   Files.createDirectory(content);long total=0;int count=0;
   try(var files=Files.walk(folder)) {
    for(Path file:files.toList()) {
     if(Files.isSymbolicLink(file)||!file.toRealPath().startsWith(folder.toRealPath()))throw new IOException("Pack links are not allowed");
     if(file.equals(folder))continue;
     Path target=PackPaths.inside(content,folder.relativize(file).toString().replace('\\','/'));
     if(Files.isDirectory(file)){Files.createDirectories(target);continue;}
     if(++count>PackValidator.MAX_FILES)throw new IOException("Too many pack files");
     PackValidator.extension(file.toString());long size=Files.size(file);total+=size;
     if(size>PackValidator.MAX_FILE||total>PackValidator.MAX_TOTAL)throw new IOException("Pack exceeds size limits");
     Files.createDirectories(target.getParent());Files.copy(file,target);
    }
   }
   var manifest=PackValidator.validate(content,id);ok=true;return new Preview(stage,content,approvedRoot,manifest);
  }finally{if(!ok)removeStage(stage,approvedRoot);}
 }
 private static void removeStage(Path stage,Path approvedRoot)throws IOException {
  Path resolved=stage.toAbsolutePath().normalize();
  if(!resolved.getParent().equals(approvedRoot)||!resolved.getFileName().toString().startsWith(".import-"))throw new IOException("Invalid staging cleanup target");
  if(!Files.exists(resolved))return;
  try(var paths=Files.walk(resolved)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}
 }
}
