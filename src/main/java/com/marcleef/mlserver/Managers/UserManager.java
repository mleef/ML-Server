package com.marcleef.mlserver.Managers;
import com.marcleef.mlserver.MachineLearning.DecisionTree;
import com.marcleef.mlserver.MachineLearning.Model;
import com.marcleef.mlserver.Util.JSON.JSONResult;
import com.marcleef.mlserver.Util.JSON.Token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.Date;

import java.util.HashMap;

/**
 * Created by marc_leef on 4/27/15.
 * Manage user registration, authentication, and token generation.
 */
public final class UserManager {
    private Connection connection;

    private static final String SQL_GET_USER = "SELECT * FROM user WHERE username = ?";
    private static final String SQL_NEW_USER = "INSERT INTO user(username, password, lastLogin, modified, created) VALUES (?, ?, ?, ?, ?)";

    public UserManager() throws ClassNotFoundException,
            SQLException, IOException {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/ml";
        String username = "root";
        String password = "";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
    }

    public JSONResult registerNewUser(String name, String password) throws SQLException {

        // Check for uniqueness of user name
        PreparedStatement checkAvailibility = connection
                .prepareStatement(SQL_GET_USER);
        checkAvailibility.setString(1, name);
        checkAvailibility.execute();
        ResultSet rs = checkAvailibility.getResultSet();
        if(rs.next()) {
            return new JSONResult("Error", "Username already exists.");
        }

        // If username is unique, generate timestamp and insert new user into database.
        java.util.Date date= new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());
        PreparedStatement pstmt = connection
                .prepareStatement(SQL_NEW_USER);

        // Populate statement with name, password, and time stamps.
        pstmt.setString(1, name);
        pstmt.setString(2, password);
        pstmt.setTimestamp(3, ts);
        pstmt.setTimestamp(4, ts);
        pstmt.setTimestamp(5, ts);
        pstmt.executeUpdate();
        pstmt.close();

        return new JSONResult("Success", "User registered.");
    }


    public Token loginUser(String name, String password) throws SQLException {
        // Check for uniqueness of user name
        PreparedStatement checkAvailibility = connection
                .prepareStatement(SQL_GET_USER);
        checkAvailibility.setString(1, name);
        checkAvailibility.execute();
        ResultSet rs = checkAvailibility.getResultSet();

        // Validate password.
        if(rs.next()) {
            if(rs.getString("password").equals(password)) {
                // Update last login of user.
                java.util.Date curDate= new java.util.Date();
                Timestamp curTS = new Timestamp(curDate.getTime());
                rs.updateTimestamp("lastLogin", curTS);

                // Add a day to the current time.
                Timestamp expTS = new Timestamp(curDate.getTime() + (1000 * 60 * 60 * 24));
                Token t = new Token(expTS.toString());
                return t;
            }
            else {
                throw new SQLException();
            }
        }
        else {
            throw new SQLException();
        }

    }

    //TODO: Validate token method.
    public String authenticateUser(String token) {

        //should return username if found and valid, otherwise null.
        return null;
    }

}
