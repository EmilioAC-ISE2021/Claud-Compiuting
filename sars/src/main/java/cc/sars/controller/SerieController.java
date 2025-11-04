package cc.sars.controller;

import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.User; // Importar User
import cc.sars.service.SerieService;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar
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
     * Mapeo para procesar el formulario de creación de una nueva serie.
     * ¡MODIFICADO para Grupos!
     */
    @PostMapping("/crear-serie")
    public String createSerie(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @AuthenticationPrincipal User user) { // 1. Obtenemos el usuario logueado
        
        try {
            // 2. Obtenemos el grupo del usuario
            String nombreGrupo = user.getGrupos().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Usuario sin grupo no puede crear series")).getNombre();
            
            // 3. Llamamos al servicio con 3 argumentos (esto arregla el error de compilación)
            serieService.createSerie(nombre, descripcion, nombreGrupo);
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        return "redirect:/"; // Redirigimos al index (que mostrará la lista actualizada)
    }

    /**
     * Mapeo para la página de "administrar serie" (detalles).
     * (Sin cambios funcionales, pero ahora está protegido por la autenticación)
     */
    @GetMapping("/serie/{nombreSerie}")
    public String getSerieDetalle(@PathVariable String nombreSerie, Model model) {
        
        // TODO (Seguridad): Faltaría comprobar que el usuario logueado
        // pertenece al grupo dueño de esta serie. (Mejora futura).

        Serie serie = serieService.getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la serie: " + nombreSerie));
        
        model.addAttribute("serie", serie);
        model.addAttribute("todosLosEstados", serieService.getTodosLosEstados());
        
        return "serie-detalle";
    }

    /**
     * Mapeo para procesar el formulario de añadir un capítulo a una serie.
     * (Sin cambios funcionales)
     */
    @PostMapping("/serie/{nombreSerie}/crear-capitulo")
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
     * (Sin cambios funcionales)
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
     * (Sin cambios funcionales)
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
        return "redirect:/serie/" + nombreSerie;
    }
}