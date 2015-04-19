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
        Converter conv = new Converter();
        post("/build/tree", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                //response.status(201);
                response.header("Access-Control-Allow-Origin", "*");
                JSONObject obj = new JSONObject(request.body());
                JSONArray examples = obj.getJSONArray("examples");
                JSONArray attributes = obj.getJSONArray("attributes");
                DecisionTree dt = new DecisionTree(conv.JSONtoExampleList(attributes, examples), attributes.get(attributes.length() - 1).toString(), false);
                return dt.getNodes();
            }
        }, new JsonUtil());
    }


}

