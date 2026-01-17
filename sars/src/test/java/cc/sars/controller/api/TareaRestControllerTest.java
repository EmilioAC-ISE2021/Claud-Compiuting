package cc.sars.controller.api;

import cc.sars.config.SecurityConfig;
import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.service.SerieService;
import cc.sars.service.UsuarioService;
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

import java.util.Optional;

import cc.sars.controller.api.dto.TareaCreateDTO;
import cc.sars.controller.api.dto.TareaUpdateDTO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.springframework.http.MediaType;

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
    @MockBean
    private UsuarioService usuarioService;

    private static final String TEST_GROUP = "TestGroup";
    private static final String TEST_SERIE = "SerieA";
    private Serie serie;
    private Capitulo capitulo;



    @Test
    void getTareasByCapitulo_shouldReturnTareas() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        Tarea tarea1 = new Tarea("Task 1");
        tarea1.setEstadoTarea(EstadosTareas.NoAsignado);
        tarea1.setUsuarioAsignado("NADIE");

        Tarea tarea2 = new Tarea("Task 2");
        tarea2.setEstadoTarea(EstadosTareas.Asignado);
        tarea2.setUsuarioAsignado("user1");

        capitulo.anyadirTarea(tarea1);
        capitulo.anyadirTarea(tarea2);

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas", TEST_GROUP, TEST_SERIE, "Chapter 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Task 1")))
                .andExpect(jsonPath("$[0].estadoTarea", is("NoAsignado")))
                .andExpect(jsonPath("$[0].usuarioAsignado", is("NADIE")))
                .andExpect(jsonPath("$[1].nombre", is("Task 2")))
                .andExpect(jsonPath("$[1].estadoTarea", is("Asignado")))
                .andExpect(jsonPath("$[1].usuarioAsignado", is("user1")));
    }

    @Test
    void getTareasByCapitulo_shouldReturnNotFound_whenCapituloNotFound() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas", TEST_GROUP, TEST_SERIE, "NonExistentChapter"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTareaToCapitulo_shouldCreateTarea() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        TareaCreateDTO tareaCreateDTO = new TareaCreateDTO("New Task");
        Capitulo updatedCapitulo = new Capitulo("Chapter 1");
        updatedCapitulo.anyadirTarea(new Tarea("New Task"));

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));
                when(serieService.addTareaToCapitulo(TEST_GROUP, TEST_SERIE,"Chapter 1", "New Task")).thenReturn(updatedCapitulo);

        // When & Then
        mockMvc.perform(post("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas", TEST_GROUP, TEST_SERIE, "Chapter 1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tareaCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("New Task")));
    }

    @Test
    void updateTarea_shouldUpdateTarea() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        TareaUpdateDTO tareaUpdateDTO = new TareaUpdateDTO(EstadosTareas.Completado, "user2");
        Tarea updatedTarea = new Tarea("Task 1");
        updatedTarea.setEstadoTarea(EstadosTareas.Completado);
        updatedTarea.setUsuarioAsignado("user2");

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));
                when(serieService.updateTarea(TEST_GROUP, TEST_SERIE, "Chapter 1", "Task 1", EstadosTareas.Completado, "user2")).thenReturn(updatedTarea);

        // When & Then
        mockMvc.perform(put("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas/{t}", TEST_GROUP, TEST_SERIE, "Chapter 1", "Task 1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tareaUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Task 1")))
                .andExpect(jsonPath("$.estadoTarea", is("Completado")))
                .andExpect(jsonPath("$.usuarioAsignado", is("user2")));
    }

    @Test
    void getTareaByNombre_shouldReturnTarea() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        Tarea tarea = new Tarea("Task 1");
        tarea.setEstadoTarea(EstadosTareas.NoAsignado);
        tarea.setUsuarioAsignado("NADIE");

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));
                when(serieService.getTareaByNombre(TEST_GROUP, TEST_SERIE, "Chapter 1", "Task 1")).thenReturn(Optional.of(tarea));

        // When & Then
        mockMvc.perform(get("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas/{t}", TEST_GROUP, TEST_SERIE, "Chapter 1", "Task 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Task 1")))
                .andExpect(jsonPath("$.estadoTarea", is("NoAsignado")))
                .andExpect(jsonPath("$.usuarioAsignado", is("NADIE")));
    }

    @Test
    void deleteTarea_shouldDeleteTarea() throws Exception {
        serie = new Serie(TEST_SERIE, "Description");
        capitulo = new Capitulo("Chapter 1");
        serie.addCapitulo(capitulo);
        // Given
        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, TEST_SERIE)).thenReturn(Optional.of(serie));
        // No specific setup needed for service method that returns void

        // When & Then
        mockMvc.perform(delete("/api/grupos/{g}/series/{s}/capitulos/{c}/tareas/{t}", TEST_GROUP, TEST_SERIE, "Chapter 1", "Task 1"))
                .andExpect(status().isNoContent());
    }
}
