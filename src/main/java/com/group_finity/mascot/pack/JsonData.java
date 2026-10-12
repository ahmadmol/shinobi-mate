package com.group_finity.mascot.pack;
import java.util.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.json.JsonParser;
/** Parses data through Rhino's JSON parser, never evaluateString or executable JavaScript. */
public final class JsonData {
 private JsonData(){ }
 @SuppressWarnings("unchecked")
 public static Map<String,Object> object(String text) throws Exception {
  if(text.length()>65536)throw new IllegalArgumentException("JSON exceeds 64 KiB");
  int depth=0;boolean string=false,escape=false;
  for(char c:text.toCharArray()){
   if(string){if(escape)escape=false;else if(c=='\\')escape=true;else if(c=='"')string=false;}
   else if(c=='"')string=true;else if(c=='{'||c=='['){if(++depth>24)throw new IllegalArgumentException("JSON nesting too deep");}else if(c=='}'||c==']')depth--;
  }
  Context cx=Context.enter();
  try{Object value=convert(new JsonParser(cx,cx.initSafeStandardObjects()).parseValue(text),0);if(!(value instanceof Map))throw new IllegalArgumentException("Expected JSON object");return (Map<String,Object>)value;}
  finally{Context.exit();}
 }
 private static Object convert(Object value,int depth) {
  if(depth>24)throw new IllegalArgumentException("JSON nesting too deep");
  if(value instanceof NativeArray a){List<Object> result=new ArrayList<>();if(a.getLength()>512)throw new IllegalArgumentException("JSON array too large");for(int i=0;i<a.getLength();i++)result.add(convert(a.get(i,a),depth+1));return List.copyOf(result);}
  if(value instanceof Scriptable object){Map<String,Object> result=new LinkedHashMap<>();for(Object key:object.getIds()){if(!(key instanceof String))throw new IllegalArgumentException("Invalid object key");result.put((String)key,convert(object.get((String)key,object),depth+1));}return Collections.unmodifiableMap(result);}
  return value;
 }
 public static String string(Map<String,Object> map,String key,String fallback) {
  Object value=map.get(key);if(value==null)return fallback;if(!(value instanceof String s)||s.length()>2000)throw new IllegalArgumentException("Invalid "+key);return s;
 }
 public static boolean flag(Map<String,Object> map,String key,boolean fallback) {
  Object value=map.get(key);if(value==null)return fallback;if(!(value instanceof Boolean b))throw new IllegalArgumentException("Invalid "+key);return b;
 }
 public static List<String> strings(Map<String,Object> map,String key) {
  Object value=map.get(key);if(value==null)return List.of();if(!(value instanceof List<?> a)||a.size()>64||a.stream().anyMatch(v->!(v instanceof String)))throw new IllegalArgumentException("Invalid "+key);
  return a.stream().map(v->(String)v).toList();
 }
}
