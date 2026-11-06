package com.registro.api.modelo;

import java.math.BigDecimal;

public record Vehiculo(
        Long id,
        String marca,
        String modelo,
        Integer anio,
        BigDecimal precio,
        String color
) {

    public Vehiculo conId(long nuevoId) {
        return new Vehiculo(nuevoId, marca, modelo, anio, precio, color);
    }

    public String aJson() {
        return "{\"id\":" + id
                + ",\"marca\":" + texto(marca)
                + ",\"modelo\":" + texto(modelo)
                + ",\"anio\":" + anio
                + ",\"precio\":" + (precio == null ? "null" : precio.toPlainString())
                + ",\"color\":" + texto(color)
                + "}";
    }

    private static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        String escapado = valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escapado + "\"";
    }
}