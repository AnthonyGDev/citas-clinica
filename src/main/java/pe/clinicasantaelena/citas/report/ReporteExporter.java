package pe.clinicasantaelena.citas.report;

import pe.clinicasantaelena.citas.dto.FilaReporte;

import java.time.LocalDate;
import java.util.List;

/**
 * Estrategia de exportacion de reportes. Agregar un formato nuevo (por ejemplo PDF o CSV) solo requiere
 * crear otra implementacion: no se modifica el controlador (principio abierto/cerrado, OCP).
 */
public interface ReporteExporter {

    /** @return clave del formato usada en la URL ({@code xlsx}) */
    String formato();

    /** @return tipo MIME del archivo */
    String contentType();

    /**
     * @param filas datos del reporte
     * @param desde fecha inicial del filtro
     * @param hasta fecha final del filtro
     * @return contenido del archivo
     */
    byte[] exportar(List<FilaReporte> filas, LocalDate desde, LocalDate hasta);
}
