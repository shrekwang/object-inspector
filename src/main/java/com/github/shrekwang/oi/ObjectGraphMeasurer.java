package com.github.shrekwang.oi;

import java.util.Set;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import com.github.shrekwang.oi.ObjectExplorer.Feature;
import java.util.EnumSet;


public class ObjectGraphMeasurer {

    public static class ObjectRecord {
        public String objectClassName;
        public long objectSize;

        public ObjectRecord(String name, long size) {
            objectClassName = name;
            objectSize = size;
        }
    }

    public static class Footprint {
        private final String padStr = "    ";
        private final int objects;
        private final int references;
        private final Counter counter;
        private final List<ObjectRecord> objectRecords;
        private final int sharedRef;

        public Footprint(int objects, int references, Counter counter, 
                List<ObjectRecord> records, int sharedRef) {
            this.objects = objects;
            this.references = references;
            this.counter = counter;
            this.objectRecords = records;
            this.sharedRef = sharedRef;
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
            StringBuilder sb = new StringBuilder("{\n");
            sb.append(padStr).append("object count:").append(objects).append("\n");
            sb.append(padStr).append("references:").append(references).append("\n");
            sb.append(padStr).append("shared references:").append(sharedRef).append("\n");
            sb.append(padStr).append("primitives:").append(counter).append("\n");
            sb.append(padStr).append("field size:{\n");
            for (ObjectRecord record: objectRecords) {
                sb.append(padStr).append(padStr).append(record.objectClassName);
                sb.append(":").append(record.objectSize).append("\n");
            }
            sb.append(padStr).append("}\n");
            sb.append("}\n");
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
        private int sharedRef = 0;

        private final Counter counter = new Counter();
        private final List<ObjectRecord> records = new ArrayList<ObjectRecord>();
        private final ObjectFilter<Chain> filter;
        private final Set<Object> interner = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

        ObjectGraphVisitor(ObjectFilter<Chain> filter) {
            this.filter = filter;
        }

        public Traversal visit(Chain chain) {
            if (chain.isPrimitive()) {
                counter.add(chain.getValueTypeName());
                return Traversal.SKIP;
            } else {
                references++;
                if  (!(chain.getValue() instanceof Class<?>)) {
                   if (! interner.add(chain.getValue())) {
                       sharedRef++;
                   }
                }
            }
            if (filter.apply(chain) && chain.getValue() != null) {
                objects++;
                if (chain.getParent() == chain.getRoot()) {
                    long fieldSize = MemoryMeasurer.measureBytes(chain.getValue());
                    String valueName = chain.getValueName();
                    if (valueName == null ) {
                        valueName = chain.getValueTypeName();
                    }
                    records.add(new ObjectRecord(valueName, fieldSize));
                }
                return Traversal.EXPLORE;
            }
            return Traversal.SKIP;
        }

        public Footprint result() {
            return new Footprint(objects, references, counter, records,sharedRef);
        }
    }
}
