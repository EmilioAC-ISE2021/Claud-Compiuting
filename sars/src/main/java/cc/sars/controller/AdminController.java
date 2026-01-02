package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import cc.sars.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')") // Solo usuarios con rol ADMIN pueden acceder a este controlador
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final GrupoService grupoService;
    private final UsuarioService usuarioService;
    private final AdminService adminService; // Inyectar AdminService

    public AdminController(GrupoService grupoService, UsuarioService usuarioService, AdminService adminService) {
        this.grupoService = grupoService;
        this.usuarioService = usuarioService;
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        List<Grupo> allGroups = grupoService.getAllGrupos();
        model.addAttribute("grupos", allGroups);
        List<User> allUsers = usuarioService.getTodosLosUsuarios(); // Obtener todos los usuarios
        model.addAttribute("usuarios", allUsers); // Añadir usuarios al modelo

        // Calcular restricciones de eliminación para cada usuario
        Map<String, String> userDeletionRestrictions = new HashMap<>();
        for (User user : allUsers) {
            String restrictionMessage = "";
            if (user.getRole() == Role.ROLE_ADMIN) {
                restrictionMessage = "No se puede eliminar a un usuario ADMIN.";
            } else { // Para usuarios que no son ADMIN, verificar liderazgo en grupos
                for (cc.sars.model.UsuarioGrupo ug : user.getUsuarioGrupos()) {
                    if (ug.getRol() == Role.ROLE_LIDER) {
                        long liderCountInGroup = usuarioService.countLeadersInGroup(ug.getGrupo());
                        if (liderCountInGroup <= 1) {
                            restrictionMessage = "Es el único LIDER del grupo '" + ug.getGrupo().getNombre() + "'. Elimine el grupo primero.";
                            break; // Solo necesitamos una razón
                        }
                    }
                }
            }
            userDeletionRestrictions.put(user.getUsername(), restrictionMessage);
        }
        model.addAttribute("userDeletionRestrictions", userDeletionRestrictions);

        return "app/admin"; // Devuelve la plantilla app/admin.html
    }

    @PostMapping("/admin/grupo/eliminar")
    public String deleteGrupo(@RequestParam String nombreGrupo, RedirectAttributes redirectAttributes) {
        try {
            grupoService.deleteGrupo(nombreGrupo);
            redirectAttributes.addFlashAttribute("success_message", "Grupo '" + nombreGrupo + "' eliminado correctamente.");
        } catch (Exception e) {
            logger.error("Error al eliminar el grupo '{}': {}", nombreGrupo, e.getMessage(), e);
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
            logger.error("Error al eliminar el usuario '{}': {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Error al eliminar el usuario '" + username + "': " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/reset-database")
    public String resetDatabase(RedirectAttributes redirectAttributes) {
        try {
            adminService.resetDatabase();
            redirectAttributes.addFlashAttribute("success_message", "Base de datos reseteada correctamente.");
        } catch (Exception e) {
            logger.error("Error al resetear la base de datos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Error al resetear la base de datos: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}

