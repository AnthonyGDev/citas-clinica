package pe.clinicasantaelena.citas.exception;

/**
 * El bloque horario elegido ya fue tomado por otra cita (RF07 / RN01).
 * Puede originarse en la validacion Java o en la restriccion UNIQUE de la base de datos.
 */
public class HorarioOcupadoException extends ReglaNegocioException {

    /** @param mensaje mensaje legible para el usuario */
    public HorarioOcupadoException(String mensaje) {
        super(mensaje);
    }
}
