package com.group_finity.mascot.script;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.exception.VariableException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;

public class VariableMap extends AbstractMap<String, Object> implements Bindings {
   private final Map<String, Variable> rawMap = new LinkedHashMap<>();
   private final Set<Map.Entry<String, Object>> entrySet = new AbstractSet<>() {
      public Iterator<Map.Entry<String, Object>> iterator() {
         return new Iterator<>() {
            private final Iterator<Map.Entry<String, Variable>> rawIterator = VariableMap.this.getRawMap().entrySet().iterator();

            public boolean hasNext() {
               return this.rawIterator.hasNext();
            }

            public Map.Entry<String, Object> next() {
               final Map.Entry<String, Variable> rawKeyValue = this.rawIterator.next();
               final Variable value = rawKeyValue.getValue();
               return new Map.Entry<>() {
                  public String getKey() {
                     return rawKeyValue.getKey();
                  }

                  public Object getValue() {
                     try {
                        return value.get(VariableMap.this);
                     } catch (VariableException e) {
                        throw new RuntimeException(e);
                     }
                  }

                  public Object setValue(Object valuex) {
                     throw new UnsupportedOperationException(Main.getInstance().getLanguageBundle().getString("SetValueNotSupportedErrorMessage"));
                  }
               };
            }

            public void remove() {
               this.rawIterator.remove();
            }
         };
      }

      public int size() {
         return VariableMap.this.getRawMap().size();
      }
   };

   public Map<String, Variable> getRawMap() {
      return this.rawMap;
   }

   public void init() {
      this.getRawMap().values().forEach(Variable::init);
   }

   public void initFrame() {
      this.getRawMap().values().forEach(Variable::initFrame);
   }

   public Set<Map.Entry<String, Object>> entrySet() {
      return this.entrySet;
   }

   public Object put(String key, Object value) {
      if (value instanceof Variable varVal) {
         return this.getRawMap().put(key, varVal);
      } else {
         return this.getRawMap().put(key, new Constant(value));
      }
   }
}
