-- Script para MySQL 8 (perfil prod). Mismo modelo que schema-h2.sql, con:
--  * columna generada STORED para el indice unico de bloque (RF07)
--  * trigger de auditoria (RN05)
--  * usuario de aplicacion con minimo privilegio (sin DELETE ni DROP)
CREATE DATABASE IF NOT EXISTS bd_citas_clinica CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE bd_citas_clinica;

-- (Las tablas usuario, paciente, especialidad, consultorio, medico, horario y auditoria
--  son identicas a src/main/resources/schema-h2.sql; se cambia solo la definicion de cita.)

CREATE TABLE cita (
    id_cita             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo              CHAR(10)     NOT NULL UNIQUE,
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
    fecha_registro      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    slot_activo         TINYINT GENERATED ALWAYS AS
                        (CASE WHEN estado IN ('CANCELADA','REPROGRAMADA') THEN NULL ELSE 1 END) STORED,
    CONSTRAINT uq_cita_bloque UNIQUE (id_medico, fecha, hora_inicio, slot_activo),
    CONSTRAINT ck_cita_horas CHECK (hora_fin > hora_inicio)
);

DELIMITER $$
CREATE TRIGGER tr_cita_auditoria_upd AFTER UPDATE ON cita FOR EACH ROW
BEGIN
    IF OLD.estado <> NEW.estado OR OLD.fecha <> NEW.fecha OR OLD.hora_inicio <> NEW.hora_inicio THEN
        INSERT INTO auditoria (tabla, id_registro, accion, detalle)
        VALUES ('cita', NEW.id_cita, 'UPDATE', CONCAT(OLD.estado, ' -> ', NEW.estado));
    END IF;
END$$
DELIMITER ;

-- Usuario de la aplicacion: puede leer, insertar y actualizar, pero no borrar ni alterar tablas
CREATE USER IF NOT EXISTS 'app_citas'@'localhost' IDENTIFIED BY 'cambiar_en_produccion';
GRANT SELECT, INSERT, UPDATE ON bd_citas_clinica.* TO 'app_citas'@'localhost';
