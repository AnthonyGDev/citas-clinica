package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.dto.SolicitudReserva;
import pe.clinicasantaelena.citas.model.Cita;

import java.time.LocalDate;
import java.util.List;

/** Casos de uso de citas (RF04, RF05, RF07, RF08). */
public interface CitaService {

    /** @param solicitud datos de la reserva @return la cita registrada con su codigo */
    Cita reservar(SolicitudReserva solicitud);

    /**
     * @param idCita     cita
     * @param idPaciente paciente que solicita (debe ser el duenio)
     * @param motivo     motivo opcional
     */
    void cancelarPorPaciente(Long idCita, Long idPaciente, String motivo);

    /** @param idPaciente paciente @return historial de citas */
    List<Cita> citasDelPaciente(Long idPaciente);

    /** @param idCita cita @param idPaciente duenio @return la cita, validando que le pertenezca */
    Cita citaDelPaciente(Long idCita, Long idPaciente);

    /** @param idMedico medico @param fecha dia @return agenda del dia (HU10) */
    List<Cita> agendaDelMedico(Long idMedico, LocalDate fecha);

    /** @param idCita cita @param idMedico medico que la atiende */
    void marcarAtendida(Long idCita, Long idMedico);

    /** @param idCita cita @param idMedico medico que la atiende */
    void marcarNoAsistio(Long idCita, Long idMedico);
}
