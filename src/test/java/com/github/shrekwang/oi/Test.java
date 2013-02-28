package com.github.shrekwang.oi;


public class Test {

    public static void main(String[] args) {
        System.out.println( MemoryMeasurer.measureBytes(new Book()));
        System.out.println( ObjectGraphMeasurer.measure(new Book()));

        System.out.println( MemoryMeasurer.measureBytes(new Apple()));
        System.out.println( ObjectGraphMeasurer.measure(new Apple()));

        int a = 10;
        System.out.print("sf");
    }
}
