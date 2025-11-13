package cc.sars.controller.api;

import cc.sars.controller.api.dto.UserRoleUpdateDTO;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios") // Base path for user-related operations
public class UsuarioGrupoRestController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioGrupoRestController.class);

    private final UsuarioService usuarioService;
    private final GrupoService grupoService;

    public UsuarioGrupoRestController(UsuarioService usuarioService, GrupoService grupoService) {
        this.usuarioService = usuarioService;
        this.grupoService = grupoService;
    }

    /**
     * Añade un usuario a un grupo.
     * POST /api/usuarios/{username}/grupos/{nombreGrupo}
     */
    @PostMapping("/{username}/grupos/{nombreGrupo}")
    @ResponseStatus(HttpStatus.OK)
    public void agregarUsuarioAGrupo(@PathVariable String username, @PathVariable String nombreGrupo) {
        log.info("Solicitud para añadir usuario '{}' al grupo '{}'", username, nombreGrupo);
        try {
            grupoService.agregarUsuarioAGrupo(username, nombreGrupo);
            log.info("Usuario '{}' añadido exitosamente al grupo '{}'", username, nombreGrupo);
        } catch (RuntimeException e) {
            log.error("Error al añadir usuario '{}' al grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Elimina un usuario de un grupo.
     * DELETE /api/usuarios/{username}/grupos/{nombreGrupo}
     */
    @DeleteMapping("/{username}/grupos/{nombreGrupo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuarioDeGrupo(@PathVariable String username, @PathVariable String nombreGrupo) {
        log.info("Solicitud para eliminar usuario '{}' del grupo '{}'", username, nombreGrupo);
        try {
            grupoService.eliminarUsuarioDeGrupo(nombreGrupo, username);
            log.info("Usuario '{}' eliminado exitosamente del grupo '{}'", username, nombreGrupo);
        } catch (RuntimeException e) {
            log.error("Error al eliminar usuario '{}' del grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Cambia el rol de un usuario dentro de un grupo específico.
     * PUT /api/usuarios/{username}/grupos/{nombreGrupo}/rol
     */
    @PutMapping("/{username}/grupos/{nombreGrupo}/rol")
    @ResponseStatus(HttpStatus.OK)
    public void cambiarRolUsuarioEnGrupo(@PathVariable String username, @PathVariable String nombreGrupo, @RequestBody UserRoleUpdateDTO userRoleUpdateDTO) {
        log.info("Solicitud para cambiar rol de usuario '{}' en grupo '{}' a '{}'", username, nombreGrupo, userRoleUpdateDTO.getRol());
        try {
            usuarioService.changeUserRole(username, userRoleUpdateDTO.getRol(), nombreGrupo);
            log.info("Rol de usuario '{}' en grupo '{}' cambiado exitosamente a '{}'", username, nombreGrupo, userRoleUpdateDTO.getRol());
        } catch (RuntimeException e) {
            log.error("Error al cambiar rol de usuario '{}' en grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
