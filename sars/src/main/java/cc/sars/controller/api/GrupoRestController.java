package cc.sars.controller.api;

import cc.sars.controller.api.dto.GrupoCreateDTO;
import cc.sars.model.Grupo;
import cc.sars.service.GrupoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
public class GrupoRestController {

    private static final Logger log = LoggerFactory.getLogger(GrupoRestController.class);

    private final GrupoService grupoService;

    public GrupoRestController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    /**
     * Obtiene todos los grupos existentes.
     */
    @GetMapping
    public ResponseEntity<List<Grupo>> obtenerTodosLosGrupos() {
        log.info("Solicitud para obtener todos los grupos");
        List<Grupo> grupos = grupoService.getAllGrupos();
        log.info("Se encontraron {} grupos", grupos.size());
        return new ResponseEntity<>(grupos, HttpStatus.OK);
    }

    /**
     * Obtiene un grupo específico por su nombre.
     */
    @GetMapping("/{nombre}")
    public ResponseEntity<Grupo> obtenerGrupoPorNombre(@PathVariable String nombre) {
        log.info("Solicitud para obtener grupo por nombre: {}", nombre);
        try {
            Grupo grupo = grupoService.getGrupoPorNombre(nombre);
            return new ResponseEntity<>(grupo, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.warn("Grupo no encontrado con el nombre: {}", nombre);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea un nuevo grupo.
     */
    @PostMapping
    public ResponseEntity<Grupo> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO) {
        log.info("Solicitud para crear grupo: {}", grupoCreateDTO.getNombre());
        try {
            Grupo nuevoGrupo = grupoService.crearGrupo(grupoCreateDTO.getNombre());
            log.info("Grupo '{}' creado exitosamente", nuevoGrupo.getNombre());
            return new ResponseEntity<>(nuevoGrupo, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Error al crear grupo '{}': {}", grupoCreateDTO.getNombre(), e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    /**
     * Elimina un grupo por su nombre.
     */
    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminarGrupo(@PathVariable String nombre) {
        log.info("Solicitud para eliminar grupo: {}", nombre);
        try {
            grupoService.deleteGrupo(nombre);
            log.info("Grupo '{}' eliminado exitosamente", nombre);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            log.warn("No se pudo eliminar el grupo '{}': {}", nombre, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
