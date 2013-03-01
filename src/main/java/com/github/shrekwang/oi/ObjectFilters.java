package com.github.shrekwang.oi;

import java.util.Set;
import java.util.IdentityHashMap;
import java.util.Collections;

public class ObjectFilters {

  public static class AtMostOncePredicate implements ObjectFilter<Chain> {
    private final Set<Object> interner = Collections.newSetFromMap(
        new IdentityHashMap<Object, Boolean>());

    public boolean apply(Chain chain) {
      Object o = chain.getValue();
      return o instanceof Class<?> || interner.add(o);
    }
    
  }

  public static final ObjectFilter<Chain> notEnumFieldsOrClasses = new ObjectFilter<Chain>(){
    public boolean apply(Chain chain) {
      return !(Enum.class.isAssignableFrom(chain.getValueType())
          || chain.getValue() instanceof Class<?>);
    }
  };

  public static ObjectFilter<Chain> compose(final ObjectFilter<Chain>... values) {
      return new ObjectFilter<Chain>() {
          public boolean apply(final Chain input) {
              for (ObjectFilter<Chain> value: values) {
                  if (! value.apply(input)) {
                      return false;
                  }
              }
              return true;
          }
      };
  }
}
