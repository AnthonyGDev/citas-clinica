package pe.clinicasantaelena.citas.dto;

import java.time.LocalTime;

/**
 * Bloque de la agenda de un medico tal como se muestra en P04: hora de inicio, hora de fin
 * y si esta libre.
 */
public class BloqueHorario {

    private final LocalTime hora;
    private final LocalTime horaFin;
    private final boolean libre;

    /**
     * @param hora    hora de inicio del bloque
     * @param horaFin hora de fin del bloque
     * @param libre   {@code true} si nadie lo ha reservado
     */
    public BloqueHorario(LocalTime hora, LocalTime horaFin, boolean libre) {
        this.hora = hora;
        this.horaFin = horaFin;
        this.libre = libre;
    }

    public LocalTime getHora() { return hora; }
    public LocalTime getHoraFin() { return horaFin; }
    public boolean isLibre() { return libre; }

    @Override
    public String toString() {
        return hora + (libre ? " libre" : " ocupado");
    }
}
