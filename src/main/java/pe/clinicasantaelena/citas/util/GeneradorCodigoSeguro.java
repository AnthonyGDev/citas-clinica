package pe.clinicasantaelena.citas.util;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera codigos con {@link SecureRandom} para que no sean predecibles
 * (un codigo secuencial permitiria adivinar citas de otros pacientes).
 */
@Component
public class GeneradorCodigoSeguro implements GeneradorCodigoCita {

    /** Prefijo institucional de la Clinica Santa Elena. */
    public static final String PREFIJO = "CSE-";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String siguiente() {
        return PREFIJO + Strings.padStart(String.valueOf(random.nextInt(1_000_000)), 6, '0');
    }
}
