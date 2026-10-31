package pe.clinicasantaelena.citas.service.impl;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasantaelena.citas.dao.CitaDao;
import pe.clinicasantaelena.citas.dao.PacienteDao;
import pe.clinicasantaelena.citas.dto.BloqueHorario;
import pe.clinicasantaelena.citas.dto.SolicitudReserva;
import pe.clinicasantaelena.citas.exception.HorarioOcupadoException;
import pe.clinicasantaelena.citas.exception.RecursoNoEncontradoException;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.model.EstadoCita;
import pe.clinicasantaelena.citas.model.Medico;
import pe.clinicasantaelena.citas.model.Paciente;
import pe.clinicasantaelena.citas.service.AgendaService;
import pe.clinicasantaelena.citas.service.CatalogoService;
import pe.clinicasantaelena.citas.service.CitaService;
import pe.clinicasantaelena.citas.service.NotificacionService;
import pe.clinicasantaelena.citas.util.GeneradorCodigoCita;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Coordina la reserva de citas aplicando la doble barrera contra duplicados (RF07):
 * <ol>
 *   <li>Java: {@link AgendaService#bloqueLibre} verifica que el bloque exista y este libre.</li>
 *   <li>Base de datos: la restriccion UNIQUE {@code uq_cita_bloque} rechaza reservas simultaneas;
 *       el DAO la traduce a {@link HorarioOcupadoException}.</li>
 * </ol>
 */
@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaServiceImpl.class);

    private final CitaDao citaDao;
    private final PacienteDao pacienteDao;
    private final CatalogoService catalogoService;
    private final AgendaService agendaService;
    private final NotificacionService notificacionService;
    private final GeneradorCodigoCita generadorCodigo;
    private final Clock clock;

    /**
     * Inyeccion por constructor: todas las dependencias son interfaces (DIP).
     *
     * @param citaDao             DAO de citas
     * @param pacienteDao         DAO de pacientes
     * @param catalogoService     catalogos
     * @param agendaService       disponibilidad
     * @param notificacionService notificaciones
     * @param generadorCodigo     generador del codigo visible
     * @param clock               reloj del sistema
     */
    public CitaServiceImpl(CitaDao citaDao, PacienteDao pacienteDao, CatalogoService catalogoService,
                           AgendaService agendaService, NotificacionService notificacionService,
                           GeneradorCodigoCita generadorCodigo, Clock clock) {
        this.citaDao = citaDao;
        this.pacienteDao = pacienteDao;
        this.catalogoService = catalogoService;
        this.agendaService = agendaService;
        this.notificacionService = notificacionService;
        this.generadorCodigo = generadorCodigo;
        this.clock = clock;
    }

    @Override
    public Cita reservar(SolicitudReserva s) {
        Preconditions.checkNotNull(s, "La solicitud es obligatoria");
        Preconditions.checkArgument(s.idPaciente() != null && s.idMedico() != null
                && s.fecha() != null && s.hora() != null, "Faltan datos de la reserva");

        Paciente paciente = pacienteDao.buscarPorId(s.idPaciente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
        Medico medico = catalogoService.medicoActivo(s.idMedico());

        // 1.a barrera (Java): el bloque debe existir en el horario y estar libre
        BloqueHorario bloque = agendaService.bloqueLibre(medico.getId(), s.fecha(), s.hora())
                .orElseThrow(() -> new HorarioOcupadoException("Ese horario ya no está disponible. Elige otro."));

        // RN03: una sola cita vigente por especialidad
        if (citaDao.existeVigenteEnEspecialidad(paciente.getId(), medico.getIdEspecialidad(),
                LocalDate.now(clock))) {
            throw new ReglaNegocioException("Ya tienes una cita vigente en " + medico.getNombreEspecialidad()
                    + ". Cancélala o espera a ser atendido para reservar otra.");
        }

        Cita cita = new Cita();
        cita.setCodigo(generadorCodigo.siguiente());
        cita.setIdPaciente(paciente.getId());
        cita.setIdMedico(medico.getId());
        cita.setIdConsultorio(medico.getIdConsultorio());
        cita.setFecha(s.fecha());
        cita.setHoraInicio(bloque.getHora());
        cita.setHoraFin(bloque.getHoraFin());
        cita.setEstado(EstadoCita.CONFIRMADA);
        cita.setCanal(s.canal());
        cita.setMotivo(s.motivo());
        cita.setIdUsuarioRegistro(s.idUsuarioRegistro());
        cita.setFechaRegistro(LocalDateTime.now(clock));
        cita.setNombrePaciente(paciente.getNombreCompleto());
        cita.setNombreMedico(medico.getNombreCompleto());
        cita.setNombreEspecialidad(medico.getNombreEspecialidad());
        cita.setCodigoConsultorio(medico.getCodigoConsultorio());

        // 2.a barrera (BD): UNIQUE (id_medico, fecha, hora_inicio, slot_activo)
        citaDao.insertar(cita);

        log.info("Cita {} registrada: medico={}, fecha={} {}, canal={}", cita.getCodigo(), medico.getId(),
                cita.getFecha(), cita.getHoraInicio(), cita.getCanal());
        notificacionService.enviarConfirmacion(cita, paciente.getCorreo());
        return cita;
    }

    @Override
    public void cancelarPorPaciente(Long idCita, Long idPaciente, String motivo) {
        Cita cita = citaDelPaciente(idCita, idPaciente);
        cita.cancelar(motivo, LocalDateTime.now(clock)); // RN02 vive en la entidad
        citaDao.actualizarEstado(cita);
        log.info("Cita {} cancelada por el paciente", cita.getCodigo());
        pacienteDao.buscarPorId(idPaciente)
                .ifPresent(p -> notificacionService.enviarCancelacion(cita, p.getCorreo()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> citasDelPaciente(Long idPaciente) {
        return citaDao.listarPorPaciente(idPaciente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cita citaDelPaciente(Long idCita, Long idPaciente) {
        // Si la cita es de otro paciente se responde "no encontrada" para no revelar que existe (IDOR)
        return citaDao.buscarPorId(idCita)
                .filter(c -> c.getIdPaciente().equals(idPaciente))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> agendaDelMedico(Long idMedico, LocalDate fecha) {
        return citaDao.listarPorMedicoYFecha(idMedico, fecha);
    }

    @Override
    public void marcarAtendida(Long idCita, Long idMedico) {
        Cita cita = citaDelMedicoParaCerrar(idCita, idMedico);
        cita.marcarAtendida();
        citaDao.actualizarEstado(cita);
    }

    @Override
    public void marcarNoAsistio(Long idCita, Long idMedico) {
        Cita cita = citaDelMedicoParaCerrar(idCita, idMedico);
        cita.marcarNoAsistio();
        citaDao.actualizarEstado(cita);
    }

    private Cita citaDelMedicoParaCerrar(Long idCita, Long idMedico) {
        Cita cita = citaDao.buscarPorId(idCita)
                .filter(c -> c.getIdMedico().equals(idMedico))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada en su agenda"));
        if (cita.getFecha().isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("No se puede cerrar una cita de una fecha futura");
        }
        return cita;
    }
}
