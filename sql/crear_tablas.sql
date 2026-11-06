CREATE TABLE vehiculo (
    id_vehiculo     NUMBER GENERATED ALWAYS AS IDENTITY,
    marca           VARCHAR2(50)  NOT NULL,
    modelo          VARCHAR2(50)  NOT NULL,
    anio            NUMBER(4)     NOT NULL,
    precio          NUMBER(12,2)  NOT NULL,
    color           VARCHAR2(30)  NOT NULL,
    medio_registro  VARCHAR2(15)  NOT NULL,
    fecha_registro  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_vehiculo PRIMARY KEY (id_vehiculo),
    CONSTRAINT ck_vehiculo_anio CHECK (anio BETWEEN 1900 AND 2100),
    CONSTRAINT ck_vehiculo_precio CHECK (precio > 0),
    CONSTRAINT ck_vehiculo_medio CHECK (medio_registro IN ('BASE_DATOS', 'API'))
);

CREATE TABLE bitacora (
    id_bitacora      NUMBER GENERATED ALWAYS AS IDENTITY,
    fecha_hora       TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    tipo_operacion   VARCHAR2(50)   NOT NULL,
    datos_recibidos  VARCHAR2(4000),
    resultado        VARCHAR2(500)  NOT NULL,
    id_vehiculo      NUMBER,
    CONSTRAINT pk_bitacora PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculo (id_vehiculo)
);
