package com.marcleef.mlserver.Managers;

import com.marcleef.mlserver.MachineLearning.DecisionTree;
import com.marcleef.mlserver.MachineLearning.Model;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import java.util.HashMap;



/**
 * Created by marc_leef on 4/20/15.
 * Manages the saving, loading, and validation of models.
 */
public final class ModelManager {
    private static Connection connection;
    private HashMap<String, Model> models;
    private static final String SQL_SERIALIZE_MODEL = "INSERT INTO dt(username, treename, serialized_object) VALUES (?, ?, ?)";
    private static final String SQL_DESERIALIZE_MODEL = "SELECT serialized_object FROM dt WHERE username = ? AND treename = ?";

    public ModelManager() throws ClassNotFoundException,
            SQLException, IOException {
        models = new HashMap<>();
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/ml";
        String username = "root";
        String password = "";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
    }
    public static long serializeModelToDB(Model m, String username, String treename) throws SQLException {

        PreparedStatement pstmt = connection
                .prepareStatement(SQL_SERIALIZE_MODEL);

        pstmt.setString(1, username);
        pstmt.setString(2, treename);
        pstmt.setObject(3, m);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        int serialized_id = -1;
        if (rs.next()) {
            serialized_id = rs.getInt(1);
        }
        rs.close();
        pstmt.close();
        System.out.println("Saved serialized model to database.");
        return serialized_id;
    }

    /**
     * To de-serialize a java object from database
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Model deSerializeModelFromDB(String treename, String username) throws SQLException, IOException,
            ClassNotFoundException {
        PreparedStatement pstmt = connection
                .prepareStatement(SQL_DESERIALIZE_MODEL);
        pstmt.setString(1, username);

        //TODO: Get username using token.
        pstmt.setString(2, treename);
        ResultSet rs = pstmt.executeQuery();
        rs.next();


        byte[] buf = rs.getBytes(1);
        ObjectInputStream objectIn = null;
        if (buf != null)
            objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));

        Model m = (Model) objectIn.readObject();

        rs.close();
        pstmt.close();

        System.out.println("Java object de-serialized from database. ");
        return m;
    }

    //TODO: Save attributes in database so user doesn't need to send them everytime.

}
