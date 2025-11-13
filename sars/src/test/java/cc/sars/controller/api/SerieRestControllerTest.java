package cc.sars.controller.api;

import cc.sars.config.SecurityConfig;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import cc.sars.controller.api.dto.SerieCreateDTO;
import cc.sars.controller.api.dto.SerieUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@WebMvcTest(controllers = SerieRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilita los filtros de seguridad para este test
public class SerieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SerieService serieService;

    private static final String TEST_GROUP = "TestGroup";

    @Test
    void getSeriesByGrupo_shouldReturnListOfSeries() throws Exception {
        // Given
        Serie serie1 = new Serie("Serie A", "Description A");
        Serie serie2 = new Serie("Serie B", "Description B");
        List<Serie> allSeries = Arrays.asList(serie1, serie2);

        when(serieService.getSeriesPorGrupo(TEST_GROUP)).thenReturn(allSeries);

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series", TEST_GROUP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreSerie", is("Serie A")))
                .andExpect(jsonPath("$[0].descripcion", is("Description A")))
                .andExpect(jsonPath("$[1].nombreSerie", is("Serie B")))
                .andExpect(jsonPath("$[1].descripcion", is("Description B")));
    }

    @Test
    void getSerieByNombre_shouldReturnSerie() throws Exception {
        // Given
        Serie serie = new Serie("Serie A", "Description A");
        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, "Serie A")).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series/{nombreSerie}", TEST_GROUP, "Serie A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreSerie", is("Serie A")))
                .andExpect(jsonPath("$.descripcion", is("Description A")));
    }

    @Test
    void getSerieByNombre_shouldReturnNotFound() throws Exception {
        // Given
        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, "NonExistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series/{nombreSerie}", TEST_GROUP, "NonExistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSerie_shouldCreateSerie() throws Exception {
        // Given
        SerieCreateDTO serieCreateDTO = new SerieCreateDTO("New Serie", "New Description", TEST_GROUP);
        Serie createdSerie = new Serie("New Serie", "New Description");

        when(serieService.createSerie("New Serie", "New Description", TEST_GROUP)).thenReturn(createdSerie);

        // When & Then
        mockMvc.perform(post("/api/grupos/{nombreGrupo}/series", TEST_GROUP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serieCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreSerie", is("New Serie")))
                .andExpect(jsonPath("$.descripcion", is("New Description")));
    }

    @Test
    void updateSerie_shouldUpdateSerie() throws Exception {
        // Given
        SerieUpdateDTO serieUpdateDTO = new SerieUpdateDTO("Updated Description");
        Serie updatedSerie = new Serie("Serie A", "Updated Description");

        when(serieService.updateSerieInGrupo(TEST_GROUP, "Serie A", "Updated Description")).thenReturn(updatedSerie);

        // When & Then
        mockMvc.perform(put("/api/grupos/{nombreGrupo}/series/{nombreSerie}", TEST_GROUP, "Serie A")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serieUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreSerie", is("Serie A")))
                .andExpect(jsonPath("$.descripcion", is("Updated Description")));
    }

    @Test
    void deleteSerie_shouldDeleteSerie() throws Exception {
        // Given
        doNothing().when(serieService).deleteSerieInGrupo(TEST_GROUP, "Serie A");

        // When & Then
        mockMvc.perform(delete("/api/grupos/{nombreGrupo}/series/{nombreSerie}", TEST_GROUP, "Serie A"))
                .andExpect(status().isNoContent());

        verify(serieService).deleteSerieInGrupo(TEST_GROUP, "Serie A");
    }
}
