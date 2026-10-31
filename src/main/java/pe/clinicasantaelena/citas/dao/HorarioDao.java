package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.model.Horario;

import java.time.DayOfWeek;
import java.util.List;

/** Patron DAO para {@link Horario}. */
public interface HorarioDao {

    /**
     * @param idMedico medico
     * @param dia      dia de la semana
     * @return horarios activos del medico ese dia
     */
    List<Horario> listarPorMedicoYDia(Long idMedico, DayOfWeek dia);

    /** @param idMedico medico @return dias de la semana en que atiende */
    List<DayOfWeek> diasDeAtencion(Long idMedico);
}
