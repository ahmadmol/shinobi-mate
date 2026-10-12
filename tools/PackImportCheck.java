import com.group_finity.mascot.Main;
import com.group_finity.mascot.pack.*;
import com.group_finity.mascot.ui.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;
public class PackImportCheck extends CharacterCheck {
 public static void main(String[] args){try{uiImport();System.out.println("PASS real bilingual pack preview, cancel cleanup, explicit confirm install, metadata and RTL/LTR; "+checks+" assertions");System.exit(0);}catch(Throwable ex){ex.printStackTrace();System.exit(1);}}
 static JOptionPane question(Container parent){for(Component c:parent.getComponents()){if(c instanceof JOptionPane pane)return pane;if(c instanceof Container child){var found=question(child);if(found!=null)return found;}}return null;}
 static void uiImport()throws Exception {
  Path temp=Files.createTempDirectory("shinobi-import-ui-"),root=temp.resolve("packs");Files.createDirectories(root);System.setProperty("app.dir",temp.toString());System.setProperty("app.userDataDir",temp.toString());
  var entries=new LinkedHashMap<String,byte[]>();Path source=Path.of("img/NarutoShippuden");try(var files=Files.walk(source)){for(Path f:files.filter(Files::isRegularFile).toList())entries.put(source.relativize(f).toString().replace('\\','/'),Files.readAllBytes(f));}
  entries.put("manifest.json",PackCheck.bytes(new String(entries.get("manifest.json"),java.nio.charset.StandardCharsets.UTF_8).replace("NarutoShippuden","UIImported")));
  Path archive=PackCheck.zip(temp,"UIImported.characterpack",entries);var app=Main.getInstance();com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
  for(String language:java.util.List.of("ar","en-GB"))for(boolean install:java.util.List.of(false,true)) {
   app.getProperties().setProperty("Language",language);AtomicBoolean shown=new AtomicBoolean();AtomicReference<Throwable> failure=new AtomicReference<>();long deadline=System.nanoTime()+15_000_000_000L;
   javax.swing.Timer timer=new javax.swing.Timer(100,event->{for(java.awt.Window window:java.awt.Window.getWindows())if(window.isVisible()&&"PackPreview".equals(window.getName())){
    try {require(window.getComponentOrientation().isLeftToRight()==!language.equals("ar"),"preview orientation");var pane=question((Container)window);require(pane!=null,"missing confirmation UI");require(!Files.exists(root.resolve("UIImported")),"installed before approval");snapshot(window,"import-"+language);shown.set(true);pane.setValue(pane.getOptions()[install?0:1]);}
    catch(Throwable ex){failure.set(ex);window.dispose();}
   }
   if(System.nanoTime()>deadline){failure.set(new AssertionError("preview never appeared"));for(var window:java.awt.Window.getWindows())window.dispose();}
   });timer.start();boolean installed;
   try{installed=PackImportDialog.installWithPreview(null,archive);}finally{timer.stop();}
   if(failure.get()!=null)throw new AssertionError(failure.get());require(shown.get()&&installed==install,"confirmation choice ignored");
   require(Files.exists(root.resolve("UIImported"))==install,"cancel/install result mismatch");try(var files=Files.list(root)){require(files.noneMatch(p->p.getFileName().toString().startsWith(".import-")),"preview stage leaked");}
   if(install){require(PackValidator.validate(root.resolve("UIImported"),"UIImported").id().equals("UIImported"),"installed metadata mismatch");try(var files=Files.walk(root.resolve("UIImported"))){for(Path file:files.sorted(Comparator.reverseOrder()).toList())Files.delete(file);}}
  }
  try(var files=Files.walk(temp)){for(Path file:files.sorted(Comparator.reverseOrder()).toList())Files.delete(file);}
 }
}
