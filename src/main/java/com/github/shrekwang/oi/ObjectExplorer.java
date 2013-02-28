package com.github.shrekwang.oi;

import java.util.ArrayList;
import com.github.shrekwang.oi.ObjectVisitor.Traversal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;



public class ObjectExplorer {

  public enum Feature { VISIT_NULL, VISIT_PRIMITIVES }

  private ObjectExplorer() { }

  
  public static <T> T exploreObject(Object rootObject, ObjectVisitor<T> visitor) {
    return exploreObject(rootObject, visitor, EnumSet.noneOf(Feature.class));
  }

  
  public static <T> T exploreObject(Object rootObject,
      ObjectVisitor<T> visitor, EnumSet<Feature> features) {
    Deque<Chain> stack = new ArrayDeque<Chain>(32);
    if (rootObject != null) stack.push(Chain.root(rootObject));

    while (!stack.isEmpty()) {
      Chain chain = stack.pop();
      
      Traversal traversal = visitor.visit(chain);
      switch (traversal) {
        case SKIP: continue;
        case EXPLORE: break;
      }

      
      Object value = chain.getValue();
      Class<?> valueClass = value.getClass();
      if (valueClass.isArray()) {
        boolean isPrimitive = valueClass.getComponentType().isPrimitive();
        for (int i = Array.getLength(value) - 1; i >= 0; i--) {
          Object childValue = Array.get(value, i);
          if (isPrimitive) {
            if (features.contains(Feature.VISIT_PRIMITIVES))
              visitor.visit(chain.appendArrayIndex(i, childValue));
            continue;
          }
          if (childValue == null) {
            if (features.contains(Feature.VISIT_NULL))
              visitor.visit(chain.appendArrayIndex(i, childValue));
            continue;
          }
          stack.push(chain.appendArrayIndex(i, childValue));
        }
      } else {
        for (Field field : getAllFields(value)) {
          if (Modifier.isStatic(field.getModifiers())) continue;
          Object childValue = null;
          try {
            childValue = field.get(value);
          } catch (Exception e) {
            throw new AssertionError(e);
          }
          if (childValue == null) {
            if (features.contains(Feature.VISIT_NULL))
              visitor.visit(chain.appendField(field, childValue));
            continue;
          }
          boolean isPrimitive = field.getType().isPrimitive();
          Chain extendedChain = chain.appendField(field, childValue);
          if (isPrimitive) {
            if (features.contains(Feature.VISIT_PRIMITIVES))
              visitor.visit(extendedChain);
            continue;
          } else {
            stack.push(extendedChain);
          }
        }
      }
    }
    return visitor.result();
  }

  
  private static Iterable<Field> getAllFields(Object o) {
    List<Field> fields = new ArrayList<Field>();
    Class<?> clazz = o.getClass();
    while (clazz != null) {
      fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
      clazz = clazz.getSuperclass();
    }

    
    AccessibleObject.setAccessible(fields.toArray(new AccessibleObject[fields.size()]), true);
    return fields;
  }

}
