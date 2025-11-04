package cc.sars.service;

import cc.sars.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Este es el único método que Spring Security necesita.
     * Carga un usuario por su 'username' desde la BD.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca en nuestro repositorio. Si no lo encuentra, lanza la excepción.
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        // Como nuestra entidad 'User' implementa 'UserDetails', podemos devolverla directamente.
    }
}