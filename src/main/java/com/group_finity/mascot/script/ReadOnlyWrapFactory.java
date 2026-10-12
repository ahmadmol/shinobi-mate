package com.group_finity.mascot.script;
import java.util.Set;
import org.mozilla.javascript.*;
/** Only legacy expression data and border predicates are exposed, never arbitrary Java methods. */
final class ReadOnlyWrapFactory extends WrapFactory {
 private static final Set<String> DATA=Set.of("anchor","environment","lookRight","totalCount","x","y","dx","dy",
  "width","height","left","right","top","bottom","visible","activeIE","screen","workArea","cursor",
  "floor","ceiling","leftBorder","rightBorder","topBorder","bottomBorder","isOn");
 ReadOnlyWrapFactory(){setJavaPrimitiveWrap(false);}
 @Override public Scriptable wrapAsJavaObject(Context cx,Scriptable scope,Object value,Class<?> type) {
  return new NativeJavaObject(scope,value,type) {
   @Override public Object get(String name,Scriptable start){return DATA.contains(name)?super.get(name,start):Scriptable.NOT_FOUND;}
   @Override public boolean has(String name,Scriptable start){return DATA.contains(name)&&super.has(name,start);}
   @Override public Object[] getIds(){return DATA.stream().filter(n->super.has(n,this)).toArray();}
   @Override public void put(String name,Scriptable start,Object value){throw Context.reportRuntimeError("Expression data is read-only");}
   @Override public void put(int index,Scriptable start,Object value){throw Context.reportRuntimeError("Expression data is read-only");}
  };
 }
}
