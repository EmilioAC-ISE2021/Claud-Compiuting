package cc.sars.config;

import cc.sars.model.Grupo;
import cc.sars.model.User;
import cc.sars.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Set;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class WebConfig {

    private final UsuarioService usuarioService;

    public WebConfig(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal User user, Model model, HttpSession session, HttpServletRequest request) {
        // Ignorar la lógica de currentActiveGroup si es una solicitud de eliminación de grupo
        // para evitar problemas de concurrencia o acceso a entidades ya marcadas para eliminación.
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/grupo/eliminar".equals(request.getRequestURI())) {
            model.addAttribute("gruposDelUsuario", Collections.emptySet()); // O manejar según sea necesario
            model.addAttribute("currentActiveGroup", null); // Asegurarse de que no haya un grupo activo
            return; 
        }

        if (user != null) {
            Optional<User> fetchedUserOptional = usuarioService.findByUsername(user.getUsername());
            Set<Grupo> userGroups = Collections.emptySet();
            if (fetchedUserOptional.isPresent()) {
                userGroups = fetchedUserOptional.get().getGrupos();
            }
            model.addAttribute("gruposDelUsuario", userGroups);

            String currentActiveGroupName = (String) session.getAttribute("currentActiveGroup");
            Grupo currentActiveGroup = null;

            if (currentActiveGroupName != null) {
                Optional<Grupo> selected = userGroups.stream()
                        .filter(g -> g.getNombre().equals(currentActiveGroupName))
                        .findFirst();
                if (selected.isPresent()) {
                    currentActiveGroup = selected.get();
                } else {
                    session.removeAttribute("currentActiveGroup");
                }
            }

            if (currentActiveGroup == null && !userGroups.isEmpty()) {
                currentActiveGroup = userGroups.stream().findFirst().orElse(null);
                if (currentActiveGroup != null) {
                    session.setAttribute("currentActiveGroup", currentActiveGroup.getNombre());
                }
            }
            model.addAttribute("currentActiveGroup", currentActiveGroup);
        } else {
            model.addAttribute("gruposDelUsuario", Collections.emptySet());
            model.addAttribute("currentActiveGroup", null);
        }
    }
}
