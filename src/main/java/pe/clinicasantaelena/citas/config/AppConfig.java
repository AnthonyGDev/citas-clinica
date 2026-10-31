package pe.clinicasantaelena.citas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Beans generales de la aplicacion.
 */
@Configuration
public class AppConfig {

    /** Zona horaria de la clinica. */
    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    /**
     * Reloj inyectable: en produccion usa la hora de Lima; en las pruebas se reemplaza por
     * un {@link Clock#fixed} para que las reglas de tiempo (24 h, bloqueo 15 min) sean verificables.
     *
     * @return reloj del sistema en la zona de Lima
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZONA_LIMA);
    }
}
