package cc.sars.controller;

import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.User;
import cc.sars.model.UsuarioGrupo;
import cc.sars.model.UsuarioGrupoId;
import cc.sars.repository.UsuarioGrupoRepository;
import cc.sars.service.SerieService;
import cc.sars.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

@Controller
public class SerieController {

    private static final Logger logger = LoggerFactory.getLogger(SerieController.class);
    private final SerieService serieService;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final UsuarioService usuarioService;

    public SerieController(SerieService serieService, UsuarioGrupoRepository usuarioGrupoRepository, UsuarioService usuarioService) {
        this.serieService = serieService;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/serie/crear")
    @Transactional
    public String createSerie(@RequestParam String nombre,
                              @RequestParam String descripcion,
                              @AuthenticationPrincipal User user,
                              HttpSession session,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");

        if (nombreGrupo == null) {
            logger.error("No se pudo crear la serie porque no se encontró un grupo activo en la sesión para el usuario '{}'.", user.getUsername());
            throw new IllegalStateException("No hay un grupo activo en la sesión para realizar la operación.");
        }

        try {
            serieService.createSerie(nombre, descripcion, nombreGrupo);
        } catch (cc.sars.exception.SerieAlreadyExistsException e) {
            logger.warn("Intento de crear una serie duplicada '{}' en el grupo '{}' por el usuario '{}'.", nombre, nombreGrupo, user.getUsername());
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al crear serie '{}' en el grupo '{}': {}", nombre, nombreGrupo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Ocurrió un error inesperado al crear la serie.");
        }
        return "redirect:/";
    }

    @GetMapping("/serie/{nombreSerie}")
    public String getSerieDetalle(@PathVariable String nombreSerie, Model model, @AuthenticationPrincipal User usuarioActual,
    							  HttpSession session) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        Serie serie = serieService.getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new NoSuchElementException("Serie no encontrada: " + nombreSerie));
        
        UsuarioGrupoId usuarioGrupoId = new UsuarioGrupoId(usuarioActual.getUsername(), serie.getGrupo().getNombre());
        UsuarioGrupo usuarioGrupoActual = usuarioGrupoRepository.findById(usuarioGrupoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la membresía del usuario " + usuarioActual.getUsername() + " en el grupo " + serie.getGrupo().getNombre()));
        
        // Prepara una lista solo con los nombres de usuario para evitar problemas de serialización
        List<String> nombresUsuarios = serie.getGrupo().getUsuarios().stream()
                                            .map(User::getUsername)
                                            .collect(Collectors.toList());

        model.addAttribute("serie", serie);
        model.addAttribute("todosLosEstados", serieService.getTodosLosEstados());
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("usuariosDelGrupo", nombresUsuarios); // Pasa la lista de nombres
        model.addAttribute("rolEnGrupoActual", usuarioGrupoActual.getRol());
        return "app/serie-detalle";
    }

    @PostMapping("/serie/{nombreSerie}/capitulo/crear")
    public String createCapitulo(@PathVariable String nombreSerie,
                                 @RequestParam String nombresCapitulos,
                                 @RequestParam(required = false) String[] tareasEnMasa,
                                 HttpSession session) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        try {
            serieService.addCapitulosToSerie(nombreGrupo, nombreSerie, nombresCapitulos, tareasEnMasa);
        } catch (Exception e) {
            logger.error("Error al crear capítulos para la serie '{}': {}", nombreSerie, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    /**
     * Mapeo para procesar el formulario de añadir una tarea a un capítulo.
     */
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/crear")
    public String createTarea(@PathVariable String nombreSerie,
                              @PathVariable String nombreCapitulo,
                              @RequestParam String nombreTarea, HttpSession session) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        try {
            serieService.addTareaToCapitulo(nombreGrupo, nombreSerie, nombreCapitulo, nombreTarea);
        } catch (Exception e) {
            logger.error("Error al crear tarea '{}' en capítulo '{}': {}", nombreTarea, nombreCapitulo, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    /**
     * Mapeo para procesar la actualización del estado de una tarea (el desplegable).
     * MODIFICADO para pasar el usuario actual al servicio.
     */
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/estado")
    public String updateTareaEstado(@PathVariable String nombreSerie,
                                    @PathVariable String nombreCapitulo,
                                    @PathVariable String nombreTarea,
                                    @RequestParam EstadosTareas estado,
                                    @AuthenticationPrincipal User usuarioActual, HttpSession session) { // Obtener usuario
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        try {
            // Pasar el usuario al servicio
            serieService.updateTareaEstado(nombreGrupo, nombreSerie, nombreCapitulo, nombreTarea, estado, usuarioActual.getUsername());
        } catch (Exception e) {
            logger.error("Error al actualizar estado de tarea '{}' a '{}': {}", nombreTarea, estado, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    /**
     * Mapeo para procesar la asignación de un usuario a una tarea.
     */
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/asignarUsuario")
    public String asignarUsuarioATarea(@PathVariable String nombreSerie,
                                       @PathVariable String nombreCapitulo,
                                       @PathVariable String nombreTarea,
                                       @RequestParam String nuevoUsuarioAsignado,
                                       @AuthenticationPrincipal User usuarioActual, HttpSession session) { // El líder que realiza la acción
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        try {
            serieService.asignarUsuarioATarea(nombreGrupo, nombreSerie, nombreCapitulo, nombreTarea, nuevoUsuarioAsignado, usuarioActual.getUsername());
        } catch (Exception e) {
            logger.error("Error al asignar usuario '{}' a tarea '{}': {}", nuevoUsuarioAsignado, nombreTarea, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/eliminar")
    public String deleteCapitulo(@PathVariable String nombreSerie,
                                 @PathVariable String nombreCapitulo,
                                 HttpSession session) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");    	
        try {
            serieService.deleteCapitulo(nombreGrupo, nombreSerie, nombreCapitulo);
        } catch (Exception e) {
            logger.error("Error al eliminar capítulo '{}' de la serie '{}': {}", nombreCapitulo, nombreSerie, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    @PostMapping("/serie/{nombreSerie}/eliminar")
    public String deleteSerie(@PathVariable String nombreSerie,
                              HttpSession session,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String nombreGrupo = (String) session.getAttribute("currentActiveGroup");
        try {
            serieService.deleteSerie(nombreGrupo, nombreSerie);
            redirectAttributes.addFlashAttribute("success_message", "Serie '" + nombreSerie + "' eliminada correctamente.");
        } catch (Exception e) {
            logger.error("Error al eliminar serie '{}': {}", nombreSerie, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "Error al eliminar la serie: " + e.getMessage());
        }
        return "redirect:/";
    }
}