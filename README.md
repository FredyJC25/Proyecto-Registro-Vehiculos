# Registro de vehículos mediante Base de Datos y API REST

Proyecto final del curso de Programación II (proyecto impar). La solución está formada por una **aplicación de escritorio en Java** que registra vehículos directamente en **Oracle Database** mediante JDBC y por una **API REST con Spring Boot** que sirve como mecanismo alternativo: si la conexión directa a la base de datos falla, la aplicación envía el vehículo a la API mediante `POST`, y la API lo registra y guarda cada solicitud en una bitácora.

## Flujo de la solución

```
Aplicación de escritorio → INSERT directo con JDBC → si funciona, confirma el registro
        ↓ si falla la conexión a la base de datos
POST a la API REST → la API recibe el JSON → registra el vehículo → registra la bitácora → devuelve la respuesta
```

1. El usuario llena el formulario (marca, modelo, año, precio y color) y presiona **Guardar**.
2. La aplicación valida los datos. Si alguno no es válido, muestra el motivo y no intenta registrar.
3. La aplicación intenta conectarse a Oracle con JDBC y ejecutar el `INSERT`.
4. Si la conexión falla, la aplicación envía el vehículo en formato JSON a `POST /api/vehiculos`.
5. La API valida los datos, inserta el vehículo, guarda la solicitud en la tabla `bitacora` y responde con el vehículo registrado.
6. La aplicación indica si el registro se hizo **directamente en la base de datos** o **mediante la API**, y lo agrega al historial de la sesión.

La aplicación solo usa la API cuando falla la **conexión**. Si la base de datos está disponible pero rechaza el registro, se muestra el error sin cambiar de camino. Si fallan la base de datos y la API, la aplicación informa ambos motivos.

La API tiene su propia conexión a la base de datos, independiente de la aplicación de escritorio. Por eso puede registrar el vehículo aunque la conexión directa de la aplicación falle, por ejemplo por un problema de red, del firewall o de credenciales en la computadora del usuario.

### Simulación de la falla

El formulario tiene la casilla **Simular falla de la conexión directa a la base de datos**. Al marcarla, la aplicación intenta conectarse por JDBC a un puerto en el que no hay ningún servidor (`1599`), por lo que Oracle responde con un error real (`ORA-12541`) y se activa la contingencia por la API.

## Tecnologías

| Componente | Herramienta |
|---|---|
| Base de datos | Oracle AI Database 26ai Free |
| API REST | Spring Boot 4.1.1 (Java 17) con Spring Web, JDBC API y Oracle Driver |
| Aplicación de escritorio | Java 21 con Swing (`JFrame`) |
| Acceso a datos desde escritorio | JDBC con `ojdbc11` y `PreparedStatement` |
| Envío a la API | `HttpClient` de Java y `org.json` |
| Gestor de dependencias | Maven |
| Control de versiones | Git y GitHub |

## Estructura del repositorio

```
RegistroVehiculos/
├── api/        API REST con Spring Boot
├── sql/        Scripts de la base de datos
├── src/        Aplicación de escritorio
├── pom.xml     Configuración de Maven de la aplicación de escritorio
└── README.md
```

### API (`api/src/main/java/com/registro/api`)

La API sigue la separación **Model, Controller, Service y acceso a datos**:

| Capa | Paquete | Clases | Responsabilidad |
|---|---|---|---|
| Model | `modelo` | `Vehiculo`, `RegistroBitacora`, `RespuestaRegistro` | Datos del JSON recibido, del registro de bitácora y de la respuesta |
| Controller | `controlador` | `VehiculoControlador` | Recibe `POST /api/vehiculos` y devuelve la respuesta con su código HTTP |
| Service | `servicio` | `VehiculoServicio` | Valida los datos, registra el vehículo y guarda la bitácora |
| Acceso a datos | `repositorio` | `VehiculoRepositorio`, `BitacoraRepositorio` | Ejecutan los `INSERT` con `JdbcTemplate` |

### Aplicación de escritorio (`src/main/java/com/registro`)

