import com.group_finity.mascot.*;
import java.nio.file.*;
public class GuardProbe {
 public static void main(String[] args)throws Exception {
  try(var lock=SingleInstance.acquire(AppPaths.userData())) {
   Files.writeString(AppPaths.application().resolve("guard-result.txt"),lock==null?"PASS second process refused":"FAIL second process acquired profile");System.exit(lock==null?0:1);
  }
 }
}
