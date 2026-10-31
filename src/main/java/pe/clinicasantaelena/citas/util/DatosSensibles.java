package pe.clinicasantaelena.citas.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Enmascara datos personales antes de escribirlos en los logs (Ley N.° 29733).
 * Ejemplo: DNI {@code 72345678} &rarr; {@code 72****78}; correo
 * {@code maria.torres@gmail.com} &rarr; {@code ma***@gmail.com}.
 */
public final class DatosSensibles {

    private DatosSensibles() { }

    /**
     * @param dni DNI completo
     * @return DNI con los 4 digitos centrales ocultos
     */
    public static String enmascararDni(String dni) {
        if (StringUtils.length(dni) < 4) {
            return "****";
        }
        return StringUtils.overlay(dni, StringUtils.repeat('*', dni.length() - 4), 2, dni.length() - 2);
    }

    /**
     * @param correo correo completo
     * @return correo con el usuario parcialmente oculto
     */
    public static String enmascararCorreo(String correo) {
        if (StringUtils.isBlank(correo) || !correo.contains("@")) {
            return "***";
        }
        String usuario = StringUtils.substringBefore(correo, "@");
        String dominio = StringUtils.substringAfter(correo, "@");
        return StringUtils.left(usuario, 2) + "***@" + dominio;
    }
}
