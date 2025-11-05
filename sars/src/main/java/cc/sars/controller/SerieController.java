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

    // ... (createSerie, getSerieDetalle, createCapitulo... quedan igual) ...

    /**
     * Mapeo para procesar el formulario de añadir una tarea a un capítulo.
     */
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/crear-tarea")
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
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/actualizar-estado")
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
            // TODO: Enviar este error a la vista con RedirectAttributes
        }
        return "redirect:/serie/" + nombreSerie;
    }
}