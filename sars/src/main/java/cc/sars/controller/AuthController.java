package cc.sars.controller;

import cc.sars.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra la página de inicio de sesión personalizada.
     * Si el usuario ya está autenticado, lo redirige al inicio.
     */
    @GetMapping("/login")
    public String getLoginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login"; // Devuelve la plantilla login.html
    }

    /**
     * Muestra la página de registro.
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "register"; // Devuelve la plantilla register.html
    }

    /**
     * Procesa el formulario de registro.
     */
    @PostMapping("/register")
    public String postRegister(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("contrasenya") String contrasenya,
            @RequestParam(value = "crearGrupo", required = false) boolean crearGrupo,
            @RequestParam("nombreGrupo") String nombreGrupo,
            RedirectAttributes redirectAttributes) {

        try {
            // El servicio maneja la lógica de crear LIDER o USER
            usuarioService.registrarUsuario(nombreUsuario, contrasenya, crearGrupo, nombreGrupo);
            
            // Añade un mensaje de éxito para mostrar en la página de login
            redirectAttributes.addFlashAttribute("success_message", "¡Cuenta creada! Por favor, inicia sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            // Añade un mensaje de error para mostrar en la página de registro
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
            return "redirect:/register";
        }
    }
}