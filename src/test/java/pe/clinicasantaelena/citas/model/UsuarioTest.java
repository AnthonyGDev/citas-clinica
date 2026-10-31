package pe.clinicasantaelena.citas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de la regla RN04: bloqueo de 15 minutos tras 3 intentos fallidos. */
class UsuarioTest {

    private final LocalDateTime ahora = LocalDateTime.of(2026, 11, 2, 10, 0);

    @Test
    @DisplayName("RN04: al tercer intento fallido la cuenta se bloquea 15 minutos")
    void bloqueaAlTercerIntento() {
        Usuario u = new Usuario();
        u.registrarIntentoFallido(ahora);
        u.registrarIntentoFallido(ahora);
        assertFalse(u.estaBloqueado(ahora));
        assertEquals(1, u.intentosRestantes());

        u.registrarIntentoFallido(ahora);
        assertTrue(u.estaBloqueado(ahora.plusMinutes(14)));
        assertFalse(u.estaBloqueado(ahora.plusMinutes(15)));
    }

    @Test
    @DisplayName("Un ingreso exitoso reinicia el contador")
    void ingresoExitosoReinicia() {
        Usuario u = new Usuario();
        u.registrarIntentoFallido(ahora);
        u.registrarIntentoFallido(ahora);
        u.reiniciarIntentos();
        assertEquals(Usuario.MAX_INTENTOS, u.intentosRestantes());
    }
}
