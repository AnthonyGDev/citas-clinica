package pe.clinicasantaelena.citas.service.impl;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasantaelena.citas.dao.ReporteDao;
import pe.clinicasantaelena.citas.dto.FilaReporte;
import pe.clinicasantaelena.citas.service.ReporteService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Implementacion de {@link ReporteService}. */
@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    /** Rango maximo permitido para evitar consultas muy pesadas. */
    static final long MAX_DIAS = 366;

    private final ReporteDao reporteDao;

    /** @param reporteDao DAO de reportes */
    public ReporteServiceImpl(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public List<FilaReporte> citasPorEspecialidad(LocalDate desde, LocalDate hasta) {
        Preconditions.checkArgument(desde != null && hasta != null, "Indica el rango de fechas");
        Preconditions.checkArgument(!hasta.isBefore(desde), "La fecha final debe ser posterior a la inicial");
        Preconditions.checkArgument(ChronoUnit.DAYS.between(desde, hasta) <= MAX_DIAS,
                "El rango no puede superar un año");
        return reporteDao.citasPorEspecialidad(desde, hasta);
    }
}
