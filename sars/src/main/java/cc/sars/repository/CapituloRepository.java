package cc.sars.repository;

import cc.sars.model.Capitulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CapituloRepository extends JpaRepository<Capitulo, Integer> {

    @Query("SELECT c FROM Capitulo c JOIN c.serie s JOIN s.grupo g WHERE g.nombre = :nombreGrupo AND s.nombre = :nombreSerie AND c.nombre = :nombreCapitulo")
    Optional<Capitulo> findByNaturalKey(@Param("nombreGrupo") String nombreGrupo, @Param("nombreSerie") String nombreSerie, @Param("nombreCapitulo") String nombreCapitulo);
}