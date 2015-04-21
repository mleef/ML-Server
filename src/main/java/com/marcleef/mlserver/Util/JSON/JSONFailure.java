package com.marcleef.mlserver.Util.JSON;

/**
 * Created by marc_leef on 4/20/15.
 */
public class JSONFailure {
    public String requestFailed;

    public JSONFailure(String code) {
        requestFailed = code;
    }
}