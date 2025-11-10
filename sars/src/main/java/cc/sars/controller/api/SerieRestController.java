package cc.sars.controller.api;

import cc.sars.controller.api.dto.SerieCreateDTO;
import cc.sars.controller.api.dto.SerieDTO;
import cc.sars.controller.api.dto.SerieUpdateDTO;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/series")
public class SerieRestController {

    private static final Logger log = LoggerFactory.getLogger(SerieRestController.class);

    private final SerieService serieService;

    public SerieRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping
    public List<SerieDTO> getAllSeries() {
        log.info("Request to get all series");
        List<Serie> series = serieService.buscarTodas();
        List<SerieDTO> seriesDTO = series.stream()
                .map(serie -> new SerieDTO(serie.getNombre(), serie.getDescripcion()))
                .collect(Collectors.toList());
        log.info("Found {} series", seriesDTO.size());
        return seriesDTO;
    }

    @GetMapping("/{nombre}")
    public SerieDTO getSerieByNombre(@PathVariable String nombre) {
        log.info("Request to get serie by nombre: {}", nombre);
        Serie serie = serieService.getSerieByNombre(nombre)
                .orElseThrow(() -> new cc.sars.exception.SerieNotFoundException("Serie no encontrada con el nombre: " + nombre));
        return new SerieDTO(serie.getNombre(), serie.getDescripcion());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SerieDTO createSerie(@RequestBody SerieCreateDTO serieCreateDTO) {
        log.info("Request to create serie: {}", serieCreateDTO.getNombre());
        Serie nuevaSerie = serieService.createSerie(serieCreateDTO.getNombre(), serieCreateDTO.getDescripcion(), serieCreateDTO.getNombreGrupo());
        return new SerieDTO(nuevaSerie.getNombre(), nuevaSerie.getDescripcion());
    }

    @PutMapping("/{nombre}")
    public SerieDTO updateSerie(@PathVariable String nombre, @RequestBody SerieUpdateDTO serieUpdateDTO) {
        log.info("Request to update serie: {}", nombre);
        Serie serieActualizada = serieService.updateSerie(nombre, serieUpdateDTO.getDescripcion());
        return new SerieDTO(serieActualizada.getNombre(), serieActualizada.getDescripcion());
    }
}
