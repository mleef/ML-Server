package com.marcleef.mlserver.MachineLearning;

/**
 * Created by marc_leef on 4/15/15.
 */
import java.util.*;
import java.io.*;
import com.google.gson.*;

import com.marcleef.mlserver.Util.*;

public class DecisionTree {

    ArrayList<Example> Examples;
    static Node tree;
    private final static double CHI_SQUARE_THRESHOLD = 3.84;
    private static String classVariable;



    public DecisionTree(ArrayList<Example> s, String classVar, boolean chiSqr) {
        Examples = s;
        classVariable = classVar;
        HashMap<String, Boolean> attributeMap = new HashMap<String, Boolean>();
        for(String attr : Examples.get(0).getAttributes()) {
            attributeMap.put(attr, true);
        }
        attributeMap.remove(classVariable);
        tree = buildTree(Examples, attributeMap, chiSqr);

    }

    public ArrayList<Example> getSubset(ArrayList<Example> examples, String attr, Boolean b) {
        ArrayList<Example> result = new ArrayList<Example>();
        for(int i = 0; i < examples.size(); i++) {
            if(examples.get(i).getValue(attr) == b) {
                result.add(examples.get(i));
            }
        }
        return result;
    }




    private double entropy(ArrayList<Example> examples) {
        double yes = 0;
        double no = 0;
        double total = examples.size();
        double result;


        for(int i = 0; i < total; i++) {
            if(examples.get(i).getValue(classVariable)) {
                yes++;
            }
            else {
                no++;
            }
        }

        if(yes == 0 || no == 0) {
            result = 0;
        }
        else {
            result = (-((double)yes/total) * logBase2(((double)yes/total))) - (((double)no/total) * logBase2(((double)no/total)));
        }

        if(Double.isNaN(result)) {
            System.out.println(no);
        }

        return result;
    }

    public double gain(ArrayList<Example> examples, String subAttr) {
        double eNorm = entropy(examples);
        double eWeak = entropy(getSubset(examples, subAttr, false));
        double eStrong = entropy(getSubset(examples, subAttr, true));

        double yes = 0;
        double no = 0;
        double total = examples.size();
        for(int i = 0; i < total; i++) {
            if(examples.get(i).getValue(subAttr)) {
                yes++;
            }
            else {
                no++;
            }
        }



        double result =  eNorm - (((double)no/total) * eWeak) - (((double)yes/total) * eStrong);
        return result;
    }


    private static double logBase2(double num) {
        return Math.log(num) / Math.log(2);
    }



    private Node buildTree(ArrayList<Example> examples, HashMap<String, Boolean> attributes, boolean chiSqr) {
        Node root;
        int numPos = 0;
        int numNeg = 0;
        for(int i = 0; i < examples.size(); i++) {
            if(examples.get(i).getValue(classVariable)) {
                numPos++;
            }
            else {
                numNeg++;
            }
        }


        // Check for all positive examples
        if(numNeg == 0) {
            return new Node("+");
        }

        // Check for all negative examples
        else if(numPos == 0) {
            return new Node("-");
        }

        // Check for no attributes
        if(attributes.size() == 0) {
            if(numPos > numNeg) {
                return new Node("+");
            }
            return new Node("-");
        }

        // rootAttr is the attribute that best classifies examples (highest gain)
        String rootAttr = "";
        double highestGain = -2;
        double curGain = 0;
        for(String attr : attributes.keySet()) {
            curGain = gain(examples, attr);
            if(curGain > highestGain) {
                rootAttr = attr;
                highestGain = curGain;
            }
        }


        // Remove highest gain attribute from consideration
        attributes.remove(rootAttr);
        root = new Node(rootAttr);

        ArrayList<Example> posExamples = getSubset(examples, rootAttr, true);
        ArrayList<Example> negExamples = getSubset(examples, rootAttr, false);

        // Need new copies of attributes as java passes objects by reference
        HashMap<String, Boolean> a1 = copy(attributes);
        HashMap<String, Boolean> a2 = copy(attributes);

        // Perform chi-square test, if it fails, stop adding branches and create leaves
        if(chiSqr) {
            if(chiSquare(examples, posExamples, negExamples) > CHI_SQUARE_THRESHOLD) {
                root.no = buildTree(negExamples, a1, chiSqr);
                root.yes = buildTree(posExamples, a2, chiSqr);
            }
            else {
                if(numPos > numNeg) {
                    return new Node("+");
                }
                return new Node("-");
            }
        }
        else {
            root.no = buildTree(negExamples, a1, chiSqr);
            root.yes = buildTree(posExamples, a2, chiSqr);
        }

        return root;
    }

