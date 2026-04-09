/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgi.sgi.desktop;

import com.sgi.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
/**
 *
 * @author erice
 */
public class LoginController {
    
@FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        // Validar que no estén vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Favor de llenar los campos.");
            return;
        }

        // Verificar contra la base de datos
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = TRUE";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Por ahora comparamos la contraseña directamente
                // (más adelante agregaremos BCrypt)
                String passwordDB = rs.getString("password");
                if (password.equals(passwordDB)) {
                    String rol = rs.getString("rol");
                    String nombre = rs.getString("nombre");
                    lblError.setStyle("-fx-text-fill: #66bb6a;");
                    Stage stageLogin = (Stage) btnLogin.getScene().getWindow();
                    stageLogin.close();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgi/sgi/desktop/Dashboard.fxml"));
                    Parent root = loader.load();
                    DashboardController dashboard = loader.getController();
                    dashboard.setUsuario(nombre, rol);
                    Stage stageDashboard = new Stage();
                    stageDashboard.setTitle("SGI-Desktop | Dashboard");
                    stageDashboard.setScene(new Scene(root, 1000, 650));
                    stageDashboard.show();
                } else {
                    lblError.setStyle("-fx-text-fill: #ef5350;");
                    lblError.setText("Contraseña incorrecta.");
                }
            } else {
                lblError.setStyle("-fx-text-fill: #ef5350;");
                lblError.setText("Usuario no encontrado.");
            }

            rs.close();
            ps.close();

        } catch (Exception e) {
            lblError.setStyle("-fx-text-fill: #ef5350;");
            lblError.setText("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }
}
