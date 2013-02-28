package com.github.shrekwang.oi;


public interface ObjectVisitor<T> {
  Traversal visit(Chain chain);
  T result();
  enum Traversal { EXPLORE, SKIP }
}
