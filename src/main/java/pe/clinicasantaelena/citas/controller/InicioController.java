package pe.clinicasantaelena.citas.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.clinicasantaelena.citas.security.UsuarioAutenticado;

/** Redirige la raiz del sitio a la pantalla de inicio de cada rol. */
@Controller
public class InicioController {

    /** @param yo usuario en sesion @return redireccion segun rol */
    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado yo) {
        if (yo == null) {
            return "redirect:/login";
        }
        return switch (yo.getRol()) {
            case PACIENTE -> "redirect:/paciente/inicio";
            case MEDICO -> "redirect:/medico/agenda";
            case ADMINISTRADOR -> "redirect:/admin/reportes";
            case RECEPCIONISTA -> "redirect:/proximamente";
        };
    }

    /** @param model modelo @return aviso de modulo pendiente (recepcion, entrega final) */
    @GetMapping("/proximamente")
    public String proximamente(Model model) {
        model.addAttribute("titulo", "Módulo en construcción");
        model.addAttribute("mensaje", "El módulo de recepción se habilitará en la entrega final del proyecto.");
        return "error";
    }
}
