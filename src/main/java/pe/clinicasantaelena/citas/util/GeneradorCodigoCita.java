package pe.clinicasantaelena.citas.util;

/**
 * Genera el codigo visible de la cita (por ejemplo {@code CSE-004812}).
 * Es una interfaz para poder reemplazarla por una version determinista en las pruebas (DIP).
 */
@FunctionalInterface
public interface GeneradorCodigoCita {

    /** @return un codigo nuevo */
    String siguiente();
}
