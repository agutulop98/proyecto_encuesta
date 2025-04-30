package Encuesta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_encuestas";
    private static final String USER = "root"; 
    private static final String PASSWORD = "root1234"; 

    public static Connection getConexion() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            return null;
        }
    }
}