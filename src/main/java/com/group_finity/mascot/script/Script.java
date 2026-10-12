package com.group_finity.mascot.script;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.exception.VariableException;
import java.util.Map;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class Script extends Variable {
   private static final ContextFactory contextFactory = new ContextFactory() {
      protected Context makeContext() {
         Context cx = super.makeContext();
         cx.setOptimizationLevel(-1);
         cx.setLanguageVersion(200);
         cx.setInstructionObserverThreshold(10000);
         cx.setWrapFactory(new ReadOnlyWrapFactory());
         cx.setClassShutter(name -> {
            if(name.equals("java.awt.Point")||name.equals("java.awt.Dimension")||name.equals("java.awt.Rectangle")||
                  name.matches("java\\.lang\\.(String|Boolean|Double|Integer|Long|Float|Short|Byte)"))return true;
            if(name.startsWith("com.group_finity.mascot.environment."))return true;
            try {
               Class<?> type=Class.forName(name,false,Script.class.getClassLoader());
               return com.group_finity.mascot.Mascot.class.isAssignableFrom(type)||
                     com.group_finity.mascot.environment.Environment.class.isAssignableFrom(type);
            } catch(ClassNotFoundException ex){return false;}
         });
         return cx;
      }

      protected void observeInstructionCount(Context cx, int instructionCount) {
         Object deadline=cx.getThreadLocal(Script.class);
         if (deadline instanceof Long && System.nanoTime()>(Long)deadline) {
            throw new RuntimeException("Script execution timeout - too many instructions");
         }
      }

      protected boolean hasFeature(Context cx, int featureIndex) {
         boolean var10000;
         switch (featureIndex) {
            case 7 -> var10000 = false;
            case 8 -> var10000 = true;
            case 9 -> var10000 = true;
            default -> var10000 = super.hasFeature(cx, featureIndex);
         }

         return var10000;
      }
   };
   private final String source;
   private final boolean clearAtInitFrame;
   private final org.mozilla.javascript.Script compiled;
   private Object value;

   public Script(String source, boolean clearAtInitFrame) throws VariableException {
      this.source = source;
      this.clearAtInitFrame = clearAtInitFrame;
      Context cx = contextFactory.enterContext();

      try {
         PackExpression.validate(this.source);
         this.compiled = cx.compileString(this.source, "<script>", 1, (Object)null);
      } catch (RhinoException e) {
         throw new VariableException(Main.getInstance().getLanguageBundle().getString("ScriptCompilationErrorMessage") + ": " + this.source, e);
      } finally {
         Context.exit();
      }

   }

   public String toString() {
      return this.isClearAtInitFrame() ? "#{" + this.getSource() + "}" : "${" + this.getSource() + "}";
   }

   public void init() {
      this.setValue((Object)null);
   }

   public void initFrame() {
      if (this.isClearAtInitFrame()) {
         this.setValue((Object)null);
      }

   }

   public synchronized Object get(VariableMap variables) throws VariableException {
      if (this.getValue() != null) {
         return this.getValue();
      } else {
         Context cx = contextFactory.enterContext();

         try {
            cx.putThreadLocal(Script.class,System.nanoTime()+100_000_000L);
            Scriptable scope = cx.initSafeStandardObjects();
            this.populateScope(cx, scope, variables);
            Object result = this.getCompiled().exec(cx, scope);
            Object javaResult = this.convertJSToJava(result);
            this.setValue(javaResult);
         } catch (RhinoException e) {
            throw new VariableException(Main.getInstance().getLanguageBundle().getString("ScriptEvaluationErrorMessage") + ": " + this.source, e);
         } catch (RuntimeException e) {
            throw new VariableException(Main.getInstance().getLanguageBundle().getString("ScriptEvaluationErrorMessage") + ": " + this.source, e);
         } finally {
            Context.exit();
         }

         return this.getValue();
      }
   }

   private void populateScope(Context cx, Scriptable scope, VariableMap variables) {
      if (variables != null && variables.getRawMap() != null) {
         for(Map.Entry entry : variables.getRawMap().entrySet()) {
            String key = (String)entry.getKey();
            Variable variable = (Variable)entry.getValue();

            try {
               Object value = this.getVariableValueSafely(variable, variables);
               if (value != null) {
                  Object jsValue = this.convertJavaToJS(value, scope);
                  ScriptableObject.putProperty(scope, key, jsValue);
               }
            } catch (Exception var10) {
            }
         }

      }
   }

   private Object getVariableValueSafely(Variable variable, VariableMap variables) throws VariableException {
      if (variable instanceof Script scriptVar) {
         return scriptVar.getValue();
      } else {
         return variable.get(variables);
      }
   }

   private Object convertJavaToJS(Object value, Scriptable scope) {
      if (value == null) {
         return null;
      } else {
         return !(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) ? Context.javaToJS(value, scope) : value;
      }
   }

   private Object convertJSToJava(Object result) {
      if (result != null && result != Undefined.instance) {
         return !(result instanceof String) && !(result instanceof Number) && !(result instanceof Boolean) ? Context.jsToJava(result, Object.class) : result;
      } else {
         return null;
      }
   }

   private void setValue(Object value) {
      this.value = value;
   }

   private Object getValue() {
      return this.value;
   }

   private boolean isClearAtInitFrame() {
      return this.clearAtInitFrame;
   }

   private org.mozilla.javascript.Script getCompiled() {
      return this.compiled;
   }

   private String getSource() {
      return this.source;
   }
}
