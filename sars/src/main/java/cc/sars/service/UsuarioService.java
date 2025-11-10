package cc.sars.service;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.HashSet; // Importar HashSet

@Service
@Transactional
public class UsuarioService {

    private final UserRepository userRepository;
    private final GrupoRepository grupoRepository;
    private final PasswordEncoder passwordEncoder;
    private final GrupoService grupoService; // Inyectar GrupoService
    private final SerieService serieService; // Inyectar SerieService

    public UsuarioService(UserRepository userRepository, GrupoRepository grupoRepository, PasswordEncoder passwordEncoder, GrupoService grupoService, SerieService serieService) {
        this.userRepository = userRepository;
        this.grupoRepository = grupoRepository;
        this.passwordEncoder = passwordEncoder;
        this.grupoService = grupoService;
        this.serieService = serieService;
    }

    /**
     * Registra un nuevo usuario, hasheando su contraseña.
     * Si 'crearGrupo' es true, también crea el grupo y lo asigna como LIDER.
     */
    public User registrarUsuario(String nombreUsuario, String contrasenya, boolean crearGrupo, String nombreGrupo) {
        
        if (userRepository.findByUsername(nombreUsuario).isPresent()) {
            throw new RuntimeException("Error: El nombre de usuario '" + nombreUsuario + "' ya existe.");
        }
        User nuevoUsuario;
        if (crearGrupo) {
            if (nombreGrupo == null || nombreGrupo.trim().isEmpty()) {
                throw new RuntimeException("Error: El nombre del grupo es obligatorio si se crea un grupo.");
            }
            if (grupoRepository.findByNombre(nombreGrupo).isPresent()) {
                throw new RuntimeException("Error: El nombre de grupo '" + nombreGrupo + "' ya existe.");
            }
            nuevoUsuario = new User(
                    nombreUsuario,
                    passwordEncoder.encode(contrasenya), 
                    Role.ROLE_LIDER
            );
            userRepository.save(nuevoUsuario);

            Grupo nuevoGrupo = new Grupo(nombreGrupo);
            
            nuevoGrupo.agregarUsuario(nuevoUsuario); 
            grupoRepository.save(nuevoGrupo);
        } else {
            nuevoUsuario = new User(
                    nombreUsuario,
                    passwordEncoder.encode(contrasenya),
                    Role.ROLE_USER
            );
            userRepository.save(nuevoUsuario);
        }
        return nuevoUsuario;
    }

    /**
     * Devuelve todos los usuarios registrados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<User> getTodosLosUsuarios() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario por su nombre de usuario.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Cambia el rol de un usuario dentro de un grupo, con validación.
     * Asegura que siempre haya al menos un LIDER en el grupo.
     */
    public void changeUserRole(String username, Role newRole, String groupName) {
        User userToModify = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Grupo group = grupoRepository.findByNombre(groupName)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + groupName));

        // Validar que el usuario pertenece al grupo
        if (!userToModify.getGrupos().contains(group)) {
            throw new RuntimeException("El usuario " + username + " no pertenece al grupo " + groupName);
        }

        // Si el rol actual es LIDER y el nuevo rol no es LIDER,
        // debemos asegurarnos de que haya al menos otro LIDER en el grupo.
        if (userToModify.getRole() == Role.ROLE_LIDER && newRole != Role.ROLE_LIDER) {
            long leaderCount = group.getUsuarios().stream()
                    .filter(u -> u.getRole() == Role.ROLE_LIDER && !u.getUsername().equals(username))
                    .count();
            if (leaderCount == 0) {
                throw new RuntimeException("No se puede cambiar el rol. Debe haber al menos un LIDER en el grupo.");
            }
        }

        userToModify.setRole(newRole);
        userRepository.save(userToModify);
    }

    public User cambiarRolAUsuario(User user) {
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }

    public User createAdminUser(String nombreUsuario, String contrasenya) {
        if (userRepository.findByUsername(nombreUsuario).isPresent()) {
            throw new RuntimeException("Error: El nombre de usuario '" + nombreUsuario + "' ya existe.");
        }
        User nuevoUsuario = new User(
                nombreUsuario,
                passwordEncoder.encode(contrasenya),
                Role.ROLE_ADMIN
        );
        return userRepository.save(nuevoUsuario);
    }

    /**
     * Elimina un usuario por su nombre de usuario.
     * Asegura que el usuario es eliminado de todos los grupos a los que pertenece
     * y que las tareas asignadas a este usuario son desasignadas.
     */
    public void eliminarUsuario(String username) {
        User userToDelete = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        // Eliminar el usuario de todos los grupos a los que pertenece
        // Se crea una copia para evitar ConcurrentModificationException
        for (Grupo grupo : new HashSet<>(userToDelete.getGrupos())) { // Usar una copia del Set
            grupoService.eliminarUsuarioDeGrupo(grupo.getNombre(), username);
        }

        // Desasignar tareas de este usuario
        serieService.desasignarUsuarioDeTareas(username);

        userRepository.deleteById(username);
    }
}
