package pe.clinicasantaelena.citas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import pe.clinicasantaelena.citas.model.Rol;

/**
 * Configuracion de seguridad (RNF03):
 * <ul>
 *   <li>Autorizacion por rol en cada grupo de rutas.</li>
 *   <li>Contrasenas con BCrypt (factor 12).</li>
 *   <li>Proteccion CSRF activa (por defecto en Spring Security) en todos los formularios POST.</li>
 *   <li>Cabecera Content-Security-Policy: solo recursos propios, sin scripts en linea.</li>
 *   <li>Mensajes de error genericos y redireccion a pantalla de bloqueo (RN04).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @param http constructor de la cadena de filtros
     * @return cadena de filtros de seguridad
     * @throws Exception si la configuracion es invalida
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/paciente/**").hasRole(Rol.PACIENTE.name())
                .requestMatchers("/medico/**").hasRole(Rol.MEDICO.name())
                .requestMatchers("/admin/**").hasRole(Rol.ADMINISTRADOR.name())
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .successHandler(redireccionPorRol())
                .failureHandler(manejoDeFallo())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salir")
                .deleteCookies("JSESSIONID")
                .permitAll())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; "
                        + "frame-ancestors 'none'")));
        return http.build();
    }

    /** @return codificador BCrypt con factor de costo 12 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** @return lleva a cada usuario a la pantalla de inicio de su rol */
    @Bean
    public AuthenticationSuccessHandler redireccionPorRol() {
        return (request, response, authentication) -> {
            String destino = "/";
            if (authentication.getPrincipal() instanceof UsuarioAutenticado u) {
                destino = switch (u.getRol()) {
                    case PACIENTE -> "/paciente/inicio";
                    case MEDICO -> "/medico/agenda";
                    case ADMINISTRADOR -> "/admin/reportes";
                    case RECEPCIONISTA -> "/proximamente";
                };
            }
            response.sendRedirect(request.getContextPath() + destino);
        };
    }

    /** @return distingue cuenta bloqueada de credenciales incorrectas sin revelar si el correo existe */
    @Bean
    public AuthenticationFailureHandler manejoDeFallo() {
        return (request, response, exception) -> {
            String parametro = exception instanceof LockedException ? "bloqueado" : "error";
            response.sendRedirect(request.getContextPath() + "/login?" + parametro);
        };
    }
}
