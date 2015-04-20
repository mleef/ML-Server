package com.marcleef.mlserver.Util.JSON;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by marc_leef on 4/19/15.
 * Object container for model generation and future usage.
 */
public class JSONid {
    private String name;
    private String key;
    private String timeCreated;
    private String timeElapsed;
    public JSONid(String n, UUID identifier, long time) {
        name = n;
        java.util.Date date = new java.util.Date();
        timeCreated = new Timestamp(date.getTime()).toString();
        key = String.valueOf(identifier);
        timeElapsed = String.valueOf(time);
    }
}
