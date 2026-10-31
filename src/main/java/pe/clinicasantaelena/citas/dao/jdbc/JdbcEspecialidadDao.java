package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.EspecialidadDao;
import pe.clinicasantaelena.citas.model.Especialidad;

import java.util.List;
import java.util.Optional;

/** Implementacion JDBC de {@link EspecialidadDao}. */
@Repository
public class JdbcEspecialidadDao implements EspecialidadDao {

    private static final RowMapper<Especialidad> MAPPER = (rs, n) -> {
        Especialidad e = new Especialidad(rs.getLong("id_especialidad"), rs.getString("nombre"),
                rs.getString("descripcion"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcEspecialidadDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Especialidad> listarActivas() {
        return jdbc.query("SELECT id_especialidad, nombre, descripcion, activo FROM especialidad "
                + "WHERE activo = TRUE ORDER BY nombre", MAPPER);
    }

    @Override
    public Optional<Especialidad> buscarPorId(Long id) {
        return jdbc.query("SELECT id_especialidad, nombre, descripcion, activo FROM especialidad "
                + "WHERE id_especialidad = ?", MAPPER, id).stream().findFirst();
    }
}
