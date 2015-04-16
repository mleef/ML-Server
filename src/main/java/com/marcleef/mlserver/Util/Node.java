package com.marcleef.mlserver.Util;

/**
 * Created by marc_leef on 4/15/15.
 */

public class Node {
    public String attribute;
    public Node no;
    public Node yes;

    public Node(String a) {
        attribute = a;
        no = null;
        yes = null;
    }

}
