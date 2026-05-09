# Banco

Sistema bancario basico con CRUD de clientes, cuentas y movimientos, y un endpoint de reportes en JSON y PDF.

## Stack

Backend: Java 17, Spring Boot 3.2, Spring Data JPA, PostgreSQL, OpenPDF, JUnit 5, Mockito.

Frontend: Angular 17 con standalone components, Reactive Forms, Jest. CSS puro, sin librerias visuales.

Todo dockerizado con docker-compose.

## Como correrlo

Con Docker:

```
docker-compose up --build
```

Esto levanta postgres, backend y frontend. Despues:

- Frontend: http://localhost
- API: http://localhost:8080/api
- DB: localhost:5432 (banco / banco123)

Para apagar todo:

```
docker-compose down
```

Para borrar los datos:

```
docker-compose down -v
```

## Correr backend solo (sin docker)

Se necesita JDK 17, Maven y postgres corriendo en local.

Crear la BD:

```
psql -U postgres -c "CREATE DATABASE banco;"
psql -U postgres -d banco -f BaseDatos.sql
```

Y arrancar:

```
cd banco-backend
mvn spring-boot:run
```

Las credenciales se pueden ajustar en application.properties (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).

## Correr frontend solo

Se necesita Node 20+.

```
cd banco-frontend
npm install
npm start
```

Sale en http://localhost:4200 y consume la API en localhost:8080. Si quieren cambiar la URL del backend, esta en src/environments/environment.ts.

## Endpoints

Base: http://localhost:8080/api

```
GET    /clientes
GET    /clientes/{id}
POST   /clientes
PUT    /clientes/{id}
DELETE /clientes/{id}

GET    /cuentas
GET    /cuentas/{id}
POST   /cuentas
PUT    /cuentas/{id}
DELETE /cuentas/{id}

GET    /movimientos
GET    /movimientos/{id}
POST   /movimientos
PUT    /movimientos/{id}
DELETE /movimientos/{id}

GET    /reportes?cliente={clienteId}&fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD&formato=json|pdf
```

Si pasan formato=pdf, la respuesta JSON trae un campo pdfBase64 con el archivo. El frontend lo decodifica y lo descarga.

La coleccion de Postman esta en Banco.postman_collection.json, cada request ya viene con data lista para probar.

## Reglas de negocio

Depositos: valor positivo. Retiros: valor negativo.

Cada movimiento guarda el saldo resultante. Si retiras mas de lo que tienes, devuelve "Saldo no disponible".

Hay un cupo diario de retiros de 1000 por cuenta. Si lo superas, devuelve "Cupo diario excedido". El limite se puede cambiar en application.properties con la propiedad banco.limite-retiro-diario.

## Tests

Backend:

```
cd banco-backend
mvn test
```

MovimientoServiceTest cubre los casos de negocio: deposito, retiro, saldo no disponible, cupo excedido y recalculo de saldos al editar o eliminar. ClienteControllerTest hace tests del controller con WebMvcTest.

Frontend:

```
cd banco-frontend
npm test
```

Tests de los servicios HTTP con HttpClientTestingModule.

## Estructura

La estructura del backend es Controller, Service y Repository. Los DTOs van separados de las entidades, y el mapeo se hace manual con metodos privados (no use MapStruct, son solo 4 entidades).

```
banco-system/
  banco-backend/
    src/main/java/com/banco/
      controller/
      service/
      repository/
      entity/
      dto/
      exception/
      config/
    Dockerfile
    pom.xml
  banco-frontend/
    src/app/
      models/
      services/
      components/
      pages/
    Dockerfile
    nginx.conf
  BaseDatos.sql
  Banco.postman_collection.json
  docker-compose.yml
```

## Datos precargados

El BaseDatos.sql ya inserta 3 clientes y 4 cuentas para que se pueda probar al toque. Pedro Martinez (clienteId Pmartinez) tiene dos cuentas: 100001 corriente con saldo 1500 y 100002 ahorros con saldo 3200. Maria Lopez (mlopez) tiene la cuenta 100003 ahorros con 5000. Carlos Diaz (cdiaz) tiene la 100004 corriente con 2500.

Pedro tiene dos cuentas a proposito, para mostrar la relacion uno-a-muchos entre cliente y cuenta.

## Notas

Persona y Cliente usan herencia JOINED en JPA: dos tablas relacionadas por la PK.

El saldo actual de una cuenta se calcula sumando saldo inicial mas todos los movimientos. No confio solo en cachear el ultimo saldo, lo recalculo siempre desde cero.

Al editar o eliminar un movimiento, los saldos posteriores se recalculan automaticamente para mantener la consistencia.

El manejo de errores esta centralizado en GlobalExceptionHandler. BusinessException da 400, NotFoundException da 404, y las validaciones fallidas tambien dan 400 con detalle del campo.

El PDF se genera con OpenPDF en el backend y se devuelve en Base64. El frontend lo decodifica y dispara la descarga con un Blob.
