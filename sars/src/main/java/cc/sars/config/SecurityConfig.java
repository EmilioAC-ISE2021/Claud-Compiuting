package cc.sars.config;

import cc.sars.service.JpaUserDetailsService;
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

    private final JpaUserDetailsService jpaUserDetailsService;

    public SecurityConfig(JpaUserDetailsService jpaUserDetailsService) {
        this.jpaUserDetailsService = jpaUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .userDetailsService(jpaUserDetailsService)

            .authorizeHttpRequests(auth -> auth
                
                // 1. PRIMERO: Definimos las rutas públicas que CUALQUIERA puede ver.
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error", "/api/**").permitAll()
                
                // 2. LUEGO: Definimos las reglas específicas por ROL.
                //OBSOLETO!!!!!! AHORA HAY ROLES POR GRUPO!!!
                .requestMatchers("/grupo/*/gestionar/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                
                // 3. FINALMENTE: Decimos que CUALQUIER OTRA RUTA requiere autenticación.
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login") 
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/", true) // A dónde ir después de un login exitoso
                .permitAll() // (Esto es redundante si ya está en permitAll() arriba, pero no hace daño)
            )
            
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
