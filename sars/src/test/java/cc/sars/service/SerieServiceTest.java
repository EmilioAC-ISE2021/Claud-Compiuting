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

    // --- Define los usuarios simulados para los tests de estado ---
    private User usuarioSimuladoUser;
    private User usuarioSimuladoLider;
    private User usuarioSimuladoQC;

    @BeforeEach
    void setUp() {
        // Estos usuarios se usarán en los tests que necesiten autenticación
        usuarioSimuladoUser = new User("usuarioPrueba", "pass", Role.ROLE_USER);
        usuarioSimuladoLider = new User("liderPrueba", "pass", Role.ROLE_LIDER);
        usuarioSimuladoQC = new User("qcPrueba", "pass", Role.ROLE_QC);
    }

    /**
     * Prueba que el servicio puede obtener las series de un grupo específico.
     */
    @Test
    void testGetSeriesPorGrupo() {
        // ARRANGE
        Grupo grupoMock = mock(Grupo.class);
        when(grupoMock.getSeries()).thenReturn(List.of(new Serie("S1", ""), new Serie("S2", "")));
        when(grupoRepository.findByNombre("MiGrupo")).thenReturn(Optional.of(grupoMock));
        // ACT
        List<Serie> resultado = serieService.getSeriesPorGrupo("MiGrupo");
        // ASSERT
        assertThat(resultado).hasSize(2);
    }

    /**
     * Prueba que se puede crear una serie si se proporciona un grupo válido.
     */
    @Test
    void testCreateSerie() {
        // ARRANGE
        String nombreSerie = "Nueva Serie";
        String nombreGrupo = "MiGrupo";
        Grupo grupoMock = mock(Grupo.class);
        when(grupoRepository.findByNombre(nombreGrupo)).thenReturn(Optional.of(grupoMock));
        when(serieRepository.findByNombre(nombreSerie)).thenReturn(Optional.empty());
        // ACT
        serieService.createSerie(nombreSerie, "Desc", nombreGrupo);
        // ASSERT
        verify(grupoMock).agregarSerie(any(Serie.class));
        verify(grupoRepository).save(grupoMock);
    }

    /**
     * Prueba que se puede añadir un capítulo a una serie existente.
     */
    @Test
    void testAddCapituloToSerie() {        
        // ARRANGE
        Serie serieMock = mock(Serie.class);
        when(serieRepository.findByNombre("MiSerie")).thenReturn(Optional.of(serieMock));
        when(capituloRepository.findByNombre("NuevoCap")).thenReturn(Optional.empty());
        // ACT
        serieService.addCapituloToSerie("MiSerie", "NuevoCap");
        // ASSERT
        verify(serieMock).addCapitulo(any(Capitulo.class));
        verify(serieRepository).save(serieMock);
    }

    /**
     * Prueba que se puede añadir una tarea a un capítulo existente.
     */
    @Test
    void testAddTareaToCapitulo() {
        // ARRANGE
        Capitulo capituloMock = mock(Capitulo.class);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloMock));
        // ACT
        serieService.addTareaToCapitulo("MiCap", "NuevaTarea");
        // ASSERT
        verify(capituloMock).anyadirTarea(any(Tarea.class));
        verify(capituloRepository).save(capituloMock);
    }

    // --- TESTS DE LÓGICA DE TAREAS ---

    /**
     * Prueba que un usuario puede cambiar el estado de una tarea NO asignada.
     */
    @Test
    void testUpdateTareaEstado_Simple() {
        // ARRANGE
        Tarea tareaReal = new Tarea("Tarea 1"); // Asignado a "NADIE"
        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // ACT
        // Llamamos con el 4º argumento (el usuario)
        serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Completado, usuarioSimuladoUser);

        // ASSERT
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Completado);
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba la regla: Si el estado cambia a "Asignado", el usuario se auto-asigna.
     */
    @Test
    void testUpdateTareaEstado_AsignacionAutomatica() {
        // ARRANGE
        Tarea tareaReal = new Tarea("Tarea 1");
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo("NADIE");
        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // ACT
        serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Asignado, usuarioSimuladoUser);

        // ASSERT
        // El estado de la tarea cambió
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Asignado);
        // Y el usuario asignado es el usuario que hizo el cambio
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo(usuarioSimuladoUser.getUsername());
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba la regla: Si la tarea está asignada a OTRO, lanza excepción.
     */
    @Test
    void testUpdateTareaEstado_Bloqueo() {
        // ARRANGE
        Tarea tareaBloqueada = new Tarea("Tarea 1");
        tareaBloqueada.setUsuarioAsignado("OtroUsuario"); // La tarea ya es de alguien

        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaBloqueada);
        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // ACT & ASSERT
        // Verificamos que si 'usuarioSimuladoUser' (que es "usuarioPrueba")
        // intenta cambiar la tarea de "OtroUsuario", el servicio lanza la excepción.
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Completado, usuarioSimuladoUser);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No puedes cambiar el estado de una tarea asignada a OtroUsuario.");

        // Verificamos que NUNCA se guardó
        verify(capituloRepository, never()).save(capituloReal);
    }

    /**
     * Prueba que un usuario QC puede marcar una tarea "Completado" como "Repetir".
     */
    @Test
    void testQcCanMarkCompletedTaskAsRepetir() {
        // ARRANGE
        String capituloNombre = "CapituloQC4";
        String tareaNombre = "TareaQC4";
        String otroUsuario = "OtroUsuario";

        Tarea tareaReal = new Tarea(tareaNombre);
        tareaReal.setEstadoTarea(EstadosTareas.Completado);
        tareaReal.setUsuarioAsignado(otroUsuario);

        Capitulo capituloReal = new Capitulo(capituloNombre);
        capituloReal.anyadirTarea(tareaReal);

        when(capituloRepository.findByNombre(capituloNombre)).thenReturn(Optional.of(capituloReal));
        when(capituloRepository.save(any(Capitulo.class))).thenReturn(capituloReal); // Mock save operation

        // ACT
        serieService.updateTareaEstado(capituloNombre, tareaNombre, EstadosTareas.Repetir, usuarioSimuladoQC);

        // ASSERT
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Repetir);
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo("NADIE"); // Should be unassigned
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba que un usuario QC no puede cambiar el estado de una tarea asignada a otro usuario,
     * excepto si es de "Completado" a "Repetir".
     */
    @Test
    void testQcCannotChangeStatusOfTaskAssignedToAnotherUserExceptRepetir() {
        // ARRANGE
        String capituloNombre = "CapituloQC5";
        String tareaNombre = "TareaQC5";
        String otroUsuario = "OtroUsuario";

        Tarea tareaReal = new Tarea(tareaNombre);
        tareaReal.setEstadoTarea(EstadosTareas.Asignado); // Not "Completado"
        tareaReal.setUsuarioAsignado(otroUsuario);

        Capitulo capituloReal = new Capitulo(capituloNombre);
        capituloReal.anyadirTarea(tareaReal);

        when(capituloRepository.findByNombre(capituloNombre)).thenReturn(Optional.of(capituloReal));

        // ACT & ASSERT
        // Try to change to Completado
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado(capituloNombre, tareaNombre, EstadosTareas.Completado, usuarioSimuladoQC);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No puedes cambiar el estado de una tarea asignada a " + otroUsuario);

        // Try to change to NoAsignado
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado(capituloNombre, tareaNombre, EstadosTareas.NoAsignado, usuarioSimuladoQC);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No puedes cambiar el estado de una tarea asignada a " + otroUsuario);

        verify(capituloRepository, never()).save(any(Capitulo.class)); // No save should happen
    }

    /**
     * Prueba que un usuario QC no puede cambiar el estado de ninguna tarea si la tarea "CC" de ese capítulo está "Completado".
     */
    @Test
    void testQcCannotChangeStatusIfCCTaskIsCompleted() {
        // ARRANGE
        String capituloNombre = "CapituloQC6";
        String tareaNormalNombre = "TareaNormalQC6";
        String ccTareaNombre = "CC";

        Tarea ccTarea = new Tarea(ccTareaNombre);
        ccTarea.setEstadoTarea(EstadosTareas.Completado);
        ccTarea.setUsuarioAsignado(usuarioSimuladoLider.getUsername()); // LIDER completes it

        Tarea tareaNormal = new Tarea(tareaNormalNombre);
        tareaNormal.setEstadoTarea(EstadosTareas.NoAsignado);

        Capitulo capituloReal = new Capitulo(capituloNombre);
        capituloReal.anyadirTarea(ccTarea);
        capituloReal.anyadirTarea(tareaNormal);

        when(capituloRepository.findByNombre(capituloNombre)).thenReturn(Optional.of(capituloReal));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado(capituloNombre, tareaNormalNombre, EstadosTareas.Asignado, usuarioSimuladoQC);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No puedes cambiar el estado de las tareas en este capítulo porque la tarea 'CC' está completada.");

        verify(capituloRepository, never()).save(any(Capitulo.class)); // No save should happen
    }

    /**
     * Prueba que un usuario normal no puede cambiar el estado de ninguna tarea si la tarea "CC" de ese capítulo está "Completado".
     */
    @Test
    void testUserCannotChangeStatusIfCCTaskIsCompleted() {
        // ARRANGE
        String capituloNombre = "CapituloUserCC";
        String tareaNormalNombre = "TareaNormalUserCC";
        String ccTareaNombre = "CC";

        Tarea ccTarea = new Tarea(ccTareaNombre);
        ccTarea.setEstadoTarea(EstadosTareas.Completado);
        ccTarea.setUsuarioAsignado(usuarioSimuladoLider.getUsername()); // LIDER completes it

        Tarea tareaNormal = new Tarea(tareaNormalNombre);
        tareaNormal.setEstadoTarea(EstadosTareas.NoAsignado);

        Capitulo capituloReal = new Capitulo(capituloNombre);
        capituloReal.anyadirTarea(ccTarea);
        capituloReal.anyadirTarea(tareaNormal);

        when(capituloRepository.findByNombre(capituloNombre)).thenReturn(Optional.of(capituloReal));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            serieService.updateTareaEstado(capituloNombre, tareaNormalNombre, EstadosTareas.Asignado, usuarioSimuladoUser);
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No puedes cambiar el estado de las tareas en este capítulo porque la tarea 'CC' está completada.");

        verify(capituloRepository, never()).save(any(Capitulo.class)); // No save should happen
    }
}
