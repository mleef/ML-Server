package com.marcleef.mlserver.Util;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;
/**
 * Created by marc_leef on 4/15/15.
 * Training/Test example objects.
 */
public class Example implements Serializable {

    private HashMap<String, Boolean> map = new HashMap<String, Boolean>();
    private ArrayList<Boolean> vector;
    public Example(String attributes[], Boolean outcomes[]) {
        vector = new ArrayList<Boolean>();
        for(int i = 0; i < attributes.length; i++) {
            map.put(attributes[i], outcomes[i]);
            vector.add(outcomes[i]);
        }
    }

    /**
     * Gets value (1 or 0) of given attribute.
     * @param attr Attribute of example to query.
     * @return Boolean value of attribute.
     */
    public Boolean getValue(String attr) {
        return map.get(attr);
    }


    /**
     * Getter for example attributes.
     * @return List of attributes.
     */
    public String[] getAttributes() {
        return map.keySet().toArray(new String[0]);
    }


    /**
     * Gets alternative data representation of data.
     * @return vector value of data.
     */
    public ArrayList<Boolean> getVector() {
        return vector;
    }

}