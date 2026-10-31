package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.MedicoDao;
import pe.clinicasantaelena.citas.model.Medico;

import java.util.List;
import java.util.Optional;

/** Implementacion JDBC de {@link MedicoDao}. */
@Repository
public class JdbcMedicoDao implements MedicoDao {

    private static final String SELECT = "SELECT m.id_medico, m.id_usuario, m.cmp, m.nombres, m.apellidos, "
            + "m.telefono, m.correo, m.activo, m.id_especialidad, e.nombre AS especialidad, "
            + "m.id_consultorio, c.codigo AS consultorio "
            + "FROM medico m JOIN especialidad e ON e.id_especialidad = m.id_especialidad "
            + "JOIN consultorio c ON c.id_consultorio = m.id_consultorio ";

    private static final RowMapper<Medico> MAPPER = (rs, n) -> {
        Medico m = new Medico();
        m.setId(rs.getLong("id_medico"));
        m.setIdUsuario(rs.getLong("id_usuario"));
        m.setCmp(rs.getString("cmp"));
        m.setNombres(rs.getString("nombres"));
        m.setApellidos(rs.getString("apellidos"));
        m.setTelefono(rs.getString("telefono"));
        m.setCorreo(rs.getString("correo"));
        m.setActivo(rs.getBoolean("activo"));
        m.setIdEspecialidad(rs.getLong("id_especialidad"));
        m.setNombreEspecialidad(rs.getString("especialidad"));
        m.setIdConsultorio(rs.getLong("id_consultorio"));
        m.setCodigoConsultorio(rs.getString("consultorio"));
        return m;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcMedicoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Medico> listarActivosPorEspecialidad(Long idEspecialidad) {
        return jdbc.query(SELECT + "WHERE m.id_especialidad = ? AND m.activo = TRUE ORDER BY m.apellidos",
                MAPPER, idEspecialidad);
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return jdbc.query(SELECT + "WHERE m.id_medico = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<Medico> buscarPorIdUsuario(Long idUsuario) {
        return jdbc.query(SELECT + "WHERE m.id_usuario = ?", MAPPER, idUsuario).stream().findFirst();
    }
}
