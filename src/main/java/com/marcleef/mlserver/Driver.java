package com.marcleef.mlserver;

import com.marcleef.mlserver.MachineLearning.DecisionTree;
import org.json.*;

import static spark.Spark.get;
import static spark.Spark.post;
import java.util.ArrayList;

import spark.Request;
import spark.Response;
import spark.Route;


import com.marcleef.mlserver.Util.*;

/**
 * Created by marc_leef on 4/15/15.
 */
public class Driver {
    public static void main(String[] args) {

        // Route for new decision tree creation.
        post("/build/tree", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                JSONObject obj = new JSONObject(request.body());

                // Get example and attribute lists from JSON.
                JSONArray examples = obj.getJSONArray("examples");
                JSONArray attributes = obj.getJSONArray("attributes");

                // Get name and class value of incoming Decision Tree.
                String name = obj.getString("name");
                String classVariable = attributes.get(attributes.length() - 1).toString();

                // Convert JSON to training set readable by the decision tree class.
                ArrayList<Example> trainingSet = Converter.JSONtoExampleList(attributes, examples);

                // Build decision tree.
                DecisionTree dt = new DecisionTree(trainingSet, classVariable, name, false);

                // Send back info and key to client.
                return dt.getID();
            }
        }, new JsonUtil());
    }


}

