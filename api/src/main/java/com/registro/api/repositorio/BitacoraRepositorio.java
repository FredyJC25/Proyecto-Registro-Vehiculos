package com.registro.api.repositorio;

import com.registro.api.modelo.RegistroBitacora;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class BitacoraRepositorio {

    private static final String INSERTAR =
            "INSERT INTO bitacora (tipo_operacion, datos_recibidos, resultado, id_vehiculo)"
                    + " VALUES (?, ?, ?, ?)";

    private static final int[] TIPOS = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC};

    private final JdbcTemplate jdbcTemplate;

    public BitacoraRepositorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrar(RegistroBitacora registro) {
        Object[] valores = {
                registro.tipoOperacion(),
                registro.datosRecibidos(),
                registro.resultado(),
                registro.idVehiculo()
        };
        jdbcTemplate.update(INSERTAR, valores, TIPOS);
    }
}