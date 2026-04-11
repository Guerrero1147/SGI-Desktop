package com.sgi.conexion;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;
 
/**
 * @author erice
 */
public class Conexion {
 
    private static final String HOST;
    private static final String PORT;
    private static final String DB;
    private static final String USUARIO;
    private static final String PASSWORD;
    private static final String URL;
 
    static {
        Properties props = new Properties();
        try (InputStream input = Conexion.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró config.properties en el classpath");
            }
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando config.properties: " + e.getMessage());
        }
 
        HOST     = props.getProperty("db.host");
        PORT     = props.getProperty("db.port");
        DB       = props.getProperty("db.name");
        USUARIO  = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
        URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
            + "?ssl-mode=REQUIRED&useSSL=true&serverTimezone=UTC";
    }
 
    public static Connection getConexion() {
        try {
            Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("✅ Conexión exitosa a la base de datos");
            return con;
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar: " + e.getMessage());
            return null;
        }
    }
}