package com.registro.dao;

import com.registro.modelo.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehiculoDAO {

    private static final String INSERTAR =
            "INSERT INTO vehiculo (marca, modelo, anio, precio, color, medio_registro)"
                    + " VALUES (?, ?, ?, ?, ?, 'BASE_DATOS')";

    public long insertar(Connection conexion, Vehiculo vehiculo) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR, new String[]{"ID_VEHICULO"})) {
            sentencia.setString(1, vehiculo.getMarca());
            sentencia.setString(2, vehiculo.getModelo());
            sentencia.setInt(3, vehiculo.getAnio());
            sentencia.setBigDecimal(4, vehiculo.getPrecio());
            sentencia.setString(5, vehiculo.getColor());
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (llaves.next()) {
                    return llaves.getLong(1);
                }
            }
        }
        throw new SQLException("Oracle no devolvió el id del vehículo registrado");
    }
}