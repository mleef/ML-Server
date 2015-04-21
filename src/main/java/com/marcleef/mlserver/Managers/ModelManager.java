package com.marcleef.mlserver.Managers;

import com.marcleef.mlserver.MachineLearning.Model;
import com.sun.xml.internal.bind.v2.TODO;

import java.util.HashMap;

/**
 * Created by marc_leef on 4/20/15.
 * Manages the saving, loading, and validation of models.
 */
public final class ModelManager {
    private HashMap<String, Model> models;

    public ModelManager() {
        models = new HashMap<>();
    }

    //TODO: Add, remove, and update various models in the map. Will move to a database in the future.
}
