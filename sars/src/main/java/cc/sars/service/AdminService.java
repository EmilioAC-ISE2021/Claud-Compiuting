package cc.sars.service;

import cc.sars.model.User;
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.SerieRepository;
import cc.sars.config.AdminUserInitializer;
import cc.sars.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final GrupoRepository grupoRepository;
    private final SerieRepository serieRepository;
    private final CapituloRepository capituloRepository;
    private final AdminUserInitializer adminUserInitializer;

    public AdminService(UserRepository userRepository, GrupoRepository grupoRepository,
                        SerieRepository serieRepository, CapituloRepository capituloRepository,
                        AdminUserInitializer adminUserInitializer) {
        this.userRepository = userRepository;
        this.grupoRepository = grupoRepository;
        this.serieRepository = serieRepository;
        this.capituloRepository = capituloRepository;
        this.adminUserInitializer = adminUserInitializer;
    }

    @Transactional
    public void resetDatabase() {
        capituloRepository.deleteAll();
        serieRepository.deleteAll();
        grupoRepository.deleteAll();
        userRepository.deleteAll();

        adminUserInitializer.initializeAdminUser();
    }
}
