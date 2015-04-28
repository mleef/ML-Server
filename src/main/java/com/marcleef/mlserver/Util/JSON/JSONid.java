package com.marcleef.mlserver.Util.JSON;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.Date;
import java.io.Serializable;
/**
 * Created by marc_leef on 4/19/15.
 * Object container for model generation and future usage.
 */
public class JSONid implements Serializable {
    private String name;
    private String key;
    private String timeCreated;
    private String timeElapsed;
    public JSONid(String n, UUID identifier) {
        name = n;
        key = String.valueOf(identifier);
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key.replace("-", "");
    }
}
