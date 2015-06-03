package com.marcleef.mlserver.MachineLearning;

/**
 * Created by marc_leef on 4/15/15.
 */

import java.util.ArrayList;
import java.io.Serializable;

import com.marcleef.mlserver.Util.Example;

public class NaiveBayes  extends Model implements Serializable {
    ArrayList<ArrayList<Boolean>> Examples;
    static ArrayList<Double> trainedWeights;
    private double baseWeight;
    private static String classVariable;
    private String[] attributes;
    private static final double THRESHOLD = .5;
    private String nbName;

    /**
     * Naive Bayes model constructor.
     * @param s List of examples to train model on.
     * @param classVar Class Variable of training samples.
     * @param beta Value to use for beta prior.
     * @return New Naive Bayes object instance.
     */
    public NaiveBayes(ArrayList<Example> s, String classVar, double beta, String modelName) {
        nbName = modelName;
        Examples = new ArrayList<ArrayList<Boolean>>();
        attributes = s.get(0).getAttributes();
        for(int i = 0; i < s.size(); i++) {
            Examples.add(s.get(i).getVector());
        }
        classVariable = classVar;
        trainedWeights = learn(Examples, beta);
    }

    /**
     * Runs the naive bayes classifier to train the model.
     * @param s Training examples with which to build models.
     * @param beta Beta prior value.
     * @return List of variable weights.
     */
    public ArrayList<Double> learn(ArrayList<ArrayList<Boolean>> s, double beta) {
        int lastIndex = s.get(0).size() - 1;
        double topBeta = beta - 1;
        double bottomBeta = (2 * beta) - 2;

        ArrayList<Double> weights = new ArrayList<Double>();

        // Initialize empty weights
        for(int i = 0; i < s.get(0).size() - 1; i++) {
            weights.add(0.0);
        }

        // Initialize counters
        int falseTrue = 0;
        int falseFalse = 0;
        int trueTrue = 0;
        int trueFalse = 0;

        // Initialize probabilities
        double pFT = 0;
        double pFF = 0;
        double pTT = 0;
        double pTF = 0;


        // Loop through each attribute, compute its probabilities and weight
        for(int i = 0; i < s.get(0).size() - 1; i++) {
            for(ArrayList<Boolean> Example : s) {
                if(Example.get(i)) {
                    if(Example.get(lastIndex)) {
                        trueTrue += 1;
                    }
                    else {
                        trueFalse += 1;
                    }
                }
                else {
                    if(Example.get(lastIndex)) {
                        falseTrue += 1;
                    }
                    else {
                        falseFalse += 1;
                    }

                }
            }

            // Calculate probabilities
            pFT = (falseTrue + topBeta)/(falseTrue + trueTrue + bottomBeta);
            pFF = (falseFalse + topBeta)/(falseFalse + trueFalse + bottomBeta);
            pTT = (trueTrue + topBeta)/(trueTrue + falseTrue + bottomBeta);
            pTF = (trueFalse + topBeta)/(trueFalse + falseFalse + bottomBeta);

            // Update base weight
            baseWeight += Math.log(pFT/pFF);

            // Set new weight
            double weight = Math.log(pTT/pTF) - Math.log(pFT/pFF);
            weights.set(i, weight);

            // Reset counters/probabilities
            falseTrue = 0;
            falseFalse = 0;
            trueTrue = 0;
            trueFalse = 0;
            pFT = 0;
            pFF = 0;
            pTT = 0;
            pTF = 0;
        }

        return weights;
    }

    /**
     * Test a list of examples.
     * @param example Example to test against model.
     * @return Label for inputted example.
     */
    public double test(ArrayList<Boolean> example) {
        double result = baseWeight;
        for(int i = 0; i < example.size() - 1; i++) {
            if(example.get(i)) {
                result += trainedWeights.get(i);
            }
        }

        return 1/(1 + Math.exp(-result));
    }

    /**
     * Getter method for attribute information.
     * @return Attributes in string array.
     */
    public String[] getAttributes() {
        return attributes;
    }

    /**
     * Test a list of examples.
     * @param testSet Query examples to test against tree.
     * @return List of predicted labels for test examples.
     */
    public ArrayList<Character> batchQuery(ArrayList<Example> testSet) {
        ArrayList<Character> results = new ArrayList<Character>();
        for (Example e : testSet) {
            results.add(this.test(e.getVector()) > THRESHOLD ? '1' : '0');
        }
        return results;
    }

    @Override
    public String getName() {
        return nbName;
    }

}
