package com.marcleef.mlserver;

import com.marcleef.mlserver.MachineLearning.DecisionTree;
import org.json.*;

import static spark.Spark.get;
import static spark.Spark.post;
import java.util.ArrayList;

import spark.Request;
import spark.Response;
import spark.Route;

import com.marcleef.mlserver.Util.Converter;
import com.marcleef.mlserver.Util.Example;
import com.marcleef.mlserver.MachineLearning.DecisionTree;
/**
 * Created by marc_leef on 4/15/15.
 */
public class Driver {
    public static void main(String[] args) {
        Converter conv = new Converter();
// http://localhost:4567/build/tree
/*        {
            "examples": [
            "1,0,0",
                    "1,1,1"
            ],
            "attributes": [
            "x",
                    "y",
                    "z"
            ]
        }*/

        post("/build/tree", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                //response.status(201);
                JSONObject obj = new JSONObject(request.body());
                JSONArray examples = obj.getJSONArray("examples");
                JSONArray attributes = obj.getJSONArray("attributes");
                DecisionTree dt = new DecisionTree(conv.JSONtoExampleList(attributes, examples), attributes.get(attributes.length() - 1).toString(), false);
                return dt.getModel();
            }
        });
    }


}

