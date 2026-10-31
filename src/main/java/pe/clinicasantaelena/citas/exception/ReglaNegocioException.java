package pe.clinicasantaelena.citas.exception;

/**
 * Se lanza cuando una operacion viola una regla de negocio (RN01 a RN07).
 * El mensaje esta pensado para mostrarse directamente al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    /** @param mensaje mensaje legible para el usuario */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
