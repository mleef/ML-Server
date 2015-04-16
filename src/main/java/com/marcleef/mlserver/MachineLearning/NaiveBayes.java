package com.marcleef.mlserver.MachineLearning;

/**
 * Created by marc_leef on 4/15/15.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.marcleef.mlserver.Util.Example;

public class NaiveBayes {
    ArrayList<ArrayList<Boolean>> Examples;
    static ArrayList<Double> trainedWeights;
    private double baseWeight;
    private static String classVariable;
    private static final double MAGNITUDE_THRESHOLD = .00001;

    public NaiveBayes(ArrayList<Example> s, String classVar, double beta) {
        Examples = new ArrayList<ArrayList<Boolean>>();
        for(int i = 0; i < s.size(); i++) {
            Examples.add(s.get(i).getVector());
        }
        classVariable = classVar;
        trainedWeights = learn(Examples, beta);
    }

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

    public double test(ArrayList<Boolean> Example) {
        double result = baseWeight;
        for(int i = 0; i < Example.size() - 1; i++) {
            if(Example.get(i)) {
                result += trainedWeights.get(i);
            }
        }

        return 1/(1 + Math.exp(-result));
    }


    public void writeModel(File f, String[] attributes) throws IOException {

        FileWriter fWriter = new FileWriter(f);
        PrintWriter pWriter = new PrintWriter(fWriter);
        pWriter.println(baseWeight);
        for(int i = 0; i < attributes.length - 1; i++) {
            pWriter.println(attributes[i] + " " + trainedWeights.get(i));
        }
        pWriter.close();
        fWriter.close();
    }

}
