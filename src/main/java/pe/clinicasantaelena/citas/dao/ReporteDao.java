package pe.clinicasantaelena.citas.dao;

import pe.clinicasantaelena.citas.dto.FilaReporte;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO de solo lectura para reportes. Se separa de {@link CitaDao} para respetar el
 * principio de segregacion de interfaces (ISP): quien reporta no necesita insertar citas.
 */
public interface ReporteDao {

    /**
     * @param desde fecha inicial (inclusive)
     * @param hasta fecha final (inclusive)
     * @return una fila por especialidad con el conteo de citas por estado
     */
    List<FilaReporte> citasPorEspecialidad(LocalDate desde, LocalDate hasta);
}
