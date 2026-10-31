package pe.clinicasantaelena.citas.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fila del reporte R01 "Citas por especialidad y estado".
 */
public class FilaReporte {

    private final String especialidad;
    private final long atendidas;
    private final long noAsistio;
    private final long canceladas;
    private final long vigentes;

    /**
     * @param especialidad nombre de la especialidad
     * @param atendidas    citas atendidas
     * @param noAsistio    inasistencias
     * @param canceladas   citas canceladas
     * @param vigentes     citas pendientes o confirmadas
     */
    public FilaReporte(String especialidad, long atendidas, long noAsistio, long canceladas, long vigentes) {
        this.especialidad = especialidad;
        this.atendidas = atendidas;
        this.noAsistio = noAsistio;
        this.canceladas = canceladas;
        this.vigentes = vigentes;
    }

    /** @return total de citas de la especialidad */
    public long getTotal() {
        return atendidas + noAsistio + canceladas + vigentes;
    }

    /**
     * Formula del reporte R02 aplicada por especialidad:
     * no asistio / (atendidas + no asistio) x 100.
     *
     * @return porcentaje con un decimal, o cero si no hay citas cerradas
     */
    public BigDecimal getPorcentajeAusentismo() {
        long cerradas = atendidas + noAsistio;
        if (cerradas == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(noAsistio * 100.0 / cerradas).setScale(1, RoundingMode.HALF_UP);
    }

    public String getEspecialidad() { return especialidad; }
    public long getAtendidas() { return atendidas; }
    public long getNoAsistio() { return noAsistio; }
    public long getCanceladas() { return canceladas; }
    public long getVigentes() { return vigentes; }
}
