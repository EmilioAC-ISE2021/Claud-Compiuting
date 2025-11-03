package cc.sars.repository;

import cc.sars.model.Capitulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CapituloRepository extends JpaRepository<Capitulo, String> {

    /**
     * Busca un Capítulo por su campo 'nombre' (nuestro ID).
     */
    Optional<Capitulo> findByNombre(String nombre);

    /**
     * Este es un método muy útil que necesitaremos más adelante.
     * Spring Data JPA lo interpretará como:
     * "Busca todos los Capítulos (Set<Capitulo>) cuyo campo 'serie' (entidad)
     * tenga un campo 'nombre' que coincida con el parámetro".
     *
     * Lo usaremos para cargar la lista de capítulos en la página de "administrar serie".
     */
    Set<Capitulo> findBySerie_Nombre(String nombreSerie);
}