package pe.clinicasantaelena.citas.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.clinicasantaelena.citas.model.Rol;

import java.util.Collection;
import java.util.List;

/**
 * Usuario en sesion. Ademas de las credenciales guarda el id del perfil
 * (id de paciente o de medico) para que los controladores no consulten la BD en cada peticion.
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long idUsuario;
    private final String correo;
    private final String passwordHash;
    private final Rol rol;
    private final Long idPerfil;
    private final String nombreVisible;
    private final boolean activo;
    private final boolean noBloqueado;

    /**
     * @param idUsuario     id de la cuenta
     * @param correo        correo (nombre de usuario)
     * @param passwordHash  hash BCrypt
     * @param rol           rol
     * @param idPerfil      id del paciente o medico asociado (puede ser nulo)
     * @param nombreVisible nombre para la barra superior
     * @param activo        cuenta habilitada
     * @param noBloqueado   {@code false} si esta bloqueada por intentos (RN04)
     */
    public UsuarioAutenticado(Long idUsuario, String correo, String passwordHash, Rol rol, Long idPerfil,
                              String nombreVisible, boolean activo, boolean noBloqueado) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.idPerfil = idPerfil;
        this.nombreVisible = nombreVisible;
        this.activo = activo;
        this.noBloqueado = noBloqueado;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.autoridad()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return correo; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return noBloqueado; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return activo; }

    public Long getIdUsuario() { return idUsuario; }
    public Rol getRol() { return rol; }
    public Long getIdPerfil() { return idPerfil; }
    public String getNombreVisible() { return nombreVisible; }
}
