package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.support.KeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Utilidades comunes de la capa DAO JDBC.
 */
final class JdbcSupport {

    private JdbcSupport() { }

    /**
     * Obtiene el id generado de forma portable entre H2 (devuelve el nombre de la columna)
     * y MySQL (devuelve {@code GENERATED_KEY}).
     *
     * @param keyHolder resultado del INSERT
     * @param columna   nombre de la columna PK
     * @return id generado
     */
    static Long idGenerado(KeyHolder keyHolder, String columna) {
        Map<String, Object> claves = keyHolder.getKeys();
        if (claves == null || claves.isEmpty()) {
            throw new IllegalStateException("La base de datos no devolvio el id generado");
        }
        if (claves.size() == 1) {
            return ((Number) claves.values().iterator().next()).longValue();
        }
        return claves.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(columna))
                .map(e -> ((Number) e.getValue()).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontro la clave " + columna));
    }

    /** Lee un TIMESTAMP nulo como {@link LocalDateTime}. */
    static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp ts = rs.getTimestamp(columna);
        return ts == null ? null : ts.toLocalDateTime();
    }

    /** Convierte un {@link LocalDateTime} nulo a {@link Timestamp}. */
    static Timestamp timestamp(LocalDateTime valor) {
        return valor == null ? null : Timestamp.valueOf(valor);
    }
}
