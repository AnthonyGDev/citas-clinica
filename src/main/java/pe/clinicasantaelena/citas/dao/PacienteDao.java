package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.model.Paciente;

import java.util.Optional;

/** Patron DAO para {@link Paciente}. */
public interface PacienteDao {

    /** @param id id del paciente @return el paciente, si existe */
    Optional<Paciente> buscarPorId(Long id);

    /** @param idUsuario id de su cuenta @return el paciente, si existe */
    Optional<Paciente> buscarPorIdUsuario(Long idUsuario);

    /** @param dni DNI de 8 digitos @return {@code true} si ya existe */
    boolean existeDni(String dni);

    /** @param paciente paciente nuevo @return id generado */
    Long insertar(Paciente paciente);
}
