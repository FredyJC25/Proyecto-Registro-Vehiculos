package com.registro.modelo;

public enum MedioRegistro {

    BASE_DATOS("directamente en la base de datos (JDBC)"),
    API("mediante la API REST (POST)");

    private final String descripcion;

    MedioRegistro(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}