package cc.sars.controller.api;

import cc.sars.controller.api.dto.GrupoCreateDTO;
import cc.sars.controller.api.dto.GrupoDTO;
import cc.sars.controller.api.dto.GrupoDetailDTO;
import cc.sars.controller.api.dto.GrupoMemberDTO;
import cc.sars.controller.api.dto.UserRoleUpdateDTO;
import cc.sars.model.Grupo;
import cc.sars.model.Serie;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
public class GrupoRestController {

    private static final Logger log = LoggerFactory.getLogger(GrupoRestController.class);

    private final GrupoService grupoService;
    private final UsuarioService usuarioService;

    public GrupoRestController(GrupoService grupoService, UsuarioService usuarioService) {
        this.grupoService = grupoService;
        this.usuarioService = usuarioService;
    }

    private GrupoDetailDTO convertToDetailDto(Grupo grupo) {
        List<GrupoMemberDTO> miembros = grupo.getUsuarioGrupos().stream()
                .map(ug -> new GrupoMemberDTO(ug.getUsuario().getUsername(), ug.getRol()))
                .collect(Collectors.toList());

        List<String> series = grupo.getSeries().stream()
                .map(Serie::getNombre)
                .collect(Collectors.toList());

        return new GrupoDetailDTO(grupo.getNombre(), series, miembros);
    }

    private GrupoDTO convertToSimpleDto(Grupo grupo) {
        return new GrupoDTO(grupo.getNombre());
    }

    /**
     * Obtiene todos los grupos existentes.
     */
    @GetMapping
    public ResponseEntity<List<GrupoDTO>> obtenerTodosLosGrupos() {
        log.info("Solicitud para obtener todos los grupos");
        List<GrupoDTO> grupos = grupoService.getAllGrupos().stream()
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());
        log.info("Se encontraron {} grupos", grupos.size());
        return new ResponseEntity<>(grupos, HttpStatus.OK);
    }

    /**
     * Obtiene un grupo específico por su nombre.
     */
    @GetMapping("/{nombre}")
    public ResponseEntity<GrupoDetailDTO> obtenerGrupoPorNombre(@PathVariable String nombre) {
        log.info("Solicitud para obtener grupo por nombre: {}", nombre);
        try {
            Grupo grupo = grupoService.getGrupoPorNombre(nombre);
            return new ResponseEntity<>(convertToDetailDto(grupo), HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado", e);
        }
    }

    /**
     * Crea un nuevo grupo.
     */
    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO) {
        log.info("Solicitud para crear grupo: {}", grupoCreateDTO.getNombre());
        try {
            Grupo nuevoGrupo = grupoService.crearGrupo(grupoCreateDTO.getNombre());
            log.info("Grupo '{}' creado exitosamente", nuevoGrupo.getNombre());
            return new ResponseEntity<>(convertToSimpleDto(nuevoGrupo), HttpStatus.CREATED);
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

    @PutMapping("/{nombreGrupo}/miembros/{username}/rol")
    public ResponseEntity<Void> cambiarRolMiembro(@PathVariable String nombreGrupo, @PathVariable String username, @RequestBody UserRoleUpdateDTO userRoleUpdateDTO) {
        log.info("Solicitud para cambiar rol de usuario '{}' en grupo '{}' a '{}'", username, nombreGrupo, userRoleUpdateDTO.getRolEnGrupo());
        try {
            usuarioService.cambiarRolEnGrupo(username, userRoleUpdateDTO.getRolEnGrupo(), nombreGrupo);
            log.info("Rol de usuario '{}' en grupo '{}' cambiado exitosamente a '{}'", username, nombreGrupo, userRoleUpdateDTO.getRolEnGrupo());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error al cambiar rol de usuario '{}' en grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
