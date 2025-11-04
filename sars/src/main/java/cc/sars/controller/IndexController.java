package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.Serie;
import cc.sars.model.User;
import cc.sars.service.SerieService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;

@Controller
public class IndexController {

    private final SerieService serieService;

    public IndexController(SerieService serieService) {
        this.serieService = serieService;
    }

    /**
     * Muestra la página principal (dashboard) del usuario.
     * El contenido depende de su rol y de si pertenece a un grupo.
     */
    @GetMapping("/")
    public String getIndexPage(@AuthenticationPrincipal User user, Model model) {

        Set<Grupo> gruposDelUsuario = user.getGrupos();

        // Caso 1: Es un USER y no tiene NINGÚN grupo.
        if (user.getRole() == Role.ROLE_USER && gruposDelUsuario.isEmpty()) {
            return "sin-grupo"; // Devuelve la plantilla sin-grupo.html
        }

        // Caso 2: Es LIDER o es USER y ya está en (al menos) un grupo.
        // Simplificación: Tomamos el primer grupo de la lista.
        Grupo miGrupo = gruposDelUsuario.stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Error: El usuario " + user.getUsername() + " no tiene grupos asignados."));

        // Buscamos las series de ESE grupo
        List<Serie> seriesDelGrupo = serieService.getSeriesPorGrupo(miGrupo.getNombre());

        model.addAttribute("usuario", user); // Para que la vista sepa el rol
        model.addAttribute("grupo", miGrupo);
        model.addAttribute("series", seriesDelGrupo);

        return "index"; // Devuelve la plantilla index.html (la de "gestionar series")
    }
}