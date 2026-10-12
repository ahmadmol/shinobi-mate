package com.group_finity.mascot.update;
import com.group_finity.mascot.pack.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
/** Explicit metadata check only. No executable download, install or subprocess path exists. */
public final class UpdateChecker {
 public record Result(String version,URI downloadPage,boolean newer){ }
 private final HttpClient transport;
 public UpdateChecker(){this(null);}
 public UpdateChecker(HttpClient transport){this.transport=transport;}
 public static URI https(String value){
  URI uri=URI.create(value);
  if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null)throw new IllegalArgumentException("Use an HTTPS page without credentials or fragments");
  return uri;
 }
 public Result check(URI metadata)throws Exception {
  https(metadata.toString());
  var client=transport==null?HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER).build():transport;
  var request=HttpRequest.newBuilder(metadata).timeout(Duration.ofSeconds(12)).header("Accept","application/json").header("User-Agent","ShinobiMate/"+PackManifest.ENGINE_VERSION).GET().build();
  try {
   var response=client.send(request,HttpResponse.BodyHandlers.ofInputStream());
   try(var input=response.body()){
   if(response.statusCode()!=200)throw new IllegalArgumentException("Update endpoint returned HTTP "+response.statusCode());
    var reader=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"ShinobiMateUpdateReader");t.setDaemon(true);return t;});
    try {
     byte[] bytes=reader.submit(()->input.readNBytes(65537)).get(12,java.util.concurrent.TimeUnit.SECONDS);
     if(bytes.length>65536)throw new IllegalArgumentException("Update metadata too large");
     return parse(new String(bytes,StandardCharsets.UTF_8),PackManifest.ENGINE_VERSION);
    }finally{reader.shutdownNow();}
   }
  }finally{if(transport==null)client.close();}
 }
 public Result parse(String data,String current)throws Exception {
  var json=JsonData.object(data);
  String version=JsonData.string(json,"version",JsonData.string(json,"tag_name",""));if(version.startsWith("v"))version=version.substring(1);
  URI page=https(JsonData.string(json,"downloadPage",JsonData.string(json,"html_url","")));
  return new Result(version,page,PackManifest.compare(version,current)>0&&!JsonData.flag(json,"prerelease",false)&&!JsonData.flag(json,"draft",false));
 }
}
