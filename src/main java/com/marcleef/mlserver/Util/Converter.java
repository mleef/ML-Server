package com.marcleef.mlserver.Util;

/**
 * Created by marc_leef on 4/15/15.
 */

import java.util.ArrayList;
import org.json.*;
import com.marcleef.mlserver.Util.Example;

public final class Converter {

    public Converter () {

    }

    /**
     * Converts JSON objects into Decision Tree readable example lists.
     * @param attrs Attributes of incoming examples.
     * @param exmps Examples to convert.
     * @throws org.json.JSONException
     * @return Correctly constructed example list.
     */
    public static ArrayList<Example> JSONtoExampleList(JSONArray attrs, JSONArray exmps) throws JSONException {
        ArrayList<Example> examples = new ArrayList<Example>();
        String[] attributes = new String[attrs.length()];
        for(int i = 0; i < attrs.length(); i++) {
            attributes[i] = attrs.get(i).toString();
        }

        for(int i = 0; i < exmps.length(); i++) {
            String line[] = exmps.get(i).toString().split(",");
            Boolean list[] = new Boolean[line.length];
            for(int j = 0; j < line.length; j++) {
                try {
                    list[j] = Integer.parseInt(line[j]) == 1;
                }
                catch (NumberFormatException e) {
                    throw new JSONException("Non integer values detected.");
                }
            }
            examples.add(new Example(attributes, list));
        }
        return examples;
    }

    /**
     * Converts JSON objects of examples into readable example lists.
     * @param attrs Attributes of incoming examples.
     * @param exmps Examples to convert.
     * @throws org.json.JSONException
     * @return Correctly constructed example list.
     */
    public static ArrayList<Example> toExampleList(String[] attrs, JSONArray exmps) throws JSONException {
        ArrayList<Example> examples = new ArrayList<Example>();

        for(int i = 0; i < exmps.length(); i++) {
            String line[] = exmps.get(i).toString().split(",");
            Boolean list[] = new Boolean[line.length];
            for(int j = 0; j < line.length; j++) {
                try {
                    list[j] = Integer.parseInt(line[j]) == 1;
                }
                catch (NumberFormatException e) {
                    throw new JSONException("Non integer values detected.");
                }
            }
            examples.add(new Example(attrs, list));
        }
        return examples;
    }

}
