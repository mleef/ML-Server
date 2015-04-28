package com.marcleef.mlserver.Util.JSON;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.Date;
import java.io.Serializable;
/**
 * Created by marc_leef on 4/19/15.
 * Object container for model generation and future usage.
 */
public class Token implements Serializable {
    private String key;
    private String expires;
    public Token(String exp) {
        key = String.valueOf(UUID.randomUUID()).replace("-","");
        expires = exp;
    }

    public String getKey() {
        return key;
    }
}
