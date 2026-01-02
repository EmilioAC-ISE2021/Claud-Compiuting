package cc.sars.controller.api;

import cc.sars.model.EstadosTareas;
import cc.sars.service.SerieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaUtilRestController {

    private final SerieService serieService;

    public TareaUtilRestController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping("/estados")
    public List<EstadosTareas> getTodosLosEstados() {
        return serieService.getTodosLosEstados();
    }
}
