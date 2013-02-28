package com.github.shrekwang.oi;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;


public abstract class Chain {
    private final Object value;
    private final Chain parent;

    Chain(Chain parent, Object value) {
        this.parent = parent;
        this.value = value;
    }

    static Chain root(Object value) {
        return new Chain(null, value) {
            @Override
                public Class<?> getValueType() {
                    return getValue().getClass();
                }
            public String getValueTypeName() {
                return getValue().getClass().getName();
            }
        };
    }

    FieldChain appendField(Field field, Object value) {
        return new FieldChain(this, field, value);
    }

    ArrayIndexChain appendArrayIndex(int arrayIndex, Object value) {
        return new ArrayIndexChain(this, arrayIndex, value);
    }


    public boolean hasParent() {
        return parent != null;
    }


    public Chain getParent() {
        return parent;
    }


    public Object getValue() {
        return value;
    }

    public abstract Class<?> getValueType();

    public abstract String getValueTypeName();


    public boolean isThroughField() {
        return false;
    }


    public boolean isThroughArrayIndex() {
        return false;
    }


    public boolean isPrimitive() {
        return getValueType().isPrimitive();
    }


    public Object getRoot() {
        Chain current = this;
        while (current.hasParent()) {
            current = current.getParent();
        }
        return current.getValue();
    }

    Deque<Chain> reverse() {
        Deque<Chain> reverseChain = new ArrayDeque<Chain>(8);
        Chain current = this;
        reverseChain.addFirst(current);
        while (current.hasParent()) {
            current = current.getParent();
            reverseChain.addFirst(current);
        }
        return reverseChain;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(32);

        Iterator<Chain> it = reverse().iterator();
        sb.append(it.next().getValue());
        while (it.hasNext()) {
            sb.append("->");
            Chain current = it.next();
            if (current.isThroughField()) {
                sb.append(((FieldChain)current).getField().getName());
            } else if (current.isThroughArrayIndex()) {
                sb.append("[").append(((ArrayIndexChain)current).getArrayIndex()).append("]");
            }
        }
        return sb.toString();
    }

    static class FieldChain extends Chain {
        private final Field field;

        FieldChain(Chain parent, Field referringField, Object value) {
            super(parent, value);
            this.field = referringField;
        }

        public boolean isThroughField() {
            return true;
        }

        public boolean isThroughArrayIndex() {
            return false;
        }

        public Class<?> getValueType() {
            return field.getType();
        }
        public String getValueTypeName() {
            return field.getType().getName();
        }
        public Field getField() {
            return field;
        }
    }

    static class ArrayIndexChain extends Chain {
        private final int index;

        ArrayIndexChain(Chain parent, int index, Object value) {
            super(parent, value);
            this.index = index;
        }

        public boolean isThroughField() {
            return false;
        }

        public boolean isThroughArrayIndex() {
            return true;
        }

        public Class<?> getValueType() {
            return getParent().getValue().getClass().getComponentType();
        }

        public String getValueTypeName() {
            return getParent().getValue().getClass().getComponentType().getName();
        }

        public int getArrayIndex() {
            return index;
        }
    }
}
