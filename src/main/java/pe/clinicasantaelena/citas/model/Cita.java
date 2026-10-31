package pe.clinicasantaelena.citas.model;

import pe.clinicasantaelena.citas.exception.ReglaNegocioException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Nucleo del dominio: una cita entre un paciente y un medico en un bloque horario.
 *
 * <p>El estado solo cambia mediante metodos de negocio ({@link #cancelar(String, LocalDateTime)},
 * {@link #marcarAtendida()}, {@link #marcarNoAsistio()}), de modo que las reglas
 * (por ejemplo, la de 24 horas, RN02) viven en un solo lugar y no en las pantallas.</p>
 */
public class Cita {

    /** Anticipacion minima para que el paciente modifique su cita (RN02). */
    public static final Duration ANTICIPACION_MINIMA = Duration.ofHours(24);

    private Long id;
    private String codigo;
    private Long idPaciente;
    private Long idMedico;
    private Long idConsultorio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoCita estado = EstadoCita.CONFIRMADA;
    private CanalReserva canal = CanalReserva.WEB;
    private String motivo;
    private Long idUsuarioRegistro;
    private LocalDateTime fechaRegistro;

    // Datos de solo lectura para las vistas (se llenan con JOIN en el DAO)
    private String nombrePaciente;
    private String nombreMedico;
    private String nombreEspecialidad;
    private String codigoConsultorio;

    /** @return fecha y hora de inicio de la atencion */
    public LocalDateTime inicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    /**
     * Regla RN02: el paciente solo puede cancelar o reprogramar hasta 24 horas antes.
     *
     * @param ahora momento actual
     * @return {@code true} si la cita esta vigente y faltan al menos 24 horas
     */
    public boolean puedeModificarse(LocalDateTime ahora) {
        return estado.esVigente() && !ahora.plus(ANTICIPACION_MINIMA).isAfter(inicio());
    }

    /**
     * Cancela la cita y libera el bloque (RN05: la cita no se elimina, cambia de estado).
     *
     * @param motivoCancelacion texto opcional
     * @param ahora             momento actual
     * @throws ReglaNegocioException si faltan menos de 24 horas o la cita no esta vigente
     */
    public void cancelar(String motivoCancelacion, LocalDateTime ahora) {
        if (!puedeModificarse(ahora)) {
            throw new ReglaNegocioException(
                    "Solo puedes cancelar hasta 24 horas antes. Comunícate con recepción.");
        }
        this.estado = EstadoCita.CANCELADA;
        this.motivo = motivoCancelacion;
    }

    /** Marca la cita como atendida (lo hace el medico). */
    public void marcarAtendida() {
        exigirVigente();
        this.estado = EstadoCita.ATENDIDA;
    }

    /** Marca la inasistencia del paciente (insumo del reporte R02). */
    public void marcarNoAsistio() {
        exigirVigente();
        this.estado = EstadoCita.NO_ASISTIO;
    }

    private void exigirVigente() {
        if (!estado.esVigente()) {
            throw new ReglaNegocioException("La cita ya fue " + estado.getEtiqueta().toLowerCase());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Long getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Long idPaciente) { this.idPaciente = idPaciente; }
    public Long getIdMedico() { return idMedico; }
    public void setIdMedico(Long idMedico) { this.idMedico = idMedico; }
    public Long getIdConsultorio() { return idConsultorio; }
    public void setIdConsultorio(Long idConsultorio) { this.idConsultorio = idConsultorio; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public CanalReserva getCanal() { return canal; }
    public void setCanal(CanalReserva canal) { this.canal = canal; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Long idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }
    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String nombreEspecialidad) { this.nombreEspecialidad = nombreEspecialidad; }
    public String getCodigoConsultorio() { return codigoConsultorio; }
    public void setCodigoConsultorio(String codigoConsultorio) { this.codigoConsultorio = codigoConsultorio; }
}
