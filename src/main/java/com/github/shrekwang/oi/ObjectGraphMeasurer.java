package com.github.shrekwang.oi;

import com.github.shrekwang.oi.ObjectExplorer.Feature;
import java.util.EnumSet;


public class ObjectGraphMeasurer {

  public static class Footprint {
    private final int objects;
    private final int references;
    private final Counter counter;

    public Footprint(int objects, int references, Counter counter) {
      this.objects = objects;
      this.references = references;
      this.counter = counter;
    }

    
    public int getObjects() {
      return objects;
    }

    
    public int getReferences() {
      return references;
    }
    
    public Counter getPrimitivesCounter() {
      return counter;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      sb.append("objects:").append(objects).append(",");
      sb.append("references:").append(references).append(",");
      sb.append("primitives:").append(counter);
      sb.append("}");
      return sb.toString();
    }
  }

  
  @SuppressWarnings("all")
  public static Footprint measure(Object rootObject) {
    ObjectFilter<Chain> chains = ObjectFilters.compose(ObjectFilters.notEnumFieldsOrClasses, new ObjectFilters.AtMostOncePredicate()); 
    return ObjectExplorer.exploreObject(rootObject, new ObjectGraphVisitor(chains),
        EnumSet.of(Feature.VISIT_PRIMITIVES, Feature.VISIT_NULL));
  }

  private static class ObjectGraphVisitor implements ObjectVisitor<Footprint> {
    private int objects;
    
    private int references = -1;
    private final Counter counter = new Counter();
    private final ObjectFilter<Chain> filter;

    ObjectGraphVisitor(ObjectFilter<Chain> filter) {
      this.filter = filter;
    }

    public Traversal visit(Chain chain) {
      if (chain.isPrimitive()) {
        counter.add(chain.getValueTypeName());
        return Traversal.SKIP;
      } else {
        references++;
      }
      if (filter.apply(chain) && chain.getValue() != null) {
        objects++;
        return Traversal.EXPLORE;
      }
      return Traversal.SKIP;
    }

    public Footprint result() {
      return new Footprint(objects, references, counter);
    }
  }
}
