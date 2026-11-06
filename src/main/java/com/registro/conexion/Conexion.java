package com.registro.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
    private static final String URL_SIMULACION_FALLA = "jdbc:oracle:thin:@localhost:1599/FREEPDB1";
    private static final String USUARIO = "vehiculos";
    private static final String CONTRASENA = "vehiculos123";
    private static final String TIEMPO_ESPERA_MS = "5000";

    private Conexion() {
    }

    public static Connection obtenerConexion(boolean simularFalla) throws SQLException {
        Properties propiedades = new Properties();
        propiedades.setProperty("user", USUARIO);
        propiedades.setProperty("password", CONTRASENA);
        propiedades.setProperty("oracle.net.CONNECT_TIMEOUT", TIEMPO_ESPERA_MS);
        return DriverManager.getConnection(simularFalla ? URL_SIMULACION_FALLA : URL, propiedades);
    }
}