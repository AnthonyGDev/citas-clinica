package pe.clinicasantaelena.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Sistema Web de Gestion de Citas Medicas de la Clinica Santa Elena.
 *
 * <p>La aplicacion sigue el patron MVC en capas:
 * {@code controller} (C) &rarr; {@code service} (reglas de negocio) &rarr;
 * {@code dao} (acceso a datos) &rarr; base de datos; las vistas (V) son plantillas Thymeleaf.</p>
 *
 * @author Huaraca Huarca Anthony Gean Franco
 * @version 0.3.0 (Avance 3)
 */
@SpringBootApplication
public class CitasApplication {

    /**
     * Arranca el contenedor de Spring Boot.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}
