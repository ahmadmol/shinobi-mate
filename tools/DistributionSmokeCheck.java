import java.nio.file.*;
import java.io.PrintStream;
/** Exercises the real GUI launcher/runtime with results preserved even without a console. */
public class DistributionSmokeCheck {
 public static void main(String[] args)throws Exception{
  Path root=Path.of(System.getProperty("app.dir")).toAbsolutePath().normalize();
  PrintStream log=new PrintStream(Files.newOutputStream(root.resolve("smoke-result.txt")),true,java.nio.charset.StandardCharsets.UTF_8);
  System.setOut(log);System.setErr(log);
  System.out.println("RUNTIME "+System.getProperty("java.version")+" app.dir="+root+" cwd="+System.getProperty("user.dir"));
  CombatDesktopCheck.main(args);
 }
}
