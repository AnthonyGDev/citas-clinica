package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.model.Especialidad;

import java.util.List;
import java.util.Optional;

/** Patron DAO para el catalogo de {@link Especialidad}. */
public interface EspecialidadDao {

    /** @return especialidades activas ordenadas por nombre */
    List<Especialidad> listarActivas();

    /** @param id id de la especialidad @return la especialidad, si existe */
    Optional<Especialidad> buscarPorId(Long id);
}
