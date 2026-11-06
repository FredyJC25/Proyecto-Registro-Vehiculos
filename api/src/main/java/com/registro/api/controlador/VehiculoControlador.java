package com.registro.api.controlador;

import com.registro.api.modelo.RespuestaRegistro;
import com.registro.api.modelo.Vehiculo;
import com.registro.api.servicio.VehiculoServicio;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoControlador {

    private final VehiculoServicio servicio;

    public VehiculoControlador(VehiculoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<RespuestaRegistro> registrar(@RequestBody Vehiculo vehiculo) {
        Vehiculo registrado = servicio.registrar(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaRegistro("Vehículo registrado correctamente mediante la API", registrado));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        String detalle = "El cuerpo de la solicitud no es un JSON válido o algún campo tiene un tipo incorrecto";
        servicio.registrarSolicitudInvalida(detalle);
        return ResponseEntity.badRequest().body(Map.of("mensaje", detalle));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> errorBaseDatos(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error de base de datos: " + e.getMostSpecificCause().getMessage()));
    }
}