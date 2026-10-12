package com.group_finity.mascot;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
/** OS releases the profile lock even after a crash; the lock file is never deleted while held. */
public final class SingleInstance implements AutoCloseable {
 private final FileChannel channel;
 private final FileLock lock;
 private final Path endpoint;
 private ServerSocket server;
 private SingleInstance(FileChannel channel,FileLock lock,Path profile){this.channel=channel;this.lock=lock;this.endpoint=profile.resolve("instance.activation");}
 public static SingleInstance acquire(Path profile)throws IOException {
  Files.createDirectories(profile);
  FileChannel channel=FileChannel.open(profile.resolve("instance.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
  try {
   FileLock lock=channel.tryLock();
   if(lock==null){channel.close();return null;}
   return new SingleInstance(channel,lock,profile);
  }catch(OverlappingFileLockException ex){channel.close();return null;}
  catch(IOException|RuntimeException ex){channel.close();throw ex;}
 }
 /** Only a local authenticated OPEN request is accepted; no paths or executable commands. */
 public void listen(Runnable activate)throws IOException {
  String token=UUID.randomUUID().toString();
  server=new ServerSocket(0,8,InetAddress.getByName("127.0.0.1"));
  Files.writeString(endpoint,server.getLocalPort()+"\n"+token,StandardCharsets.UTF_8);
  Thread listener=new Thread(()->{
   while(!server.isClosed())try(Socket client=server.accept()) {
    client.setSoTimeout(1500);
    var input=new BufferedReader(new InputStreamReader(client.getInputStream(),StandardCharsets.UTF_8));
    // Fixed-length request prevents unbounded allocation by another local process.
    char[] request=new char[token.length()+5];int count=0,n;
    while(count<request.length&&(n=input.read(request,count,request.length-count))>0)count+=n;
    if(count==request.length&&new String(request).equals(token+" OPEN")) {
     RuntimeDiagnostics.event("ACTIVATION_RECEIVED");
     activate.run();client.getOutputStream().write(1);client.getOutputStream().flush();
    }
   }catch(IOException ignored){/* stale/invalid local clients cannot stop activation */}
  },"ShinobiMate activation");listener.setDaemon(true);listener.start();
 }
 public static boolean activate(Path profile) {
  // The profile lock can become visible just before the listener publishes its endpoint.
  for(int attempt=0;attempt<20;attempt++) {
   try {
    Path file=profile.resolve("instance.activation");if(Files.size(file)>128)return false;
    String[] info=Files.readString(file,StandardCharsets.UTF_8).split("\n");
    if(info.length!=2||!info[1].matches("[a-f0-9-]{36}"))return false;
    try(Socket client=new Socket()) {
     client.connect(new InetSocketAddress("127.0.0.1",Integer.parseInt(info[0])),300);client.setSoTimeout(1500);
     client.getOutputStream().write((info[1]+" OPEN").getBytes(StandardCharsets.UTF_8));client.getOutputStream().flush();
     return client.getInputStream().read()==1;
    }
   }catch(IOException|IllegalArgumentException ignored) { }
   try{Thread.sleep(100);}catch(InterruptedException ex){Thread.currentThread().interrupt();return false;}
  }
  return false;
 }
 public void close()throws IOException {
  try{if(server!=null)server.close();Files.deleteIfExists(endpoint);}
  finally{try{lock.release();}finally{channel.close();}}
 }
}
