package com.marcleef.mlserver.MachineLearning;


/**
 * Created by marc_leef on 4/20/15.
 * Super class for various machine learning models/algorithms.
 */
public abstract class Model {
    private String name;
    private String key;

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
