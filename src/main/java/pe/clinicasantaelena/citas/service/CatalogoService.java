package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.model.Especialidad;
import pe.clinicasantaelena.citas.model.Medico;

import java.util.List;

/** Consulta de especialidades y medicos (RF02). */
public interface CatalogoService {

    /** @return especialidades activas */
    List<Especialidad> especialidadesActivas();

    /** @param idEspecialidad especialidad @return medicos habilitados en ella (HU04) */
    List<Medico> medicosPorEspecialidad(Long idEspecialidad);

    /** @param idMedico medico @return medico existente y activo */
    Medico medicoActivo(Long idMedico);
}
