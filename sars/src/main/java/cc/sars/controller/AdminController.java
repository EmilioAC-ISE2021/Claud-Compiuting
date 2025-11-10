package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.service.GrupoService;
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

    public AdminController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        List<Grupo> allGroups = grupoService.getAllGrupos(); // Necesitamos un método para obtener todos los grupos
        model.addAttribute("grupos", allGroups);
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
}
