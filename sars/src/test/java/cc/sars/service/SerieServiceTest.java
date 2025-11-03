package cc.sars.service;

import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.SerieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test unitario (rápido) para la lógica de negocio en SerieService.
 * Simula los repositorios usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class SerieServiceTest {

    @Mock
    private SerieRepository serieRepository;

    @Mock
    private CapituloRepository capituloRepository;

    @InjectMocks
    private SerieService serieService;

    /**
     * Prueba que se puede crear una serie si el nombre es único.
     */
    @Test
    void createSerie_shouldSaveNewSerie_whenNombreIsUnique() {
        // --- ARRANGE ---
        String nombre = "Nueva Serie";
        String desc = "Descripción";
        Serie serieAGuardar = new Serie(nombre, desc);

        when(serieRepository.findByNombre(nombre)).thenReturn(Optional.empty());
        when(serieRepository.save(any(Serie.class))).thenReturn(serieAGuardar);

        // --- ACT ---
        Serie resultado = serieService.createSerie(nombre, desc);

        // --- ASSERT ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo(nombre);
        verify(serieRepository).save(any(Serie.class));
    }

    /**
     * Prueba que lanza una excepción si la serie ya existe.
     */
    @Test
    void createSerie_shouldThrowException_whenSerieExists() {
        // --- ARRANGE ---
        String nombreExistente = "Serie Existente";
        Serie serieExistente = new Serie(nombreExistente, "");
        when(serieRepository.findByNombre(nombreExistente)).thenReturn(Optional.of(serieExistente));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> {
            serieService.createSerie(nombreExistente, "...");
        })
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("ya existe");

        verify(serieRepository, never()).save(any(Serie.class));
    }

    /**
     * Prueba que se puede añadir un capítulo a una serie existente.
     */
    @Test
    void addCapituloToSerie_shouldAddCapitulo_whenSerieExists() {
        // --- ARRANGE ---
        Serie serie = new Serie("Mi Serie", "Desc");
        String nuevoCapituloNombre = "Capitulo 1";
        when(serieRepository.findByNombre("Mi Serie")).thenReturn(Optional.of(serie));
        when(capituloRepository.findByNombre(nuevoCapituloNombre)).thenReturn(Optional.empty());

        // --- ACT ---
        serieService.addCapituloToSerie("Mi Serie", nuevoCapituloNombre);

        // --- ASSERT ---
        assertThat(serie.getCapitulos()).hasSize(1);
        assertThat(serie.getCapitulos().get(0).getNombre()).isEqualTo(nuevoCapituloNombre);
        verify(serieRepository).save(serie);
    }

    /**
     * Prueba que se puede actualizar el estado de una tarea.
     */
    @Test
    void updateTareaEstado_shouldUpdateState_whenCapituloAndTareaExist() {
        // --- ARRANGE ---
        Tarea tarea = new Tarea("Hacer Guion");
        Capitulo capitulo = new Capitulo("Cap 1");
        capitulo.anyadirTarea(tarea);
        when(capituloRepository.findByNombre("Cap 1")).thenReturn(Optional.of(capitulo));

        // --- ACT ---
        serieService.updateTareaEstado("Cap 1", "Hacer Guion", EstadosTareas.Completado);

        // --- ASSERT ---
        assertThat(capitulo.getTareas().get(0).getEstadoTarea()).isEqualTo(EstadosTareas.Completado);
        verify(capituloRepository).save(capitulo);
    }

    /**
     * Prueba que se puede actualizar el usuario asignado a una tarea.
     */
    @Test
    void updateTareaUsuario_shouldUpdateUsuario_whenCapituloAndTareaExist() {
        // --- ARRANGE ---
        Tarea tarea = new Tarea("Hacer Guion"); // El usuario inicial es "NADIE"
        assertThat(tarea.getUsuarioAsignado()).isEqualTo("NADIE"); // Verificación inicial

        Capitulo capitulo = new Capitulo("Cap 1");
        capitulo.anyadirTarea(tarea);
        
        when(capituloRepository.findByNombre("Cap 1")).thenReturn(Optional.of(capitulo));

        // --- ACT ---
        // Llamamos al nuevo método del servicio
        serieService.updateTareaUsuario("Cap 1", "Hacer Guion", "UsuarioNuevo");

        // --- ASSERT ---
        // Verificamos que el usuario cambió y que se guardó
        assertThat(capitulo.getTareas().get(0).getUsuarioAsignado()).isEqualTo("UsuarioNuevo");
        verify(capituloRepository).save(capitulo);
    }
}