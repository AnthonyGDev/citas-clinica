package pe.clinicasantaelena.citas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.clinicasantaelena.citas.dto.RegistroPacienteForm;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.fake.Fakes;
import pe.clinicasantaelena.citas.model.Rol;
import pe.clinicasantaelena.citas.model.Usuario;
import pe.clinicasantaelena.citas.service.impl.UsuarioServiceImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas del registro de pacientes (RF01) y del bloqueo por intentos (RN04). */
class UsuarioServiceImplTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), LIMA);

    private Fakes.UsuarioDaoEnMemoria usuarioDao;
    private Fakes.PacienteDaoEnMemoria pacienteDao;
    private UsuarioService servicio;

    @BeforeEach
    void setUp() {
        usuarioDao = new Fakes.UsuarioDaoEnMemoria();
        pacienteDao = new Fakes.PacienteDaoEnMemoria();
        servicio = new UsuarioServiceImpl(usuarioDao, pacienteDao, new Fakes.CodificadorFalso(), RELOJ);
    }

    @Test
    @DisplayName("RF01: registra paciente con rol PACIENTE, correo normalizado y contraseña cifrada")
    void registraPaciente() {
        servicio.registrarPaciente(form("72345678", " Maria.Torres@Correo.pe ", "Clinica2026"));

        Usuario u = usuarioDao.usuarios.get("maria.torres@correo.pe");
        assertEquals(Rol.PACIENTE, u.getRol());
        assertNotEquals("Clinica2026", u.getPasswordHash());
        assertTrue(pacienteDao.existeDni("72345678"));
    }

    @Test
    @DisplayName("RF01: no permite dos cuentas con el mismo DNI")
    void rechazaDniDuplicado() {
        servicio.registrarPaciente(form("72345678", "a@correo.pe", "Clinica2026"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarPaciente(form("72345678", "b@correo.pe", "Clinica2026")));
    }

    @Test
    @DisplayName("Seguridad: rechaza contraseñas sin números")
    void rechazaContrasenaDebil() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarPaciente(form("72345678", "a@correo.pe", "soloLetras")));
    }

    @Test
    @DisplayName("RN04: tres intentos fallidos bloquean la cuenta")
    void bloqueaTrasTresIntentos() {
        servicio.registrarPaciente(form("72345678", "a@correo.pe", "Clinica2026"));
        for (int i = 0; i < 3; i++) {
            servicio.registrarIntentoFallido("a@correo.pe");
        }
        assertTrue(usuarioDao.usuarios.get("a@correo.pe").estaBloqueado(LocalDateTime.now(RELOJ)));
    }

    private static RegistroPacienteForm form(String dni, String correo, String password) {
        RegistroPacienteForm f = new RegistroPacienteForm();
        f.setDni(dni);
        f.setNombres("María");
        f.setApellidos("Torres Salas");
        f.setCorreo(correo);
        f.setPassword(password);
        f.setConfirmarPassword(password);
        f.setAceptaDatos(true);
        return f;
    }
}
