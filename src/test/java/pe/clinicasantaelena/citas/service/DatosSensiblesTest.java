package pe.clinicasantaelena.citas.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.clinicasantaelena.citas.util.DatosSensibles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pruebas del enmascarado de datos personales en los logs (Ley N.° 29733). */
class DatosSensiblesTest {

    @Test
    @DisplayName("Enmascara DNI y correo antes de escribirlos en el log")
    void enmascara() {
        assertEquals("72****78", DatosSensibles.enmascararDni("72345678"));
        assertEquals("ma***@gmail.com", DatosSensibles.enmascararCorreo("maria.torres@gmail.com"));
    }
}
