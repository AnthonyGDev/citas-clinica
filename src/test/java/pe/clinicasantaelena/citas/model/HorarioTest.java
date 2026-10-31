package pe.clinicasantaelena.citas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de la generacion de bloques (RN06). */
class HorarioTest {

    @Test
    @DisplayName("Un turno de 08:00 a 09:00 cada 20 minutos genera 3 bloques")
    void generaBloques() {
        Horario h = new Horario(1L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0), 20);
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(8, 20), LocalTime.of(8, 40)), h.generarBloques());
    }

    @Test
    @DisplayName("RN06: solo se aceptan duraciones de 15, 20, 30, 45 o 60 minutos")
    void rechazaDuracionNoPermitida() {
        assertThrows(IllegalArgumentException.class,
                () -> new Horario(1L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0), 25));
    }
}
