package cc.sars.repository;

import cc.sars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Spring Security necesita este método para buscar un usuario
     * por su 'username' (que es nuestro ID).
     */
    Optional<User> findByUsername(String username);
}