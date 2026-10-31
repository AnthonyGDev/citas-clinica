package pe.clinicasantaelena.citas.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.clinicasantaelena.citas.dao.MedicoDao;
import pe.clinicasantaelena.citas.dao.PacienteDao;
import pe.clinicasantaelena.citas.dao.UsuarioDao;
import pe.clinicasantaelena.citas.model.EstadoUsuario;
import pe.clinicasantaelena.citas.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Carga el usuario para Spring Security a partir de la tabla {@code usuario}.
 * Si la cuenta esta bloqueada (RN04) Spring Security rechaza el ingreso con {@code LockedException}
 * incluso antes de comparar la contrasena.
 */
@Service
public class ClinicaUserDetailsService implements UserDetailsService {

    private final UsuarioDao usuarioDao;
    private final PacienteDao pacienteDao;
    private final MedicoDao medicoDao;
    private final Clock clock;

    /**
     * @param usuarioDao  DAO de usuarios
     * @param pacienteDao DAO de pacientes
     * @param medicoDao   DAO de medicos
     * @param clock       reloj del sistema
     */
    public ClinicaUserDetailsService(UsuarioDao usuarioDao, PacienteDao pacienteDao, MedicoDao medicoDao,
                                     Clock clock) {
        this.usuarioDao = usuarioDao;
        this.pacienteDao = pacienteDao;
        this.medicoDao = medicoDao;
        this.clock = clock;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) {
        String normalizado = StringUtils.trimToEmpty(correo).toLowerCase(Locale.ROOT);
        Usuario u = usuarioDao.buscarPorCorreo(normalizado)
                // mensaje generico: no se revela si el correo existe
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        Long idPerfil = null;
        String nombre = u.getCorreo();
        switch (u.getRol()) {
            case PACIENTE -> {
                var p = pacienteDao.buscarPorIdUsuario(u.getId());
                if (p.isPresent()) {
                    idPerfil = p.get().getId();
                    nombre = p.get().getNombreCompleto();
                }
            }
            case MEDICO -> {
                var m = medicoDao.buscarPorIdUsuario(u.getId());
                if (m.isPresent()) {
                    idPerfil = m.get().getId();
                    nombre = m.get().getNombreCompleto();
                }
            }
            default -> nombre = "Administración";
        }
        return new UsuarioAutenticado(u.getId(), u.getCorreo(), u.getPasswordHash(), u.getRol(), idPerfil, nombre,
                u.getEstado() == EstadoUsuario.ACTIVO, !u.estaBloqueado(LocalDateTime.now(clock)));
    }
}
