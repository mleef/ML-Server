package com.marcleef.mlserver.Util;
import com.google.gson.*;
import spark.ResponseTransformer;
/**
 * Created by marc_leef on 4/19/15.
 */
public class JsonUtil implements ResponseTransformer {
    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

}
