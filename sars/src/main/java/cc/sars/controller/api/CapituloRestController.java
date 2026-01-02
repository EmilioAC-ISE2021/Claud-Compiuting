package cc.sars.controller.api;

import cc.sars.controller.api.dto.CapituloCreateDTO;
import cc.sars.controller.api.dto.CapituloDTO;
import cc.sars.controller.api.dto.CapituloBulkCreateDTO;
import cc.sars.controller.api.dto.TareaDTO;
import cc.sars.exception.SerieNotFoundException;
import cc.sars.model.Capitulo;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@RestController
@RequestMapping("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos")
public class CapituloRestController {

    // No se provee un endpoint PUT para Capítulo ya que su nombre es inmutable y las tareas se gestionan por separado.

    private static final Logger log = LoggerFactory.getLogger(CapituloRestController.class);

    private final SerieService serieService;

    public CapituloRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping
    public List<CapituloDTO> getCapitulosBySerie(@PathVariable String nombreGrupo, @PathVariable String nombreSerie) {
        log.info("Solicitud para obtener capítulos de la serie: {} en el grupo: {}", nombreSerie, nombreGrupo);
        Serie serie = serieService.getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("Serie no encontrada con el nombre: " + nombreSerie));

        List<Capitulo> capitulos = serie.getCapitulos();
        List<CapituloDTO> capitulosDTO = capitulos.stream()
                .map(capitulo -> new CapituloDTO(
                        capitulo.getNombre(),
                        nombreSerie,
                        capitulo.getTareas().stream()
                                .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} capítulos para la serie {}", capitulosDTO.size(), nombreSerie);
        return capitulosDTO;
    }

    @GetMapping("/{nombreCapitulo}")
    public CapituloDTO getCapituloByNombre(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo) {
        log.info("Solicitud para obtener el capítulo '{}' de la serie '{}' en el grupo '{}'", nombreCapitulo, nombreSerie, nombreGrupo);
        Serie serie = serieService.getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("Serie no encontrada con el nombre: " + nombreSerie));

        Capitulo capitulo = serie.getCapitulos().stream()
                .filter(c -> c.getNombre().equals(nombreCapitulo))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("Capítulo no encontrado con el nombre: " + nombreCapitulo + " en la serie: " + nombreSerie));

        return new CapituloDTO(
                capitulo.getNombre(),
                nombreSerie,
                capitulo.getTareas().stream()
                        .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CapituloDTO createCapitulo(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @RequestBody CapituloCreateDTO capituloCreateDTO) {
        log.info("Solicitud para crear el capítulo '{}' para la serie '{}' en el grupo '{}'", capituloCreateDTO.getNombreCapitulo(), nombreSerie, nombreGrupo);
        Serie serieActualizada = serieService.addCapituloToSerie(nombreGrupo, nombreSerie, capituloCreateDTO.getNombreCapitulo());

        Capitulo nuevoCapitulo = serieActualizada.getCapitulos().stream()
                .filter(c -> c.getNombre().equals(capituloCreateDTO.getNombreCapitulo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo encontrar el capítulo recién creado")); // Should not happen

        return new CapituloDTO(
                nuevoCapitulo.getNombre(),
                nombreSerie,
                nuevoCapitulo.getTareas().stream()
                        .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CapituloDTO> createBulkCapitulos(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @RequestBody CapituloBulkCreateDTO bulkCreateDTO) {
        log.info("Solicitud para crear capítulos en masa para la serie '{}' en el grupo '{}'", nombreSerie, nombreGrupo);

        // Convert List<TareaCreateDTO> to String[] expected by service
        String[] tareasEnMasaArray = bulkCreateDTO.getTareasEnMasa().stream()
                .map(tareaDTO -> String.format("%s###%s###%s",
                        tareaDTO.getNombre(),
                        tareaDTO.getEstadoTarea() != null ? tareaDTO.getEstadoTarea().name() : "NoAsignado", // Default if null
                        tareaDTO.getUsuarioAsignado() != null ? tareaDTO.getUsuarioAsignado() : "NADIE")) // Default if null
                .toArray(String[]::new);

        Serie serieActualizada = serieService.addCapitulosToSerie(nombreGrupo, nombreSerie, bulkCreateDTO.getNombresCapitulos(), tareasEnMasaArray);

        // Return the newly created chapters as DTOs
        return serieActualizada.getCapitulos().stream()
                .filter(capitulo -> Arrays.asList(bulkCreateDTO.getNombresCapitulos().split("\r?\n")).contains(capitulo.getNombre())) // Filter for newly added chapters
                .map(capitulo -> new CapituloDTO(
                        capitulo.getNombre(),
                        nombreSerie,
                        capitulo.getTareas().stream()
                                .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{nombreCapitulo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCapitulo(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @PathVariable String nombreCapitulo) {
        log.info("Solicitud para eliminar el capítulo '{}' de la serie '{}' en el grupo '{}'", nombreCapitulo, nombreSerie, nombreGrupo);
        serieService.deleteCapitulo(nombreGrupo, nombreSerie, nombreCapitulo);
    }
}
