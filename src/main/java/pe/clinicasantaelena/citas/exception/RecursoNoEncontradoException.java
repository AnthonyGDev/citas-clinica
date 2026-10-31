package pe.clinicasantaelena.citas.exception;

/** El registro solicitado no existe o el usuario no tiene permiso para verlo. */
public class RecursoNoEncontradoException extends RuntimeException {

    /** @param mensaje descripcion del recurso no encontrado */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
