package pe.clinicasantaelena.citas.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.clinicasantaelena.citas.dao.UsuarioDao;
import pe.clinicasantaelena.citas.model.EstadoUsuario;
import pe.clinicasantaelena.citas.model.Rol;
import pe.clinicasantaelena.citas.model.Usuario;

import java.sql.PreparedStatement;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link UsuarioDao}. Todas las consultas usan parametros
 * ({@code ?}), por lo que los datos del usuario nunca se concatenan al SQL (prevencion de inyeccion SQL).
 */
@Repository
public class JdbcUsuarioDao implements UsuarioDao {

    private static final String SELECT = "SELECT id_usuario, correo, password_hash, rol, estado, "
            + "intentos_fallidos, bloqueado_hasta FROM usuario ";

    private static final RowMapper<Usuario> MAPPER = (rs, n) -> {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id_usuario"));
        u.setCorreo(rs.getString("correo"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setEstado(EstadoUsuario.valueOf(rs.getString("estado")));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setBloqueadoHasta(JdbcSupport.fechaHora(rs, "bloqueado_hasta"));
        return u;
    };

    private final JdbcTemplate jdbc;

    /** @param jdbc plantilla JDBC inyectada por Spring */
    public JdbcUsuarioDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jdbc.query(SELECT + "WHERE correo = ?", MAPPER, correo).stream().findFirst();
    }

    @Override
    public boolean existeCorreo(String correo) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE correo = ?", Integer.class, correo);
        return n != null && n > 0;
    }

    @Override
    public Long insertar(Usuario u) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuario (correo, password_hash, rol, estado, intentos_fallidos) VALUES (?, ?, ?, ?, 0)",
                    new String[]{"id_usuario"});
            ps.setString(1, u.getCorreo());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRol().name());
            ps.setString(4, u.getEstado().name());
            return ps;
        }, kh);
        Long id = JdbcSupport.idGenerado(kh, "id_usuario");
        u.setId(id);
        return id;
    }

    @Override
    public void actualizarControlAcceso(Usuario u) {
        jdbc.update("UPDATE usuario SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id_usuario = ?",
                u.getIntentosFallidos(), JdbcSupport.timestamp(u.getBloqueadoHasta()), u.getId());
    }
}
