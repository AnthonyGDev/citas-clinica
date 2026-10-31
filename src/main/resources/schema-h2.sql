-- Esquema para el perfil dev (H2 en modo MySQL).
-- El script completo para MySQL 8 (con trigger y usuarios de BD) esta en docs/script_bd_mysql.sql

CREATE TABLE usuario (
    id_usuario        BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo            VARCHAR(120) NOT NULL,
    password_hash     CHAR(60)     NOT NULL,
    rol               VARCHAR(20)  NOT NULL,
    estado            VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    intentos_fallidos INT          NOT NULL DEFAULT 0,
    bloqueado_hasta   TIMESTAMP    NULL,
    fecha_registro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('PACIENTE','RECEPCIONISTA','MEDICO','ADMINISTRADOR')),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE paciente (
    id_paciente          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario           BIGINT       NULL,
    dni                  CHAR(8)      NOT NULL,
    nombres              VARCHAR(80)  NOT NULL,
    apellidos            VARCHAR(80)  NOT NULL,
    telefono             VARCHAR(15)  NULL,
    correo               VARCHAR(120) NULL,
    fecha_nacimiento     DATE         NULL,
    consentimiento_datos BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_consentimiento TIMESTAMP    NULL,
    CONSTRAINT uq_paciente_dni UNIQUE (dni),
    CONSTRAINT uq_paciente_usuario UNIQUE (id_usuario),
    CONSTRAINT ck_paciente_dni CHECK (REGEXP_LIKE(dni, '^[0-9]{8}$')),
    CONSTRAINT fk_paciente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE especialidad (
    id_especialidad BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(60)  NOT NULL,
    descripcion     VARCHAR(200) NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_especialidad_nombre UNIQUE (nombre)
);

CREATE TABLE consultorio (
    id_consultorio BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo         VARCHAR(10) NOT NULL,
    piso           INT         NOT NULL,
    CONSTRAINT uq_consultorio_codigo UNIQUE (codigo)
);

CREATE TABLE medico (
    id_medico       BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      BIGINT       NOT NULL,
    id_especialidad BIGINT       NOT NULL,
    id_consultorio  BIGINT       NOT NULL,
    cmp             CHAR(6)      NOT NULL,
    nombres         VARCHAR(80)  NOT NULL,
    apellidos       VARCHAR(80)  NOT NULL,
    telefono        VARCHAR(15)  NULL,
    correo          VARCHAR(120) NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_medico_cmp UNIQUE (cmp),
    CONSTRAINT uq_medico_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_medico_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_medico_especialidad FOREIGN KEY (id_especialidad) REFERENCES especialidad (id_especialidad),
    CONSTRAINT fk_medico_consultorio FOREIGN KEY (id_consultorio) REFERENCES consultorio (id_consultorio)
);

CREATE TABLE horario (
    id_horario   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_medico    BIGINT  NOT NULL,
    dia_semana   TINYINT NOT NULL,
    hora_inicio  TIME    NOT NULL,
    hora_fin     TIME    NOT NULL,
    duracion_min INT     NOT NULL,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_rango CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_horario_duracion CHECK (duracion_min IN (15, 20, 30, 45, 60)),
    CONSTRAINT fk_horario_medico FOREIGN KEY (id_medico) REFERENCES medico (id_medico) ON DELETE CASCADE
);

CREATE TABLE cita (
    id_cita             BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo              CHAR(10)     NOT NULL,
    id_paciente         BIGINT       NOT NULL,
    id_medico           BIGINT       NOT NULL,
    id_consultorio      BIGINT       NOT NULL,
    fecha               DATE         NOT NULL,
    hora_inicio         TIME         NOT NULL,
    hora_fin            TIME         NOT NULL,
    estado              VARCHAR(15)  NOT NULL DEFAULT 'CONFIRMADA',
    canal               VARCHAR(12)  NOT NULL,
    motivo              VARCHAR(255) NULL,
    id_usuario_registro BIGINT       NOT NULL,
    fecha_registro      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 1 si la cita ocupa el bloque; NULL si fue cancelada o reprogramada (el UNIQUE ignora los NULL)
    slot_activo         TINYINT GENERATED ALWAYS AS
                        (CASE WHEN estado IN ('CANCELADA','REPROGRAMADA') THEN NULL ELSE 1 END),
    CONSTRAINT uq_cita_codigo UNIQUE (codigo),
    CONSTRAINT uq_cita_bloque UNIQUE (id_medico, fecha, hora_inicio, slot_activo),
    CONSTRAINT ck_cita_horas CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_cita_estado CHECK (estado IN ('PENDIENTE','CONFIRMADA','ATENDIDA','CANCELADA','NO_ASISTIO','REPROGRAMADA')),
    CONSTRAINT ck_cita_canal CHECK (canal IN ('WEB','PRESENCIAL','TELEFONO')),
    CONSTRAINT fk_cita_paciente FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente),
    CONSTRAINT fk_cita_medico FOREIGN KEY (id_medico) REFERENCES medico (id_medico),
    CONSTRAINT fk_cita_consultorio FOREIGN KEY (id_consultorio) REFERENCES consultorio (id_consultorio),
    CONSTRAINT fk_cita_usuario FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id_usuario)
);

CREATE INDEX ix_cita_fecha_estado ON cita (fecha, estado);
CREATE INDEX ix_cita_paciente ON cita (id_paciente);

CREATE TABLE auditoria (
    id_auditoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    tabla        VARCHAR(30)  NOT NULL,
    id_registro  BIGINT       NOT NULL,
    accion       VARCHAR(20)  NOT NULL,
    detalle      VARCHAR(255) NULL,
    fecha        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
