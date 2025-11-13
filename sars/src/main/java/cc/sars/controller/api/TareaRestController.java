package cc.sars.controller.api;

import cc.sars.controller.api.dto.TareaCreateDTO;
import cc.sars.controller.api.dto.TareaDTO;
import cc.sars.controller.api.dto.TareaUpdateDTO;
import cc.sars.exception.SerieNotFoundException;
import cc.sars.model.Capitulo;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas")
public class TareaRestController {

    private static final Logger log = LoggerFactory.getLogger(TareaRestController.class);

    private final SerieService serieService;

    public TareaRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    private Capitulo findCapituloOrThrow(String nombreGrupo, String nombreSerie, String nombreCapitulo) {
        Serie serie = serieService.getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("Serie no encontrada con el nombre: " + nombreSerie + " en el grupo: " + nombreGrupo));

        return serie.getCapitulos().stream()
                .filter(c -> c.getNombre().equals(nombreCapitulo))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("Capítulo no encontrado con el nombre: " + nombreCapitulo + " en la serie: " + nombreSerie));
    }

    @GetMapping
    public List<TareaDTO> getTareasByCapitulo(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo) {
        log.info("Solicitud para obtener las tareas del capítulo '{}' en la serie '{}' del grupo '{}'", nombreCapitulo, nombreSerie, nombreGrupo);
        Capitulo capitulo = findCapituloOrThrow(nombreGrupo, nombreSerie, nombreCapitulo);

        return capitulo.getTareas().stream()
                .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TareaDTO addTareaToCapitulo(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo, @RequestBody TareaCreateDTO tareaCreateDTO) {
        log.info("Solicitud para añadir la tarea '{}' al capítulo '{}'", tareaCreateDTO.getNombre(), nombreCapitulo);
        // First, ensure the chapter exists in the hierarchy
        findCapituloOrThrow(nombreGrupo, nombreSerie, nombreCapitulo);

        Capitulo capituloActualizado = serieService.addTareaToCapitulo(nombreCapitulo, tareaCreateDTO.getNombre());
        Tarea nuevaTarea = capituloActualizado.getTareas().stream()
                .filter(t -> t.getNombre().equals(tareaCreateDTO.getNombre()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo encontrar la tarea recién creada")); // No debería ocurrir
        return new TareaDTO(nuevaTarea.getNombre(), nuevaTarea.getEstadoTarea(), nuevaTarea.getUsuarioAsignado());
    }

    @PutMapping("/{nombreTarea}")
    public TareaDTO updateTarea(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo, @PathVariable String nombreTarea, @RequestBody TareaUpdateDTO tareaUpdateDTO) {
        log.info("Solicitud para actualizar la tarea '{}' en el capítulo '{}'", nombreTarea, nombreCapitulo);
        // First, ensure the chapter exists in the hierarchy
        findCapituloOrThrow(nombreGrupo, nombreSerie, nombreCapitulo);

        Tarea tareaActualizada = serieService.updateTarea(nombreCapitulo, nombreTarea, tareaUpdateDTO.getEstado(), tareaUpdateDTO.getUsuarioAsignado());
        return new TareaDTO(tareaActualizada.getNombre(), tareaActualizada.getEstadoTarea(), tareaActualizada.getUsuarioAsignado());
    }

    @GetMapping("/{nombreTarea}")
    public TareaDTO getTareaByNombre(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo, @PathVariable String nombreTarea) {
        log.info("Solicitud para obtener la tarea '{}' del capítulo '{}' en la serie '{}' del grupo '{}'", nombreTarea, nombreCapitulo, nombreSerie, nombreGrupo);
        // First, ensure the chapter exists in the hierarchy
        findCapituloOrThrow(nombreGrupo, nombreSerie, nombreCapitulo);

        Tarea tarea = serieService.getTareaByNombre(nombreCapitulo, nombreTarea)
                .orElseThrow(() -> new SerieNotFoundException("Tarea no encontrada con el nombre: " + nombreTarea + " en el capítulo: " + nombreCapitulo));
        return new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado());
    }

    @DeleteMapping("/{nombreTarea}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTarea(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo, @PathVariable String nombreTarea) {
        log.info("Solicitud para eliminar la tarea '{}' del capítulo '{}'", nombreTarea, nombreCapitulo);
        // First, ensure the chapter exists in the hierarchy
        findCapituloOrThrow(nombreGrupo, nombreSerie, nombreCapitulo);
        
        serieService.deleteTarea(nombreCapitulo, nombreTarea);
    }
}