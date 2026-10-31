package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.dto.FilaReporte;

import java.time.LocalDate;
import java.util.List;

/** Reportes de gestion (RF10). */
public interface ReporteService {

    /**
     * Reporte R01: citas por especialidad y estado.
     *
     * @param desde fecha inicial
     * @param hasta fecha final
     * @return filas del reporte
     */
    List<FilaReporte> citasPorEspecialidad(LocalDate desde, LocalDate hasta);
}
