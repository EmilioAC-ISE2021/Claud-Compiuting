package cc.sars.controller;

import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.User;
import cc.sars.service.SerieService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.NoSuchElementException;

@Controller
public class SerieController {

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
            System.err.println(e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/serie/{nombreSerie}")
    public String getSerieDetalle(@PathVariable String nombreSerie, Model model, @AuthenticationPrincipal User usuarioActual) {
        Serie serie = serieService.getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new NoSuchElementException("Serie no encontrada: " + nombreSerie));
        model.addAttribute("serie", serie);
        model.addAttribute("todosLosEstados", serieService.getTodosLosEstados());
        model.addAttribute("usuarioActual", usuarioActual); // Añadir el usuario actual al modelo
        return "app/serie-detalle";
    }

    @PostMapping("/serie/{nombreSerie}/capitulo/crear")
    public String createCapitulo(@PathVariable String nombreSerie,
                                 @RequestParam String nombreCapitulo) {
        try {
            serieService.addCapituloToSerie(nombreSerie, nombreCapitulo);
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
        }
        return "redirect:/serie/" + nombreSerie;
    }
}