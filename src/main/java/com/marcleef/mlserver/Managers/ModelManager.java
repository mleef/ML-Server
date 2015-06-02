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
    private static final String SQL_SERIALIZE_MODEL_DT = "INSERT INTO dt(username, modelname, serialized_object) VALUES (?, ?, ?)";
    private static final String SQL_SERIALIZE_MODEL_NB = "INSERT INTO nb(username, modelname, serialized_object) VALUES (?, ?, ?)";
    private static final String SQL_DESERIALIZE_MODEL_DT = "SELECT serialized_object FROM dt WHERE username = ? AND modelname = ?";
    private static final String SQL_DESERIALIZE_MODEL_NB = "SELECT serialized_object FROM nb WHERE username = ? AND modelname = ?";


    /**
     * Model manager constructor. MySQL routes must be configured to work with given system.
     * @return New Model Manager.
     */
    public ModelManager() throws ClassNotFoundException,
            SQLException, IOException {
        models = new HashMap<>();
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/ml";
        String username = "root";
        String password = "";
        Class.forName(driver);
        try {
            connection = DriverManager.getConnection(url, username, password);
        }

        catch(SQLException e) {
            System.out.println("Could not connect to MySQL Server, exiting...");
            System.exit(0);
        }
    }

    /**
     * To serialize a java object from database
     * @param m Model to serialize.
     * @param username Username of user submitting model.
     * @param model Type of model to serialize (for table selection purposes).
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static long serializeModelToDB(Model m, String username, String model) throws SQLException {
        PreparedStatement checkUniqueness = null;
        PreparedStatement storeModel = null;

        // Select correct table.
        if(model.equals("dt")) {
            checkUniqueness = connection.prepareStatement(SQL_DESERIALIZE_MODEL_DT);
            storeModel = connection.prepareStatement(SQL_SERIALIZE_MODEL_DT);
        }
        else if(model.equals("nb")) {
            checkUniqueness = connection.prepareStatement(SQL_DESERIALIZE_MODEL_NB);
            storeModel = connection.prepareStatement(SQL_SERIALIZE_MODEL_NB);
        }

        // Fill in statement parameters and execute.
        checkUniqueness.setString(1, username);
        checkUniqueness.setString(2, m.getName());
        ResultSet modelsOfSameName = checkUniqueness.executeQuery();

        // Make sure user isn't trying to make another model of the same name.
        if(modelsOfSameName.next()) {
            throw new SQLException();
        }

        storeModel.setString(1, username);
        storeModel.setString(2, m.getName());
        storeModel.setObject(3, m);
        storeModel.executeUpdate();
        ResultSet rs = storeModel.getGeneratedKeys();
        int serialized_id = -1;
        if (rs.next()) {
            serialized_id = rs.getInt(1);
        }
        rs.close();
        storeModel.close();

        return serialized_id;
    }

    /**
     * To de-serialize a java object from database
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Model deSerializeModelFromDB(String modelname, String username, String model) throws SQLException, IOException,
            ClassNotFoundException {
        PreparedStatement loadModel = null;

        // Select correct table.
        if(model.equals("dt")) {
            loadModel = connection.prepareStatement(SQL_DESERIALIZE_MODEL_DT);
        }
        else if(model.equals("nb")) {
            loadModel = connection.prepareStatement(SQL_DESERIALIZE_MODEL_NB);
        }

        loadModel.setString(1, username);

        loadModel.setString(2, modelname);
        ResultSet rs = loadModel.executeQuery();

        if(rs.next()) {
            byte[] buf = rs.getBytes(1);
            ObjectInputStream objectIn = null;
            if (buf != null)
                objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));

            Model m = (Model) objectIn.readObject();

            rs.close();
            loadModel.close();

            return m;
        }

        throw new SQLException();
    }


}
