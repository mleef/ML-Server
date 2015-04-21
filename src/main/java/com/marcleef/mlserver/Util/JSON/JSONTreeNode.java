package com.marcleef.mlserver.Util.JSON;

/**
 * Created by marc_leef on 4/19/15.
 * Used for building d3.js trees on the client side.
 */
public class JSONTreeNode {
    public String name;
    public String parent;

    public JSONTreeNode(String n, String p) {
        name = n;
        parent = p;
    }
}
