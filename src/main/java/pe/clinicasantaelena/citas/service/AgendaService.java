package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.dto.BloqueHorario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Calculo de disponibilidad de la agenda medica (RF03). */
public interface AgendaService {

    /**
     * Bloques del dia indicando cuales estan libres. No devuelve bloques pasados.
     *
     * @param idMedico medico
     * @param fecha    dia consultado
     * @return bloques ordenados por hora
     */
    List<BloqueHorario> bloquesDelDia(Long idMedico, LocalDate fecha);

    /**
     * @param idMedico medico
     * @param fecha    dia
     * @param hora     hora de inicio buscada
     * @return el bloque si existe en el horario del medico y esta libre
     */
    Optional<BloqueHorario> bloqueLibre(Long idMedico, LocalDate fecha, LocalTime hora);

    /** @param idMedico medico @return dias en que atiende (para resaltar el calendario) */
    List<DayOfWeek> diasDeAtencion(Long idMedico);
}
