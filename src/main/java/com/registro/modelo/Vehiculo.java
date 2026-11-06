package com.registro.modelo;

import java.math.BigDecimal;

public class Vehiculo {

    private final String marca;
    private final String modelo;
    private final int anio;
    private final BigDecimal precio;
    private final String color;

    public Vehiculo(String marca, String modelo, int anio, BigDecimal precio, String color) {
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.precio = precio;
        this.color = color;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAnio() {
        return anio;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getColor() {
        return color;
    }

    public String getDescripcion() {
        return marca + " " + modelo + " " + anio;
    }
}