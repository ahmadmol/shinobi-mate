import com.group_finity.mascot.*;
import com.group_finity.mascot.pack.*;
import com.group_finity.mascot.script.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.nio.charset.StandardCharsets;
public class PackCheck extends CombatCheck {
 public static void main(String[] args) {
  try{testPacks();System.out.println("PASS pack manifests, legacy adapter, staged import/install/cleanup, adversarial archives and script boundaries; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 static Path zip(Path directory,String name,Map<String,byte[]> files)throws Exception {
  Path out=directory.resolve(name);try(var z=new ZipOutputStream(Files.newOutputStream(out))){for(var e:files.entrySet()){z.putNextEntry(new ZipEntry(e.getKey()));z.write(e.getValue());z.closeEntry();}}return out;
 }
 static byte[] bytes(String s){return s.getBytes(StandardCharsets.UTF_8);}
 static void rejected(SafePackImporter importer,Path archive,Path root)throws Exception {
  boolean rejected=false;try(var preview=importer.preview(archive,root)){ }catch(Exception ex){rejected=true;}
  require(rejected,"unsafe archive accepted: "+archive.getFileName());
  try(var paths=Files.list(root)){require(paths.noneMatch(p->p.getFileName().toString().startsWith(".import-")),"failed preview leaked stage");}
 }
 static void testPacks()throws Exception {
  for(String name:java.util.List.of("NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover")) {
   var m=PackValidator.validate(Path.of("img",name),name);require(m.combat()&&m.cloud()&&m.mood()&&!m.legacy(),"manifest capabilities lost");
  }
  Path temp=Files.createTempDirectory("shinobi-pack-test-"),root=temp.resolve("img");Files.createDirectories(root);
  Map<String,byte[]> pack=new LinkedHashMap<>();Path source=Path.of("img","NarutoShippuden");
  try(var files=Files.walk(source)){for(Path f:files.filter(Files::isRegularFile).toList())pack.put(source.relativize(f).toString().replace('\\','/'),Files.readAllBytes(f));}
  pack.put("manifest.json",bytes(new String(pack.get("manifest.json"),StandardCharsets.UTF_8).replace("NarutoShippuden","TestImported")));
  var importer=new SafePackImporter();Path archive=zip(temp,"TestImported.characterpack",pack);
  try(var preview=importer.preview(archive,root)){require(preview.manifest().id().equals("TestImported"),"preview metadata");require(!Files.exists(root.resolve("TestImported")),"preview installed prematurely");preview.install();}
  require(Files.isRegularFile(root.resolve("TestImported/conf/actions.xml")),"install missing data");
  rejected(importer,zip(temp,"traversal.zip",Map.of("../escaped.txt",bytes("bad"))),root);
  rejected(importer,zip(temp,"absolute.zip",Map.of("C:/Windows/evil.txt",bytes("bad"))),root);
  rejected(importer,zip(temp,"ads.zip",Map.of("good.png:evil.exe",bytes("bad"))),root);
  rejected(importer,zip(temp,"device.zip",Map.of("CON.txt",bytes("bad"))),root);
  rejected(importer,zip(temp,"executable.zip",Map.of("launch.exe",bytes("bad"))),root);
  var duplicate=new LinkedHashMap<>(pack);duplicate.put("preview.png",pack.get("Preview.png"));
  rejected(importer,zip(temp,"duplicate.zip",duplicate),root);
  var badXml=new LinkedHashMap<>(pack);badXml.put("conf/actions.xml",bytes("<!DOCTYPE Mascot [<!ENTITY x SYSTEM 'file:///missing'>]><Mascot>&x;</Mascot>"));
  rejected(importer,zip(temp,"xxe.zip",badXml),root);
  var upperXml=new LinkedHashMap<>(pack);upperXml.put("extra.XML",bytes("<!DOCTYPE Mascot [<!ENTITY x SYSTEM 'file:///missing'>]><Mascot>&x;</Mascot>"));
  rejected(importer,zip(temp,"uppercase-xxe.zip",upperXml),root);
  var largeImage=new java.awt.image.BufferedImage(2049,1,java.awt.image.BufferedImage.TYPE_INT_ARGB);var imageBytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(largeImage,"png",imageBytes);
  var upperPng=new LinkedHashMap<>(pack);upperPng.put("extra.PNG",imageBytes.toByteArray());rejected(importer,zip(temp,"uppercase-size.zip",upperPng),root);
  var badManifest=new LinkedHashMap<>(pack);badManifest.put("manifest.json",bytes("{\"id\":\"../../escape\"}"));
  rejected(importer,zip(temp,"bad-id.zip",badManifest),root);
  byte[] bomb=new byte[(int)PackValidator.MAX_FILE+1];rejected(importer,zip(temp,"bomb.zip",Map.of("oversize.txt",bomb)),root);
  boolean refused=false;try(var preview=importer.preview(archive,root)){try{preview.install();}catch(FileAlreadyExistsException expected){refused=true;}}
  require(refused,"existing pack overwritten");
  pack.remove("manifest.json");var legacy=zip(temp,"LegacyAdapted.zip",pack);
  try(var preview=importer.preview(legacy,root)){require(preview.manifest().legacy()&&preview.manifest().combat(),"legacy adaptation");}
  var app=Main.getInstance();var lang=Main.class.getDeclaredField("languageBundle");lang.setAccessible(true);
  lang.set(app,ResourceBundle.getBundle("language",Locale.forLanguageTag("ar"),new com.joconner.i18n.ConfDirectoryUtf8Control(new java.io.File("conf"))));
  for(String script:java.util.List.of("java.lang.Runtime.getRuntime()","Packages.java.lang.System","while(true){}")) {
   boolean stopped=false;try{new Script(script,true).get(new VariableMap());}catch(Exception expected){stopped=true;}require(stopped,"unsafe/unbounded script executed");
  }
  require(((Number)new Script("Math.pow(3,2)+2",true).get(new VariableMap())).intValue()==11,"ordinary expressions broken");
  try(var paths=Files.walk(temp)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}
 }
}
