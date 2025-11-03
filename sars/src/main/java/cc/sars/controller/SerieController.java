package cc.sars.controller;

import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.NoSuchElementException;

@Controller // Marcamos esta clase como un controlador web de Spring
public class SerieController {

    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    /**
     * Mapeo para la página de inicio (/).
     * Muestra la lista de todas las series.
     */
    @GetMapping("/")
    public String getIndex(Model model) {
        // 1. Obtenemos todas las series del servicio
        model.addAttribute("series", serieService.getAllSeries());
        // 2. Devolvemos el nombre de la plantilla "index.html"
        return "index";
    }

    /**
     * Mapeo para procesar el formulario de creación de una nueva serie.
     */
    @PostMapping("/crear-serie")
    public String createSerie(@RequestParam String nombre,
                              @RequestParam String descripcion) {
        
        try {
            serieService.createSerie(nombre, descripcion);
        } catch (Exception e) {
            // Aquí podrías manejar el error, por ejemplo, si la serie ya existe
            // Por ahora, solo lo imprimimos y redirigimos
            System.err.println(e.getMessage());
        }
        // Redirigimos a la página de inicio para ver la lista actualizada
        return "redirect:/";
    }

    /**
     * Mapeo para la página de "administrar serie" (detalles).
     * Muestra los detalles de UNA serie y sus capítulos/tareas.
     */
    @GetMapping("/serie/{nombreSerie}")
    public String getSerieDetalle(@PathVariable String nombreSerie, Model model) {
        // 1. Buscamos la serie por su nombre
        Serie serie = serieService.getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la serie: " + nombreSerie));
        
        // 2. La añadimos al modelo para que Thymeleaf la use
        model.addAttribute("serie", serie);
        // 3. Añadimos la lista de todos los estados posibles (para los desplegables)
        model.addAttribute("todosLosEstados", serieService.getTodosLosEstados());
        
        // 4. Devolvemos el nombre de la plantilla "serie-detalle.html"
        return "serie-detalle";
    }

    /**
     * Mapeo para procesar el formulario de añadir un capítulo a una serie.
     */
    @PostMapping("/serie/{nombreSerie}/crear-capitulo")
    public String createCapitulo(@PathVariable String nombreSerie,
                                 @RequestParam String nombreCapitulo) {
        try {
            serieService.addCapituloToSerie(nombreSerie, nombreCapitulo);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        // Redirigimos de vuelta a la página de detalles de la serie
        return "redirect:/serie/" + nombreSerie;
    }

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
        // Redirigimos de vuelta a la página de detalles de la serie
        return "redirect:/serie/" + nombreSerie;
    }

    /**
     * Mapeo para procesar la actualización del estado de una tarea (el desplegable).
     */
    @PostMapping("/serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/actualizar-estado")
    public String updateTareaEstado(@PathVariable String nombreSerie,
                                    @PathVariable String nombreCapitulo,
                                    @PathVariable String nombreTarea,
                                    @RequestParam EstadosTareas estado) {
        try {
            serieService.updateTareaEstado(nombreCapitulo, nombreTarea, estado);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        // Redirigimos de vuelta a la página de detalles de la serie
        return "redirect:/serie/" + nombreSerie;
    }
}