| Paquete | Clases | Responsabilidad |
|---|---|---|
| `principal` | `Main` | Inicia la aplicación |
| `vista` | `VentanaRegistro` | Formulario, casilla de simulación, mensajes e historial |
| `negocio` | `RegistroVehiculoServicio` | Valida los datos y decide el medio de registro: primero JDBC y, si falla la conexión, la API |
| `dao` | `VehiculoDAO` | `INSERT` directo en la tabla `vehiculo` |
| `servicio` | `VehiculoApiCliente` | Construye el JSON con `org.json` y lo envía a la API por `POST` |
| `conexion` | `Conexion` | Abre la conexión JDBC a Oracle |
| `modelo` | `Vehiculo`, `MedioRegistro`, `ResultadoRegistro` | Datos del vehículo y resultado del registro |

## Base de datos

### Tabla `vehiculo`

| Columna | Descripción |
|---|---|
| `id_vehiculo` | Identificador generado por Oracle |
| `marca`, `modelo`, `anio`, `precio`, `color` | Datos del formulario |
| `medio_registro` | `BASE_DATOS` si se registró por JDBC o `API` si se registró mediante la API |
| `fecha_registro` | Fecha y hora del registro |

### Tabla `bitacora`

Cada solicitud que recibe la API se registra, sea exitosa o no:

| Columna | Descripción |
|---|---|
| `fecha_hora` | Fecha y hora de la solicitud |
| `tipo_operacion` | Operación realizada: `POST /api/vehiculos - INSERT vehiculo` |
| `datos_recibidos` | JSON recibido |
| `resultado` | `EXITOSO` con el id del vehículo, o `ERROR` con el motivo |
| `id_vehiculo` | Vehículo registrado, si la solicitud fue exitosa |

## Endpoint

`POST http://localhost:8080/api/vehiculos`

Cuerpo de la solicitud:

```json
{
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2024,
  "precio": 150000,
  "color": "Blanco"
}
```

Si el JSON incluye un `id`, la API lo ignora, porque Oracle lo genera.

| Código | Cuándo |
|---|---|
| `201 Created` | El vehículo se registró. La respuesta incluye un mensaje y el vehículo con su `id` |
| `400 Bad Request` | Algún dato no es válido o el JSON está mal formado. La respuesta incluye el motivo |
| `500 Internal Server Error` | La base de datos rechazó la operación |

## Validaciones

Se aplican tanto en la aplicación de escritorio como en la API:

- Marca, modelo y color son obligatorios (máximo 50, 50 y 30 caracteres).
- El año debe ser un número entero entre 1900 y el año siguiente al actual.
- El precio debe ser mayor que cero, con máximo 10 enteros y 2 decimales.

La tabla `vehiculo` también tiene restricciones `CHECK` sobre el año, el precio y el medio de registro.

## Instalación y ejecución

### Requisitos

- JDK 21
- Oracle Database con el servicio `FREEPDB1` y SQL Developer
- IntelliJ IDEA

### 1. Crear la base de datos

1. Conectado como `system`, ejecutar `sql/crear_usuario.sql`.
2. Conectado como `vehiculos` (contraseña `vehiculos123`), ejecutar `sql/crear_tablas.sql`.
3. Para revisar los vehículos y la bitácora, ejecutar `sql/consultas.sql`.

Para borrar las tablas y volver a empezar, ejecutar `sql/eliminar_tablas.sql` y después `sql/crear_tablas.sql`.

### 2. Ejecutar la API

Abrir la carpeta `api` en IntelliJ como proyecto Maven y ejecutar `com.registro.api.ApiApplication`. También se puede ejecutar desde la carpeta `api` con:

```
mvnw spring-boot:run
```

Los datos de conexión están en `api/src/main/resources/application.properties`. El archivo `api/pruebas.http` contiene solicitudes de prueba que se pueden ejecutar desde IntelliJ.

### 3. Ejecutar la aplicación de escritorio

Abrir el proyecto en IntelliJ y ejecutar `com.registro.principal.Main`.

- Con la casilla de simulación **desmarcada**, el vehículo se registra directamente en la base de datos.
- Con la casilla **marcada**, la conexión directa falla y el vehículo se registra mediante la API.

## Flujo de trabajo con Git

- `master` contiene la versión estable del proyecto.
- `develop` es la rama de trabajo.
- Cada parte terminada se integra a `master` mediante un pull request desde `develop`.

## Autor

Fredy Jóm — [FredyJC25](https://github.com/FredyJC25)