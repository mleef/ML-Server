package com.marcleef.mlserver.Util.JSON;
import com.google.gson.*;
import spark.ResponseTransformer;
/**
 * Created by marc_leef on 4/19/15.
 * Convert SPARK return values to JSON by overring render method.
 */
public class JSONUtil implements ResponseTransformer {
    private Gson gson = new Gson();

    // Override render to return JSON.
    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

}
