package cc.sars.service;

import cc.sars.model.Grupo;
import cc.sars.model.User;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        grupo.agregarUsuario(usuario); // Adds user to grupo's collection
        usuario.addGrupo(grupo);       // Adds group to user's collection (and also adds user to group's collection again)

        // Save both entities to ensure changes are persisted
        grupoRepository.saveAndFlush(grupo); // Force flush for Grupo
        userRepository.saveAndFlush(usuario); // Force flush for User
    }
}