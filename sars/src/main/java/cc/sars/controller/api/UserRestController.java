package cc.sars.controller.api;

import cc.sars.controller.api.dto.UserCreateDTO;
import cc.sars.controller.api.dto.UserDTO;
import cc.sars.controller.api.dto.UserGroupMembershipDTO;
import cc.sars.controller.api.dto.UserRoleUpdateDTO;
import cc.sars.model.Grupo;
import cc.sars.model.Serie;
import cc.sars.model.User;
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
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
public class UserRestController {

    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    private final UsuarioService usuarioService;
    private final GrupoService grupoService;

    public UserRestController(UsuarioService usuarioService, GrupoService grupoService) {
        this.usuarioService = usuarioService;
        this.grupoService = grupoService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        log.info("Solicitud para obtener todos los usuarios");
        return usuarioService.getTodosLosUsuarios().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        log.info("Solicitud para obtener usuario por nombre: {}", username);
        User user = usuarioService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return convertToDto(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody UserCreateDTO userCreateDTO) {
        log.info("Solicitud para crear usuario: {}", userCreateDTO.getUsername());
        
        boolean crearGrupo = userCreateDTO.getNombreGrupo() != null && !userCreateDTO.getNombreGrupo().trim().isEmpty();
        
        User newUser = usuarioService.registrarUsuario(
                userCreateDTO.getUsername(),
                userCreateDTO.getPassword(),
                crearGrupo,
                userCreateDTO.getNombreGrupo()
        );
        return convertToDto(newUser);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username) {
        log.info("Solicitud para eliminar usuario: {}", username);
        usuarioService.eliminarUsuario(username);
    }

    @PostMapping("/{username}/grupos/{nombreGrupo}")
    public ResponseEntity<Void> agregarUsuarioAGrupo(@PathVariable String username, @PathVariable String nombreGrupo) {
        log.info("Solicitud para añadir usuario '{}' al grupo '{}'", username, nombreGrupo);
        try {
            grupoService.agregarUsuarioAGrupo(username, nombreGrupo);
            log.info("Usuario '{}' añadido exitosamente al grupo '{}'", username, nombreGrupo);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error al añadir usuario '{}' al grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{username}/grupos/{nombreGrupo}")
    public ResponseEntity<Void> eliminarUsuarioDeGrupo(@PathVariable String username, @PathVariable String nombreGrupo) {
        log.info("Solicitud para eliminar usuario '{}' del grupo '{}'", username, nombreGrupo);
        try {
            grupoService.eliminarUsuarioDeGrupo(nombreGrupo, username);
            log.info("Usuario '{}' eliminado exitosamente del grupo '{}'", username, nombreGrupo);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar usuario '{}' del grupo '{}': {}", username, nombreGrupo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{username}/grupos/{nombreGrupo}/rol")
    public ResponseEntity<Void> cambiarRolUsuarioEnGrupo(@PathVariable String username, @PathVariable String nombreGrupo, @RequestBody UserRoleUpdateDTO userRoleUpdateDTO) {
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

    private UserDTO convertToDto(User user) {
        List<UserGroupMembershipDTO> membresiasGrupo = user.getUsuarioGrupos().stream()
                .map(ug -> new UserGroupMembershipDTO(ug.getGrupo().getNombre(), ug.getRol()))
                .collect(Collectors.toList());
        return new UserDTO(user.getUsername(), membresiasGrupo);
    }
}
