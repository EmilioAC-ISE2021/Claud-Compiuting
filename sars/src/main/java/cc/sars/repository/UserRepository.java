package cc.sars.repository;

import cc.sars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Spring Security necesita este método para buscar un usuario
     * por su 'username' (que es nuestro ID).
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su nombre de usuario y carga eagermente sus grupos asociados.
     * ME ESTOY VOLVIENDO LOCO POR FAVOR FUNCIONA
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.usuarioGrupos WHERE u.username = :username")
    Optional<User> findByUsernameWithGrupos(String username);
}
