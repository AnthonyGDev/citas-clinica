package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.ReporteDao;
import pe.clinicasantaelena.citas.dto.FilaReporte;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/** Implementacion JDBC de {@link ReporteDao} (consultas de agregacion de solo lectura). */
@Repository
public class JdbcReporteDao implements ReporteDao {

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcReporteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<FilaReporte> citasPorEspecialidad(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT e.nombre AS especialidad, "
                + "SUM(CASE WHEN ci.estado = 'ATENDIDA' THEN 1 ELSE 0 END) AS atendidas, "
                + "SUM(CASE WHEN ci.estado = 'NO_ASISTIO' THEN 1 ELSE 0 END) AS no_asistio, "
                + "SUM(CASE WHEN ci.estado = 'CANCELADA' THEN 1 ELSE 0 END) AS canceladas, "
                + "SUM(CASE WHEN ci.estado IN ('PENDIENTE', 'CONFIRMADA') THEN 1 ELSE 0 END) AS vigentes "
                + "FROM especialidad e "
                + "LEFT JOIN medico m ON m.id_especialidad = e.id_especialidad "
                + "LEFT JOIN cita ci ON ci.id_medico = m.id_medico AND ci.fecha BETWEEN ? AND ? "
                + "WHERE e.activo = TRUE GROUP BY e.nombre ORDER BY e.nombre";
        return jdbc.query(sql, (rs, n) -> new FilaReporte(rs.getString("especialidad"),
                rs.getLong("atendidas"), rs.getLong("no_asistio"), rs.getLong("canceladas"),
                rs.getLong("vigentes")), Date.valueOf(desde), Date.valueOf(hasta));
    }
}
