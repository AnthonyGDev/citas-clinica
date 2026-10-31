package pe.clinicasantaelena.citas.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import pe.clinicasantaelena.citas.service.UsuarioService;

/**
 * Escucha los eventos de autenticacion de Spring Security para aplicar la regla RN04
 * (bloqueo tras 3 intentos fallidos).
 */
@Component
public class AutenticacionListener {

    private final UsuarioService usuarioService;

    /** @param usuarioService servicio de usuarios */
    public AutenticacionListener(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** @param evento contrasena incorrecta */
    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        usuarioService.registrarIntentoFallido(evento.getAuthentication().getName());
    }

    /** @param evento ingreso correcto */
    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        usuarioService.registrarIngresoExitoso(evento.getAuthentication().getName());
    }
}
