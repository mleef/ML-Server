package com.marcleef.mlserver.Managers;
import com.marcleef.mlserver.MachineLearning.DecisionTree;
import com.marcleef.mlserver.MachineLearning.NaiveBayes;
import com.marcleef.mlserver.Util.JSON.JSONResult;
import com.marcleef.mlserver.Util.JSON.JSONQueryResult;
import com.marcleef.mlserver.Util.JSON.JSONUtil;
import org.json.*;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;


import com.marcleef.mlserver.Util.*;
import com.marcleef.mlserver.MachineLearning.Model;

/**
 * Created by marc_leef on 4/20/15.
 * Manages various SPARK routes.
 */
public final class RouteManager {
    private ModelManager modelManager;
    private UserManager userManager;

    public RouteManager(ModelManager mm, UserManager um) {
        modelManager = mm;
        userManager = um;
    }

    /**
     * Listens for and handles requests to create new decision tree classifier.
     */
    public void decisionTreeBuildListener() {
        // Sample JSON for testing: {"token" : "", "name" : "Test", "attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for new decision tree creation.
        post("/build/decision-tree", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            JSONArray examples;
            JSONArray attributes;
            String modelName;
            String token;
            String classVariable;
            ArrayList<Example> trainingSet;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "examples", "attributes", "model-name", "token");

            if (missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            examples = obj.getJSONArray("examples");
            attributes = obj.getJSONArray("attributes");
            modelName = obj.getString("model-name");
            token = obj.getString("token");


            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if (username != null) {
                // Get class variable value.
                classVariable = attributes.get(attributes.length() - 1).toString();

                // Convert JSON to training set readable by the decision tree class.
                try {
                    trainingSet = Converter.JSONtoExampleList(attributes, examples);
                } catch (JSONException e) {
                    response.status(404);
                    return new JSONResult("Error", "Examples contain non-integer boolean values.");
                }

                // Build decision tree.
                DecisionTree dt = new DecisionTree(trainingSet, classVariable, modelName, false);

                // Save to db.
                try {
                    ModelManager.serializeModelToDB(dt, username, "dt");
                } catch (SQLException e) {
                    response.status(404);
                    return new JSONResult("Error", "Tree name already exists for this account. Choose another name for your model.");
                }

                // Send back results to the client.
                return new JSONResult("Success", "Tree built successfully.");
            } else {
                response.status(401);
                return new JSONResult("Error", "Token has expired or does not exist.");
            }
        }, new JSONUtil());
    }

    /**
     * Listens for and handles requests to query existing decision tree.
     */
    public void decisionTreeQueryListener() {
        // Sample JSON for testing: {"attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for querying existing decision tree.
        post("/query/decision-tree", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");

            JSONArray examples;
            JSONArray attributes;
            JSONObject obj;
            String modelName;
            String token;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "examples", "model-name", "token");

            if(missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            examples = obj.getJSONArray("examples");
            modelName = obj.getString("model-name");
            token = obj.getString("token");


            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if(username != null) {
                // Get decision tree from database.
                try {
                    Model m = ModelManager.deSerializeModelFromDB(modelName, username, "dt");
                    DecisionTree dt;
                    ArrayList<Character> results = new ArrayList<Character>();
                    // Query decision tree and build up result list.
                    if (m instanceof DecisionTree) {
                        dt = (DecisionTree) m;
                        // Read in test examples to query.
                        try {
                            ArrayList<Example> testSet = Converter.toExampleList(dt.getAttributes(), examples);
                            results = dt.batchQuery(testSet);
                        }
                        catch(JSONException e){
                            response.status(404);
                            return new JSONResult("Error", "Non integer values detected in test samples.");
                        }
                    }
                    else {
                        response.status(404);
                        return new JSONResult("Error", "Could not find given tree name.");
                    }
                    // TODO: Error checking on query examples.

                    // Send back results.
                    return new JSONQueryResult(results);
                }

                catch (SQLException e) {
                    response.status(404);
                    return new JSONResult("Error", "Given tree name has not been constructed.");
                }

            }
            else {
                response.status(401);
                return new JSONResult("Error", "Token has expired or does not exist.");
            }


        }, new JSONUtil());
    }

    /**
     * Listens for and handles requests to create new naive bayes classifier.
     */
    public void naiveBayesBuildListener() {
        // Route for new naive bayes creation
        post("/build/naive-bayes", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            JSONArray examples;
            JSONArray attributes;
            String modelName;
            String token;
            String classVariable;
            ArrayList<Example> trainingSet;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            }
            catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "examples", "attributes", "model-name", "token");

            if(missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            examples = obj.getJSONArray("examples");
            attributes = obj.getJSONArray("attributes");
            modelName = obj.getString("model-name");
            token = obj.getString("token");


            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if(username != null) {
                // Get class variable value.
                classVariable = attributes.get(attributes.length() - 1).toString();

                // Convert JSON to training set readable by the decision tree class.
                try {
                    trainingSet = Converter.JSONtoExampleList(attributes, examples);
                }
                catch (JSONException e) {
                    response.status(404);
                    return new JSONResult("Error", "Examples contain non-integer boolean values.");
                }

                // Build decision tree.
                NaiveBayes nb = new NaiveBayes(trainingSet, classVariable, 2, modelName);

                // Save to db.
                try {
                    ModelManager.serializeModelToDB(nb, username, "nb");
                }
                catch(SQLException e) {
                    response.status(404);
                    return new JSONResult("Error", "Model name already exists for this account. Choose another name for your model.");
                }

                // Send back results to the client.
                return new JSONResult("Success", "Naive Bayes model built successfully.");
            }
            else {
                response.status(401);
                return new JSONResult("Error", "Token has expired or does not exist.");
            }
        }, new JSONUtil());
    }


    /**
     * Listens for and handles requests to query existing naive bayes model.
     */
    public void naiveBayesQueryListener() {
        // Sample JSON for testing: {"attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for querying existing decision tree.
        post("/query/naive-bayes", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");

            JSONArray examples;
            JSONArray attributes;
            JSONObject obj;
            String modelName;
            String token;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "examples", "model-name", "token");

            if(missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            examples = obj.getJSONArray("examples");
            modelName = obj.getString("model-name");
            token = obj.getString("token");


            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if(username != null) {
                // Get decision tree from database.
                try {
                    Model m = ModelManager.deSerializeModelFromDB(modelName, username, "nb");
                    NaiveBayes nb;
                    ArrayList<Character> results = new ArrayList<Character>();
                    // Query decision tree and build up result list.
                    if (m instanceof NaiveBayes) {
                        nb = (NaiveBayes) m;
                        // Read in test examples to query.
                        try {
                            ArrayList<Example> testSet = Converter.toExampleList(nb.getAttributes(), examples);
                            results = nb.batchQuery(testSet);
                        }
                        catch(JSONException e){
                            response.status(404);
                            return new JSONResult("Error", "Non integer values detected in test samples.");
                        }
                    }
                    else {
                        response.status(404);
                        return new JSONResult("Error", "Internal database error.");
                    }
                    // TODO: Error checking on query examples.

                    // Send back results.
                    return new JSONQueryResult(results);
                }

                catch (SQLException e) {
                    response.status(404);
                    return new JSONResult("Error", "Given naive bayes model name has not been constructed.");
                }

            }
            else {
                response.status(401);
                return new JSONResult("Error", "Token has expired or does not exist.");
            }


        }, new JSONUtil());
    }
    /**
     * Listens for and handles requests add a new user to the database.
     */
    public void newUserListener() {
        post("/user/register", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            String username = "";
            String password = "";

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "username", "password");

            if(missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            username = obj.getString("username");
            password = obj.getString("password");

            if(username.length() > 0 && password.length() > 0) {
                return userManager.registerNewUser(username, password);
            }
            else {
                return new JSONResult("Error", "0 length username or password.");
            }

        }, new JSONUtil());

    }

    /**
     * Listens for and handles requests login users and generate tokens.
     */
    public void userLoginListener() {
        post("/user/login", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            String username = "";
            String password = "";

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            ArrayList<String> missingParams = validateJSON(obj, "username", "password");

            if(missingParams.size() > 0) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain required properties: " + missingParams.toString());
            }

            username = obj.getString("username");
            password = obj.getString("password");

            // Return new token to user.
            if(username.length() > 0 && password.length() > 0) {
                try {
                    return userManager.loginUser(username, password);
                }
                catch(SQLException e) {
                    response.status(404);
                    return new JSONResult("Error", "Incorrect username/password combination or still active token.");
                }
            }
            else {
                return new JSONResult("Error", "0 length username or password.");
            }



        }, new JSONUtil());

    }

    /**
     * Helper method to verify incoming JSON has the correct parameters for the given listener.
     * @param obj Incoming JSONObject.
     * @param params Variable length list of required params for the given piece of JSON.
     * @return Missing parameters.
     */
    private ArrayList<String> validateJSON(JSONObject obj, String... params) {

        ArrayList<String> missingParams = new ArrayList<>();
        for(String param : params) {
            try {
                obj.get(param);
            }
            catch (JSONException e) {
                missingParams.add(param);
            }
        }
        return missingParams;

    }
}
