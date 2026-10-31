package pe.clinicasantaelena.citas.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de la regla RN02 (24 horas) y de los cambios de estado de la cita. */
class CitaTest {

    private Cita cita;

    @BeforeEach
    void setUp() {
        cita = new Cita();
        cita.setFecha(LocalDate.of(2026, 11, 10));
        cita.setHoraInicio(LocalTime.of(9, 0));
        cita.setEstado(EstadoCita.CONFIRMADA);
    }

    @Test
    @DisplayName("RN02: se puede cancelar con 24 horas o más de anticipación")
    void cancelaConAnticipacionSuficiente() {
        cita.cancelar("Viaje", LocalDateTime.of(2026, 11, 9, 9, 0));
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        assertEquals("Viaje", cita.getMotivo());
    }

    @Test
    @DisplayName("RN02: no se puede cancelar con menos de 24 horas")
    void noCancelaConMenosDe24Horas() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 9, 9, 1);
        assertFalse(cita.puedeModificarse(ahora));
        assertThrows(ReglaNegocioException.class, () -> cita.cancelar(null, ahora));
        assertEquals(EstadoCita.CONFIRMADA, cita.getEstado());
    }

    @Test
    @DisplayName("Una cita atendida ya no puede cancelarse ni volver a cerrarse")
    void citaAtendidaNoCambia() {
        cita.marcarAtendida();
        assertFalse(cita.puedeModificarse(LocalDateTime.of(2026, 11, 1, 8, 0)));
        assertThrows(ReglaNegocioException.class, cita::marcarNoAsistio);
    }

    @Test
    @DisplayName("Solo las citas activas ocupan el bloque (base del UNIQUE slot_activo)")
    void estadosQueOcupanBloque() {
        assertTrue(EstadoCita.CONFIRMADA.ocupaBloque());
        assertTrue(EstadoCita.ATENDIDA.ocupaBloque());
        assertFalse(EstadoCita.CANCELADA.ocupaBloque());
        assertFalse(EstadoCita.REPROGRAMADA.ocupaBloque());
    }
}
