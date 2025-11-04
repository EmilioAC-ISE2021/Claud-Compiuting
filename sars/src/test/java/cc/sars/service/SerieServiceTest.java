package cc.sars.service;

import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Grupo;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.GrupoRepository; // 1. IMPORTAR
import cc.sars.repository.SerieRepository;
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

/**
 * Test unitario (rápido) para la lógica de negocio en SerieService.
 * Simula todos los repositorios usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class SerieServiceTest {

    @Mock
    private SerieRepository serieRepository;

    @Mock
    private CapituloRepository capituloRepository;

    @Mock
    private GrupoRepository grupoRepository; // 2. AÑADIR MOCK PARA EL NUEVO REPO

    @InjectMocks
    private SerieService serieService; // 3. SE INYECTAN LOS 3 MOCKS

    /**
     * Prueba que el servicio puede obtener las series de un grupo específico.
     */
    @Test
    void testGetSeriesPorGrupo() {
        // --- ARRANGE ---
        Serie serieMock1 = new Serie("Serie 1", "");
        Serie serieMock2 = new Serie("Serie 2", "");
        List<Serie> listaDeSeries = List.of(serieMock1, serieMock2);
        
        Grupo grupoMock = mock(Grupo.class); // Creamos un mock de Grupo
        when(grupoMock.getSeries()).thenReturn(listaDeSeries); // Le decimos qué devolver

        // Simulamos que el repo encuentra el grupo
        when(grupoRepository.findByNombre("MiGrupo")).thenReturn(Optional.of(grupoMock));

        // --- ACT ---
        List<Serie> resultado = serieService.getSeriesPorGrupo("MiGrupo");

        // --- ASSERT ---
        assertThat(resultado).hasSize(2);
        assertThat(resultado).isEqualTo(listaDeSeries);
    }

    /**
     * Prueba que se puede crear una serie si se proporciona un grupo válido.
     */
    @Test
    void testCreateSerie() {
        // --- ARRANGE ---
        String nombreSerie = "Nueva Serie";
        String descripcion = "Desc";
        String nombreGrupo = "MiGrupo";

        Grupo grupoMock = mock(Grupo.class);
        when(grupoRepository.findByNombre(nombreGrupo)).thenReturn(Optional.of(grupoMock));
        
        // Simular que la serie no existe
        when(serieRepository.findByNombre(nombreSerie)).thenReturn(Optional.empty());

        // --- ACT ---
        serieService.createSerie(nombreSerie, descripcion, nombreGrupo);

        // --- ASSERT ---
        // Verificamos que se buscó el grupo
        verify(grupoRepository).findByNombre(nombreGrupo);
        // Verificamos que se llamó al método 'agregarSerie' dentro del grupo
        verify(grupoMock).agregarSerie(any(Serie.class));
        // Verificamos que el grupo (con la nueva serie) se guardó
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
        // Verificamos que se llamó al método 'addCapitulo' de la entidad Serie
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
        // Verificamos que se llamó al método 'anyadirTarea' (el correcto) de la entidad Capitulo
        verify(capituloMock).anyadirTarea(any(Tarea.class));
        verify(capituloRepository).save(capituloMock);
    }

    /**
     * Prueba que se puede actualizar el estado de una tarea.
     */
    @Test
    void testUpdateTareaEstado() {
        // --- ARRANGE ---
        Tarea tareaReal = new Tarea("Tarea 1");
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.NoAsignado);
        
        // Usamos un capítulo real con datos reales para probar el stream()
        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);

        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // --- ACT ---
        serieService.updateTareaEstado("MiCap", "Tarea 1", EstadosTareas.Completado);

        // --- ASSERT ---
        assertThat(tareaReal.getEstadoTarea()).isEqualTo(EstadosTareas.Completado);
        verify(capituloRepository).save(capituloReal);
    }

    /**
     * Prueba que se puede actualizar el usuario de una tarea.
     */
    @Test
    void testUpdateTareaUsuario() {
        // --- ARRANGE ---
        Tarea tareaReal = new Tarea("Tarea 1");
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo("NADIE");

        Capitulo capituloReal = new Capitulo("MiCap");
        capituloReal.anyadirTarea(tareaReal);

        when(capituloRepository.findByNombre("MiCap")).thenReturn(Optional.of(capituloReal));

        // --- ACT ---
        serieService.updateTareaUsuario("MiCap", "Tarea 1", "NuevoUsuario");

        // --- ASSERT ---
        assertThat(tareaReal.getUsuarioAsignado()).isEqualTo("NuevoUsuario");
        verify(capituloRepository).save(capituloReal);
    }
}