package com.group_finity.mascot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;
import javax.swing.SwingUtilities;

/** Local, bounded diagnostics: event codes, timing/counts and stack frames only. */
public final class RuntimeDiagnostics {
 private static final Logger LOG=Logger.getLogger(RuntimeDiagnostics.class.getName());
 private static final AtomicBoolean started=new AtomicBoolean(),queued=new AtomicBoolean();
 private static volatile long edtSeen=System.nanoTime(),engineSeen=edtSeen;
 private RuntimeDiagnostics(){}
 public static void event(String code,long... numbers){
  if(!code.matches("[A-Z_0-9]+"))throw new IllegalArgumentException("Diagnostic event code");
  StringBuilder message=new StringBuilder("DIAG ").append(code);
  for(long number:numbers)message.append(' ').append(Math.max(0,number));
  LOG.warning(message.toString());
 }
 public static void start(Manager manager){
  if(!started.compareAndSet(false,true))return;
  event("STARTUP_BETA_0_9_0");
  Thread monitor=new Thread(()->{
   long previous=-1,lastReport=0;
   while(true)try{
    Thread.sleep(2000);long now=System.nanoTime();
    if(queued.compareAndSet(false,true))SwingUtilities.invokeLater(()->{edtSeen=System.nanoTime();queued.set(false);});
    long ticks=manager.getLogicTicks();if(ticks!=previous){previous=ticks;engineSeen=now;}
    long edtDelay=(now-edtSeen)/1_000_000,engineDelay=(now-engineSeen)/1_000_000;
    if((edtDelay>5000||engineDelay>5000)&&now-lastReport>30_000_000_000L){
     lastReport=now;event(edtDelay>5000?"EDT_DELAY":"ENGINE_DELAY",edtDelay,engineDelay,ticks);
     for(var entry:Thread.getAllStackTraces().entrySet())if(entry.getKey().getName().startsWith("AWT-EventQueue")||entry.getKey().getName().equals("ShinobiMate engine")){
      var trace=new IllegalStateException("Runtime stall");trace.setStackTrace(entry.getValue());LOG.log(Level.WARNING,"Runtime stall stack",trace);
     }
    }
   }catch(InterruptedException ex){Thread.currentThread().interrupt();return;}
  },"ShinobiMate diagnostics");monitor.setDaemon(true);monitor.start();
 }
}