    public static HashMap<String, Boolean> copy(HashMap<String, Boolean> map) {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        for(String str : map.keySet()) {
            result.put(str, map.get(str));
        }
        return result;
    }

    public void printTree(Node root) {
        if(root == null) {
            return;
        }
        System.out.println(root.attribute);
        printTree(root.no);
        printTree(root.yes);
    }

    public String buildModel(Node root, String depth, String passedResult) {
        String noAttr, yesAttr, result;
        if(root.no == null || root.yes == null) {
            return "Invalid Tree";
        }
        noAttr = root.no.attribute;
        yesAttr = root.yes.attribute;
        result = passedResult;


        if(noAttr == "+") {
            result += depth + root.attribute + " = 0 : 1" + "\n";
        }

        else if(noAttr == "-") {
            result += depth + root.attribute + " = 0 : 0" + "\n";
        }

        else {
            result += depth + root.attribute + " = 0" + "\n";
            result = buildModel(root.no, depth + " | ", result);
        }

        if(yesAttr == "+") {
            result += depth + root.attribute + " = 1 : 1" + "\n";
        }

        else if(yesAttr == "-") {
            result += depth + root.attribute + " = 1 : 0" + "\n";
        }

        else {
            result += depth + root.attribute + " = 1" + "\n";
            result = buildModel(root.yes, depth + " | ", result);
        }

        return result;


    }

    public double chiSquare(ArrayList<Example> examples, ArrayList<Example> e1, ArrayList<Example> e2 ) {

        int n = 0;
        int p = 0;
        int S = examples.size();
        int S1 = e1.size();
        int S2 = e2.size();
        int n1 = 0;  // actual negatives
        int p1 = 0;	 //actual positives
        int n2 = 0;
        int p2 = 0;
        double Ep1 = 0;
        double Ep2 = 0;
        double En1 = 0;
        double En2 = 0;

        // (Ei) Expected positive Examples, S1 * p/S
        // (Ei) Expected negative Examples, S1 * n/S
        // Sum of (Ei - Pi)^2/Ei + (Ei - Ni)^2/Ei
        // Large value means you are diverging, this is good
        // Degrees of freedom: 1
        // 3.841

        for(int i = 0; i < examples.size(); i++) {
            if(examples.get(i).getValue(classVariable)) {
                p++;
            }
            else {
                n++;
            }
        }

        for(int i = 0; i < e1.size(); i++) {
            if(e1.get(i).getValue(classVariable)) {
                p1++;
            }
            else {
                n1++;
            }
        }

        for(int i = 0; i < e2.size(); i++) {
            if(e2.get(i).getValue(classVariable)) {
                p2++;
            }
            else {
                n2++;
            }
        }

        Ep1 = S1 * (double)p/S;
        En1 = S1 * (double)n/S;
        Ep2 = S2 * (double)p/S;
        En2 = S2 * (double)n/S;



        return  ((Math.pow(Ep1 - p1, 2))/Ep1) + ((Math.pow(En1 - n1, 2))/En1) +((Math.pow(Ep2 - p2, 2))/Ep2) + ((Math.pow(En2 - n2, 2))/En2);
    }

    public Boolean testExample(Example s) {
        Node cur = tree;
        int steps = 0;
        while(cur.attribute != "+" && cur.attribute != "-") {
            steps++;
            if(s.getValue(cur.attribute)) {
                cur = cur.yes;
            }
            else {
                cur = cur.no;
            }
        }

        if(cur.attribute == "+") {
            return true;
        }
        return false;

    }

    public void writeModel(File f) throws IOException {
        FileWriter fWriter = new FileWriter(f);
        PrintWriter pWriter = new PrintWriter(fWriter);
        pWriter.println(buildModel(tree, "", ""));
        pWriter.close();
        fWriter.close();
    }

    public String getModel() {
        return buildModel(tree, "", "");
    }

    public ArrayList<JSONNode> getJSON(Node cur, String parent) {
        if (cur == null) return new ArrayList();
        ArrayList<JSONNode> nodeValues = new ArrayList<JSONNode>();
        nodeValues.add(new JSONNode(cur.attribute, parent));
        nodeValues.addAll(getJSON(cur.no, cur.attribute));
        nodeValues.addAll(getJSON(cur.yes, cur.attribute));
        return nodeValues;
    }

    public ArrayList<JSONNode> getNodes() {
        return  getJSON(tree, "null");
    }


}
