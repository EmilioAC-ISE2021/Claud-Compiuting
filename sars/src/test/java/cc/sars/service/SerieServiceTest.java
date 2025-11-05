package cc.sars.service;

import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Grupo;
import cc.sars.model.Role; // Importar Role
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.model.User; // Importar User
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.SerieRepository;
import org.junit.jupiter.api.BeforeEach; // Importar
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SerieServiceTest {

    @Mock
    private SerieRepository serieRepository;
    @Mock
    private CapituloRepository capituloRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @InjectMocks
    private SerieService serieService;

    // --- Definimos un usuario simulado para los tests de estado ---
    private User usuarioSimulado;

    @BeforeEach
    void setUp() {
        // Este usuario se usará en los tests que necesiten autenticación
        usuarioSimulado = new User("usuarioPrueba", "pass", Role.ROLE_USER);
    }

    /**
     * Prueba que el servicio puede obtener las series de un grupo específico.
     */
    @Test
    void testGetSeriesPorGrupo() {
        // --- ARRANGE ---
        Grupo grupoMock = mock(Grupo.class);
        when(grupoMock.getSeries()).thenReturn(List.of(new Serie("S1", ""), new Serie("S2", "")));
        when(grupoRepository.findByNombre("MiGrupo")).thenReturn(Optional.of(grupoMock));
        // --- ACT ---
        List<Serie> resultado = serieService.getSeriesPorGrupo("MiGrupo");
        // --- ASSERT ---
        assertThat(resultado).hasSize(2);
    }

    /**
     * Prueba que se puede crear una serie si se proporciona un grupo válido.
     */
    @Test
    void testCreateSerie() {
        // --- ARRANGE ---
        String nombreSerie = "Nueva Serie";
        String nombreGrupo = "MiGrupo";
        Grupo grupoMock = mock(Grupo.class);
        when(grupoRepository.findByNombre(nombreGrupo)).thenReturn(Optional.of(grupoMock));
        when(serieRepository.findByNombre(nombreSerie)).thenReturn(Optional.empty());
        // --- ACT ---
        serieService.createSerie(nombreSerie, "Desc", nombreGrupo);
        // --- ASSERT ---
        verify(grupoMock).agregarSerie(any(Serie.class));
        verify(grupoRepository).save(grupoMock);
    }

    /**
     * Prueba que se puede añadir un capítulo a una serie existente.
     */
    @Test
    void testAddCapituloToSerie() {
        // --- ARRANGE ---
        Serie serieMock = mock(Serie.class);
        when(serieRepository.findByNombre("MiSerie")).thenReturn(Optional.of(serieMock));
        when(capituloRepository.findByNombre("NuevoCap")).thenReturn(Optional.empty());
        // --- ACT ---
        serieService.addCapituloToSerie("MiSerie", "NuevoCap");
        // --- ASSERT ---
        verify(serieMock).addCapitulo(any(Capitulo.class));
        verify(serieRepository).save(serieMock);
    }

    /**
     * Prueba que se puede añadir una tarea a un capítulo existente.
     */
    @Test
    void testAddTareaToCapitulo() {
        // --- ARRANGE ---
        Capitulo capituloMock = mock(Capitulo.class);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloMock));
        // --- ACT ---
        serieService.addTareaToCapitulo("MiCap", "NuevaTarea");
        // --- ASSERT ---
        verify(capituloMock).anyadirTarea(any(Tarea.class));
        verify(capituloRepository).save(capituloMock);
    }

    // --- TESTS DE LÓGICA DE TAREAS (ACTUALIZADOS) ---

    /**
     * Prueba que un usuario puede cambiar el estado de una tarea NO asignada.
     */
    @Test
    void testUpdateTareaEstado_Simple() {
        // --- ARRANGE ---
        Tarea tareaReal = new Tarea("Tarea 1"); // Asignado a "NADIE"
        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // --- ACT ---
        // Llamamos con el 4º argumento (el usuario)
        serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Completado, usuarioSimulado);

        // --- ASSERT ---
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Completado);
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba la regla: Si el estado cambia a "Asignado", el usuario se auto-asigna.
     */
    @Test
    void testUpdateTareaEstado_AsignacionAutomatica() {
        // --- ARRANGE ---
        Tarea tareaReal = new Tarea("Tarea 1");
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo("NADIE");
        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // --- ACT ---
        serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Asignado, usuarioSimulado);

        // --- ASSERT ---
        // El estado de la tarea cambió
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Asignado);
        // Y el usuario asignado es el usuario que hizo el cambio
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo(usuarioSimulado.getUsername());
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba la regla: Si la tarea está asignada a OTRO, lanza excepción.
     */
    @Test
    void testUpdateTareaEstado_Bloqueo() {
        // --- ARRANGE ---
        Tarea tareaBloqueada = new Tarea("Tarea 1");
        tareaBloqueada.setUsuarioAsignado("OtroUsuario"); // La tarea ya es de alguien

        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaBloqueada);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // --- ACT & ASSERT ---
        // Verificamos que si 'usuarioSimulado' (que es "usuarioPrueba")
        // intenta cambiar la tarea de "OtroUsuario", el servicio lanza la excepción.
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Completado, usuarioSimulado);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No puedes cambiar el estado de una tarea asignada a OtroUsuario.");

        // Verificamos que NUNCA se guardó
        verify(capituloRepository, never()).save(capituloReal);
    }
}