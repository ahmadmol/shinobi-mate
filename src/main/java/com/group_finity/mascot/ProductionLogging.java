package com.group_finity.mascot;
import java.io.IOException;
import java.util.logging.*;
/** Bounded logs without config values, custom messages, credentials or desktop window titles. */
public final class ProductionLogging {
 private ProductionLogging(){ }
 public static void initialize()throws IOException {
  AppPaths.initialize();LogManager.getLogManager().reset();
  Logger root=Logger.getLogger("");root.setLevel(Level.WARNING);
  FileHandler file=new FileHandler(AppPaths.userData().resolve("logs/shinobimate.log").toString(),1_048_576,3,true);
  file.setLevel(Level.WARNING);
  file.setFormatter(new Formatter(){
   public String format(LogRecord record){
    // Exception messages can embed pack expressions, URLs or user content. Log only types and frames.
    StringBuilder out=new StringBuilder(java.time.Instant.ofEpochMilli(record.getMillis()).toString())
      .append(' ').append(record.getLevel()).append(' ').append(record.getLoggerName()).append('.')
      .append(record.getSourceMethodName()).append('\n');
    if(record.getMessage()!=null&&record.getMessage().matches("DIAG [A-Z_0-9]+( [0-9]+)*"))out.append(record.getMessage()).append('\n');
    Throwable error=record.getThrown();
    if(error!=null){out.append(error.getClass().getName()).append('\n');
     StackTraceElement[] stack=error.getStackTrace();for(int i=0;i<Math.min(stack.length,24);i++)out.append("  at ").append(stack[i]).append('\n');}
    return out.toString();
   }
  });root.addHandler(file);
  Thread.setDefaultUncaughtExceptionHandler((thread,error)->{
   root.log(Level.SEVERE,"Unhandled application error",error);
   javax.swing.SwingUtilities.invokeLater(()->javax.swing.JOptionPane.showMessageDialog(null,
    "تعذر إكمال العملية. راجع سجل ShinobiMate.\nCould not complete the operation. Check ShinobiMate logs.",BuildInfo.NAME,javax.swing.JOptionPane.ERROR_MESSAGE));
  });
 }
}
