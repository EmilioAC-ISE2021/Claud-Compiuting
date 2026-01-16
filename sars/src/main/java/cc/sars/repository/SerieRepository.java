package cc.sars.repository;

import cc.sars.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Integer> {

    /**
     * Busca una Serie por el nombre del grupo y el nombre de la serie.
     * @param grupoNombre El nombre del grupo.
     * @param nombre El nombre de la serie.
     * @return Un Optional que contiene la Serie si se encuentra.
     */
    Optional<Serie> findByGrupo_NombreAndNombre(String grupoNombre, String nombre);
}