package cc.sars.controller.api;

import cc.sars.controller.api.dto.SerieCreateDTO;
import cc.sars.controller.api.dto.SerieDTO;
import cc.sars.controller.api.dto.SerieUpdateDTO;
import cc.sars.exception.SerieAlreadyExistsException;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos/{nombreGrupo}/series")
public class SerieRestController {

    private static final Logger log = LoggerFactory.getLogger(SerieRestController.class);

    private final SerieService serieService;

    public SerieRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping
    public List<SerieDTO> getSeriesByGrupo(@PathVariable String nombreGrupo) {
        log.info("Solicitud para obtener todas las series del grupo {}", nombreGrupo);
        List<Serie> series = serieService.getSeriesPorGrupo(nombreGrupo);
        List<SerieDTO> seriesDTO = series.stream()
                .map(serie -> new SerieDTO(serie.getNombre(), serie.getDescripcion()))
                .collect(Collectors.toList());
        log.info("Se encontraron {} series para el grupo {}", seriesDTO.size(), nombreGrupo);
        return seriesDTO;
    }

    @GetMapping("/{nombreSerie}")
    public SerieDTO getSerieByNombre(@PathVariable String nombreGrupo, @PathVariable String nombreSerie) {
        log.info("Solicitud para obtener serie por nombre: {} en el grupo {}", nombreSerie, nombreGrupo);
        Serie serie = serieService.getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new cc.sars.exception.SerieNotFoundException("Serie no encontrada con el nombre: " + nombreSerie + " en el grupo: " + nombreGrupo));
        return new SerieDTO(serie.getNombre(), serie.getDescripcion());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SerieDTO createSerie(@PathVariable String nombreGrupo, @RequestBody SerieCreateDTO serieCreateDTO) {
        log.info("Solicitud para crear serie: {} en el grupo {}", serieCreateDTO.getNombre(), nombreGrupo);
        // Usamos el nombreGrupo del path, ignorando el que pueda venir en el DTO.
        Serie nuevaSerie = serieService.createSerie(serieCreateDTO.getNombre(), serieCreateDTO.getDescripcion(), nombreGrupo);
        return new SerieDTO(nuevaSerie.getNombre(), nuevaSerie.getDescripcion());
    }

    @PutMapping("/{nombreSerie}")
    public SerieDTO updateSerie(@PathVariable String nombreGrupo, @PathVariable String nombreSerie, @RequestBody SerieUpdateDTO serieUpdateDTO) {
        log.info("Solicitud para actualizar serie: {} en el grupo {}", nombreSerie, nombreGrupo);
        Serie serieActualizada = serieService.updateSerieInGrupo(nombreGrupo, nombreSerie, serieUpdateDTO.getDescripcion());
        return new SerieDTO(serieActualizada.getNombre(), serieActualizada.getDescripcion());
    }

    @DeleteMapping("/{nombreSerie}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSerie(@PathVariable String nombreGrupo, @PathVariable String nombreSerie) {
        log.info("Solicitud para eliminar serie: {} en el grupo {}", nombreSerie, nombreGrupo);
        serieService.deleteSerie(nombreGrupo, nombreSerie);
    }
}
