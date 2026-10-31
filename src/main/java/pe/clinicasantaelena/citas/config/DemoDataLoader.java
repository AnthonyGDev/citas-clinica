package pe.clinicasantaelena.citas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pe.clinicasantaelena.citas.dao.HorarioDao;
import pe.clinicasantaelena.citas.model.Horario;

import java.sql.Date;
import java.sql.Time;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Genera citas de demostracion relativas a la fecha actual (45 dias atras y 14 hacia adelante),
 * para que la agenda, el historial y los reportes tengan datos al presentar el avance.
 * Solo se ejecuta en el perfil {@code dev}.
 */
@Component
@Profile("dev")
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);
    private static final String INSERT = "INSERT INTO cita (codigo, id_paciente, id_medico, id_consultorio, fecha, "
            + "hora_inicio, hora_fin, estado, canal, motivo, id_usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /** Consultorio asignado a cada medico (indice = id_medico). */
    private static final long[] CONSULTORIO = {0, 1, 1, 2, 3, 4, 5, 6, 2};
    private static final long ID_PACIENTE_DEMO = 1L;
    private static final long ID_USUARIO_DEMO = 10L;
    private static final long ID_ADMIN = 1L;

    private final JdbcTemplate jdbc;
    private final HorarioDao horarioDao;
    private final Clock clock;
    private final Random rnd = new Random(2026);
    private final Set<String> ocupados = new HashSet<>();
    private final List<Object[]> filas = new ArrayList<>();
    private int secuencia = 100_001;

    /**
     * @param jdbc       plantilla JDBC
     * @param horarioDao DAO de horarios
     * @param clock      reloj
     */
    public DemoDataLoader(JdbcTemplate jdbc, HorarioDao horarioDao, Clock clock) {
        this.jdbc = jdbc;
        this.horarioDao = horarioDao;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        Integer existentes = jdbc.queryForObject("SELECT COUNT(*) FROM cita", Integer.class);
        if (existentes != null && existentes > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now(clock);
        citasDeLaPacienteDemo(hoy);
        for (long medico = 1; medico <= 8; medico++) {
            for (LocalDate d = hoy.minusDays(45); !d.isAfter(hoy.plusDays(14)); d = d.plusDays(1)) {
                for (Horario h : horarioDao.listarPorMedicoYDia(medico, d.getDayOfWeek())) {
                    for (LocalTime t : h.generarBloques()) {
                        generarAleatoria(medico, d, t, h.getDuracionMin(), hoy);
                    }
                }
            }
        }
        jdbc.batchUpdate(INSERT, filas);
        log.info("Datos demo: {} citas generadas", filas.size());
    }

    private void citasDeLaPacienteDemo(LocalDate hoy) {
        // Historial: una atendida y una cancelada
        agregar(1L, 3L, diaDeAtencion(3L, hoy.minusDays(30)), LocalTime.of(9, 0), 20, "ATENDIDA", "WEB",
                "Chequeo general");
        agregar(1L, 5L, diaDeAtencion(5L, hoy.minusDays(12)), LocalTime.of(10, 0), 30, "CANCELADA", "WEB",
                "Viaje de trabajo");
        // Proxima cita cancelable (mas de 24 h)
        agregar(1L, 1L, diaDeAtencion(1L, hoy.plusDays(5)), LocalTime.of(9, 0), 20, "CONFIRMADA", "WEB",
                "Control de presion arterial");
        // Cita cercana: muestra el bloqueo de la regla de 24 h
        agregar(1L, 3L, diaDeAtencion(3L, hoy.plusDays(1)), LocalTime.of(8, 0), 20, "CONFIRMADA", "TELEFONO",
                "Resultados de laboratorio");
    }

    private void generarAleatoria(long medico, LocalDate d, LocalTime t, int dur, LocalDate hoy) {
        long dias = d.toEpochDay() - hoy.toEpochDay();
        double probabilidad = dias < 0 ? 0.70 : dias == 0 ? 0.60 : Math.max(0.15, 0.50 - dias * 0.025);
        if (rnd.nextDouble() > probabilidad) {
            return;
        }
        String estado = "CONFIRMADA";
        if (dias < 0) {
            double r = rnd.nextDouble();
            estado = r < 0.76 ? "ATENDIDA" : r < 0.88 ? "NO_ASISTIO" : "CANCELADA";
        }
        long paciente = 2 + rnd.nextInt(20); // pacientes 2..21
        String canal = rnd.nextDouble() < 0.55 ? "PRESENCIAL" : "TELEFONO";
        agregar(paciente, medico, d, t, dur, estado, canal, null);
    }

    private void agregar(long paciente, long medico, LocalDate d, LocalTime t, int dur, String estado,
                         String canal, String motivo) {
        String clave = medico + "|" + d + "|" + t;
        if (!ocupados.add(clave)) {
            return;
        }
        long registra = paciente == ID_PACIENTE_DEMO && "WEB".equals(canal) ? ID_USUARIO_DEMO : ID_ADMIN;
        filas.add(new Object[]{"CSE-" + secuencia++, paciente, medico, CONSULTORIO[(int) medico], Date.valueOf(d),
                Time.valueOf(t), Time.valueOf(t.plusMinutes(dur)), estado, canal, motivo, registra});
    }

    /** Primer dia, a partir de {@code desde}, en que el medico atiende. */
    private LocalDate diaDeAtencion(long medico, LocalDate desde) {
        List<DayOfWeek> dias = horarioDao.diasDeAtencion(medico);
        LocalDate d = desde;
        while (!dias.contains(d.getDayOfWeek())) {
            d = d.plusDays(1);
        }
        return d;
    }
}
