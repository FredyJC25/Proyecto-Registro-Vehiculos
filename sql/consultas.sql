SELECT id_vehiculo, marca, modelo, anio, precio, color, medio_registro, fecha_registro
FROM vehiculo
ORDER BY id_vehiculo;

    SELECT id_bitacora, fecha_hora, tipo_operacion, datos_recibidos, resultado, id_vehiculo
    FROM bitacora
    ORDER BY id_bitacora;
