package com.registro.servicio;

import com.registro.modelo.Vehiculo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class VehiculoApiCliente {

    private static final String URL_API = "http://localhost:8080/api/vehiculos";

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public long registrar(Vehiculo vehiculo) throws IOException, InterruptedException {
        JSONObject json = new JSONObject();
        json.put("marca", vehiculo.getMarca());
        json.put("modelo", vehiculo.getModelo());
        json.put("anio", vehiculo.getAnio());
        json.put("precio", vehiculo.getPrecio());
        json.put("color", vehiculo.getColor());

        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(URL_API))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException | HttpTimeoutException e) {
            throw new IOException("No se pudo conectar con la API en " + URL_API
                    + ". Verifique que ApiApplication esté en ejecución.", e);
        }

        if (respuesta.statusCode() != 201) {
            throw new IOException("La API rechazó el registro: " + mensajeError(respuesta));
        }
        try {
            return new JSONObject(respuesta.body()).getJSONObject("vehiculo").getLong("id");
        } catch (JSONException e) {
            throw new IOException("La respuesta de la API no tiene el formato esperado: " + e.getMessage(), e);
        }
    }

    private String mensajeError(HttpResponse<String> respuesta) {
        try {
            return new JSONObject(respuesta.body()).getString("mensaje");
        } catch (JSONException e) {
            return "código HTTP " + respuesta.statusCode();
        }
    }
}