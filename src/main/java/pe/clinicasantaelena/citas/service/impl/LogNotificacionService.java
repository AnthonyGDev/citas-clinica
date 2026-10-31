package pe.clinicasantaelena.citas.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.service.NotificacionService;
import pe.clinicasantaelena.citas.util.DatosSensibles;

/**
 * Implementacion provisional de {@link NotificacionService} para el Avance 3: registra el aviso
 * en el log de notificaciones (Logback) con el correo enmascarado. En la entrega final se
 * reemplazara por una implementacion SMTP sin cambiar {@code CitaServiceImpl} (OCP + DIP).
 */
@Service
public class LogNotificacionService implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger("NOTIFICACIONES");

    @Override
    public void enviarConfirmacion(Cita cita, String correo) {
        log.info("[CONFIRMACION] cita={} para={} fecha={} hora={} consultorio={}", cita.getCodigo(),
                DatosSensibles.enmascararCorreo(correo), cita.getFecha(), cita.getHoraInicio(),
                cita.getCodigoConsultorio());
    }

    @Override
    public void enviarCancelacion(Cita cita, String correo) {
        log.info("[CANCELACION] cita={} para={}", cita.getCodigo(), DatosSensibles.enmascararCorreo(correo));
    }
}
