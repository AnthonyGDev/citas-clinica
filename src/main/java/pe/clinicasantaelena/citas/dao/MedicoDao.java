package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.model.Medico;

import java.util.List;
import java.util.Optional;

/** Patron DAO para {@link Medico}. */
public interface MedicoDao {

    /** @param idEspecialidad especialidad @return medicos activos de esa especialidad */
    List<Medico> listarActivosPorEspecialidad(Long idEspecialidad);

    /** @param id id del medico @return el medico, si existe */
    Optional<Medico> buscarPorId(Long id);

    /** @param idUsuario id de su cuenta @return el medico, si existe */
    Optional<Medico> buscarPorIdUsuario(Long idUsuario);
}
