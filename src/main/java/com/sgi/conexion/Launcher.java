package com.sgi.sgi.desktop;

/**
 * Clase lanzadora separada de App.
 * Necesaria para que el fat JAR funcione con JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
