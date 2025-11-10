package cc.sars.controller.api;

import cc.sars.controller.api.dto.SerieDTO;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
