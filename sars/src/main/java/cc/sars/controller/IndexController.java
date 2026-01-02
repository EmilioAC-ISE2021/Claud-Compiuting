package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.Serie;
import cc.sars.model.User;
import cc.sars.service.SerieService;
import cc.sars.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cc.sars.model.UsuarioGrupo;
import cc.sars.model.UsuarioGrupoId;
import cc.sars.repository.UsuarioGrupoRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class IndexController {

    private final SerieService serieService;
    private final UsuarioService usuarioService;
    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public IndexController(SerieService serieService, UsuarioService usuarioService, UsuarioGrupoRepository usuarioGrupoRepository) {
        this.serieService = serieService;
        this.usuarioService = usuarioService;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Muestra la página principal (dashboard) del usuario.
     * El contenido depende de su rol y de si pertenece a un grupo.
     */
    @GetMapping("/")
    @Transactional
    public String getIndexPage(@AuthenticationPrincipal User user, Model model, HttpSession session) {
        User authenticatedUser = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + user.getUsername()));

        if (authenticatedUser.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/admin";
        }

        Set<Grupo> gruposDelUsuario = authenticatedUser.getGrupos();

        // Caso 1: No tiene grupo
        
        if (gruposDelUsuario.isEmpty()) {
            return "app/sin-grupo"; // Devuelve la plantilla sin-grupo.html
        }

        // Caso 2: Es LIDER o es USER y ya está en (al menos) un grupo.
        String currentActiveGroupName = (String) session.getAttribute("currentActiveGroup");
        if (currentActiveGroupName == null) {
            return "app/sin-grupo";
        }

        Grupo miGrupo = authenticatedUser.getGrupos().stream()
                .filter(g -> g.getNombre().equals(currentActiveGroupName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error: El grupo activo '" + currentActiveGroupName + "' no se encontró o no pertenece al usuario " + authenticatedUser.getUsername()));

        UsuarioGrupoId usuarioGrupoId = new UsuarioGrupoId(authenticatedUser.getUsername(), miGrupo.getNombre());
        UsuarioGrupo usuarioGrupoActual = usuarioGrupoRepository.findById(usuarioGrupoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la membresía del usuario " + authenticatedUser.getUsername() + " en el grupo " + miGrupo.getNombre()));

        // Buscamos las series de ESE grupo
        List<Serie> seriesDelGrupo = serieService.getSeriesPorGrupo(miGrupo.getNombre());

        model.addAttribute("usuario", authenticatedUser); // Para que la vista sepa el rol
        model.addAttribute("grupo", miGrupo);
        model.addAttribute("series", seriesDelGrupo);
        model.addAttribute("rolEnGrupoActual", usuarioGrupoActual.getRol());

        return "app/index"; // Devuelve la plantilla app/index.html (la de "gestionar series")
    }
}
