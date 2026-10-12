import java.nio.file.*;
import java.io.PrintWriter;
import java.util.spi.ToolProvider;
/** Invoke the standard jpackage ToolProvider already present in the bundled OpenJDK 21 runtime. */
public final class BuildPortableTool {
 public static void main(String[] args){
  Path root=Path.of(System.getProperty("packaging.workspace")).toAbsolutePath().normalize();
  Path destination=Path.of(System.getProperty("packaging.destination")).toAbsolutePath().normalize();
  Path log=root.resolve("output/portable-package-tool.log");
  int exit=1;
  try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(log))){
   if(!destination.startsWith(root.resolve("output")))throw new IllegalArgumentException("Packaging destination must remain under workspace output");
   var tool=ToolProvider.findFirst("jpackage").orElseThrow(()->new IllegalStateException("Bundled runtime has no jpackage provider"));
   var metadata=new java.util.Properties();try(var in=Files.newInputStream(root.resolve("target/classes/build-info.properties"))){metadata.load(in);}
   exit=tool.run(out,out,"--type","app-image","--name",metadata.getProperty("name"),"--app-version",metadata.getProperty("version").split("-",2)[0],"--vendor",metadata.getProperty("publisher"),
    "--description","ShinobiMate desktop companion","--dest",destination.toString(),"--input",root.resolve("output/packaging-input").toString(),
    "--main-jar","ShinobiMate.jar","--main-class","com.group_finity.mascot.Main","--runtime-image",root.resolve("Shimeji-ee/runtime").toString(),
    "--icon",root.resolve("conf/ShinobiMate.ico").toString(),"--java-options","-Dapp.dir=$APPDIR/..","--java-options","-Xmx512M",
    "--java-options","-Xms64M","--java-options","--enable-native-access=ALL-UNNAMED");
   out.println("jpackage exit="+exit);out.flush();
  }catch(Throwable ex){try{Files.writeString(log,ex.toString(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception ignored){ }}
  System.exit(exit);
 }
}
