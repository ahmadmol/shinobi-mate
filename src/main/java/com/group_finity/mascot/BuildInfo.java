package com.group_finity.mascot;
import java.util.Properties;
/** Values filtered from Maven's project metadata, also consumed by the distribution scripts. */
public final class BuildInfo {
 public static final String NAME,VERSION,NUMERIC_VERSION,PUBLISHER;
 static {
  Properties p=new Properties();
  try(var in=BuildInfo.class.getResourceAsStream("/build-info.properties")) {
   if(in==null)throw new IllegalStateException("Missing build metadata");p.load(in);
  }catch(Exception ex){throw new ExceptionInInitializerError(ex);}
  NAME=p.getProperty("name");VERSION=p.getProperty("version");PUBLISHER=p.getProperty("publisher");
  NUMERIC_VERSION=VERSION.split("-",2)[0];
 }
 private BuildInfo(){ }
}
