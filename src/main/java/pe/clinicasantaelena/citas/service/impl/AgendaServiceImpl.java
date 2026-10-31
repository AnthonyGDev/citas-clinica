package pe.clinicasantaelena.citas.service.impl;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasantaelena.citas.dao.CitaDao;
import pe.clinicasantaelena.citas.dao.HorarioDao;
import pe.clinicasantaelena.citas.dto.BloqueHorario;
import pe.clinicasantaelena.citas.model.Horario;
import pe.clinicasantaelena.citas.service.AgendaService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Calcula los bloques libres combinando los horarios del medico con las citas que ocupan bloque.
 */
@Service
@Transactional(readOnly = true)
public class AgendaServiceImpl implements AgendaService {

    private final HorarioDao horarioDao;
    private final CitaDao citaDao;
    private final Clock clock;

    /**
     * @param horarioDao DAO de horarios
     * @param citaDao    DAO de citas
     * @param clock      reloj del sistema
     */
    public AgendaServiceImpl(HorarioDao horarioDao, CitaDao citaDao, Clock clock) {
        this.horarioDao = horarioDao;
        this.citaDao = citaDao;
        this.clock = clock;
    }

    @Override
    public List<BloqueHorario> bloquesDelDia(Long idMedico, LocalDate fecha) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (idMedico == null || fecha == null || fecha.isBefore(ahora.toLocalDate())) {
            return List.of();
        }
        List<Horario> horarios = horarioDao.listarPorMedicoYDia(idMedico, fecha.getDayOfWeek());
        if (horarios.isEmpty()) {
            return List.of();
        }
        Set<LocalTime> ocupadas = citaDao.horasOcupadas(idMedico, fecha);
        boolean esHoy = fecha.equals(ahora.toLocalDate());

        ImmutableList.Builder<BloqueHorario> bloques = ImmutableList.builder();
        for (Horario h : horarios) {
            for (LocalTime inicio : h.generarBloques()) {
                if (esHoy && !inicio.isAfter(ahora.toLocalTime())) {
                    continue; // no se ofrecen bloques que ya pasaron
                }
                bloques.add(new BloqueHorario(inicio, inicio.plusMinutes(h.getDuracionMin()),
                        !ocupadas.contains(inicio)));
            }
        }
        return bloques.build().stream()
                .sorted(Comparator.comparing(BloqueHorario::getHora))
                .toList();
    }

    @Override
    public Optional<BloqueHorario> bloqueLibre(Long idMedico, LocalDate fecha, LocalTime hora) {
        return bloquesDelDia(idMedico, fecha).stream()
                .filter(b -> b.getHora().equals(hora) && b.isLibre())
                .findFirst();
    }

    @Override
    public List<DayOfWeek> diasDeAtencion(Long idMedico) {
        return idMedico == null ? List.of() : horarioDao.diasDeAtencion(idMedico);
    }
}
