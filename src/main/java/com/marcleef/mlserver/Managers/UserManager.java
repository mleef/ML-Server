package com.marcleef.mlserver.Managers;
import com.marcleef.mlserver.Util.JSON.JSONResult;
import com.marcleef.mlserver.Util.JSON.Token;

import java.io.IOException;
import java.sql.*;
import java.util.Date;

import java.util.HashMap;

/**
 * Created by marc_leef on 4/27/15.
 * Manage user registration, authentication, and token generation.
 */
public final class UserManager {
    private Connection connection;

    private static final int DAY_LENGTH = 86400000;
    private static final String SQL_GET_USER = "SELECT * FROM user WHERE username = ?";
    private static final String SQL_GET_USER_BY_ID = "SELECT * FROM user WHERE id = ?";
    private static final String SQL_GET_TOKEN = "SELECT * FROM authToken WHERE token = ?";
    private static final String SQL_NEW_TOKEN = "INSERT INTO authToken(userId, token, expires) VALUES (?, ?, ?)";
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
                .prepareStatement(SQL_GET_USER, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        checkAvailibility.setString(1, name);
        ResultSet rs = checkAvailibility.executeQuery();
        if(rs.next()) {
            rs.close();
            return new JSONResult("Error", "Username already exists.");
        }

        // If username is unique, generate timestamp and insert new user into database.
        Date date= new java.util.Date();
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
        rs.close();

        return new JSONResult("Success", "User registered.");
    }


    public Token loginUser(String name, String password) throws SQLException {
        // Check for uniqueness of user name
        PreparedStatement checkAvailibility = connection
                .prepareStatement(SQL_GET_USER, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        checkAvailibility.setString(1, name);
        ResultSet rs = checkAvailibility.executeQuery();

        // Validate existence of some return data.
        if(rs.next()) {
            // Check password is valid.
            if(rs.getString("password").equals(password)) {
                // Get user ID to update authentication table.
                int userID = rs.getInt("id");
                // Update last login of user.
                Date curDate= new java.util.Date();
                Timestamp curTS = new Timestamp(curDate.getTime());
                rs.updateTimestamp("lastLogin", curTS);
                rs.updateRow();
                // Add a day to the current time.
                Timestamp expTS = new Timestamp(curDate.getTime() + DAY_LENGTH);

                // Generate new token and set expiration.
                Token t = new Token(expTS.toString());
                // Place new token with user id and timestamp into DB.
                PreparedStatement newToken = connection
                        .prepareStatement(SQL_NEW_TOKEN);
                newToken.setInt(1, userID);
                newToken.setString(2, t.getKey());
                newToken.setTimestamp(3, expTS);
                newToken.execute();
                // Close statement and result set.
                rs.close();
                checkAvailibility.close();
                newToken.close();

                // Return new token.
                return t;
            }
            else {
                throw new SQLException();
            }
        }
        else {
            System.out.println("here");
            throw new SQLException();
        }

    }

    public String authenticateUser(String token) throws SQLException{

        // Check for token.
        PreparedStatement authenticate = connection
                .prepareStatement(SQL_GET_TOKEN);
        authenticate.setString(1, token);
        ResultSet rs = authenticate.executeQuery();

        int userID;

        Date curDate= new java.util.Date();
        if(rs.next()) {
            // Check if token has expired.
            if(getCurrentTime().before(rs.getTimestamp("expires"))) {
                userID = rs.getInt("userId");
                // Find user by ID.
                PreparedStatement findUser = connection
                        .prepareStatement(SQL_GET_USER_BY_ID);
                findUser.setInt(1, userID);
                ResultSet results = findUser.executeQuery();
                if(results.next()) {
                    // Return username.
                    return results.getString("username");
                }
                else {
                    return "";
                }
            }
            else {
                return "";
            }
        }
        else {
            return "";
        }
    }

    private static Timestamp getCurrentTime() {
        Date curDate= new java.util.Date();
        return new Timestamp(curDate.getTime());
    }


}
