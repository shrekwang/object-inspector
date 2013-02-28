package com.github.shrekwang.oi;

import java.util.HashMap;
import java.util.Map;

public class Counter {

    private Map<String, Integer> store = new HashMap<String, Integer>();

    public void add(String name) {
        Integer c = store.get(name);
        if (c == null ) c = 0;
        c = c.intValue() + 1;
        store.put(name, c);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (String key : store.keySet()) {
            sb.append(key).append("*").append(store.get(key)).append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("]");
        return sb.toString();
    }

}
