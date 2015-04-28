package com.marcleef.mlserver.Util.JSON;

/**
 * Created by marc_leef on 4/27/15.
 */
public class JSONResult {
    private String status;
    private String message;

    public JSONResult(String s, String m) {
        status = s;
        message = m;
    }
}
