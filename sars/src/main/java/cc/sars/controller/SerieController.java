package cc.sars.controller;

import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.User;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
public class SerieController {

    private static final Logger logger = LoggerFactory.getLogger(SerieController.class);
    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    @PostMapping("/serie/crear")
    public String createSerie(@RequestParam String nombre,
                              @RequestParam String descripcion,
                              @AuthenticationPrincipal User user) {
        String nombreGrupo = user.getGrupos().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no tiene grupo"))
                .getNombre();
        try {
            serieService.createSerie(nombre, descripcion, nombreGrupo);
        } catch (Exception e) {
            logger.error("Error al crear serie '{}': {}", nombre, e.getMessage(), e);
        }
        return "redirect:/";
    }

    @GetMapping("/serie/{nombreSerie}")
    public String getSerieDetalle(@PathVariable String nombreSerie, Model model, @AuthenticationPrincipal User usuarioActual) {
        Serie serie = serieService.getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new NoSuchElementException("Serie no encontrada: " + nombreSerie));
        
        // Prepara una lista solo con los nombres de usuario para evitar problemas de serialización
        List<String> nombresUsuarios = serie.getGrupo().getUsuarios().stream()
                                            .map(User::getUsername)
                                            .collect(Collectors.toList());

        model.addAttribute("serie", serie);
        model.addAttribute("todosLosEstados", serieService.getTodosLosEstados());
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("usuariosDelGrupo", nombresUsuarios); // Pasa la lista de nombres
        return "app/serie-detalle";
    }

    @PostMapping("/serie/{nombreSerie}/capitulo/crear")
    public String createCapitulo(@PathVariable String nombreSerie,
                                 @RequestParam String nombresCapitulos,
                                 @RequestParam(required = false) String[] tareasEnMasa) {
        try {
            serieService.addCapitulosToSerie(nombreSerie, nombresCapitulos, tareasEnMasa);
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
                              @RequestParam String nombreTarea) {
        try {
            serieService.addTareaToCapitulo(nombreCapitulo, nombreTarea);
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
                                    @AuthenticationPrincipal User usuarioActual) { // Obtener usuario
        try {
            // Pasar el usuario al servicio
            serieService.updateTareaEstado(nombreCapitulo, nombreTarea, estado, usuarioActual);
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
                                       @AuthenticationPrincipal User usuarioActual) { // El líder que realiza la acción
        try {
            serieService.asignarUsuarioATarea(nombreSerie, nombreCapitulo, nombreTarea, nuevoUsuarioAsignado, usuarioActual);
        } catch (Exception e) {
            logger.error("Error al asignar usuario '{}' a tarea '{}': {}", nuevoUsuarioAsignado, nombreTarea, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/eliminar")
    public String deleteCapitulo(@PathVariable String nombreSerie,
                                 @PathVariable String nombreCapitulo) {
        try {
            serieService.deleteCapitulo(nombreSerie, nombreCapitulo);
        } catch (Exception e) {
            logger.error("Error al eliminar capítulo '{}' de la serie '{}': {}", nombreCapitulo, nombreSerie, e.getMessage(), e);
        }
        return "redirect:/serie/" + nombreSerie;
    }

    @PostMapping("/serie/{nombreSerie}/eliminar")
    public String deleteSerie(@PathVariable String nombreSerie) {
        try {
            serieService.deleteSerie(nombreSerie);
        } catch (Exception e) {
            logger.error("Error al eliminar serie '{}': {}", nombreSerie, e.getMessage(), e);
        }
        return "redirect:/";
    }
}