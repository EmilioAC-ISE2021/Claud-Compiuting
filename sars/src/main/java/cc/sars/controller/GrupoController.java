package cc.sars.controller;

import cc.sars.model.Grupo;
import cc.sars.model.Role; // Importar Role
import cc.sars.model.User;
import cc.sars.model.UsuarioGrupo;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import cc.sars.exception.GrupoAlreadyExistsException; // ADDED IMPORT
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import org.springframework.transaction.annotation.Transactional;

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
    @GetMapping("/grupo/{nombreGrupo}/gestionar")
    @Transactional
    public String getGestionPage(@PathVariable String nombreGrupo, @AuthenticationPrincipal User user, Model model) {
        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        boolean esLiderDelGrupo = usuarioActual.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER);

        if (!esLiderDelGrupo) {
            throw new RuntimeException("No tienes permiso para gestionar este grupo.");
        }
        Grupo miGrupo = grupoService.getGrupoPorNombre(nombreGrupo);

        // 2. Obtener los usuarios y sus roles en el grupo
        Set<UsuarioGrupo> membresias = miGrupo.getUsuarioGrupos();
        Set<User> usuariosEnGrupo = membresias.stream()
                .map(UsuarioGrupo::getUsuario)
                .collect(Collectors.toSet());

        // 3. Obtener TODOS los usuarios del sistema
        List<User> todosLosUsuarios = usuarioService.getTodosLosUsuarios();

        // 4. Filtrar para el desplegable: usuarios que NO están en este grupo y que NO son ADMIN
        List<User> usuariosDisponibles = todosLosUsuarios.stream()
                .filter(u -> !usuariosEnGrupo.contains(u))
                .filter(u -> u.getRole() != Role.ROLE_ADMIN)
                .collect(Collectors.toList());

        long liderCount = membresias.stream()
                .filter(ug -> ug.getRol() == Role.ROLE_LIDER)
                .count();

        model.addAttribute("grupo", miGrupo);
        model.addAttribute("membresias", membresias); // Enviar las membresias (UsuarioGrupo) que contienen usuario y rol
        model.addAttribute("usuariosDisponibles", usuariosDisponibles);
        model.addAttribute("rolesDisponibles", Arrays.asList(Role.ROLE_LIDER, Role.ROLE_USER, Role.ROLE_QC));
        model.addAttribute("liderCount", liderCount);
        model.addAttribute("user", usuarioActual);

        return "app/gestionar-grupo";
    }

    /**
     * Procesa el formulario para añadir un usuario al grupo.
     */
    @PostMapping("/grupo/{nombreGrupo}/gestionar/agregar")
    @Transactional
    public String agregarUsuario(
            @PathVariable String nombreGrupo,
            @AuthenticationPrincipal User user,
            @RequestParam("nombreUsuario") String nombreUsuarioAAgregar,
            RedirectAttributes redirectAttributes) {
        
        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        boolean esLiderDelGrupo = usuarioActual.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER);

        if (!esLiderDelGrupo) {
            redirectAttributes.addFlashAttribute("error_message", "No tienes permiso para modificar este grupo.");
            return "redirect:/";
        }

        try {
            grupoService.agregarUsuarioAGrupo(nombreUsuarioAAgregar, nombreGrupo);
        } catch (Exception e) {
            logger.error("Error al agregar usuario al grupo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Error al agregar usuario: " + e.getMessage());
        }

        return "redirect:/grupo/" + nombreGrupo + "/gestionar";
    }

    /**
     * Procesa el cambio de rol de un usuario en el grupo.
     */
    @PostMapping("/grupo/{nombreGrupo}/gestionar/cambiar-rol")
    @Transactional
    public String cambiarRolUsuario(
            @PathVariable String nombreGrupo,
            @AuthenticationPrincipal User user,
            @RequestParam("username") String usernameToChange,
            @RequestParam("newRole") Role newRole,
            RedirectAttributes redirectAttributes) {

        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        
        boolean esLiderDelGrupo = usuarioActual.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER);

        if (!esLiderDelGrupo) {
            redirectAttributes.addFlashAttribute("error_message", "No tienes permiso para modificar este grupo.");
            return "redirect:/";
        }

        try {
            usuarioService.cambiarRolEnGrupo(usernameToChange, newRole, nombreGrupo);
            redirectAttributes.addFlashAttribute("success_message", "Rol de usuario actualizado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
        }
        esLiderDelGrupo = usuarioActual.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER);
        
        if (esLiderDelGrupo) 
        	return "redirect:/grupo/" + nombreGrupo + "/gestionar";
        else
            return "redirect:/";
    }

    /**
     * Procesa la eliminación de un usuario de un grupo.
     */
    @PostMapping("/grupo/{nombreGrupo}/gestionar/eliminarUsuario")
    @Transactional
    public String eliminarUsuario(
            @PathVariable String nombreGrupo,
            @AuthenticationPrincipal User user,
            @RequestParam("username") String usernameAEliminar,
            RedirectAttributes redirectAttributes) {

        User usuarioActual = usuarioService.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        boolean esLiderDeEsteGrupo = usuarioActual.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER);

        if (!esLiderDeEsteGrupo) {
            redirectAttributes.addFlashAttribute("error_message", "No tienes permiso para eliminar usuarios de este grupo.");
            return "redirect:/";
        }
        
        // Evitar que un líder se elimine a sí mismo si es el último
        if (user.getUsername().equals(usernameAEliminar)) {
             long liderCount = usuarioActual.getUsuarioGrupos().stream()
                .filter(ug -> ug.getGrupo().getNombre().equals(nombreGrupo) && ug.getRol() == Role.ROLE_LIDER)
                .count();
             if (liderCount <=1) {
                redirectAttributes.addFlashAttribute("error_message", "No puedes eliminarte a ti mismo si eres el último líder del grupo.");
                return "redirect:/grupo/" + nombreGrupo + "/gestionar";
             }
        }


        try {
            grupoService.eliminarUsuarioDeGrupo(nombreGrupo, usernameAEliminar);
            redirectAttributes.addFlashAttribute("success_message", "Usuario '" + usernameAEliminar + "' eliminado del grupo correctamente.");
        } catch (RuntimeException e) {
            logger.error("Error al eliminar usuario del grupo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
        }
        return "redirect:/grupo/" + nombreGrupo + "/gestionar";
    }



    @PostMapping("/grupo/eliminar")
    public String deleteGrupo(@RequestParam String nombreGrupo, RedirectAttributes redirectAttributes) {
        try {
            grupoService.deleteGrupo(nombreGrupo);
            redirectAttributes.addFlashAttribute("success_message", "Grupo '" + nombreGrupo + "' eliminado correctamente.");
        } catch (Exception e) {
            logger.error("Error al eliminar grupo '{}': {}", nombreGrupo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Error al eliminar el grupo: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/grupo/crear")
    public String crear(
            @RequestParam("nombreGrupo") String nombreGrupo,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (nombreGrupo == null || nombreGrupo.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error_message", "El nombre del grupo no puede estar vacío.");
            return "redirect:/";
        }

        try {
            grupoService.crearGrupoYAsignarLider(nombreGrupo, user.getUsername());
            redirectAttributes.addFlashAttribute("success_message", "Grupo '" + nombreGrupo + "' creado exitosamente. Ahora eres el líder.");
            
            // Set active group in session
            session.setAttribute("currentActiveGroup", nombreGrupo);

            return "redirect:/";
        } catch (GrupoAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error_message", "Ya existe un grupo con ese nombre.");
            logger.warn("Intento de crear grupo con nombre existente: {}", nombreGrupo);
            return "redirect:/";
        }
    }
}