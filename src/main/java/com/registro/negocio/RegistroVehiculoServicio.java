package com.registro.negocio;

import com.registro.conexion.Conexion;
import com.registro.dao.VehiculoDAO;
import com.registro.modelo.MedioRegistro;
import com.registro.modelo.ResultadoRegistro;
import com.registro.modelo.Vehiculo;
import com.registro.servicio.VehiculoApiCliente;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientConnectionException;
import java.time.Year;

public class RegistroVehiculoServicio {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("9999999999.99");

    private final VehiculoDAO dao = new VehiculoDAO();
    private final VehiculoApiCliente apiCliente = new VehiculoApiCliente();

    public Vehiculo crearVehiculo(String marca, String modelo, String anio, String precio, String color) {
        String marcaValida = requerido(marca, "La marca", "obligatoria", 50);
        String modeloValido = requerido(modelo, "El modelo", "obligatorio", 50);
        int anioValido = validarAnio(anio);
        BigDecimal precioValido = validarPrecio(precio);
        String colorValido = requerido(color, "El color", "obligatorio", 30);
        return new Vehiculo(marcaValida, modeloValido, anioValido, precioValido, colorValido);
    }

    public ResultadoRegistro registrar(Vehiculo vehiculo, boolean simularFalla)
            throws SQLException, IOException, InterruptedException {
        Connection conexion;
        try {
            conexion = Conexion.obtenerConexion(simularFalla);
        } catch (SQLException errorConexion) {
            return registrarMedianteApi(vehiculo, errorConexion);
        }
        try (conexion) {
            long id = dao.insertar(conexion, vehiculo);
            return new ResultadoRegistro(MedioRegistro.BASE_DATOS, id, null);
        } catch (SQLException error) {
            if (esFallaDeConexion(error)) {
                return registrarMedianteApi(vehiculo, error);
            }
            throw error;
        }
    }

    private ResultadoRegistro registrarMedianteApi(Vehiculo vehiculo, SQLException errorConexion)
            throws IOException, InterruptedException {
        try {
            long id = apiCliente.registrar(vehiculo);
            return new ResultadoRegistro(MedioRegistro.API, id, errorConexion.getMessage());
        } catch (IOException errorApi) {
            throw new IOException("La conexión directa a la base de datos falló ("
                    + errorConexion.getMessage() + ") y el envío a la API tampoco se pudo completar: "
                    + errorApi.getMessage(), errorApi);
        }
    }

    private boolean esFallaDeConexion(SQLException error) {
        String estado = error.getSQLState();
        return error instanceof SQLRecoverableException
                || error instanceof SQLTransientConnectionException
                || (estado != null && estado.startsWith("08"));
    }

    private String requerido(String valor, String campo, String obligatorio, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es " + obligatorio + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(campo + " admite como máximo " + maximo + " caracteres.");
        }
        return limpio;
    }

    private int validarAnio(String texto) {
        int maximo = Year.now().getValue() + 1;
        int anio;
        try {
            anio = Integer.parseInt(requerido(texto, "El año", "obligatorio", 10));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El año debe ser un número entero, por ejemplo 2024.");
        }
        if (anio < 1900 || anio > maximo) {
            throw new IllegalArgumentException("El año debe estar entre 1900 y " + maximo + ".");
        }
        return anio;
    }

    private BigDecimal validarPrecio(String texto) {
        BigDecimal precio;
        try {
            precio = new BigDecimal(requerido(texto, "El precio", "obligatorio", 20));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número, por ejemplo 150000 o 150000.50.");
        }
        if (precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (precio.scale() > 2 || precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El precio admite como máximo 10 enteros y 2 decimales.");
        }
        return precio;
    }
}