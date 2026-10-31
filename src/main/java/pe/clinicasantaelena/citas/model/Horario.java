package pe.clinicasantaelena.citas.model;

import com.google.common.base.Preconditions;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Bloque semanal de atencion de un medico (por ejemplo, lunes de 08:00 a 12:00 cada 20 minutos).
 * Un horario no existe sin su medico (composicion).
 */
public class Horario {

    /** Duraciones permitidas por la regla RN06. */
    public static final Set<Integer> DURACIONES_PERMITIDAS = Set.of(15, 20, 30, 45, 60);

    private Long id;
    private Long idMedico;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionMin;
    private boolean activo = true;

    public Horario() { }

    /**
     * Crea un horario validando sus invariantes con Guava {@link Preconditions}.
     *
     * @param idMedico    medico al que pertenece
     * @param diaSemana   dia de la semana
     * @param horaInicio  inicio del turno
     * @param horaFin     fin del turno (exclusivo)
     * @param duracionMin duracion de cada bloque (RN06)
     */
    public Horario(Long idMedico, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin, int duracionMin) {
        Preconditions.checkNotNull(diaSemana, "El dia es obligatorio");
        Preconditions.checkArgument(horaInicio != null && horaFin != null && horaFin.isAfter(horaInicio),
                "La hora de fin debe ser posterior a la de inicio");
        Preconditions.checkArgument(DURACIONES_PERMITIDAS.contains(duracionMin),
                "Duracion no permitida: %s minutos", duracionMin);
        this.idMedico = idMedico;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionMin = duracionMin;
    }

    /**
     * Genera las horas de inicio de los bloques del turno.
     * Un bloque solo se genera si cabe completo antes de {@code horaFin}.
     *
     * @return lista inmutable de horas de inicio
     */
    public List<LocalTime> generarBloques() {
        if (!activo) {
            return Collections.emptyList();
        }
        List<LocalTime> bloques = new ArrayList<>();
        LocalTime t = horaInicio;
        while (!t.plusMinutes(duracionMin).isAfter(horaFin) && t.isBefore(horaFin)) {
            bloques.add(t);
            LocalTime siguiente = t.plusMinutes(duracionMin);
            if (siguiente.isBefore(t)) { // cruce de medianoche: se detiene
                break;
            }
            t = siguiente;
        }
        return List.copyOf(bloques);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdMedico() { return idMedico; }
    public void setIdMedico(Long idMedico) { this.idMedico = idMedico; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
