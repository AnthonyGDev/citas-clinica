package pe.clinicasantaelena.citas.util;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Politica minima de contrasenas: 8 a 64 caracteres, al menos una letra y un numero,
 * y que no contenga el DNI del usuario. Usa {@link CharMatcher} de Google Guava.
 */
public final class PoliticaContrasena {

    private PoliticaContrasena() { }

    /**
     * @param password contrasena propuesta
     * @param dni      DNI del usuario (puede ser nulo)
     * @return lista de incumplimientos; vacia si la contrasena es valida
     */
    public static List<String> validar(String password, String dni) {
        List<String> errores = new ArrayList<>();
        if (StringUtils.length(password) < 8 || StringUtils.length(password) > 64) {
            errores.add("Debe tener entre 8 y 64 caracteres");
        }
        if (password == null || !CharMatcher.inRange('0', '9').matchesAnyOf(password)) {
            errores.add("Debe incluir al menos un número");
        }
        if (password == null || !CharMatcher.javaLetter().matchesAnyOf(password)) {
            errores.add("Debe incluir al menos una letra");
        }
        if (StringUtils.isNotBlank(dni) && StringUtils.contains(password, dni)) {
            errores.add("No debe contener tu DNI");
        }
        return errores;
    }
}
