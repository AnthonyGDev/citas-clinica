package pe.clinicasantaelena.citas.fake;

import org.springframework.security.crypto.password.PasswordEncoder;
import pe.clinicasantaelena.citas.dao.CitaDao;
import pe.clinicasantaelena.citas.dao.HorarioDao;
import pe.clinicasantaelena.citas.dao.PacienteDao;
import pe.clinicasantaelena.citas.dao.UsuarioDao;
import pe.clinicasantaelena.citas.exception.HorarioOcupadoException;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.model.Horario;
import pe.clinicasantaelena.citas.model.Paciente;
import pe.clinicasantaelena.citas.model.Usuario;
import pe.clinicasantaelena.citas.service.NotificacionService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementaciones en memoria de los DAO para las pruebas unitarias.
 * Gracias a que los servicios dependen de interfaces (DIP), se prueban sin base de datos.
 */
public final class Fakes {

    private Fakes() { }

    /** DAO de citas en memoria que simula tambien la restriccion UNIQUE de bloque. */
    public static class CitaDaoEnMemoria implements CitaDao {
        public final Map<Long, Cita> citas = new HashMap<>();
        public final Map<Long, Long> especialidadPorMedico = new HashMap<>();
        private long secuencia = 1;

        @Override
        public Long insertar(Cita c) {
            boolean ocupado = citas.values().stream().anyMatch(x -> x.getEstado().ocupaBloque()
                    && x.getIdMedico().equals(c.getIdMedico()) && x.getFecha().equals(c.getFecha())
                    && x.getHoraInicio().equals(c.getHoraInicio()));
            if (ocupado) {
                throw new HorarioOcupadoException("Ese horario acaba de ser reservado");
            }
            c.setId(secuencia++);
            citas.put(c.getId(), c);
            return c.getId();
        }

        @Override
        public Optional<Cita> buscarPorId(Long id) { return Optional.ofNullable(citas.get(id)); }

        @Override
        public List<Cita> listarPorPaciente(Long idPaciente) {
            return citas.values().stream().filter(c -> c.getIdPaciente().equals(idPaciente)).toList();
        }

        @Override
        public List<Cita> listarPorMedicoYFecha(Long idMedico, LocalDate fecha) {
            return citas.values().stream().filter(c -> c.getIdMedico().equals(idMedico) && c.getFecha().equals(fecha))
                    .toList();
        }

        @Override
        public Set<LocalTime> horasOcupadas(Long idMedico, LocalDate fecha) {
            return citas.values().stream().filter(c -> c.getEstado().ocupaBloque() && c.getIdMedico().equals(idMedico)
                    && c.getFecha().equals(fecha)).map(Cita::getHoraInicio).collect(Collectors.toSet());
        }

        @Override
        public boolean existeVigenteEnEspecialidad(Long idPaciente, Long idEspecialidad, LocalDate desde) {
            return citas.values().stream().anyMatch(c -> c.getIdPaciente().equals(idPaciente)
                    && idEspecialidad.equals(especialidadPorMedico.get(c.getIdMedico()))
                    && !c.getFecha().isBefore(desde) && c.getEstado().esVigente());
        }

        @Override
        public void actualizarEstado(Cita cita) { citas.put(cita.getId(), cita); }
    }

    /** DAO de horarios en memoria. */
    public static class HorarioDaoEnMemoria implements HorarioDao {
        public final List<Horario> horarios = new ArrayList<>();

        @Override
        public List<Horario> listarPorMedicoYDia(Long idMedico, DayOfWeek dia) {
            return horarios.stream().filter(h -> h.getIdMedico().equals(idMedico) && h.getDiaSemana() == dia).toList();
        }

        @Override
        public List<DayOfWeek> diasDeAtencion(Long idMedico) {
            return horarios.stream().filter(h -> h.getIdMedico().equals(idMedico)).map(Horario::getDiaSemana)
                    .distinct().toList();
        }
    }

    /** DAO de pacientes en memoria. */
    public static class PacienteDaoEnMemoria implements PacienteDao {
        public final Map<Long, Paciente> pacientes = new HashMap<>();

        @Override
        public Optional<Paciente> buscarPorId(Long id) { return Optional.ofNullable(pacientes.get(id)); }

        @Override
        public Optional<Paciente> buscarPorIdUsuario(Long idUsuario) {
            return pacientes.values().stream().filter(p -> idUsuario.equals(p.getIdUsuario())).findFirst();
        }

        @Override
        public boolean existeDni(String dni) {
            return pacientes.values().stream().anyMatch(p -> p.getDni().equals(dni));
        }

        @Override
        public Long insertar(Paciente p) {
            p.setId((long) pacientes.size() + 1);
            pacientes.put(p.getId(), p);
            return p.getId();
        }
    }

    /** DAO de usuarios en memoria. */
    public static class UsuarioDaoEnMemoria implements UsuarioDao {
        public final Map<String, Usuario> usuarios = new HashMap<>();

        @Override
        public Optional<Usuario> buscarPorCorreo(String correo) { return Optional.ofNullable(usuarios.get(correo)); }

        @Override
        public boolean existeCorreo(String correo) { return usuarios.containsKey(correo); }

        @Override
        public Long insertar(Usuario u) {
            u.setId((long) usuarios.size() + 1);
            usuarios.put(u.getCorreo(), u);
            return u.getId();
        }

        @Override
        public void actualizarControlAcceso(Usuario u) { usuarios.put(u.getCorreo(), u); }
    }

    /** Notificador que solo cuenta los envios. */
    public static class NotificadorEspia implements NotificacionService {
        public int confirmaciones;
        public int cancelaciones;

        @Override
        public void enviarConfirmacion(Cita cita, String correo) { confirmaciones++; }

        @Override
        public void enviarCancelacion(Cita cita, String correo) { cancelaciones++; }
    }

    /** Codificador falso: evita el costo de BCrypt en pruebas unitarias. */
    public static class CodificadorFalso implements PasswordEncoder {
        @Override
        public String encode(CharSequence raw) { return "hash:" + raw; }

        @Override
        public boolean matches(CharSequence raw, String encoded) { return encoded.equals("hash:" + raw); }
    }
}
