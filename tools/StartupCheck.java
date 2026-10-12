import com.group_finity.mascot.win.AutoStartManager;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class StartupCheck extends CombatCheck {
 public static void main(String[] args){try{startup();System.out.println("PASS native Windows shortcut target/working directory, Unicode/spaces/apostrophe/percent paths and safe batch fallback; "+checks+" assertions; real user Startup untouched");System.exit(0);}catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 static void startup()throws Exception {
  Path temp=Files.createTempDirectory("shinobi-shortcut-check-"),folder=temp.resolve("Shinobi test's folder");Files.createDirectories(folder);Path target=folder.resolve("ShinobiMate.exe"),shortcut=folder.resolve("ShinobiMate.lnk");Files.copy(Path.of("output/release-image/ShinobiMate-Portable/ShinobiMate.exe"),target);Files.setAttribute(target,"dos:readonly",false);System.setProperty("app.dir",folder.toString());
  var executable=AutoStartManager.class.getDeclaredMethod("getExecutablePath");executable.setAccessible(true);require(Path.of((String)executable.invoke(null)).equals(target.toRealPath()),"product launcher resolution");
  var create=AutoStartManager.class.getDeclaredMethod("createShortcut",String.class,String.class);create.setAccessible(true);require((boolean)create.invoke(null,target.toString(),shortcut.toString()),"shortcut creation failed");require(Files.isRegularFile(shortcut),"native .lnk missing");
  String read="$ProgressPreference='SilentlyContinue';$s=(New-Object -ComObject WScript.Shell).CreateShortcut('"+shortcut.toString().replace("'","''")+"');'TARGET:'+([Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($s.TargetPath)));'WORK:'+([Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($s.WorkingDirectory)))";
  String encoded=Base64.getEncoder().encodeToString(read.getBytes(StandardCharsets.UTF_16LE));Process process=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-WindowStyle","Hidden","-EncodedCommand",encoded).redirectErrorStream(true).start();
  if(!process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();throw new AssertionError("shortcut read timed out");}require(process.exitValue()==0,"COM shortcut read failed");String[] values=new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8).trim().split("\\R");
  String targetValue=Arrays.stream(values).filter(v->v.startsWith("TARGET:")).findFirst().orElseThrow().substring(7),workValue=Arrays.stream(values).filter(v->v.startsWith("WORK:")).findFirst().orElseThrow().substring(5);
  require(Path.of(new String(Base64.getDecoder().decode(targetValue),StandardCharsets.UTF_8)).equals(target),"shortcut target quoting");require(Path.of(new String(Base64.getDecoder().decode(workValue),StandardCharsets.UTF_8)).equals(folder),"shortcut working directory quoting");
  Path unicode=temp.resolve("رفيق 100%");Files.createDirectories(unicode);Path unicodeTarget=unicode.resolve("ShinobiMate.exe"),unicodeShortcut=unicode.resolve("ShinobiMate.lnk");Files.copy(target,unicodeTarget);Files.setAttribute(unicodeTarget,"dos:readonly",false);
  var fallback=AutoStartManager.class.getDeclaredMethod("createShortcutFallback",String.class,String.class);fallback.setAccessible(true);require((boolean)fallback.invoke(null,unicodeTarget.toString(),unicodeShortcut.toString()),"batch fallback creation failed");String batch=Files.readString(unicode.resolve("ShinobiMate.bat"));require(batch.contains("100%%")&&batch.contains("رفيق")&&batch.contains("chcp 65001")&&batch.contains("start \"\" \""),"UTF-8 batch quoting");
  try(var files=Files.walk(temp)){for(Path file:files.sorted(Comparator.reverseOrder()).toList())Files.delete(file);}
 }
}
