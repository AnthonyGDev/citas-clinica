package pe.clinicasantaelena.citas.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Paciente de la clinica. Solo se almacenan los datos minimos necesarios para la cita
 * (minimizacion de datos, Ley N.° 29733).
 */
public class Paciente extends Persona {

    private String dni;
    private LocalDate fechaNacimiento;
    private Long idUsuario;

    /**
     * Calcula la edad a una fecha dada.
     *
     * @param hoy fecha de referencia
     * @return edad en anios cumplidos, o 0 si no se registro la fecha de nacimiento
     */
    public int getEdad(LocalDate hoy) {
        return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, hoy).getYears();
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
