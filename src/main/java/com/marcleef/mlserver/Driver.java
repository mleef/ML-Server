package com.marcleef.mlserver;
import com.marcleef.mlserver.Managers.ModelManager;
import com.marcleef.mlserver.Managers.RouteManager;
import com.marcleef.mlserver.Managers.UserManager;

import static spark.Spark.post;

/**
 * Created by marc_leef on 4/15/15.
 */


public class Driver {
    public static void main(String[] args) {

        // Create new route and model managers.
        try {
            RouteManager rm = new RouteManager(new ModelManager(), new UserManager());
            // Listen for POST requests for new user registration.
            rm.newUserListener();

            // Listen for POST requests for user login.
            rm.userLoginListener();

            // Listen for POST requests for decision tree creation.
            rm.decisionTreeBuildListener();

            // Listen for POST requests for tree querying.
            rm.decisionTreeQueryListener();

            // Listen for POST requests for naive bayes creation.
            rm.naiveBayesBuildListener();

            // Listen for POST requests for naive bayes querying.
            rm.naiveBayesQueryListener();
        }
        catch (Exception e) {

        }


    }


}

