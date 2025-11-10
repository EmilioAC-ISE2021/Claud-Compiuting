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
import java.util.List;

@Service
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;

    public GrupoService(GrupoRepository grupoRepository, UserRepository userRepository) {
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
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
     * Añade un usuario existente (por su username) a un grupo existente (por su nombre).
     */
    public void agregarUsuarioAGrupo(String nombreUsuario, String nombreGrupo) {
        Grupo grupo = getGrupoPorNombre(nombreGrupo);
        User usuario = userRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario: " + nombreUsuario));

        // Verificar si el usuario es ADMIN
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Error: Los usuarios con rol ADMIN no pueden ser añadidos a grupos.");
        }

        grupo.agregarUsuario(usuario); // Añade el usuario a la colección del grupo
        usuario.addGrupo(grupo);       // Añade el grupo a la colección del usuario (y también añade el usuario a la colección del grupo de nuevo)

        // Guarda ambas entidades para asegurar que los cambios persistan
        grupoRepository.saveAndFlush(grupo); // Forzar el guardado para Grupo
        userRepository.saveAndFlush(usuario); // Forzar el guardado para Usuario
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

        if (!grupo.getUsuarios().contains(usuario)) {
            throw new RuntimeException("El usuario " + username + " no pertenece al grupo " + grupo.getNombre());
        }

        grupo.removeUsuario(usuario); // Elimina el usuario de la colección del grupo
        usuario.removeGrupo(grupo);   // Elimina el grupo de la colección del usuario

        grupoRepository.saveAndFlush(grupo);
        userRepository.saveAndFlush(usuario);
    }

    public void deleteGrupo(String nombreGrupo) {
        Grupo grupo = getGrupoPorNombre(nombreGrupo);

        // Crear una copia del conjunto para evitar ConcurrentModificationException
        Set<User> usuarios = new HashSet<>(grupo.getUsuarios());

        // Eliminar todos los usuarios del grupo y restablecer sus roles
        for (User usuario : usuarios) {
            if (usuario != null) {
                usuario.removeGrupo(grupo);
                usuario.setRole(Role.ROLE_USER); // Restablecer rol a ROLE_USER
                userRepository.save(usuario); // Guardar el usuario actualizado
            }
        }

        grupoRepository.delete(grupo);
    }
}