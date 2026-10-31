package pe.clinicasantaelena.citas.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.clinicasantaelena.citas.dto.RegistroPacienteForm;
import pe.clinicasantaelena.citas.exception.ReglaNegocioException;
import pe.clinicasantaelena.citas.service.UsuarioService;

/**
 * Pantallas P01 (inicio de sesion) y P02 (registro de paciente). RF01.
 * El POST de /login lo procesa Spring Security.
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    /** @param usuarioService servicio de usuarios */
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** @return vista P01 */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /** @param model modelo @return vista P02 */
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("form", new RegistroPacienteForm());
        return "auth/registro";
    }

    /**
     * @param form   datos del formulario
     * @param result errores de validacion
     * @param model  modelo
     * @param ra     atributos flash
     * @return redireccion al login o la misma vista con errores
     */
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroPacienteForm form, BindingResult result,
                            Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarioService.registrarPaciente(form);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
        ra.addFlashAttribute("exito", "Tu cuenta fue creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
