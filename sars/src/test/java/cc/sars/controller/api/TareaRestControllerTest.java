package cc.sars.controller.api;

import cc.sars.config.SecurityConfig;
import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Tarea;
import cc.sars.service.SerieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TareaRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
public class TareaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SerieService serieService;

    @Test
    void getTareasByCapitulo_shouldReturnTareas() throws Exception {
        // Given
        Tarea tarea1 = new Tarea("Task 1");
        tarea1.setEstadoTarea(EstadosTareas.NoAsignado);
        tarea1.setUsuarioAsignado("NADIE");

        Tarea tarea2 = new Tarea("Task 2");
        tarea2.setEstadoTarea(EstadosTareas.Asignado);
        tarea2.setUsuarioAsignado("user1");

        Capitulo capitulo = new Capitulo("Chapter 1");
        capitulo.anyadirTarea(tarea1);
        capitulo.anyadirTarea(tarea2);

        when(serieService.getCapituloByNombre("Chapter 1")).thenReturn(Optional.of(capitulo));

        // When & Then
        mockMvc.perform(get("/api/series/Serie A/capitulos/Chapter 1/tareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Task 1")))
                .andExpect(jsonPath("$[0].estado", is("NoAsignado")))
                .andExpect(jsonPath("$[0].usuarioAsignado", is("NADIE")))
                .andExpect(jsonPath("$[1].nombre", is("Task 2")))
                .andExpect(jsonPath("$[1].estado", is("Asignado")))
                .andExpect(jsonPath("$[1].usuarioAsignado", is("user1")));
    }

    @Test
    void getTareasByCapitulo_shouldReturnNotFound_whenCapituloNotFound() throws Exception {
        // Given
        when(serieService.getCapituloByNombre("NonExistentChapter")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/series/Serie A/capitulos/NonExistentChapter/tareas"))
                .andExpect(status().isNotFound());
    }
}
