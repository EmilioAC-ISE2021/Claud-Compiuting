package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService; // Importar UsuarioService
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')") // Solo usuarios con rol ADMIN pueden acceder a este controlador
public class AdminController {

    private final GrupoService grupoService;
    private final UsuarioService usuarioService; // Inyectar UsuarioService

    public AdminController(GrupoService grupoService, UsuarioService usuarioService) { // Modificar constructor
        this.grupoService = grupoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        List<Grupo> allGroups = grupoService.getAllGrupos();
        model.addAttribute("grupos", allGroups);
        List<User> allUsers = usuarioService.getTodosLosUsuarios(); // Obtener todos los usuarios
        model.addAttribute("usuarios", allUsers); // Añadir usuarios al modelo
        return "app/admin"; // Devuelve la plantilla app/admin.html
    }

    @PostMapping("/admin/grupo/eliminar")
    public String deleteGrupo(@RequestParam String nombreGrupo, RedirectAttributes redirectAttributes) {
        try {
            grupoService.deleteGrupo(nombreGrupo);
            redirectAttributes.addFlashAttribute("success_message", "Grupo '" + nombreGrupo + "' eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_message", "Error al eliminar el grupo: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/usuario/eliminar") // Nuevo método para eliminar usuarios
    public String eliminarUsuario(@RequestParam String username, RedirectAttributes redirectAttributes) { // Cambiado a String username
        try {
            usuarioService.eliminarUsuario(username); // Llamar al método en español con username
            redirectAttributes.addFlashAttribute("success_message", "Usuario '" + username + "' eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_message", "Error al eliminar el usuario '" + username + "': " + e.getMessage());
        }
        return "redirect:/admin";
    }
}
