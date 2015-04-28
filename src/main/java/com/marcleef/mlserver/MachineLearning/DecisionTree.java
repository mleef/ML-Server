package com.marcleef.mlserver.MachineLearning;

/**
 * Created by marc_leef on 4/15/15.
 * Decision Tree build and query logic implementation.
 */
import java.util.*;
import java.io.Serializable;

import com.marcleef.mlserver.Util.*;
import com.marcleef.mlserver.Util.JSON.JSONTreeNode;
import com.marcleef.mlserver.Util.JSON.JSONid;

public class DecisionTree extends Model implements Serializable {

    ArrayList<Example> Examples;
    static TreeNode tree;
    private final double CHI_SQUARE_THRESHOLD = 3.84;
    private  String classVariable;
    private JSONid id;


    /**
     * Decision Tree constructor
     * @param s List of training examples to build tree out of.
     * @param classVar Class variable for training set labeling.
     * @param name Name of decision tree.
     * @param chiSqr Enables chi-square threshold in building process.
     * @return New Decision Tree.
     */
    public DecisionTree(ArrayList<Example> s, String classVar, String name, boolean chiSqr) {
        Examples = s;
        classVariable = classVar;
        HashMap<String, Boolean> attributeMap = new HashMap<>();
        for(String attr : Examples.get(0).getAttributes()) {
            attributeMap.put(attr, true);
        }
        attributeMap.remove(classVariable);
        long startTime = System.nanoTime();
        tree = buildTree(Examples, attributeMap, chiSqr);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        id = new JSONid(name, UUID.randomUUID(), duration);

    }

    /**
     * Helper method for reducing example pools based on given criteria.
     * @param examples List of examples to reduce.
     * @param attr Attribute to narrow list on.
     * @param b Attribute value to reduce list on.
     * @return Subset of given example list;
     */
    public ArrayList<Example> getSubset(ArrayList<Example> examples, String attr, Boolean b) {
        ArrayList<Example> result = new ArrayList<>();
        for(int i = 0; i < examples.size(); i++) {
            if(examples.get(i).getValue(attr) == b) {
                result.add(examples.get(i));
            }
        }
        return result;
    }



    /**
     * Entropy calculation for given example subset.
     * @param examples List of examples to calculate entropy of.
     * @return Entropy of subset.
     */
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

    /**
     * Information gain calculation for given example list.
     * @param examples Example list to calculate information gain on.
     * @param subAttr Attribute to calculate IF on.
     * @return Information Gain for given subset.
     */
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

    /**
     * Helper method for logarithm calculation.
     * @param num Number to take the log base 2 of.
     * @return Log base 2 of input.
     */
    private static double logBase2(double num) {
        return Math.log(num) / Math.log(2);
    }


    /**
     * Recursive tree construction using ID3 algorithm.
     * @param examples List of training examples to build tree out of.
     * @param attributes Remaining attributes to test for information gain.
     * @param chiSqr Chi-square threshold switch.
     * @return Built decision tree.
     */
    private TreeNode buildTree(ArrayList<Example> examples, HashMap<String, Boolean> attributes, boolean chiSqr) {
        TreeNode root;
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
            return new TreeNode("+");
        }

        // Check for all negative examples
        else if(numPos == 0) {
            return new TreeNode("-");
        }

        // Check for no attributes
        if(attributes.size() == 0) {
            if(numPos > numNeg) {
                return new TreeNode("+");
            }
            return new TreeNode("-");
        }

        // rootAttr is the attribute that best classifies examples (highest gain)
        String rootAttr = "";
        double highestGain = -2;
        double curGain;
        for(String attr : attributes.keySet()) {
            curGain = gain(examples, attr);
            if(curGain > highestGain) {
                rootAttr = attr;
                highestGain = curGain;
            }
        }


        // Remove highest gain attribute from consideration
        attributes.remove(rootAttr);
        root = new TreeNode(rootAttr);

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
                    return new TreeNode("+");
                }
                return new TreeNode("-");
            }
        }
        else {
            root.no = buildTree(negExamples, a1, chiSqr);
            root.yes = buildTree(posExamples, a2, chiSqr);
        }

        return root;
    }

    /**
     * Helper method for keeping consistent copies of attribute lists in tree construction.
     * @param map HashMap to make new copy of.
     * @return Subset of given example list;
     */
    public static HashMap<String, Boolean> copy(HashMap<String, Boolean> map) {
        HashMap<String, Boolean> result = new HashMap<>();
        for(String str : map.keySet()) {
            result.put(str, map.get(str));
        }
        return result;
    }

    /**
     * Chi-square test calculation.
     * @param examples Example list to calculate chi-square of
     * @param e1
     * @param e2
     * @return Chi-square test result.
     */
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
        double Ep1;
        double Ep2;
        double En1;
        double En2;

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

    /**
     * Test an example against the built tree.
     * @param s Query example to test against tree.
     * @return Predicted class label for given input query.
     */
    public Boolean testExample(Example s) {
        TreeNode cur = tree;
        while(!cur.attribute.equals("+") && !cur.attribute.equals("-")) {
            if(s.getValue(cur.attribute)) {
                cur = cur.yes;
            }
            else {
                cur = cur.no;
            }
        }

        if(cur.attribute.equals("+")) {
            return true;
        }
        return false;

    }

    /**
     * Recursively builds JSON representation of tree for use in tree drawing on the client side.
     * @param cur Current node in tree.
     * @param parent Parent of current node in tree.
     * @return List of nodes and their relationships with one another.
     */
    public ArrayList<JSONTreeNode> getJSONNodes(TreeNode cur, String parent) {
        if (cur == null) return new ArrayList<>();
        ArrayList<JSONTreeNode> nodeValues = new ArrayList<>();
        nodeValues.add(new JSONTreeNode(cur.attribute, parent));
        nodeValues.addAll(getJSONNodes(cur.no, cur.attribute));
        nodeValues.addAll(getJSONNodes(cur.yes, cur.attribute));
        return nodeValues;
    }


    /**
     * Getter method for id object of tree containing name and key information.
     * @return id
     */
    public JSONid getID() {
        return id;
    }

    @Override
    public String getName() {
        return id.getName();
    }

    @Override
    public String getKey() {
        return id.getKey();
    }
}
