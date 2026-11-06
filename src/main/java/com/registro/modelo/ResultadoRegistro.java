package com.registro.modelo;

public class ResultadoRegistro {

    private final MedioRegistro medio;
    private final long idVehiculo;
    private final String motivoContingencia;

    public ResultadoRegistro(MedioRegistro medio, long idVehiculo, String motivoContingencia) {
        this.medio = medio;
        this.idVehiculo = idVehiculo;
        this.motivoContingencia = motivoContingencia;
    }

    public MedioRegistro getMedio() {
        return medio;
    }

    public long getIdVehiculo() {
        return idVehiculo;
    }

    public String getMotivoContingencia() {
        return motivoContingencia;
    }
}