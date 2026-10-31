package pe.clinicasantaelena.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos de la pantalla P04 "Reservar cita": especialidad, medico, fecha y bloque.
 */
public class ReservaForm {

    @NotNull(message = "Elige una especialidad")
    private Long idEspecialidad;

    @NotNull(message = "Elige un médico")
    private Long idMedico;

    @NotNull(message = "Elige una fecha")
    @FutureOrPresent(message = "La fecha no puede ser pasada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Elige un horario")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String motivo;

    public Long getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Long idEspecialidad) { this.idEspecialidad = idEspecialidad; }
    public Long getIdMedico() { return idMedico; }
    public void setIdMedico(Long idMedico) { this.idMedico = idMedico; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
