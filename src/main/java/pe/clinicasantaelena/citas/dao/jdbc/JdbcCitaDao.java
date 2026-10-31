package pe.clinicasantaelena.citas.dao.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.CitaDao;
import pe.clinicasantaelena.citas.exception.HorarioOcupadoException;
import pe.clinicasantaelena.citas.model.CanalReserva;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.model.EstadoCita;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementacion JDBC de {@link CitaDao}.
 *
 * <p>Traduce la violacion de la restriccion {@code uq_cita_bloque} a
 * {@link HorarioOcupadoException}, de modo que la capa de servicio no depende de excepciones de Spring.</p>
 */
@Repository
public class JdbcCitaDao implements CitaDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcCitaDao.class);

    private static final String SELECT = "SELECT ci.id_cita, ci.codigo, ci.id_paciente, ci.id_medico, ci.id_consultorio, "
            + "ci.fecha, ci.hora_inicio, ci.hora_fin, ci.estado, ci.canal, ci.motivo, ci.id_usuario_registro, "
            + "ci.fecha_registro, p.nombres AS p_nom, p.apellidos AS p_ape, m.nombres AS m_nom, m.apellidos AS m_ape, "
            + "e.nombre AS especialidad, c.codigo AS consultorio "
            + "FROM cita ci "
            + "JOIN paciente p ON p.id_paciente = ci.id_paciente "
            + "JOIN medico m ON m.id_medico = ci.id_medico "
            + "JOIN especialidad e ON e.id_especialidad = m.id_especialidad "
            + "JOIN consultorio c ON c.id_consultorio = ci.id_consultorio ";

    private static final RowMapper<Cita> MAPPER = (rs, n) -> {
        Cita c = new Cita();
        c.setId(rs.getLong("id_cita"));
        c.setCodigo(rs.getString("codigo"));
        c.setIdPaciente(rs.getLong("id_paciente"));
        c.setIdMedico(rs.getLong("id_medico"));
        c.setIdConsultorio(rs.getLong("id_consultorio"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        c.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        c.setEstado(EstadoCita.valueOf(rs.getString("estado")));
        c.setCanal(CanalReserva.valueOf(rs.getString("canal")));
        c.setMotivo(rs.getString("motivo"));
        c.setIdUsuarioRegistro(rs.getLong("id_usuario_registro"));
        c.setFechaRegistro(JdbcSupport.fechaHora(rs, "fecha_registro"));
        c.setNombrePaciente(rs.getString("p_nom") + " " + rs.getString("p_ape"));
        c.setNombreMedico("Dr(a). " + rs.getString("m_nom") + " " + rs.getString("m_ape"));
        c.setNombreEspecialidad(rs.getString("especialidad"));
        c.setCodigoConsultorio(rs.getString("consultorio"));
        return c;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC */
    public JdbcCitaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Cita c) {
        KeyHolder kh = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO cita (codigo, id_paciente, id_medico, id_consultorio, fecha, hora_inicio, hora_fin, "
                                + "estado, canal, motivo, id_usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{"id_cita"});
                ps.setString(1, c.getCodigo());
                ps.setLong(2, c.getIdPaciente());
                ps.setLong(3, c.getIdMedico());
                ps.setLong(4, c.getIdConsultorio());
                ps.setDate(5, Date.valueOf(c.getFecha()));
                ps.setTime(6, Time.valueOf(c.getHoraInicio()));
                ps.setTime(7, Time.valueOf(c.getHoraFin()));
                ps.setString(8, c.getEstado().name());
                ps.setString(9, c.getCanal().name());
                if (c.getMotivo() == null) {
                    ps.setNull(10, Types.VARCHAR);
                } else {
                    ps.setString(10, c.getMotivo());
                }
                ps.setLong(11, c.getIdUsuarioRegistro());
                return ps;
            }, kh);
        } catch (DuplicateKeyException e) {
            // 2.a barrera: la restriccion UNIQUE de la BD detecto una reserva simultanea
            log.warn("Colision de bloque detectada por la BD: medico={}, fecha={}, hora={}",
                    c.getIdMedico(), c.getFecha(), c.getHoraInicio());
            throw new HorarioOcupadoException("Ese horario acaba de ser reservado por otra persona. Elige otro.");
        }
        Long id = JdbcSupport.idGenerado(kh, "id_cita");
        c.setId(id);
        return id;
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return jdbc.query(SELECT + "WHERE ci.id_cita = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Cita> listarPorPaciente(Long idPaciente) {
        return jdbc.query(SELECT + "WHERE ci.id_paciente = ? ORDER BY ci.fecha DESC, ci.hora_inicio DESC",
                MAPPER, idPaciente);
    }

    @Override
    public List<Cita> listarPorMedicoYFecha(Long idMedico, LocalDate fecha) {
        return jdbc.query(SELECT + "WHERE ci.id_medico = ? AND ci.fecha = ? AND ci.estado <> 'REPROGRAMADA' "
                + "ORDER BY ci.hora_inicio", MAPPER, idMedico, Date.valueOf(fecha));
    }

    @Override
    public Set<LocalTime> horasOcupadas(Long idMedico, LocalDate fecha) {
        List<Time> horas = jdbc.queryForList("SELECT hora_inicio FROM cita WHERE id_medico = ? AND fecha = ? "
                + "AND estado NOT IN ('CANCELADA', 'REPROGRAMADA')", Time.class, idMedico, Date.valueOf(fecha));
        Set<LocalTime> resultado = new HashSet<>();
        horas.forEach(t -> resultado.add(t.toLocalTime()));
        return resultado;
    }

    @Override
    public boolean existeVigenteEnEspecialidad(Long idPaciente, Long idEspecialidad, LocalDate desde) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM cita ci JOIN medico m ON m.id_medico = ci.id_medico "
                        + "WHERE ci.id_paciente = ? AND m.id_especialidad = ? AND ci.fecha >= ? "
                        + "AND ci.estado IN ('PENDIENTE', 'CONFIRMADA')",
                Integer.class, idPaciente, idEspecialidad, Date.valueOf(desde));
        return n != null && n > 0;
    }

    @Override
    public void actualizarEstado(Cita c) {
        jdbc.update("UPDATE cita SET estado = ?, motivo = ? WHERE id_cita = ?",
                c.getEstado().name(), c.getMotivo(), c.getId());
        // RN05: cada cambio queda registrado en auditoria (en MySQL tambien lo hace el trigger)
        jdbc.update("INSERT INTO auditoria (tabla, id_registro, accion, detalle) VALUES ('cita', ?, 'CAMBIO_ESTADO', ?)",
                c.getId(), "Nuevo estado: " + c.getEstado().name());
    }
}
