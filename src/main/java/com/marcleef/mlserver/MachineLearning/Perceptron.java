package com.marcleef.mlserver.MachineLearning;

/**
 * Created by marc_leef on 4/15/15.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.marcleef.mlserver.Util.Example;


public class Perceptron {
    ArrayList<ArrayList<Boolean>> Examples;
    static ArrayList<Double> trainedWeights;
    private static final double THRESHOLD = .5;
    private double bias = 0;

    private static String classVariable;
    public Perceptron(ArrayList<Example> s, String classVar, double learningRate, boolean permute) {
        Examples = new ArrayList<ArrayList<Boolean>>();
        for(int i = 0; i < s.size(); i++) {
            Examples.add(s.get(i).getVector());
        }
        classVariable = classVar;
        trainedWeights = learn(Examples, learningRate, permute);
    }


    public ArrayList<Double> learn(ArrayList<ArrayList<Boolean>> s, double learningRate, boolean permute) {
        int lastIndex = s.get(0).size() - 1;
        //int errorCount = 0;
        double b = 0;
        double a = 0;
        double y = 0;
        int numMistakes = 0;
        long seed = System.nanoTime();

        // Initialize weights to 0
        ArrayList<Double> weights = new ArrayList<Double>();
        for(int i = 0; i < s.get(0).size() - 1; i++) {
            weights.add(0.0);
        }
        // Run perceptron 100 times
        for(int i = 0; i < 100; i++) {
            for(ArrayList<Boolean> Example : s) {
                a = dotProduct(weights, Example) + b;
                y = Example.get(lastIndex) ? 1.0 : -1.0;
                System.out.println("Activation: " + a);
                System.out.println("Y: " + y);
                if(y * a <= 0) {
                    System.out.println("Mistake Made");
                    numMistakes++;
                    for(int j = 0; j < weights.size(); j++) {
                        weights.set(j, weights.get(j) + y * learningRate * (Example.get(j) ? 1.0 : 0.0));
                    }
                    b = b + (y * learningRate);
                    System.out.println("Weights: ");
                    for(int k = 0; k < weights.size(); k++) {
                        System.out.print(weights.get(k) + ",");
                    }
                    System.out.println();
                    System.out.println("Bias: " + b);
                }
            }
            if(numMistakes == 0) {
                break;
            }
            else {
                numMistakes = 0;
            }
            // Permute examples
            if(permute) {
                Collections.shuffle(s, new Random(seed));
            }

        }
        bias = b;
        return weights;
    }

    public double dotProduct(ArrayList<Double> weights, ArrayList<Boolean> example) {
        double result = 0.0;
        for(int i = 0; i < weights.size(); i++) {
            if(example.get(i)) {
                result += weights.get(i);
            }
        }
        //System.out.println("Dot Product: " + result);
        return result;
    }

    public boolean perceptronTest(ArrayList<Boolean> Example, boolean print) {
        double result = 0.0;
        for(int i = 0; i < trainedWeights.size(); i++) {
            if(Example.get(i)) {
                result += trainedWeights.get(i);
            }
        }
        if(print) System.out.println(result + bias);
        return result + bias >= 0;
    }

    public void writeModel(File f, String[] attributes) throws IOException {
        FileWriter fWriter = new FileWriter(f);
        PrintWriter pWriter = new PrintWriter(fWriter);
        pWriter.println(bias);
        for(int i = 0; i < attributes.length - 1; i++) {
            pWriter.println(attributes[i] + " " + trainedWeights.get(i));
        }
        pWriter.close();
        fWriter.close();
    }

}
