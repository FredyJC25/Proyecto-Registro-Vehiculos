package com.registro.api.modelo;

public record RegistroBitacora(
        String tipoOperacion,
        String datosRecibidos,
        String resultado,
        Long idVehiculo
) {
}