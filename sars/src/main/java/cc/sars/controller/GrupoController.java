package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.User;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class GrupoController {

    private static final Logger logger = LoggerFactory.getLogger(GrupoController.class);

    private final GrupoService grupoService;
    private final UsuarioService usuarioService;

    public GrupoController(GrupoService grupoService, UsuarioService usuarioService) {
        this.grupoService = grupoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra la página de gestión de usuarios del grupo.
     */
    @GetMapping("/grupo/gestionar")
    public String getGestionPage(@AuthenticationPrincipal User user, Model model) {

        // Recargar el usuario para asegurar que sus grupos estén actualizados
        User currentUser = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 1. Encontrar el grupo del LIDER
        Grupo miGrupo = grupoService.getGrupoPorNombre(currentUser.getGrupos().stream().findFirst().orElseThrow(() -> new RuntimeException("Líder sin grupo.")).getNombre());

        // 2. Obtener los usuarios que YA están en el grupo
        Set<User> usuariosEnGrupo = miGrupo.getUsuarios();

        // 3. Obtener TODOS los usuarios del sistema
        List<User> todosLosUsuarios = usuarioService.getTodosLosUsuarios();

        // 4. Filtrar para el desplegable:
        //    (Queremos solo usuarios que NO estén ya en este grupo)
        List<User> usuariosDisponibles = todosLosUsuarios.stream()
                .filter(u -> !usuariosEnGrupo.contains(u))
                .collect(Collectors.toList());

        model.addAttribute("grupo", miGrupo);
        model.addAttribute("usuariosEnGrupo", usuariosEnGrupo);
        model.addAttribute("usuariosDisponibles", usuariosDisponibles);

        return "app/gestionar-grupo"; // Devuelve gestionar-grupo.html
    }

    /**
     * Procesa el formulario para añadir un usuario al grupo.
     */
    @PostMapping("/grupo/gestionar/agregar")
    public String agregarUsuario(
            @AuthenticationPrincipal User user,
            @RequestParam("nombreUsuario") String nombreUsuarioAAgregar) {
        
        // 1. Obtener el nombre del grupo del LIDER que hace la petición
        String nombreGrupo = user.getGrupos().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Líder sin grupo.")).getNombre();

        // 2. Llamar al servicio para añadirlo
        try {
            grupoService.agregarUsuarioAGrupo(nombreUsuarioAAgregar, nombreGrupo);
        } catch (Exception e) {
            logger.error("Error al agregar usuario al grupo: {}", e.getMessage(), e);
            // Manejar error (ej: usuario no existe)
            // Por ahora, solo redirigimos.
        }

        return "redirect:/grupo/gestionar";
    }
}