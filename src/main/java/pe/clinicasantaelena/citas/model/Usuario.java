package pe.clinicasantaelena.citas.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cuenta de acceso al sistema. Solo guarda el hash BCrypt de la contrasena, nunca la contrasena.
 *
 * <p>Encapsula la regla RN04: tras {@value #MAX_INTENTOS} intentos fallidos la cuenta
 * queda bloqueada {@link #DURACION_BLOQUEO}.</p>
 */
public class Usuario {

    /** Numero de intentos fallidos permitidos (RN04). */
    public static final int MAX_INTENTOS = 3;
    /** Tiempo de bloqueo tras superar los intentos (RN04). */
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private Long id;
    private String correo;
    private String passwordHash;
    private Rol rol;
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    /**
     * Registra un intento de inicio de sesion fallido y bloquea la cuenta si corresponde.
     *
     * @param ahora momento del intento
     */
    public void registrarIntentoFallido(LocalDateTime ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
            intentosFallidos = 0;
        }
    }

    /**
     * @param ahora momento de la consulta
     * @return {@code true} si la cuenta sigue bloqueada
     */
    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /** Limpia el contador tras un inicio de sesion exitoso. */
    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    /** @return intentos que quedan antes del bloqueo */
    public int intentosRestantes() {
        return Math.max(0, MAX_INTENTOS - intentosFallidos);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}
