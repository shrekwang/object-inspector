package com.github.shrekwang.oi;

import java.lang.instrument.Instrumentation;

public class MemoryMeasurer {
  private static final Instrumentation instrumentation =
    InstrumentationGrabber.instrumentation();

  
  private static final long costOfBareEnumConstant =
    instrumentation.getObjectSize(DummyEnum.CONSTANT);

  private enum DummyEnum {
    CONSTANT;
  }

  
  @SuppressWarnings("all")
  public static long measureBytes(Object rootObject) {
      ObjectFilter<Chain> chains = ObjectFilters.compose(new ObjectFilters.AtMostOncePredicate(), ObjectFilters.notEnumFieldsOrClasses); 
      return ObjectExplorer.exploreObject(rootObject, new MemoryMeasurerVisitor(chains));
  }

  private static class MemoryMeasurerVisitor implements ObjectVisitor<Long> {
    private long memory;
    private final ObjectFilter<Chain> filter;

    MemoryMeasurerVisitor(ObjectFilter<Chain> filter) {
      this.filter = filter;
    }

    public Traversal visit(Chain chain) {
      if (filter.apply(chain)) {
        Object o = chain.getValue();
        memory += instrumentation.getObjectSize(o);
        if (Enum.class.isAssignableFrom(o.getClass())) {
          memory -= costOfBareEnumConstant;
        }
        return Traversal.EXPLORE;
      }
      return Traversal.SKIP;
    }

    public Long result() {
      return memory;
    }
  }
}
