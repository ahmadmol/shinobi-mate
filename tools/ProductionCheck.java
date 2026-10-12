import com.group_finity.mascot.*;
import com.group_finity.mascot.settings.*;
import com.group_finity.mascot.script.*;
import com.group_finity.mascot.cloud.*;
import com.group_finity.mascot.pack.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

/** Profile isolation, old/corrupt settings, log privacy/rotation, expression boundaries and imports. */
public class ProductionCheck extends CharacterCheck {
 public static void main(String[] args){try{production();System.out.println("PASS production profile, corrupt/old settings, restore defaults, missing resources, safe expressions, log rotation/privacy and profile lock; "+checks+" assertions");System.exit(0);}catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 static void production()throws Exception {
  Path profile=Path.of(System.getProperty("app.userDataDir"));AppPaths.initialize();
  require(!SettingsStore.path().startsWith(AppPaths.application()),"profile writes application directory");
  require(BuildInfo.VERSION.equals("0.9.0-beta")&&PackManifest.ENGINE_VERSION.equals("0.9.0"),"metadata mismatch");
  Properties invalid=new Properties();invalid.setProperty("Scaling","NaN");invalid.setProperty("Opacity","Infinity");invalid.setProperty("WindowSize","garbage");invalid.setProperty("MenuDPI","oops");invalid.setProperty("Background","invalid");invalid.setProperty("Language","../../fake");invalid.setProperty("Environment","some.Class");
  SettingsStore.save(invalid,SettingsStore.path());var recovered=SettingsStore.load();
  require(recovered.getProperty("Scaling").equals("1.0")&&recovered.getProperty("Opacity").equals("1.0"),"bad numeric settings");require(recovered.getProperty("Language").equals("ar")&&recovered.getProperty("Environment").equals("generic"),"bad language/environment");
  require(recovered.getProperty("MenuDPI").equals("96")&&recovered.getProperty("WindowSize").equals("600x500"),"old setting format");
  Files.writeString(SettingsStore.path(),"Language=\\uQQQQ\n");
  javax.swing.Timer dismiss=new javax.swing.Timer(50,e->{for(var window:java.awt.Window.getWindows())if(window instanceof JDialog&&window.isShowing())window.dispose();});dismiss.start();
  try{require(SettingsStore.load().getProperty("Language").equals("ar"),"corrupt file not recovered");}finally{dismiss.stop();}
  try(var files=Files.list(profile)){require(files.anyMatch(p->p.getFileName().toString().startsWith("settings-corrupt-")),"corrupt settings lost");}
  Main app=Main.getInstance();app.getProperties().clear();app.getProperties().putAll(recovered);
  var language=Main.class.getDeclaredField("languageBundle");language.setAccessible(true);language.set(app,ResourceBundle.getBundle("language",Locale.forLanguageTag("ar")));
  for(String theme:List.of("PrimaryColour1=oops\nFontSize=NaN\nFontStyle=999\nMenuOpacity=invalid\n","PrimaryColour1=\\uQQQQ\n")) {
   Files.writeString(profile.resolve("theme.properties"),theme);
   SwingUtilities.invokeAndWait(()->{SettingsWindow legacy=new SettingsWindow(null,false);legacy.init();legacy.dispose();});
   require(true,"legacy invalid theme tolerated");
  }Files.delete(profile.resolve("theme.properties"));
  CustomMessages.save(List.of(new CustomMessages.Entry(1,"سر خاص / private message",true)));Files.writeString(profile.resolve("progress.properties"),"saved=1\n");
  app.getProperties().setProperty("ActiveShimeji","NarutoShippuden");app.getProperties().setProperty("OnboardingComplete","true");app.getProperties().setProperty("Scaling","2.5");
  SettingsService.restoreDefaults();require(app.getProperties().getProperty("Scaling").equals("1.0"),"defaults not restored");
  require(app.getProperties().getProperty("ActiveShimeji").equals("NarutoShippuden")&&CustomMessages.load().size()==1&&Files.readString(profile.resolve("progress.properties")).equals("saved=1\n"),"restore deleted user data");
  var mascot=new Mascot("NarutoShippuden");VariableMap data=new VariableMap();data.put("mascot",new Constant(mascot));
  require(new Script("mascot.anchor.x + Math.pow(2,3)",true).get(data) instanceof Number,"legacy expression broken");
  for(String source:List.of("mascot.dispose()","mascot.getClass()","mascot.anchor.x=999","mascot.environment.dispose()","while(true){}","new java.lang.ProcessBuilder()","(function(){return 2;})()","eval('1')","Math.pow(1,2);Math.random()")){
   boolean rejected=false;try{new Script(source,true).get(data);}catch(Exception expected){rejected=true;}require(rejected,"unsafe expression: "+source);
  }mascot.dispose();
  Path pack=profile.resolve("packs/BetaImported");Files.createDirectories(pack);Path source=Path.of("img/NarutoShippuden");
  try(var files=Files.walk(source)){for(Path file:files.toList()){Path dest=pack.resolve(source.relativize(file));if(Files.isDirectory(file))Files.createDirectories(dest);else Files.copy(file,dest);}}
  Path manifest=pack.resolve("manifest.json");Files.writeString(manifest,Files.readString(manifest).replace("NarutoShippuden","BetaImported"));
  require(AppPaths.pack("BetaImported").equals(pack),"import resolver");require(new com.group_finity.mascot.character.CharacterCatalog().list().stream().anyMatch(p->p.id().equals("BetaImported")),"import missing in manager");
  require(!CloudMessages.load("BetaImported").isEmpty(),"imported cloud missing");
  byte[] image=Files.readAllBytes(pack.resolve("Stand.png"));Files.delete(pack.resolve("Stand.png"));
  boolean missing=false;try{PackValidator.validate(pack,"BetaImported");}catch(Exception expected){missing=true;}require(missing,"missing resource accepted");Files.write(pack.resolve("Stand.png"),image);
  require(PackValidator.validate(pack,"BetaImported").id().equals("BetaImported"),"valid import rejected");
  Path directoryInstall=profile.resolve("folder-import");
  try(var preview=new SafePackImporter().previewDirectory(pack,directoryInstall)) {require(!Files.exists(directoryInstall.resolve("BetaImported")),"directory preview wrote installed pack");preview.install();}
  require(Files.isRegularFile(directoryInstall.resolve("BetaImported/conf/actions.xml")),"directory import missing");
  try(SingleInstance guard=SingleInstance.acquire(profile)){require(guard!=null&&SingleInstance.acquire(profile)==null,"duplicate instance accepted");}
  try(SingleInstance guard=SingleInstance.acquire(profile)){require(guard!=null,"stale lock after exit");}
  ProductionLogging.initialize();Logger logger=Logger.getLogger("BetaPrivacyProbe");String secret="DO_NOT_LOG_PASSWORD_TOKEN_CUSTOM_MESSAGE";
  for(int i=0;i<16000;i++)logger.log(Level.WARNING,secret,new IllegalArgumentException(secret));
  LogManager.getLogManager().reset();
  try(var logs=Files.list(profile.resolve("logs"))){var list=logs.filter(p->p.getFileName().toString().startsWith("shinobimate.log")).toList();require(list.size()==3,"missing bounded rotation");for(Path file:list){require(Files.size(file)<1_050_000,"unbounded log");require(!Files.readString(file).contains(secret),"sensitive log content");}}
 }
}
