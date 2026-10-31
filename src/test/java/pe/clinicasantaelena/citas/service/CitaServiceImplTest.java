package pe.clinicasantaelena.citas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.clinicasantaelena.citas.dto.BloqueHorario;
import pe.clinicasantaelena.citas.dto.SolicitudReserva;
import pe.clinicasantaelena.citas.exception.HorarioOcupadoException;
import pe.clinicasantaelena.citas.exception.RecursoNoEncontradoException;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.fake.Fakes;
import pe.clinicasantaelena.citas.model.CanalReserva;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.model.EstadoCita;
import pe.clinicasantaelena.citas.model.Especialidad;
import pe.clinicasantaelena.citas.model.Horario;
import pe.clinicasantaelena.citas.model.Medico;
import pe.clinicasantaelena.citas.model.Paciente;
import pe.clinicasantaelena.citas.service.impl.AgendaServiceImpl;
import pe.clinicasantaelena.citas.service.impl.CitaServiceImpl;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso "Reservar cita" (RF04, RF07, RN01, RN02, RN03).
 * Se escribieron antes que {@link CitaServiceImpl} siguiendo el ciclo TDD: rojo, verde, refactorizar.
 */
class CitaServiceImplTest {

    // Lunes 02/11/2026 a las 10:00 (hora de Lima)
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Clock RELOJ = Clock.fixed(LocalDateTime.of(2026, 11, 2, 10, 0).atZone(LIMA).toInstant(), LIMA);
    private static final LocalDate JUEVES = LocalDate.of(2026, 11, 5);

    private Fakes.CitaDaoEnMemoria citaDao;
    private Fakes.NotificadorEspia notificador;
    private AgendaService agenda;
    private CitaService servicio;

    @BeforeEach
    void setUp() {
        citaDao = new Fakes.CitaDaoEnMemoria();
        citaDao.especialidadPorMedico.put(1L, 10L);
        Fakes.HorarioDaoEnMemoria horarioDao = new Fakes.HorarioDaoEnMemoria();
        horarioDao.horarios.add(new Horario(1L, DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(10, 0), 20));
        Fakes.PacienteDaoEnMemoria pacienteDao = new Fakes.PacienteDaoEnMemoria();
        pacienteDao.pacientes.put(1L, paciente(1L, "maria@correo.pe"));
        pacienteDao.pacientes.put(2L, paciente(2L, "jose@correo.pe"));
        notificador = new Fakes.NotificadorEspia();

        agenda = new AgendaServiceImpl(horarioDao, citaDao, RELOJ);
        servicio = new CitaServiceImpl(citaDao, pacienteDao, catalogoConUnMedico(), agenda, notificador,
                () -> "CSE-000001", RELOJ);
    }

    @Test
    @DisplayName("RF04: reserva un bloque libre, genera código y envía confirmación")
    void reservaBloqueLibre() {
        Cita cita = servicio.reservar(solicitud(1L, LocalTime.of(8, 20)));

        assertEquals("CSE-000001", cita.getCodigo());
        assertEquals(LocalTime.of(8, 40), cita.getHoraFin());
        assertEquals(EstadoCita.CONFIRMADA, cita.getEstado());
        assertEquals(1, notificador.confirmaciones);
    }

    @Test
    @DisplayName("RF07: el bloque reservado deja de aparecer como libre")
    void bloqueReservadoQuedaOcupado() {
        servicio.reservar(solicitud(1L, LocalTime.of(8, 20)));
        List<BloqueHorario> bloques = agenda.bloquesDelDia(1L, JUEVES);

        assertEquals(6, bloques.size());
        assertFalse(bloques.get(1).isLibre());
        assertTrue(bloques.get(0).isLibre());
    }

    @Test
    @DisplayName("RF07 / RN01: dos pacientes no pueden tomar el mismo bloque")
    void noPermiteDuplicidad() {
        servicio.reservar(solicitud(1L, LocalTime.of(8, 20)));
        assertThrows(HorarioOcupadoException.class, () -> servicio.reservar(solicitud(2L, LocalTime.of(8, 20))));
    }

    @Test
    @DisplayName("No se acepta una hora que no está en el horario del médico")
    void rechazaHoraFueraDelHorario() {
        assertThrows(HorarioOcupadoException.class, () -> servicio.reservar(solicitud(1L, LocalTime.of(8, 10))));
    }

    @Test
    @DisplayName("RN03: un paciente no puede tener dos citas vigentes en la misma especialidad")
    void unaCitaVigentePorEspecialidad() {
        servicio.reservar(solicitud(1L, LocalTime.of(8, 0)));
        assertThrows(ReglaNegocioException.class, () -> servicio.reservar(solicitud(1L, LocalTime.of(9, 0))));
    }

    @Test
    @DisplayName("RF05: al cancelar, el bloque vuelve a estar libre")
    void cancelarLiberaBloque() {
        Cita cita = servicio.reservar(solicitud(1L, LocalTime.of(8, 20)));
        servicio.cancelarPorPaciente(cita.getId(), 1L, "No podré asistir");

        assertEquals(EstadoCita.CANCELADA, citaDao.citas.get(cita.getId()).getEstado());
        assertTrue(agenda.bloqueLibre(1L, JUEVES, LocalTime.of(8, 20)).isPresent());
        assertEquals(1, notificador.cancelaciones);
    }

    @Test
    @DisplayName("Seguridad: un paciente no puede cancelar la cita de otro")
    void noCancelaCitaAjena() {
        Cita cita = servicio.reservar(solicitud(1L, LocalTime.of(8, 20)));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cancelarPorPaciente(cita.getId(), 2L, null));
    }

    private static SolicitudReserva solicitud(Long idPaciente, LocalTime hora) {
        return new SolicitudReserva(idPaciente, 1L, JUEVES, hora, null, CanalReserva.WEB, 99L);
    }

    private static Paciente paciente(Long id, String correo) {
        Paciente p = new Paciente();
        p.setId(id);
        p.setCorreo(correo);
        p.setNombres("Paciente");
        p.setApellidos(String.valueOf(id));
        return p;
    }

    private static CatalogoService catalogoConUnMedico() {
        Medico m = new Medico();
        m.setId(1L);
        m.setNombres("Luis");
        m.setApellidos("Ramos");
        m.setIdEspecialidad(10L);
        m.setNombreEspecialidad("Cardiología");
        m.setIdConsultorio(1L);
        m.setCodigoConsultorio("C-101");
        return new CatalogoService() {
            @Override
            public List<Especialidad> especialidadesActivas() { return List.of(); }

            @Override
            public List<Medico> medicosPorEspecialidad(Long idEspecialidad) { return List.of(m); }

            @Override
            public Medico medicoActivo(Long idMedico) {
                if (!idMedico.equals(1L)) {
                    throw new RecursoNoEncontradoException("No existe");
                }
                return m;
            }
        };
    }
}
