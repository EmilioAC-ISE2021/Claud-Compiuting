package cc.sars.repository;

import cc.sars.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, String> {

    /**
     * Busca un Grupo por su 'nombre' (que es nuestro ID).
     */
    Optional<Grupo> findByNombre(String nombre);
}