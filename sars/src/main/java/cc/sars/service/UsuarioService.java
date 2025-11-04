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

@Service
@Transactional
public class UsuarioService {

    private final UserRepository userRepository;
    private final GrupoRepository grupoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UserRepository userRepository, GrupoRepository grupoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.grupoRepository = grupoRepository;
        this.passwordEncoder = passwordEncoder;
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
}