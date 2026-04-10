package com.pidev.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myconnexion {
    private final   String url="jdbc:mysql://localhost:3306/javafx_Pidev";
    private   final   String user ="root";
    private   final String pws ="";

    private Connection connection;
    private static myconnexion instance;
    private myconnexion(){
        try {
            connection= DriverManager.getConnection(url,user,pws);
            System.out.println("connecter a la base de données");
        } catch (SQLException e) {
            System.err.println(e.getMessage());    }
    }
    public static myconnexion getInstance(){
        if (instance==null){
            instance= new myconnexion();

        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

