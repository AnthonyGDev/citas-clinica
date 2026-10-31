package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.PacienteDao;
import pe.clinicasantaelena.citas.model.Paciente;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Optional;

/** Implementacion JDBC de {@link PacienteDao}. */
@Repository
public class JdbcPacienteDao implements PacienteDao {

    private static final String SELECT = "SELECT id_paciente, id_usuario, dni, nombres, apellidos, telefono, "
            + "correo, fecha_nacimiento FROM paciente ";

    private static final RowMapper<Paciente> MAPPER = (rs, n) -> {
        Paciente p = new Paciente();
        p.setId(rs.getLong("id_paciente"));
        p.setIdUsuario(rs.getLong("id_usuario"));
        p.setDni(rs.getString("dni"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        Date fn = rs.getDate("fecha_nacimiento");
        p.setFechaNacimiento(fn == null ? null : fn.toLocalDate());
        return p;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcPacienteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jdbc.query(SELECT + "WHERE id_paciente = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<Paciente> buscarPorIdUsuario(Long idUsuario) {
        return jdbc.query(SELECT + "WHERE id_usuario = ?", MAPPER, idUsuario).stream().findFirst();
    }

    @Override
    public boolean existeDni(String dni) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM paciente WHERE dni = ?", Integer.class, dni);
        return n != null && n > 0;
    }

    @Override
    public Long insertar(Paciente p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO paciente (id_usuario, dni, nombres, apellidos, telefono, correo, fecha_nacimiento, "
                            + "consentimiento_datos, fecha_consentimiento) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP)",
                    new String[]{"id_paciente"});
            ps.setLong(1, p.getIdUsuario());
            ps.setString(2, p.getDni());
            ps.setString(3, p.getNombres());
            ps.setString(4, p.getApellidos());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getCorreo());
            if (p.getFechaNacimiento() == null) {
                ps.setNull(7, Types.DATE);
            } else {
                ps.setDate(7, Date.valueOf(p.getFechaNacimiento()));
            }
            return ps;
        }, kh);
        Long id = JdbcSupport.idGenerado(kh, "id_paciente");
        p.setId(id);
        return id;
    }
}
