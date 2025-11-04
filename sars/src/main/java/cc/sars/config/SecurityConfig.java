package cc.sars.config;

import cc.sars.service.JpaUserDetailsService; // Importamos nuestro servicio
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inyectamos nuestro servicio de detalles de usuario
    private final JpaUserDetailsService jpaUserDetailsService;

    public SecurityConfig(JpaUserDetailsService jpaUserDetailsService) {
        this.jpaUserDetailsService = jpaUserDetailsService;
    }

    /**
     * Define el bean para hashear contraseñas.
     * Usamos BCrypt, el estándar recomendado.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Define qué servicio de usuarios usar
            .userDetailsService(jpaUserDetailsService)
            
            // 2. Define las reglas de autorización (permisos)
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (login, registro, CSS)
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                
                // Rutas solo para LIDER (gestión de usuarios)
                .requestMatchers("/grupo/gestionar/**").hasRole("LIDER") 
                
                // Rutas solo para ADMIN (futuro)
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                
                // Todas las demás rutas (index, serie, etc.) requieren estar logueado
                .anyRequest().authenticated()
            )
            
            // 3. Define la página de login
            .formLogin(form -> form
                .loginPage("/login") // Le dice a Spring "mi página de login está en /login"
                .loginProcessingUrl("/login") // La URL a la que el formulario debe enviar (POST)
                .defaultSuccessUrl("/", true) // A dónde ir después de un login exitoso
                .permitAll() // Permite a todos ver la página de login
            )
            
            // 4. Define el logout
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // A dónde ir después de cerrar sesión
                .permitAll()
            );

        return http.build();
    }
}