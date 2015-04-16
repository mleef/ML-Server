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

    public static ArrayList<Example> JSONtoExampleList(JSONArray attrs, JSONArray exmps) {
        ArrayList<Example> examples = new ArrayList<Example>();
        String[] attributes = new String[attrs.length()];
        for(int i = 0; i < attrs.length(); i++) {
            attributes[i] = attrs.get(i).toString();
        }

        for(int i = 0; i < exmps.length(); i++) {
            String line[] = exmps.get(i).toString().split(",");
            Boolean list[] = new Boolean[line.length];
            for(int j = 0; j < line.length; j++) {
                if(Integer.parseInt(line[j]) == 1) {
                    list[j] = true;
                }
                else {
                    list[j] = false;
                }
            }
            examples.add(new Example(attributes, list));
        }
        return examples;
    }
}
