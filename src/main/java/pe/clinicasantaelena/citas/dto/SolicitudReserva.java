package pe.clinicasantaelena.citas.dto;

import pe.clinicasantaelena.citas.model.CanalReserva;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Comando inmutable que recibe {@code CitaService.reservar}. Desacopla el servicio del
 * formulario web, de modo que la misma logica sirve para la web y para recepcion.
 *
 * @param idPaciente        paciente que se atiende
 * @param idMedico          medico elegido
 * @param fecha             fecha de la cita
 * @param hora              hora de inicio del bloque
 * @param motivo            motivo de consulta (opcional)
 * @param canal             canal de ingreso
 * @param idUsuarioRegistro usuario que registra (paciente o recepcionista)
 */
public record SolicitudReserva(Long idPaciente, Long idMedico, LocalDate fecha, LocalTime hora,
                               String motivo, CanalReserva canal, Long idUsuarioRegistro) {
}
