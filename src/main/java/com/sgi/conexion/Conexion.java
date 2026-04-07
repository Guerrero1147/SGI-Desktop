/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgi.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author erice
 */
public class Conexion {
    
    private static final String URL      = "jdbc:mysql://localhost:3306/sgi_desktop";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "1234"; 

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
