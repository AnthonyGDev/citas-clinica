package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.HorarioDao;
import pe.clinicasantaelena.citas.model.Horario;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Implementacion JDBC de {@link HorarioDao}. El dia se guarda como numero ISO (1 = lunes ... 7 = domingo).
 */
@Repository
public class JdbcHorarioDao implements HorarioDao {

    private static final RowMapper<Horario> MAPPER = (rs, n) -> {
        Horario h = new Horario();
        h.setId(rs.getLong("id_horario"));
        h.setIdMedico(rs.getLong("id_medico"));
        h.setDiaSemana(DayOfWeek.of(rs.getInt("dia_semana")));
        h.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        h.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        h.setDuracionMin(rs.getInt("duracion_min"));
        h.setActivo(rs.getBoolean("activo"));
        return h;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcHorarioDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Horario> listarPorMedicoYDia(Long idMedico, DayOfWeek dia) {
        return jdbc.query("SELECT id_horario, id_medico, dia_semana, hora_inicio, hora_fin, duracion_min, activo "
                + "FROM horario WHERE id_medico = ? AND dia_semana = ? AND activo = TRUE ORDER BY hora_inicio",
                MAPPER, idMedico, dia.getValue());
    }

    @Override
    public List<DayOfWeek> diasDeAtencion(Long idMedico) {
        return jdbc.queryForList("SELECT DISTINCT dia_semana FROM horario WHERE id_medico = ? AND activo = TRUE "
                + "ORDER BY dia_semana", Integer.class, idMedico).stream().map(DayOfWeek::of).toList();
    }
}
