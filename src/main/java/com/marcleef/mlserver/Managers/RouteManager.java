package com.marcleef.mlserver.Managers;
import com.marcleef.mlserver.MachineLearning.DecisionTree;
import com.marcleef.mlserver.Util.JSON.JSONResult;
import com.marcleef.mlserver.Util.JSON.JSONQueryResult;
import com.marcleef.mlserver.Util.JSON.JSONUtil;
import org.json.*;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;


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
     * Listens for and handles requests to create new decision tree.
     */
    public void decisionTreeBuildListener() {
        // Sample JSON for testing: {"token" : "", "name" : "Test", "attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"]}
        // Route for new decision tree creation.
        post("/build/decision-tree", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            JSONObject obj;
            JSONArray examples;
            JSONArray attributes;
            String treeName;
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

            // Get example and attribute lists from JSON.
            try {
                examples = obj.getJSONArray("examples");
                attributes = obj.getJSONArray("attributes");
            }
            // Make sure the appropriate properties are specified in the request.
            catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain examples or attributes property.");
            }

            // Get name of the incoming Decision Tree.
            try {
                treeName = obj.getString("name");
            }
            catch(JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain name property.");
            }

            // Get API key from request.
            try {
                token = obj.getString("token");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "No token included.");
            }

            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if(!username.equals("")) {
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
                DecisionTree dt = new DecisionTree(trainingSet, classVariable, treeName, false);

                // Save to db.
                ModelManager.serializeModelToDB(dt, username, treeName);

                // Send back results to the client.
                return new JSONResult("Success", "Tree built successfully.");
            }
            else {
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
            String treeName;
            String token;

            // Get JSONObject for easy parsing.
            try {
                obj = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Invalid JSON.");
            }

            // Get example and attribute lists from JSON.
            try {
                examples = obj.getJSONArray("examples");
                attributes = obj.getJSONArray("attributes");
            }
            // Make sure the appropriate properties are specified in the request.
            catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Request did not contain examples or attributes property.");
            }

            // Get tree name from request.
            try {
                treeName = obj.getString("name");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "No name attribute specified.");
            }

            // Get API key from request.
            try {
                token = obj.getString("token");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "No token included.");
            }

            // Authenticate token.
            String username = userManager.authenticateUser(token);
            if(!username.equals("")) {
                // Get decision tree from database.
                try {
                    Model m = ModelManager.deSerializeModelFromDB(treeName, username);
                    DecisionTree dt;
                    ArrayList<Character> results = new ArrayList<Character>();
                    // Query decision tree and build up result list.
                    if (m instanceof DecisionTree) {
                        dt = (DecisionTree) m;
                        // Read in test examples to query.
                        ArrayList<Example> testSet = Converter.JSONtoExampleList(attributes, examples);
                        for (Example e : testSet) {
                            results.add(dt.testExample(e) ? '1' : '0');
                        }
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

            // Get username.
            try {
                username = obj.getString("username");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Missing username.");
            }

            // Get username.
            try {
                password = obj.getString("password");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Missing password.");
            }

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

            // Get username.
            try {
                username = obj.getString("username");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Missing username.");
            }

            // Get username.
            try {
                password = obj.getString("password");
            } catch (JSONException e) {
                response.status(404);
                return new JSONResult("Error", "Missing password.");
            }

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


}
