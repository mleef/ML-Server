package com.marcleef.mlserver.Managers;
import com.marcleef.mlserver.MachineLearning.DecisionTree;
import com.marcleef.mlserver.Util.JSON.JSONFailure;
import com.marcleef.mlserver.Util.JSON.JSONUtil;
import org.json.*;

import static spark.Spark.get;
import static spark.Spark.post;
import java.util.ArrayList;


import com.marcleef.mlserver.Util.*;
import spark.Request;
import spark.Response;

/**
 * Created by marc_leef on 4/20/15.
 * Manages various SPARK routes.
 */
public final class RouteManager {
    private ModelManager manager;

    public RouteManager(ModelManager mm) {
        manager = mm;
    }

    /**
     * Listens for and handles requests to create new decision tree.
     */
    public void decisionTreeBuildListener() {
        // Sample JSON for testing: {"name" : "Test Tree", "attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for new decision tree creation.
        post("/build/decision-tree", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            JSONArray examples;
            JSONArray attributes;
            String name;
            String classVariable;
            ArrayList<Example> trainingSet;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            }
            catch (JSONException e) {
                response.status(404);
                return new JSONFailure(("Invalid JSON."));
            }

            // Get example and attribute lists from JSON.
            try {
                examples = obj.getJSONArray("examples");
                attributes = obj.getJSONArray("attributes");
            }
            // Make sure the appropriate properties are specified in the request.
            catch (JSONException e) {
                response.status(404);
                return new JSONFailure(("Request did not contain examples or attributes property."));
            }

            // Get name of the incoming Decision Tree.
            try {
                name = obj.getString("name");
            }
            catch(JSONException e) {
                response.status(404);
                return new JSONFailure(("Request did not contain name property."));
            }

            // Get class variable value.
            classVariable = attributes.get(attributes.length() - 1).toString();

            // Convert JSON to training set readable by the decision tree class.
            try {
                trainingSet = Converter.JSONtoExampleList(attributes, examples);
            }
            catch (JSONException e) {
                response.status(404);
                return new JSONFailure(("Examples contain non-integer boolean values."));
            }

            // Build decision tree.
            DecisionTree dt = new DecisionTree(trainingSet, classVariable, name, false);

            // Save to db.
            manager.serializeModelToDB(dt);

            // Send back info and key to client.
            return dt.getID();
        }, new JSONUtil());
    }

    /**
     * Listens for and handles requests to query existing decision tree.
     */
    public void decisionTreeQueryListener() {
        // Sample JSON for testing: {"attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for querying existing decision tree.
        get("/query/decision-tree/*", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");

            // Get name of tree from route.
            String treeName = request.splat()[0];
            JSONArray examples;
            JSONArray attributes;
            JSONObject obj;
            String key;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            }
            catch (JSONException e) {
                response.status(404);
                return new JSONFailure(("Invalid JSON."));
            }

            // Get example and attribute lists from JSON.
            try {
                examples = obj.getJSONArray("examples");
                attributes = obj.getJSONArray("attributes");
            }
            // Make sure the appropriate properties are specified in the request.
            catch (JSONException e) {
                response.status(404);
                return new JSONFailure(("Request did not contain examples or attributes property."));
            }

            // Get API key from request.
            try {
                key = obj.getString("key");
            }
            catch(JSONException e) {
                response.status(404);
                return new JSONFailure(("Invalid key."));
            }

            ArrayList<Example> testSet = Converter.JSONtoExampleList(attributes, examples);

            // TODO: Confirm validity of name/key combination, query tree, build result JSON, send back to client.
            return null;
        }, new JSONUtil());
    }


}
