package cc.sars.config;

import cc.sars.model.Role;
import cc.sars.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    public AdminUserInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificar si el usuario 'admin' ya existe
        if (usuarioService.findByUsername("admin").isEmpty()) {
            // Si no existe, crearlo
            System.out.println("Creando usuario administrador por defecto: admin/admin");
            usuarioService.createAdminUser("admin", "admin");
        }
    }
}
