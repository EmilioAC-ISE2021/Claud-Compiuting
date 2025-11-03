package cc.sars.repository;

import cc.sars.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerieRepository extends JpaRepository<Serie, String> {

    /**
     * Spring Data JPA creará automáticamente este método para
     * buscar una Serie por su campo 'nombre' (que es nuestro ID).
     * Es funcionalmente idéntico a findById().
     */
    Optional<Serie> findByNombre(String nombre);
}