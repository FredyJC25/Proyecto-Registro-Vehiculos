package com.registro.api.repositorio;

import com.registro.api.modelo.Vehiculo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

@Repository
public class VehiculoRepositorio {

    private static final String INSERTAR =
            "INSERT INTO vehiculo (marca, modelo, anio, precio, color, medio_registro)"
                    + " VALUES (?, ?, ?, ?, ?, 'API')";

    private final JdbcTemplate jdbcTemplate;

    public VehiculoRepositorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertar(Vehiculo vehiculo) {
        KeyHolder llaveGenerada = new GeneratedKeyHolder();
        jdbcTemplate.update(conexion -> {
            PreparedStatement sentencia = conexion.prepareStatement(INSERTAR, new String[]{"ID_VEHICULO"});
            sentencia.setString(1, vehiculo.marca());
            sentencia.setString(2, vehiculo.modelo());
            sentencia.setInt(3, vehiculo.anio());
            sentencia.setBigDecimal(4, vehiculo.precio());
            sentencia.setString(5, vehiculo.color());
            return sentencia;
        }, llaveGenerada);
        Number id = llaveGenerada.getKey();
        if (id == null) {
            throw new IllegalStateException("Oracle no devolvió el id del vehículo registrado");
        }
        return id.longValue();
    }
}