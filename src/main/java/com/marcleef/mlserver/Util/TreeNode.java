package com.marcleef.mlserver.Util;


import java.io.Serializable;
/**
 * Created by marc_leef on 4/15/15.
 * Node objects for decision tree structure.
 */

public class TreeNode implements Serializable {
    public String attribute;
    public TreeNode no;
    public TreeNode yes;

    public TreeNode(String a) {
        attribute = a;
        no = null;
        yes = null;
    }

}
