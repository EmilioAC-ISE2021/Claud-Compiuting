package cc.sars.service;

import cc.sars.model.Grupo;
import cc.sars.model.User;
import cc.sars.model.Role; // Importar Role
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import cc.sars.repository.UsuarioGrupoRepository;
import cc.sars.model.UsuarioGrupo;
import cc.sars.model.UsuarioGrupoId;
import java.util.List;

@Service
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public GrupoService(GrupoRepository grupoRepository, UserRepository userRepository, UsuarioGrupoRepository usuarioGrupoRepository) {
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Busca un grupo por su nombre (ID).
     */
    @Transactional(readOnly = true)
    public Grupo getGrupoPorNombre(String nombreGrupo) {
        return grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new RuntimeException("No se encontró el grupo: " + nombreGrupo));
    }

    /**
     * Crea un nuevo grupo.
     */
    public Grupo crearGrupo(String nombreGrupo) {
        if (grupoRepository.findByNombre(nombreGrupo).isPresent()) {
            throw new RuntimeException("Error: El grupo con el nombre '" + nombreGrupo + "' ya existe.");
        }
        Grupo nuevoGrupo = new Grupo(nombreGrupo);
        return grupoRepository.save(nuevoGrupo);
    }

    /**
     * Añade un usuario existente a un grupo con un rol por defecto.
     */
    public void agregarUsuarioAGrupo(String nombreUsuario, String nombreGrupo) {
        agregarUsuarioAGrupo(nombreUsuario, nombreGrupo, Role.ROLE_USER);
    }

    /**
     * Añade un usuario existente (por su username) a un grupo existente (por su nombre) con un rol específico.
     */
    public void agregarUsuarioAGrupo(String nombreUsuario, String nombreGrupo, Role rol) {
        Grupo grupo = getGrupoPorNombre(nombreGrupo);
        User usuario = userRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario: " + nombreUsuario));

        // Eliminar este bloque para permitir que los ADMIN globales sean añadidos a grupos
        // if (usuario.getRole() == Role.ROLE_ADMIN) {
        //     throw new RuntimeException("Error: Los usuarios con rol ADMIN no pueden ser añadidos a grupos.");
        // }

        if (rol == Role.ROLE_ADMIN) {
            throw new RuntimeException("Error: No se puede asignar el rol ADMIN dentro de un grupo.");
        }

        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setUsuario(usuario);
        usuarioGrupo.setGrupo(grupo);
        usuarioGrupo.setRol(rol);

        usuarioGrupoRepository.save(usuarioGrupo);
    }

    @Transactional(readOnly = true)
    public List<Grupo> getAllGrupos() {
        return grupoRepository.findAll();
    }

    /**
     * Elimina un usuario de un grupo.
     */
    public void eliminarUsuarioDeGrupo(String nombreGrupo, String username) {
        Grupo grupo = getGrupoPorNombre(nombreGrupo);
        User usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario: " + username));
        
        UsuarioGrupoId usuarioGrupoId = new UsuarioGrupoId(usuario.getUsername(), grupo.getNombre());

        usuarioGrupoRepository.findById(usuarioGrupoId)
                .orElseThrow(() -> new RuntimeException("El usuario " + username + " no pertenece al grupo " + nombreGrupo));

        usuarioGrupoRepository.deleteById(usuarioGrupoId);
    }

    public void deleteGrupo(String nombreGrupo) {
        Grupo grupo = getGrupoPorNombre(nombreGrupo);
        grupoRepository.delete(grupo);
    }
}