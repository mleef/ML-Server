package com.marcleef.mlserver.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marc_leef on 4/15/15.
 */
public class Example {

    private HashMap<String, Boolean> map = new HashMap<String, Boolean>();
    private ArrayList<Boolean> vector;
    public Example(String attributes[], Boolean outcomes[]) {
        vector = new ArrayList<Boolean>();
        for(int i = 0; i < attributes.length; i++) {
            map.put(attributes[i], outcomes[i]);
            vector.add(outcomes[i]);
        }
    }

    public Boolean getValue(String attr) {
        return map.get(attr);
    }

    public String[] getAttributes() {
        return map.keySet().toArray(new String[0]);
    }

    public String toString() {
        String result = "";
        for(String attr : map.keySet()) {
            result += map.get(attr) + "\t";
        }
        return result;
    }

    public ArrayList<Boolean> getVector() {
        return vector;
    }

}