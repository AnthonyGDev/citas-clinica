package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.model.Cita;

/**
 * Envio de notificaciones (RF06). En el Avance 3 la implementacion activa registra el aviso
 * en el log; la version SMTP se agregara sin modificar a quienes la usan (principio OCP).
 */
public interface NotificacionService {

    /** @param cita cita registrada @param correo destinatario */
    void enviarConfirmacion(Cita cita, String correo);

    /** @param cita cita cancelada @param correo destinatario */
    void enviarCancelacion(Cita cita, String correo);
}
