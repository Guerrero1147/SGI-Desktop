/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgi.sgi.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author erice
 */
public class App extends Application {
 
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/sgi/sgi/desktop/Login.fxml"));
        Scene scene = new Scene(root, 720, 500);
        stage.setTitle("SGI-Desktop | Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}