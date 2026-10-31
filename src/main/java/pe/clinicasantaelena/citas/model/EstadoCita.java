package pe.clinicasantaelena.citas.model;

/**
 * Estados posibles de una cita. Se usa una enumeracion en lugar de textos libres
 * para que el compilador impida estados invalidos.
 */
public enum EstadoCita {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    NO_ASISTIO("No asistió"),
    REPROGRAMADA("Reprogramada");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return texto legible para las vistas */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Indica si la cita ocupa su bloque horario (regla RN01).
     *
     * @return {@code true} si el bloque no puede ser tomado por otra cita
     */
    public boolean ocupaBloque() {
        return this != CANCELADA && this != REPROGRAMADA;
    }

    /** @return {@code true} si la cita aun puede ser atendida, cancelada o reprogramada */
    public boolean esVigente() {
        return this == PENDIENTE || this == CONFIRMADA;
    }
}
