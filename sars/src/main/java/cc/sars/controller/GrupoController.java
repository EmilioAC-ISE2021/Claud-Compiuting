package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role; // Importar Role
import cc.sars.model.User;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importar RedirectAttributes

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays; // Importar Arrays

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
        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 1. Encontrar el grupo del LIDER
        Grupo miGrupo = grupoService.getGrupoPorNombre(usuarioActual.getGrupos().stream().findFirst().orElseThrow(() -> new RuntimeException("Líder sin grupo.")).getNombre());

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
        model.addAttribute("rolesDisponibles", Arrays.asList(Role.ROLE_LIDER, Role.ROLE_USER, Role.ROLE_QC)); // Pasar los roles disponibles
        logger.debug("Usuario autenticado en el modelo: {}", usuarioActual.getUsername()); // Debug log
        model.addAttribute("user", usuarioActual); // Añadir el usuario autenticado al modelo

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

    /**
     * Procesa el cambio de rol de un usuario en el grupo.
     */
    @PostMapping("/grupo/gestionar/cambiar-rol")
    public String cambiarRolUsuario(
            @AuthenticationPrincipal User user,
            @RequestParam("username") String usernameToChange,
            @RequestParam("newRole") Role newRole,
            RedirectAttributes redirectAttributes) {

        // Obtener el nombre del grupo del LIDER que hace la petición
        String nombreGrupo = user.getGrupos().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Líder sin grupo.")).getNombre();

        try {
            usuarioService.changeUserRole(usernameToChange, newRole, nombreGrupo);
            redirectAttributes.addFlashAttribute("success_message", "Rol de usuario actualizado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
        }
        return "redirect:/grupo/gestionar";
    }

    /**
     * Procesa la eliminación de un usuario de un grupo.
     */
    @PostMapping("/grupo/gestionar/eliminarUsuario")
    public String eliminarUsuario(
            @AuthenticationPrincipal User user,
            @RequestParam("nombreGrupo") String nombreGrupo,
            @RequestParam("username") String usernameAEliminar,
            RedirectAttributes redirectAttributes) {

        // Recargar el usuario para asegurar que sus grupos estén actualizados
        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Verificar que el usuario autenticado es líder del grupo
        boolean esLiderDeEsteGrupo = usuarioActual.getGrupos().stream()
                .anyMatch(g -> g.getNombre().equals(nombreGrupo) && usuarioActual.getRole().equals(Role.ROLE_LIDER));

        if (!esLiderDeEsteGrupo) {
            redirectAttributes.addFlashAttribute("error_message", "No tienes permiso para eliminar usuarios de este grupo.");
            return "redirect:/grupo/gestionar";
        }

        try {
            grupoService.eliminarUsuarioDeGrupo(nombreGrupo, usernameAEliminar);
            redirectAttributes.addFlashAttribute("success_message", "Usuario '" + usernameAEliminar + "' eliminado del grupo correctamente.");
        } catch (RuntimeException e) {
            logger.error("Error al eliminar usuario del grupo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
        }
        return "redirect:/grupo/gestionar";
    }
}