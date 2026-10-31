package pe.clinicasantaelena.citas.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Manejo centralizado de errores. Nunca se muestra la traza de la excepcion al usuario
 * (buena practica de seguridad: no revelar detalles internos).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * @param ex    excepcion de recurso inexistente
     * @param model modelo de la vista
     * @return vista de error
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("titulo", "No encontramos lo que buscas");
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

    /**
     * @param ex    regla de negocio violada fuera de un formulario
     * @param model modelo de la vista
     * @return vista de error
     */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String reglaNegocio(ReglaNegocioException ex, Model model) {
        model.addAttribute("titulo", "No se pudo completar la operación");
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

    /**
     * Parametros invalidos detectados con Guava {@code Preconditions} (por ejemplo, un rango de fechas
     * del reporte mal ingresado).
     *
     * @param ex    excepcion de argumento invalido
     * @param model modelo de la vista
     * @return vista de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String argumentoInvalido(IllegalArgumentException ex, Model model) {
        model.addAttribute("titulo", "Datos no válidos");
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }
}
