package com.group_finity.mascot;

import com.group_finity.mascot.pack.PackPaths;
import java.io.IOException;
import java.nio.file.*;

/** Application resources are read-only; every persistent user file lives in one profile. */
public final class AppPaths {
 private AppPaths() { }
 public static Path application() {
  return Path.of(System.getProperty("app.dir",System.getProperty("user.dir","."))).toAbsolutePath().normalize();
 }
 public static Path userData() {
  String override=System.getProperty("app.userDataDir");
  if(override!=null&&!override.isBlank())return Path.of(override).toAbsolutePath().normalize();
  String local=System.getenv("LOCALAPPDATA");
  Path base=local!=null&&!local.isBlank()?Path.of(local):Path.of(System.getProperty("user.home"),"AppData","Local");
  return base.resolve("ShinobiMate").toAbsolutePath().normalize();
 }
 public static Path packs(){return userData().resolve("packs");}
 public static void initialize()throws IOException {
  Files.createDirectories(userData());Files.createDirectories(packs());Files.createDirectories(userData().resolve("logs"));
 }
 public static Path pack(String id) {
  PackPaths.id(id);
  Path bundled=PackPaths.inside(application().resolve("img"),id);
  return Files.isDirectory(bundled)?bundled:PackPaths.inside(packs(),id);
 }
 public static Path image(String imagePath) {
  String value=imagePath.startsWith("/")?imagePath.substring(1):imagePath;
  int separator=value.indexOf('/');
  if(separator<0)return PackPaths.inside(application().resolve("img"),value);
  return PackPaths.inside(pack(value.substring(0,separator)),value.substring(separator+1));
 }
 public static Path theme() {
  Path custom=userData().resolve("theme.properties");
  return Files.isRegularFile(custom)?custom:application().resolve("conf/theme.properties");
 }
}
