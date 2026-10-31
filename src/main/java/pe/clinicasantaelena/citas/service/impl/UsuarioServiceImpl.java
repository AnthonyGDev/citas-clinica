package pe.clinicasantaelena.citas.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasantaelena.citas.dao.PacienteDao;
import pe.clinicasantaelena.citas.dao.UsuarioDao;
import pe.clinicasantaelena.citas.dto.RegistroPacienteForm;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.model.EstadoUsuario;
import pe.clinicasantaelena.citas.model.Paciente;
import pe.clinicasantaelena.citas.model.Rol;
import pe.clinicasantaelena.citas.model.Usuario;
import pe.clinicasantaelena.citas.service.UsuarioService;
import pe.clinicasantaelena.citas.util.DatosSensibles;
import pe.clinicasantaelena.citas.util.PoliticaContrasena;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Implementacion de {@link UsuarioService}.
 *
 * <p>SRP: solo gestiona cuentas. DIP: depende de interfaces DAO y de {@link PasswordEncoder};
 * las implementaciones las inyecta Spring por constructor.</p>
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioDao usuarioDao;
    private final PacienteDao pacienteDao;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    /**
     * @param usuarioDao      DAO de usuarios
     * @param pacienteDao     DAO de pacientes
     * @param passwordEncoder codificador BCrypt
     * @param clock           reloj del sistema (inyectable para pruebas)
     */
    public UsuarioServiceImpl(UsuarioDao usuarioDao, PacienteDao pacienteDao,
                              PasswordEncoder passwordEncoder, Clock clock) {
        this.usuarioDao = usuarioDao;
        this.pacienteDao = pacienteDao;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public Paciente registrarPaciente(RegistroPacienteForm form) {
        String correo = normalizarCorreo(form.getCorreo());
        String dni = StringUtils.trimToEmpty(form.getDni());

        if (dni.length() != 8 || !StringUtils.isNumeric(dni)) {
            throw new ReglaNegocioException("El DNI debe tener 8 dígitos");
        }
        if (!form.isAceptaDatos()) {
            throw new ReglaNegocioException("Debes aceptar el tratamiento de tus datos personales");
        }
        if (!StringUtils.equals(form.getPassword(), form.getConfirmarPassword())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        List<String> errores = PoliticaContrasena.validar(form.getPassword(), dni);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException("Contraseña insegura: " + String.join(", ", errores));
        }
        if (usuarioDao.existeCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo");
        }
        if (pacienteDao.existeDni(dni)) {
            throw new ReglaNegocioException("Ya existe un paciente registrado con ese DNI");
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        usuario.setRol(Rol.PACIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        Long idUsuario = usuarioDao.insertar(usuario);

        Paciente paciente = new Paciente();
        paciente.setIdUsuario(idUsuario);
        paciente.setDni(dni);
        paciente.setNombres(StringUtils.normalizeSpace(form.getNombres()));
        paciente.setApellidos(StringUtils.normalizeSpace(form.getApellidos()));
        paciente.setTelefono(StringUtils.trimToNull(form.getTelefono()));
        paciente.setCorreo(correo);
        pacienteDao.insertar(paciente);

        log.info("Paciente registrado: dni={}, correo={}", DatosSensibles.enmascararDni(dni),
                DatosSensibles.enmascararCorreo(correo));
        return paciente;
    }

    @Override
    public void registrarIntentoFallido(String correo) {
        usuarioDao.buscarPorCorreo(normalizarCorreo(correo)).ifPresent(u -> {
            LocalDateTime ahora = LocalDateTime.now(clock);
            u.registrarIntentoFallido(ahora);
            usuarioDao.actualizarControlAcceso(u);
            if (u.estaBloqueado(ahora)) {
                log.warn("Cuenta bloqueada por intentos fallidos: {}", DatosSensibles.enmascararCorreo(correo));
            }
        });
    }

    @Override
    public void registrarIngresoExitoso(String correo) {
        usuarioDao.buscarPorCorreo(normalizarCorreo(correo)).ifPresent(u -> {
            if (u.getIntentosFallidos() > 0 || u.getBloqueadoHasta() != null) {
                u.reiniciarIntentos();
                usuarioDao.actualizarControlAcceso(u);
            }
        });
    }

    private static String normalizarCorreo(String correo) {
        return StringUtils.trimToEmpty(correo).toLowerCase(Locale.ROOT);
    }
}
