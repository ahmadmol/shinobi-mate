import com.group_finity.mascot.update.*;
import com.group_finity.mascot.ui.*;
import com.group_finity.mascot.Main;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.security.*;
import javax.net.ssl.*;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import javax.swing.*;
public class UpdateCheck extends CharacterCheck {
 public static void main(String[] args){
  try{testUpdates();System.out.println("PASS HTTPS metadata, normal/GitHub versions, TLS fixture, size limits, redirect/protocol rejection and update UI; "+checks+" assertions");System.exit(0);}
  catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
 static void fail(RunnableWithException task,String label)throws Exception{boolean rejected=false;try{task.run();}catch(Exception ex){rejected=true;}require(rejected,label);}
 interface RunnableWithException{void run()throws Exception;}
 static void testUpdates()throws Exception {
  var checker=new UpdateChecker();
  require(checker.parse("{\"version\":\"2.0.2\",\"downloadPage\":\"https://example.com/releases\"}","2.0.1").newer(),"new version");
  require(!checker.parse("{\"tag_name\":\"v2.0.1\",\"html_url\":\"https://github.com/example/project/releases/tag/v2.0.1\"}","2.0.1").newer(),"same version");
  require(!checker.parse("{\"version\":\"2.1.0\",\"prerelease\":true,\"downloadPage\":\"https://example.com/releases\"}","2.0.1").newer(),"prerelease offered");
  for(String url:java.util.List.of("http://example.com","file:///C:/evil","javascript:alert(1)","https://user:password@example.com/releases"))fail(()->UpdateChecker.https(url),"unsafe URL accepted");
  fail(()->checker.parse("{\"version\":\"999.1\",\"downloadPage\":\"https://example.com/releases\"}","2.0.1"),"malformed version");
  KeyStore keys=KeyStore.getInstance("PKCS12");try(var in=Files.newInputStream(Path.of("output/update-test.p12"))){keys.load(in,"test-password".toCharArray());}
  var km=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());km.init(keys,"test-password".toCharArray());
  var tm=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());tm.init(keys);
  var ssl=SSLContext.getInstance("TLS");ssl.init(km.getKeyManagers(),tm.getTrustManagers(),null);
  HttpsServer server=HttpsServer.create(new InetSocketAddress("localhost",0),0);server.setHttpsConfigurator(new HttpsConfigurator(ssl));
  server.createContext("/new",exchange->{byte[] body="{\"version\":\"2.0.2\",\"downloadPage\":\"https://example.com/releases\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,body.length);try(var out=exchange.getResponseBody()){out.write(body);}});
  server.createContext("/redirect",exchange->{exchange.getResponseHeaders().add("Location","https://example.com/");exchange.sendResponseHeaders(302,-1);exchange.close();});
  server.createContext("/large",exchange->{byte[] body=new byte[65537];exchange.sendResponseHeaders(200,body.length);try(var out=exchange.getResponseBody()){out.write(body);}});
  server.start();
  try(var client=HttpClient.newBuilder().sslContext(ssl).followRedirects(HttpClient.Redirect.NEVER).build()){
   var fixture=new UpdateChecker(client);String base="https://localhost:"+server.getAddress().getPort();
   require(fixture.check(URI.create(base+"/new")).newer(),"TLS metadata request");
   fail(()->fixture.check(URI.create(base+"/redirect")),"redirect followed");fail(()->fixture.check(URI.create(base+"/large")),"oversize response accepted");
  }finally{server.stop(0);}
  SwingUtilities.invokeAndWait(()->{try{
   com.formdev.flatlaf.FlatDarkLaf.setup();Theme.defaults();
   for(String language:java.util.List.of("ar","en-GB")){Main.getInstance().getProperties().setProperty("Language",language);UpdateDialog d=new UpdateDialog(null);d.setVisible(true);snapshot(d,"updates-"+language);d.dispose();}
  }catch(Exception ex){throw new RuntimeException(ex);}});
 }
}
