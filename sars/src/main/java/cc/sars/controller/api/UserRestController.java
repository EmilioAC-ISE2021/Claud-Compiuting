package cc.sars.controller.api;

import cc.sars.controller.api.dto.UserCreateDTO;
import cc.sars.controller.api.dto.UserDTO;
import cc.sars.controller.api.dto.UserUpdateDTO;
import cc.sars.model.Grupo;
import cc.sars.model.User;
import cc.sars.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UserRestController {

    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    private final UsuarioService usuarioService;

    public UserRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
        User newUser = usuarioService.crearUsuario(
                userCreateDTO.getUsername(),
                userCreateDTO.getPassword(),
                userCreateDTO.getRole(),
                userCreateDTO.getNombreGrupo()
        );
        return convertToDto(newUser);
    }

    @PutMapping("/{username}")
    public UserDTO updateUser(@PathVariable String username, @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("Solicitud para actualizar usuario: {}", username);
        User updatedUser = usuarioService.actualizarUsuario(
                username,
                userUpdateDTO.getRole(),
                userUpdateDTO.getNombreGrupo()
        );
        return convertToDto(updatedUser);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username) {
        log.info("Solicitud para eliminar usuario: {}", username);
        usuarioService.eliminarUsuario(username);
    }

    private UserDTO convertToDto(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getRole(),
                user.getGrupos().stream().map(Grupo::getNombre).collect(Collectors.toSet())
        );
    }
}