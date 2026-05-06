package esprit.tn.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myconnexion {

    //DB properties
    final String URL = "jdbc:mysql://localhost:3306/db_formation?useSSL=false&serverTimezone=UTC";
    final String USR = "root";
    final String PWD = "";

    //Attributes
    //2. static instance
    static myconnexion instance = null;
    Connection cnx;

    public static myconnexion getInstance() {
        //3 verif
        if (instance == null) {
            instance = new myconnexion();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    //constructor
    //1 : Privatisation du constructeur
    private myconnexion(){
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            cnx = DriverManager.getConnection(URL, USR, PWD);
            System.out.println("Connexion établie avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
            System.err.println("Vérifiez que mysql-connector-java est dans le classpath");
            cnx = null;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            System.err.println("Vérifiez que MySQL est démarré et que la base db_formation existe");
            cnx = null;
        }
    }
}
