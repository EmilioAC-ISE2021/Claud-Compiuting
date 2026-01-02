package cc.sars.service;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.UserRepository;
import cc.sars.exception.ResourceAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import cc.sars.repository.UsuarioGrupoRepository;
import cc.sars.model.UsuarioGrupo;
import cc.sars.model.UsuarioGrupoId;
import java.util.HashSet; // Importar HashSet
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class UsuarioService {

    private final UserRepository userRepository;
    private final GrupoRepository grupoRepository;
    private final PasswordEncoder passwordEncoder;
    private final GrupoService grupoService;
    private final @Lazy SerieService serieService;
    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public UsuarioService(UserRepository userRepository, GrupoRepository grupoRepository, PasswordEncoder passwordEncoder, GrupoService grupoService, SerieService serieService, UsuarioGrupoRepository usuarioGrupoRepository) {
        this.userRepository = userRepository;
        this.grupoRepository = grupoRepository;
        this.passwordEncoder = passwordEncoder;
        this.grupoService = grupoService;
        this.serieService = serieService;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Registra un nuevo usuario, hasheando su contraseña.
     * Si 'crearGrupo' es true, también crea el grupo y lo asigna como LIDER.
     */
    public User registrarUsuario(String nombreUsuario, String contrasenya, boolean crearGrupo, String nombreGrupo) {
        
        if (userRepository.findByUsername(nombreUsuario).isPresent()) {
            throw new ResourceAlreadyExistsException("Error: El nombre de usuario '" + nombreUsuario + "' ya existe.");
        }
        User nuevoUsuario;
        if (crearGrupo) {
            if (nombreGrupo == null || nombreGrupo.trim().isEmpty()) {
                throw new RuntimeException("Error: El nombre del grupo es obligatorio si se crea un grupo.");
            }
            if (grupoRepository.findByNombre(nombreGrupo).isPresent()) {
                throw new ResourceAlreadyExistsException("Error: El nombre de grupo '" + nombreGrupo + "' ya existe.");
            }
            nuevoUsuario = new User(
                    nombreUsuario,
                    passwordEncoder.encode(contrasenya), 
                    Role.ROLE_USER
            );
            userRepository.save(nuevoUsuario);

            Grupo nuevoGrupo = new Grupo(nombreGrupo);
            grupoRepository.save(nuevoGrupo);

            UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
            usuarioGrupo.setUsuario(nuevoUsuario);
            usuarioGrupo.setGrupo(nuevoGrupo);
            usuarioGrupo.setRol(Role.ROLE_LIDER);
            usuarioGrupoRepository.save(usuarioGrupo);
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
        return userRepository.findByUsernameWithGrupos(username);
    }

    /**
     * Cambia el rol de un usuario dentro de un grupo, con validación.
     * Asegura que siempre haya al menos un LIDER en el grupo.
     */
    public void cambiarRolEnGrupo(String username, Role newRole, String groupName) {
        User userToModify = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Grupo group = grupoRepository.findByNombre(groupName)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + groupName));

        UsuarioGrupoId usuarioGrupoId = new UsuarioGrupoId(userToModify.getUsername(), group.getNombre());
        UsuarioGrupo usuarioGrupo = usuarioGrupoRepository.findById(usuarioGrupoId)
                .orElseThrow(() -> new RuntimeException("El usuario " + username + " no pertenece al grupo " + groupName));

        // Si el rol actual es LIDER y el nuevo rol no es LIDER,
        // debemos asegurarnos de que haya al menos otro LIDER en el grupo.
        if (usuarioGrupo.getRol() == Role.ROLE_LIDER && newRole != Role.ROLE_LIDER) {
            long leaderCount = usuarioGrupoRepository.countByGrupoAndRol(group, Role.ROLE_LIDER);
            if (leaderCount <= 1) {
                throw new RuntimeException("No se puede cambiar el rol. Debe haber al menos un LIDER en el grupo.");
            }
        }

        usuarioGrupo.setRol(newRole);
        usuarioGrupoRepository.save(usuarioGrupo);
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

        // Verificar si el usuario es el único LIDER de algún grupo
        for (UsuarioGrupo usuarioGrupo : userToDelete.getUsuarioGrupos()) {
            if (usuarioGrupo.getRol() == Role.ROLE_LIDER) {
                long liderCount = usuarioGrupoRepository.countByGrupoAndRol(usuarioGrupo.getGrupo(), Role.ROLE_LIDER);
                if (liderCount <= 1) {
                    throw new RuntimeException("No se puede eliminar al usuario '" + username + "' porque es el único LIDER del grupo '" + usuarioGrupo.getGrupo().getNombre() + "'. Elimine el grupo primero.");
                }
            }
        }

        // Eliminar el usuario de todos los grupos a los que pertenece
        // Se crea una copia para evitar ConcurrentModificationException al modificar la colección subyacente
        for (UsuarioGrupo usuarioGrupo : new HashSet<>(userToDelete.getUsuarioGrupos())) {
            grupoService.eliminarUsuarioDeGrupo(usuarioGrupo.getGrupo().getNombre(), username);
        }

        // Desasignar tareas de este usuario
        serieService.desasignarUsuarioDeTareas(username);

        userRepository.deleteById(username);
    }

    /**
     * Crea un nuevo usuario y lo asigna a un grupo existente.
     */
    public User crearUsuario(String username, String password, Role role, String nombreGrupo) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResourceAlreadyExistsException("Error: El nombre de usuario '" + username + "' ya existe.");
        }
        
        User nuevoUsuario = new User(
                username,
                passwordEncoder.encode(password),
                Role.ROLE_USER
        );
        userRepository.save(nuevoUsuario);

        if (nombreGrupo != null && !nombreGrupo.trim().isEmpty()) {
            Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + nombreGrupo));

            UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
            usuarioGrupo.setUsuario(nuevoUsuario);
            usuarioGrupo.setGrupo(grupo);
            usuarioGrupo.setRol(Role.ROLE_LIDER);
            usuarioGrupoRepository.save(usuarioGrupo);
        }
        
        return nuevoUsuario;
    }

    /**
     * Verifica si un usuario es LÍDER en un grupo específico.
     */
    @Transactional(readOnly = true)
    public boolean esLiderEnGrupo(User user, Grupo grupo) {
        if (user == null || grupo == null) {
            return false;
        }
        return user.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(grupo.getNombre()) && ug.getRol() == Role.ROLE_LIDER);
    }

    /**
     * Verifica si un usuario pertenece a un grupo específico.
     */
    @Transactional(readOnly = true)
    public boolean perteneceAGrupo(User user, Grupo grupo) {
        if (user == null || grupo == null) {
            return false;
        }
        return user.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(grupo.getNombre()));
    }

    /**
     * Verifica si un usuario tiene el rol QC en un grupo específico.
     */
    @Transactional(readOnly = true)
    public boolean esQcEnGrupo(User user, Grupo grupo) {
        if (user == null || grupo == null) {
            return false;
        }
        final String nombreGrupoTarget = grupo.getNombre();
        return user.getUsuarioGrupos().stream()
                .anyMatch(ug -> ug.getGrupo().getNombre().equals(nombreGrupoTarget) && ug.getRol() == Role.ROLE_QC);
    }

    /**
     * Cuenta el número de LIDERES en un grupo específico.
     */
    @Transactional(readOnly = true)
    public long countLeadersInGroup(Grupo grupo) {
        return usuarioGrupoRepository.countByGrupoAndRol(grupo, Role.ROLE_LIDER);
    }
}
