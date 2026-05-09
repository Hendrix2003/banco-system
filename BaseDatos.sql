-- Base de datos sistema bancario
-- Tablas: persona, cliente, cuenta, movimiento

DROP TABLE IF EXISTS movimiento CASCADE;
DROP TABLE IF EXISTS cuenta CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS persona CASCADE;

CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(20),
    edad INTEGER,
    identificacion VARCHAR(30) NOT NULL UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(30)
);

CREATE TABLE cliente (
    id BIGINT PRIMARY KEY,
    cliente_id VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id) REFERENCES persona(id) ON DELETE CASCADE
);

CREATE TABLE cuenta (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(15,2) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    saldo NUMERIC(15,2) NOT NULL,
    cuenta_id BIGINT NOT NULL,
    CONSTRAINT fk_mov_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta(id)
);

CREATE INDEX idx_mov_cuenta_fecha ON movimiento(cuenta_id, fecha);
CREATE INDEX idx_cuenta_cliente ON cuenta(cliente_id);

-- Datos de prueba
INSERT INTO persona (id, nombre, genero, edad, identificacion, direccion, telefono) VALUES
    (1, 'Pedro Martinez ',   'M', 28, '00112345678', 'Los Mina, Santo Domingo Este', '8095551111'),
    (2, 'Maria Lopez',  'F', 35, '40298765432', 'Villa Mella, Santo Domingo Norte', '8095552222'),
    (3, 'Carlos Diaz',  'M', 41, '22345678901', 'Santiago de los Caballeros', '8095553333');

SELECT setval('persona_id_seq', 3);

INSERT INTO cliente (id, cliente_id, password, estado) VALUES
    (1, 'Pmartinez',   '1234', TRUE),
    (2, 'mlopez',   '1234', TRUE),
    (3, 'cdiaz',    '1234', TRUE);

INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id) VALUES
    ('100001', 'Corriente', 1500.00, TRUE, 1),
    ('100002', 'Ahorros',   3200.00, TRUE, 1),
    ('100003', 'Ahorros',   5000.00, TRUE, 2),
    ('100004', 'Corriente', 2500.00, TRUE, 3);