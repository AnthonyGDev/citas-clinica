package pe.clinicasantaelena.citas.model;

/**
 * Roles del sistema. Cada rol ve solo el menu que le corresponde (control de acceso por roles, RNF03).
 */
public enum Rol {
    /** Paciente que reserva sus propias citas. */
    PACIENTE,
    /** Personal de admision que registra citas presenciales y telefonicas. */
    RECEPCIONISTA,
    /** Medico que consulta su agenda diaria. */
    MEDICO,
    /** Administrador: catalogos y reportes. */
    ADMINISTRADOR;

    /**
     * Nombre de autoridad que usa Spring Security ({@code ROLE_PACIENTE}, ...).
     *
     * @return nombre con prefijo {@code ROLE_}
     */
    public String autoridad() {
        return "ROLE_" + name();
    }
}
