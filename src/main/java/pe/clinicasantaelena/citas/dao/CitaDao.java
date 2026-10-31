package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.exception.HorarioOcupadoException;
import pe.clinicasantaelena.citas.model.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Patron DAO para {@link Cita}. */
public interface CitaDao {

    /**
     * Inserta la cita.
     *
     * @param cita cita nueva
     * @return id generado
     * @throws HorarioOcupadoException si la restriccion UNIQUE de bloque activo se viola (2.ª barrera RF07)
     */
    Long insertar(Cita cita);

    /** @param id id de la cita @return la cita con datos de medico y especialidad */
    Optional<Cita> buscarPorId(Long id);

    /** @param idPaciente paciente @return sus citas, de la mas reciente a la mas antigua */
    List<Cita> listarPorPaciente(Long idPaciente);

    /** @param idMedico medico @param fecha dia @return citas del dia ordenadas por hora */
    List<Cita> listarPorMedicoYFecha(Long idMedico, LocalDate fecha);

    /** @param idMedico medico @param fecha dia @return horas de inicio ya ocupadas */
    Set<LocalTime> horasOcupadas(Long idMedico, LocalDate fecha);

    /**
     * Regla RN03.
     *
     * @param idPaciente     paciente
     * @param idEspecialidad especialidad
     * @param desde          fecha a partir de la cual una cita se considera vigente
     * @return {@code true} si ya tiene una cita vigente en esa especialidad
     */
    boolean existeVigenteEnEspecialidad(Long idPaciente, Long idEspecialidad, LocalDate desde);

    /** @param cita cita con su nuevo estado y motivo */
    void actualizarEstado(Cita cita);
}
