package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.model.Usuario;

import java.util.Optional;

/**
 * Patron DAO: contrato de acceso a datos de {@link Usuario}.
 * Los servicios dependen de esta interfaz, no de la implementacion JDBC (principio DIP).
 */
public interface UsuarioDao {

    /** @param correo correo normalizado en minusculas @return el usuario, si existe */
    Optional<Usuario> buscarPorCorreo(String correo);

    /** @param correo correo normalizado @return {@code true} si ya esta registrado */
    boolean existeCorreo(String correo);

    /** @param usuario usuario nuevo @return id generado */
    Long insertar(Usuario usuario);

    /** Persiste el contador de intentos y la fecha de bloqueo (RN04). @param usuario usuario */
    void actualizarControlAcceso(Usuario usuario);
}
