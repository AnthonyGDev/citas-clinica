package pe.clinicasantaelena.citas.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del formulario de registro de paciente (P02, RF01).
 * La validacion se hace dos veces: aqui (Bean Validation) y en el servicio (defensa en profundidad).
 */
public class RegistroPacienteForm {

    @NotBlank(message = "Ingresa tu DNI")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "Ingresa tus nombres")
    @Size(max = 80)
    private String nombres;

    @NotBlank(message = "Ingresa tus apellidos")
    @Size(max = 80)
    private String apellidos;

    @Pattern(regexp = "^$|9\\d{8}", message = "El celular debe tener 9 dígitos y empezar con 9")
    private String telefono;

    @NotBlank(message = "Ingresa tu correo")
    @Email(message = "Correo no válido")
    @Size(max = 120)
    private String correo;

    @NotBlank(message = "Crea una contraseña")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    private String password;

    private String confirmarPassword;

    @AssertTrue(message = "Debes aceptar el tratamiento de tus datos personales (Ley N.° 29733)")
    private boolean aceptaDatos;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
    public boolean isAceptaDatos() { return aceptaDatos; }
    public void setAceptaDatos(boolean aceptaDatos) { this.aceptaDatos = aceptaDatos; }
}
