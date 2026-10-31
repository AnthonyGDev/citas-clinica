package pe.clinicasantaelena.citas.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.clinicasantaelena.citas.dto.ReservaForm;
import pe.clinicasantaelena.citas.dto.SolicitudReserva;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.model.CanalReserva;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.security.UsuarioAutenticado;
import pe.clinicasantaelena.citas.service.AgendaService;
import pe.clinicasantaelena.citas.service.CatalogoService;
import pe.clinicasantaelena.citas.service.CitaService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pantallas del paciente: P03 inicio, P04 reservar, P05 confirmacion y P06 mis citas.
 * El controlador solo coordina: las reglas estan en los servicios y en la entidad {@link Cita}.
 */
@Controller
@RequestMapping("/paciente")
public class PacienteController {

    /** Dias que se muestran en el calendario de reserva. */
    static final int DIAS_CALENDARIO = 21;

    private final CatalogoService catalogoService;
    private final AgendaService agendaService;
    private final CitaService citaService;
    private final Clock clock;

    /**
     * @param catalogoService catalogos
     * @param agendaService   disponibilidad
     * @param citaService     citas
     * @param clock           reloj del sistema
     */
    public PacienteController(CatalogoService catalogoService, AgendaService agendaService,
                              CitaService citaService, Clock clock) {
        this.catalogoService = catalogoService;
        this.agendaService = agendaService;
        this.citaService = citaService;
        this.clock = clock;
    }

    /**
     * P03 - inicio del paciente con su proxima cita.
     *
     * @param yo    paciente en sesion
     * @param model modelo
     * @return vista
     */
    @GetMapping("/inicio")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado yo, Model model) {
        LocalDate hoy = LocalDate.now(clock);
        List<Cita> citas = citaService.citasDelPaciente(yo.getIdPerfil());
        citas.stream()
                .filter(c -> c.getEstado().esVigente() && !c.getFecha().isBefore(hoy))
                .min(Comparator.comparing(Cita::inicio))
                .ifPresent(c -> model.addAttribute("proxima", c));
        model.addAttribute("totalCitas", citas.size());
        return "paciente/inicio";
    }

    /**
     * P04 - formulario guiado. Cada seleccion recarga la pagina con los datos del paso siguiente.
     *
     * @param form  selecciones actuales
     * @param model modelo
     * @return vista
     */
    @GetMapping("/reservar")
    public String reservar(@ModelAttribute("reserva") ReservaForm form, Model model) {
        cargarOpciones(form, model);
        return "paciente/reservar";
    }

    /**
     * Registra la cita.
     *
     * @param form   datos completos
     * @param result errores de validacion
     * @param yo     paciente en sesion
     * @param model  modelo
     * @return redireccion a la confirmacion o la misma vista con el error
     */
    @PostMapping("/reservar")
    public String confirmar(@Valid @ModelAttribute("reserva") ReservaForm form, BindingResult result,
                            @AuthenticationPrincipal UsuarioAutenticado yo, Model model) {
        if (result.hasErrors()) {
            cargarOpciones(form, model);
            return "paciente/reservar";
        }
        try {
            Cita cita = citaService.reservar(new SolicitudReserva(yo.getIdPerfil(), form.getIdMedico(),
                    form.getFecha(), form.getHora(), form.getMotivo(), CanalReserva.WEB, yo.getIdUsuario()));
            return "redirect:/paciente/citas/" + cita.getId() + "/confirmacion";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            form.setHora(null);
            cargarOpciones(form, model);
            return "paciente/reservar";
        }
    }

    /**
     * P05 - confirmacion con el codigo de cita.
     *
     * @param id    cita
     * @param yo    paciente en sesion
     * @param model modelo
     * @return vista
     */
    @GetMapping("/citas/{id}/confirmacion")
    public String confirmacion(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo, Model model) {
        model.addAttribute("cita", citaService.citaDelPaciente(id, yo.getIdPerfil()));
        return "paciente/confirmacion";
    }

    /**
     * P06 - historial con la accion de cancelar.
     *
     * @param yo    paciente en sesion
     * @param model modelo
     * @return vista
     */
    @GetMapping("/mis-citas")
    public String misCitas(@AuthenticationPrincipal UsuarioAutenticado yo, Model model) {
        model.addAttribute("citas", citaService.citasDelPaciente(yo.getIdPerfil()));
        model.addAttribute("ahora", LocalDateTime.now(clock));
        return "paciente/mis-citas";
    }

    /**
     * Cancela una cita propia (RF05, RN02).
     *
     * @param id     cita
     * @param motivo motivo opcional
     * @param yo     paciente en sesion
     * @param ra     mensajes flash
     * @return redireccion a mis citas
     */
    @PostMapping("/citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           @AuthenticationPrincipal UsuarioAutenticado yo, RedirectAttributes ra) {
        try {
            citaService.cancelarPorPaciente(id, yo.getIdPerfil(), motivo);
            ra.addFlashAttribute("exito", "Tu cita fue cancelada y el horario quedó libre.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/paciente/mis-citas";
    }

    private void cargarOpciones(ReservaForm form, Model model) {
        model.addAttribute("especialidades", catalogoService.especialidadesActivas());
        model.addAttribute("medicos", catalogoService.medicosPorEspecialidad(form.getIdEspecialidad()));
        List<DayOfWeek> diasAtencion = agendaService.diasDeAtencion(form.getIdMedico());
        LocalDate hoy = LocalDate.now(clock);
        List<DiaCalendario> calendario = new ArrayList<>();
        for (int i = 0; i < DIAS_CALENDARIO; i++) {
            LocalDate d = hoy.plusDays(i);
            calendario.add(new DiaCalendario(d, diasAtencion.contains(d.getDayOfWeek())));
        }
        model.addAttribute("calendario", calendario);
        model.addAttribute("bloques", agendaService.bloquesDelDia(form.getIdMedico(), form.getFecha()));
        int paso = form.getIdEspecialidad() == null ? 1 : form.getIdMedico() == null ? 2
                : form.getFecha() == null ? 3 : 4;
        model.addAttribute("paso", paso);
    }

    /**
     * Dia del calendario de la pantalla P04.
     */
    public static final class DiaCalendario {
        private final LocalDate fecha;
        private final boolean atiende;

        DiaCalendario(LocalDate fecha, boolean atiende) {
            this.fecha = fecha;
            this.atiende = atiende;
        }

        public LocalDate getFecha() { return fecha; }
        public boolean isAtiende() { return atiende; }
    }
}
