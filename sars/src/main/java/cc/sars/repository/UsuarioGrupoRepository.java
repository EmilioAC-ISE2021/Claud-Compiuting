package cc.sars.repository;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.UsuarioGrupo;
import cc.sars.model.UsuarioGrupoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, UsuarioGrupoId> {
    long countByGrupoAndRol(Grupo grupo, Role rol);
}