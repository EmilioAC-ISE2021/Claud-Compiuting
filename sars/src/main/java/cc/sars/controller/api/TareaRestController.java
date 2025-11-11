package cc.sars.controller.api;

import cc.sars.controller.api.dto.TareaDTO;
import cc.sars.model.Capitulo;
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
@RequestMapping("/api/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas")
public class TareaRestController {

    private static final Logger log = LoggerFactory.getLogger(TareaRestController.class);

    private final SerieService serieService;

    public TareaRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping
    public List<TareaDTO> getTareasByCapitulo(@PathVariable String nombreSerie, @PathVariable String nombreCapitulo) {
        log.info("Request to get all tasks for chapter {} in series {}", nombreCapitulo, nombreSerie);
        Capitulo capitulo = serieService.getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new cc.sars.exception.SerieNotFoundException("Capitulo no encontrado con el nombre: " + nombreCapitulo));

        return capitulo.getTareas().stream()
                .map(tarea -> new TareaDTO(tarea.getNombre(), tarea.getEstadoTarea(), tarea.getUsuarioAsignado()))
                .collect(Collectors.toList());
    }
}
