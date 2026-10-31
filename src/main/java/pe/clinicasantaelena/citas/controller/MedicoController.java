package pe.clinicasantaelena.citas.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.model.Cita;
import pe.clinicasantaelena.citas.model.EstadoCita;
import pe.clinicasantaelena.citas.security.UsuarioAutenticado;
import pe.clinicasantaelena.citas.service.CitaService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Pantalla P08 - agenda del dia del medico (RF08). */
@Controller
@RequestMapping("/medico")
public class MedicoController {

    private final CitaService citaService;
    private final Clock clock;

    /**
     * @param citaService citas
     * @param clock       reloj
     */
    public MedicoController(CitaService citaService, Clock clock) {
        this.citaService = citaService;
        this.clock = clock;
    }

    /**
     * @param fecha dia (por defecto hoy)
     * @param yo    medico en sesion
     * @param model modelo
     * @return vista
     */
    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                         LocalDate fecha, @AuthenticationPrincipal UsuarioAutenticado yo, Model model) {
        LocalDate dia = fecha == null ? LocalDate.now(clock) : fecha;
        List<Cita> citas = citaService.agendaDelMedico(yo.getIdPerfil(), dia);
        model.addAttribute("fecha", dia);
        model.addAttribute("hoy", LocalDate.now(clock));
        model.addAttribute("citas", citas);
        model.addAttribute("atendidas", citas.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count());
        model.addAttribute("pendientes", citas.stream().filter(c -> c.getEstado().esVigente()).count());
        return "medico/agenda";
    }

    /**
     * @param id    cita
     * @param fecha dia mostrado
     * @param yo    medico
     * @param ra    flash
     * @return redireccion a la agenda
     */
    @PostMapping("/citas/{id}/atendida")
    public String atendida(@PathVariable Long id, @RequestParam String fecha,
                           @AuthenticationPrincipal UsuarioAutenticado yo, RedirectAttributes ra) {
        try {
            citaService.marcarAtendida(id, yo.getIdPerfil());
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medico/agenda?fecha=" + LocalDate.parse(fecha);
    }

    /**
     * @param id    cita
     * @param fecha dia mostrado
     * @param yo    medico
     * @param ra    flash
     * @return redireccion a la agenda
     */
    @PostMapping("/citas/{id}/no-asistio")
    public String noAsistio(@PathVariable Long id, @RequestParam String fecha,
                            @AuthenticationPrincipal UsuarioAutenticado yo, RedirectAttributes ra) {
        try {
            citaService.marcarNoAsistio(id, yo.getIdPerfil());
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medico/agenda?fecha=" + LocalDate.parse(fecha);
    }
}
